package com.example.carmaintenance

import java.util.Calendar

fun getWorkStatus(work: MaintenanceWork, car: Car): String {
    if (work.skipped) return "Пропущена"
    if (!work.hasLastExecution) return "Необходимо выполнить"
    val currentMileage = car.mileage.toIntOrNull() ?: 0
    val dueByKm = work.intervalKm > 0 && currentMileage >= work.lastMileage + work.intervalKm
    var dueByDate = false
    if (work.intervalMonths > 0 && work.lastDate > 0L) {
        val date = Calendar.getInstance().apply { timeInMillis = work.lastDate; add(Calendar.MONTH, work.intervalMonths) }
        dueByDate = System.currentTimeMillis() >= date.timeInMillis
    }
    return if (dueByKm || dueByDate) "Требуется" else "Выполнено"
}
