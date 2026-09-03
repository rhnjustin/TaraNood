import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'providers/app_provider.dart';
import 'screens/splash_screen.dart';
import 'utils/storage_helper.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final prefs = await SharedPreferences.getInstance();
  final storageHelper = StorageHelper(prefs);

  runApp(
    ChangeNotifierProvider(
      create: (_) => AppProvider(storageHelper),
      child: const TaraNoodApp(),
    ),
  );
}

class TaraNoodApp extends StatelessWidget {
  const TaraNoodApp({super.key});

  @override
  Widget build(BuildContext context) {
    final isDarkMode = context.select<AppProvider, bool>((p) => p.isDarkMode);

    return MaterialApp(
      title: 'TaraNood',
      debugShowCheckedModeBanner: false,
      themeMode: isDarkMode ? ThemeMode.dark : ThemeMode.light,
      darkTheme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0B131E),
        colorSchemeSeed: Colors.blue,
      ),
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.light,
        colorSchemeSeed: Colors.blue,
      ),
      home: const SplashScreen(),
    );
  }
}