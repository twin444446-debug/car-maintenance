package com.example.carmaintenance

import androidx.compose.ui.text.font.FontWeight
import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

// === ДИАЛОГ АВТО (С ФОТО И ЗАМЕТКАМИ) ===
@Composable
fun AddCarDialog(existingCar: Car? = null, onDismiss: () -> Unit, onSave: (Car) -> Unit) {
    var brand by remember { mutableStateOf(existingCar?.brand ?: "") }
    var model by remember { mutableStateOf(existingCar?.model ?: "") }
    var year by remember { mutableStateOf(existingCar?.year ?: "") }
    var mileage by remember { mutableStateOf(existingCar?.mileage ?: "") }
    var vin by remember { mutableStateOf(existingCar?.vin ?: "") }
    var engine by remember { mutableStateOf(existingCar?.engine ?: "") }
    var hasLpg by remember { mutableStateOf(existingCar?.hasLpg ?: false) }
    var imageUri by remember { mutableStateOf(existingCar?.imageUri ?: "") }
    var notes by remember { mutableStateOf(existingCar?.notes ?: "") }

    var attemptedSave by remember { mutableStateOf(false) }
    val isBrandError = attemptedSave && brand.isBlank()
    val isModelError = attemptedSave && model.isBlank()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) imageUri = uri.toString() }

    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = CardColor, 
        title = { Text(if (existingCar == null) "Новый автомобиль" else "Редактировать авто", color = TextPrimary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2A45))
                ) {
                    Text(if (imageUri.isEmpty()) "Выбрать фото" else "Фото выбрано ✓", color = TextPrimary)
                }
                OutlinedTextField(value = brand, onValueChange = { brand = it; if(attemptedSave) attemptedSave = brand.isBlank() || model.isBlank() }, label = { Text("Марка *") }, singleLine = true, isError = isBrandError, supportingText = { if (isBrandError) Text("Обязательное поле", color = ColorRed) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, errorTextColor = ColorRed, errorBorderColor = ColorRed, errorLabelColor = ColorRed))
                OutlinedTextField(value = model, onValueChange = { model = it; if(attemptedSave) attemptedSave = brand.isBlank() || model.isBlank() }, label = { Text("Модель *") }, singleLine = true, isError = isModelError, supportingText = { if (isModelError) Text("Обязательное поле", color = ColorRed) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, errorTextColor = ColorRed, errorBorderColor = ColorRed, errorLabelColor = ColorRed))
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Год выпуска") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = mileage, onValueChange = { mileage = it }, label = { Text("Пробег (км)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = vin, onValueChange = { vin = it }, label = { Text("VIN-код") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = engine, onValueChange = { engine = it }, label = { Text("Двигатель") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Заметки") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
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
                    onSave(Car(existingCar?.id ?: System.currentTimeMillis(), brand, model, year, vin, mileage, engine, fuelType, hasLpg, notes, imageUri))
                } else attemptedSave = true
            }) { Text("Сохранить", color = ColorGreen) } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) } }
    )
}

// === ДИАЛОГ РАБОТЫ ===
@Composable
fun AddEditWorkDialog(existingWork: MaintenanceWork?, onDismiss: () -> Unit, onSave: (MaintenanceWork) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingWork?.name ?: "") }
    var intervalKm by remember { mutableStateOf(if ((existingWork?.intervalKm ?: 0) > 0) existingWork!!.intervalKm.toString() else "") }
    var intervalMonths by remember { mutableStateOf(if ((existingWork?.intervalMonths ?: 0) > 0) existingWork!!.intervalMonths.toString() else "") }
    var notDone by remember { mutableStateOf(existingWork?.hasLastExecution != true) }
    var preventive by remember { mutableStateOf(existingWork?.preventive ?: false) }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = preventive, onCheckedChange = { preventive = it }, colors = CheckboxDefaults.colors(checkedColor = ColorYellow))
                    Text("Предупредительная (Скоро)", color = TextSecondary, fontSize = 14.sp)
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
                    onSave(MaintenanceWork(existingWork?.id ?: System.currentTimeMillis(), existingWork?.carId ?: 0L, name, intervalKm.toIntOrNull() ?: 0, intervalMonths.toIntOrNull() ?: 0, lastMileage.toIntOrNull() ?: 0, lastDate, !notDone, existingWork?.skipped ?: false, preventive))
                } else attemptedSave = true
            }) { Text("Сохранить", color = ColorGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) } }
    )
}

// === ДИАЛОГ ДЕЙСТВИЙ ===
@Composable
fun WorkActionsDialog(work: MaintenanceWork, onDismiss: () -> Unit, onComplete: (Int) -> Unit, onSkip: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, onNotDone: () -> Unit) {
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
                    ActionRowButton("Требует внимания", ColorRed) { onNotDone() }
                    ActionRowButton("Пропустить работу", ColorPurple) { onSkip() }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onEdit) { Text("Изменить", color = TextSecondary, fontSize = 13.sp) }
                        TextButton(onClick = onDelete) { Text("Удалить", color = TextSecondary, fontSize = 13.sp) }
                        TextButton(onClick = onDismiss) { Text("Отмена", color = ColorRed, fontSize = 13.sp) }
                    }
                }
            }
        },
        confirmButton = { 
            if (showCompleteInput) TextButton(onClick = { onComplete(mileageInput.toIntOrNull() ?: 0) }) { Text("Сохранить", color = ColorGreen) } 
        },
        dismissButton = { 
            if (showCompleteInput) TextButton(onClick = { showCompleteInput = false }) { Text("Назад", color = TextSecondary) }
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

// === ДИАЛОГ ИНФОРМАЦИИ О МАШИНЕ (С КНОПКОЙ ЗАПОЛНЕНИЯ) ===
@Composable
fun CarInfoDialog(car: Car, onDismiss: () -> Unit, onEdit: () -> Unit, onFillDefaults: () -> Unit) {
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
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = { onFillDefaults(); onDismiss() }) { 
                    Text("Заполнить регламент", color = ColorGreen) 
                }
                TextButton(onClick = onEdit) { Text("Редактировать авто", color = ColorBlue) } 
            }
        },
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
