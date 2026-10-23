package com.example.carmaintenance

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

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
fun CarCard(car: Car, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
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
                            Surface(color = ColorBlue.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) { Text("ГБО", color = ColorBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
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

@Composable fun WorkItemCard(name: String, status: String, statusColor: Color, intervalKm: Int, intervalMonths: Int, onClick: () -> Unit) {
    // Формируем красивую строку с периодичностью
    val periodText = buildString {
        if (intervalKm > 0) append("$intervalKm км")
        if (intervalKm > 0 && intervalMonths > 0) append(" / ")
        if (intervalMonths > 0) append("$intervalMonths мес.")
        if (intervalKm == 0 && intervalMonths == 0) append("По износу")
    }

    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(statusColor.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { 
                Icon(if(status == "Выполнено") Icons.Default.Check else Icons.Default.Warning, null, tint = statusColor) 
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Периодичность: $periodText", color = TextSecondary, fontSize = 12.sp)
            }
            Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)) { 
                Text(status, color = statusColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Medium) 
            }
        }
    }
}
