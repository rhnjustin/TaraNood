import 'package:shared_preferences/shared_preferences.dart';
import '../models/watch_item.dart';

class StorageHelper {
  final SharedPreferences prefs;

  StorageHelper({required this.prefs});

  Future<void> saveItems(List<WatchItem> items) async {
    // Nagse-save ng items sa SharedPreferences
    final List<String> stringList = items.map((item) => item.toJson()).toList();
    await prefs.setStringList('watch_items', stringList);
  }

  List<WatchItem> getItems() {
    final List<String>? stringList = prefs.getStringList('watch_items');
    if (stringList == null) return [];
    return stringList.map((item) => WatchItem.fromJson(item)).toList();
  }
}