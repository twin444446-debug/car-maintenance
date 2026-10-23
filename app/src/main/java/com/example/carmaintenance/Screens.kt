package com.example.carmaintenance

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// === ГЛАВНЫЙ КОНТЕЙНЕР (AppScreen) ===
@Composable
fun AppScreen() {
    val context = LocalContext.current
    var cars by remember { mutableStateOf(loadCars(context)) }
    var works by remember { mutableStateOf(loadWorks(context)) }
    var history by remember { mutableStateOf(loadHistory(context)) } // НОВОЕ
    
    var currentScreen by remember { mutableStateOf("Главная") }
    var selectedCarIndex by remember { mutableIntStateOf(0) }

    var showAddCarDialog by remember { mutableStateOf(false) }
    var editingCar by remember { mutableStateOf<Car?>(null) }
    var showAddWorkDialog by remember { mutableStateOf(false) }
    var editingWork by remember { mutableStateOf<MaintenanceWork?>(null) }
    var actionWork by remember { mutableStateOf<MaintenanceWork?>(null) }
    var infoCar by remember { mutableStateOf<Car?>(null) }
    var updatingMileageCar by remember { mutableStateOf<Car?>(null) }

    val selectedCar = cars.getOrNull(selectedCarIndex)

    Scaffold(
        containerColor = BgColor,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().background(BgColor).statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                Text(currentScreen, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = NavColor) {
                val navItems = listOf(
                    Triple("Главная", Icons.Filled.Home, Icons.Outlined.Home),
                    Triple("Работы", Icons.Filled.List, Icons.Outlined.List), // Здесь теперь будет журнал
                    Triple("Регламент", Icons.Filled.DateRange, Icons.Outlined.DateRange),
                    Triple("Статистика", Icons.Filled.Info, Icons.Outlined.Info),
                    Triple("Ещё", Icons.Filled.MoreVert, Icons.Outlined.MoreVert)
                )
                navItems.forEach { (title, filledIcon, outlinedIcon) ->
                    NavigationBarItem(icon = { Icon(if (currentScreen == title) filledIcon else outlinedIcon, contentDescription = title) }, label = { Text(title, fontSize = 10.sp) }, selected = currentScreen == title, onClick = { currentScreen = title }, colors = NavigationBarItemDefaults.colors(selectedIconColor = ColorGreen, selectedTextColor = ColorGreen, indicatorColor = ColorGreen.copy(alpha = 0.1f), unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary))
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentScreen) {
                "Главная" -> HomeScreenContent(cars, works, selectedCarIndex, onCarSwipe = { selectedCarIndex = it }, onAddCarClick = { editingCar = null; showAddCarDialog = true }, onAddWorkClick = { if (selectedCar != null) { editingWork = null; showAddWorkDialog = true } }, onWorkClick = { actionWork = it }, onCarClick = { infoCar = it })
                "Работы" -> HistoryScreen(history.filter { it.carId == selectedCar?.id }, selectedCar, onAddWorkClick = { if (selectedCar != null) { editingWork = null; showAddWorkDialog = true } })
                "Регламент" -> RegulationScreen(works.filter { it.carId == selectedCar?.id }, selectedCar, onWorkClick = { actionWork = it }, onAddClick = { if (selectedCar != null) { editingWork = null; showAddWorkDialog = true } })
                "Статистика" -> StatisticsScreen(works.filter { it.carId == selectedCar?.id }, selectedCar)
                "Ещё" -> MoreScreen()
            }
        }
    }

    if (showAddCarDialog) {
        AddCarDialog(existingCar = editingCar, onDismiss = { showAddCarDialog = false; editingCar = null }, onSave = { newCar ->
            if (editingCar == null) cars = cars + newCar else cars = cars.map { if (it.id == newCar.id) newCar else it }
            saveCars(context, cars); showAddCarDialog = false; editingCar = null
        })
    }

    if (updatingMileageCar != null) {
        UpdateMileageDialog(currentMileage = updatingMileageCar!!.mileage, onDismiss = { updatingMileageCar = null }, onSave = { newMileage ->
            cars = cars.map { if (it.id == updatingMileageCar!!.id) it.copy(mileage = newMileage) else it }
            saveCars(context, cars); updatingMileageCar = null
        })
    }

    if (infoCar != null) {
        CarInfoDialog(car = infoCar!!, onDismiss = { infoCar = null }, onEdit = { editingCar = infoCar; showAddCarDialog = true; infoCar = null }, onFillDefaults = { 
            val currentCar = infoCar!!; var newWorks = getDefaultWorks(currentCar.id); if (currentCar.hasLpg) newWorks = newWorks + getLpgDefaultWorks(currentCar.id)
            works = works.filter { it.carId != currentCar.id } + newWorks; saveWorks(context, works); infoCar = null 
        }, onUpdateMileage = { updatingMileageCar = infoCar; infoCar = null })
    }

    if (showAddWorkDialog && selectedCar != null) {
        AddEditWorkDialog(existingWork = editingWork, onDismiss = { showAddWorkDialog = false }, onSave = { work ->
            val updatedWork = work.copy(carId = selectedCar.id)
            works = if (editingWork == null) works + updatedWork else works.map { if (it.id == updatedWork.id) updatedWork else it }
            saveWorks(context, works); showAddWorkDialog = false
        })
    }

    if (actionWork != null) {
        WorkActionsDialog(work = actionWork!!, onDismiss = { actionWork = null },
            onComplete = { newMileage, newDate, newNotes -> 
                // 1. Обновляем статус самой работы
                works = works.map { if (it.id == actionWork!!.id) it.copy(lastMileage = newMileage, lastDate = newDate, hasLastExecution = true, skipped = false) else it }
                saveWorks(context, works)
                // 2. Добавляем запись в историю
                val entry = WorkHistory(System.currentTimeMillis(), selectedCar!!.id, actionWork!!.name, newDate, newMileage, newNotes.ifBlank { "Регламентное ТО" })
                history = history + entry
                saveHistory(context, history)
                actionWork = null 
            },
            onSkip = { works = works.map { if (it.id == actionWork!!.id) it.copy(skipped = true) else it }; saveWorks(context, works); actionWork = null },
            onEdit = { editingWork = actionWork; actionWork = null; showAddWorkDialog = true },
            onDelete = { works = works.filter { it.id != actionWork!!.id }; saveWorks(context, works); actionWork = null },
            onNotDone = { works = works.map { if (it.id == actionWork!!.id) it.copy(skipped = false, hasLastExecution = false) else it }; saveWorks(context, works); actionWork = null }
        )
    }
}

// === ГЛАВНЫЙ ЭКРАН (С РАЗДЕЛЕНИЕМ НА ГБО) ===
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(cars: List<Car>, works: List<MaintenanceWork>, selectedCarIndex: Int, onCarSwipe: (Int) -> Unit, onAddCarClick: () -> Unit, onAddWorkClick: () -> Unit, onWorkClick: (MaintenanceWork) -> Unit, onCarClick: (Car) -> Unit) {
    val selectedCar = cars.getOrNull(selectedCarIndex)
    val carWorks = works.filter { it.carId == selectedCar?.id }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (cars.isEmpty()) item { EmptyGarageCard(onAddCarClick) }
        else {
            item {
                val pagerState = rememberPagerState(pageCount = { cars.size })
                LaunchedEffect(pagerState.currentPage) { onCarSwipe(pagerState.currentPage) }
                HorizontalPager(state = pagerState, pageSpacing = 16.dp) { page -> CarCard(cars[page], onClick = { onCarClick(cars[page]) }) }
                if (cars.size > 1) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
                        repeat(cars.size) { i -> Box(modifier = Modifier.padding(horizontal = 4.dp).size(8.dp).background(if (i == pagerState.currentPage) ColorGreen else TextSecondary.copy(alpha = 0.3f), CircleShape)) }
                    }
                }
            }
        }
        if (selectedCar != null) {
            item {
                val requires = carWorks.count { getWorkStatus(it, selectedCar) == "Требуется" || getWorkStatus(it, selectedCar) == "Необходимо выполнить" }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Всего работ", carWorks.size.toString(), ColorGreen, Modifier.weight(1f))
                    StatCard("Требуют", requires.toString(), ColorRed, Modifier.weight(1f))
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionBtn("Добавить авто", Icons.Default.Home, ColorGreen, Modifier.weight(1f), onAddCarClick)
                QuickActionBtn("Добавить работу", Icons.Default.Build, ColorPurple, Modifier.weight(1f), onAddWorkClick)
            }
        }
        item { Text("Ближайшие работы", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        if (carWorks.isEmpty()) item { Text("Работ пока нет", color = TextSecondary) }
        else {
            items(carWorks.size) { index ->
                val work = carWorks[index]
                val status = getWorkStatus(work, selectedCar!!)
                val statusColor = when(status) { "Требуется", "Необходимо выполнить" -> ColorRed; "Пропущена" -> ColorPurple; "Скоро" -> ColorYellow; else -> ColorGreen }
                val currentMileage = selectedCar.mileage.toIntOrNull() ?: 0
                WorkItemCard(work.name, status, statusColor, work.intervalKm, work.intervalMonths, work.lastMileage, currentMileage, work.name.startsWith("ГБО:")) { onWorkClick(work) }
            }
        }
    }
}

// === ЖУРНАЛ РАБОТ (БЫВШАЯ ИСТОРИЯ) ===
@Composable
fun HistoryScreen(history: List<WorkHistory>, selectedCar: Car?, onAddWorkClick: () -> Unit) {
    if (selectedCar == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Сначала добавьте авто", color = TextSecondary) }; return }
    
    // Сортируем историю по дате (сначала новые)
    val sortedHistory = history.sortedByDescending { it.date }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Журнал работ: ${selectedCar.brand} ${selectedCar.model}", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            if (sortedHistory.isEmpty()) {
                item { Text("Записей пока нет. Выполните работу в регламенте или добавьте ремонт.", color = TextSecondary) }
            } else {
                items(sortedHistory.size) { index ->
                    val record = sortedHistory[index]
                    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(record.date))
                    Card(colors = CardDefaults.cardColors(containerColor = CardColor), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(record.workName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(dateStr, color = TextSecondary, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Пробег: ${record.mileage} км", color = ColorBlue, fontSize = 13.sp)
                            if (record.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Примечание: ${record.notes}", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
        
        Button(onClick = onAddWorkClick, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = ColorPurple)) { 
            Text("Добавить запись (ремонт)", color = TextPrimary) 
        }
    }
}

// === РЕГЛАМЕНТ (С РАЗДЕЛЕНИЕМ) ===
@Composable
fun RegulationScreen(works: List<MaintenanceWork>, selectedCar: Car?, onWorkClick: (MaintenanceWork) -> Unit, onAddClick: () -> Unit) {
    if (selectedCar == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Сначала добавьте авто", color = TextSecondary) }; return }
    
    val regularWorks = works.filter { !it.name.startsWith("ГБО:") }
    val lpgWorks = works.filter { it.name.startsWith("ГБО:") }
    val currentMileage = selectedCar.mileage.toIntOrNull() ?: 0

    val reqRegular = regularWorks.filter { getWorkStatus(it, selectedCar) == "Требуется" || getWorkStatus(it, selectedCar) == "Необходимо выполнить" || getWorkStatus(it, selectedCar) == "Пропущена" }
    val soonRegular = regularWorks.filter { getWorkStatus(it, selectedCar) == "Скоро" }
    val okRegular = regularWorks.filter { getWorkStatus(it, selectedCar) == "Выполнено" }

    val reqLpg = lpgWorks.filter { getWorkStatus(it, selectedCar) == "Требуется" || getWorkStatus(it, selectedCar) == "Необходимо выполнить" || getWorkStatus(it, selectedCar) == "Пропущена" }
    val soonLpg = lpgWorks.filter { getWorkStatus(it, selectedCar) == "Скоро" }
    val okLpg = lpgWorks.filter { getWorkStatus(it, selectedCar) == "Выполнено" }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Регламент: ${selectedCar.brand} ${selectedCar.model} (${selectedCar.mileage} км)", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold) }

            if (regularWorks.isNotEmpty()) {
                item { Text("🚗 Автомобиль", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
                if (reqRegular.isNotEmpty()) {
                    item { RegulationHeader("Требуют внимания", ColorRed) }
                    items(reqRegular.size) { i -> val w = reqRegular[i]; WorkItemCard(w.name, getWorkStatus(w, selectedCar), if (getWorkStatus(w, selectedCar) == "Пропущена") ColorPurple else ColorRed, w.intervalKm, w.intervalMonths, w.lastMileage, currentMileage, false) { onWorkClick(w) } }
                }
                if (soonRegular.isNotEmpty()) {
                    item { RegulationHeader("Скоро", ColorYellow) }
                    items(soonRegular.size) { i -> val w = soonRegular[i]; WorkItemCard(w.name, getWorkStatus(w, selectedCar), ColorYellow, w.intervalKm, w.intervalMonths, w.lastMileage, currentMileage, false) { onWorkClick(w) } }
                }
                if (okRegular.isNotEmpty()) {
                    item { RegulationHeader("В порядке", ColorGreen) }
                    items(okRegular.size) { i -> val w = okRegular[i]; WorkItemCard(w.name, getWorkStatus(w, selectedCar), ColorGreen, w.intervalKm, w.intervalMonths, w.lastMileage, currentMileage, false) { onWorkClick(w) } }
                }
            }

            if (lpgWorks.isNotEmpty()) {
                item { Text("⛽ ГБО", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp)) }
                if (reqLpg.isNotEmpty()) {
                    item { RegulationHeader("Требуют внимания", ColorRed) }
                    items(reqLpg.size) { i -> val w = reqLpg[i]; WorkItemCard(w.name, getWorkStatus(w, selectedCar), if (getWorkStatus(w, selectedCar) == "Пропущена") ColorPurple else ColorRed, w.intervalKm, w.intervalMonths, w.lastMileage, currentMileage, true) { onWorkClick(w) } }
                }
                if (soonLpg.isNotEmpty()) {
                    item { RegulationHeader("Скоро", ColorYellow) }
                    items(soonLpg.size) { i -> val w = soonLpg[i]; WorkItemCard(w.name, getWorkStatus(w, selectedCar), ColorYellow, w.intervalKm, w.intervalMonths, w.lastMileage, currentMileage, true) { onWorkClick(w) } }
                }
                if (okLpg.isNotEmpty()) {
                    item { RegulationHeader("В порядке", ColorGreen) }
                    items(okLpg.size) { i -> val w = okLpg[i]; WorkItemCard(w.name, getWorkStatus(w, selectedCar), ColorGreen, w.intervalKm, w.intervalMonths, w.lastMileage, currentMileage, true) { onWorkClick(w) } }
                }
            }

            if (works.isEmpty()) item { Text("В регламенте пока нет работ", color = TextSecondary) }
        }
        Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth().padding(16.dp), colors = ButtonDefaults.buttonColors(containerColor = ColorPurple)) { Text("Добавить работу", color = TextPrimary) }
    }
}

@Composable
fun RegulationHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatisticsScreen(works: List<MaintenanceWork>, selectedCar: Car?) {
    if (selectedCar == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Сначала добавьте авто", color = TextSecondary) }; return }
    val total = works.size
    val requires = works.count { getWorkStatus(it, selectedCar) == "Требуется" || getWorkStatus(it, selectedCar) == "Необходимо выполнить" }
    val soon = works.count { getWorkStatus(it, selectedCar) == "Скоро" }
    val completed = works.count { getWorkStatus(it, selectedCar) == "Выполнено" }
    val skipped = works.count { getWorkStatus(it, selectedCar) == "Пропущена" }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Статистика по ${selectedCar.brand} ${selectedCar.model}", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        StatCard("Всего работ", total.toString(), ColorBlue, Modifier.fillMaxWidth())
        StatCard("Требуют внимания", requires.toString(), ColorRed, Modifier.fillMaxWidth())
        StatCard("Скоро (Предупредительные)", soon.toString(), ColorYellow, Modifier.fillMaxWidth())
        StatCard("Выполнено", completed.toString(), ColorGreen, Modifier.fillMaxWidth())
        StatCard("Пропущено", skipped.toString(), ColorPurple, Modifier.fillMaxWidth())
    }
}

@Composable fun MoreScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Экран Ещё в разработке", color = TextSecondary) } }
