package com.example.carmaintenance

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
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
    var lastMileage: Int,
    var lastDate: Long,
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
        "Главная", "Перечень работ", "Регламент",
        "Статистика", "Ещё"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadCars()
        loadWorks()
        selectedCarId = prefs.getLong("selected_car_id", -1L)
        showSection("Главная")
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
                    o.optLong("id", System.currentTimeMillis() + i),
                    o.optString("brand"),
                    o.optString("model"),
                    o.optString("year"),
                    o.optString("vin"),
                    o.optString("mileage"),
                    o.optString("engine"),
                    o.optString("fuel", "Бензин"),
                    o.optBoolean("hasLpg"),
                    o.optString("notes")
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
            works.add(
                MaintenanceWork(
                    o.optLong("id"),
                    o.optLong("carId"),
                    o.optString("name"),
                    o.optInt("intervalKm"),
                    o.optInt("intervalMonths"),
                    o.optInt("lastMileage"),
                    o.optLong("lastDate", System.currentTimeMillis()),
                    o.optBoolean("skipped"),
                    o.optBoolean("preventive")
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
                put("skipped", w.skipped)
                put("preventive", w.preventive)
            })
        }
        prefs.edit()
            .putString("maintenance_works", a.toString())
            .apply()
    }

    private fun currentCar(): Car? =
        cars.firstOrNull { it.id == selectedCarId }

    private fun showSection(section: String) {
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 20)
            setBackgroundColor(Color.rgb(248, 249, 252))
        }
        scroll.addView(content)

        content.addView(TextView(this).apply {
            text = "Учёт автомобильного ТО"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(35, 55, 75))
            setPadding(0, 12, 0, 20)
        })

        content.addView(TextView(this).apply {
            text = section
            textSize = 21f
            setTextColor(Color.rgb(45, 75, 100))
            setPadding(0, 8, 0, 16)
        })

        when (section) {
            "Главная", "Автомобили" -> showCars(content)
            "Регламент" -> showRegulation(content)
            "Перечень работ" -> showWorkHistory(content)
            "Статистика" -> info(
                content, "Статистика расходов будет добавлена следующим этапом."
            )
            else -> info(
                content, "Здесь появятся шаблоны, запчасти, шины, документы и резервные копии."
            )
        }

        sections.forEach { name ->
            content.addView(Button(this).apply {
                text = name
                isAllCaps = false
                setOnClickListener { showSection(name) }
            })
        }

        setContentView(scroll)
    }

    private fun showCars(container: LinearLayout) {
        container.addView(Button(this).apply {
            text = "＋ Добавить автомобиль"
            isAllCaps = false
            setOnClickListener { editCar(null) }
        })

        if (cars.isEmpty()) {
            info(container, "Автомобилей пока нет.")
        }

        cars.forEach { car ->
            val card = card(container)
            card.addView(TextView(this).apply {
                text = "${car.brand} ${car.model}"
                textSize = 19f
                setTextColor(Color.rgb(35, 55, 75))
            })
            card.addView(TextView(this).apply {
                text = "Год: ${car.year.ifBlank { "не указан" }}\n" +
                    "Пробег: ${car.mileage.ifBlank { "не указан" }} км\n" +
                    "Двигатель: ${car.engine.ifBlank { "не указан" }}\n" +
                    "VIN: ${car.vin.ifBlank { "не указан" }}\n" +
                    "Топливо: ${car.fuel}" +
                    if (car.hasLpg) "\nГБО: установлено" else "\nГБО: нет"
                textSize = 15f
                setTextColor(Color.DKGRAY)
                setPadding(0, 8, 0, 8)
            })

            card.addView(Button(this).apply {
                text = "Выбрать для регламента"
                isAllCaps = false
                setOnClickListener {
                    selectedCarId = car.id
                    prefs.edit().putLong("selected_car_id", car.id).apply()
                    showSection("Регламент")
                }
            })

            card.addView(Button(this).apply {
                text = "Редактировать автомобиль"
                isAllCaps = false
                setOnClickListener { editCar(car) }
            })
        }
    }

    private fun editCar(existing: Car?) {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 8)
        }
        val fields = mutableListOf<EditText>()

        fun field(label: String, value: String = "") {
            form.addView(TextView(this).apply { text = label })
            val e = EditText(this).apply {
                setSingleLine(true)
                setText(value)
                hint = label
            }
            fields.add(e)
            form.addView(e)
        }

        field("Марка", existing?.brand.orEmpty())
        field("Модель", existing?.model.orEmpty())
        field("Год выпуска", existing?.year.orEmpty())
        field("VIN-код автомобиля", existing?.vin.orEmpty())
        field("Текущий пробег, км", existing?.mileage.orEmpty())
        field("Объём двигателя", existing?.engine.orEmpty())

        form.addView(TextView(this).apply { text = "Основное топливо" })
        val fuels = listOf("Бензин", "Дизель", "Гибрид", "Электро")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, fuels
        )
        spinner.setSelection(fuels.indexOf(existing?.fuel).coerceAtLeast(0))
        form.addView(spinner)

        val lpg = CheckBox(this).apply {
            text = "Установлено газовое оборудование (ГБО)"
            isChecked = existing?.hasLpg ?: false
        }
        form.addView(lpg)
        field("Примечания", existing?.notes.orEmpty())

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Добавить автомобиль" else "Редактировать автомобиль")
            .setView(ScrollView(this).apply { addView(form) })
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val brand = fields[0].text.toString().trim()
                val model = fields[1].text.toString().trim()
                if (brand.isBlank() || model.isBlank()) {
                    toast("Укажи марку и модель")
                    return@setOnClickListener
                }

                val car = existing ?: Car(
                    System.currentTimeMillis(), "", "", "", "",
                    "", "", "Бензин", false, ""
                )
                car.brand = brand
                car.model = model
                car.year = fields[2].text.toString().trim()
                car.vin = fields[3].text.toString().trim()
                car.mileage = fields[4].text.toString().trim()
                car.engine = fields[5].text.toString().trim()
                car.fuel = spinner.selectedItem.toString()
                car.hasLpg = lpg.isChecked
                car.notes = fields[6].text.toString().trim()

                if (existing == null) cars.add(car)
                saveCars()
                dialog.dismiss()
                showSection("Автомобили")
                toast("Автомобиль сохранён")
            }
        }
        dialog.show()
    }

    private fun showRegulation(container: LinearLayout) {
        if (cars.isEmpty()) {
            info(container, "Сначала добавь автомобиль.")
            return
        }

        if (currentCar() == null) selectedCarId = cars.first().id
        val car = currentCar() ?: return

        container.addView(TextView(this).apply {
            text = "Автомобиль: ${car.brand} ${car.model}"
            textSize = 18f
            setTextColor(Color.rgb(35, 55, 75))
        })

        container.addView(Button(this).apply {
            text = "Выбрать другой автомобиль"
            isAllCaps = false
            setOnClickListener { showSection("Автомобили") }
        })

        container.addView(Button(this).apply {
            text = "＋ Добавить регламентную работу"
            isAllCaps = false
            setOnClickListener { editWork(null, car) }
        })

        val list = works.filter { it.carId == car.id }
        if (list.isEmpty()) info(container, "Регламент пока пуст. Добавь первую работу.")

        list.forEach { work ->
            val mileage = car.mileage.toIntOrNull() ?: 0
            val kmDue = work.intervalKm > 0 &&
                mileage >= work.lastMileage + work.intervalKm
            val dateDue = work.intervalMonths > 0 &&
                System.currentTimeMillis() >= addMonths(work.lastDate, work.intervalMonths)
            val overdue = kmDue || dateDue

            val statusText: String
            val statusColor: Int
            when {
                work.skipped -> {
                    statusText = "Пропущено"
                    statusColor = purple
                }
                work.preventive -> {
                    statusText = "Рекомендовано для профилактики"
                    statusColor = yellow
                }
                overdue -> {
                    statusText = "Пора выполнить"
                    statusColor = red
                }
                else -> {
                    statusText = "В норме"
                    statusColor = green
                }
            }

            val c = card(container)
            c.addView(TextView(this).apply {
                text = work.name
                textSize = 18f
                setTextColor(Color.rgb(35, 55, 75))
            })
            c.addView(TextView(this).apply {
                text = statusText
                textSize = 15f
                setTextColor(statusColor)
                setPadding(0, 6, 0, 6)
            })
            c.addView(TextView(this).apply {
                val kmText = if (work.intervalKm > 0)
                    "Интервал: ${work.intervalKm} км" else "Интервал по пробегу не задан"
                val monthText = if (work.intervalMonths > 0)
                    "${work.intervalMonths} мес." else "интервал по времени не задан"
                val nextKm = if (work.intervalKm > 0)
                    "\nСледующий пробег: ${work.lastMileage + work.intervalKm} км" else ""
                val nextDate = if (work.intervalMonths > 0)
                    "\nСледующая дата: ${formatDate(addMonths(work.lastDate, work.intervalMonths))}" else ""
                text = "$kmText\nИнтервал по времени: $monthText" +
                    "\nПоследнее выполнение: ${formatDate(work.lastDate)}" +
                    "\nПробег при выполнении: ${work.lastMileage} км" +
                    nextKm + nextDate
                textSize = 14f
                setTextColor(Color.DKGRAY)
            })

            c.addView(Button(this).apply {
                text = "Отметить выполненной"
                isAllCaps = false
                setOnClickListener { completeWork(work, car) }
            })
            c.addView(Button(this).apply {
                text = "Пропустить работу"
                isAllCaps = false
                setOnClickListener {
                    work.skipped = true
                    work.preventive = false
                    saveWorks()
                    showSection("Регламент")
                }
            })
            c.addView(Button(this).apply {
                text = "Изменить"
                isAllCaps = false
                setOnClickListener { editWork(work, car) }
            })
            c.addView(Button(this).apply {
                text = "Удалить работу"
                isAllCaps = false
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage("Удалить работу «${work.name}» из регламента?")
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Удалить") { _, _ ->
                            works.remove(work)
                            saveWorks()
                            showSection("Регламент")
                        }.show()
                }
            })
        }
    }

    private fun editWork(existing: MaintenanceWork?, car: Car) {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 8)
        }
        val fields = mutableListOf<EditText>()

        fun field(label: String, value: String = "") {
            form.addView(TextView(this).apply { text = label })
            val e = EditText(this).apply {
                setSingleLine(true)
                hint = label
                setText(value)
            }
            fields.add(e)
            form.addView(e)
        }

        field("Название работы", existing?.name.orEmpty())
        field("Интервал, км (0 — не задан)", (existing?.intervalKm ?: 0).toString())
        field("Интервал, месяцев (0 — не задан)", (existing?.intervalMonths ?: 0).toString())
        field("Пробег последнего выполнения, км", (existing?.lastMileage ?: (car.mileage.toIntOrNull() ?: 0)).toString())

        val preventive = CheckBox(this).apply {
            text = "Рекомендовано для профилактики"
            isChecked = existing?.preventive ?: false
        }
        form.addView(preventive)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Новая работа" else "Изменить работу")
            .setView(ScrollView(this).apply { addView(form) })
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = fields[0].text.toString().trim()
                val km = fields[1].text.toString().toIntOrNull()
                val months = fields[2].text.toString().toIntOrNull()
                val lastMileage = fields[3].text.toString().toIntOrNull()

                if (name.isBlank() || km == null || months == null ||
                    lastMileage == null || km < 0 || months < 0 || lastMileage < 0 ||
                    (km == 0 && months == 0)
                ) {
                    toast("Укажи название и хотя бы один интервал больше нуля")
                    return@setOnClickListener
                }

                val w = existing ?: MaintenanceWork(
                    System.currentTimeMillis(), car.id, name, km, months,
                    lastMileage, System.currentTimeMillis()
                )
                w.name = name
                w.intervalKm = km
                w.intervalMonths = months
                w.lastMileage = lastMileage
                w.preventive = preventive.isChecked
                w.skipped = false

                if (existing == null) works.add(w)
                saveWorks()
                dialog.dismiss()
                showSection("Регламент")
                toast("Регламент сохранён")
            }
        }
        dialog.show()
    }

    private fun completeWork(work: MaintenanceWork, car: Car) {
        val input = EditText(this).apply {
            hint = "Текущий пробег, км"
            setSingleLine(true)
            setText(car.mileage)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Выполнить: ${work.name}")
            .setMessage("Укажи текущий пробег автомобиля.")
            .setView(input)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Подтвердить") { _, _ ->
                val mileage = input.text.toString().toIntOrNull()
                if (mileage == null || mileage < 0) {
                    toast("Введи корректный пробег")
                    return@setPositiveButton
                }
                if (mileage < work.lastMileage) {
                    toast("Пробег не может быть меньше предыдущего")
                    return@setPositiveButton
                }
                car.mileage = mileage.toString()
                work.lastMileage = mileage
                work.lastDate = System.currentTimeMillis()
                work.skipped = false
                work.preventive = false
                saveCars()
                saveWorks()
                showSection("Регламент")
                toast("Выполнение сохранено")
            }.show()
    }

    private fun showWorkHistory(container: LinearLayout) {
        if (cars.isEmpty()) {
            info(container, "Сначала добавь автомобиль.")
            return
        }
        if (currentCar() == null) selectedCarId = cars.first().id
        val car = currentCar() ?: return

        info(container, "Регламентные работы автомобиля ${car.brand} ${car.model}")
        container.addView(Button(this).apply {
            text = "Открыть регламент"
            isAllCaps = false
            setOnClickListener { showSection("Регламент") }
        })
        info(container, "Журнал выполненных работ будет расширен следующим этапом.")
    }

    private fun card(container: LinearLayout): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(Color.WHITE)
        }
        container.addView(c)
        container.addView(TextView(this).apply { text = " " })
        return c
    }

    private fun info(container: LinearLayout, message: String) {
        container.addView(TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(4, 12, 4, 20)
        })
    }

    private fun addMonths(time: Long, months: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        cal.add(Calendar.MONTH, months)
        return cal.timeInMillis
    }

    private fun formatDate(time: Long): String =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(time))

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
