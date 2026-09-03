import 'package:flutter/material.dart';
import '../models/watch_item.dart';

class WatchDetailsDialog extends StatelessWidget {
  final WatchItem item;

  const WatchDetailsDialog({super.key, required this.item});

  @override
  Widget build(BuildContext context) {
    final imageUrl = item.posterPath.isNotEmpty
        ? 'https://image.tmdb.org/t/p/w500${item.posterPath}'
        : null;

    return AlertDialog(
      title: Text(item.title),
      content: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            if (imageUrl != null)
              Center(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: Image.network(imageUrl, height: 180, fit: BoxFit.cover),
                ),
              ),
            const SizedBox(height: 12),
            Text('Type: ${item.type}', style: const TextStyle(fontWeight: FontWeight.bold)),
            Text('Status: ${item.status}'),
            Text('Rating: ${item.rating} / 10'),
            if (item.type == 'TV Show')
              Text('Episodes: ${item.episodesWatched} / ${item.totalEpisodes}'),
            const SizedBox(height: 8),
            if (item.note.isNotEmpty) ...[
              const Text('Notes:', style: TextStyle(fontWeight: FontWeight.bold)),
              Text(item.note, style: const TextStyle(fontStyle: FontStyle.italic)),
            ],
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Close'),
        ),
      ],
    );
  }
}