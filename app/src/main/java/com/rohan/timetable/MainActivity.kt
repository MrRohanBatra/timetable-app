package com.rohan.timetable
import android.content.Intent
import android.net.Uri
import com.rohan.timetable.ui.TimetableList
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import com.rohan.timetable.ui.AddClass

import com.rohan.timetable.ui.SettingsScreen

// 🔹 Theme
import com.rohan.timetable.ui.theme.timetableTheme
//import com.rohan.timetable.utils.FileUtils
import com.rohan.timetable.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// 🔹 Coroutines
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private lateinit var timetableViewModel: TimetableViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timetableViewModel = TimetableViewModel(application)

        HandleShareIntent(intent)
        setContent {
            timetableTheme {
                val viewModel: TimetableViewModel = viewModel()
                TimetableApp(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        HandleShareIntent(intent)
    }

    private fun HandleShareIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND) {


            val textData = intent.getStringExtra(Intent.EXTRA_TEXT)

            if (textData != null) {
                Log.d("SharedJson", "Received via Text: $textData")
                timetableViewModel.importFromJson(textData);
            }
        }
        // 2. Fallback: Try to read as a File (Legacy/Standard Share)
        else {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (uri != null) {
                val json = contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }

                Log.d("SharedJson", "Received via File: ${json ?: "NULL"}")

            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableApp(viewModel: TimetableViewModel) {
    val context=LocalContext.current;
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope();
    val today = TimeUtils.getToday()
    var selectedScreen by remember { mutableStateOf(today) }
    var showAddClasses by remember { mutableStateOf(false) }
    LaunchedEffect(selectedScreen) {
        if (selectedScreen != "Settings") {
            viewModel.setDay(selectedScreen)
        }
    }

    val currentData by viewModel.classesForDay.collectAsState()
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
            floatingActionButton = {
                if(!selectedScreen.equals("Settings")){
                    FloatingActionButton(
                        onClick = {
                                showAddClasses=true;
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Class"
                        )
                    }
                }
            },
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
                // 🔹 ANIMATION WRAPPER
                AnimatedContent(
                    targetState = selectedScreen,
                    transitionSpec = {
                        // ⚡ LAG FIX: Use Slide + Fade instead of Scale.
                        // Scaling triggers expensive layout recalculations on every frame.
                        (fadeIn(animationSpec = tween(300)) +
                                slideInVertically(animationSpec = tween(300)) { height -> height / 10 })
                            .togetherWith(
                                fadeOut(animationSpec = tween(300))
                            )
                    },
                    label = "ScreenTransition"
                ) { targetState ->
                    if (targetState == "Settings") {
                        SettingsScreen()
                    } else {
                        if (currentData.isEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No classes today! 🎉",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            TimetableList(
                                entries = currentData, // Use the optimized variable
                                dayname = targetState, // ⚡ FIX: Use targetState, NOT selectedScreen (prevents text flicker)
                                onEdit = { /* Edit Click */ }
                            )
                        }
                    }
                }
                if(showAddClasses){
                    AddClass(
                        selectedScreen,
                        onDismiss = {showAddClasses=false},
                        onConfirm = {entry->
                            Log.e("AddClass","Entry: ${entry}");
                            Toast.makeText(context,"Added Entry",Toast.LENGTH_SHORT).show()
                            showAddClasses=false
                            viewModel.addClass(entry)
                        }
                    )
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

