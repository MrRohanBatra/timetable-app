import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:timezone/data/latest_all.dart' as tz;
import 'package:timezone/timezone.dart' as tz;

class NotificationService {
  static final NotificationService _instance = NotificationService._internal();
  factory NotificationService() => _instance;
  NotificationService._internal();

  final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

  Future<void> init() async {
    tz.initializeTimeZones();

    const AndroidInitializationSettings androidSettings =
        AndroidInitializationSettings('@mipmap/launcher_icon');
    const DarwinInitializationSettings iosSettings =
        DarwinInitializationSettings();

    const InitializationSettings initSettings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    await flutterLocalNotificationsPlugin.initialize(initSettings);
  }

  // Helper to convert day string to TZ DateTime
  tz.TZDateTime _nextInstanceOfDayTime(String day, int hour, int minute) {
    final tz.TZDateTime now = tz.TZDateTime.now(tz.local);
    int dayIndex = _getDayIndex(day); // Monday = 1, Sunday = 7

    tz.TZDateTime scheduledDate =
        tz.TZDateTime(tz.local, now.year, now.month, now.day, hour, minute);

    // If the time has already passed today, or it's a different day, move to next occurrence
    while (scheduledDate.isBefore(now) || scheduledDate.weekday != dayIndex) {
      scheduledDate = scheduledDate.add(const Duration(days: 1));
    }
    return scheduledDate;
  }

  int _getDayIndex(String day) {
    switch (day) {
      case 'Monday':
        return 1;
      case 'Tuesday':
        return 2;
      case 'Wednesday':
        return 3;
      case 'Thursday':
        return 4;
      case 'Friday':
        return 5;
      case 'Saturday':
        return 6;
      case 'Sunday':
        return 7;
      default:
        return 1;
    }
  }

  Future<void> scheduleClassNotification({
    required int id,
    required String day,
    required String timeRange, // "9:00 AM - 10:00 AM"
    required String subject,
    required String room,
  }) async {
    // Parse the start time
    final startTimeStr = timeRange.split('-').first.trim(); // "9:00 AM"
    final parts = startTimeStr.split(' ');
    final timeParts = parts[0].split(':');
    int hour = int.parse(timeParts[0]);
    int minute = int.parse(timeParts[1]);
    if (parts[1] == 'PM' && hour != 12) hour += 12;
    if (parts[1] == 'AM' && hour == 12) hour = 0;

    // Schedule 5 minutes before class
    final scheduledTime = _nextInstanceOfDayTime(day, hour, minute)
        .subtract(const Duration(minutes: 5));

    await flutterLocalNotificationsPlugin.zonedSchedule(
      id,
      'Upcoming Class: $subject',
      'Starts in 5 mins at $room',
      scheduledTime,
      const NotificationDetails(
        android: AndroidNotificationDetails(
          'timetable_channel',
          'Class Reminders',
          importance: Importance.max,
          priority: Priority.high,
        ),
        iOS: DarwinNotificationDetails(), // Added for compatibility
      ),
      // Updated for newer versions:
      androidScheduleMode: AndroidScheduleMode.exactAllowWhileIdle,
      matchDateTimeComponents: DateTimeComponents.dayOfWeekAndTime,
      // The line 'uiLocalNotificationDateInterpretation' is deleted
    );
  }

  Future<void> showTestNotification() async {
    const AndroidNotificationDetails androidDetails =
        AndroidNotificationDetails(
      'test_channel',
      'Test Notifications',
      channelDescription: 'Used for testing if notifications work',
      importance: Importance.max,
      priority: Priority.high,
    );

    const NotificationDetails platformDetails = NotificationDetails(
      android: androidDetails,
      iOS: DarwinNotificationDetails(),
    );

    await flutterLocalNotificationsPlugin.show(
      999, // Unique ID for test
      'Test Notification',
      'Success! Your notifications are working correctly.',
      platformDetails,
    );
  }

  Future<void> cancelAll() async =>
      await flutterLocalNotificationsPlugin.cancelAll();
}
