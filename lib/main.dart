import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:intl/intl.dart';
import 'package:flex_color_scheme/flex_color_scheme.dart';
import "package:hive_flutter/hive_flutter.dart";
import 'package:package_info_plus/package_info_plus.dart';
import 'package:timetable_app/check_for_updates.dart';
import 'package:timetable_app/settings.dart';

import 'manage.dart';
import 'notification_service.dart';
import "whats_new.dart";

late String day;
Future<void> checkAndShowWhatsNew(BuildContext context) async {
  final packageInfo = await PackageInfo.fromPlatform();
  final currentVersion = packageInfo.version; // e.g. 1.1.5

  final appStateBox = Hive.box('app_state');

  final lastSeenVersion =
      appStateBox.get("last_seen_version", defaultValue: "");

  if (currentVersion != lastSeenVersion) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      showWhatsNewSheet(context);
      appStateBox.put("last_seen_version", currentVersion);
    });
  }
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize Hive
  await Hive.initFlutter();
  await Hive.openBox('timetable');
  await Hive.openBox("app_state");
  day = getTodayDay();
  await NotificationService().init();
  runApp(const RestartWidget(child: const MyApp()));
}

String getTodayDay() {
  final now = DateTime.now();
  final format = DateFormat("EEEE");
  return format.format(now);
}

class RestartWidget extends StatefulWidget {
  final Widget child;
  const RestartWidget({super.key, required this.child});

  static void restartApp(BuildContext context) {
    context.findAncestorStateOfType<_RestartWidgetState>()?.restart();
  }

  @override
  State<RestartWidget> createState() => _RestartWidgetState();
}

Future<void> syncNotifications() async {
  final service = NotificationService();
  await service.cancelAll(); // Clear old ones to avoid duplicates

  final timetable = loadTimetable();
  int idCounter = 0;

  timetable.forEach((day, sessions) {
    final sessionMap = Map<String, dynamic>.from(sessions);
    sessionMap.forEach((timeRange, details) {
      service.scheduleClassNotification(
        id: idCounter++,
        day: day,
        timeRange: timeRange,
        subject: details['subject_name'],
        room: details['classroom'],
      );
    });
  });
}

class _RestartWidgetState extends State<RestartWidget> {
  Key key = UniqueKey();

  void restart() {
    setState(() {
      key = UniqueKey();
    });
  }

  @override
  Widget build(BuildContext context) {
    return KeyedSubtree(
      key: key,
      child: widget.child,
    );
  }
}

Future<String?> selectTimeRange(BuildContext context) async {
  // Pick Start Time
  TimeOfDay? start = await showTimePicker(
    context: context,
    initialTime: const TimeOfDay(hour: 9, minute: 0),
    helpText: "SELECT START TIME",
  );
  if (start == null) return null;

  // Pick End Time
  TimeOfDay? end = await showTimePicker(
    context: context,
    initialTime: TimeOfDay(hour: start.hour + 1, minute: start.minute),
    helpText: "SELECT END TIME",
  );
  if (end == null) return null;

  // Format to your existing string style
  final String startStr = start.format(context);
  final String endStr = end.format(context);
  return "$startStr - $endStr";
}

/// Loads timetable from Hive.
/// If Hive is empty, it returns a default empty structure for the week.
Map<String, dynamic> loadTimetable() {
  final box = Hive.box('timetable');
  String? jsonString = box.get('schedule_data');

  if (jsonString != null) {
    return Map<String, dynamic>.from(json.decode(jsonString));
  } else {
    // Default empty structure if nothing is in Hive yet
    final emptyWeek = {
      "Monday": {},
      "Tuesday": {},
      "Wednesday": {},
      "Thursday": {},
      "Friday": {},
      "Saturday": {},
      "Sunday": {},
    };
    // Save this initial structure so we have something to edit later
    box.put('schedule_data', json.encode(emptyWeek));
    return emptyWeek;
  }
}

Map<String, dynamic> loadTimetableSorted() {
  final box = Hive.box('timetable');
  String? jsonString = box.get('schedule_data');

  final Map<String, dynamic> weekData;

  if (jsonString != null) {
    weekData = Map<String, dynamic>.from(json.decode(jsonString));
  } else {
    weekData = {
      "Monday": {},
      "Tuesday": {},
      "Wednesday": {},
      "Thursday": {},
      "Friday": {},
      "Saturday": {},
      "Sunday": {},
    };
    box.put('schedule_data', json.encode(weekData));
  }

  /// Sort each day's classes by time
  final sortedWeek = <String, dynamic>{};

  weekData.forEach((day, sessions) {
    final sessionMap = Map<String, dynamic>.from(sessions);

    final sortedEntries = sessionMap.entries.toList()
      ..sort((a, b) {
        final aTime = _startTimeInMinutes(a.key);
        final bTime = _startTimeInMinutes(b.key);
        return aTime.compareTo(bTime);
      });

    sortedWeek[day] = Map.fromEntries(sortedEntries);
  });

  return sortedWeek;
}

int _startTimeInMinutes(String timeRange) {
  // Example: "8:00 AM - 9:00 AM"
  final start = timeRange.split('-').first.trim();
  final parts = start.split(' ');

  final time = parts[0]; // 8:00
  final period = parts[1]; // AM / PM

  final hourMinute = time.split(':');
  int hour = int.parse(hourMinute[0]);
  final int minute = int.parse(hourMinute[1]);

  if (period == 'PM' && hour != 12) hour += 12;
  if (period == 'AM' && hour == 12) hour = 0;

  return hour * 60 + minute;
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'My Timetable',
      theme: FlexThemeData.light(
        scheme: FlexScheme.deepPurple,
        useMaterial3: true,
      ),
      darkTheme: FlexThemeData.dark(
        darkIsTrueBlack: false,
        useMaterial3: true,
      ),
      themeMode: ThemeMode.system,
      // home: const TimetableScreen(),
      initialRoute: "/",
      routes: {
        '/': (context) => const TimetableScreen(),
      },
    );
  }
}

class TimetableScreen extends StatefulWidget {
  const TimetableScreen({super.key});

  @override
  State<TimetableScreen> createState() => _TimetableScreenState();
}

class _TimetableScreenState extends State<TimetableScreen> {
  late Map<String, dynamic> timetable;
  String selectedDay = day;

  @override
  void initState() {
    super.initState();
    timetable = loadTimetableSorted();
    _requestNotificationPermissions();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      checkAndShowWhatsNew(context);
      syncNotifications();
      Future.delayed(Duration(seconds: 4), () {
        checkUpdateInBackground(context);
      });
    });
  }

  Future<void> _requestNotificationPermissions() async {
    final android = NotificationService()
        .flutterLocalNotificationsPlugin
        .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin>();

    await android?.requestNotificationsPermission();
    await android?.requestExactAlarmsPermission();
  }

  /// Saves the current state of [timetable] to Hive
  void _saveToHive() {
    final box = Hive.box('timetable');
    box.put('schedule_data', json.encode(timetable));
    syncNotifications();
  }

  String classType(String a) {
    switch (a) {
      case 'L':
        return 'Lecture';
      case 'T':
        return 'Tutorial';
      case 'P':
        return 'Practical';
      default:
        return 'Lecture';
    }
  }

  IconData getClassIcon(String classType) {
    switch (classType) {
      case 'L':
        return Icons.menu_book_rounded;
      case 'T':
        return Icons.my_library_books_rounded;
      case 'P':
        return Icons.computer_sharp;
      default:
        return Icons.class_rounded;
    }
  }

  bool isClassLive(String timeSlot) {
    try {
      final now = DateTime.now();
      final parts = timeSlot.split(' - ');
      final DateFormat parser = DateFormat("h:mm a");
      final DateTime startTime = parser.parse(parts[0]);
      final DateTime endTime = parser.parse(parts[1]);

      final DateTime classStart = DateTime(
          now.year, now.month, now.day, startTime.hour, startTime.minute);
      final DateTime classEnd =
          DateTime(now.year, now.month, now.day, endTime.hour, endTime.minute);

      return now.isAfter(classStart) && now.isBefore(classEnd);
    } catch (e) {
      return false;
    }
  }

  /// Dialog to edit a specific class entry
  Future<void> _editEntry(String timeSlot, Map<String, dynamic> details) async {
    final subjectController =
        TextEditingController(text: details['subject_name']);
    final roomController = TextEditingController(text: details['classroom']);
    String selectedType = details['class_type'] ?? 'L';

    await showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text("Edit Class ($timeSlot)"),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: subjectController,
                  decoration: const InputDecoration(labelText: "Subject Name"),
                ),
                TextField(
                  controller: roomController,
                  decoration:
                      const InputDecoration(labelText: "Classroom / Lab"),
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  value: selectedType,
                  decoration: const InputDecoration(labelText: "Class Type"),
                  items: const [
                    DropdownMenuItem(value: 'L', child: Text("Lecture (L)")),
                    DropdownMenuItem(value: 'T', child: Text("Tutorial (T)")),
                    DropdownMenuItem(value: 'P', child: Text("Practical (P)")),
                  ],
                  onChanged: (val) {
                    if (val != null) selectedType = val;
                  },
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              style: TextButton.styleFrom(
                foregroundColor: Colors.red,
              ),
              onPressed: () async {
                final confirm = await showDialog<bool>(
                  context: context,
                  builder: (ctx) => AlertDialog(
                    title: const Text("Delete Class"),
                    content: Text(
                      "Are you sure you want to delete the class at $timeSlot?",
                    ),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(ctx, false),
                        child: const Text("Cancel"),
                      ),
                      FilledButton(
                        style: FilledButton.styleFrom(
                          backgroundColor: Colors.red,
                        ),
                        onPressed: () => Navigator.pop(ctx, true),
                        child: const Text("Delete"),
                      ),
                    ],
                  ),
                );

                if (confirm != true) return;

                setState(() {
                  timetable[selectedDay]?.remove(timeSlot);
                });

                _saveToHive();

                if (context.mounted) {
                  Navigator.pop(context); // close edit dialog
                }
              },
              child: const Text("Delete"),
            ),
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Cancel"),
            ),
            FilledButton(
              onPressed: () {
                setState(() {
                  // Update the local map
                  timetable[selectedDay][timeSlot] = {
                    "subject_name": subjectController.text,
                    "classroom": roomController.text,
                    "class_type": selectedType,
                    // Preserve existing teacher data if you have it, or defaults
                    "teacher": details['teacher'] ?? "Unknown",
                  };
                });
                _saveToHive();
                Navigator.pop(context);
              },
              child: const Text("Save"),
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final days = timetable.keys.toList();
    // Ensure we handle cases where the day key might be missing
    final daySchedule = Map<String, dynamic>.from(timetable[selectedDay] ?? {});
    final today = getTodayDay();
    final theme = Theme.of(context);
    final isToday = selectedDay == today;

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          "Timetable",
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        centerTitle: true,
      ),
      drawer: Drawer(
        backgroundColor: theme.colorScheme.surface,
        child: Column(
          children: [
            UserAccountsDrawerHeader(
              decoration: BoxDecoration(color: theme.colorScheme.surface),
              accountName: Text(
                'Timetable Viewer',
                style: TextStyle(
                  color: theme.colorScheme.onSurface,
                  fontWeight: FontWeight.bold,
                ),
              ),
              accountEmail: Text(
                'Today: $today',
                style: TextStyle(color: theme.colorScheme.onSurfaceVariant),
              ),
              currentAccountPicture: CircleAvatar(
                backgroundColor: theme.colorScheme.primaryContainer,
                child: Icon(
                  Icons.calendar_month_outlined,
                  color: theme.colorScheme.onPrimaryContainer,
                ),
              ),
            ),
            ...days.map((dayName) {
              final isSelected = dayName == selectedDay;
              final isDayToday = dayName == today;
              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8.0),
                child: ListTile(
                  leading: Icon(
                    isDayToday
                        ? Icons.today_rounded
                        : Icons.calendar_view_day_rounded,
                  ),
                  title: Text(
                    dayName,
                    style: TextStyle(
                      fontWeight:
                          isSelected ? FontWeight.bold : FontWeight.normal,
                    ),
                  ),
                  selected: isSelected,
                  selectedTileColor: theme.colorScheme.primary.withOpacity(0.1),
                  onTap: () {
                    setState(() {
                      selectedDay = dayName;
                    });
                    Navigator.pop(context);
                  },
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
              );
            }),
            const Spacer(),
            const Divider(),
            // ListTile(
            //   leading: const Icon(Icons.delete_outline_rounded, color: Colors.red),
            //   title: const Text("Clear All Data", style: TextStyle(color: Colors.red)),
            //   onTap: () {
            //     // Simple reset logic
            //     Hive.box('timetable').delete('schedule_data');
            //     setState(() {
            //       timetable = loadTimetable();
            //     });
            //     Navigator.pop(context);
            //   },
            // ),
            ListTile(
              leading: const Icon(Icons.edit_calendar_rounded),
              title: const Text("Manage Schedule"),
              onTap: () {
                Navigator.pop(context); // Close drawer
                Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (context) =>
                            const ManageTimetableScreen())).then((_) async {
                  setState(() {
                    timetable = loadTimetableSorted();
                  }); // Refresh UI on return
                  await syncNotifications();
                });
              },
            ),
            ListTile(
              leading: const Icon(Icons.settings),
              title: const Text("Settings"),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (context) => const SettingsPage()));
              },
            ),
          ],
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.only(top: 16.0, bottom: 16.0),
              child: Row(
                children: [
                  Text(
                    selectedDay,
                    style: theme.textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  if (isToday)
                    Padding(
                      padding: const EdgeInsets.only(left: 12.0),
                      child: Chip(
                        label: const Text("Today"),
                        labelStyle: TextStyle(
                          color: theme.colorScheme.onSurface,
                          fontWeight: FontWeight.bold,
                        ),
                        backgroundColor: theme.chipTheme.backgroundColor,
                      ),
                    ),
                ],
              ),
            ),
            Expanded(
              child: daySchedule.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.edit_note_rounded,
                            size: 64,
                            color: Colors.grey.shade400,
                          ),
                          const SizedBox(height: 16),
                          Text(
                            'No Classes Set',
                            style: theme.textTheme.titleLarge?.copyWith(
                              color: Colors.grey.shade600,
                            ),
                          ),
                          const SizedBox(height: 8),
                          // Optional: Add button to add a class here
                          const Text(
                              "Tap + to add a class (Not implemented yet)")
                        ],
                      ),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.only(top: 8),
                      itemCount: daySchedule.length,
                      itemBuilder: (context, index) {
                        final entry = daySchedule.entries.elementAt(index);
                        final time = entry.key;
                        final details = entry.value;
                        final type = classType(details['class_type']);
                        final bool live = isToday && isClassLive(time);

                        return Card(
                          margin: const EdgeInsets.only(bottom: 16),
                          clipBehavior: Clip.antiAlias,
                          child: InkWell(
                            // Make the card clickable for editing
                            onTap: () => _editEntry(time, details),
                            child: Padding(
                              padding: const EdgeInsets.all(16.0),
                              child: Row(
                                children: [
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Icon(
                                          getClassIcon(details['class_type']),
                                        ),
                                        const SizedBox(height: 4),
                                        Text(
                                          details['subject_name'],
                                          style: theme.textTheme.titleMedium
                                              ?.copyWith(
                                            fontWeight: FontWeight.bold,
                                          ),
                                        ),
                                        const SizedBox(height: 12),
                                        Row(
                                          children: [
                                            Icon(
                                              Icons.access_time_rounded,
                                              size: 16,
                                              color: theme
                                                  .colorScheme.onSurfaceVariant,
                                            ),
                                            const SizedBox(width: 8),
                                            Text(
                                              time,
                                              style: TextStyle(
                                                color: theme.colorScheme
                                                    .onSurfaceVariant,
                                              ),
                                            ),
                                            const SizedBox(width: 16),
                                            Icon(
                                              Icons.location_on_rounded,
                                              size: 16,
                                              color: theme
                                                  .colorScheme.onSurfaceVariant,
                                            ),
                                            const SizedBox(width: 8),
                                            Text(
                                              details['classroom'],
                                              style: TextStyle(
                                                color: theme.colorScheme
                                                    .onSurfaceVariant,
                                              ),
                                            ),
                                          ],
                                        ),
                                      ],
                                    ),
                                  ),
                                  const SizedBox(width: 16),
                                  Column(
                                    children: [
                                      if (live)
                                        Row(
                                          children: const [
                                            Icon(
                                              Icons.circle,
                                              color: Colors.green,
                                              size: 12,
                                            ),
                                            SizedBox(width: 4),
                                            Text(
                                              "Live",
                                              style: TextStyle(
                                                color: Colors.green,
                                                fontWeight: FontWeight.bold,
                                              ),
                                            ),
                                          ],
                                        ),
                                      if (live) const SizedBox(height: 8),
                                      Chip(label: Text(type)),
                                      // Edit hint icon
                                      const SizedBox(height: 8),
                                      const Icon(Icons.edit,
                                          size: 14, color: Colors.grey)
                                    ],
                                  ),
                                ],
                              ),
                            ),
                          ),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
      // floatingActionButton: FloatingActionButton(
      //   onPressed: () {
      //     // Navigate to ManageTimetableScreen passing the currently viewed day
      //     Navigator.push(
      //       context,
      //       MaterialPageRoute(
      //         builder: (context) =>
      //             ManageTimetableScreen(initialDay: selectedDay),
      //       ),
      //     ).then((_) {
      //       // This refreshes the main screen UI when you come back from managing
      //       setState(() {
      //         timetable = loadTimetable();
      //       });
      //     });
      //   },
      //   tooltip: "Edit Schedule",
      //   child: const Icon(
      //       Icons.edit_note_rounded), // Changed to an edit-style icon
      // ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) =>
                  ManageTimetableScreen(initialDay: selectedDay),
            ),
          ).then((_) async {
            // <--- 1. Mark this callback as 'async'

            // 2. Reload the latest data from Hive to update the UI
            final updatedTimetable = loadTimetableSorted();

            setState(() {
              timetable = updatedTimetable;
            });

            // 3. Force a sync with the Notification Service
            print("🔄 Syncing notifications with new schedule...");
            await syncNotifications();
            print("✅ Sync complete!");
          });
        },
        tooltip: "Edit Schedule",
        child: const Icon(Icons.edit_note_rounded),
      ),
    );
  }
}
