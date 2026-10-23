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
            list.add(Car(
                id = o.optLong("id"), 
                brand = o.optString("brand"), 
                model = o.optString("model"),
                year = o.optString("year"), 
                vin = o.optString("vin"), 
                mileage = o.optString("mileage"),
                engine = o.optString("engine"), 
                fuel = o.optString("fuel"), 
                hasLpg = o.optBoolean("hasLpg"), 
                notes = o.optString("notes"),
                imageUri = o.optString("imageUri") // <--- ДОБАВЛЕНО ФОТО
            ))
        }
    } catch (e: Exception) { 
        e.printStackTrace() 
    }
    return list
}

fun saveCars(context: Context, cars: List<Car>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    cars.forEach { car ->
        val o = JSONObject()
        o.put("id", car.id)
        o.put("brand", car.brand)
        o.put("model", car.model)
        o.put("year", car.year)
        o.put("vin", car.vin)
        o.put("mileage", car.mileage)
        o.put("engine", car.engine)
        o.put("fuel", car.fuel)
        o.put("hasLpg", car.hasLpg)
        o.put("notes", car.notes)
        o.put("imageUri", car.imageUri) // <--- ДОБАВЛЕНО ФОТО
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
            list.add(MaintenanceWork(
                id = o.optLong("id"), 
                carId = o.optLong("carId"), 
                name = o.optString("name"),
                intervalKm = o.optInt("intervalKm"), 
                intervalMonths = o.optInt("intervalMonths"), 
                lastMileage = o.optInt("lastMileage"),
                lastDate = o.optLong("lastDate"), 
                hasLastExecution = o.optBoolean("hasLastExecution"), 
                skipped = o.optBoolean("skipped"),
                preventive = o.optBoolean("preventive")
            ))
        }
    } catch (e: Exception) { 
        e.printStackTrace() 
    }
    return list
}

fun saveWorks(context: Context, works: List<MaintenanceWork>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    works.forEach { work ->
        val o = JSONObject()
        o.put("id", work.id)
        o.put("carId", work.carId)
        o.put("name", work.name)
        o.put("intervalKm", work.intervalKm)
        o.put("intervalMonths", work.intervalMonths)
        o.put("lastMileage", work.lastMileage)
        o.put("lastDate", work.lastDate)
        o.put("hasLastExecution", work.hasLastExecution)
        o.put("skipped", work.skipped)
        o.put("preventive", work.preventive)
        array.put(o)
    }
    prefs.edit().putString("works", array.toString()).apply()
}
