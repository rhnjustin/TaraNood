import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';
import '../widgets/watch_card.dart';

class HomeFragment extends StatefulWidget {
  const HomeFragment({super.key});

  @override
  State<HomeFragment> createState() => _HomeFragmentState();
}

class _HomeFragmentState extends State<HomeFragment> {
  String _selectedType = 'All';
  final List<String> _types = ['All', 'Movie', 'TV Show', 'Anime', 'Other'];

  @override
  Widget build(BuildContext context) {
    return Consumer<AppProvider>(
      builder: (context, provider, child) {
        final watchlistItems = provider.watchItems
            .where((item) => item.status.trim().toLowerCase() != 'completed')
            .toList()
            .reversed
            .toList();

        final items = _selectedType == 'All'
            ? watchlistItems
            : watchlistItems.where((i) => i.type == _selectedType).toList();

        return Column(
          children: [
            const SizedBox(height: 10),
            // Horizontal Chips
            SizedBox(
              height: 50, // Nilakihan nang konti para sa safety
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 12),
                itemCount: _types.length,
                itemBuilder: (context, index) {
                  final type = _types[index];
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: ChoiceChip(
                      label: Text(type),
                      selected: _selectedType == type,
                      onSelected: (val) {
                        if (val) setState(() => _selectedType = type);
                      },
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 10),
            // Watchlist
            Expanded(
              child: items.isEmpty
                  ? const Center(child: Text('Walang laman ang iyong watchlist.'))
                  : ListView.builder(
                      key: const PageStorageKey('watchlist_list'), // Dagdag key
                      itemCount: items.length,
                      padding: const EdgeInsets.only(bottom: 80), // Space para sa FAB
                      itemBuilder: (context, index) => WatchCard(
                        key: ValueKey(items[index].id),
                        item: items[index],
                      ),
                    ),
            ),
          ],
        );
      },
    );
  }
}
