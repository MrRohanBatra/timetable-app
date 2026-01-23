import 'package:flutter/material.dart';
import 'package:package_info_plus/package_info_plus.dart';

final List<Map<String, dynamic>> newFeatures = const [
  {
    "title": "Developer Message",
    "description":
    "🎉 We’re now on the Google Play Store! This update introduces an easy in-app migration so you can safely move your existing data to the new version.",
    "icon": Icons.campaign_outlined,

  },
  {
    "title": "Data Migration",
    "description":
    "Easily migrate your existing timetable and subjects to the new version without losing any data.",
    "icon": Icons.sync_alt_rounded,
  },
  {
    "title": "Class Reminders",
    "description":
    "Never miss a lecture again! Get smart notifications 5 minutes before your class starts.",
    "icon": Icons.notifications_active_outlined,
  },
  {
    "title": "Custom Subjects",
    "description":
    "Not in the list? No problem! You can now type and save any custom subject name you want.",
    "icon": Icons.edit_note_rounded,
  },
  {
    "title": "Bug Fixes",
    "description":
    "Fixed an issue where the display refresh rate was locked at 60Hz on OnePlus, Samsung, Nothing, and Oppo devices.",
    "icon": Icons.bug_report_outlined,
  },
];

// void showWhatsNewSheet(BuildContext context) {
//   final theme = Theme.of(context);
//
//   showModalBottomSheet(
//     context: context,
//     isScrollControlled: true,
//     shape: const RoundedRectangleBorder(
//       borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
//     ),
//     builder: (_) {
//       return Padding(
//         padding: const EdgeInsets.fromLTRB(24, 24, 24, 32),
//         child: Column(
//           mainAxisSize: MainAxisSize.min,
//           crossAxisAlignment: CrossAxisAlignment.start,
//           children: [
//             Center(
//               child: Container(
//                 width: 40,
//                 height: 4,
//                 margin: const EdgeInsets.only(bottom: 16),
//                 decoration: BoxDecoration(
//                   color: theme.colorScheme.outlineVariant,
//                   borderRadius: BorderRadius.circular(4),
//                 ),
//               ),
//             ),
//             Text(
//               "What’s New ✨",
//               style: theme.textTheme.headlineSmall?.copyWith(
//                 fontWeight: FontWeight.bold,
//               ),
//             ),
//             const SizedBox(height: 16),
//             ...newFeatures.map((item) {
//               return Padding(
//                 padding: const EdgeInsets.only(bottom: 16),
//                 child: Row(
//                   crossAxisAlignment: CrossAxisAlignment.start,
//                   children: [
//                     CircleAvatar(
//                       radius: 20,
//                       backgroundColor: theme.colorScheme.primaryContainer,
//                       child: Icon(
//                         item['icon'] ?? Icons.event,
//                         color: theme.colorScheme.primary,
//                       ),
//                     ),
//                     const SizedBox(width: 16),
//                     Expanded(
//                       child: Column(
//                         crossAxisAlignment: CrossAxisAlignment.start,
//                         children: [
//                           Text(
//                             item['title']!,
//                             style: theme.textTheme.titleMedium?.copyWith(
//                               fontWeight: FontWeight.w600,
//                             ),
//                           ),
//                           const SizedBox(height: 4),
//                           Text(
//                             item['description']!,
//                             style: theme.textTheme.bodyMedium?.copyWith(
//                               color: theme.colorScheme.onSurfaceVariant,
//                             ),
//                           ),
//                         ],
//                       ),
//                     ),
//                   ],
//                 ),
//               );
//             }),
//             const SizedBox(height: 8),
//             SizedBox(
//               width: double.infinity,
//               child: FilledButton(
//                 onPressed: () => Navigator.pop(context),
//                 child: const Text("Got it"),
//               ),
//             ),
//           ],
//         ),
//       );
//     },
//   );
// }
void showWhatsNewSheet(BuildContext context) async {
  final theme = Theme.of(context);
  // Get version info for the footer
  final packageInfo = await PackageInfo.fromPlatform();
  final version = packageInfo.version;

  if (!context.mounted) return;

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
    ),
    builder: (_) {
      return Padding(
        padding: const EdgeInsets.fromLTRB(24, 12, 24, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 32,
                height: 4,
                margin: const EdgeInsets.only(bottom: 24, top: 8),
                decoration: BoxDecoration(
                  color: theme.colorScheme.outlineVariant,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  "What’s New ✨",
                  style: theme.textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  "v$version",
                  style: theme.textTheme.labelLarge?.copyWith(
                    color: theme.colorScheme.primary.withOpacity(0.7),
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            ...newFeatures.map((item) {
              return Padding(
                padding: const EdgeInsets.only(bottom: 20),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color:
                            theme.colorScheme.primaryContainer.withOpacity(0.4),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Icon(
                        item['icon'] ?? Icons.event,
                        color: theme.colorScheme.primary,
                        size: 24,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            item['title']!,
                            style: theme.textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            item['description']!,
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                              height: 1.3,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              );
            }),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              height: 50,
              child: FilledButton(
                style: FilledButton.styleFrom(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                onPressed: () => Navigator.pop(context),
                child: const Text("Got it", style: TextStyle(fontSize: 16)),
              ),
            ),
          ],
        ),
      );
    },
  );
}
