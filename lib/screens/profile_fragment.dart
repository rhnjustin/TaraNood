import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';

class ProfileFragment extends StatelessWidget {
  const ProfileFragment({super.key});

  void _showEditProfileDialog(BuildContext context) {
    final provider = Provider.of<AppProvider>(context, listen: false);
    final nameController = TextEditingController(text: provider.userName);
    final ageController = TextEditingController(text: provider.userAge.toString());

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Edit Profile'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameController,
              decoration: const InputDecoration(labelText: 'Name'),
            ),
            TextField(
              controller: ageController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(labelText: 'Age'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('CANCEL'),
          ),
          ElevatedButton(
            onPressed: () {
              final age = int.tryParse(ageController.text) ?? 0;
              provider.updateProfile(nameController.text, age);
              Navigator.pop(context);
            },
            child: const Text('SAVE'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<AppProvider>(context);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          // Profile Image Avatar Placeholder
          Container(
            width: 100,
            height: 100,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(color: Colors.grey.shade400, width: 2),
            ),
            child: Icon(Icons.image, size: 50, color: Colors.grey.shade400),
          ),
          const SizedBox(height: 12),
          Text(
            provider.userName,
            style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
          ),
          Text('Age: ${provider.userAge}', style: const TextStyle(color: Colors.grey)),
          const SizedBox(height: 12),
          OutlinedButton(
            onPressed: () => _showEditProfileDialog(context),
            child: const Text('EDIT PROFILE'),
          ),
          const SizedBox(height: 24),

          // Watch Statistics Grid
          const Align(
            alignment: Alignment.centerLeft,
            child: Text(
              'Watch Statistics',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ),
          const SizedBox(height: 12),
          GridView.count(
            crossAxisCount: 2,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            childAspectRatio: 2.2,
            crossAxisSpacing: 12,
            mainAxisSpacing: 12,
            children: [
              _buildStatCard(provider.totalMoviesSaved.toString(), 'TOTAL MOVIES SAVED'),
              _buildStatCard(provider.totalSeriesSaved.toString(), 'TOTAL SERIES SAVED'),
              _buildStatCard(provider.totalAnimeSaved.toString(), 'TOTAL ANIME SAVED'),
              _buildStatCard(provider.totalOtherSaved.toString(), 'TOTAL OTHER SAVED'),
              _buildStatCard(provider.totalCompleted.toString(), 'TOTAL COMPLETED'),
              _buildStatCard(provider.totalFavorites.toString(), 'TOTAL FAVORITES'),
              _buildStatCard(provider.totalWatchTimeFormatted, 'TOTAL WATCH TIME'),
              _buildStatCard(provider.episodesWatched.toString(), 'EPISODES WATCHED'),
            ],
          ),
          const SizedBox(height: 24),

          // Activity Log Section
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Activity Log',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
              TextButton(
                onPressed: () {},
                child: const Text('See All'),
              )
            ],
          ),
          const SizedBox(height: 8),
          provider.logs.isEmpty
              ? const Padding(
            padding: EdgeInsets.symmetric(vertical: 16.0),
            child: Text('No recent activity', style: TextStyle(color: Colors.grey)),
          )
              : ListView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: provider.logs.length > 5 ? 5 : provider.logs.length,
            itemBuilder: (context, index) {
              final log = provider.logs[index];
              return ListTile(
                title: Text(log.title),
                subtitle: Text(log.action),
              );
            },
          )
        ],
      ),
    );
  }

  Widget _buildStatCard(String value, String label) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFF152238),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            value,
            style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.lightBlueAccent),
          ),
          const SizedBox(height: 2),
          Text(
            label,
            style: const TextStyle(fontSize: 9, color: Colors.grey, fontWeight: FontWeight.bold),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}