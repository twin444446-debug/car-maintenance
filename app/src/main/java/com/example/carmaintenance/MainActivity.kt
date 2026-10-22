package com.example.carmaintenance

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Car(
    val id: Long,
    var brand: String,
    var model: String,
    var year: String,
    var vin: String,
    var mileage: String,
    var engine: String,
    var fuel: String,
    var hasLpg: Boolean,
    var notes: String
)

data class MaintenanceWork(
    val id: Long,
    val carId: Long,
    var name: String,
    var intervalKm: Int,
    var intervalMonths: Int,
    var lastMileage: Int = 0,
    var lastDate: Long = 0L,
    var hasLastExecution: Boolean = false,
    var skipped: Boolean = false,
    var preventive: Boolean = false
)

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("car_maintenance", MODE_PRIVATE)
    }

    private val cars = mutableListOf<Car>()
    private val works = mutableListOf<MaintenanceWork>()
    private var selectedCarId = -1L

    private val green = Color.rgb(46, 125, 50)
    private val red = Color.rgb(198, 40, 40)
    private val purple = Color.rgb(123, 31, 162)
    private val yellow = Color.rgb(249, 168, 37)

    private val sections = listOf(
        "Главная", "Перечень работ", "Регламент", "Статистика", "Ещё"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Не позволяем приложению заходить под системные панели.
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        loadCars()
        loadWorks()
        selectedCarId = prefs.getLong("selected_car_id", -1L)

        if (cars.none { it.id == selectedCarId } && cars.isNotEmpty()) {
            selectedCarId = cars.first().id
            saveSelectedCar()
        }

        showSection("Главная")
    }

    private fun saveSelectedCar() {
        prefs.edit().putLong("selected_car_id", selectedCarId).apply()
    }

    private fun loadCars() {
        cars.clear()
        val a = try {
            JSONArray(prefs.getString("cars", "[]"))
        } catch (_: Exception) {
            JSONArray()
        }

        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            cars.add(
                Car(
                    id = o.optLong("id", System.currentTimeMillis() + i),
                    brand = o.optString("brand"),
                    model = o.optString("model"),
                    year = o.optString("year"),
                    vin = o.optString("vin"),
                    mileage = o.optString("mileage"),
                    engine = o.optString("engine"),
                    fuel = o.optString("fuel", "Бензин"),
                    hasLpg = o.optBoolean("hasLpg"),
                    notes = o.optString("notes")
                )
            )
        }
    }

    private fun saveCars() {
        val a = JSONArray()
        cars.forEach { c ->
            a.put(JSONObject().apply {
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
            })
        }
        prefs.edit().putString("cars", a.toString()).apply()
    }

    private fun loadWorks() {
        works.clear()
        val a = try {
            JSONArray(prefs.getString("maintenance_works", "[]"))
        } catch (_: Exception) {
            JSONArray()
        }

        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            val hasExecution = if (o.has("hasLastExecution")) {
                o.optBoolean("hasLastExecution")
            } else {
                true
            }

            works.add(
                MaintenanceWork(
                    id = o.optLong("id", System.currentTimeMillis() + i),
                    carId = o.optLong("carId"),
                    name = o.optString("name"),
                    intervalKm = o.optInt("intervalKm"),
                    intervalMonths = o.optInt("intervalMonths"),
                    lastMileage = o.optInt("lastMileage"),
                    lastDate = o.optLong("lastDate"),
                    hasLastExecution = hasExecution,
                    skipped = o.optBoolean("skipped"),
                    preventive = o.optBoolean("preventive")
                )
            )
        }
    }

    private fun saveWorks() {
        val a = JSONArray()
        works.forEach { w ->
            a.put(JSONObject().apply {
                put("id", w.id)
                put("carId", w.carId)
                put("name", w.name)
                put("intervalKm", w.intervalKm)
                put("intervalMonths", w.intervalMonths)
                put("lastMileage", w.lastMileage)
                put("lastDate", w.lastDate)
                put("hasLastExecution", w.hasLastExecution)
                put("skipped", w.skipped)
                put("preventive", w.preventive)
            })
        }
        prefs.edit().putString("maintenance_works", a.toString()).apply()
    }

    private fun showSection(section: String) {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(4))
            fitsSystemWindows = true
        }

        val title = TextView(this).apply {
            text = section
            textSize = 23f
            setTextColor(Color.BLACK)
            setPadding(dp(4), dp(4), dp(4), dp(8))
        }
        page.addView(title)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        when (section) {
            "Главная" -> showHome(content)
            "Перечень работ" -> showWorkList(content)
            "Регламент" -> showRegulation(content)
            "Статистика" -> showStatistics(content)
            "Ещё" -> showMore(content)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }

        page.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(245, 245, 245))
            setPadding(0, dp(2), 0, dp(2))
        }

        val labels = listOf(
            "Главная",
            "Перечень\nработ",
            "Регламент",
            "Статистика",
            "Ещё"
        )

        sections.forEachIndexed { index, item ->
            val button = TextView(this).apply {
                text = labels[index]
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                setPadding(dp(1), dp(6), dp(1), dp(6))
                minHeight = dp(48)
                isClickable = true
                isFocusable = true

                if (item == section) {
                    setBackgroundColor(Color.rgb(220, 235, 220))
                    setTextColor(green)
                }

                setOnClickListener { showSection(item) }
            }

            nav.addView(
                button,
                LinearLayout.LayoutParams(0, -2, 1f)
            )
        }

        page.addView(
            nav,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(page)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun selectedCar(): Car? =
        cars.firstOrNull { it.id == selectedCarId }

    private fun showHome(root: LinearLayout) {
        val car = selectedCar()

        if (car == null) {
            root.addView(label("Автомобиль пока не добавлен."))
            root.addView(actionButton("Добавить автомобиль") {
                showCarDialog(null)
            })
            if (cars.isNotEmpty()) {
                root.addView(actionButton("Выбрать автомобиль") {
                    selectCarDialog()
                })
            }
            return
        }

        root.addView(label(
            "${car.brand} ${car.model}\n" +
            "Год: ${car.year}\n" +
            "Пробег: ${car.mileage} км\n" +
            "VIN: ${car.vin.ifBlank { "не указан" }}\n" +
            "Двигатель: ${car.engine}\n" +
            "Топливо: ${car.fuel}" +
            if (car.hasLpg) "\nУстановлено ГБО" else ""
        ))

        val carWorks = works.filter { it.carId == car.id }
        val overdue = carWorks.count {
            getWorkStatus(it, car) == "Необходимо выполнить"
        }

        root.addView(label("Регламентных работ: ${carWorks.size}"))
        root.addView(label("Требуют выполнения: $overdue"))

        root.addView(actionButton("Выбрать автомобиль") {
            selectCarDialog()
        })
        root.addView(actionButton("Изменить автомобиль") {
            showCarDialog(car)
        })
        root.addView(actionButton("Добавить автомобиль") {
            showCarDialog(null)
        })
        root.addView(actionButton("Добавить регламентную работу") {
            showWorkDialog(null)
        })
    }

    private fun selectCarDialog() {
        if (cars.isEmpty()) return

        val names = cars.map {
            "${it.brand} ${it.model} (${it.year})"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Выберите автомобиль")
            .setItems(names) { _, index ->
                selectedCarId = cars[index].id
                saveSelectedCar()
                showSection("Главная")
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showCarDialog(existing: Car?) {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(32), dp(8), dp(32), dp(8))
        }

        fun field(
            hintText: String,
            value: String = "",
            numeric: Boolean = false
        ) = EditText(this).apply {
            hint = hintText
            setText(value)
            if (numeric) inputType = InputType.TYPE_CLASS_NUMBER
        }

        val brand = field("Марка", existing?.brand ?: "")
        val model = field("Модель", existing?.model ?: "")
        val year = field("Год выпуска", existing?.year ?: "", true)
        val vin = field("VIN-код автомобиля", existing?.vin ?: "")
        val mileage = field("Текущий пробег, км", existing?.mileage ?: "", true)
        val engine = field("Двигатель", existing?.engine ?: "")
        val fuel = field("Топливо", existing?.fuel ?: "Бензин")
        val lpg = CheckBox(this).apply {
            text = "Установлено ГБО"
            isChecked = existing?.hasLpg ?: false
        }
        val notes = field("Примечания", existing?.notes ?: "")

        listOf(brand, model, year, vin, mileage, engine, fuel).forEach {
            form.addView(it)
        }
        form.addView(lpg)
        form.addView(notes)

        val scroll = ScrollView(this).apply {
            addView(form)
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Новый автомобиль" else "Изменить автомобиль")
            .setView(scroll)
            .setPositiveButton("Сохранить") { _, _ ->
                if (existing == null) {
                    val car = Car(
                        id = System.currentTimeMillis(),
                        brand = brand.text.toString(),
                        model = model.text.toString(),
                        year = year.text.toString(),
                        vin = vin.text.toString(),
                        mileage = mileage.text.toString(),
                        engine = engine.text.toString(),
                        fuel = fuel.text.toString(),
                        hasLpg = lpg.isChecked,
                        notes = notes.text.toString()
                    )
                    cars.add(car)
                    selectedCarId = car.id
                    saveSelectedCar()
                } else {
                    existing.brand = brand.text.toString()
                    existing.model = model.text.toString()
                    existing.year = year.text.toString()
                    existing.vin = vin.text.toString()
                    existing.mileage = mileage.text.toString()
                    existing.engine = engine.text.toString()
                    existing.fuel = fuel.text.toString()
                    existing.hasLpg = lpg.isChecked
                    existing.notes = notes.text.toString()
                }
                saveCars()
                showSection("Главная")
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showWorkList(root: LinearLayout) {
        val car = selectedCar()
        if (car == null) {
            root.addView(label("Сначала добавьте автомобиль."))
            return
        }

        val list = works.filter { it.carId == car.id }
        if (list.isEmpty()) root.addView(label("Работ пока нет."))

        list.forEach { work ->
            root.addView(actionButton(work.name + "\n" + getWorkStatus(work, car)) {
                showWorkActions(work)
            })
        }

        root.addView(actionButton("Добавить работу") {
            showWorkDialog(null)
        })
    }

    private fun showRegulation(root: LinearLayout) {
        val car = selectedCar()
        if (car == null) {
            root.addView(label("Сначала добавьте автомобиль."))
            return
        }

        val list = works.filter { it.carId == car.id }
        if (list.isEmpty()) root.addView(label("Регламент пока пуст."))

        list.forEach { work ->
            val status = getWorkStatus(work, car)
            val block = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }

            block.addView(TextView(this).apply {
                text = work.name
                textSize = 18f
                setTextColor(statusColor(work, car))
            })

            block.addView(label(
                if (work.hasLastExecution) {
                    "Последнее выполнение: ${formatDate(work.lastDate)}\n" +
                    "Пробег выполнения: ${work.lastMileage} км\n$status"
                } else {
                    "Работа ещё не выполнялась\n$status"
                }
            ))

            block.addView(actionButton("Действия") {
                showWorkActions(work)
            })
            root.addView(block)
            root.addView(label("────────────"))
        }

        root.addView(actionButton("Добавить работу") {
            showWorkDialog(null)
        })
    }

    private fun showWorkActions(work: MaintenanceWork) {
        val choices = arrayOf(
            "Выполнить работу",
            "Пропустить работу",
            "Редактировать",
            "Удалить"
        )

        AlertDialog.Builder(this)
            .setTitle(work.name)
            .setItems(choices) { _, index ->
                when (index) {
                    0 -> completeWork(work)
                    1 -> {
                        work.skipped = true
                        saveWorks()
                        showSection("Регламент")
                    }
                    2 -> showWorkDialog(work)
                    3 -> AlertDialog.Builder(this)
                        .setTitle("Удалить работу?")
                        .setMessage(work.name)
                        .setPositiveButton("Удалить") { _, _ ->
                            works.remove(work)
                            saveWorks()
                            showSection("Регламент")
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showWorkDialog(existing: MaintenanceWork?) {
        val car = selectedCar()
        if (car == null) {
            Toast.makeText(this, "Сначала добавьте автомобиль", Toast.LENGTH_SHORT).show()
            return
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(32), dp(8), dp(32), dp(8))
        }

        val name = EditText(this).apply {
            hint = "Название работы"
            setText(existing?.name ?: "")
        }
        val km = EditText(this).apply {
            hint = "Интервал, км (если нужен)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(if ((existing?.intervalKm ?: 0) > 0) existing!!.intervalKm.toString() else "")
        }
        val months = EditText(this).apply {
            hint = "Интервал, месяцев (если нужен)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(if ((existing?.intervalMonths ?: 0) > 0) existing!!.intervalMonths.toString() else "")
        }

        val notDone = CheckBox(this).apply {
            text = "Работа ещё не выполнялась"
            isChecked = existing?.hasLastExecution != true
            isEnabled = existing == null
        }

        form.addView(name)
        form.addView(km)
        form.addView(months)
        form.addView(notDone)

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Добавить регламентную работу" else "Изменить работу")
            .setView(form)
            .setPositiveButton("Сохранить") { _, _ ->
                val kmValue = km.text.toString().toIntOrNull() ?: 0
                val monthValue = months.text.toString().toIntOrNull() ?: 0
                val workName = name.text.toString().trim()

                if (workName.isBlank()) {
                    Toast.makeText(this, "Укажи название работы", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (existing == null) {
                    works.add(
                        MaintenanceWork(
                            id = System.currentTimeMillis(),
                            carId = car.id,
                            name = workName,
                            intervalKm = kmValue,
                            intervalMonths = monthValue,
                            lastMileage = 0,
                            lastDate = 0L,
                            hasLastExecution = !notDone.isChecked
                        )
                    )
                } else {
                    existing.name = workName
                    existing.intervalKm = kmValue
                    existing.intervalMonths = monthValue
                }

                saveWorks()
                showSection("Регламент")
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun completeWork(work: MaintenanceWork) {
        val car = cars.firstOrNull { it.id == work.carId } ?: return
        val mileage = car.mileage.toIntOrNull()

        if (mileage == null) {
            Toast.makeText(this, "Сначала укажи пробег автомобиля", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Выполнить работу?")
            .setMessage("Записать дату и текущий пробег ${mileage} км?")
            .setPositiveButton("Подтвердить") { _, _ ->
                work.lastMileage = mileage
                work.lastDate = System.currentTimeMillis()
                work.hasLastExecution = true
                work.skipped = false
                saveWorks()
                showSection("Регламент")
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun getWorkStatus(work: MaintenanceWork, car: Car): String {
        if (work.skipped) return "Пропущена"
        if (!work.hasLastExecution) return "Необходимо выполнить"

        val currentMileage = car.mileage.toIntOrNull() ?: 0
        val dueByKm = work.intervalKm > 0 &&
            currentMileage >= work.lastMileage + work.intervalKm

        var dueByDate = false
        if (work.intervalMonths > 0 && work.lastDate > 0L) {
            val date = Calendar.getInstance().apply {
                timeInMillis = work.lastDate
                add(Calendar.MONTH, work.intervalMonths)
            }
            dueByDate = System.currentTimeMillis() >= date.timeInMillis
        }

        return if (dueByKm || dueByDate) "Необходимо выполнить" else "Срок ещё не наступил"
    }

    private fun statusColor(work: MaintenanceWork, car: Car): Int {
        if (work.skipped) return purple
        if (work.preventive) return yellow
        if (!work.hasLastExecution || getWorkStatus(work, car) == "Необходимо выполнить") {
            return red
        }
        return green
    }

    private fun showStatistics(root: LinearLayout) {
        val car = selectedCar()
        if (car == null) {
            root.addView(label("Сначала добавьте автомобиль."))
            return
        }

        val list = works.filter { it.carId == car.id }
        val completed = list.count { it.hasLastExecution && !it.skipped }
        val skipped = list.count { it.skipped }
        val notDone = list.count { !it.hasLastExecution && !it.skipped }

        root.addView(label(
            "Автомобиль: ${car.brand} ${car.model}\n\n" +
            "Всего работ: ${list.size}\n" +
            "Выполнено: $completed\n" +
            "Ещё не выполнялось: $notDone\n" +
            "Пропущено: $skipped"
        ))
    }

    private fun showMore(root: LinearLayout) {
        root.addView(label(
            "Здесь будут дополнительные функции: перечень деталей, " +
            "история обслуживания, вложения и резервное копирование."
        ))
        root.addView(actionButton("Добавить автомобиль") {
            showCarDialog(null)
        })
    }

    private fun label(value: String): TextView =
        TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(dp(8), dp(12), dp(8), dp(12))
        }

    private fun actionButton(value: String, action: () -> Unit): Button =
        Button(this).apply {
            text = value
            isAllCaps = false
            setOnClickListener { action() }
        }

    private fun formatDate(time: Long): String {
        if (time <= 0L) return "не указана"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(time))
    }
}
