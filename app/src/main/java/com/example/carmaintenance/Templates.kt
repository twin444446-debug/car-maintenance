package com.example.carmaintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TemplateEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var templateWorks by remember { mutableStateOf(loadDefaultTemplate(context)) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Шаблон регламента", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад", tint = TextPrimary) }
        }
        
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(templateWorks.size) { index ->
                val work = templateWorks[index]
                Card(colors = CardDefaults.cardColors(containerColor = CardColor), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(work.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Интервал: ${work.intervalKm} км / ${work.intervalMonths} мес", color = TextSecondary, fontSize = 12.sp)
                        }
                        IconButton(onClick = { 
                            templateWorks = templateWorks.filter { it.id != work.id }
                            saveDefaultTemplate(context, templateWorks)
                        }) { Icon(Icons.Default.Delete, null, tint = ColorRed) }
                    }
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showAddDialog = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ColorGreen)) {
                Text("Добавить", color = TextPrimary)
            }
        }
    }

    if (showAddDialog) {
        AddTemplateWorkDialog(
            existingWork = null,
            onDismiss = { showAddDialog = false },
            onSave = { newWork ->
                templateWorks = templateWorks + newWork
                saveDefaultTemplate(context, templateWorks)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddTemplateWorkDialog(existingWork: MaintenanceWork?, onDismiss: () -> Unit, onSave: (MaintenanceWork) -> Unit) {
    var name by remember { mutableStateOf(existingWork?.name ?: "") }
    var intervalKm by remember { mutableStateOf(existingWork?.intervalKm?.toString() ?: "") }
    var intervalMonths by remember { mutableStateOf(existingWork?.intervalMonths?.toString() ?: "") }
    var isLpg by remember { mutableStateOf(existingWork?.name?.startsWith("ГБО:") ?: false) }
    
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = CardColor, title = { Text("Новая работа в шаблон", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLpg, onCheckedChange = { isLpg = it }, colors = CheckboxDefaults.colors(checkedColor = ColorBlue))
                    Text("Это работа по ГБО", color = TextSecondary)
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = intervalKm, onValueChange = { intervalKm = it }, label = { Text("Км") }, modifier = Modifier.weight(1f), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                    OutlinedTextField(value = intervalMonths, onValueChange = { intervalMonths = it }, label = { Text("Мес") }, modifier = Modifier.weight(1f), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                }
            }
        },
        confirmButton = { 
            TextButton(onClick = { 
                if (name.isNotBlank()) {
                    val finalName = if (isLpg && !name.startsWith("ГБО:")) "ГБО: $name" else name
                    onSave(MaintenanceWork(carId = "", name = finalName, intervalKm = intervalKm.toIntOrNull() ?: 0, intervalMonths = intervalMonths.toIntOrNull() ?: 0))
                }
            }) { Text("Сохранить", color = ColorGreen) } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) } }
    )
}
