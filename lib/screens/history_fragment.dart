import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';

class HistoryFragment extends StatelessWidget {
  const HistoryFragment({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("History")),
      body: Consumer<AppProvider>(
        builder: (context, provider, child) {
          final historyList = provider.historyList;

          if (historyList.isEmpty) {
            return const Center(
              child: Text("Walang nakatagong History."),
            );
          }

          return Column(
            children: [
              // Naka-wrap sa Expanded para maiwasan ang layout crash
              Expanded(
                child: ListView.builder(
                  itemCount: historyList.length,
                  itemBuilder: (context, index) {
                    final item = historyList[index];
                    return ListTile(
                      title: Text(item.title),
                      subtitle: const Text("Status: Completed"),
                      trailing: IconButton(
                        icon: const Icon(Icons.delete, color: Colors.red),
                        onPressed: () {
                          provider.deleteItem(item.id);
                        },
                      ),
                    );
                  },
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}