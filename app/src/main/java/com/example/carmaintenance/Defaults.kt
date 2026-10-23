package com.example.carmaintenance

fun getDefaultWorks(carId: Long): List<MaintenanceWork> {
    return listOf(
        MaintenanceWork(id = 1, carId = carId, name = "Масло в двигателе", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(id = 2, carId = carId, name = "Масло в мех. коробке передач", intervalKm = 90000, intervalMonths = 60),
        MaintenanceWork(id = 3, carId = carId, name = "Фильтр масляный", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(id = 4, carId = carId, name = "Фильтр топливный (тонкой очистки)", intervalKm = 20000, intervalMonths = 24),
        MaintenanceWork(id = 5, carId = carId, name = "Фильтр топливный (сеточка)", intervalKm = 40000, intervalMonths = 48),
        MaintenanceWork(id = 6, carId = carId, name = "Фильтр воздушный", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(id = 7, carId = carId, name = "Фильтр кондиционера (салона)", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(id = 8, carId = carId, name = "Свечи зажигания", intervalKm = 20000, intervalMonths = 24),
        MaintenanceWork(id = 9, carId = carId, name = "Чистка инжектора (форсунок)", intervalKm = 20000, intervalMonths = 24),
        MaintenanceWork(id = 10, carId = carId, name = "Высоковольтные провода", intervalKm = 40000, intervalMonths = 48),
        MaintenanceWork(id = 11, carId = carId, name = "Датчик CO2 4 контакта", intervalKm = 100000, intervalMonths = 120),
        MaintenanceWork(id = 12, carId = carId, name = "Помпа (водяной насос)", intervalKm = 60000, intervalMonths = 60),
        MaintenanceWork(id = 13, carId = carId, name = "Тормозная жидкость DOT4", intervalKm = 30000, intervalMonths = 36),
        MaintenanceWork(id = 14, carId = carId, name = "Жидкость охлаждающая", intervalKm = 40000, intervalMonths = 24),
        MaintenanceWork(id = 15, carId = carId, name = "Колодки тормозные передние", intervalKm = 0, intervalMonths = 0, preventive = true),
        MaintenanceWork(id = 16, carId = carId, name = "Колодки тормозные задние", intervalKm = 0, intervalMonths = 0, preventive = true),
        MaintenanceWork(id = 17, carId = carId, name = "Ремни привода ГРМ, кондея, генератора, ролики", intervalKm = 60000, intervalMonths = 60)
    )
}

fun getLpgDefaultWorks(carId: Long): List<MaintenanceWork> {
    return listOf(
        MaintenanceWork(id = 101, carId = carId, name = "ГБО: Фильтр жидкой фракции (в редукторе)", intervalKm = 15000, intervalMonths = 12),
        MaintenanceWork(id = 102, carId = carId, name = "ГБО: Фильтр паровой фракции (тонкой очистки)", intervalKm = 15000, intervalMonths = 12),
        MaintenanceWork(id = 103, carId = carId, name = "ГБО: BARRACUDA (замена)", intervalKm = 30000, intervalMonths = 36),
        MaintenanceWork(id = 104, carId = carId, name = "ГБО: Переборка редуктора (ремкомплект)", intervalKm = 50000, intervalMonths = 0, preventive = true)
    )
}
