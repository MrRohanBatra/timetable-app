package com.rohan.jetpacklearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// 🔹 Foundation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// 🔹 Material Icons (base)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings

// 🔹 Material Icons (Extended – Google Fonts)
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*

// 🔹 Material 3
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider

// 🔹 Runtime
import androidx.compose.runtime.*

// 🔹 UI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// 🔹 Theme
import com.rohan.jetpacklearn.ui.theme.JetPackLearnTheme

// 🔹 Coroutines
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetPackLearnTheme {
                TimetableApp()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableApp() {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val today:String=getToday()
    var selectedScreen by remember { mutableStateOf(today) }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxHeight()
                ) {

                    // 🔹 MAIN DRAWER CONTENT
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Timetable Viewer",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Manage your daily schedule",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Days",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )

                        val days = listOf(
                            "Monday", "Tuesday", "Wednesday",
                            "Thursday", "Friday", "Saturday", "Sunday"
                        )

                        days.forEach { day ->
                            DrawerDayItem(
                                day = day,
                                isToday = day == today,
                                selected = selectedScreen == day
                            ) {
                                selectedScreen = day
                                scope.launch { drawerState.close() }
                            }
                        }
                    }

                    // 🔹 FOOTER (ONLY SETTINGS)
                    DrawerFooter {
                        selectedScreen = "Settings"
                        scope.launch { drawerState.close() }
                    }
                }
            }

        }
    ) {

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Timetable",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {

                TimetableList(entries = sampleClasses) { }
            }
        }
    }
}

fun getToday(): String{
    return "Monday"
}
@Composable
fun DrawerDayItem(
    day: String,
    isToday: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = if (isToday)
                    Icons.Default.LocationOn
                else
                    Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = if (isToday)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = {
            Text(
                text = day,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
@Composable
fun DrawerFooter(
    onSettingsClick: () -> Unit
) {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null
            )
        },
        label = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        selected = false,
        onClick = onSettingsClick,
        modifier = Modifier.padding(
            start = 12.dp,
            end = 12.dp,
            bottom = 12.dp,
            top = 6.dp
        )
    )
}
data class ClassEntry(
    val time: String,
    val subjectName: String,
    val classroom: String,
    val classType: String, // Lecture / Lab / Tutorial
    val isLive: Boolean
)
val sampleClasses = listOf(
    ClassEntry(
        time = "8:00 AM - 9:00 AM",
        subjectName = "Operations Research",
        classroom = "FF9",
        classType = "Lecture",
        isLive = false
    ),
    ClassEntry(
        time = "9:00 AM - 10:00 AM",
        subjectName = "Sensor Technology & Android Programming",
        classroom = "G2",
        classType = "Lecture",
        isLive = true
    ),
    ClassEntry(
        time = "11:00 AM - 12:00 PM",
        subjectName = "Cloud Based Enterprise Systems",
        classroom = "CS3",
        classType = "Lecture",
        isLive = false
    ),
    ClassEntry(
        time = "2:00 PM - 4:00 PM",
        subjectName = "Cloud Based Enterprise Systems Lab",
        classroom = "CL22",
        classType = "Lab",
        isLive = false
    )
)


@Composable
fun getClassIcon(type: String) = when (type) {
    "Lecture" -> Icons.Filled.MenuBook
    "Lab" -> Icons.Filled.Science
    "Tutorial" -> Icons.Filled.Groups
    else -> Icons.Filled.Event
}

// 🔹 Helper function to get color based on class type
@Composable
fun getClassColor(type: String): androidx.compose.ui.graphics.Color {
    return when (type) {
        "Lecture" -> MaterialTheme.colorScheme.primary
        "Lab" -> MaterialTheme.colorScheme.tertiary
        "Tutorial" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
}

@Composable
fun TimetableList(
    entries: List<ClassEntry>,
    onEdit: (ClassEntry) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Optional: Add a header
        item {
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }

        items(entries) { entry ->
            TimetableCard(
                entry = entry,
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
    entry: ClassEntry,
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

                    if (entry.isLive) {
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
                            text = entry.classType,
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