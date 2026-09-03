import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../providers/app_provider.dart';
import '../models/log_entry.dart';

class ActivityLogFragment extends StatelessWidget {
  const ActivityLogFragment({super.key});

  String _formatHeaderDate(String isoString) {
    try {
      final date = DateTime.parse(isoString);
      final now = DateTime.now();
      final today = DateTime(now.year, now.month, now.day);
      final yesterday = DateTime(now.year, now.month, now.day - 1);
      final compareDate = DateTime(date.year, date.month, date.day);

      if (compareDate == today) return 'TODAY';
      if (compareDate == yesterday) return 'YESTERDAY';
      return DateFormat('MMMM dd, yyyy').format(date).toUpperCase();
    } catch (_) {
      return 'RECENT';
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<AppProvider>(context);
    final logs = provider.logs;

    if (logs.isEmpty) {
      return const Center(child: Text('Walang nakatala sa history log.'));
    }

    final Map<String, List<LogEntry>> groupedLogs = {};
    for (var log in logs) {
      final header = _formatHeaderDate(log.timestamp);
      groupedLogs.putIfAbsent(header, () => []).add(log);
    }

    return ListView.builder(
      itemCount: groupedLogs.keys.length,
      itemBuilder: (context, index) {
        final groupKey = groupedLogs.keys.elementAt(index);
        final groupItems = groupedLogs[groupKey]!;

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: Text(
                groupKey,
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
            ),
            ...groupItems.map((log) => ListTile(
              leading: const Icon(Icons.history),
              title: Text(log.title),
              subtitle: Text(log.action),
              trailing: Text(
                DateFormat('hh:mm a').format(DateTime.tryParse(log.timestamp) ?? DateTime.now()),
                style: const TextStyle(fontSize: 12, color: Colors.grey),
              ),
            )),
          ],
        );
      },
    );
  }
}