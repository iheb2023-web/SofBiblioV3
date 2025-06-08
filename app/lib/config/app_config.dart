class AppConfig {
  static const String apiBaseUrl = 'http://192.168.43.241:8080';
  static const String googleBooksUrl = 'https://www.googleapis.com/books/v1/volumes';
  static const String chatUrl = 'http://192.168.43.241:5000/chat';
    static const String recommandationIa = 'http://192.168.43.241:6000/recommendations';

  static const String SocketUrlEmulator = 'http://10.0.2.2:9092';
  static const String SocketwsUrlPhysical = 'http://192.168.43.241:9092';
}

// class AppConfig {
//   static const String apiBaseUrl = 'https://backendsofbiblio.onrender.com';
//   static const String googleBooksUrl =
//       'https://www.googleapis.com/books/v1/volumes';
//   static const String chatUrl = 'https://backendsofbiblio.onrender.com/chat';

//   static const String SocketUrlEmulator = 'https://backendsofbiblio.onrender.com'; // si tu utilises WebSocket sur HTTPS
//   static const String SocketwsUrlPhysical = 'https://backendsofbiblio.onrender.com'; // idem ici
// }
