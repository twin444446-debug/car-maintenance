package com.example.carmaintenance

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                val statusColor = when(status) { "Требуется", "Необходимо выполнить" -> ColorRed; "Пропущена" -> ColorPurple; else -> ColorGreen }
                WorkItemCard(work.name, status, statusColor, work.intervalKm) { onWorkClick(work) }
            }
        }
    }
}

// === ЭКРАНЫ ВКЛАДОК ===
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
fun CarInfoDialog(car: Car, onDismiss: () -> Unit) {
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
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = ColorGreen) }
        }
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
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E2A45)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Home, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)) }
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
