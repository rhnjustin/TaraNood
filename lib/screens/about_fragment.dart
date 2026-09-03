import 'package:flutter/material.dart';

class AboutFragment extends StatelessWidget {
  const AboutFragment({super.key});

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          Container(
            width: 80,
            height: 80,
            decoration: BoxDecoration(
              color: Colors.blue.shade900,
              shape: BoxShape.circle,
            ),
            child: const Icon(Icons.play_circle_fill, size: 50, color: Colors.orange),
          ),
          const SizedBox(height: 12),
          const Text(
            'TaraNood v1.0',
            style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 4),
          const Text(
            'Your personal offline movie and series tracker.',
            style: TextStyle(color: Colors.grey, fontSize: 13),
          ),
          const SizedBox(height: 16),
          Card(
            color: const Color(0xFF152238),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  Text(
                    'TaraNood helps users organize movies, anime, and TV series they want to watch — all in one simple and clean app.',
                    style: TextStyle(fontSize: 13),
                  ),
                  SizedBox(height: 16),
                  Center(
                    child: Text(
                      'KEY FEATURES',
                      style: TextStyle(color: Colors.blueAccent, fontWeight: FontWeight.bold, fontSize: 12),
                    ),
                  ),
                  SizedBox(height: 8),
                  Text('• Track Movies, Series, and Anime'),
                  Text('• Manage Favorites for Quick Access'),
                  Text('• Monitor Watch Progress'),
                  Text('• Detailed Watch Statistics'),
                  Text('• Data Backup and Restore'),
                  Text('• Adaptive Dark Mode Support'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            color: const Color(0xFF152238),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: const [
                  Text(
                    'DEVELOPER',
                    style: TextStyle(color: Colors.grey, fontSize: 11, fontWeight: FontWeight.bold),
                  ),
                  SizedBox(height: 6),
                  Text(
                    'Rohn Justin Gamboa',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  Text(
                    'BSIT - SD',
                    style: TextStyle(color: Colors.grey, fontSize: 13),
                  ),
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}