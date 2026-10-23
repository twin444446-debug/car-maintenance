package com.example.carmaintenance

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddCarDialog(onDismiss: () -> Unit, onSave: (Car) -> Unit) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf("") }
    var hasLpg by remember { mutableStateOf(false) }

    var attemptedSave by remember { mutableStateOf(false) }
    val isBrandError = attemptedSave && brand.isBlank()
    val isModelError = attemptedSave && model.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = CardColor, 
        title = { Text("Новый автомобиль", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = brand, onValueChange = { brand = it; if(attemptedSave) attemptedSave = brand.isBlank() || model.isBlank() }, 
                    label = { Text("Марка *") }, singleLine = true, isError = isBrandError,
                    supportingText = { if (isBrandError) Text("Обязательное поле", color = ColorRed) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, errorTextColor = ColorRed, errorBorderColor = ColorRed, errorLabelColor = ColorRed)
                )
                OutlinedTextField(
                    value = model, onValueChange = { model = it; if(attemptedSave) attemptedSave = brand.isBlank() || model.isBlank() }, 
                    label = { Text("Модель *") }, singleLine = true, isError = isModelError,
                    supportingText = { if (isModelError) Text("Обязательное поле", color = ColorRed) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, errorTextColor = ColorRed, errorBorderColor = ColorRed, errorLabelColor = ColorRed)
                )
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Год выпуска") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = mileage, onValueChange = { mileage = it }, label = { Text("Пробег (км)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = vin, onValueChange = { vin = it }, label = { Text("VIN-код") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = engine, onValueChange = { engine = it }, label = { Text("Двигатель (например, 1.4)") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasLpg, onCheckedChange = { hasLpg = it }, colors = CheckboxDefaults.colors(checkedColor = ColorGreen))
                    Text("Установлено ГБО (Газ)", color = TextSecondary, fontSize = 14.sp)
                }
            }
        },
        confirmButton = { 
            TextButton(onClick = { 
                if (brand.isNotBlank() && model.isNotBlank()) {
                    val fuelType = if (hasLpg) "Бензин/Газ" else "Бензин"
                    onSave(Car(System.currentTimeMillis(), brand, model, year, vin, mileage, engine, fuelType, hasLpg, ""))
                } else {
                    attemptedSave = true
                }
            }) { Text("Сохранить", color = ColorGreen) } 
        },
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
    var lastDate by remember { mutableStateOf(if (existingWork?.lastDate ?: 0L > 0L) existingWork!!.lastDate else System.currentTimeMillis()) }
    
    var attemptedSave by remember { mutableStateOf(false) }
    val isNameError = attemptedSave && name.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = CardColor, title = { Text(if (existingWork == null) "Новая работа" else "Изменить работу", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; if(attemptedSave) attemptedSave = name.isBlank() }, 
                    label = { Text("Название *") }, singleLine = true, isError = isNameError,
                    supportingText = { if (isNameError) Text("Обязательное поле", color = ColorRed) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, errorTextColor = ColorRed, errorBorderColor = ColorRed, errorLabelColor = ColorRed)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = intervalKm, onValueChange = { intervalKm = it }, label = { Text("Интервал, км") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                    OutlinedTextField(value = intervalMonths, onValueChange = { intervalMonths = it }, label = { Text("Интервал, мес") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notDone, onCheckedChange = { notDone = it }, colors = CheckboxDefaults.colors(checkedColor = ColorGreen))
                    Text("Не выполнялась", color = TextSecondary, fontSize = 14.sp)
                }
                
                val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(lastDate))
                Button(
                    onClick = {
                        val c = Calendar.getInstance(); c.timeInMillis = lastDate
                        DatePickerDialog(context, { _, y, m, d -> lastDate = Calendar.getInstance().apply { set(y, m, d, 12, 0, 0) }.timeInMillis }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2A45))
                ) { Text("Дата выполнения: $dateStr", color = TextPrimary) }

                if (!notDone) {
                    OutlinedTextField(value = lastMileage, onValueChange = { lastMileage = it }, label = { Text("Пробег при выполнении") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(MaintenanceWork(existingWork?.id ?: System.currentTimeMillis(), existingWork?.carId ?: 0L, name, intervalKm.toIntOrNull() ?: 0, intervalMonths.toIntOrNull() ?: 0, lastMileage.toIntOrNull() ?: 0, lastDate, !notDone, existingWork?.skipped ?: false))
                } else {
                    attemptedSave = true
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
        onDismissRequest = onDismiss, 
        containerColor = CardColor, 
        title = { Text(work.name, color = TextPrimary) },
        text = {
            if (showCompleteInput) {
                Column { 
                    Text("Введите текущий пробег:", color = TextSecondary); 
                    OutlinedTextField(value = mileageInput, onValueChange = { mileageInput = it }, label = { Text("Пробег (км)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)) 
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionRowButton("Выполнить работу", ColorGreen) { showCompleteInput = true }
                    ActionRowButton("Пропустить работу", ColorYellow) { onSkip() }
                    ActionRowButton("Редактировать данные", ColorBlue) { onEdit() }
                    ActionRowButton("Удалить запись", ColorRed) { onDelete() }
                }
            }
        },
        confirmButton = { 
            if (showCompleteInput) TextButton(onClick = { onComplete(mileageInput.toIntOrNull() ?: 0) }) { Text("Сохранить", color = ColorGreen) } 
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) } 
        }
    )
}

@Composable
fun ActionRowButton(text: String, dotColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = TextPrimary, fontSize = 16.sp)
        Box(modifier = Modifier.size(10.dp).background(dotColor, CircleShape))
    }
}
