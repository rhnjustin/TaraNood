class LogEntry {
  String id;
  String title;
  String action;
  String timestamp;

  LogEntry({
    required this.id,
    required this.title,
    required this.action,
    required this.timestamp,
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'title': title,
    'action': action,
    'timestamp': timestamp,
  };

  factory LogEntry.fromJson(Map<String, dynamic> json) => LogEntry(
    id: json['id'] ?? '',
    title: json['title'] ?? '',
    action: json['action'] ?? '',
    timestamp: json['timestamp'] ?? '',
  );
}