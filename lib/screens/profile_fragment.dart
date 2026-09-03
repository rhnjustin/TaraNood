import 'package:flutter/material.dart';

class LogItem {
  final String title;
  final String action;

  LogItem({required this.title, required this.action});
}

class ProfileFragment extends StatelessWidget {
  final List<LogItem> logs;

  const ProfileFragment({super.key, this.logs = const []});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
      ),
      body: logs.isEmpty
          ? const Center(child: Text('No activity logs.'))
          : ListView.builder(
        itemCount: logs.length,
        itemBuilder: (context, index) {
          final log = logs[index];
          return ListTile(
            title: Text(log.title),
            subtitle: Text(log.action),
          );
        },
      ),
    );
  }
}