import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:timezone/data/latest_all.dart' as tz;
import 'package:timezone/timezone.dart' as tz;
// import 'package:flutter_timezone/flutter_timezone.dart'; // Not needed if hardcoding

class NotificationService {
  static final NotificationService _instance = NotificationService._internal();
  factory NotificationService() => _instance;
  NotificationService._internal();

  final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

  Future<void> init() async {
    tz.initializeTimeZones();

    try {
      // HARDCODED: Force India Standard Time as requested
      const String timeZoneName = 'Asia/Kolkata';
      tz.setLocalLocation(tz.getLocation(timeZoneName));
      print("✅ FORCE SET Timezone to: $timeZoneName");
    } catch (e) {
      print("⚠️ Error setting timezone: $e");
      tz.setLocalLocation(tz.UTC);
    }

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

  tz.TZDateTime _nextInstanceOfDayTime(String day, int hour, int minute) {
    final tz.TZDateTime now = tz.TZDateTime.now(tz.local);
    int dayIndex = _getDayIndex(day);

    tz.TZDateTime scheduledDate =
        tz.TZDateTime(tz.local, now.year, now.month, now.day, hour, minute);

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
    required String timeRange,
    required String subject,
    required String room,
  }) async {
    final startTimeStr = timeRange.split('-').first.trim();
    final parts = startTimeStr.split(' ');
    final timeParts = parts[0].split(':');
    int hour = int.parse(timeParts[0]);
    int minute = int.parse(timeParts[1]);
    if (parts[1] == 'PM' && hour != 12) hour += 12;
    if (parts[1] == 'AM' && hour == 12) hour = 0;

    tz.TZDateTime scheduledTime = _nextInstanceOfDayTime(day, hour, minute);
    scheduledTime = scheduledTime.subtract(const Duration(minutes: 5));

    if (scheduledTime.isBefore(tz.TZDateTime.now(tz.local))) {
      scheduledTime = scheduledTime.add(const Duration(days: 7));
    }

    print("📅 SCHEDULING ALERT: $subject for $scheduledTime");

    // NEW FORMAT: Concise & Bold
    // Title: "Maths @ Room 302" (Bold by default on Android)
    // Body: "Starts in 5 mins"

    final AndroidNotificationDetails androidDetails =
        AndroidNotificationDetails(
      'timetable_channel',
      'Class Reminders',
      importance: Importance.max,
      priority: Priority.high,
      styleInformation: BigTextStyleInformation(
        'Get ready! $subject is starting in 5 minutes at $room.', // Expanded text
        htmlFormatBigText: true,
        contentTitle: '<b>$subject @ $room</b>', // Expanded Title (Bold)
        htmlFormatContentTitle: true,
      ),
    );

    await flutterLocalNotificationsPlugin.zonedSchedule(
      id,
      '$subject @ $room', // Collapsed Title (Visible on Lock Screen)
      'Starts in 5 mins', // Collapsed Body
      scheduledTime,
      NotificationDetails(
        android: androidDetails,
        iOS: const DarwinNotificationDetails(),
      ),
      androidScheduleMode: AndroidScheduleMode.exactAllowWhileIdle,
      matchDateTimeComponents: DateTimeComponents.dayOfWeekAndTime,
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
      999,
      'Timetable Test',
      'This is how your class alerts will look!',
      platformDetails,
    );
  }

  Future<void> cancelAll() async =>
      await flutterLocalNotificationsPlugin.cancelAll();
}
