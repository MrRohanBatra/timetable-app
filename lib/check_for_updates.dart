import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:package_info_plus/package_info_plus.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:dio/dio.dart';
import 'package:path_provider/path_provider.dart';
import 'package:open_filex/open_filex.dart';

Future<void> checkUpdateInBackground(BuildContext context) async {
  final latest = await fetchLatestRelease();
  final info = await PackageInfo.fromPlatform();

  if (!context.mounted) return;
  if (latest == null) return;

  if (latest["version"] != info.version) {
    _showUpdateAvailableSheet(
      context,
      latest["version"]!,
      latest["apk_url"]!,
    );
  }
}

/// =======================================================
/// INSTALL PERMISSION
/// =======================================================
Future<bool> _ensureInstallPermission(BuildContext context) async {
  if (!Platform.isAndroid) return false;

  final status = await Permission.requestInstallPackages.status;
  if (status.isGranted) return true;

  final result = await Permission.requestInstallPackages.request();
  return result.isGranted;
}

/// =======================================================
/// FETCH LATEST GITHUB RELEASE (SAFE)
/// =======================================================
Future<Map<String, String>?> fetchLatestRelease() async {
  const apiUrl =
      "https://api.github.com/repos/MrRohanBatra/timetable-app/releases/latest";

  try {
    final res = await http.get(Uri.parse(apiUrl));
    if (res.statusCode != 200) return null;

    final data = jsonDecode(res.body);

    if (data["published_at"] == null) return null;

    final assets = data["assets"] as List;
    if (assets.isEmpty) return null;

    final apk = assets.firstWhere(
      (a) => a["name"].toString().endsWith(".apk"),
      orElse: () => null,
    );

    if (apk == null) return null;

    return {
      "version": data["tag_name"].replaceFirst("v", ""),
      "apk_url": apk["browser_download_url"],
    };
  } catch (_) {
    return null;
  }
}

/// =======================================================
/// PUBLIC ENTRY POINT
/// =======================================================
void showCheckUpdatesSheet(BuildContext context) {
  _showCheckingSheet(context);
  _handleUpdateCheck(context);
}

/// =======================================================
/// UPDATE CHECK LOGIC
/// =======================================================
Future<void> _handleUpdateCheck(BuildContext context) async {
  final latest = await fetchLatestRelease();
  final info = await PackageInfo.fromPlatform();

  if (!context.mounted) return;
  Navigator.pop(context); // close checking sheet

  if (latest == null) {
    _snack(context, "Unable to check for updates");
    return;
  }

  if (latest["version"] != info.version) {
    _showUpdateAvailableSheet(
      context,
      latest["version"]!,
      latest["apk_url"]!,
    );
  } else {
    _snack(context, "You are on the latest version");
  }
}

/// =======================================================
/// DOWNLOAD + INSTALL
/// =======================================================
final ValueNotifier<double> _progress = ValueNotifier(0);

Future<void> _startDownload(BuildContext context, String apkUrl) async {
  if (!await _ensureInstallPermission(context)) {
    _snack(context, "Allow install permission to continue");
    return;
  }

  _showDownloadSheet(context);

  final dir = await getExternalStorageDirectory();
  final file = File("${dir!.path}/timetable_update.apk");

  final dio = Dio();
  await dio.download(
    apkUrl,
    file.path,
    onReceiveProgress: (r, t) {
      if (t != -1) {
        _progress.value = (r / t) * 100;
      }
    },
  );

  if (!context.mounted) return;
  Navigator.pop(context);

  // 🔥 SYSTEM INSTALLER (STABLE)
  await OpenFilex.open(file.path);
}

/// =======================================================
/// COMMON BOTTOM SHEET (SINGLE SOURCE OF TRUTH)
/// =======================================================
void _showCommonSheet({
  required BuildContext context,
  required String title,
  required Widget leading,
  required String heading,
  required String description,
  Widget? footer,
  bool dismissible = false,
}) {
  final theme = Theme.of(context);

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    isDismissible: dismissible,
    enableDrag: dismissible,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
    ),
    builder: (_) {
      return Padding(
        padding: const EdgeInsets.fromLTRB(24, 24, 24, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                margin: const EdgeInsets.only(bottom: 16),
                decoration: BoxDecoration(
                  color: theme.colorScheme.outlineVariant,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
            ),
            Text(
              title,
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                CircleAvatar(
                  radius: 20,
                  backgroundColor: theme.colorScheme.primaryContainer,
                  child: leading,
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        heading,
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        description,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            if (footer != null) ...[
              const SizedBox(height: 24),
              footer,
            ],
          ],
        ),
      );
    },
  );
}

/// =======================================================
/// SHEETS (USING COMMON DESIGN)
/// =======================================================
void _showCheckingSheet(BuildContext context) {
  _showCommonSheet(
    context: context,
    title: "Checking for updates",
    leading: SizedBox(
      width: 20,
      height: 20,
      child: CircularProgressIndicator(
        strokeWidth: 2.5,
        color: Theme.of(context).colorScheme.primary,
      ),
    ),
    heading: "Please wait",
    description: "We’re checking if a new version is available.",
    footer: SizedBox(
      width: double.infinity,
      child: FilledButton(
        onPressed: null,
        child: const Text("Checking…"),
      ),
    ),
  );
}

void _showUpdateAvailableSheet(
  BuildContext context,
  String version,
  String apkUrl,
) {
  _showCommonSheet(
    context: context,
    title: "Update available",
    leading: Icon(
      Icons.system_update_alt,
      color: Theme.of(context).colorScheme.primary,
    ),
    heading: "Version $version",
    description: "A new version is ready with improvements and fixes.",
    footer: Row(
      children: [
        Expanded(
          child: OutlinedButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("Later"),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: FilledButton(
            onPressed: () {
              Navigator.pop(context);
              _startDownload(context, apkUrl);
            },
            child: const Text("Update now"),
          ),
        ),
      ],
    ),
    dismissible: true,
  );
}

void _showDownloadSheet(BuildContext context) {
  _progress.value = 0;

  _showCommonSheet(
    context: context,
    title: "Downloading update",
    leading: const Icon(Icons.download),
    heading: "Please wait",
    description: "Downloading the latest version.",
    footer: ValueListenableBuilder<double>(
      valueListenable: _progress,
      builder: (_, v, __) => Column(
        children: [
          LinearProgressIndicator(value: v / 100),
          const SizedBox(height: 8),
          Text("${v.toStringAsFixed(0)}%"),
        ],
      ),
    ),
  );
}

/// =======================================================
/// UTIL
/// =======================================================
void _snack(BuildContext context, String msg) {
  ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
}
