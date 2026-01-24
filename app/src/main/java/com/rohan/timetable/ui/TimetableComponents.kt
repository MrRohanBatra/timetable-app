package com.rohan.timetable.ui

import android.text.Layout
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.room.Delete
import com.rohan.timetable.ClassEntity
import com.rohan.timetable.TimetableViewModel
import com.rohan.timetable.getClassType
import com.rohan.timetable.utils.TimeUtils
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun getClassIcon(type: String) = when (type) {
    "L" -> Icons.Filled.MenuBook
    "P" -> Icons.Filled.Science
    "T" -> Icons.Filled.Groups
    else -> Icons.Filled.Event
}

// 🔹 Helper function to get color based on class type
@Composable
fun getClassColor(type: String): Color {
    return when (type) {
        "L" -> MaterialTheme.colorScheme.primary
        "P" -> MaterialTheme.colorScheme.tertiary
        "T" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableList(
    entries: List<ClassEntity>,
    dayname: String,
    viewModel: TimetableViewModel
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf<ClassEntity?>(null) }
    val sheetstate = rememberModalBottomSheetState(false);
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Optional: Add a header
        item {
            Text(
                text = "${dayname}'s Schedule",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 4.dp, start = 4.dp)
                    .wrapContentSize(Alignment.TopStart)
            )
        }

        items(entries) { entry ->
            TimetableCard(
                entry = entry,
                dayname=dayname,
                onClick = { },
                onEditClick = {
                    selectedClass=entry;
                    showEditDialog=true;
                }
            )
        }

        // Add some bottom padding for scroll comfort
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
    if (showEditDialog && selectedClass != null) {
        EditClassDialog(
            classEntity = selectedClass!!,
            onDismiss = {
                showEditDialog = false
                selectedClass = null
            },
            onDelete = {
                viewModel.deleteClass(it);
                showEditDialog=false;
                selectedClass=null;
            },
            onSave = {
                viewModel.updateClass(it);
                showEditDialog = false
                selectedClass = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableCard(
    entry: ClassEntity,
    dayname:String,
    onClick: () -> Unit,
    onEditClick:()-> Unit
) {
    val accentColor = getClassColor(entry.classType)

    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Ensures the color strip matches card height
        ) {
            // 🔹 1. Left Color Strip Accent
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(accentColor)
            )

            // 🔹 2. Main Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // --- Top Row: Time & Live Badge ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = accentColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = entry.time,
                            style = MaterialTheme.typography.labelLarge,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (TimeUtils.isLiveNow(entry.time,dayname)) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Middle: Subject Name ---
                Text(
                    text = entry.subjectName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Bottom Row: Location & Type & Edit ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Location Pill
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.classroom,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Class Type Text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getClassIcon(entry.classType),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getClassType(entry.classType),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Edit Icon (Subtle)
                    IconButton(onClick={onEditClick()}) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.outline,

                            )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClassDialog(
    classEntity: ClassEntity,
    onDismiss: () -> Unit,
    onSave: (ClassEntity) -> Unit,
    onDelete: (ClassEntity)->Unit
) {
    var subject by remember(classEntity.id) {
        mutableStateOf(classEntity.subjectName)
    }
    var classroom by remember(classEntity.id) {
        mutableStateOf(classEntity.classroom)
    }
    var classType by remember(classEntity.id) {
        mutableStateOf(classEntity.classType)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Class",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

                // 🔒 Time (read-only)
                OutlinedTextField(
                    value = classEntity.time,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Time") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = classroom,
                    onValueChange = { classroom = it },
                    label = { Text("Classroom") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("L", "T", "P").forEach { type ->
                        FilterChip(
                            selected = classType == type,
                            onClick = { classType = type },
                            label = { Text(type) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onDelete(classEntity) },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    )
                ) {
                    Text("Delete");
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(
                            classEntity.copy(
                                subjectName = subject,
                                classroom = classroom,
                                classType = classType
                            )
                        )
                    }
                ) {
                    Text("Save")
                }
            }
        },

    )
}
// === 1. INTERNAL ENUM FOR WIZARD STEPS ===
private enum class DialogStep {
    PickStartTime,
    PickEndTime,
    EnterDetails
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClass(
    day: String, // e.g. "Monday"
    onDismiss: () -> Unit,
    onConfirm: (ClassEntity) -> Unit
) {
    // --- STATE ---
    var currentStep by remember { mutableStateOf(DialogStep.PickStartTime) }

    // Time State
    val startTimeState = rememberTimePickerState(initialHour = LocalTime.now().hour, initialMinute = 0, is24Hour = false)
    val endTimeState = rememberTimePickerState(initialHour = LocalTime.now().plusHours(1).hour, initialMinute = 0, is24Hour = false)

    // Form State
    var className by remember { mutableStateOf("") }
    var classLocation by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("L") } // Default selection

    // Autocomplete State
    val classSuggestions = listOf("Mathematics", "Physics", "Chemistry", "English", "History", "Computer Science")
    var isExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .heightIn(max = 650.dp) // Limits height to avoid full screen on tablets
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER ---
                Text(
                    text = when (currentStep) {
                        DialogStep.PickStartTime -> "Start Time"
                        DialogStep.PickEndTime -> "End Time"
                        DialogStep.EnterDetails -> "Class Details"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // Show condensed time range only on the final step
                if (currentStep == DialogStep.EnterDetails) {
                    // Quick preview formatter
                    val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
                    val s = LocalTime.of(startTimeState.hour, startTimeState.minute).format(fmt)
                    val e = LocalTime.of(endTimeState.hour, endTimeState.minute).format(fmt)
                    Text(
                        text = "$s - $e ($day)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- CONTENT SWITCHER ---
                when (currentStep) {
                    DialogStep.PickStartTime -> {
                        TimePicker(state = startTimeState)
                    }
                    DialogStep.PickEndTime -> {
                        TimePicker(state = endTimeState)
                    }
                    DialogStep.EnterDetails -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                            // 1. AUTOCOMPLETE: Class Name
                            ExposedDropdownMenuBox(
                                expanded = isExpanded,
                                onExpandedChange = { isExpanded = !isExpanded }
                            ) {
                                OutlinedTextField(
                                    value = className,
                                    onValueChange = {
                                        className = it
                                        isExpanded = true
                                    },
                                    label = { Text("Subject Name") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    singleLine = true
                                )

                                val filteredOptions = classSuggestions.filter {
                                    it.contains(className, ignoreCase = true)
                                }.take(10)

                                if (filteredOptions.isNotEmpty()) {
                                    ExposedDropdownMenu(
                                        expanded = isExpanded,
                                        onDismissRequest = { isExpanded = false }
                                    ) {
                                        filteredOptions.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                text = { Text(selectionOption) },
                                                onClick = {
                                                    className = selectionOption
                                                    isExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. CHIPS: Class Type
                            Column {
                                Text("Type", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("L", "P", "T").forEach { type ->
                                        FilterChip(
                                            selected = (selectedType == type),
                                            onClick = { selectedType = type },
                                            label = { Text(type) },
                                            leadingIcon = if (selectedType == type) {
                                                { Icon(Icons.Filled.Check, null) }
                                            } else null
                                        )
                                    }
                                }
                            }

                            // 3. TEXT: Room
                            OutlinedTextField(
                                value = classLocation,
                                onValueChange = { classLocation = it },
                                label = { Text("Room / Location") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- NAVIGATION BUTTONS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Back Button
                    if (currentStep != DialogStep.PickStartTime) {
                        TextButton(onClick = {
                            currentStep = when (currentStep) {
                                DialogStep.EnterDetails -> DialogStep.PickEndTime
                                DialogStep.PickEndTime -> DialogStep.PickStartTime
                                else -> DialogStep.PickStartTime
                            }
                        }) {
                            Text("Back")
                        }
                    } else {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Next / Save Button
                    Button(
                        onClick = {
                            when (currentStep) {
                                DialogStep.PickStartTime -> {
                                    currentStep = DialogStep.PickEndTime
                                }
                                DialogStep.PickEndTime -> {
                                    currentStep = DialogStep.EnterDetails
                                }
                                DialogStep.EnterDetails -> {
                                    // --- FINAL PROCESSING ---
                                    val start = LocalTime.of(startTimeState.hour, startTimeState.minute)
                                    val end = LocalTime.of(endTimeState.hour, endTimeState.minute)

                                    // Strict English Formatting (AM/PM uppercase)
                                    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
                                    val timeString = "${start.format(formatter).uppercase()} - ${end.format(formatter).uppercase()}"

                                    // Construct the Entity
                                    val resultEntity = ClassEntity(
                                        day = day,
                                        time = timeString,
                                        subjectName = className,
                                        classroom = classLocation,
                                        classType = selectedType
                                    )

                                    Log.e("AddClass", "About to call onConfirm")
                                    // Return the Entity
                                    onConfirm(resultEntity)
                                }
                            }
                        }
                    ) {
                        Text(if (currentStep == DialogStep.EnterDetails) "Save" else "Next")
                    }
                }
            }
        }
    }
}