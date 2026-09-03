import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';

class HomeFragment extends StatelessWidget {
  const HomeFragment({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Watchlist")),
      body: Consumer<AppProvider>(
        builder: (context, provider, child) {
          final watchlist = provider.watchlist;

          if (watchlist.isEmpty) {
            return const Center(
              child: Text("Walang laman ang iyong Watchlist."),
            );
          }

          return Column(
            children: [
              // Ginagamitan ng Expanded para maiwasan ang Bottom Overflowed at Assertion Error
              Expanded(
                child: ListView.builder(
                  itemCount: watchlist.length,
                  itemBuilder: (context, index) {
                    final item = watchlist[index];
                    return ListTile(
                      title: Text(item.title),
                      subtitle: Text(item.status),
                      trailing: IconButton(
                        icon: const Icon(Icons.check_circle_outline),
                        onPressed: () {
                          // Lilipat ito sa History dahil magiging 'Completed' ang status
                          provider.updateStatus(item.id, 'Completed');
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