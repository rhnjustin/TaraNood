class TmdbSearchResult {
  final int id;
  final String title;
  final String mediaType;
  final String? posterPath;
  final String? releaseDate;
  final String overview;

  TmdbSearchResult({
    required this.id,
    required this.title,
    required this.mediaType,
    this.posterPath,
    this.releaseDate,
    required this.overview,
  });

  factory TmdbSearchResult.fromJson(Map<String, dynamic> json) {
    return TmdbSearchResult(
      id: json['id'] ?? 0,
      title: json['title'] ?? json['name'] ?? 'Untitled',
      mediaType: json['media_type'] ?? 'movie',
      posterPath: json['poster_path'],
      releaseDate: json['release_date'] ?? json['first_air_date'],
      overview: json['overview'] ?? '',
    );
  }
}