import 'package:flutter/material.dart';
import '../models/watch_item.dart';
import '../models/log_entry.dart';
import '../utils/storage_helper.dart';

class AppProvider with ChangeNotifier {
  final StorageHelper storageHelper;

  List<WatchItem> _watchItems = [];
  List<LogEntry> _logs = [];
  bool _isDarkMode = true;
  String _userName = 'User';
  int _userAge = 0;

  AppProvider(this.storageHelper) {
    _loadData();
  }

  List<WatchItem> get watchItems => _watchItems;
  List<LogEntry> get logs => _logs;
  bool get isDarkMode => _isDarkMode;
  String get userName => _userName;
  int get userAge => _userAge;

  List<WatchItem> get favorites => _watchItems.where((i) => i.isFavorite).toList();
  List<WatchItem> get history => _watchItems.where((i) => i.status == 'Completed').toList();

  // Statistics
  int get totalMoviesSaved => _watchItems.where((i) => i.type == 'Movie').length;
  int get totalSeriesSaved => _watchItems.where((i) => i.type == 'TV Show').length;
  int get totalAnimeSaved => _watchItems.where((i) => i.type == 'Anime').length;
  int get totalOtherSaved => _watchItems.where((i) => i.type == 'Other').length;
  int get totalCompleted => history.length;
  int get totalFavorites => favorites.length;

  int get episodesWatched => _watchItems.fold(0, (sum, item) => sum + item.episodesWatched);

  String get totalWatchTimeFormatted {
    int totalMinutes = _watchItems.fold(0, (sum, item) => sum + (item.runtimeMinutes * (item.episodesWatched > 0 ? item.episodesWatched : 1)));
    int hours = totalMinutes ~/ 60;
    int mins = totalMinutes % 60;
    return '${hours}h ${mins}m';
  }

  void _loadData() {
    _watchItems = storageHelper.getWatchItems();
    _logs = storageHelper.getLogs();
    _isDarkMode = storageHelper.isDarkMode();
    _userName = storageHelper.getUserName();
    _userAge = storageHelper.getUserAge();
    notifyListeners();
  }

  Future<void> toggleDarkMode(bool value) async {
    _isDarkMode = value;
    await storageHelper.setDarkMode(value);
    notifyListeners();
  }

  Future<void> updateProfile(String name, int age) async {
    _userName = name;
    _userAge = age;
    await storageHelper.setUserName(name);
    await storageHelper.setUserAge(age);
    notifyListeners();
  }

  Future<void> addItem(WatchItem item) async {
    await storageHelper.addWatchItem(item);
    _refresh();
  }

  Future<void> updateItem(WatchItem item) async {
    await storageHelper.updateWatchItem(item);
    _refresh();
  }

  Future<void> deleteItem(String id) async {
    await storageHelper.deleteWatchItem(id);
    _refresh();
  }

  Future<void> toggleFavorite(WatchItem item) async {
    final updated = item.copyWith(isFavorite: !item.isFavorite);
    await storageHelper.updateWatchItem(updated);
    _refresh();
  }

  Future<void> clearLogs() async {
    await storageHelper.clearLogs();
    _refresh();
  }

  void _refresh() {
    _watchItems = storageHelper.getWatchItems();
    _logs = storageHelper.getLogs();
    notifyListeners();
  }
}
