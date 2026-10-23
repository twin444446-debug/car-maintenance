package com.example.carmaintenance

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

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
            list.add(Car(
                id = o.optLong("id"), brand = o.optString("brand"), model = o.optString("model"),
                year = o.optString("year"), vin = o.optString("vin"), mileage = o.optString("mileage"),
                engine = o.optString("engine"), fuel = o.optString("fuel"), hasLpg = o.optBoolean("hasLpg"),
                notes = o.optString("notes")
            ))
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
            list.add(MaintenanceWork(
                id = o.optLong("id"), carId = o.optLong("carId"), name = o.optString("name"),
                intervalKm = o.optInt("intervalKm"), intervalMonths = o.optInt("intervalMonths"),
                lastMileage = o.optInt("lastMileage"), lastDate = o.optLong("lastDate"),
                hasLastExecution = o.optBoolean("hasLastExecution"), skipped = o.optBoolean("skipped"),
                preventive = o.optBoolean("preventive")
            ))
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

// === ОПРЕДЕЛЕНИЕ СТАТУСА РАБОТЫ ===
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

// === ГЛАВНЫЙ ЭКРАН ===
@Composable
fun AppScreen() {
    val context = LocalContext.current
    var cars by remember { mutableStateOf(loadCars(context)) }
    var works by remember { mutableStateOf(loadWorks(context)) }
    var showAddCarDialog by remember { mutableStateOf(false) }
    var showAddWorkDialog by remember { mutableStateOf(false) }

    val selectedCar = cars.firstOrNull()
    val carWorks = works.filter { it.carId == selectedCar?.id }

    Column(
        modifier = Modifier.fillMaxSize().background(BgColor).systemBarsPadding()
    ) {
        // Верхняя панель
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
            Text("Главная", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Карточка машины
            if (selectedCar == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardColor)
                    ) {
                        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Гараж пуст", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Добавьте автомобиль", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                item { CarCard(selectedCar) }
            }

            // Статистика (считается реально)
            item {
                val total = carWorks.size
                val requires = carWorks.count { getWorkStatus(it, selectedCar!!) == "Требуется" }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Всего работ", total.toString(), ColorGreen, Modifier.weight(1f))
                    StatCard("Требуют", requires.toString(), ColorRed, Modifier.weight(1f))
                }
            }

            // Кнопки
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionBtn("Добавить авто", Icons.Default.Home, ColorGreen, Modifier.weight(1f)) { showAddCarDialog = true }
                    QuickActionBtn("Добавить работу", Icons.Default.Build, ColorPurple, Modifier.weight(1f)) {
                        if (selectedCar != null) showAddWorkDialog = true
                    }
                }
            }

            // Заголовок списка работ
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ближайшие работы", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Все работы >", color = ColorBlue, fontSize = 14.sp)
                }
            }

            // Список работ
            if (carWorks.isEmpty()) {
                item { Text("Работ пока нет", color = TextSecondary, modifier = Modifier.padding(16.dp)) }
            } else {
                items(carWorks.size) { index ->
                    val work = carWorks[index]
                    val status = getWorkStatus(work, selectedCar!!)
                    val statusColor = when(status) {
                        "Требуется" -> ColorRed
                        "Выполнено" -> ColorGreen
                        else -> ColorYellow
                    }
                    WorkItemCard(work.name, status, statusColor, work.intervalKm)
                }
            }
        }
    }

    // Диалог добавления авто
    if (showAddCarDialog) {
        AddCarDialog(
            onDismiss = { showAddCarDialog = false },
            onSave = { newCar ->
                val updated = cars + newCar
                cars = updated
                saveCars(context, updated)
                showAddCarDialog = false
            }
        )
    }

    // Диалог добавления работы
    if (showAddWorkDialog && selectedCar != null) {
        AddWorkDialog(
            onDismiss = { showAddWorkDialog = false },
            onSave = { name, km, lastMileage ->
                val newWork = MaintenanceWork(
                    id = System.currentTimeMillis(),
                    carId = selectedCar.id,
                    name = name,
                    intervalKm = km,
                    intervalMonths = 0,
                    lastMileage = lastMileage,
                    lastDate = System.currentTimeMillis(),
                    hasLastExecution = true
                )
                val updated = works + newWork
                works = updated
                saveWorks(context, updated)
                showAddWorkDialog = false
            }
        )
    }
}

@Composable
fun AddCarDialog(onDismiss: () -> Unit, onSave: (Car) -> Unit) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardColor,
        title = { Text("Новый автомобиль", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Марка") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Модель") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Год выпуска") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = mileage, onValueChange = { mileage = it }, label = { Text("Пробег (км)") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (brand.isNotBlank() && model.isNotBlank()) {
                    onSave(Car(System.currentTimeMillis(), brand, model, year, "", mileage, "", "Бензин", false, ""))
                }
            }) { Text("Сохранить", color = ColorGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) } }
    )
}

@Composable
fun AddWorkDialog(onDismiss: () -> Unit, onSave: (String, Int, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var intervalKm by remember { mutableStateOf("") }
    var lastMileage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardColor,
        title = { Text("Новая работа", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название работы") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = intervalKm, onValueChange = { intervalKm = it }, label = { Text("Интервал (км)") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = lastMileage, onValueChange = { lastMileage = it }, label = { Text("Пробег при выполнении") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(name, intervalKm.toIntOrNull() ?: 0, lastMileage.toIntOrNull() ?: 0)
                }
            }) { Text("Сохранить", color = ColorGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) } }
    )
}

// === КОМПОНЕНТЫ UI ===
@Composable
fun CarCard(car: Car) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Column {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E2A45)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Home, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                }
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

@Composable
fun SpecItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(if(title == "Всего работ") Icons.Default.Check else Icons.Default.Warning, null, tint = color)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = TextSecondary, fontSize = 12.sp)
                Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickActionBtn(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor), onClick = onClick) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, color = TextPrimary, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
fun WorkItemCard(name: String, status: String, statusColor: Color, intervalKm: Int) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(statusColor.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(if(status == "Выполнено") Icons.Default.Check else Icons.Default.Warning, null, tint = statusColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Интервал: $intervalKm км", color = TextSecondary, fontSize = 12.sp)
            }
            Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)) {
                Text(status, color = statusColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
