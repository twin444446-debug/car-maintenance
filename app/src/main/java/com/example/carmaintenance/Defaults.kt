package com.example.carmaintenance

fun getDefaultWorks(carId: String): List<MaintenanceWork> {
    return listOf(
        MaintenanceWork(carId = carId, name = "Масло в двигателе", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(carId = carId, name = "Масло в мех. коробке передач", intervalKm = 90000, intervalMonths = 60),
        MaintenanceWork(carId = carId, name = "Фильтр масляный", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(carId = carId, name = "Фильтр топливный (тонкой очистки)", intervalKm = 20000, intervalMonths = 24),
        MaintenanceWork(carId = carId, name = "Фильтр топливный (сеточка)", intervalKm = 40000, intervalMonths = 48),
        MaintenanceWork(carId = carId, name = "Фильтр воздушный", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(carId = carId, name = "Фильтр кондиционера (салона)", intervalKm = 10000, intervalMonths = 12),
        MaintenanceWork(carId = carId, name = "Свечи зажигания", intervalKm = 20000, intervalMonths = 24),
        MaintenanceWork(carId = carId, name = "Чистка инжектора (форсунок)", intervalKm = 20000, intervalMonths = 24),
        MaintenanceWork(carId = carId, name = "Высоковольтные провода", intervalKm = 40000, intervalMonths = 48),
        MaintenanceWork(carId = carId, name = "Датчик CO2 4 контакта", intervalKm = 100000, intervalMonths = 120),
        MaintenanceWork(carId = carId, name = "Помпа (водяной насос)", intervalKm = 60000, intervalMonths = 60),
        MaintenanceWork(carId = carId, name = "Тормозная жидкость DOT4", intervalKm = 30000, intervalMonths = 36),
        MaintenanceWork(carId = carId, name = "Жидкость охлаждающая", intervalKm = 40000, intervalMonths = 24),
        MaintenanceWork(carId = carId, name = "Колодки тормозные передние", intervalKm = 0, intervalMonths = 0, preventive = true),
        MaintenanceWork(carId = carId, name = "Колодки тормозные задние", intervalKm = 0, intervalMonths = 0, preventive = true),
        MaintenanceWork(carId = carId, name = "Ремни привода ГРМ, кондея, генератора, ролики", intervalKm = 60000, intervalMonths = 60)
    )
}

fun getLpgDefaultWorks(carId: String): List<MaintenanceWork> {
    return listOf(
        MaintenanceWork(carId = carId, name = "ГБО: Фильтр жидкой фракции (в редукторе)", intervalKm = 15000, intervalMonths = 12),
        MaintenanceWork(carId = carId, name = "ГБО: Фильтр паровой фракции (тонкой очистки)", intervalKm = 15000, intervalMonths = 12),
        MaintenanceWork(carId = carId, name = "ГБО: BARRACUDA (замена)", intervalKm = 30000, intervalMonths = 36),
        MaintenanceWork(carId = carId, name = "ГБО: Переборка редуктора (ремкомплект)", intervalKm = 50000, intervalMonths = 0, preventive = true)
    )
}
