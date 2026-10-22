package com.example.carmaintenance

import android.app.Activity
import android.app.DatePickerDialog
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
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
private const val PANEL2 = 0xFF303641.toInt()
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

    private var selectedCarId: String = ""
    private var currentTab = "Главная"

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private fun today(): String = dateFormat.format(Date())

    private fun car(): Car? = cars.find { it.id == selectedCarId }

    private fun carWorks(): List<MaintenanceWork> =
        works.filter { it.carId == selectedCarId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = BG
        window.navigationBarColor = BG
        window.decorView.systemUiVisibility = 0

        loadData()
        createLayout()
        showHome()
    }

    private fun loadData() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        selectedCarId = prefs.getString(SELECTED_CAR_KEY, "") ?: ""

        try {
            val array = JSONArray(prefs.getString(CARS_KEY, "[]"))
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                cars.add(
                    Car(
                        id = o.optString("id", UUID.randomUUID().toString()),
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
            val array = JSONArray(prefs.getString(WORKS_KEY, "[]"))
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                works.add(
                    MaintenanceWork(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        carId = o.optString("carId"),
                        name = o.optString("name"),
                        intervalKm = o.optInt("intervalKm"),
                        intervalMonths = o.optInt("intervalMonths"),
                        lastMileage = o.optInt("lastMileage"),
                        lastDate = o.optString("lastDate"),
                        hasLastExecution = o.optBoolean("hasLastExecution"),
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
            selectedCarId = cars.firstOrNull()?.id ?: ""
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
                    put("hasLastExecution", w.hasLastExecution)
                    put("skipped", w.skipped)
                    put("preventive", w.preventive)
                    put("oilType", w.oilType)
                    put("oilName", w.oilName)
                }
            )
        }

        getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CARS_KEY, carArray.toString())
            .putString(WORKS_KEY, workArray.toString())
            .putString(SELECTED_CAR_KEY, selectedCarId)
            .apply()
    }

    private fun createLayout() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
        }

        val header
