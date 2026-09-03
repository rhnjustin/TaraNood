import 'dart:convert';

class WatchItem {
  final String id;
  final String title;
  final String type; // 'Movie' or 'TV Show'
  final String status; // 'Plan to Watch', 'Completed', etc.
  final double rating;
  final int episodesWatched;
  final int totalEpisodes;
  final String note;
  final bool isFavorite;
  final String posterPath;

  WatchItem({
    required this.id,
    required this.title,
    this.type = 'Movie',
    this.status = 'Plan to Watch',
    this.rating = 0.0,
    this.episodesWatched = 0,
    this.totalEpisodes = 0,
    this.note = '',
    this.isFavorite = false,
    this.posterPath = '',
  });

  WatchItem copyWith({
    String? id,
    String? title,
    String? type,
    String? status,
    double? rating,
    int? episodesWatched,
    int? totalEpisodes,
    String? note,
    bool? isFavorite,
    String? posterPath,
  }) {
    return WatchItem(
      id: id ?? this.id,
      title: title ?? this.title,
      type: type ?? this.type,
      status: status ?? this.status,
      rating: rating ?? this.rating,
      episodesWatched: episodesWatched ?? this.episodesWatched,
      totalEpisodes: totalEpisodes ?? this.totalEpisodes,
      note: note ?? this.note,
      isFavorite: isFavorite ?? this.isFavorite,
      posterPath: posterPath ?? this.posterPath,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'title': title,
      'type': type,
      'status': status,
      'rating': rating,
      'episodesWatched': episodesWatched,
      'totalEpisodes': totalEpisodes,
      'note': note,
      'isFavorite': isFavorite,
      'posterPath': posterPath,
    };
  }

  factory WatchItem.fromMap(Map<String, dynamic> map) {
    return WatchItem(
      id: map['id'] ?? '',
      title: map['title'] ?? '',
      type: map['type'] ?? 'Movie',
      status: map['status'] ?? 'Plan to Watch',
      rating: (map['rating'] ?? 0.0).toDouble(),
      episodesWatched: map['episodesWatched']?.toInt() ?? 0,
      totalEpisodes: map['totalEpisodes']?.toInt() ?? 0,
      note: map['note'] ?? '',
      isFavorite: map['isFavorite'] ?? false,
      posterPath: map['posterPath'] ?? '',
    );
  }

  String toJson() => json.encode(toMap());

  factory WatchItem.fromJson(String source) => WatchItem.fromMap(json.decode(source));
}