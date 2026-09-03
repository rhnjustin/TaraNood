import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/watch_item.dart';
import '../models/log_entry.dart';

class StorageHelper {
  static const String _keyWatchList = 'key_watch_list';
  static const String _keyLogs = 'key_logs';
  static const String _keyDarkMode = 'key_dark_mode';
  static const String _keyUserName = 'key_user_name';
  static const String _keyUserAge = 'key_user_age';

  final SharedPreferences _prefs;

  StorageHelper(this._prefs);

  List<WatchItem> getWatchItems() {
    final String? jsonString = _prefs.getString(_keyWatchList);
    if (jsonString == null || jsonString.isEmpty) return [];
    final List<dynamic> jsonList = jsonDecode(jsonString);
    return jsonList.map((item) => WatchItem.fromJson(item)).toList();
  }

  Future<bool> saveWatchItems(List<WatchItem> items) async {
    final List<Map<String, dynamic>> jsonList = items.map((i) => i.toJson()).toList();
    return await _prefs.setString(_keyWatchList, jsonEncode(jsonList));
  }

  Future<void> addWatchItem(WatchItem item) async {
    final items = getWatchItems();
    items.add(item);
    await saveWatchItems(items);
    await addLog(LogEntry(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      title: item.title,
      action: 'Added to Watchlist',
      timestamp: DateTime.now().toIso8601String(),
    ));
  }

  Future<void> updateWatchItem(WatchItem updatedItem) async {
    final items = getWatchItems();
    final index = items.indexWhere((i) => i.id == updatedItem.id);
    if (index != -1) {
      items[index] = updatedItem;
      await saveWatchItems(items);
      await addLog(LogEntry(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        title: updatedItem.title,
        action: 'Updated details',
        timestamp: DateTime.now().toIso8601String(),
      ));
    }
  }

  Future<void> deleteWatchItem(String id) async {
    final items = getWatchItems();
    final item = items.firstWhere(
          (i) => i.id == id,
      orElse: () => WatchItem(id: '', title: '', type: 'Movie', status: ''),
    );
    items.removeWhere((i) => i.id == id);
    await saveWatchItems(items);
    if (item.id.isNotEmpty) {
      await addLog(LogEntry(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        title: item.title,
        action: 'Deleted from Watchlist',
        timestamp: DateTime.now().toIso8601String(),
      ));
    }
  }

  List<LogEntry> getLogs() {
    final String? jsonString = _prefs.getString(_keyLogs);
    if (jsonString == null || jsonString.isEmpty) return [];
    final List<dynamic> jsonList = jsonDecode(jsonString);
    return jsonList.map((item) => LogEntry.fromJson(item)).toList();
  }

  Future<void> addLog(LogEntry log) async {
    final logs = getLogs();
    logs.insert(0, log);
    await _prefs.setString(_keyLogs, jsonEncode(logs.map((l) => l.toJson()).toList()));
  }

  Future<void> clearLogs() async {
    await _prefs.remove(_keyLogs);
  }

  bool isDarkMode() => _prefs.getBool(_keyDarkMode) ?? true;
  Future<void> setDarkMode(bool value) async => await _prefs.setBool(_keyDarkMode, value);

  String getUserName() => _prefs.getString(_keyUserName) ?? 'User';
  Future<void> setUserName(String name) async => await _prefs.setString(_keyUserName, name);

  int getUserAge() => _prefs.getInt(_keyUserAge) ?? 0;
  Future<void> setUserAge(int age) async => await _prefs.setInt(_keyUserAge, age);
}