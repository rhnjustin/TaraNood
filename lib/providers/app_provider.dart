import 'package:flutter/material.dart';
import '../models/watch_item.dart';
import '../utils/storage_helper.dart';

class AppProvider with ChangeNotifier {
  final StorageHelper storageHelper;
  List<WatchItem> _items = [];
  bool _isDarkMode = false;

  AppProvider({required this.storageHelper}) {
    _items = storageHelper.getItems();
  }

  List<WatchItem> get items => _items;
  bool get isDarkMode => _isDarkMode;

  // Lahat ng nakagawiang watchlist (Lalabas dito ang lahat ng na-add mong palabas)
  List<WatchItem> get watchlist => _items;

  // Mga paborito at natapos na
  List<WatchItem> get favorites =>
      _items.where((item) => item.isFavorite).toList();

  List<WatchItem> get historyList =>
      _items.where((item) => item.status == 'Completed').toList();

  void toggleTheme() {
    _isDarkMode = !_isDarkMode;
    notifyListeners();
  }

  // Pag-add ng bagong item (Agad nitong ire-refresh ang Home/Watchlist)
  Future<void> addItem(WatchItem item) async {
    _items.add(item);
    await storageHelper.saveItems(_items);
    notifyListeners(); // Ito ang nagpapakita agad ng pagbabago sa UI
  }

  Future<void> updateItem(WatchItem item) async {
    final index = _items.indexWhere((element) => element.id == item.id);
    if (index != -1) {
      _items[index] = item;
      await storageHelper.saveItems(_items);
      notifyListeners();
    }
  }

  Future<void> updateStatus(String id, String newStatus) async {
    final index = _items.indexWhere((item) => item.id == id);
    if (index != -1) {
      _items[index] = _items[index].copyWith(status: newStatus);
      await storageHelper.saveItems(_items);
      notifyListeners();
    }
  }

  Future<void> toggleFavorite(WatchItem item) async {
    final index = _items.indexWhere((element) => element.id == item.id);
    if (index != -1) {
      _items[index] = _items[index].copyWith(isFavorite: !_items[index].isFavorite);
      await storageHelper.saveItems(_items);
      notifyListeners();
    }
  }

  Future<void> deleteItem(String id) async {
    _items.removeWhere((item) => item.id == id);
    await storageHelper.saveItems(_items);
    notifyListeners();
  }
}