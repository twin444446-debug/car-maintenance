package com.example.carmaintenance

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// === МОДЕЛИ ДАННЫХ ===
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

// === ЦВЕТА ===
val BgColor = Color(0xFF0A1128)
val CardColor = Color(0xFF111A32)
val NavColor = Color(0xFF0D1526)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8E9CB2)
val ColorGreen = Color(0xFF00C853)
val ColorRed = Color(0xFFE74C3C)
val ColorYellow = Color(0xFFF1C40F)
val ColorBlue = Color(0xFF3498DB)
val ColorPurple = Color(0xFF9C27B0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppScreen()
            }
        }
    }
}

// === ЛОГИКА СОХРАНЕНИЯ ===
fun loadCars(context: Context): List<Car> {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val jsonStr = prefs.getString("cars", "[]") ?: "[]"
    val list = mutableListOf<Car>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            list.add(Car(o.optLong("id"), o.optString("brand"), o.optString("model"),
                o.optString("year"), o.optString("vin"), o.optString("mileage"),
                o.optString("engine"), o.optString("fuel"), o.optBoolean("hasLpg"), o.optString("notes")))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveCars(context: Context, cars: List<Car>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    cars.forEach { car ->
        val o = JSONObject()
        o.put("id", car.id); o.put("brand", car.brand); o.put("model", car.model)
        o.put("year", car.year); o.put("vin", car.vin); o.put("mileage", car.mileage)
        o.put("engine", car.engine); o.put("fuel", car.fuel); o.put("hasLpg", car.hasLpg)
        o.put("notes", car.notes)
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
            list.add(MaintenanceWork(o.optLong("id"), o.optLong("carId"), o.optString("name"),
                o.optInt("intervalKm"), o.optInt("intervalMonths"), o.optInt("lastMileage"),
                o.optLong("lastDate"), o.optBoolean("hasLastExecution"), o.optBoolean("skipped"),
                o.optBoolean("preventive")))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveWorks(context: Context, works: List<MaintenanceWork>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    works.forEach { work ->
        val o = JSONObject()
        o.put("id", work.id); o.put("carId", work.carId); o.put("name", work.name)
        o.put("intervalKm", work.intervalKm); o.put("intervalMonths", work.intervalMonths)
        o.put("lastMileage", work.lastMileage); o.put("lastDate", work.lastDate)
        o.put("hasLastExecution", work.hasLastExecution); o.put("skipped", work.skipped)
        o.put("preventive", work.preventive)
        array.put(o)
    }
    prefs.edit().putString("works", array.toString()).apply()
}

// === ОПРЕДЕЛЕНИЕ СТАТУСА ===
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

// === ГЛАВНЫЙ ЭКРАН С НАВИГАЦИЕЙ ===
@Composable
fun AppScreen() {
    val context = LocalContext.current
    var cars by remember { mutableStateOf(loadCars(context)) }
    var works by remember { mutableStateOf(loadWorks(context)) }
    
    var currentScreen by remember { mutableStateOf("Главная") }
    var selectedCarIndex by remember { mutableIntStateOf(0) }

    var showAddCarDialog by remember { mutableStateOf(false) }
    var showAddWorkDialog by remember { mutableStateOf(false) }
    var editingWork by remember { mutableStateOf<MaintenanceWork?>(null) }
    var actionWork by remember { mutableStateOf<MaintenanceWork?>(null) }

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
                    onAddCarClick = { showAddCarDialog = true }, 
                    onAddWorkClick = { if (selectedCar != null) { editingWork = null; showAddWorkDialog = true } },
                    onWorkClick = { actionWork = it })
                "Работы" -> WorkListScreen(works.filter { it.carId == selectedCar?.id })
                "Регламент" -> RegulationScreen()
                "Статистика" -> StatisticsScreen()
                "Ещё" -> MoreScreen()
            }
        }
    }

    if (showAddCarDialog) {
        AddCarDialog(onDismiss = { showAddCarDialog = false }, onSave = { newCar ->
            cars = cars + newCar
            saveCars(context, cars)
            showAddCarDialog = false
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
            }
        )
    }
}

// === ЭКРАНЫ ===
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(cars: List<Car>, works: List<MaintenanceWork>, selectedCarIndex: Int, onCarSwipe: (Int) -> Unit, onAddCarClick: () -> Unit, onAddWorkClick: () -> Unit, onWorkClick: (MaintenanceWork) -> Unit) {
    val selectedCar = cars.getOrNull(selectedCarIndex)
    val carWorks = works.filter { it.carId == selectedCar?.id }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (cars.isEmpty()) {
            item { EmptyGarageCard(onAddCarClick) }
        } else {
            item {
                val pagerState = rememberPagerState(pageCount = { cars.size })
                LaunchedEffect(pagerState.currentPage) { onCarSwipe(pagerState.currentPage) }
                HorizontalPager(state = pagerState, pageSpacing = 16.dp) { page -> CarCard(cars[page]) }
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
                val statusColor = when(status) { "Требуется", "Необходимо выполнить" -> ColorRed; "Пропущена" -> ColorPurple; else -> ColorGreen }
                WorkItemCard(work.name, status, statusColor, work.intervalKm) { onWorkClick(work) }
            }
        }
    }
}

@Composable
fun WorkListScreen(works: List<MaintenanceWork>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item { Text("Список всех работ", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)) }
        if (works.isEmpty()) {
            item { Text("Работ пока нет", color = TextSecondary) }
        } else {
            items(works.size) { index ->
                Text(works[index].name, color = TextPrimary, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable fun RegulationScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Экран Регламент в разработке", color = TextSecondary) } }
@Composable fun StatisticsScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Экран Статистика в разработке", color = TextSecondary) } }
@Composable fun MoreScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Экран Ещё в разработке", color = TextSecondary) } }

// === КОМПОНЕНТЫ И ДИАЛОГИ ===
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
fun AddCarDialog(onDismiss: () -> Unit, onSave: (Car) -> Unit) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = CardColor, title = { Text("Новый автомобиль", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Марка") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Модель") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Год выпуска") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = mileage, onValueChange = { mileage = it }, label = { Text("Пробег (км)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            }
        },
        confirmButton = { TextButton(onClick = { if (brand.isNotBlank() && model.isNotBlank()) onSave(Car(System.currentTimeMillis(), brand, model, year, "", mileage, "", "Бензин", false, "")) }) { Text("Сохранить", color = ColorGreen) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) } }
    )
}

@Composable
fun AddEditWorkDialog(existingWork: MaintenanceWork?, onDismiss: () -> Unit, onSave: (MaintenanceWork) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingWork?.name ?: "") }
    var intervalKm by remember { mutableStateOf(if ((existingWork?.intervalKm ?: 0) > 0) existingWork!!.intervalKm.toString() else "") }
    var intervalMonths by remember { mutableStateOf(if ((existingWork?.intervalMonths ?: 0) > 0) existingWork!!.intervalMonths.toString() else "") }
    var notDone by remember { mutableStateOf(existingWork?.hasLastExecution != true) }
    var lastMileage by remember { mutableStateOf(if (existingWork?.hasLastExecution == true) existingWork.lastMileage.toString() else "") }
    // Дата по умолчанию - сегодня, либо та, что уже была выбрана
    var lastDate by remember { mutableStateOf(if (existingWork?.lastDate ?: 0L > 0L) existingWork!!.lastDate else System.currentTimeMillis()) }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = CardColor, title = { Text(if (existingWork == null) "Новая работа" else "Изменить работу", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = intervalKm, onValueChange = { intervalKm = it }, label = { Text("Интервал, км") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                    OutlinedTextField(value = intervalMonths, onValueChange = { intervalMonths = it }, label = { Text("Интервал, мес") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notDone, onCheckedChange = { notDone = it }, colors = CheckboxDefaults.colors(checkedColor = ColorGreen))
                    Text("Не выполнялась", color = TextSecondary, fontSize = 14.sp)
                }
                
                // Поле даты - показываем всегда, чтобы можно было заносить задним числом
                val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(lastDate))
                Button(
                    onClick = {
                        val c = Calendar.getInstance(); c.timeInMillis = lastDate
                        DatePickerDialog(context, { _, y, m, d -> 
                            lastDate = Calendar.getInstance().apply { set(y, m, d, 12, 0, 0) }.timeInMillis 
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2A45))
                ) { Text("Дата выполнения: $dateStr", color = TextPrimary) }

                // Поле пробега при выполнении - скрываем, если "Не выполнялась"
                if (!notDone) {
                    OutlinedTextField(value = lastMileage, onValueChange = { lastMileage = it }, label = { Text("Пробег при выполнении") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(MaintenanceWork(
                        existingWork?.id ?: System.currentTimeMillis(), 
                        existingWork?.carId ?: 0L, 
                        name, 
                        intervalKm.toIntOrNull() ?: 0, 
                        intervalMonths.toIntOrNull() ?: 0, 
                        lastMileage.toIntOrNull() ?: 0, 
                        lastDate, // сохраняем выбранную дату
                        !notDone, 
                        existingWork?.skipped ?: false
                    ))
                }
            }) { Text("Сохранить", color = ColorGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) } }
    )
}

@Composable
fun WorkActionsDialog(work: MaintenanceWork, onDismiss: () -> Unit, onComplete: (Int) -> Unit, onSkip: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showCompleteInput by remember { mutableStateOf(false) }
    var mileageInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = CardColor, title = { Text(work.name, color = TextPrimary) },
        text = {
            if (showCompleteInput) {
                Column { 
                    Text("Введите текущий пробег:", color = TextSecondary); 
                    OutlinedTextField(value = mileageInput, onValueChange = { mileageInput = it }, label = { Text("Пробег (км)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)) 
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showCompleteInput = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ColorGreen)) { Text("Выполнить") }
                    Button(onClick = onSkip, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ColorYellow)) { Text("Пропустить", color = Color.Black) }
                    Button(onClick = onEdit, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ColorBlue)) { Text("Редактировать") }
                    Button(onClick = onDelete, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ColorRed)) { Text("Удалить") }
                }
            }
        },
        confirmButton = { if (showCompleteInput) TextButton(onClick = { onComplete(mileageInput.toIntOrNull() ?: 0) }) { Text("Сохранить", color = ColorGreen) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) } }
    )
}

@Composable
fun CarCard(car: Car) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Column {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E2A45)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Home, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)) }
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, CardColor.copy(alpha = 0.9f)), startY = 300f)))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text("${car.brand} ${car.model}", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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

@Composable fun SpecItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { Icon(if(title == "Всего работ") Icons.Default.Check else Icons.Default.Warning, null, tint = color) }
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
