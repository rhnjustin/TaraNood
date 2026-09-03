class WatchItem {
  String id;
  String title;
  String type; // "Movie", "TV Show", "Anime", "Other"
  String status; // "Plan to Watch", "Watching", "Completed", "Dropped"
  double rating;
  int episodesWatched;
  int totalEpisodes;
  int runtimeMinutes;
  String note;
  bool isFavorite;
  String posterPath;

  WatchItem({
    required this.id,
    required this.title,
    required this.type,
    required this.status,
    this.rating = 0.0,
    this.episodesWatched = 0,
    this.totalEpisodes = 0,
    this.runtimeMinutes = 0,
    this.note = '',
    this.isFavorite = false,
    this.posterPath = '',
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'title': title,
    'type': type,
    'status': status,
    'rating': rating,
    'episodesWatched': episodesWatched,
    'totalEpisodes': totalEpisodes,
    'runtimeMinutes': runtimeMinutes,
    'note': note,
    'isFavorite': isFavorite,
    'posterPath': posterPath,
  };

  factory WatchItem.fromJson(Map<String, dynamic> json) => WatchItem(
    id: json['id'] ?? '',
    title: json['title'] ?? '',
    type: json['type'] ?? json['category'] ?? 'Movie',
    status: json['status'] ?? 'Plan to Watch',
    rating: (json['rating'] ?? 0.0).toDouble(),
    episodesWatched: json['episodesWatched'] ?? 0,
    totalEpisodes: json['totalEpisodes'] ?? 0,
    runtimeMinutes: json['runtimeMinutes'] ?? 0,
    note: json['note'] ?? '',
    isFavorite: json['isFavorite'] ?? false,
    posterPath: json['posterPath'] ?? '',
  );

  WatchItem copyWith({
    String? id,
    String? title,
    String? type,
    String? status,
    double? rating,
    int? episodesWatched,
    int? totalEpisodes,
    int? runtimeMinutes,
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
      runtimeMinutes: runtimeMinutes ?? this.runtimeMinutes,
      note: note ?? this.note,
      isFavorite: isFavorite ?? this.isFavorite,
      posterPath: posterPath ?? this.posterPath,
    );
  }
}
