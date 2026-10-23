package com.example.carmaintenance

data class Car(
    val id: Long, var brand: String, var model: String, var year: String,
    var vin: String, var mileage: String, var engine: String, var fuel: String,
    var hasLpg: Boolean, var notes: String, var imageUri: String = ""
)

data class MaintenanceWork(
    val id: Long, val carId: Long, var name: String,
    var intervalKm: Int, var intervalMonths: Int,
    var lastMileage: Int = 0, var lastDate: Long = 0L,
    var hasLastExecution: Boolean = false, var skipped: Boolean = false,
    var preventive: Boolean = false,
    var notes: String = "" // Поле для примечаний
)

// Класс для журнала выполненных работ
data class WorkHistory(
    val id: Long,
    val carId: Long,
    val workName: String,
    val date: Long,
    val mileage: Int,
    val notes: String
)
