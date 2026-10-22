package com.example.carmaintenance

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val PREFS = "car_maintenance"
private const val CARS_KEY = "cars"
private const val WORKS_KEY = "maintenance_works"
private const val SELECTED_CAR_KEY = "selected_car_id"

private const val BG = 0xFF17191E.toInt()
private const val PANEL = 0xFF252932.toInt()
private const val TEXT = 0xFFF2F3F5.toInt()
private const val MUTED = 0xFFADB4C0.toInt()
private const val GREEN = 0xFF42C985.toInt()
private const val RED = 0xFFFF6464.toInt()
private const val PURPLE = 0xFFBE91FF.toInt()
private const val YELLOW = 0xFFFFCF62.toInt()
private const val BLUE = 0xFF78AEFF.toInt()

data class Car(
    val id: String = UUID.randomUUID().toString(),
    var brand: String = "",
    var model: String = "",
    var year: Int = 0,
    var vin: String = "",
    var mileage: Int = 0,
    var engine: String = "",
    var fuel: String = "",
    var hasLpg: Boolean = false,
    var notes: String = ""
)

data class MaintenanceWork(
    val id: String = UUID.randomUUID().toString(),
    var carId: String = "",
    var name: String = "",
    var intervalKm: Int = 0,
    var intervalMonths: Int = 0,
    var lastMileage: Int = 0,
    var lastDate: String = "",
    var hasLastExecution: Boolean = false,
    var skipped: Boolean = false,
    var preventive: Boolean = false,
    var oilType: String = "",
    var oilName: String = ""
)

class MainActivity : Activity() {

    private val cars = mutableListOf<Car>()
    private val works = mutableListOf<MaintenanceWork>()

    private lateinit var content: LinearLayout
    private lateinit var navigation: LinearLayout

    private var selectedCarId = ""
    private var currentTab = "Главная"

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private fun today(): String =
        dateFormat.format(Date())

    private fun car(): Car? =
        cars.find { it.id == selectedCarId }

    private fun carWorks(): List<MaintenanceWork> =
        works.filter { it.carId == selectedCarId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = BG
        window.navigationBarColor = BG

        loadData()
        createLayout()
        showHome()
    }

    // =========================================================
    // СОХРАНЕНИЕ
    // =========================================================

    private fun loadData() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        selectedCarId =
            prefs.getString(SELECTED_CAR_KEY, "") ?: ""

        try {
            val array =
                JSONArray(prefs.getString(CARS_KEY, "[]"))

            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)

                cars.add(
                    Car(
                        id = o.optString(
                            "id",
                            UUID.randomUUID().toString()
                        ),
                        brand = o.optString("brand"),
                        model = o.optString("model"),
                        year = o.optInt("year"),
                        vin = o.optString("vin"),
                        mileage = o.optInt("mileage"),
                        engine = o.optString("engine"),
                        fuel = o.optString("fuel"),
                        hasLpg = o.optBoolean("hasLpg"),
                        notes = o.optString("notes")
                    )
                )
            }
        } catch (_: Exception) {
        }

        try {
            val array =
                JSONArray(prefs.getString(WORKS_KEY, "[]"))

            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)

                works.add(
                    MaintenanceWork(
                        id = o.optString(
                            "id",
                            UUID.randomUUID().toString()
                        ),
                        carId = o.optString("carId"),
                        name = o.optString("name"),
                        intervalKm = o.optInt("intervalKm"),
                        intervalMonths = o.optInt("intervalMonths"),
                        lastMileage = o.optInt("lastMileage"),
                        lastDate = o.optString("lastDate"),
                        hasLastExecution =
                            o.optBoolean("hasLastExecution"),
                        skipped = o.optBoolean("skipped"),
                        preventive = o.optBoolean("preventive"),
                        oilType = o.optString("oilType"),
                        oilName = o.optString("oilName")
                    )
                )
            }
        } catch (_: Exception) {
        }

        if (cars.none { it.id == selectedCarId }) {
            selectedCarId =
                cars.firstOrNull()?.id ?: ""
        }
    }

    private fun saveData() {

        val carArray = JSONArray()

        cars.forEach { c ->
            carArray.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("brand", c.brand)
                    put("model", c.model)
                    put("year", c.year)
                    put("vin", c.vin)
                    put("mileage", c.mileage)
                    put("engine", c.engine)
                    put("fuel", c.fuel)
                    put("hasLpg", c.hasLpg)
                    put("notes", c.notes)
                }
            )
        }

        val workArray = JSONArray()

        works.forEach { w ->
            workArray.put(
                JSONObject().apply {
                    put("id", w.id)
                    put("carId", w.carId)
                    put("name", w.name)
                    put("intervalKm", w.intervalKm)
                    put("intervalMonths", w.intervalMonths)
                    put("lastMileage", w.lastMileage)
                    put("lastDate", w.lastDate)
                    put(
                        "hasLastExecution",
                        w.hasLastExecution
                    )
                    put("skipped", w.skipped)
                    put("preventive", w.preventive)
                    put("oilType", w.oilType)
                    put("oilName", w.oilName)
                }
            )
        }

        getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                CARS_KEY,
                carArray.toString()
            )
            .putString(
                WORKS_KEY,
                workArray.toString()
            )
            .putString(
                SELECTED_CAR_KEY,
                selectedCarId
            )
            .apply()
    }

    // =========================================================
    // ОСНОВНОЙ ИНТЕРФЕЙС
    // =========================================================

    private fun createLayout() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 14, 20, 10)
        }

        header.addView(
            label(
                "АВТОМОБИЛЬ • ТО",
                12,
                MUTED,
                true
            )
        )

        header.addView(
            label(
                "Мои автомобили",
                25,
                TEXT,
                true
            )
        )

        root.addView(header)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 8, 16, 16)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                0,
                0,
                1f
            )
        )

        navigation = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(4, 6, 4, 6)
            setBackgroundColor(PANEL)
        }

        root.addView(navigation)

        setContentView(root)

        updateNavigation()
    }

    private fun updateNavigation() {

        navigation.removeAllViews()

        val tabs = listOf(
            "Главная",
            "Перечень",
            "Регламент",
            "Статистика",
            "Ещё"
        )

        tabs.forEach { tab ->

            val item = TextView(this).apply {

                text =
                    if (tab == currentTab) {
                        "●\n$tab"
                    } else {
                        tab
                    }

                gravity = Gravity.CENTER
                textSize = 10f

                setTextColor(
                    if (tab == currentTab) {
                        GREEN
                    } else {
                        MUTED
                    }
                )

                setPadding(2, 8, 2, 8)

                setOnClickListener {

                    currentTab = tab
                    updateNavigation()

                    when (tab) {

                        "Главная" -> showHome()

                        "Перечень" -> showWorks()

                        "Регламент" -> showRegulations()

                        "Статистика" -> showStatistics()

                        "Ещё" -> showMore()
                    }
                }
            }

            navigation.addView(
                item,
                LinearLayout.LayoutParams(
                    0,
                    -2,
                    1f
                )
            )
        }
    }

    private fun render(
        title: String,
        block: LinearLayout.() -> Unit
    ) {

        content.removeAllViews()

        content.addView(
            label(
                title,
                23,
                TEXT,
                true
            )
        )

        content.addView(space(12))

        content.block()
    }

    private fun label(
        value: String,
        size: Int = 15,
        color: Int = TEXT,
        bold: Boolean = false
    ): TextView {

        return TextView(this).apply {

            text = value
            textSize = size.toFloat()
            setTextColor(color)

            if (bold) {
                setTypeface(null, Typeface.BOLD)
            }

            setPadding(0, 3, 0, 3)
        }
    }

    private fun space(height: Int): View =
        View(this).apply {
            layoutParams =
                LinearLayout.LayoutParams(
                    1,
                    height
                )
        }

    private fun card(): LinearLayout =
        LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                16,
                14,
                16,
                14
            )

            background =
                GradientDrawable().apply {
                    setColor(PANEL)
                    cornerRadius = 20f
                }
        }

    private fun addCard(
        parent: LinearLayout,
        panel: LinearLayout
    ) {

        parent.addView(
            panel,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = 12
            }
        )
    }

    private fun button(
        title: String,
        action: () -> Unit
    ): Button =
        Button(this).apply {

            text = title

            setOnClickListener {
                action()
            }
        }

    private fun addButton(
        parent: LinearLayout,
        title: String,
        action: () -> Unit
    ) {

        parent.addView(
            button(title, action),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = 5
            }
        )
    }

    private fun editText(
        hintText: String,
        value: String = "",
        number: Boolean = false,
        multiline: Boolean = false
    ): EditText {

        return EditText(this).apply {

            hint = hintText
            setText(value)

            setTextColor(TEXT)
            setHintTextColor(MUTED)

            textSize = 15f

            if (number) {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER
            }

            if (multiline) {

                minLines = 2
                maxLines = 4
                gravity = Gravity.TOP

            } else {

                setSingleLine(true)
            }
        }
    }

    // =========================================================
    // ГЛАВНАЯ
    // =========================================================

    private fun showHome() {

        render("Главная") {

            val selected = car()

            if (selected == null) {

                val welcome = card()

                welcome.addView(
                    label(
                        "Добро пожаловать!",
                        21,
                        TEXT,
                        true
                    )
                )

                welcome.addView(
                    label(
                        "Добавьте автомобиль, чтобы начать вести учёт ТО.",
                        14,
                        MUTED
                    )
                )

                addCard(this, welcome)

                addButton(
                    this,
                    "＋ Добавить автомобиль"
                ) {
                    editCar(null)
                }

                return@render
            }

            val vehicle = card()

            vehicle.addView(
                label(
                    "${selected.brand} ${selected.model}",
                    22,
                    TEXT,
                    true
                )
            )

            vehicle.addView(
                label(
                    "${selected.year} год • ${
                        selected.engine.ifBlank {
                            "Двигатель не указан"
                        }
                    }",
                    14,
                    MUTED
                )
            )

            vehicle.addView(space(8))

            vehicle.addView(
                label(
                    "Пробег: ${selected.mileage} км",
                    18,
                    GREEN,
                    true
                )
            )

            if (selected.vin.isNotBlank()) {

                vehicle.addView(
                    label(
                        "VIN: ${selected.vin}",
                        13,
                        MUTED
                    )
                )
            }

            vehicle.addView(
                label(
                    "Топливо: ${
                        selected.fuel.ifBlank {
                            "не указано"
                        }
                    }" +
                        if (selected.hasLpg) {
                            " • ГБО"
                        } else {
                            ""
                        },
                    13,
                    MUTED
                )
            )

            if (selected.notes.isNotBlank()) {

                vehicle.addView(
                    label(
                        selected.notes,
                        13,
                        MUTED
                    )
                )
            }

            addCard(this, vehicle)

            val list = carWorks()

            val overdue =
                list.count {
                    status(it) == "Выполнить"
                }

            val normal =
                list.count {
                    status(it) == "В норме"
                }

            val skipped =
                list.count {
                    status(it) == "Пропущено"
                }

            val preventive =
                list.count {
                    status(it) == "Профилактика"
                }

            val summary = card()

            summary.addView(
                label(
                    "Обслуживание",
                    19,
                    TEXT,
                    true
                )
            )

            summary.addView(
                label(
                    "Всего работ: ${list.size}",
                    14,
                    MUTED
                )
            )

            summary.addView(space(8))

            addStatus(
                summary,
                "Требуют выполнения",
                overdue,
                RED
            )

            addStatus(
                summary,
                "Срок не наступил",
                normal,
                GREEN
            )

            addStatus(
                summary,
                "Пропущено",
                skipped,
                PURPLE
            )

            addStatus(
                summary,
                "Профилактика",
                preventive,
                YELLOW
            )

            addCard(this, summary)

            addButton(
                this,
                "＋ Добавить работу"
            ) {
                editWork(null)
            }

            addButton(
                this,
                "Открыть перечень работ"
            ) {

                currentTab = "Перечень"
                updateNavigation()
                showWorks()
            }

            addButton(
                this,
                "Изменить автомобиль"
            ) {
                editCar(selected)
            }

            addButton(
                this,
                "Выбрать автомобиль"
            ) {
                chooseCar()
            }
        }
    }

    private fun addStatus(
        parent: LinearLayout,
        title: String,
        count: Int,
        color: Int
    ) {

        val row =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    12,
                    9,
                    12,
                    9
                )

                background =
                    GradientDrawable().apply {
                        setColor(
                            0xFF303641.toInt()
                        )
                        cornerRadius = 12f
                    }
            }

        row.addView(
            label(
                title,
                14,
                TEXT
            ),
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        row.addView(
            label(
                count.toString(),
                19,
                color,
                true
            )
        )

        parent.addView(
            row,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = 6
            }
        )
    }

    // =========================================================
    // СТАТУСЫ
    // =========================================================

    private fun status(
        work: MaintenanceWork
    ): String {

        if (work.skipped) {
            return "Пропущено"
        }

        if (work.preventive) {
            return "Профилактика"
        }

        if (!work.hasLastExecution) {
            return "Выполнить"
        }

        val currentMileage =
            car()?.mileage ?: 0

        if (
            work.intervalKm > 0 &&
            currentMileage >=
            work.lastMileage + work.intervalKm
        ) {
            return "Выполнить"
        }

        if (
            work.intervalMonths > 0 &&
            work.lastDate.isNotBlank()
        ) {

            try {

                val last =
                    dateFormat.parse(
                        work.lastDate
                    )

                if (last != null) {

                    val calendar =
                        Calendar.getInstance()

                    calendar.time = last

                    calendar.add(
                        Calendar.MONTH,
                        work.intervalMonths
                    )

                    if (
                        Date().after(
                            calendar.time
                        )
                    ) {
                        return "Выполнить"
                    }
                }

            } catch (_: Exception) {
            }
        }

        return "В норме"
    }

    private fun statusColor(
        work: MaintenanceWork
    ): Int {

        return when (status(work)) {

            "Выполнить" -> RED

            "В норме" -> GREEN

            "Пропущено" -> PURPLE

            else -> YELLOW
        }
    }

    // =========================================================
    // ПЕРЕЧЕНЬ РАБОТ
    // =========================================================

    private fun showWorks() {

        render("Перечень работ") {

            val list = carWorks()

            if (list.isEmpty()) {

                val empty = card()

                empty.addView(
                    label(
                        "Работ пока нет",
                        17,
                        TEXT,
                        true
                    )
                )

                empty.addView(
                    label(
                        "Добавьте работу или выберите её из регламента.",
                        14,
                        MUTED
                    )
                )

                addCard(this, empty)
            }

            list.forEach { work ->

                val item = card()

                item.addView(
                    label(
                        work.name,
                        18,
                        TEXT,
                        true
                    )
                )

                item.addView(
                    label(
                        status(work),
                        14,
                        statusColor(work),
                        true
                    )
                )

                item.addView(
                    label(
                        "Интервал: " +
                            if (work.intervalKm > 0) {
                                "${work.intervalKm} км"
                            } else {
                                "не задан"
                            } +
                            " • " +
                            if (work.intervalMonths > 0) {
                                "${work.intervalMonths} мес."
                            } else {
                                "не задан"
                            },
                        13,
                        MUTED
                    )
                )

                if (work.hasLastExecution) {

                    item.addView(
                        label(
                            "Последнее выполнение: ${
                                work.lastDate.ifBlank {
                                    "не указана дата"
                                }
                            }",
                            13,
                            MUTED
                        )
                    )

                    item.addView(
                        label(
                            "Пробег: ${work.lastMileage} км",
                            13,
                            MUTED
                        )
                    )
                }

                if (
                    work.oilType.isNotBlank() ||
                    work.oilName.isNotBlank()
                ) {

                    item.addView(
                        label(
                            "Тип масла: ${
                                work.oilType.ifBlank {
                                    "не указан"
                                }
                            }",
                            13,
                            BLUE
                        )
                    )

                    item.addView(
                        label(
                            "Название масла: ${
                                work.oilName.ifBlank {
                                    "не указано"
                                }
                            }",
                            13,
                            BLUE
                        )
                    )
                }

                addButton(
                    item,
                    "Отметить выполнение"
                ) {
                    recordExecution(work)
                }

                addButton(
                    item,
                    "Изменить работу"
                ) {
                    editWork(work)
                }

                addButton(
                    item,
                    "Отметить пропущенной"
                ) {

                    work.skipped = true
                    work.preventive = false

                    saveData()
                    showWorks()
                }

                addButton(
                    item,
                    "Снять отметку «пропущено»"
                ) {

                    work.skipped = false

                    saveData()
                    showWorks()
                }

                addButton(
                    item,
                    "Удалить работу"
                ) {

                    AlertDialog.Builder(
                        this@MainActivity
                    )
                        .setTitle(
                            "Удалить работу?"
                        )
                        .setMessage(work.name)
                        .setNegativeButton(
                            "Отмена",
                            null
                        )
                        .setPositiveButton(
                            "Удалить"
                        ) { _, _ ->

                            works.remove(work)

                            saveData()
                            showWorks()
                        }
                        .show()
                }

                addCard(this, item)
            }

            addButton(
                this,
                "＋ Добавить работу"
            ) {
                editWork(null)
            }
        }
    }

    // =========================================================
    // ВЫПОЛНЕНИЕ РАБОТЫ
    // =========================================================

    private fun recordExecution(
        work: MaintenanceWork
    ) {

        val mileageInput =
            editText(
                "Пробег при выполнении, км",
                (car()?.mileage ?: 0).toString(),
                number = true
            )

        val dateInput =
            editText(
                "Дата выполнения",
                today()
            )

        val form =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    24,
                    8,
                    24,
                    0
                )

                addView(mileageInput)
                addView(dateInput)
            }

        dateInput.setOnClickListener {

            val calendar =
                Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    dateInput.setText(
                        String.format(
                            Locale.getDefault(),
                            "%02d.%02d.%04d",
                            day,
                            month + 1,
                            year
                        )
                    )
                },
                calendar.get(
                    Calendar.YEAR
                ),
                calendar.get(
                    Calendar.MONTH
                ),
                calendar.get(
                    Calendar.DAY_OF_MONTH
                )
            ).show()
        }

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    "Выполнение работы"
                )
                .setView(form)
                .setNegativeButton(
                    "Отмена",
                    null
                )
                .setPositiveButton(
                    "Сохранить",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val mileage =
                    mileageInput.text
                        .toString()
                        .toIntOrNull()

                val date =
                    dateInput.text
                        .toString()
                        .trim()

                if (
                    mileage == null ||
                    mileage < 0
                ) {

                    Toast.makeText(
                        this,
                        "Укажите корректный пробег",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                if (
                    dateFormat.parseOrNull(date) == null
                ) {

                    Toast.makeText(
                        this,
                        "Укажите дату в формате дд.мм.гггг",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                work.lastMileage = mileage
                work.lastDate = date
                work.hasLastExecution = true
                work.skipped = false
                work.preventive = false

                car()?.let {

                    if (mileage > it.mileage) {
                        it.mileage = mileage
                    }
                }

                saveData()

                dialog.dismiss()

                showWorks()
            }
        }

        dialog.show()
    }

    private fun SimpleDateFormat.parseOrNull(
        value: String
    ): Date? {

        return try {

            isLenient = false

            parse(value)

        } catch (_: Exception) {

            null
        }
    }

    // =========================================================
    // РЕГЛАМЕНТ
    // =========================================================

    private fun showRegulations() {

        render("Регламент ТО") {

            val info = card()

            info.addView(
                label(
                    "Регламент выбранного автомобиля",
                    17,
                    TEXT,
                    true
                )
            )

            info.addView(
                label(
                    "Добавляйте типовые работы и настраивайте интервалы отдельно для автомобиля.",
                    14,
                    MUTED
                )
            )

            addCard(this, info)

            val templates =
                listOf(
                    Triple(
                        "Замена моторного масла",
                        10000,
                        12
                    ),
                    Triple(
                        "Замена масляного фильтра",
                        10000,
                        12
                    ),
                    Triple(
                        "Замена воздушного фильтра",
                        15000,
                        12
                    ),
                    Triple(
                        "Замена салонного фильтра",
                        15000,
                        12
                    ),
                    Triple(
                        "Проверка тормозной системы",
                        10000,
                        12
                    ),
                    Triple(
                        "Замена тормозной жидкости",
                        0,
                        24
                    ),
                    Triple(
                        "Проверка охлаждающей жидкости",
                        10000,
                        12
                    ),
                    Triple(
                        "Проверка свечей зажигания",
                        30000,
                        24
                    ),
                    Triple(
                        "Обслуживание газового оборудования (ГБО)",
                        10000,
                        12
                    )
                )

            templates.forEach { template ->

                val item = card()

                item.addView(
                    label(
                        template.first,
                        16,
                        TEXT,
                        true
                    )
                )

                item.addView(
                    label(
                        "Интервал: " +
                            if (template.second > 0) {
                                "${template.second} км"
                            } else {
                                "по сроку"
                            } +
                            " / ${template.third} мес.",
                        13,
                        MUTED
                    )
                )

                addButton(
                    item,
                    "Добавить в регламент"
                ) {

                    if (car() == null) {

                        Toast.makeText(
                            this@MainActivity,
                            "Сначала добавьте автомобиль",
                            Toast.LENGTH_LONG
                        ).show()

                    } else if (
                        carWorks().any {
                            it.name == template.first
                        }
                    ) {

                        Toast.makeText(
                            this@MainActivity,
                            "Такая работа уже есть",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        works.add(
                            MaintenanceWork(
                                carId = selectedCarId,
                                name = template.first,
                                intervalKm =
                                    template.second,
                                intervalMonths =
                                    template.third
                            )
                        )

                        saveData()

                        Toast.makeText(
                            this@MainActivity,
                            "Работа добавлена",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                addCard(this, item)
            }

            addButton(
                this,
                "＋ Создать свою работу"
            ) {
                editWork(null)
            }
        }
    }

    // =========================================================
    // СТАТИСТИКА
    // =========================================================

    private fun showStatistics() {

        render("Статистика") {

            val list = carWorks()

            val summary = card()

            summary.addView(
                label(
                    "Сводка по автомобилю",
                    18,
                    TEXT,
                    true
                )
            )

            addStatus(
                summary,
                "Всего работ",
                list.size,
                BLUE
            )

            addStatus(
                summary,
                "Требуют выполнения",
                list.count {
                    status(it) == "Выполнить"
                },
                RED
            )

            addStatus(
                summary,
                "Срок не наступил",
                list.count {
                    status(it) == "В норме"
                },
                GREEN
            )

            addStatus(
                summary,
                "Пропущено",
                list.count {
                    status(it) == "Пропущено"
                },
                PURPLE
            )

            addStatus(
                summary,
                "Профилактика",
                list.count {
                    status(it) == "Профилактика"
                },
                YELLOW
            )

            addCard(this, summary)

            val history = card()

            history.addView(
                label(
                    "Последние записи",
                    18,
                    TEXT,
                    true
                )
            )

            val executed =
                list
                    .filter {
                        it.hasLastExecution
                    }
                    .sortedByDescending {
                        it.lastDate
                    }

            if (executed.isEmpty()) {

                history.addView(
                    label(
                        "Записей о выполнении пока нет.",
                        14,
                        MUTED
                    )
                )

            } else {

                executed.forEach { work ->

                    history.addView(
                        label(
                            "${work.lastDate} • ${work.name}",
                            14,
                            TEXT
                        )
                    )

                    history.addView(
                        label(
                            "Пробег: ${work.lastMileage} км",
                            13,
                            MUTED
                        )
                    )

                    history.addView(space(5))
                }
            }

            addCard(this, history)

            addButton(
                this,
                "Сохранить статистику в TXT"
            ) {
                exportStatistics()
            }
        }
    }

    private fun exportStatistics() {

        try {

            val selected = car()

            val lines =
                mutableListOf<String>()

            lines.add(
                "ОТЧЁТ ПО ТЕХНИЧЕСКОМУ ОБСЛУЖИВАНИЮ"
            )

            lines.add(
                "Дата отчёта: ${today()}"
            )

            lines.add("")

            if (selected != null) {

                lines.add(
                    "Автомобиль: ${selected.brand} ${selected.model}"
                )

                lines.add(
                    "Год: ${selected.year}"
                )

                lines.add(
                    "VIN: ${selected.vin}"
                )

                lines.add(
                    "Пробег: ${selected.mileage} км"
                )

                lines.add("")

                lines.add(
                    "РАБОТЫ:"
                )

                lines.add("")

                carWorks().forEach { work ->

                    lines.add(
                        "Работа: ${work.name}"
                    )

                    lines.add(
                        "Статус: ${status(work)}"
                    )

                    lines.add(
                        "Интервал: " +
                            if (work.intervalKm > 0) {
                                "${work.intervalKm} км"
                            } else {
                                "не задан"
                            } +
                            " / " +
                            if (work.intervalMonths > 0) {
                                "${work.intervalMonths} мес."
                            } else {
                                "не задан"
                            }
                    )

                    if (work.hasLastExecution) {

                        lines.add(
                            "Последнее выполнение: ${work.lastDate}"
                        )

                        lines.add(
                            "Пробег выполнения: ${work.lastMileage} км"
                        )
                    }

                    if (work.oilType.isNotBlank()) {

                        lines.add(
                            "Тип масла: ${work.oilType}"
                        )
                    }

                    if (work.oilName.isNotBlank()) {

                        lines.add(
                            "Название масла: ${work.oilName}"
                        )
                    }

                    lines.add("")
                }

            } else {

                lines.add(
                    "Автомобиль не выбран."
                )
            }

            val fileName =
                "ТО_${selected?.brand ?: "автомобиль"}_" +
                    "${selected?.model ?: ""}_" +
                    "${today().replace(".", "-")}.txt"

            val safeFileName =
                fileName.replace(
                    Regex("[\\\\/:*?\"<>|]"),
                    "_"
                )

            val file =
                File(
                    getExternalFilesDir(null),
                    safeFileName
                )

            file.writeText(
                lines.joinToString("\n"),
                Charsets.UTF_8
            )

            Toast.makeText(
                this,
                "Статистика сохранена:\n${file.name}",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Ошибка сохранения: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =========================================================
    // ЕЩЁ
    // =========================================================

    private fun showMore() {

        render("Ещё") {

            addButton(
                this,
                "＋ Добавить автомобиль"
            ) {
                editCar(null)
            }

            addButton(
                this,
                "Выбрать автомобиль"
            ) {
                chooseCar()
            }

            cars.forEach { selected ->

                val item = card()

                item.addView(
                    label(
                        "${selected.brand} ${selected.model}",
                        17,
                        TEXT,
                        true
                    )
                )

                item.addView(
                    label(
                        "${selected.year} • ${selected.mileage} км",
                        13,
                        MUTED
                    )
                )

                addButton(
                    item,
                    "Изменить"
                ) {
                    editCar(selected)
                }

                addButton(
                    item,
                    "Выбрать этот автомобиль"
                ) {

                    selectedCarId =
                        selected.id

                    saveData()

                    currentTab = "Главная"

                    updateNavigation()
                    showHome()
                }

                addButton(
                    item,
                    "Удалить автомобиль"
                ) {

                    AlertDialog.Builder(
                        this@MainActivity
                    )
                        .setTitle(
                            "Удалить автомобиль?"
                        )
                        .setMessage(
                            "Связанные с ним работы также будут удалены."
                        )
                        .setNegativeButton(
                            "Отмена",
                            null
                        )
                        .setPositiveButton(
                            "Удалить"
                        ) { _, _ ->

                            cars.remove(selected)

                            works.removeAll {
                                it.carId == selected.id
                            }

                            if (
                                selectedCarId ==
                                selected.id
                            ) {

                                selectedCarId =
                                    cars.firstOrNull()?.id
                                        ?: ""
                            }

                            saveData()

                            showMore()
                        }
                        .show()
                }

                addCard(this, item)
            }
        }
    }

    // =========================================================
    // ВЫБОР АВТОМОБИЛЯ
    // =========================================================

    private fun chooseCar() {

        if (cars.isEmpty()) {

            editCar(null)
            return
        }

        AlertDialog.Builder(this)
            .setTitle(
                "Выбрать автомобиль"
            )
            .setItems(
                cars.map {
                    "${it.brand} ${it.model} (${it.year})"
                }.toTypedArray()
            ) { _, which ->

                selectedCarId =
                    cars[which].id

                saveData()

                currentTab = "Главная"

                updateNavigation()
                showHome()
            }
            .setNegativeButton(
                "Отмена",
                null
            )
            .show()
    }

    // =========================================================
    // АВТОМОБИЛЬ
    // =========================================================

    private fun editCar(
        existing: Car?
    ) {

        val isNew =
            existing == null

        val target =
            existing ?: Car()

        val brand =
            editText(
                "Марка",
                target.brand
            )

        val model =
            editText(
                "Модель",
                target.model
            )

        val year =
            editText(
                "Год выпуска",
                if (target.year == 0) {
                    ""
                } else {
                    target.year.toString()
                },
                number = true
            )

        val vin =
            editText(
                "VIN-код автомобиля",
                target.vin
            )

        val mileage =
            editText(
                "Текущий пробег, км",
                target.mileage.toString(),
                number = true
            )

        val engine =
            editText(
                "Двигатель / объём",
                target.engine
            )

        val fuel =
            editText(
                "Топливо",
                target.fuel
            )

        val notes =
            editText(
                "Примечания",
                target.notes,
                multiline = true
            )

        val lpg =
            CheckBox(this).apply {

                text =
                    "Установлено газовое оборудование (ГБО)"

                setTextColor(TEXT)

                isChecked =
                    target.hasLpg
            }

        val form =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    24,
                    8,
                    24,
                    0
                )

                addView(brand)
                addView(model)
                addView(year)
                addView(vin)
                addView(mileage)
                addView(engine)
                addView(fuel)
                addView(lpg)
                addView(notes)
            }

        val scroll =
            ScrollView(this).apply {
                addView(form)
            }

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    if (isNew) {
                        "Новый автомобиль"
                    } else {
                        "Изменить автомобиль"
                    }
                )
                .setView(scroll)
                .setNegativeButton(
                    "Отмена",
                    null
                )
                .setPositiveButton(
                    "Сохранить",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val brandValue =
                    brand.text
                        .toString()
                        .trim()

                val modelValue =
                    model.text
                        .toString()
                        .trim()

                val mileageValue =
                    mileage.text
                        .toString()
                        .toIntOrNull()

                val yearValue =
                    year.text
                        .toString()
                        .toIntOrNull()

                if (
                    brandValue.isBlank() ||
                    modelValue.isBlank()
                ) {

                    Toast.makeText(
                        this,
                        "Укажите марку и модель",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                if (
                    mileageValue == null ||
                    mileageValue < 0
                ) {

                    Toast.makeText(
                        this,
                        "Проверьте пробег",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                target.brand =
                    brandValue

                target.model =
                    modelValue

                target.year =
                    yearValue ?: 0

                target.vin =
                    vin.text
                        .toString()
                        .trim()

                target.mileage =
                    mileageValue

                target.engine =
                    engine.text
                        .toString()
                        .trim()

                target.fuel =
                    fuel.text
                        .toString()
                        .trim()

                target.hasLpg =
                    lpg.isChecked

                target.notes =
                    notes.text
                        .toString()
                        .trim()

                if (isNew) {

                    cars.add(target)

                    selectedCarId =
                        target.id
                }

                saveData()

                dialog.dismiss()

                showHome()
            }
        }

        dialog.show()
    }

    // =========================================================
    // РАБОТА
    // =========================================================

    private fun editWork(
        existing: MaintenanceWork?
    ) {

        if (car() == null) {

            Toast.makeText(
                this,
                "Сначала добавьте автомобиль",
                Toast.LENGTH_LONG
            ).show()

            editCar(null)

            return
        }

        val isNew =
            existing == null

        val target =
            existing ?: MaintenanceWork(
                carId = selectedCarId
            )

        val name =
            editText(
                "Название работы",
                target.name
            )

        val intervalKm =
            editText(
                "Интервал в километрах (0 — не задан)",
                if (target.intervalKm == 0) {
                    ""
                } else {
                    target.intervalKm.toString()
                },
                number = true
            )

        val intervalMonths =
            editText(
                "Интервал в месяцах (0 — не задан)",
                if (target.intervalMonths == 0) {
                    ""
                } else {
                    target.intervalMonths.toString()
                },
                number = true
            )

        val oilType =
            editText(
                "Тип масла",
                target.oilType
            )

        val oilName =
            editText(
                "Название / марка масла",
                target.oilName
            )

        val preventive =
            CheckBox(this).apply {

                text =
                    "Профилактическая работа"

                setTextColor(TEXT)

                isChecked =
                    target.preventive
            }

        val form =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    24,
                    8,
                    24,
                    0
                )

                addView(name)
                addView(intervalKm)
                addView(intervalMonths)
                addView(oilType)
                addView(oilName)
                addView(preventive)
            }

        val scroll =
            ScrollView(this).apply {
                addView(form)
            }

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    if (isNew) {
                        "Новая работа"
                    } else {
                        "Изменить работу"
                    }
                )
                .setView(scroll)
                .setNegativeButton(
                    "Отмена",
                    null
                )
                .setPositiveButton(
                    "Сохранить",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val nameValue =
                    name.text
                        .toString()
                        .trim()

                val intervalKmValue =
                    intervalKm.text
                        .toString()
                        .toIntOrNull()
                        ?: 0

                val intervalMonthsValue =
                    intervalMonths.text
                        .toString()
                        .toIntOrNull()
                        ?: 0

                if (nameValue.isBlank()) {

                    Toast.makeText(
                        this,
                        "Укажите название работы",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                if (
                    intervalKmValue < 0 ||
                    intervalMonthsValue < 0
                ) {

                    Toast.makeText(
                        this,
                        "Интервал не может быть отрицательным",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                target.carId =
                    selectedCarId

                target.name =
                    nameValue

                target.intervalKm =
                    intervalKmValue

                target.intervalMonths =
                    intervalMonthsValue

                target.oilType =
                    oilType.text
                        .toString()
                        .trim()

                target.oilName =
                    oilName.text
                        .toString()
                        .trim()

                target.preventive =
                    preventive.isChecked

                if (preventive.isChecked) {
                    target.skipped = false
                }

                if (isNew) {
                    works.add(target)
                }

                saveData()

                dialog.dismiss()

                currentTab = "Перечень"

                updateNavigation()
                showWorks()
            }
        }

        dialog.show()
    }
}
