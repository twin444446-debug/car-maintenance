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
            list.add(
                Car(
                    id = o.optString("id"),
                    brand = o.optString("brand"),
                    model = o.optString("model"),
                    year = o.optString("year"),
                    vin = o.optString("vin"),
                    mileage = o.optString("mileage"),
                    engine = o.optString("engine"),
                    fuel = o.optString("fuel"),
                    hasLpg = o.optBoolean("hasLpg"),
                    notes = o.optString("notes"),
                    imageUri = o.optString("imageUri")
                )
            )
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
            list.add(
                MaintenanceWork(
                    id = o.optString("id"),
                    carId = o.optString("carId"),
                    name = o.optString("name"),
                    intervalKm = o.optInt("intervalKm"),
                    intervalMonths = o.optInt("intervalMonths"),
                    lastMileage = o.optInt("lastMileage"),
                    lastDate = o.optLong("lastDate"),
                    hasLastExecution = o.optBoolean("hasLastExecution"),
                    skipped = o.optBoolean("skipped"),
                    preventive = o.optBoolean("preventive"),
                    notes = o.optString("notes")
                )
            )
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
            list.add(
                WorkHistory(
                    id = o.optString("id"),
                    carId = o.optString("carId"), // <-- ИСПРАВЛЕНО ЗДЕСЬ
                    workName = o.optString("workName"),
                    date = o.optLong("date"),
                    mileage = o.optInt("mileage"),
                    notes = o.optString("notes")
                )
            )
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

fun loadDefaultTemplate(context: Context): List<MaintenanceWork> {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val jsonStr = prefs.getString("default_template", null)
    if (jsonStr == null) {
        val initial = getDefaultWorks("") + getLpgDefaultWorks("")
        saveDefaultTemplate(context, initial)
        return initial
    }
    val list = mutableListOf<MaintenanceWork>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            list.add(
                MaintenanceWork(
                    id = o.optString("id"),
                    carId = "", // Шаблон не привязан к машине
                    name = o.optString("name"),
                    intervalKm = o.optInt("intervalKm"),
                    intervalMonths = o.optInt("intervalMonths"),
                    lastMileage = 0,
                    lastDate = 0L,
                    hasLastExecution = false,
                    skipped = false,
                    preventive = o.optBoolean("preventive"),
                    notes = o.optString("notes")
                )
            )
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
