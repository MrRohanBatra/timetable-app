package com.rohan.jetpacklearn
import com.rohan.jetpacklearn.ui.TimetableList
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

// 🔹 Foundation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// 🔹 Material Icons (base)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu

import androidx.compose.material.icons.outlined.Settings

// 🔹 Material Icons (Extended – Google Fonts)
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.*

// 🔹 Material 3
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider

// 🔹 Runtime
import androidx.compose.runtime.*

// 🔹 UI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// 🔹 Theme
import com.rohan.jetpacklearn.ui.theme.JetPackLearnTheme
import com.rohan.jetpacklearn.utils.FileUtils
import com.rohan.jetpacklearn.utils.TimeUtils

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
    val context= LocalContext.current;
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val tt by produceState(initialValue = emptyMap<String, List<ClassEntry>>()) {
        value= FileUtils.loadTimetable(context);
    }
    val today:String= TimeUtils.getToday();
    var selectedScreen by remember { mutableStateOf(today) }
    val currentClasses=tt[selectedScreen]?:emptyList();
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
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {

                // 🔹 ANIMATION WRAPPER
                AnimatedContent(
                    targetState = selectedScreen, // The trigger: when this changes, animate!
                    transitionSpec = {
                        // A smooth Fade + Scale effect (looks very modern)
                        (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400)))
                            .togetherWith(fadeOut(animationSpec = tween(200)))
                    },
                    label = "DayTransition"
                ) { targetDay ->

                    // 🔹 Get data for the *target* day (the one fading in)
                    val classesForDay = tt[targetDay] ?: emptyList()

                    if (classesForDay.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No classes today! 🎉",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        TimetableList(
                            entries = classesForDay,
                            dayname = selectedScreen,

                        ) { /* Edit Click */ }
                    }
                }
            }
        }
    }
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
                    Icons.Filled.Today
                else
                    Icons.Outlined.CalendarMonth,
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
val sampleClasses = TimeUtils.sortClassesByTime(listOf(
    ClassEntry(
        time = "9:00 AM - 10:00 AM",
        subjectName = "Sensor Technology & Android Programming",
        classroom = "G2",
        classType = "Lecture",
    ),
    ClassEntry(
        time = "8:00 AM - 9:00 AM",
        subjectName = "Operations Research",
        classroom = "FF9",
        classType = "Lecture",
    ),

    ClassEntry(
        time = "11:00 AM - 12:00 PM",
        subjectName = "Cloud Based Enterprise Systems",
        classroom = "CS3",
        classType = "Lecture",
    ),
    ClassEntry(
        time = "2:00 PM - 4:00 PM",
        subjectName = "Cloud Based Enterprise Systems Lab",
        classroom = "CL22",
        classType = "Lab",
    )
)
)
