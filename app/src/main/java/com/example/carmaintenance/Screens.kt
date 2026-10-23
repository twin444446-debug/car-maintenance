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
                "Регламент" -> RegulationScreen()
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

    if (infoCar != null) {
        CarInfoDialog(car = infoCar!!, onDismiss = { infoCar = null }, onEdit = { 
            editingCar = infoCar
            showAddCarDialog = true
            infoCar = null 
        })
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
            onComplete = { newMileage ->
                works = works.map { if (it.id == actionWork!!.id) it.copy(lastMileage = newMileage, lastDate = System.currentTimeMillis(), hasLastExecution = true, skipped = false) else it }
                saveWorks(context, works); actionWork = null
            },
            onSkip = {
                works = works.map { if (it.id == actionWork!!.id) it.copy(skipped = true) else it }
                saveWorks(context, works); actionWork = null
            },
            onEdit = { editingWork = actionWork; actionWork = null; showAddWorkDialog = true },
            onDelete = {
                works = works.filter { it.id != actionWork!!.id }
                saveWorks(context, works); actionWork = null
            },
            onNotDone = {
                works = works.map { if (it.id == actionWork!!.id) it.copy(skipped = false, hasLastExecution = false) else it }
                saveWorks(context, works); actionWork = null
            }
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
        if (cars.isEmpty()) {
            item { EmptyGarageCard(onAddCarClick) }
        } else {
            item {
                val pagerState = rememberPagerState(pageCount = { cars.size })
                LaunchedEffect(pagerState.currentPage) { onCarSwipe(pagerState.currentPage) }
                HorizontalPager(state = pagerState, pageSpacing = 16.dp) { page -> 
                    CarCard(cars[page], onClick = { onCarClick(cars[page]) }) 
                }
                if (cars.size > 1) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
                        repeat(cars.size) { i ->
                            Box(modifier = Modifier.padding(horizontal = 4.dp).size(8.dp).background(if (i == pagerState.currentPage) ColorGreen else TextSecondary.copy(alpha = 0.3f), CircleShape))
                        }
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

        if (carWorks.isEmpty()) {
            item { Text("Работ пока нет", color = TextSecondary) }
        } else {
            items(carWorks.size) { index ->
                val work = carWorks[index]
                val status = getWorkStatus(work, selectedCar!!)
                val statusColor = when(status) { "Требуется", "Необходимо выполнить" -> ColorRed; "Пропущена" -> ColorPurple; "Скоро" -> ColorYellow; else -> ColorGreen }
                WorkItemCard(work.name, status, statusColor, work.intervalKm) { onWorkClick(work) }
            }
        }
    }
}

// === ЭКРАН РАБОТ (С ФИЛЬТРАМИ И КНОПКОЙ ДОБАВЛЕНИЯ) ===
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
                Button(onClick = { filter = f }, colors = ButtonDefaults.buttonColors(containerColor = if (filter == f) ColorBlue else CardColor)) {
                    Text(f, color = TextPrimary)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            if (filteredWorks.isEmpty()) {
                item { Text("Работ нет", color = TextSecondary) }
            } else {
                items(filteredWorks.size) { index ->
                    val work = filteredWorks[index]
                    val status = getWorkStatus(work, selectedCar)
                    val statusColor = when(status) { "Требуется", "Необходимо выполнить" -> ColorRed; "Пропущена" -> ColorPurple; "Скоро" -> ColorYellow; else -> ColorGreen }
                    WorkItemCard(work.name, status, statusColor, work.intervalKm) { }
                }
            }
        }
        Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = ColorPurple)) {
            Text("Добавить работу", color = TextPrimary)
        }
    }
}

// === ЭКРАН СТАТИСТИКИ ===
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

@Composable fun RegulationScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Экран Регламент в разработке", color = TextSecondary) } }
@Composable fun MoreScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Экран Ещё в разработке", color = TextSecondary) } }

// === КОМПОНЕНТЫ (КАРТОЧКИ) ===
@Composable
fun EmptyGarageCard(onAddClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardColor), onClick = onAddClick) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, null, tint = TextSecondary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Гараж пуст", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Нажмите, чтобы добавить авто", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun CarInfoDialog(car: Car, onDismiss: () -> Unit, onEdit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = CardColor, 
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${car.brand} ${car.model}", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (car.hasLpg) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = ColorBlue.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text("ГБО", color = ColorBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(label = "Год выпуска", value = car.year.ifBlank { "Не указан" })
                InfoRow(label = "Пробег", value = "${car.mileage} км")
                InfoRow(label = "VIN-код", value = car.vin.ifBlank { "Не указан" })
                InfoRow(label = "Двигатель", value = car.engine.ifBlank { "Не указан" })
                InfoRow(label = "Топливо", value = car.fuel)
                if (car.notes.isNotBlank()) {
                    InfoRow(label = "Примечания", value = car.notes)
                }
            }
        },
        confirmButton = { TextButton(onClick = onEdit) { Text("Редактировать", color = ColorBlue) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть", color = TextSecondary) } }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CarCard(car: Car, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, 
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                if (car.imageUri.isNotEmpty()) {
                    AsyncImage(model = Uri.parse(car.imageUri), contentDescription = "Car Photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E2A45)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Home, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)) }
                }
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, CardColor.copy(alpha = 0.9f)), startY = 300f)))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${car.brand} ${car.model}", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        if (car.hasLpg) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = ColorBlue.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text("ГБО", color = ColorBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text("${car.year} г.", color = TextSecondary, fontSize = 14.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecItem(Icons.Default.Info, "Пробег", "${car.mileage} км")
                SpecItem(Icons.Default.Info, "VIN", car.vin.ifBlank { "не указан" })
                SpecItem(Icons.Default.Build, "Двигатель", car.engine.ifBlank { "не указан" })
            }
        }
    }
}

@Composable
fun SpecItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = color) }
            Spacer(modifier = Modifier.width(12.dp))
            Column { Text(title, color = TextSecondary, fontSize = 12.sp); Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable fun QuickActionBtn(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor), onClick = onClick) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, color = TextPrimary, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable fun WorkItemCard(name: String, status: String, statusColor: Color, intervalKm: Int, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(statusColor.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { Icon(if(status == "Выполнено") Icons.Default.Check else Icons.Default.Warning, null, tint = statusColor) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Интервал: $intervalKm км", color = TextSecondary, fontSize = 12.sp)
            }
            Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)) { Text(status, color = statusColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Medium) }
        }
    }
}
