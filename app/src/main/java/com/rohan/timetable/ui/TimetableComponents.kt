package com.rohan.timetable.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohan.timetable.ClassEntity
import com.rohan.timetable.getClassType
import com.rohan.timetable.utils.TimeUtils


@Composable
fun getClassIcon(type: String) = when (type) {
    "L" -> Icons.Filled.MenuBook
    "P" -> Icons.Filled.Science
    "T" -> Icons.Filled.Groups
    else -> Icons.Filled.Event
}

// 🔹 Helper function to get color based on class type
@Composable
fun getClassColor(type: String): androidx.compose.ui.graphics.Color {
    return when (type) {
        "L" -> MaterialTheme.colorScheme.primary
        "P" -> MaterialTheme.colorScheme.tertiary
        "T" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
}

@Composable
fun TimetableList(
    entries: List<ClassEntity>,
    dayname: String,
    onEdit: (ClassEntity) -> Unit
) {
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
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp).wrapContentSize(Alignment.TopStart)
            )
        }

        items(entries) { entry ->
            TimetableCard(
                entry = entry,
                dayname=dayname,
                onClick = { onEdit(entry) }
            )
        }

        // Add some bottom padding for scroll comfort
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableCard(
    entry: ClassEntity,
    dayname:String,
    onClick: () -> Unit
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
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
