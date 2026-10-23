package com.example.carmaintenance

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MoreScreen(context: android.content.Context, onEditTemplateClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Дополнительно", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        Button(onClick = onEditTemplateClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ColorBlue)) {
            Text("Редактировать шаблон", color = TextPrimary)
        }
        
        Text("Здесь можно настроить, какие работы будут автоматически подтягиваться при заполнении регламента для новых автомобилей.", color = TextSecondary, fontSize = 14.sp)
    }
}
