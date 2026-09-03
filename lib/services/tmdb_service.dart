import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../models/tmdb_models.dart';

class TmdbService {
  final Dio _dio = Dio(BaseOptions(
    baseUrl: 'https://api.themoviedb.org/3',
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));

  // TANDAAN: Siguraduhin na ang API Key na ito ay valid at active.
  static const String _apiKey = '7302a4ed77541fda75035aab9fcde816';

  Future<List<TmdbSearchResult>> searchMedia(String query) async {
    if (query.trim().isEmpty) return [];
    
    try {
      debugPrint('Searching for: $query');
      final response = await _dio.get('/search/multi', queryParameters: {
        'api_key': _apiKey,
        'query': query,
        'include_adult': false,
        'language': 'en-US',
        'page': 1,
      });
      
      final List results = response.data['results'] ?? [];
      final List<TmdbSearchResult> searchResults = [];
      
      for (var item in results) {
        try {
          searchResults.add(TmdbSearchResult.fromJson(item));
        } catch (e) {
          debugPrint('Error parsing item: $e');
        }
      }
      
      debugPrint('Found ${searchResults.length} results');
      return searchResults;
    } on DioException catch (e) {
      debugPrint('TMDB API Error: ${e.response?.statusCode} - ${e.message}');
      // Kung 401, posibleng mali ang API Key
      rethrow; 
    } catch (e) {
      debugPrint('TMDB General Error: $e');
      rethrow;
    }
  }
}
