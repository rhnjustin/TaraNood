import 'dart:io';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:image_picker/image_picker.dart';
import 'dart:convert';
import 'package:http/http.dart' as http;

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (_) => AppState(),
      child: const TaraNoodApp(),
    ),
  );
}

// -----------------------------------------------------------------------------
// MODELS & STATE MANAGEMENT
// -----------------------------------------------------------------------------

enum ItemType { All, Movie, Series, Anime, Other }
enum WatchStatus { Planned, Watching, Completed }

class WatchItem {
  final String id;
  String title;
  ItemType type;
  WatchStatus status;
  int totalRuntimeMinutes;
  int watchedRuntimeMinutes;
  String description;
  String watchUrl;
  String? imagePath;
  bool isFavorite;
  DateTime createdAt;

  WatchItem({
    required this.id,
    required this.title,
    required this.type,
    this.status = WatchStatus.Planned,
    this.totalRuntimeMinutes = 0,
    this.watchedRuntimeMinutes = 0,
    this.description = '',
    this.watchUrl = '',
    this.imagePath,
    this.isFavorite = false,
    DateTime? createdAt,
  }) : createdAt = createdAt ?? DateTime.now();

  double get progressPercentage {
    if (totalRuntimeMinutes == 0) return 0.0;
    return (watchedRuntimeMinutes / totalRuntimeMinutes).clamp(0.0, 1.0);
  }
}

class ActivityLogItem {
  final String id;
  final String title;
  final String subtitle;
  final IconData icon;
  final Color iconColor;
  final DateTime timestamp;

  ActivityLogItem({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.iconColor,
    DateTime? timestamp,
  }) : timestamp = timestamp ?? DateTime.now();
}

class AppState extends ChangeNotifier {
  bool isDarkMode = true;
  String username = "Rohn Justin Gamboa";
  int userAge = 20;
  String? profileImagePath;

  final List<WatchItem> _items = [];
  final List<ActivityLogItem> _logs = [];

  ItemType selectedFilter = ItemType.All;
  String searchQuery = '';
  String sortOption = 'Recently Added';

  List<WatchItem> get items => List.unmodifiable(_items);
  List<ActivityLogItem> get logs => List.unmodifiable(_logs);

  void toggleDarkMode(bool value) {
    isDarkMode = value;
    notifyListeners();
  }

  void updateProfile(String name, int age, String? image) {
    username = name;
    userAge = age;
    if (image != null) profileImagePath = image;
    notifyListeners();
  }

  void addWatchItem(WatchItem item) {
    _items.add(item);
    addLog(
      title: 'Added "${item.title}" to watchlist',
      subtitle: 'Type: ${item.type.name}',
      icon: Icons.add_circle,
      iconColor: Colors.blue,
    );
    notifyListeners();
  }

  void updateWatchItem(WatchItem updated) {
    final index = _items.indexWhere((i) => i.id == updated.id);
    if (index != -1) {
      final old = _items[index];
      if (old.status != updated.status) {
        addLog(
          title: 'Status changed for "${updated.title}"',
          subtitle: '${old.status.name} -> ${updated.status.name}',
          icon: Icons.build,
          iconColor: Colors.lightBlue,
        );
      }
      if (old.watchedRuntimeMinutes != updated.watchedRuntimeMinutes) {
        addLog(
          title: 'Updated progress of "${updated.title}"',
          subtitle: 'Minutes: ${old.watchedRuntimeMinutes} -> ${updated.watchedRuntimeMinutes}',
          icon: Icons.refresh,
          iconColor: Colors.lightBlue,
        );
      }
      if (updated.status == WatchStatus.Completed && old.status != WatchStatus.Completed) {
        addLog(
          title: 'Marked "${updated.title}" as Completed',
          subtitle: '',
          icon: Icons.star,
          iconColor: Colors.blue,
        );
      }
      _items[index] = updated;
      notifyListeners();
    }
  }

  void toggleFavorite(WatchItem item) {
    item.isFavorite = !item.isFavorite;
    if (item.isFavorite) {
      addLog(
        title: 'Added "${item.title}" to favorites',
        subtitle: '',
        icon: Icons.add_circle,
        iconColor: Colors.blue,
      );
    }
    notifyListeners();
  }

  void addLog({
    required String title,
    required String subtitle,
    required IconData icon,
    required Color iconColor,
  }) {
    _logs.insert(
      0,
      ActivityLogItem(
        id: DateTime.now().toString(),
        title: title,
        subtitle: subtitle,
        icon: icon,
        iconColor: iconColor,
      ),
    );
    notifyListeners();
  }

  void deleteLogs(Set<String> ids) {
    _logs.removeWhere((log) => ids.contains(log.id));
    notifyListeners();
  }

  void clearAllLogs() {
    _logs.clear();
    notifyListeners();
  }

  void clearAllData() {
    _items.clear();
    _logs.clear();
    username = "User";
    userAge = 0;
    profileImagePath = null;
    notifyListeners();
  }

  // Statistics
  int get totalSaved => _items.length;
  int get totalMoviesSaved => _items.where((i) => i.type == ItemType.Movie).length;
  int get totalSeriesSaved => _items.where((i) => i.type == ItemType.Series).length;
  int get totalAnimeSaved => _items.where((i) => i.type == ItemType.Anime).length;
  int get totalOtherSaved => _items.where((i) => i.type == ItemType.Other).length;
  int get totalCompleted => _items.where((i) => i.status == WatchStatus.Completed).length;
  int get totalFavorites => _items.where((i) => i.isFavorite).length;

  int get totalWatchTimeMinutes => _items.fold(0, (sum, i) => sum + i.watchedRuntimeMinutes);

  List<WatchItem> get filteredWatchlist {
    return _items.where((item) {
      if (item.status == WatchStatus.Completed) return false;
      if (selectedFilter != ItemType.All && item.type != selectedFilter) return false;
      if (searchQuery.isNotEmpty && !item.title.toLowerCase().contains(searchQuery.toLowerCase())) {
        return false;
      }
      return true;
    }).toList();
  }

  List<WatchItem> get historyList {
    return _items.where((i) => i.status == WatchStatus.Completed).toList();
  }

  List<WatchItem> get favoritesList {
    return _items.where((i) => i.isFavorite).toList();
  }
}

// -----------------------------------------------------------------------------
// MAIN APP & THEMING
// -----------------------------------------------------------------------------

class TaraNoodApp extends StatelessWidget {
  const TaraNoodApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return MaterialApp(
      title: 'TaraNood',
      debugShowCheckedModeBanner: false,
      themeMode: state.isDarkMode ? ThemeMode.dark : ThemeMode.light,
      theme: ThemeData(
        brightness: Brightness.light,
        scaffoldBackgroundColor: const Color(0xFFEBF3FA),
        primarySwatch: Colors.blue,
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFFEBF3FA),
          elevation: 0,
          iconTheme: IconThemeData(color: Colors.black),
          titleTextStyle: TextStyle(color: Colors.black, fontSize: 22, fontWeight: FontWeight.bold),
        ),
        bottomNavigationBarTheme: const BottomNavigationBarThemeData(
          backgroundColor: Colors.white,
          selectedItemColor: Colors.blue,
          unselectedItemColor: Colors.grey,
        ),
      ),
      darkTheme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0F172A),
        cardColor: const Color(0xFF1E293B),
        dialogBackgroundColor: const Color(0xFF1E293B),
        bottomSheetTheme: const BottomSheetThemeData(backgroundColor: Color(0xFF1E293B)),
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF0F172A),
          elevation: 0,
          titleTextStyle: TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.bold),
        ),
        bottomNavigationBarTheme: const BottomNavigationBarThemeData(
          backgroundColor: Color(0xFF0F172A),
          selectedItemColor: Colors.blue,
          unselectedItemColor: Colors.grey,
        ),
      ),
      home: const MainNavigationScreen(),
    );
  }
}

// -----------------------------------------------------------------------------
// NAVIGATION & SCREENS CONTAINER
// -----------------------------------------------------------------------------

class MainNavigationScreen extends StatefulWidget {
  const MainNavigationScreen({Key? key}) : super(key: key);

  @override
  State<MainNavigationScreen> createState() => _MainNavigationScreenState();
}

class _MainNavigationScreenState extends State<MainNavigationScreen> {
  int _currentIndex = 0;

  final List<Widget> _screens = const [
    WatchlistTab(),
    HistoryTab(),
    FavoritesTab(),
    ProfileTab(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: _screens,
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        type: BottomNavigationBarType.fixed,
        onTap: (index) => setState(() => _currentIndex = index),
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home_max_outlined), activeIcon: Icon(Icons.home_max), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.access_time), label: 'History'),
          BottomNavigationBarItem(icon: Icon(Icons.star_border), activeIcon: Icon(Icons.star), label: 'Favorites'),
          BottomNavigationBarItem(icon: Icon(Icons.person_outline), activeIcon: Icon(Icons.person), label: 'Profile'),
        ],
      ),
    );
  }
}

// -----------------------------------------------------------------------------
// TAB 1: WATCHLIST
// -----------------------------------------------------------------------------

class WatchlistTab extends StatefulWidget {
  const WatchlistTab({Key? key}) : super(key: key);

  @override
  State<WatchlistTab> createState() => _WatchlistTabState();
}

class _WatchlistTabState extends State<WatchlistTab> {
  bool isSearching = false;

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();
    final items = state.filteredWatchlist;

    return Scaffold(
      appBar: AppBar(
        title: isSearching
            ? TextField(
          autofocus: true,
          decoration: const InputDecoration(
            hintText: 'Search your watch list...',
            border: InputBorder.none,
          ),
          onChanged: (val) => state.searchQuery = val,
        )
            : const Text('Watchlist'),
        actions: [
          IconButton(
            icon: Icon(isSearching ? Icons.close : Icons.search),
            onPressed: () {
              setState(() {
                isSearching = !isSearching;
                if (!isSearching) state.searchQuery = '';
              });
            },
          ),
          PopupMenuButton<String>(
            icon: const Icon(Icons.filter_list),
            onSelected: (val) => state.sortOption = val,
            itemBuilder: (context) => [
              'Recently Added',
              'Recently Watched',
              'Recently Updated',
              'Alphabetic',
              'Progress',
            ].map((e) => PopupMenuItem(value: e, child: Text(e))).toList(),
          ),
        ],
      ),
      body: Column(
        children: [
          // Filter Chips
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Row(
              children: ItemType.values.map((type) {
                final isSelected = state.selectedFilter == type;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: FilterChip(
                    label: Text(type.name),
                    selected: isSelected,
                    selectedColor: Colors.blue,
                    onSelected: (_) => setState(() => state.selectedFilter = type),
                  ),
                );
              }).toList(),
            ),
          ),
          Expanded(
            child: items.isEmpty
                ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: const [
                  Text('No Watchlist Yet', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  SizedBox(height: 8),
                  Text('Start adding movies or series you want to watch.\nTap the + button to begin.',
                      textAlign: TextAlign.center, style: TextStyle(color: Colors.grey)),
                ],
              ),
            )
                : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: items.length,
              itemBuilder: (context, index) => ItemCard(item: items[index]),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: () => _showAddOptions(context),
      ),
    );
  }

  void _showAddOptions(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (ctx) => Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ListTile(
            leading: const Icon(Icons.edit),
            title: const Text('Manual Add'),
            onTap: () {
              Navigator.pop(ctx);
              _showEditBottomSheet(context, null);
            },
          ),
          ListTile(
            leading: const Icon(Icons.flash_on),
            title: const Text('Quick Add'),
            onTap: () {
              Navigator.pop(ctx);
              _showQuickAddSheet(context);
            },
          ),
        ],
      ),
    );
  }
}

// -----------------------------------------------------------------------------
// QUICK ADD & MANUAL EDIT SHEETS
// -----------------------------------------------------------------------------

void _showQuickAddSheet(BuildContext context) {
  final controller = TextEditingController();
  final state = context.read<AppState>();

  // IPALIT DITO ANG IYONG TMDB API KEY (v3)
  const String apiKey = '7302a4ed77541fda75035aab9fcde816';

  List<dynamic> searchResults = [];
  bool isLoading = false;

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    builder: (ctx) => StatefulBuilder(
      builder: (context, setModalState) {

        Future<void> searchTMDB(String query) async {
          if (query.trim().isEmpty) {
            setModalState(() {
              searchResults = [];
              isLoading = false;
            });
            return;
          }

          setModalState(() => isLoading = true);

          final url = Uri.parse(
            'https://api.themoviedb.org/3/search/multi?api_key=$apiKey&query=${Uri.encodeComponent(query)}&include_adult=false',
          );

          try {
            final response = await http.get(url);
            if (response.statusCode == 200) {
              final data = json.decode(response.body);
              setModalState(() {
                searchResults = data['results'] ?? [];
                isLoading = false;
              });
            } else {
              setModalState(() => isLoading = false);
            }
          } catch (e) {
            setModalState(() => isLoading = false);
          }
        }

        return Padding(
          padding: EdgeInsets.only(
            bottom: MediaQuery.of(ctx).viewInsets.bottom,
            left: 16,
            right: 16,
            top: 16,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Quick Add', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 12),
              TextField(
                controller: controller,
                decoration: const InputDecoration(
                  hintText: 'Search movie, series, or anime',
                  prefixIcon: Icon(Icons.search),
                  border: OutlineInputBorder(),
                ),
                onChanged: (val) {
                  searchTMDB(val);
                },
              ),
              const SizedBox(height: 16),
              if (isLoading)
                const Center(child: CircularProgressIndicator())
              else if (searchResults.isEmpty && controller.text.isNotEmpty)
                const Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Center(child: Text('No results found.')),
                )
              else
                SizedBox(
                  height: 300,
                  child: ListView.builder(
                    shrinkWrap: true,
                    itemCount: searchResults.length,
                    itemBuilder: (context, index) {
                      final item = searchResults[index];

                      final String title = item['title'] ?? item['name'] ?? 'Unknown Title';
                      final String mediaTypeRaw = item['media_type'] ?? 'movie';
                      final String posterPath = item['poster_path'] ?? '';
                      final String overview = item['overview'] ?? '';
                      final String releaseDate = item['release_date'] ?? item['first_air_date'] ?? '';
                      final String year = releaseDate.isNotEmpty ? releaseDate.split('-')[0] : '';

                      // Convert TMDB media_type to App ItemType
                      ItemType type = ItemType.Movie;
                      if (mediaTypeRaw == 'tv') {
                        type = ItemType.Series;
                      }

                      final imageUrl = posterPath.isNotEmpty
                          ? 'https://image.tmdb.org/t/p/w185$posterPath'
                          : null;

                      return ListTile(
                        leading: imageUrl != null
                            ? Image.network(
                          imageUrl,
                          width: 45,
                          fit: BoxFit.cover,
                          errorBuilder: (c, e, s) => const Icon(Icons.movie, size: 40),
                        )
                            : Container(
                          width: 45,
                          color: Colors.grey[800],
                          child: const Icon(Icons.movie),
                        ),
                        title: Text(title),
                        subtitle: Text(
                          '${type.name} ${year.isNotEmpty ? "• $year" : ""}\n$overview',
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                        isThreeLine: true,
                        onTap: () {
                          state.addWatchItem(
                            WatchItem(
                              id: DateTime.now().toString(),
                              title: title,
                              type: type,
                              totalRuntimeMinutes: 120, // Default runtime placeholder
                              description: overview,
                              imagePath: imageUrl,
                            ),
                          );
                          Navigator.pop(ctx);
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text('"$title" added to watchlist!')),
                          );
                        },
                      );
                    },
                  ),
                ),
              const SizedBox(height: 16),
            ],
          ),
        );
      },
    ),
  );
}

void _showEditBottomSheet(BuildContext context, WatchItem? existingItem) {
  final isEdit = existingItem != null;
  final titleController = TextEditingController(text: isEdit ? existingItem.title : '');
  final descController = TextEditingController(text: isEdit ? existingItem.description : '');
  final urlController = TextEditingController(text: isEdit ? existingItem.watchUrl : '');

  ItemType selectedType = isEdit ? existingItem.type : ItemType.Movie;
  WatchStatus selectedStatus = isEdit ? existingItem.status : WatchStatus.Planned;
  int hours = isEdit ? existingItem.totalRuntimeMinutes ~/ 60 : 2;
  int minutes = isEdit ? existingItem.totalRuntimeMinutes % 60 : 8;
  int watchedHours = isEdit ? existingItem.watchedRuntimeMinutes ~/ 60 : 0;
  int watchedMinutes = isEdit ? existingItem.watchedRuntimeMinutes % 60 : 0;

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    builder: (ctx) => StatefulBuilder(
      builder: (context, setModalState) {
        return Padding(
          padding: EdgeInsets.only(bottom: MediaQuery.of(ctx).viewInsets.bottom, left: 16, right: 16, top: 16),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(isEdit ? 'Edit Item' : 'Add New Item', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                const SizedBox(height: 12),
                Center(
                  child: Container(
                    height: 120,
                    width: 90,
                    color: Colors.grey[800],
                    child: const Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.image, size: 40),
                        SizedBox(height: 4),
                        Text('CHOOSE IMAGE', style: TextStyle(fontSize: 10)),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: titleController,
                  decoration: const InputDecoration(labelText: 'Title', border: OutlineInputBorder()),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: DropdownButtonFormField<ItemType>(
                        value: selectedType,
                        decoration: const InputDecoration(labelText: 'TYPE'),
                        items: ItemType.values.where((t) => t != ItemType.All).map((t) {
                          return DropdownMenuItem(value: t, child: Text(t.name));
                        }).toList(),
                        onChanged: (v) => setModalState(() => selectedType = v!),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: DropdownButtonFormField<WatchStatus>(
                        value: selectedStatus,
                        decoration: const InputDecoration(labelText: 'STATUS'),
                        items: WatchStatus.values.map((s) {
                          return DropdownMenuItem(value: s, child: Text(s.name));
                        }).toList(),
                        onChanged: (v) => setModalState(() => selectedStatus = v!),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                Text('TOTAL RUNTIME: ${hours} hr ${minutes} min'),
                Slider(
                  value: (hours * 60 + minutes).toDouble(),
                  max: 300,
                  onChanged: (val) {
                    setModalState(() {
                      hours = val.toInt() ~/ 60;
                      minutes = val.toInt() % 60;
                    });
                  },
                ),
                Text('WATCHED RUNTIME: ${watchedHours} hr ${watchedMinutes} min'),
                Slider(
                  value: (watchedHours * 60 + watchedMinutes).toDouble(),
                  max: (hours * 60 + minutes).toDouble() == 0 ? 1 : (hours * 60 + minutes).toDouble(),
                  onChanged: (val) {
                    setModalState(() {
                      watchedHours = val.toInt() ~/ 60;
                      watchedMinutes = val.toInt() % 60;
                    });
                  },
                ),
                TextField(
                  controller: descController,
                  decoration: const InputDecoration(labelText: 'Notes', border: OutlineInputBorder()),
                  maxLines: 2,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: urlController,
                  decoration: const InputDecoration(
                    labelText: 'Watch Link (optional)',
                    prefixIcon: Icon(Icons.share),
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(padding: const EdgeInsets.all(16)),
                    child: Text(isEdit ? 'SAVE ITEM' : 'SAVE TO WATCHLIST'),
                    onPressed: () {
                      final totalMin = hours * 60 + minutes;
                      final watchedMin = watchedHours * 60 + watchedMinutes;

                      if (isEdit) {
                        existingItem.title = titleController.text;
                        existingItem.type = selectedType;
                        existingItem.status = selectedStatus;
                        existingItem.totalRuntimeMinutes = totalMin;
                        existingItem.watchedRuntimeMinutes = watchedMin;
                        existingItem.description = descController.text;
                        existingItem.watchUrl = urlController.text;
                        context.read<AppState>().updateWatchItem(existingItem);
                      } else {
                        context.read<AppState>().addWatchItem(
                          WatchItem(
                            id: DateTime.now().toString(),
                            title: titleController.text,
                            type: selectedType,
                            status: selectedStatus,
                            totalRuntimeMinutes: totalMin,
                            watchedRuntimeMinutes: watchedMin,
                            description: descController.text,
                            watchUrl: urlController.text,
                          ),
                        );
                      }
                      Navigator.pop(ctx);
                    },
                  ),
                ),
                const SizedBox(height: 16),
              ],
            ),
          ),
        );
      },
    ),
  );
}

// -----------------------------------------------------------------------------
// ITEM CARD & DIALOG
// -----------------------------------------------------------------------------

class ItemCard extends StatelessWidget {
  final WatchItem item;

  const ItemCard({Key? key, required this.item}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            // UPDATE: Lalabas dito ang na-save na poster image
            ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: item.imagePath != null && item.imagePath!.isNotEmpty
                  ? Image.network(
                item.imagePath!,
                width: 60,
                height: 90,
                fit: BoxFit.cover,
                errorBuilder: (c, e, s) => Container(
                  width: 60,
                  height: 90,
                  color: Colors.grey[800],
                  child: const Icon(Icons.movie, size: 30),
                ),
              )
                  : Container(
                width: 60,
                height: 90,
                color: Colors.grey[800],
                child: const Icon(Icons.movie, size: 30),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Expanded(
                        child: Text(
                          item.title,
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      IconButton(
                        icon: Icon(
                          item.isFavorite ? Icons.star : Icons.star_border,
                          color: item.isFavorite ? Colors.amber : Colors.grey,
                        ),
                        onPressed: () => state.toggleFavorite(item),
                      ),
                    ],
                  ),
                  Text('${item.type.name} | ${item.status.name}', style: const TextStyle(color: Colors.grey)),
                  const SizedBox(height: 8),
                  Text('Progress: ${item.watchedRuntimeMinutes}/${item.totalRuntimeMinutes} min'),
                  LinearProgressIndicator(value: item.progressPercentage),
                ],
              ),
            ),
            Column(
              children: [
                IconButton(
                  icon: const Icon(Icons.play_arrow),
                  onPressed: () {
                    if (item.watchUrl.isNotEmpty) {
                      launchUrl(Uri.parse(item.watchUrl));
                    }
                  },
                ),
                IconButton(
                  icon: const Icon(Icons.info_outline),
                  onPressed: () => _showDetailsDialog(context, item),
                ),
              ],
            )
          ],
        ),
      ),
    );
  }

  void _showDetailsDialog(BuildContext context, WatchItem item) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: item.imagePath != null && item.imagePath!.isNotEmpty
                  ? Image.network(
                item.imagePath!,
                height: 180,
                fit: BoxFit.cover,
                errorBuilder: (c, e, s) => Container(
                  height: 150,
                  width: 100,
                  color: Colors.grey[800],
                  child: const Icon(Icons.movie, size: 50),
                ),
              )
                  : Container(
                height: 150,
                width: 100,
                color: Colors.grey[800],
                child: const Icon(Icons.movie, size: 50),
              ),
            ),
            const SizedBox(height: 12),
            Text(item.title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
            Text('${item.type.name} | ${item.status.name}'),
            const SizedBox(height: 8),
            const Text('Progress', style: TextStyle(fontWeight: FontWeight.bold)),
            Text('${item.watchedRuntimeMinutes} / ${item.totalRuntimeMinutes} minutes watched'),
            const SizedBox(height: 8),
            const Text('Description / Notes', style: TextStyle(fontWeight: FontWeight.bold)),
            Text(item.description.isEmpty ? 'No notes added.' : item.description),
          ],
        ),
        actions: [
          TextButton(
            child: const Text('Edit'),
            onPressed: () {
              Navigator.pop(ctx);
              _showEditBottomSheet(context, item);
            },
          ),
          TextButton(
            child: const Text('Close'),
            onPressed: () => Navigator.pop(ctx),
          ),
        ],
      ),
    );
  }
}

  void _showDetailsDialog(BuildContext context, WatchItem item) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                height: 150,
                width: 100,
                color: Colors.grey[800],
                child: const Icon(Icons.movie, size: 50),
              ),
            ),
            const SizedBox(height: 12),
            Text(item.title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
            Text('${item.type.name} | ${item.status.name}'),
            const SizedBox(height: 8),
            const Text('Progress', style: TextStyle(fontWeight: FontWeight.bold)),
            Text('${item.watchedRuntimeMinutes} / ${item.totalRuntimeMinutes} minutes watched'),
            const SizedBox(height: 8),
            const Text('Description / Notes', style: TextStyle(fontWeight: FontWeight.bold)),
            Text(item.description.isEmpty ? 'No notes added.' : item.description),
          ],
        ),
        actions: [
          TextButton(
            child: const Text('Edit'),
            onPressed: () {
              Navigator.pop(ctx);
              _showEditBottomSheet(context, item);
            },
          ),
          TextButton(
            child: const Text('Close'),
            onPressed: () => Navigator.pop(ctx),
          ),
        ],
      ),
    );
  }

// -----------------------------------------------------------------------------
// TAB 2: HISTORY
// -----------------------------------------------------------------------------

class HistoryTab extends StatelessWidget {
  const HistoryTab({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final historyItems = context.watch<AppState>().historyList;

    return Scaffold(
      appBar: AppBar(title: const Text('Watch History')),
      body: historyItems.isEmpty
          ? Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: const [
            Text('No Watch History', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            SizedBox(height: 8),
            Text('Completed movies and series will appear here.', style: TextStyle(color: Colors.grey)),
          ],
        ),
      )
          : ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: historyItems.length,
        itemBuilder: (context, index) => ItemCard(item: historyItems[index]),
      ),
    );
  }
}

// -----------------------------------------------------------------------------
// TAB 3: FAVORITES
// -----------------------------------------------------------------------------

class FavoritesTab extends StatelessWidget {
  const FavoritesTab({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final favItems = context.watch<AppState>().favoritesList;

    return Scaffold(
      appBar: AppBar(title: const Text('My Favorites')),
      body: favItems.isEmpty
          ? Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: const [
            Text('No Favorites Yet', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            SizedBox(height: 8),
            Text('Add movies or series to your favorites for quick access.', style: TextStyle(color: Colors.grey)),
          ],
        ),
      )
          : ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: favItems.length,
        itemBuilder: (context, index) => ItemCard(item: favItems[index]),
      ),
    );
  }
}

// -----------------------------------------------------------------------------
// TAB 4: PROFILE & ACTIVITY LOGS
// -----------------------------------------------------------------------------

class ProfileTab extends StatelessWidget {
  const ProfileTab({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
        actions: [
          IconButton(
            icon: const Icon(Icons.info_outline),
            onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AboutPage())),
          ),
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const SettingsPage())),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            CircleAvatar(
              radius: 40,
              backgroundColor: Colors.grey[700],
              child: const Icon(Icons.person, size: 40, color: Colors.white),
            ),
            const SizedBox(height: 8),
            Text(state.username, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            Text('Age: ${state.userAge}', style: const TextStyle(color: Colors.grey)),
            const SizedBox(height: 8),
            OutlinedButton(
              child: const Text('EDIT PROFILE'),
              onPressed: () => _showEditProfileDialog(context),
            ),
            const SizedBox(height: 16),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text('Watch Statistics', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            ),
            const SizedBox(height: 12),
            GridView.count(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              crossAxisCount: 2,
              childAspectRatio: 2.5,
              children: [
                _buildStatTile('${state.totalMoviesSaved}', 'TOTAL MOVIES SAVED'),
                _buildStatTile('${state.totalSeriesSaved}', 'TOTAL SERIES SAVED'),
                _buildStatTile('${state.totalAnimeSaved}', 'TOTAL ANIME SAVED'),
                _buildStatTile('${state.totalOtherSaved}', 'TOTAL OTHER SAVED'),
                _buildStatTile('${state.totalCompleted}', 'TOTAL COMPLETED'),
                _buildStatTile('${state.totalFavorites}', 'TOTAL FAVORITES'),
                _buildStatTile('${state.totalWatchTimeMinutes ~/ 60}h ${state.totalWatchTimeMinutes % 60}m', 'TOTAL WATCH TIME'),
                _buildStatTile('0', 'EPISODES WATCHED'),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Activity Log', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                TextButton(
                  child: const Text('See All'),
                  onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const ActivityLogPage())),
                ),
              ],
            ),
            state.logs.isEmpty
                ? const Text('No recent activity', style: TextStyle(color: Colors.grey))
                : Column(
              children: state.logs.take(5).map((log) => _buildLogTile(log)).toList(),
            )
          ],
        ),
      ),
    );
  }

  Widget _buildStatTile(String value, String label) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(value, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.blue)),
        Text(label, style: const TextStyle(fontSize: 9, color: Colors.grey)),
      ],
    );
  }

  Widget _buildLogTile(ActivityLogItem log) {
    return ListTile(
      leading: Icon(log.icon, color: log.iconColor),
      title: Text(log.title, style: const TextStyle(fontSize: 13)),
      subtitle: log.subtitle.isNotEmpty ? Text(log.subtitle, style: const TextStyle(fontSize: 11)) : null,
      dense: true,
    );
  }

  void _showEditProfileDialog(BuildContext context) {
    final state = context.read<AppState>();
    final nameController = TextEditingController(text: state.username);
    final ageController = TextEditingController(text: state.userAge.toString());

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Edit Profile'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(controller: nameController, decoration: const InputDecoration(labelText: 'Username')),
            TextField(controller: ageController, decoration: const InputDecoration(labelText: 'Age'), keyboardType: TextInputType.number),
          ],
        ),
        actions: [
          TextButton(child: const Text('Cancel'), onPressed: () => Navigator.pop(ctx)),
          TextButton(
            child: const Text('Save'),
            onPressed: () {
              state.updateProfile(nameController.text, int.tryParse(ageController.text) ?? 0, null);
              Navigator.pop(ctx);
            },
          ),
        ],
      ),
    );
  }
}

// -----------------------------------------------------------------------------
// ABOUT & SETTINGS PAGES
// -----------------------------------------------------------------------------

class AboutPage extends StatelessWidget {
  const AboutPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('About')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            const Icon(Icons.play_circle_fill, size: 80, color: Colors.blue),
            const SizedBox(height: 12),
            const Text('TaraNood v1.0', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            const Text(
              'Your personal offline movie and series tracker.\n\nTaraNood helps users organize movies, anime, and TV series they want to watch — all in one simple and clean app.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey),
            ),
            const Spacer(),
            const Text('DEVELOPER', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: Colors.blue)),
            const SizedBox(height: 4),
            const Text('Rohn Justin Gamboa\nBSIT - SD', textAlign: TextAlign.center, style: TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }
}

class SettingsPage extends StatelessWidget {
  const SettingsPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        children: [
          SwitchListTile(
            title: const Text('Dark Mode'),
            value: state.isDarkMode,
            onChanged: (val) => state.toggleDarkMode(val),
          ),
          const Divider(),
          ListTile(
            title: const Text('EXPORT DATA (BACKUP)'),
            onTap: () {},
          ),
          ListTile(
            title: const Text('IMPORT DATA (RESTORE)'),
            onTap: () {},
          ),
          ListTile(
            title: const Text('CLEAR ALL DATA', style: TextStyle(color: Colors.red)),
            onTap: () => _showClearAllDataDialog(context),
          ),
        ],
      ),
    );
  }

  void _showClearAllDataDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Clear All Data'),
        content: const Text(
          'Are you sure you want to clear all data? This will remove all items, history, and activity logs.',
        ),
        actions: [
          TextButton(
            child: const Text('Cancel'),
            onPressed: () => Navigator.pop(ctx),
          ),
          TextButton(
            child: const Text('Clear', style: TextStyle(color: Colors.red)),
            onPressed: () {
              context.read<AppState>().clearAllData();
              Navigator.pop(ctx);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('All data has been cleared.')),
              );
            },
          ),
        ],
      ),
    );
  }
}

// -----------------------------------------------------------------------------
// FULL ACTIVITY LOG PAGE (WITH DELETE & MULTI-SELECT)
// -----------------------------------------------------------------------------

class ActivityLogPage extends StatefulWidget {
  const ActivityLogPage({Key? key}) : super(key: key);

  @override
  State<ActivityLogPage> createState() => _ActivityLogPageState();
}

class _ActivityLogPageState extends State<ActivityLogPage> {
  final Set<String> selectedIds = {};

  @override
  Widget build(BuildContext context) {
    final state = context.watch<AppState>();

    return Scaffold(
      appBar: AppBar(
        title: Text(selectedIds.isNotEmpty ? '${selectedIds.length} selected' : 'Activity Log'),
        actions: [
          if (selectedIds.isNotEmpty)
            IconButton(
              icon: const Icon(Icons.delete),
              onPressed: () {
                state.deleteLogs(selectedIds);
                setState(() => selectedIds.clear());
              },
            )
          else
            IconButton(
              icon: const Icon(Icons.delete_sweep),
              onPressed: () => state.clearAllLogs(),
            ),
        ],
      ),
      body: state.logs.isEmpty
          ? const Center(child: Text('No activity logs.'))
          : ListView.builder(
        itemCount: state.logs.length,
        itemBuilder: (context, index) {
          final log = state.logs[index];
          final isSelected = selectedIds.contains(log.id);

          return CheckboxListTile(
            value: isSelected,
            onChanged: (val) {
              setState(() {
                if (val == true) {
                  selectedIds.add(log.id);
                } else {
                  selectedIds.remove(log.id);
                }
              });
            },
            secondary: Icon(log.icon, color: log.iconColor),
            title: Text(log.title),
            subtitle: Text(log.subtitle),
          );
        },
      ),
    );
  }
}