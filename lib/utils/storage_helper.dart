import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/watch_item.dart';

class StorageHelper {
  static const String _key = 'watch_items_key';

  // I-save ang listahan sa SharedPreferences
  static Future<void> saveItems(List<WatchItem> items) async {
    final prefs = await SharedPreferences.getInstance();
    final String encodedData = jsonEncode(
      items.map((item) => item.toJson()).toList(),
    );
    await prefs.setString(_key, encodedData);
  }

  // Kukunin ang listahan mula sa SharedPreferences
  static Future<List<WatchItem>> getItems() async {
    final prefs = await SharedPreferences.getInstance();
    final String? encodedData = prefs.getString(_key);

    if (encodedData == null || encodedData.isEmpty) {
      return [];
    }

    final List<dynamic> decodedData = jsonDecode(encodedData);
    return decodedData.map((item) => WatchItem.fromJson(item)).toList();
  }
}