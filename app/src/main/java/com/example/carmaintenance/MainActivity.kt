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

// Модель данных
data class Car(
    val id: Long, var brand: String, var model: String, var year: String,
    var vin: String, var mileage: String, var engine: String, var fuel: String,
    var hasLpg: Boolean, var notes: String
)

// Цветовая палитра
val BgColor = Color(0xFF0A1128)
val CardColor = Color(0xFF111A32)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8E9CB2)
val ColorGreen = Color(0xFF00C853)
val ColorRed = Color(0xFFE74C3C)
val ColorYellow = Color(0xFFF1C40F)
val ColorBlue = Color(0xFF3498DB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HomeScreen()
            }
        }
    }
}

// Функции для сохранения/загрузки машин
fun loadCars(context: Context): List<Car> {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val jsonStr = prefs.getString("cars", "[]") ?: "[]"
    val list = mutableListOf<Car>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            list.add(
                Car(
                    id = o.optLong("id"),
                    brand = o.optString("brand"),
                    model = o.optString("model"),
                    year = o.optString("year"),
                    vin = o.optString("vin"),
                    mileage = o.optString("mileage"),
                    engine = o.optString("engine"),
                    fuel = o.optString("fuel"),
                    hasLpg = o.optBoolean("hasLpg"),
                    notes = o.optString("notes")
                )
            )
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveCars(context: Context, cars: List<Car>) {
    val prefs = context.getSharedPreferences("car_maintenance", Context.MODE_PRIVATE)
    val array = JSONArray()
    cars.forEach { car ->
        val o = JSONObject()
        o.put("id", car.id)
        o.put("brand", car.brand)
        o.put("model", car.model)
        o.put("year", car.year)
        o.put("vin", car.vin)
        o.put("mileage", car.mileage)
        o.put("engine", car.engine)
        o.put("fuel", car.fuel)
        o.put("hasLpg", car.hasLpg)
        o.put("notes", car.notes)
        array.put(o)
    }
    prefs.edit().putString("cars", array.toString()).apply()
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    // Загружаем машины при старте
    var cars by remember { mutableStateOf(loadCars(context)) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .systemBarsPadding() // Отступы от системных панелей
    ) {
        // Верхняя панель
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
            // Блок с машиной (или заглушка, если пусто)
            if (cars.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Info, null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Гараж пуст", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Добавьте автомобиль, чтобы начать", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                // Показываем первый автомобиль из списка (позже сделаем карусель)
                item { CarCard(cars.first()) }
            }

            // Статистика
            item { StatsRow() }

            // Кнопки действий
            item {
                QuickActionsRow(
                    onAddClick = { showAddDialog = true }
                )
            }

            // Заголовок списка
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ближайшие работы", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Все работы >", color = ColorBlue, fontSize = 14.sp)
                }
            }
            
            // Тестовый список работ (пока оставим для визуала)
            items(3) { index ->
                val status = when(index) { 0 -> "Требуется"; 1 -> "Скоро"; else -> "Выполнено" }
                val statusColor = when(index) { 0 -> ColorRed; 1 -> ColorYellow; else -> ColorGreen }
                val workName = when(index) { 
                    0 -> "Замена масла и масляного фильтра"
                    1 -> "Замена тормозных колодок"
                    else -> "Замена воздушного фильтра"
                }
                MaintenanceItemCard(workName, status, statusColor)
            }
        }
    }

    // Диалоговое окно добавления машины
    if (showAddDialog) {
        AddCarDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newCar ->
                val updatedList = cars + newCar
                cars = updatedList
                saveCars(context, updatedList)
                showAddDialog = false
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
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("Новый автомобиль") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = brand, onValueChange = { brand = it },
                    label = { Text("Марка (например, Hyundai)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ColorBlue, unfocusedBorderColor = TextSecondary
                    )
                )
                OutlinedTextField(
                    value = model, onValueChange = { model = it },
                    label = { Text("Модель (например, Verna)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ColorBlue, unfocusedBorderColor = TextSecondary
                    )
                )
                OutlinedTextField(
                    value = year, onValueChange = { year = it },
                    label = { Text("Год выпуска") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ColorBlue, unfocusedBorderColor = TextSecondary
                    )
                )
                OutlinedTextField(
                    value = mileage, onValueChange = { mileage = it },
                    label = { Text("Пробег (км)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ColorBlue, unfocusedBorderColor = TextSecondary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (brand.isNotBlank() && model.isNotBlank()) {
                    onSave(
                        Car(
                            id = System.currentTimeMillis(),
                            brand = brand, model = model, year = year,
                            vin = "", mileage = mileage, engine = "",
                            fuel = "Бензин", hasLpg = false, notes = ""
                        )
                    )
                }
            }) {
                Text("Сохранить", color = ColorGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}

@Composable
fun CarCard(car: Car) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF1E2A45)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                }
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(colors = listOf(Color.Transparent, CardColor.copy(alpha = 0.9f)), startY = 300f)
                    )
                )
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text("${car.brand} ${car.model}", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${car.year} г.", color = TextSecondary, fontSize = 14.sp)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
fun StatsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("Всего работ", "12", ColorGreen, Modifier.weight(1f))
        StatCard("Требуют", "4", ColorRed, Modifier.weight(1f))
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
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
fun QuickActionsRow(onAddClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickActionBtn("Выбрать", Icons.Default.Home, ColorGreen, Modifier.weight(1f)) { }
        QuickActionBtn("Изменить", Icons.Default.Edit, ColorBlue, Modifier.weight(1f)) { }
        QuickActionBtn("Добавить", Icons.Default.Add, Color(0xFF9C27B0), Modifier.weight(1f), onAddClick)
    }
}

@Composable
fun QuickActionBtn(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, color = TextPrimary, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
fun MaintenanceItemCard(workName: String, status: String, statusColor: Color) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(statusColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(if(status == "Выполнено") Icons.Default.Check else Icons.Default.Warning, null, tint = statusColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(workName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Пробег: 120000 км", color = TextSecondary, fontSize = 12.sp)
            }
            Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)) {
                Text(status, color = statusColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
