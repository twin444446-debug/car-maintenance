package com.example.carmaintenance

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

fun loadCars(context: Context): List<Car> {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val jsonStr = prefs.getString("cars", "[]") ?: "[]"
    val list = mutableListOf<Car>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            list.add(Car(o.optLong("id"), o.optString("brand"), o.optString("model"), o.optString("year"), o.optString("vin"), o.optString("mileage"), o.optString("engine"), o.optString("fuel"), o.optBoolean("hasLpg"), o.optString("notes"), o.optString("imageUri")))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveCars(context: Context, cars: List<Car>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    cars.forEach { car ->
        val o = JSONObject()
        o.put("id", car.id); o.put("brand", car.brand); o.put("model", car.model); o.put("year", car.year); o.put("vin", car.vin); o.put("mileage", car.mileage); o.put("engine", car.engine); o.put("fuel", car.fuel); o.put("hasLpg", car.hasLpg); o.put("notes", car.notes); o.put("imageUri", car.imageUri)
        array.put(o)
    }
    prefs.edit().putString("cars", array.toString()).apply()
}

fun loadWorks(context: Context): List<MaintenanceWork> {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val jsonStr = prefs.getString("works", "[]") ?: "[]"
    val list = mutableListOf<MaintenanceWork>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            list.add(MaintenanceWork(o.optLong("id"), o.optLong("carId"), o.optString("name"), o.optInt("intervalKm"), o.optInt("intervalMonths"), o.optInt("lastMileage"), o.optLong("lastDate"), o.optBoolean("hasLastExecution"), o.optBoolean("skipped"), o.optBoolean("preventive"), o.optString("notes")))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveWorks(context: Context, works: List<MaintenanceWork>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    works.forEach { work ->
        val o = JSONObject()
        o.put("id", work.id); o.put("carId", work.carId); o.put("name", work.name); o.put("intervalKm", work.intervalKm); o.put("intervalMonths", work.intervalMonths); o.put("lastMileage", work.lastMileage); o.put("lastDate", work.lastDate); o.put("hasLastExecution", work.hasLastExecution); o.put("skipped", work.skipped); o.put("preventive", work.preventive); o.put("notes", work.notes)
        array.put(o)
    }
    prefs.edit().putString("works", array.toString()).apply()
}

fun loadHistory(context: Context): List<WorkHistory> {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val jsonStr = prefs.getString("history", "[]") ?: "[]"
    val list = mutableListOf<WorkHistory>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            list.add(WorkHistory(o.optLong("id"), o.optLong("carId"), o.optString("workName"), o.optLong("date"), o.optInt("mileage"), o.optString("notes")))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveHistory(context: Context, history: List<WorkHistory>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    history.forEach { h ->
        val o = JSONObject()
        o.put("id", h.id); o.put("carId", h.carId); o.put("workName", h.workName); o.put("date", h.date); o.put("mileage", h.mileage); o.put("notes", h.notes)
        array.put(o)
    }
    prefs.edit().putString("history", array.toString()).apply()
}

// Функции для редактора шаблона
fun loadDefaultTemplate(context: Context): List<MaintenanceWork> {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val jsonStr = prefs.getString("default_template", null)
    if (jsonStr == null) {
        val initial = getDefaultWorks(0L) + getLpgDefaultWorks(0L)
        saveDefaultTemplate(context, initial)
        return initial
    }
    val list = mutableListOf<MaintenanceWork>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            list.add(MaintenanceWork(o.optLong("id"), 0L, o.optString("name"), o.optInt("intervalKm"), o.optInt("intervalMonths"), 0, 0, false, false, o.optBoolean("preventive"), o.optString("notes")))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveDefaultTemplate(context: Context, works: List<MaintenanceWork>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    works.forEach { work ->
        val o = JSONObject()
        o.put("id", work.id); o.put("name", work.name); o.put("intervalKm", work.intervalKm); o.put("intervalMonths", work.intervalMonths); o.put("preventive", work.preventive); o.put("notes", work.notes)
        array.put(o)
    }
    prefs.edit().putString("default_template", array.toString()).apply()
}
