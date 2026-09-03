import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/watch_item.dart';
import '../models/tmdb_models.dart';
import '../providers/app_provider.dart';
import '../services/tmdb_service.dart';
import '../screens/add_edit_screen.dart';

class QuickAddDialog extends StatefulWidget {
  const QuickAddDialog({super.key});

  @override
  State<QuickAddDialog> createState() => _QuickAddDialogState();
}

class _QuickAddDialogState extends State<QuickAddDialog> {
  final _titleController = TextEditingController();
  final _tmdbService = TmdbService();
  Timer? _debounce;

  String _selectedType = 'Movie';
  String _selectedStatus = 'Plan to Watch';
  List<TmdbSearchResult> _searchResults = [];
  bool _isSearching = false;
  String _errorMessage = '';
  String _posterPath = '';

  @override
  void dispose() {
    _debounce?.cancel();
    _titleController.dispose();
    super.dispose();
  }

  void _onSearchChanged(String query) {
    if (_debounce?.isActive ?? false) _debounce?.cancel();
    
    if (query.trim().isEmpty) {
      setState(() {
        _searchResults = [];
        _isSearching = false;
        _errorMessage = '';
      });
      return;
    }

    _debounce = Timer(const Duration(milliseconds: 600), () async {
      if (!mounted) return;
      
      setState(() {
        _isSearching = true;
        _errorMessage = '';
      });
      
      try {
        final results = await _tmdbService.searchMedia(query);
        if (mounted) {
          setState(() {
            _searchResults = results
                .where((r) => r.mediaType == 'movie' || r.mediaType == 'tv')
                .toList();
            _isSearching = false;
            if (_searchResults.isEmpty) {
              _errorMessage = 'Walang nahanap na resulta.';
            }
          });
        }
      } catch (e) {
        if (mounted) {
          setState(() {
            _isSearching = false;
            _errorMessage = 'May error sa pag-search.';
          });
        }
      }
    });
  }

  void _selectSearchResult(TmdbSearchResult result) {
    setState(() {
      _titleController.text = result.title;
      _selectedType = result.mediaType == 'tv' ? 'TV Show' : 'Movie';
      _posterPath = result.posterPath ?? '';
      _searchResults = [];
      _errorMessage = '';
    });
  }

  bool _isSaving = false;

  Future<void> _quickSave() async {
    final title = _titleController.text.trim();
    if (title.isEmpty || _isSaving) return;

    setState(() => _isSaving = true);

    final newItem = WatchItem(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      title: title,
      type: _selectedType,
      status: _selectedStatus,
      posterPath: _posterPath,
    );

    try {
      await Provider.of<AppProvider>(context, listen: false).addItem(newItem);
      if (mounted) {
        Navigator.of(context).pop();
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isSaving = false);
      }
    }
  }

  void _openFullAddScreen() {
    Navigator.of(context).pop();
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => AddEditScreen(
          initialTitle: _titleController.text.trim(),
          initialType: _selectedType,
          initialPosterPath: _posterPath,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Quick Add Item'),
      content: Container(
        width: 350, // Fixed width helps with layout stability
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: _titleController,
              decoration: InputDecoration(
                labelText: 'Title',
                hintText: 'Search movie/show...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _isSearching 
                  ? const Padding(
                      padding: EdgeInsets.all(12.0),
                      child: SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      ),
                    )
                  : null,
              ),
              onChanged: _onSearchChanged,
            ),
            const SizedBox(height: 8),
            if (_errorMessage.isNotEmpty)
              Text(_errorMessage, style: const TextStyle(color: Colors.red, fontSize: 12)),
            if (_searchResults.isNotEmpty)
              Container(
                constraints: const BoxConstraints(maxHeight: 200),
                decoration: BoxDecoration(
                  border: Border.all(color: Colors.grey.shade300),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: ListView.builder(
                  shrinkWrap: true,
                  physics: const ClampingScrollPhysics(),
                  itemCount: _searchResults.length,
                  itemBuilder: (context, index) {
                    final res = _searchResults[index];
                    return ListTile(
                      dense: true,
                      leading: res.posterPath != null
                          ? Image.network(
                              'https://image.tmdb.org/t/p/w92${res.posterPath}',
                              width: 30,
                              errorBuilder: (_, __, ___) => const Icon(Icons.movie),
                            )
                          : const Icon(Icons.movie),
                      title: Text(res.title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                      subtitle: Text('${res.mediaType.toUpperCase()} • ${res.releaseDate?.split('-')[0] ?? 'N/A'}', style: const TextStyle(fontSize: 11)),
                      onTap: () => _selectSearchResult(res),
                    );
                  },
                ),
              ),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              value: _selectedType,
              items: ['Movie', 'TV Show', 'Anime', 'Other']
                  .map((t) => DropdownMenuItem(value: t, child: Text(t)))
                  .toList(),
              onChanged: (val) => setState(() => _selectedType = val!),
              decoration: const InputDecoration(labelText: 'Type', border: OutlineInputBorder(), contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8)),
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              value: _selectedStatus,
              items: ['Plan to Watch', 'Watching', 'Completed', 'Dropped']
                  .map((s) => DropdownMenuItem(value: s, child: Text(s)))
                  .toList(),
              onChanged: (val) => setState(() => _selectedStatus = val!),
              decoration: const InputDecoration(labelText: 'Status', border: OutlineInputBorder(), contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8)),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: _openFullAddScreen,
          child: const Text('More Details'),
        ),
        ElevatedButton(
          onPressed: _quickSave,
          child: const Text('Save'),
        ),
      ],
    );
  }
}
