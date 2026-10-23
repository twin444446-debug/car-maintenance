package com.example.carmaintenance

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Твои модели данных (оставил как есть)
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

// Цветовая палитра
val BgColor = Color(0xFF0A1128)
val CardColor = Color(0xFF111A32)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8E9CB2)
val ColorGreen = Color(0xFF00C853)
val ColorRed = Color(0xFFE74C3C)
val ColorYellow = Color(0xFFF1C40F)

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

@Composable
fun HomeScreen() {
    // Тестовые данные для визуала
    val testCar = Car(1, "Hyundai", "Verna", "2008", "KMH...", "125000", "1.4", "Бензин", false, "")
    val testWorks = listOf(
        MaintenanceWork(1, 1, "Замена масла и масляного фильтра", 10000, 12, 120000, 0, true),
        MaintenanceWork(2, 1, "Замена тормозных колодок", 15000, 12, 140000, 0, true),
        MaintenanceWork(3, 1, "Замена воздушного фильтра", 10000, 12, 100000, 0, true)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
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
            item { CarCard(testCar) }
            item { StatsRow() }
            item { QuickActionsRow() }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ближайшие работы", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Все работы >", color = Color(0xFF3498DB), fontSize = 14.sp)
                }
            }
            
            items(testWorks.size) { index ->
                val work = testWorks[index]
                val status = when(index) {
                    0 -> "Требуется"
                    1 -> "Скоро"
                    else -> "Выполнено"
                }
                val statusColor = when(index) {
                    0 -> ColorRed
                    1 -> ColorYellow
                    else -> ColorGreen
                }
                MaintenanceItemCard(work, status, statusColor)
            }
        }
    }
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
                // Локальная заглушка вместо картинки из интернета
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E2A45)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                }

                // Градиент снизу для читаемости текста
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, CardColor.copy(alpha = 0.9f)),
                                startY = 300f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text("${car.brand} ${car.model}", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${car.year} г.", color = TextSecondary, fontSize = 14.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecItem(Icons.Default.Info, "Пробег", "${car.mileage} км")
                SpecItem(Icons.Default.Info, "VIN", car.vin)
                SpecItem(Icons.Default.Build, "Двигатель", "${car.engine} л")
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("Всего работ", "12", ColorGreen, Modifier.weight(1f))
        StatCard("Требуют", "4", ColorRed, Modifier.weight(1f))
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
fun QuickActionsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionBtn("Выбрать", Icons.Default.Home, Color(0xFF4CAF50), Modifier.weight(1f))
        QuickActionBtn("Изменить", Icons.Default.Edit, Color(0xFF3498DB), Modifier.weight(1f))
        QuickActionBtn("Добавить", Icons.Default.Add, Color(0xFF9C27B0), Modifier.weight(1f))
    }
}

@Composable
fun QuickActionBtn(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, color = TextPrimary, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
fun MaintenanceItemCard(work: MaintenanceWork, status: String, statusColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(statusColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(if(status == "Выполнено") Icons.Default.Check else Icons.Default.Warning, null, tint = statusColor)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(work.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Пробег: ${work.lastMileage} км", color = TextSecondary, fontSize = 12.sp)
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = status,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
