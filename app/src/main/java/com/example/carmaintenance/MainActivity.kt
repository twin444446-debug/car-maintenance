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
            a.put(
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

        prefs.edit()
            .putString("cars", a.toString())
            .apply()
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

            val hasExecution =
                if (o.has("hasLastExecution")) {
                    o.optBoolean("hasLastExecution")
                } else {
                    true
                }

            works.add(
                MaintenanceWork(
                    id = o.optLong("id"),
                    carId = o.optLong("carId"),
                    name = o.optString("name"),
                    intervalKm = o.optInt("intervalKm"),
                    intervalMonths =
