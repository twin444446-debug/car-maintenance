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

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("car_maintenance", MODE_PRIVATE)
    }

    private val cars = mutableListOf<Car>()
    private lateinit var root: LinearLayout

    private val sections = listOf(
        "Главная", "Перечень работ", "Регламент",
        "Статистика", "Ещё"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadCars()
        showSection("Главная")
    }

    private fun loadCars() {
        cars.clear()
        val array = try {
            JSONArray(prefs.getString("cars", "[]"))
        } catch (_: Exception) {
            JSONArray()
        }

        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i) ?: continue
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
        val array = JSONArray()
        cars.forEach { car ->
            array.put(JSONObject().apply {
                put("id", car.id)
                put("brand", car.brand)
                put("model", car.model)
                put("year", car.year)
                put("vin", car.vin)
                put("mileage", car.mileage)
                put("engine", car.engine)
                put("fuel", car.fuel)
                put("hasLpg", car.hasLpg)
                put("notes", car.notes)
            })
        }
        prefs.edit().putString("cars", array.toString()).apply()
    }

    private fun showSection(section: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            setBackgroundColor(Color.rgb(248, 249, 252))
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
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
            "Перечень работ" -> addInfo(
                content, "Здесь появится история обслуживания и ремонтов."
            )
            "Регламент" -> addInfo(
                content, "Здесь появятся регламенты автомобиля и ГБО."
            )
            "Статистика" -> addInfo(
                content, "Здесь появится статистика расходов."
            )
            else -> addInfo(
                content,
                "Здесь появятся настройки, шаблоны, запчасти, шины и резервные копии."
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
            addInfo(container, "Автомобилей пока нет. Нажми «Добавить автомобиль».")
        }

        cars.forEach { car ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 12, 16, 12)
                setBackgroundColor(Color.WHITE)
            }

            card.addView(TextView(this).apply {
                text = "${car.brand} ${car.model}".trim()
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
                text = "Редактировать"
                isAllCaps = false
                setOnClickListener { editCar(car) }
            })

            container.addView(card)
            container.addView(TextView(this).apply { text = " " })
        }
    }

    private fun addInfo(container: LinearLayout, message: String) {
        container.addView(TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(4, 12, 4, 20)
        })
    }

    private fun editCar(existing: Car?) {
        val fields = mutableListOf<EditText>()

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 8)
        }

        fun field(label: String, value: String = ""): EditText {
            form.addView(TextView(this).apply {
                text = label
                textSize = 14f
            })
            val input = EditText(this).apply {
                setSingleLine(true)
                setText(value)
                hint = label
            }
            form.addView(input)
            fields.add(input)
            return input
        }

        field("Марка", existing?.brand.orEmpty())
        field("Модель", existing?.model.orEmpty())
        field("Год выпуска", existing?.year.orEmpty())
        field("VIN-код автомобиля", existing?.vin.orEmpty())
        field("Текущий пробег, км", existing?.mileage.orEmpty())
        field("Объём двигателя", existing?.engine.orEmpty())

        form.addView(TextView(this).apply {
            text = "Основное топливо"
            textSize = 14f
        })

        val fuelSpinner = Spinner(this)
        val fuelOptions = listOf("Бензин", "Дизель", "Гибрид", "Электро")
        fuelSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            fuelOptions
        )
        fuelSpinner.setSelection(
            fuelOptions.indexOf(existing?.fuel).coerceAtLeast(0)
        )
        form.addView(fuelSpinner)

        val lpgCheck = CheckBox(this).apply {
            text = "Установлено газовое оборудование (ГБО)"
            isChecked = existing?.hasLpg ?: false
        }
        form.addView(lpgCheck)

        field("Примечания", existing?.notes.orEmpty())

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (existing == null) "Добавить автомобиль"
                else "Редактировать автомобиль"
            )
            .setView(ScrollView(this).apply { addView(form) })
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val brand = fields[0].text.toString().trim()
                    val model = fields[1].text.toString().trim()

                    if (brand.isBlank() || model.isBlank()) {
                        Toast.makeText(
                            this,
                            "Укажи марку и модель автомобиля",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }

                    val car = existing ?: Car(
                        System.currentTimeMillis(),
                        "", "", "", "", "", "",
                        "Бензин", false, ""
                    )

                    car.brand = brand
                    car.model = model
                    car.year = fields[2].text.toString().trim()
                    car.vin = fields[3].text.toString().trim()
                    car.mileage = fields[4].text.toString().trim()
                    car.engine = fields[5].text.toString().trim()
                    car.fuel = fuelSpinner.selectedItem.toString()
                    car.hasLpg = lpgCheck.isChecked
                    car.notes = fields[6].text.toString().trim()

                    if (existing == null) cars.add(car)
                    saveCars()
                    dialog.dismiss()
                    showSection("Автомобили")

                    Toast.makeText(
                        this, "Автомобиль сохранён", Toast.LENGTH_SHORT
                    ).show()
                }
        }

        dialog.show()
    }
}
