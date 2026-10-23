package com.example.carmaintenance

data class Car(
    val id: Long, var brand: String, var model: String, var year: String,
    var vin: String, var mileage: String, var engine: String, var fuel: String,
    var hasLpg: Boolean, var notes: String
)

data class MaintenanceWork(
    val id: Long, val carId: Long, var name: String,
    var intervalKm: Int, var intervalMonths: Int,
    var lastMileage: Int = 0, var lastDate: Long = 0L,
    var hasLastExecution: Boolean = false, var skipped: Boolean = false,
    var preventive: Boolean = false
)
