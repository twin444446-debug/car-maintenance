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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// === ГЛАВНЫЙ КОНТЕЙНЕР (AppScreen) ===
@Composable
fun AppScreen() {
    val context = LocalContext.current
    var cars by remember { mutableStateOf(loadCars(context)) }
    var works by remember { mutableStateOf(loadWorks(context)) }
    
    var currentScreen by remember { mutableStateOf("Главная") }
    var selectedCarIndex by remember { mutableIntStateOf(0) }

    var showAddCarDialog by remember { mutableStateOf(false) }
    var editingCar by remember { mutableStateOf<Car?>(null) }
    var showAddWorkDialog by remember { mutableStateOf(false) }
    var editingWork by remember { mutableStateOf<MaintenanceWork?>(null) }
    var actionWork by remember { mutableStateOf<MaintenanceWork?>(null) }
    var infoCar by remember { mutableStateOf<Car?>(null) }
    var updatingMileageCar by remember { mutableStateOf<Car?>(null) } // ДЛЯ ОБНОВЛЕНИЯ ПРОБЕГА

    val selectedCar = cars.getOrNull(selectedCarIndex)

    Scaffold(
        containerColor = BgColor,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(BgColor).statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                Text(currentScreen, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = NavColor) {
                val navItems = listOf(
                    Triple("Главная", Icons.Filled.Home, Icons.Outlined.Home),
                    Triple("Работы", Icons.Filled.List, Icons.Outlined.List),
                    Triple("Регламент", Icons.Filled.DateRange, Icons.Outlined.DateRange),
                    Triple("Статистика", Icons.Filled.Info, Icons.Outlined.Info),
                    Triple("Ещё", Icons.Filled.MoreVert, Icons.Outlined.MoreVert)
                )
                navItems.forEach { (title, filledIcon, outlinedIcon) ->
                    NavigationBarItem(
                        icon = { Icon(if (currentScreen == title) filledIcon else outlinedIcon, contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp) },
                        selected = currentScreen == title,
                        onClick = { currentScreen = title },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ColorGreen,
                            selectedTextColor = ColorGreen,
                            indicatorColor = ColorGreen.copy(alpha = 0.1f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentScreen) {
                "Главная" -> HomeScreenContent(cars, works, selectedCarIndex, onCarSwipe = { selectedCarIndex = it }, 
                    onAddCarClick = { editingCar = null; showAddCarDialog = true }, 
                    onAddWorkClick = { if (selectedCar != null) { editingWork = null; showAddWorkDialog = true } },
                    onWorkClick = { actionWork = it },
                    onCarClick = { infoCar = it }
                )
                "Работы" -> WorkListScreen(works.filter { it.carId == selectedCar?.id }, selectedCar, onAddClick = { 
                    if (selectedCar != null) { editingWork = null; showAddWorkDialog = true } 
                })
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
        UpdateMileageDialog(
            currentMileage = updatingMileageCar!!.mileage,
            onDismiss = { updatingMileageCar = null },
            onSave = { newMileage ->
                cars = cars.map { if (it.id == updatingMileageCar!!.id) it.copy(mileage = newMileage) else it }
                saveCars(context, cars)
                updatingMileageCar = null
            }
        )
    }

    if (infoCar != null) {
        CarInfoDialog(
            car = infoCar!!, 
            onDismiss = { infoCar = null }, 
            onEdit = { editingCar = infoCar; showAddCarDialog = true; infoCar = null },
            onFillDefaults = { 
                val currentCar = infoCar!!
                var newWorks = getDefaultWorks(currentCar.id)
                if (currentCar.hasLpg) newWorks = newWorks + getLpgDefaultWorks(currentCar.id)
                works = works.filter { it.carId != currentCar.id } + newWorks
                saveWorks(context, works); infoCar = null 
            },
            onUpdateMileage = { updatingMileageCar = infoCar; infoCar = null }
        )
    }

    if (showAddWorkDialog && selectedCar != null) {
        AddEditWorkDialog(existingWork = editingWork, onDismiss = { showAddWorkDialog = false }, onSave = { work ->
            val updatedWork = work.copy(carId = selectedCar.id)
            works = if (editingWork == null) works + updatedWork else works.map { if (it.id == updatedWork.id) updatedWork else it }
            saveWorks(context, works)
            showAddWorkDialog = false
        })
    }

    if (actionWork != null) {
        WorkActionsDialog(
            work = actionWork!!, onDismiss = { actionWork = null },
            onComplete = { newMileage -> works = works.map { if (it.id == actionWork!!.id) it.copy(lastMileage = newMileage, lastDate = System.currentTimeMillis(), hasLastExecution = true, skipped = false) else it }; saveWorks(context, works); actionWork = null },
            onSkip = { works = works.map { if (it.id == actionWork!!.id) it.copy(skipped = true) else it }; saveWorks(context, works); actionWork = null },
            onEdit = { editingWork = actionWork; actionWork = null; showAddWorkDialog = true },
            onDelete = { works = works.filter { it.id != actionWork!!.id }; saveWorks(context, works); actionWork = null },
            onNotDone = { works = works.map { if (it.id == actionWork!!.id) it.copy(skipped = false, hasLastExecution = false) else it }; saveWorks(context, works); actionWork = null }
        )
    }
}

// === ГЛАВНЫЙ ЭКРАН ===
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
                WorkItemCard(work.name, status, statusColor, work.intervalKm) { onWorkClick(work) }
            }
        }
    }
}

// === ЭКРАН РАБОТ ===
@Composable
fun WorkListScreen(works: List<MaintenanceWork>, selectedCar: Car?, onAddClick: () -> Unit) {
    if (selectedCar == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Сначала добавьте авто", color = TextSecondary) }; return }
    var filter by remember { mutableStateOf("Все") }
    val filteredWorks = when(filter) {
        "Требуют" -> works.filter { getWorkStatus(it, selectedCar) == "Требуется" || getWorkStatus(it, selectedCar) == "Необходимо выполнить" }
        "Выполнено" -> works.filter { getWorkStatus(it, selectedCar) == "Выполнено" }
        else -> works
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Все", "Требуют", "Выполнено").forEach { f ->
                Button(onClick = { filter = f }, colors = ButtonDefaults.buttonColors(containerColor = if (filter == f) ColorBlue else CardColor)) { Text(f, color = TextPrimary) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            if (filteredWorks.isEmpty()) item { Text("Работ нет", color = TextSecondary) }
            else {
                items(filteredWorks.size) { index ->
                    val work = filteredWorks[index]
                    val status = getWorkStatus(work, selectedCar)
                    val statusColor = when(status) { "Требуется", "Необходимо выполнить" -> ColorRed; "Пропущена" -> ColorPurple; "Скоро" -> ColorYellow; else -> ColorGreen }
                    WorkItemCard(work.name, status, statusColor, work.intervalKm) { }
                }
            }
        }
        Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = ColorPurple)) { Text("Добавить работу", color = TextPrimary) }
    }
}

// === ЭКРАН РЕГЛАМЕНТА ===
@Composable
fun RegulationScreen(works: List<MaintenanceWork>, selectedCar: Car?, onWorkClick: (MaintenanceWork) -> Unit, onAddClick: () -> Unit) {
    if (selectedCar == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Сначала добавьте авто", color = TextSecondary) }; return }
    val requiresAttention = works.filter { getWorkStatus(it, selectedCar) == "Требуется" || getWorkStatus(it, selectedCar) == "Необходимо выполнить" || getWorkStatus(it, selectedCar) == "Пропущена" }
    val soonWorks = works.filter { getWorkStatus(it, selectedCar) == "Скоро" }
    val inOrderWorks = works.filter { getWorkStatus(it, selectedCar) == "Выполнено" }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text
