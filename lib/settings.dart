import 'dart:convert';
import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
// import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:hive/hive.dart';
import 'package:package_info_plus/package_info_plus.dart';
import "package:path_provider/path_provider.dart";
import 'package:share_plus/share_plus.dart';
import 'package:timetable_app/main.dart';
import 'package:timetable_app/notification_service.dart';
import 'package:timetable_app/whats_new.dart';

import 'check_for_updates.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  PackageInfo? info;

  @override
  void initState() {
    super.initState();
    PackageInfo.fromPlatform().then((val) => setState(() => info = val));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text("Settings"),
        centerTitle: true,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16.0),
        children: [
          _buildSectionTitle("Data Management"),
          Card(
            elevation: 0,
            color: theme.colorScheme.surfaceVariant.withOpacity(0.3),
            child: Column(
              children: [
                _buildListTile(
                  icon: Icons.share_outlined,
                  title: "Export Timetable",
                  subtitle: "Save your schedule to a JSON file",
                  onTap: _exportTimetable,
                ),
                const Divider(height: 1, indent: 56),
                _buildListTile(
                  icon: Icons.file_upload_outlined,
                  title: "Import Timetable",
                  subtitle: "Load schedule from a saved file",
                  onTap: () => _importTimetable(context),
                ),
                const Divider(height: 1, indent: 56),
                _buildListTile(
                  icon: Icons.delete_outline,
                  iconColor: Colors.red,
                  title: "Clear Data",
                  titleColor: Colors.red,
                  subtitle: "Permanently delete all schedule data",
                  onTap: () => _showDeleteConfirmation(context),
                ),
                const Divider(
                  height: 1,
                  indent: 56,
                ),
                _buildListTile(
                    icon: Icons.notification_important_outlined,
                    title: "Test Notification",
                    onTap: () async {
                      await NotificationService().showTestNotification();
                    })
              ],
            ),
          ),
          const SizedBox(height: 24),
          _buildSectionTitle("App Info"),
          Card(
            elevation: 0,
            color: theme.colorScheme.surfaceVariant.withOpacity(0.3),
            child: Column(
              children: [
                _buildListTile(
                  icon: Icons.update,
                  title: "Check for Updates",
                  onTap: () async {
                    showCheckUpdatesSheet(context);
                    // ScaffoldMessenger.of(context).showSnackBar(
                    //   const SnackBar(
                    //       content: Text("You are on the latest version!")),
                    // );
                  },
                ),
                const Divider(height: 1, indent: 56),
                _buildListTile(
                    icon: Icons.celebration,
                    title: "What's New",
                    onTap: () {
                      showWhatsNewSheet(context);
                    }),
                const Divider(height: 1, indent: 56),
                _buildListTile(
                  icon: Icons.info_outline,
                  title: "Version",
                  subtitle: info == null
                      ? "Loading..."
                      : "${info!.version} (Build ${info!.buildNumber})",
                ),
                // const Divider(height: 1, indent: 56),
              ],
            ),
          ),
          const SizedBox(height: 24),
          _buildSectionTitle("Developer"),
          Card(
            elevation: 0,
            color: theme.colorScheme.surfaceVariant.withOpacity(0.3),
            child: Column(
              children: [
                const ListTile(
                  leading: Icon(Icons.person_outline),
                  title: Text("Rohan Batra"),
                  subtitle: Text("B.Tech Information Technology"),
                ),
                const Divider(height: 1, indent: 56),
                _buildListTile(
                  icon: Icons.alternate_email,
                  title: "Contact Support",
                  subtitle: "rohanbatra.in@gmail.com",
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // --- Helper Widgets ---

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 8),
      child: Text(
        title.toUpperCase(),
        style: const TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.bold,
          letterSpacing: 1.1,
          color: Colors.deepPurple,
        ),
      ),
    );
  }

  Widget _buildListTile({
    required IconData icon,
    required String title,
    String? subtitle,
    VoidCallback? onTap,
    Color? iconColor,
    Color? titleColor,
  }) {
    return ListTile(
      leading: Icon(icon, color: iconColor),
      title: Text(title,
          style: TextStyle(color: titleColor, fontWeight: FontWeight.w500)),
      subtitle: subtitle != null ? Text(subtitle) : null,
      onTap: onTap,
      trailing:
          onTap != null ? const Icon(Icons.chevron_right, size: 20) : null,
    );
  }

  Future<void> exportTimetableTest() async {
    try {
      // 1. Get Data from Hive
      var rawData = Hive.box("timetable").get("schedule_data");

      if (rawData == null) {
        print("❌ Export Error: No data found.");
        return;
      }

      // Handle if rawData is JSON String or Map
      Map<String, dynamic> sourceMap;
      if (rawData is String) {
        sourceMap = Map<String, dynamic>.from(jsonDecode(rawData));
      } else {
        sourceMap = Map<String, dynamic>.from(rawData);
      }

      // 2. Transform nested Map -> List for Android
      final Map<String, List<Map<String, dynamic>>> finalExport = {};

      sourceMap.forEach((day, daySchedule) {
        // daySchedule is {"8:00 AM": {...}, "9:00 AM": {...}}
        // We CANNOT cast this to List. It is a Map.

        Map<String, dynamic> timeSlots;
        if (daySchedule is String) {
          timeSlots = Map<String, dynamic>.from(jsonDecode(daySchedule));
        } else {
          timeSlots = Map<String, dynamic>.from(daySchedule);
        }

        List<Map<String, dynamic>> androidClasses = [];

        // Iterate the MAP (Key = Time, Value = Details)
        timeSlots.forEach((timeRange, details) {
          // 'details' is {"subject_name": "Math", ...}
          var mapItem = Map<String, dynamic>.from(details);

          androidClasses.add({
            // Take the KEY from the map and make it the "time" value
            "time": timeRange,
            "subjectName": mapItem['subject_name'] ?? "Unknown",
            "classroom": mapItem['classroom'] ?? "N/A",
            "classType": mapItem['class_type'] ?? "Lecture"
          });
        });

        // Sort by time (Optional, but nice for UI)
        // androidClasses.sort((a, b) => a['time'].compareTo(b['time']));

        finalExport[day] = androidClasses;
      });

      // 3. Save & Share
      final jsonString = jsonEncode(finalExport);
      final dir = await getApplicationDocumentsDirectory();
      final file = File("${dir.path}/timetable.json");
      await file.writeAsString(jsonString);

      print("✅ Export Success: ${file.path}");
      await Share.shareXFiles([XFile(file.path)], text: 'My Timetable Export');
    } catch (e, stack) {
      print("❌ Export Failed: $e");
      print(stack);
    }
  }

  Future<void> _exportTimetable() async {
    final boxData = Hive.box("timetable").get("schedule_data");
    final tt = jsonEncode(boxData);
    final dir = await getApplicationDocumentsDirectory();
    final file = await File("${dir.path}/timetable.json").create();
    await file.writeAsString(tt);

    await Share.shareXFiles([XFile(file.path)], text: 'My Timetable Export');
  }

  Future<void> _importTimetable(BuildContext context) async {
    final result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['json'],
    );

    if (result == null) return;

    final filePath = result.files.single.path!;
    final jsonString = await File(filePath).readAsString();
    final data = jsonDecode(jsonString);

    final box = Hive.box("timetable");
    await box.clear();
    await box.put("schedule_data", data);

    if (!mounted) return;
    RestartWidget.restartApp(context);
  }

  void _showDeleteConfirmation(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Clear all data?"),
        content: const Text(
            "This action cannot be undone. All your saved schedules will be lost."),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Cancel")),
          TextButton(
            onPressed: () {
              Hive.box('timetable').delete('schedule_data');
              RestartWidget.restartApp(context);
            },
            child: const Text("Clear", style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }
}
