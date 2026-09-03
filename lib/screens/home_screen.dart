import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';
import '../widgets/quick_add_dialog.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<AppProvider>(context);
    final items = provider.watchItems;

    return Scaffold(
      appBar: AppBar(
        title: const Text('TaraNood'),
        actions: [
          IconButton(
            icon: Icon(provider.isDarkMode ? Icons.light_mode : Icons.dark_mode),
            onPressed: () => provider.toggleDarkMode(!provider.isDarkMode),
          )
        ],
      ),
      body: items.isEmpty
          ? const Center(child: Text('Walang laman ang iyong watchlist.'))
          : ListView.builder(
        itemCount: items.length,
        itemBuilder: (context, index) {
          final item = items[index];
          return ListTile(
            leading: CircleAvatar(
              child: Icon(item.type == 'Movie' ? Icons.movie : Icons.tv),
            ),
            title: Text(item.title),
            subtitle: Text('${item.type} • ${item.status}'),
            trailing: IconButton(
              icon: Icon(
                item.isFavorite ? Icons.favorite : Icons.favorite_border,
                color: item.isFavorite ? Colors.red : null,
              ),
              onPressed: () => provider.toggleFavorite(item),
            ),
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          showDialog(
            context: context,
            builder: (context) => const QuickAddDialog(),
          );
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}