import 'package:flutter/material.dart';
import '../models/watch_item.dart';
import '../utils/storage_helper.dart';

class AppProvider extends ChangeNotifier {
  final StorageHelper storageHelper;

  AppProvider(this.storageHelper);

  List<WatchItem> _items = [];
  bool _isDarkMode = false;
  String _userName = 'User';
  int _userAge = 0;
  List<String> _logs = [];

  // Getters
  List<WatchItem> get items => _items;
  bool get isDarkMode => _isDarkMode;
  String get userName => _userName;
  int get userAge => _userAge;
  List<String> get logs => _logs;

  List<WatchItem> get watchlist =>
      _items.where((item) => item.status != 'Completed').toList();

  List<WatchItem> get historyList =>
      _items.where((item) => item.status == 'Completed').toList();

  List<WatchItem> get favorites =>
      _items.where((item) => item.isFavorite).toList();

  // Statistics Getters
  int get totalMoviesSaved =>
      _items.where((i) => i.type.toLowerCase() == 'movie').length;

  int get totalSeriesSaved =>
      _items.where((i) => i.type.toLowerCase() == 'series').length;

  int get totalAnimeSaved =>
      _items.where((i) => i.type.toLowerCase() == 'anime').length;

  int get totalOtherSaved => _items
      .where((i) => !['movie', 'series', 'anime'].contains(i.type.toLowerCase()))
      .length;

  int get totalCompleted => historyList.length;

  int get totalFavorites => favorites.length;

  int get episodesWatched =>
      _items.fold(0, (sum, item) => sum + item.episodesWatched);

  String get totalWatchTimeFormatted {
    int totalMinutes =
    _items.fold(0, (sum, item) => sum + item.watchTimeMinutes);
    int hours = totalMinutes ~/ 60;
    int minutes = totalMinutes % 60;
    return '${hours}h ${minutes}m';
  }

  // Actions
  void toggleTheme() {
    _isDarkMode = !_isDarkMode;
    notifyListeners();
  }

  void updateProfile(String name, int age) {
    _userName = name;
    _userAge = age;
    _addLog('Updated profile info');
    notifyListeners();
  }

  Future<void> addItem(WatchItem item) async {
    _items.add(item);
    _addLog('Added "${item.title}"');
    await storageHelper.saveItems(_items);
    notifyListeners();
  }

  Future<void> updateItem(WatchItem updatedItem) async {
    final index = _items.indexWhere((item) => item.id == updatedItem.id);
    if (index != -1) {
      _items[index] = updatedItem;
      _addLog('Updated "${updatedItem.title}"');
      await storageHelper.saveItems(_items);
      notifyListeners();
    }
  }

  Future<void> toggleFavorite(WatchItem item) async {
    final index = _items.indexWhere((i) => i.id == item.id);
    if (index != -1) {
      _items[index].isFavorite = !_items[index].isFavorite;
      _addLog(_items[index].isFavorite
          ? 'Added "${item.title}" to favorites'
          : 'Removed "${item.title}" from favorites');
      await storageHelper.saveItems(_items);
      notifyListeners();
    }
  }

  Future<void> updateStatus(String id, String newStatus) async {
    final index = _items.indexWhere((item) => item.id == id);
    if (index != -1) {
      _items[index].status = newStatus;
      _addLog('Changed status of "${_items[index].title}" to $newStatus');
      await storageHelper.saveItems(_items);
      notifyListeners();
    }
  }

  Future<void> deleteItem(String id) async {
    final index = _items.indexWhere((item) => item.id == id);
    if (index != -1) {
      String title = _items[index].title;
      _items.removeAt(index);
      _addLog('Deleted "$title"');
      await storageHelper.saveItems(_items);
      notifyListeners();
    }
  }

  void _addLog(String action) {
    _logs.insert(0, '${DateTime.now().toString().substring(0, 16)} - $action');
  }
}