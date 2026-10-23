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
            list.add(Car(o.optLong("id"), o.optString("brand"), o.optString("model"),
    o.optString("year"), o.optString("vin"), o.optString("mileage"),
    o.optString("engine"), o.optString("fuel"), o.optBoolean("hasLpg"), o.optString("notes"), o.optString("imageUri")))
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveCars(context: Context, cars: List<Car>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    cars.forEach { car ->
        val o = JSONObject()
        o.put("id", car.id); o.put("brand", car.brand); o.put("model", car.model)
        o.put("year", car.year); o.put("vin", car.vin); o.put("mileage", car.mileage)
        o.put("engine", car.engine); o.put("fuel", car.fuel); o.put("hasLpg", car.hasLpg)
        o.put("imageUri", car.imageUri)
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
            list.add(MaintenanceWork(o.optLong("id"), o.optLong("carId"), o.optString("name"),
                o.optInt("intervalKm"), o.optInt("intervalMonths"), o.optInt("lastMileage"),
                o.optLong("lastDate"), o.optBoolean("hasLastExecution"), o.optBoolean("skipped"),
                o.optBoolean("preventive")))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveWorks(context: Context, works: List<MaintenanceWork>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    works.forEach { work ->
        val o = JSONObject()
        o.put("id", work.id); o.put("carId", work.carId); o.put("name", work.name)
        o.put("intervalKm", work.intervalKm); o.put("intervalMonths", work.intervalMonths)
        o.put("lastMileage", work.lastMileage); o.put("lastDate", work.lastDate)
        o.put("hasLastExecution", work.hasLastExecution); o.put("skipped", work.skipped)
        o.put("preventive", work.preventive)
        array.put(o)
    }
    prefs.edit().putString("works", array.toString()).apply()
}
