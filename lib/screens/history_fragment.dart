import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';
import '../widgets/watch_card.dart';

class HistoryFragment extends StatelessWidget {
  const HistoryFragment({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppProvider>(
      builder: (context, provider, child) {
        final historyItems = provider.watchItems
            .where((item) => item.status.trim().toLowerCase() == 'completed')
            .toList()
            .reversed
            .toList();

        if (historyItems.isEmpty) {
          return const Center(
            child: Text('Walang natapos na panoorin.'),
          );
        }

        return ListView.builder(
          key: const PageStorageKey('history_list'),
          itemCount: historyItems.length,
          padding: const EdgeInsets.symmetric(vertical: 8),
          itemBuilder: (context, index) => WatchCard(
            key: ValueKey(historyItems[index].id),
            item: historyItems[index],
          ),
        );
      },
    );
  }
}
