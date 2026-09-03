import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/watch_item.dart';
import '../providers/app_provider.dart';

class AddEditScreen extends StatefulWidget {
  final WatchItem? item;
  final String? initialTitle;
  final String? initialType;
  final String? initialPosterPath;

  const AddEditScreen({
    super.key,
    this.item,
    this.initialTitle,
    this.initialType,
    this.initialPosterPath,
  });

  @override
  State<AddEditScreen> createState() => _AddEditScreenState();
}

class _AddEditScreenState extends State<AddEditScreen> {
  final _formKey = GlobalKey<FormState>();

  late String _title;
  late String _type;
  late String _status;
  late double _rating;
  late int _episodesWatched;
  late int _totalEpisodes;
  late String _note;
  late bool _isFavorite;
  late String _posterPath;

  @override
  void initState() {
    super.initState();
    final item = widget.item;
    _title = item?.title ?? widget.initialTitle ?? '';
    _type = item?.type ?? widget.initialType ?? 'Movie';
    _status = item?.status ?? 'Plan to Watch';
    _rating = item?.rating ?? 0.0;
    _episodesWatched = item?.episodesWatched ?? 0;
    _totalEpisodes = item?.totalEpisodes ?? 0;
    _note = item?.note ?? '';
    _isFavorite = item?.isFavorite ?? false;
    _posterPath = item?.posterPath ?? widget.initialPosterPath ?? '';
  }

  void _saveForm() {
    if (!_formKey.currentState!.validate()) return;
    _formKey.currentState!.save();

    final provider = Provider.of<AppProvider>(context, listen: false);

    if (widget.item == null) {
      final newItem = WatchItem(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        title: _title,
        type: _type,
        status: _status,
        rating: _rating,
        episodesWatched: _episodesWatched,
        totalEpisodes: _totalEpisodes,
        note: _note,
        isFavorite: _isFavorite,
        posterPath: _posterPath,
      );
      provider.addItem(newItem);
    } else {
      final updatedItem = widget.item!.copyWith(
        title: _title,
        type: _type,
        status: _status,
        rating: _rating,
        episodesWatched: _episodesWatched,
        totalEpisodes: _totalEpisodes,
        note: _note,
        isFavorite: _isFavorite,
        posterPath: _posterPath,
      );
      provider.updateItem(updatedItem);
    }

    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.item == null ? 'Add Watch Item' : 'Edit Watch Item'),
        actions: [
          IconButton(
            icon: const Icon(Icons.check),
            onPressed: _saveForm,
          )
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              TextFormField(
                initialValue: _title,
                decoration: const InputDecoration(labelText: 'Title', border: OutlineInputBorder()),
                validator: (val) => val == null || val.trim().isEmpty ? 'Enter a title' : null,
                onSaved: (val) => _title = val!.trim(),
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                value: _type,
                items: ['Movie', 'TV Show']
                    .map((t) => DropdownMenuItem(value: t, child: Text(t)))
                    .toList(),
                onChanged: (val) => setState(() => _type = val!),
                decoration: const InputDecoration(labelText: 'Type', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                value: _status,
                items: ['Plan to Watch', 'Watching', 'Completed', 'Dropped']
                    .map((s) => DropdownMenuItem(value: s, child: Text(s)))
                    .toList(),
                onChanged: (val) => setState(() => _status = val!),
                decoration: const InputDecoration(labelText: 'Status', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Text('Rating: ${_rating.toStringAsFixed(1)} / 10'),
                  Expanded(
                    child: Slider(
                      value: _rating,
                      min: 0,
                      max: 10,
                      divisions: 20,
                      label: _rating.toString(),
                      onChanged: (val) => setState(() => _rating = val),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              if (_type == 'TV Show') ...[
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        initialValue: _episodesWatched.toString(),
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(labelText: 'Episodes Watched', border: OutlineInputBorder()),
                        onSaved: (val) => _episodesWatched = int.tryParse(val ?? '0') ?? 0,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: TextFormField(
                        initialValue: _totalEpisodes.toString(),
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(labelText: 'Total Episodes', border: OutlineInputBorder()),
                        onSaved: (val) => _totalEpisodes = int.tryParse(val ?? '0') ?? 0,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
              ],
              TextFormField(
                initialValue: _note,
                maxLines: 3,
                decoration: const InputDecoration(labelText: 'Notes / Review', border: OutlineInputBorder()),
                onSaved: (val) => _note = val ?? '',
              ),
              const SizedBox(height: 16),
              SwitchListTile(
                title: const Text('Add to Favorites'),
                value: _isFavorite,
                onChanged: (val) => setState(() => _isFavorite = val),
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  onPressed: _saveForm,
                  child: const Text('Save Item'),
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}