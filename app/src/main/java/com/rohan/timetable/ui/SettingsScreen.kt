package com.rohan.timetable.ui

import com.rohan.timetable.R
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohan.timetable.TimetableRepository
import com.rohan.timetable.TimetableViewModel
import com.rohan.timetable.utils.ShareUtils

fun launchMigration(context: Context) {
    try {
        val intent = Intent().apply {
            component = ComponentName(
                "com.example.timetable_app",                 // OLD app package
                "com.example.timetable_app.MainActivity" // Activity class
            )
            action = "com.example.timetable_app.ACTION_MIGRATE"
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "Old app not installed",
            Toast.LENGTH_LONG
        ).show()
    }
}
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    val timetableViewModel: TimetableViewModel=androidx.lifecycle.viewmodel.compose.viewModel()
    var sharepath by remember { mutableStateOf("") }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsHeader()
        }

        item {
            SettingsGroup(title = "General") {
                SettingsItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Adjust app appearance",
                    iconTint = MaterialTheme.colorScheme.primary,
                    trailingContent = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it }
                        )
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                SettingsItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Notifications",
                    subtitle = "Class reminders & updates",
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    trailingContent = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                )
            }
        }

        // 🔹 3. Data & Sync Group
        item {
            SettingsGroup(title = "Data & Sync") {
                SettingsItem(
                    icon = Icons.Rounded.Share,
                    title = "Share Timetable",
                    subtitle = "Share with others",
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        timetableViewModel.exportTimetable{grouped->
                            val path=ShareUtils.saveToFile(context,grouped)
                            sharepath=path;
                            ShareUtils.ShareFile(context,path);
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                SettingsItem(
                    icon = Icons.Rounded.EditCalendar, // Material Icons Extended
                    title = "Edit Schedule",
                    subtitle = "Modify subjects and times",
                    iconTint = MaterialTheme.colorScheme.error, // Or custom color
                    onClick = { /* Handle Edit */ }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
//                SettingsItem(
//                    icon = Icons.Rounded.SystemUpdateAlt,
//                    title = "Import from old app",
//                    subtitle = "Migrate timetable from previous version",
//                    iconTint = MaterialTheme.colorScheme.primary,
//                    onClick = {
//                        launchMigration(context)
//                    }
//                )
                var showMigrationDialog by remember { mutableStateOf(false) }

                SettingsItem(
                    icon = Icons.Rounded.SystemUpdateAlt,
                    title = "Import from old app",
                    subtitle = "Migrate timetable from previous version",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        showMigrationDialog = true
                    }
                )

                if (showMigrationDialog) {
                    AlertDialog(
                        onDismissRequest = { showMigrationDialog = false },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null
                            )
                        },
                        title = {
                            Text("Replace existing classes?")
                        },
                        text = {
                            Text(
                                "Importing data from the old app will erase all existing classes in this app. " +
                                        "This action cannot be undone."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showMigrationDialog = false
                                    launchMigration(context) // ❗ unchanged
                                }
                            ) {
                                Text("Import")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showMigrationDialog = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }

        // 🔹 4. Support / About Group
        item {
            SettingsGroup(title = "About") {
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    title = "Version",
                    subtitle = "v${stringResource(R.string.version)} (${stringResource(R.string.build_type)})",
                    iconTint = MaterialTheme.colorScheme.outline,
                    showChevron = false
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                SettingsItem(
                    icon = Icons.Rounded.BugReport,
                    title = "Report a Bug",
                    subtitle = "Help us improve",
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = { /* Open Github/Form */ }
                )
            }
        }
        if(context.getString(R.string.build_type)!="Release"){
//            item { Spacer(modifier = Modifier.height(30.dp)) }
            item{
                SettingsGroup(title = "Debug") {
                    SettingsItem(
                        icon =Icons.Rounded.Info,
                        title = "FilePath",
                        subtitle = sharepath,
                        iconTint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// ---------------------------------------------------------
// 🔹 COMPONENT: Settings Header
// ---------------------------------------------------------
@Composable
fun SettingsHeader() {
    Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Customize your experience",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------
// 🔹 COMPONENT: Settings Group Container
//    (Matches your TimetableCard style)
// ---------------------------------------------------------
@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

// ---------------------------------------------------------
// 🔹 COMPONENT: Individual Settings Row
// ---------------------------------------------------------
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color,
    showChevron: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Icon in a subtle circle background
        Surface(
            shape = CircleShape,
            color = iconTint.copy(alpha = 0.1f), // Subtle background matching tint
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Text Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3. Trailing (Switch, Chevron, or nothing)
        if (trailingContent != null) {
            trailingContent()
        } else if (showChevron && onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}