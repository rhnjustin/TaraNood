import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/watch_item.dart';
import '../providers/app_provider.dart';
import '../screens/add_edit_screen.dart';
import 'watch_details_dialog.dart';

class WatchCard extends StatelessWidget {
  final WatchItem item;

  const WatchCard({super.key, required this.item});

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<AppProvider>(context, listen: false);

    final imageUrl = item.posterPath.isNotEmpty
        ? 'https://image.tmdb.org/t/p/w500${item.posterPath}'
        : null;

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: () {
          showDialog(
            context: context,
            builder: (_) => WatchDetailsDialog(item: item),
          );
        },
        child: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Row(
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: imageUrl != null
                    ? Image.network(
                  imageUrl,
                  width: 60,
                  height: 90,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => _buildPlaceholder(),
                )
                    : _buildPlaceholder(),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item.title,
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    Chip(
                      labelPadding: const EdgeInsets.symmetric(horizontal: 4, vertical: -4),
                      label: Text(item.status, style: const TextStyle(fontSize: 10)),
                      backgroundColor: _getStatusColor(item.status),
                    ),
                    Text(
                      '${item.type} • Rating: ${item.rating}/10',
                      style: TextStyle(color: Colors.grey.shade600, fontSize: 12),
                    ),
                  ],
                ),
              ),
              IconButton(
                icon: Icon(
                  item.isFavorite ? Icons.favorite : Icons.favorite_border,
                  color: item.isFavorite ? Colors.red : null,
                ),
                onPressed: () => provider.toggleFavorite(item),
              ),
              PopupMenuButton<String>(
                onSelected: (value) {
                  if (value == 'edit') {
                    Navigator.of(context).push(
                      MaterialPageRoute(builder: (_) => AddEditScreen(item: item)),
                    );
                  } else if (value == 'delete') {
                    provider.deleteItem(item.id);
                  }
                },
                itemBuilder: (context) => [
                  const PopupMenuItem(value: 'edit', child: Text('Edit')),
                  const PopupMenuItem(value: 'delete', child: Text('Delete')),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPlaceholder() {
    return Container(
      width: 60,
      height: 90,
      color: Colors.purple.shade100,
      child: Icon(item.type == 'Movie' ? Icons.movie : Icons.tv, color: Colors.purple),
    );
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case 'Watching':
        return Colors.blue.shade100;
      case 'Completed':
        return Colors.green.shade100;
      case 'Dropped':
        return Colors.red.shade100;
      default:
        return Colors.orange.shade100;
    }
  }
}