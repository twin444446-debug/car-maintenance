package com.example.carmaintenance

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
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
        "Главная",
        "Перечень работ",
        "Регламент",
        "Статистика",
        "Ещё"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadCars()
        loadWorks()

        selectedCarId = prefs.getLong("selected_car_id", -1L)

        showSection("Главная")
    }

    private fun saveSelectedCar() {
        prefs.edit()
            .putLong("selected_car_id", selectedCarId)
            .apply()
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
        val array = JSONArray()

        cars.forEach { car ->
            array.put(
                JSONObject().apply {
                    put("id", car.id)
                    put("brand", car.brand)
                    put("model", car.model)
                    put("year", car.year)
                    put("vin", car.vin)
                    put("mileage", car
