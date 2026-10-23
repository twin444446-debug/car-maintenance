package com.example.carmaintenance

// Функция возвращает список базовых работ (из твоей первой таблицы)
fun getDefaultWorks(carId: Long): List<MaintenanceWork> {
    return listOf(
        MaintenanceWork(id = 1, carId = carId, name = "Масло в двигателе", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(id = 2, carId = carId, name = "Фильтр масляный", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(id = 3, carId = carId, name = "Фильтр воздушный", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(id = 4, carId = carId, name = "Фильтр салона", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(id = 5, carId = carId, name = "Свечи зажигания", intervalKm = 20000, intervalMonths = 24),
        MaintenanceWork(id = 6, carId = carId, name = "Тормозная жидкость DOT4", intervalKm = 30000, intervalMonths = 36),
        MaintenanceWork(id = 7, carId = carId, name = "Жидкость охлаждающая", intervalKm = 40000, intervalMonths = 24),
        MaintenanceWork(id = 8, carId = carId, name = "Ремни привода ГРМ, кондея и генератора, ролики", intervalKm = 60000, intervalMonths = 60),
        MaintenanceWork(id = 9, carId = carId, name = "Масло в мех. коробке передач", intervalKm = 90000, intervalMonths = 0),
        MaintenanceWork(id = 10, carId = carId, name = "Колодки тормозные передние", intervalKm = 0, intervalMonths = 0, preventive = true),
        MaintenanceWork(id = 11, carId = carId, name = "Колодки тормозные задние", intervalKm = 0, intervalMonths = 0, preventive = true)
    )
}

// Функция возвращает список работ для ГБО (из твоей второй таблицы)
fun getLpgDefaultWorks(carId: Long): List<MaintenanceWork> {
    return listOf(
        MaintenanceWork(id = 101, carId = carId, name = "Фильтр жидкой фракции - грубой очистки (в редукторе)", intervalKm = 15000, intervalMonths = 12),
        MaintenanceWork(id = 102, carId = carId, name = "Фильтр паровой фракции - тонкой очистки", intervalKm = 15000, intervalMonths = 12),
        MaintenanceWork(id = 103, carId = carId, name = "BARRACUDA (не чистится - замена)", intervalKm = 30000, intervalMonths = 36),
        MaintenanceWork(id = 104, carId = carId, name = "Переборка редуктора (ремкомплект)", intervalKm = 50000, intervalMonths = 0, preventive = true)
    )
}
