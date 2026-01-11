import 'package:flutter/material.dart';

Future<bool> fetchLatestVersion() async {
  // TODO: Implement GitHub API logic here
  await Future.delayed(const Duration(seconds: 2)); // fake delay
  return false;
}

void showCheckUpdatesSheet(BuildContext context) {
  final theme = Theme.of(context);

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    isDismissible: false,
    enableDrag: false,
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
            // Drag handle (visual consistency)
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

            // Title
            Text(
              "Checking for updates",
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 16),

            // Content row (same layout as What's New)
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                CircleAvatar(
                  radius: 20,
                  backgroundColor: theme.colorScheme.primaryContainer,
                  child: CircularProgressIndicator(
                    strokeWidth: 2.5,
                    color: theme.colorScheme.primary,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        "Please wait",
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        "We are checking if a new version is available.",
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),

            const SizedBox(height: 24),

            // Disabled action button (for visual consistency)
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: null,
                child: const Text("Checking…"),
              ),
            ),
          ],
        ),
      );
    },
  );

  // Trigger async check
  _handleUpdateCheck(context);
}

/// =======================================================
/// HANDLE UPDATE CHECK (LOGIC LATER)
/// =======================================================
Future<void> _handleUpdateCheck(BuildContext context) async {
  final hasUpdate = await fetchLatestVersion();

  if (!context.mounted) return;

  Navigator.pop(context); // Close bottom sheet

  // TODO: Replace with real logic later
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(
      content: Text(
        hasUpdate ? "Update available" : "You are on the latest version",
      ),
    ),
  );
}
