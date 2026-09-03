import 'package:flutter/material.dart';
import 'home_fragment.dart';
import 'history_fragment.dart';
import 'favorites_fragment.dart';
import 'profile_fragment.dart';
import 'about_fragment.dart';
import '../widgets/quick_add_dialog.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  int _currentIndex = 0;

  final List<String> _titles = const [
    'TaraNood',
    'History',
    'Favorites',
    'Profile',
    'About',
  ];

  Widget _buildBody() {
    switch (_currentIndex) {
      case 0:
        return const HomeFragment();
      case 1:
        return const HistoryFragment();
      case 2:
        return const FavoritesFragment();
      case 3:
        return const ProfileFragment();
      case 4:
        return const AboutFragment();
      default:
        return const HomeFragment();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_titles[_currentIndex]),
        backgroundColor: const Color(0xFF0D1B2A),
      ),
      // Gamit tayo ng SafeArea para maiwasan ang overflow sa notches/navigation bars
      body: SafeArea(child: _buildBody()),
      floatingActionButton: _currentIndex == 0
          ? FloatingActionButton(
              backgroundColor: Colors.blue.shade600,
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (context) => const QuickAddDialog(),
                );
              },
              child: const Icon(Icons.add, color: Colors.white),
            )
          : null,
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        selectedItemColor: Colors.blueAccent,
        unselectedItemColor: Colors.grey,
        backgroundColor: const Color(0xFF0D1B2A),
        type: BottomNavigationBarType.fixed,
        onTap: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.list), label: 'Watchlist'),
          BottomNavigationBarItem(icon: Icon(Icons.history), label: 'History'),
          BottomNavigationBarItem(icon: Icon(Icons.favorite), label: 'Favorites'),
          BottomNavigationBarItem(icon: Icon(Icons.person), label: 'Profile'),
          BottomNavigationBarItem(icon: Icon(Icons.info_outline), label: 'About'),
        ],
      ),
    );
  }
}
