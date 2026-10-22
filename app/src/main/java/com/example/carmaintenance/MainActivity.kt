package com.example.carmaintenance

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
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

    private val prefs by lazy { getSharedPreferences("car_maintenance", MODE_PRIVATE) }
    private val cars = mutableListOf<Car>()
    private val works = mutableListOf<MaintenanceWork>()
    private var selectedCarId = -1L

    // Тёмная палитра приложения
    private val background = Color.rgb(23, 25, 30)
    private val surface = Color.rgb(37, 41, 50)
    private val surfaceLight = Color.rgb(48, 53, 64)
    private val textPrimary = Color.rgb(242, 243, 245)
    private val textSecondary = Color.rgb(177, 184, 196)
    private val green = Color.rgb(102, 187, 106)
    private val red = Color.rgb(239, 83, 80)
    private val purple = Color.rgb(206, 147, 216)
    private val yellow = Color.rgb(255, 213, 79)
    private val accent = Color.rgb(129, 199, 132)

    private val sections = listOf("Главная", "Перечень работ", "Регламент", "Статистика", "Ещё")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.statusBarColor = background
        window.navigationBarColor = background
        window.decorView.systemUiVisibility = 0

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
        val a = try { JSONArray(prefs.getString("cars", "[]")) } catch (_: Exception) { JSONArray() }
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            cars.add(Car(
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
            ))
        }
    }

    private fun saveCars() {
        val a = JSONArray()
        cars.forEach { c ->
            a.put(JSONObject().apply {
                put("id", c.id); put("brand", c.brand); put("model", c.model)
                put("year", c.year); put("vin", c.vin); put("mileage", c.mileage)
                put("engine", c.engine); put("fuel", c.fuel); put("hasLpg", c.hasLpg)
                put("notes", c.notes)
            })
        }
        prefs.edit().putString("cars", a.toString()).apply()
    }

    private fun loadWorks() {
        works.clear()
        val a = try { JSONArray(prefs.getString("maintenance_works", "[]")) } catch (_: Exception) { JSONArray() }
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            val hasExecution = if (o.has("hasLastExecution")) o.optBoolean("hasLastExecution") else true
            works.add(MaintenanceWork(
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
            ))
        }
    }

    private fun saveWorks() {
        val a = JSONArray()
        works.forEach { w ->
            a.put(JSONObject().apply {
                put("id", w.id); put("carId", w.carId); put("name", w.name)
                put("intervalKm", w.intervalKm); put("intervalMonths", w.intervalMonths)
                put("lastMileage", w.lastMileage); put("lastDate", w.lastDate)
                put("hasLastExecution", w.hasLastExecution); put("skipped", w.skipped)
                put("preventive", w.preventive)
            })
        }
        prefs.edit().putString("maintenance_works", a.toString()).apply()
    }

    private fun showSection(section: String) {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(6))
            setBackgroundColor(this@MainActivity.background)
            fitsSystemWindows = true
        }

        page.addView(TextView(this).apply {
            text = section
            textSize = 25f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(textPrimary)
            setPadding(dp(5), dp(8), dp(5), dp(14))
        })

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(8))
            setBackgroundColor(this@MainActivity.background)
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
            setBackgroundColor(this@MainActivity.background)
            addView(content)
        }
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(surface)
            setPadding(dp(3), dp(5), dp(3), dp(5))
        }
        val labels = listOf("Главная", "Перечень\nработ", "Регламент", "Статистика", "Ещё")
        sections.forEachIndexed { index, item ->
            nav.addView(TextView(this).apply {
                text = labels[index]
                textSize = 10.5f
                gravity = Gravity.CENTER
                setTextColor(if (item == section) accent else textSecondary)
                setPadding(dp(2), dp(7), dp(2), dp(7))
                minHeight = dp(48)
                isClickable = true
                isFocusable = true
                if (item == section) setBackgroundColor(surfaceLight)
                setOnClickListener { showSection(item) }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        }
        page.addView(nav, LinearLayout.LayoutParams(-1, -2))
        setContentView(page)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun selectedCar(): Car? = cars.firstOrNull { it.id == selectedCarId }

    private fun showHome(root: LinearLayout) {
        val car = selectedCar()
        if (car == null) {
            root.addView(infoCard("Ваш гараж пока пуст", "Добавьте первый автомобиль, чтобы вести его регламент и историю обслуживания."))
            root.addView(actionButton("＋  Добавить автомобиль") { showCarDialog(null) })
            return
        }

        root.addView(infoCard(
            "${car.brand} ${car.model}",
            "Год выпуска: ${car.year.ifBlank { "не указан" }}\n" +
                "Пробег: ${car.mileage.ifBlank { "не указан" }} км\n" +
                "VIN: ${car.vin.ifBlank { "не указан" }}\n" +
                "Двигатель: ${car.engine.ifBlank { "не указан" }}\n" +
                "Топливо: ${car.fuel}" + if (car.hasLpg) "\nУстановлено ГБО" else ""
        ))

        val carWorks = works.filter { it.carId == car.id }
        val overdue = carWorks.count { getWorkStatus(it, car) == "Необходимо выполнить" }
        val completed = carWorks.count { it.hasLastExecution && !it.skipped &&
            getWorkStatus(it, car) != "Необходимо выполнить" }

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4), 0, dp(4))
        }
        stats.addView(statCard("Всего работ", carWorks.size.toString(), accent), LinearLayout.LayoutParams(0, -2, 1f))
        stats.addView(statCard("К выполнению", overdue.toString(), red), LinearLayout.LayoutParams(0, -2, 1f))
        stats.addView(statCard("Выполнено", completed.toString(), green), LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(stats)

        root.addView(actionButton("Выбрать автомобиль") { selectCarDialog() })
        root.addView(actionButton("Изменить автомобиль") { showCarDialog(car) })
        root.addView(actionButton("＋  Добавить автомобиль") { showCarDialog(null) })
        root.addView(actionButton("＋  Добавить регламентную работу") { showWorkDialog(null) })
    }

    private fun statCard(title: String, value: String, color: Int): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(12), dp(5), dp(12))
            setBackgroundColor(surface)
            addView(TextView(this@MainActivity).apply {
                text = value; textSize = 23f; typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER; setTextColor(color)
            })
            addView(TextView(this@MainActivity).apply {
                text = title; textSize = 10f; gravity = Gravity.CENTER; setTextColor(textSecondary)
            })
        }

    private fun infoCard(title: String, body: String): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(surface)
            val heading = TextView(this@MainActivity).apply {
                text = title; textSize = 20f; typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(textPrimary); setPadding(0, 0, 0, dp(8))
            }
            addView(heading)
            addView(TextView(this@MainActivity).apply {
                text = body; textSize = 14f; setTextColor(textSecondary); setLineSpacing(dp(3).toFloat(), 1f)
            })
            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = dp(10)
            layoutParams = params
        }

    private fun selectCarDialog() {
        if (cars.isEmpty()) return
        val names = cars.map { "${it.brand} ${it.model} (${it.year})" }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Выберите автомобиль").setItems(names) { _, index ->
            selectedCarId = cars[index].id
            saveSelectedCar()
            showSection("Главная")
        }.setNegativeButton("Отмена", null).show()
    }

    private fun showCarDialog(existing: Car?) {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), dp(8))
            setBackgroundColor(surface)
        }
        fun field(hintText: String, value: String = "", numeric: Boolean = false) =
            EditText(this).apply {
                hint = hintText
                setText(value)
                setTextColor(textPrimary)
                setHintTextColor(textSecondary)
                setSingleLine(true)
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
            text = "Установлено ГБО"; setTextColor(textPrimary); isChecked = existing?.hasLpg ?: false
        }
        val notes = field("Примечания", existing?.notes ?: "")
        listOf(brand, model, year, vin, mileage, engine, fuel).forEach { form.addView(it) }
        form.addView(lpg); form.addView(notes)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Новый автомобиль" else "Изменить автомобиль")
            .setView(ScrollView(this).apply { setBackgroundColor(surface); addView(form) })
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()
        dialog.setOnShowListener {
            styleDialog(dialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (brand.text.toString().isBlank() || model.text.toString().isBlank()) {
                    Toast.makeText(this, "Укажи марку и модель автомобиля", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (existing == null) {
                    val car = Car(System.currentTimeMillis(), brand.text.toString().trim(),
                        model.text.toString().trim(), year.text.toString(), vin.text.toString(),
                        mileage.text.toString(), engine.text.toString(), fuel.text.toString(),
                        lpg.isChecked, notes.text.toString())
                    cars.add(car); selectedCarId = car.id; saveSelectedCar()
                } else {
                    existing.brand = brand.text.toString().trim()
                    existing.model = model.text.toString().trim()
                    existing.year = year.text.toString()
                    existing.vin = vin.text.toString()
                    existing.mileage = mileage.text.toString()
                    existing.engine = engine.text.toString()
                    existing.fuel = fuel.text.toString()
                    existing.hasLpg = lpg.isChecked
                    existing.notes = notes.text.toString()
                }
                saveCars(); dialog.dismiss(); showSection("Главная")
            }
        }
        dialog.show()
    }

    private fun showWorkList(root: LinearLayout) {
        val car = selectedCar()
        if (car == null) { root.addView(label("Сначала добавьте автомобиль.")); return }
        val list = works.filter { it.carId == car.id }
        if (list.isEmpty()) root.addView(infoCard("Работ пока нет", "Добавьте обслуживание, чтобы отслеживать сроки и пробег."))
        list.forEach { work ->
            root.addView(workCard(work, car))
            root.addView(actionButton("Действия · ${work.name}") { showWorkActions(work) })
        }
        root.addView(actionButton("＋  Добавить работу") { showWorkDialog(null) })
    }

    private fun showRegulation(root: LinearLayout) {
        val car = selectedCar()
        if (car == null) { root.addView(label("Сначала добавьте автомобиль.")); return }
        val list = works.filter { it.carId == car.id }
        if (list.isEmpty()) root.addView(infoCard("Регламент пока пуст", "Добавьте первую регламентную работу."))
        list.forEach { work ->
            root.addView(workCard(work, car))
            root.addView(actionButton("Действия") { showWorkActions(work) })
        }
        root.addView(actionButton("＋  Добавить работу") { showWorkDialog(null) })
    }

    private fun workCard(work: MaintenanceWork, car: Car): LinearLayout {
        val status = getWorkStatus(work, car)
        val color = statusColor(work, car)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(surface)
            val title = TextView(this@MainActivity).apply {
                text = work.name; textSize = 17f; typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(textPrimary)
            }
            addView(title)
            addView(TextView(this@MainActivity).apply {
                text = status; textSize = 14f; setTextColor(color); setPadding(0, dp(5), 0, dp(5))
            })
            val details = if (work.hasLastExecution) {
                "Последнее выполнение: ${formatDate(work.lastDate)}\nПробег выполнения: ${work.lastMileage} км"
            } else "Работа ещё не выполнялась"
            addView(TextView(this@MainActivity).apply {
                text = details; textSize = 13f; setTextColor(textSecondary)
            })
            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = dp(6)
            layoutParams = params
        }
    }

    private fun showWorkActions(work: MaintenanceWork) {
        val choices = arrayOf("Выполнить работу", "Пропустить работу", "Редактировать", "Удалить")
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            choices
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val itemView = super.getView(position, convertView, parent) as TextView
                itemView.setTextColor(textPrimary)
                itemView.textSize = 16f
                itemView.setPadding(dp(24), dp(14), dp(24), dp(14))
                itemView.setBackgroundColor(surface)
                return itemView
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(work.name)
            .setAdapter(adapter) { _, index ->
                when (index) {
                    0 -> completeWork(work)
                    1 -> {
                        work.skipped = true
                        saveWorks()
                        showSection("Регламент")
                    }
                    2 -> showWorkDialog(work)
                    3 -> {
                        val deleteDialog = AlertDialog.Builder(this)
                            .setTitle("Удалить работу?")
                            .setMessage(work.name)
                            .setPositiveButton("Удалить") { _, _ ->
                                works.remove(work)
                                saveWorks()
                                showSection("Регламент")
                            }
                            .setNegativeButton("Отмена", null)
                            .create()
                        deleteDialog.setOnShowListener { styleDialog(deleteDialog) }
                        deleteDialog.show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            styleDialog(dialog)
            dialog.listView.setBackgroundColor(surface)
            for (i in 0 until dialog.listView.childCount) {
                (dialog.listView.getChildAt(i) as? TextView)?.setTextColor(textPrimary)
            }
        }
        dialog.show()
    }

    private fun showWorkDialog(existing: MaintenanceWork?) {
        val car = selectedCar()
        if (car == null) {
            Toast.makeText(this, "Сначала добавьте автомобиль", Toast.LENGTH_SHORT).show()
            return
        }
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(8), dp(24), dp(8))
            setBackgroundColor(surface)
        }
        fun field(hintText: String, value: String = "", numeric: Boolean = false) = EditText(this).apply {
            hint = hintText; setText(value); setTextColor(textPrimary); setHintTextColor(textSecondary)
            setSingleLine(true)
            if (numeric) inputType = InputType.TYPE_CLASS_NUMBER
        }
        val name = field("Название работы", existing?.name ?: "")
        val km = field("Интервал, км (если нужен)", if ((existing?.intervalKm ?: 0) > 0) existing!!.intervalKm.toString() else "", true)
        val months = field("Интервал, месяцев (если нужен)", if ((existing?.intervalMonths ?: 0) > 0) existing!!.intervalMonths.toString() else "", true)
        val notDone = CheckBox(this).apply {
            text = "Работа ещё не выполнялась"; setTextColor(textPrimary)
            isChecked = existing?.hasLastExecution != true
        }
        val executionMileage = field("Пробег при последнем выполнении, км",
            if (existing?.hasLastExecution == true) existing.lastMileage.toString() else "", true)
        var executionDate = if (existing?.hasLastExecution == true && existing.lastDate > 0L)
            existing.lastDate else System.currentTimeMillis()
        val dateButton = Button(this).apply {
            isAllCaps = false; text = "Дата выполнения: ${formatDate(executionDate)}"
            setOnClickListener { showDatePicker(executionDate) { chosen ->
                executionDate = chosen; text = "Дата выполнения: ${formatDate(executionDate)}"
            } }
        }
        val executionFields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(label("Заполняй эти поля, если работа уже выполнялась."))
            addView(dateButton); addView(executionMileage)
        }
        fun updateExecutionFields() { executionFields.visibility = if (notDone.isChecked) View.GONE else View.VISIBLE }
        notDone.setOnCheckedChangeListener { _, _ -> updateExecutionFields() }
        updateExecutionFields()
        form.addView(name); form.addView(km); form.addView(months); form.addView(notDone); form.addView(executionFields)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Добавить регламентную работу" else "Изменить работу")
            .setView(ScrollView(this).apply { setBackgroundColor(surface); addView(form) })
            .setPositiveButton("Сохранить", null).setNegativeButton("Отмена", null).create()
        dialog.setOnShowListener {
            styleDialog(dialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val kmValue = km.text.toString().toIntOrNull() ?: 0
                val monthValue = months.text.toString().toIntOrNull() ?: 0
                val workName = name.text.toString().trim()
                val hasExecution = !notDone.isChecked
                if (workName.isBlank()) { name.error = "Укажи название работы"; return@setOnClickListener }
                if (kmValue < 0 || monthValue < 0) {
                    Toast.makeText(this, "Интервалы не могут быть отрицательными", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (hasExecution && executionMileage.text.toString().toIntOrNull() == null) {
                    executionMileage.error = "Укажи пробег"; return@setOnClickListener
                }
                val actualMileage = executionMileage.text.toString().toIntOrNull() ?: 0
                if (existing == null) {
                    works.add(MaintenanceWork(System.currentTimeMillis(), car.id, workName, kmValue, monthValue,
                        actualMileage, if (hasExecution) executionDate else 0L, hasExecution))
                } else {
                    existing.name = workName; existing.intervalKm = kmValue; existing.intervalMonths = monthValue
                    existing.hasLastExecution = hasExecution
                    existing.lastMileage = if (hasExecution) actualMileage else 0
                    existing.lastDate = if (hasExecution) executionDate else 0L
                    if (!hasExecution) existing.skipped = false
                }
                saveWorks(); dialog.dismiss(); showSection("Регламент")
            }
        }
        dialog.show()
    }

    private fun completeWork(work: MaintenanceWork) {
        val car = cars.firstOrNull { it.id == work.carId } ?: return
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(8), dp(24), dp(8))
            setBackgroundColor(surface)
        }
        val mileage = EditText(this).apply {
            hint = "Фактический пробег выполнения, км"; inputType = InputType.TYPE_CLASS_NUMBER
            setText(car.mileage); setTextColor(textPrimary); setHintTextColor(textSecondary)
        }
        var selectedDate = System.currentTimeMillis()
        val dateButton = Button(this).apply {
            isAllCaps = false; text = "Дата выполнения: ${formatDate(selectedDate)}"
            setOnClickListener { showDatePicker(selectedDate) { chosen ->
                selectedDate = chosen; text = "Дата выполнения: ${formatDate(selectedDate)}"
            } }
        }
        form.addView(label("Укажи фактические дату и пробег выполнения работы."))
        form.addView(dateButton); form.addView(mileage)
        val dialog = AlertDialog.Builder(this).setTitle("Выполнить работу").setView(form)
            .setPositiveButton("Сохранить", null).setNegativeButton("Отмена", null).create()
        dialog.setOnShowListener {
            styleDialog(dialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val actualMileage = mileage.text.toString().toIntOrNull()
                if (actualMileage == null) { mileage.error = "Укажи пробег"; return@setOnClickListener }
                if (actualMileage < 0) { mileage.error = "Пробег не может быть отрицательным"; return@setOnClickListener }
                work.lastMileage = actualMileage; work.lastDate = selectedDate
                work.hasLastExecution = true; work.skipped = false
                saveWorks(); dialog.dismiss(); showSection("Регламент")
            }
        }
        dialog.show()
    }

    private fun showDatePicker(initialDate: Long, onSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialDate }
        DatePickerDialog(this, { _, year, month, day ->
            val chosen = Calendar.getInstance().apply {
                set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onSelected(chosen.timeInMillis)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun getWorkStatus(work: MaintenanceWork, car: Car): String {
        if (work.skipped) return "Пропущена"
        if (!work.hasLastExecution) return "Необходимо выполнить"
        val currentMileage = car.mileage.toIntOrNull() ?: 0
        val dueByKm = work.intervalKm > 0 && currentMileage >= work.lastMileage + work.intervalKm
        var dueByDate = false
        if (work.intervalMonths > 0 && work.lastDate > 0L) {
            val date = Calendar.getInstance().apply { timeInMillis = work.lastDate; add(Calendar.MONTH, work.intervalMonths) }
            dueByDate = System.currentTimeMillis() >= date.timeInMillis
        }
        return if (dueByKm || dueByDate) "Необходимо выполнить" else "Срок ещё не наступил"
    }

    private fun statusColor(work: MaintenanceWork, car: Car): Int {
        if (work.skipped) return purple
        if (work.preventive) return yellow
        if (!work.hasLastExecution || getWorkStatus(work, car) == "Необходимо выполнить") return red
        return green
    }

    private fun showStatistics(root: LinearLayout) {
        val car = selectedCar()
        if (car == null) { root.addView(label("Сначала добавьте автомобиль.")); return }
        val list = works.filter { it.carId == car.id }
        val completed = list.count { it.hasLastExecution && !it.skipped && getWorkStatus(it, car) != "Необходимо выполнить" }
        val skipped = list.count { it.skipped }
        val notDone = list.count { !it.hasLastExecution && !it.skipped }
        root.addView(infoCard("${car.brand} ${car.model}",
            "Всего регламентных работ: ${list.size}\n\n" +
                "Выполнено и срок не наступил: $completed\n" +
                "Ещё не выполнялось: $notDone\n" +
                "Пропущено: $skipped\n" +
                "Требуют выполнения: ${list.count { getWorkStatus(it, car) == "Необходимо выполнить" }}"))
    }

    private fun showMore(root: LinearLayout) {
        root.addView(infoCard("Дополнительные функции",
            "Здесь постепенно появятся каталог деталей и расходников, история обслуживания, вложения и резервное копирование."))
        root.addView(actionButton("＋  Добавить автомобиль") { showCarDialog(null) })
        root.addView(actionButton("Выбрать автомобиль") { selectCarDialog() })
    }

    private fun label(value: String): TextView = TextView(this).apply {
        text = value; textSize = 15f; setTextColor(textSecondary)
        setPadding(dp(8), dp(10), dp(8), dp(10))
    }

    private fun actionButton(value: String, action: () -> Unit): Button = Button(this).apply {
        text = value; isAllCaps = false; textSize = 14f
        setTextColor(textPrimary)
        setBackgroundColor(surfaceLight)
        setOnClickListener { action() }
        val params = LinearLayout.LayoutParams(-1, -2)
        params.topMargin = dp(4); params.bottomMargin = dp(4)
        layoutParams = params
    }

    private fun styleDialog(dialog: AlertDialog) {
        dialog.window?.setBackgroundDrawableResource(android.R.color.background_dark)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(accent)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(textSecondary)
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(textSecondary)
        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(textSecondary)
    }

    private fun formatDate(time: Long): String {
        if (time <= 0L) return "не указана"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(time))
    }
}
