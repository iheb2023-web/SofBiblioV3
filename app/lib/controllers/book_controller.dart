





// import 'package:app/imports.dart';
// import 'package:app/models/book.dart';

// class BookController extends GetxController {
//   final RxList<Book> allBooks = <Book>[].obs;
//   final RxList<Book> books = <Book>[].obs;
//   final RxList<Book> userBooks = <Book>[].obs;
//   final RxList<Book> popularBooks = <Book>[].obs;
//   final RxList<Book> recommendedBooks = <Book>[].obs;
//   final RxList<Book> newBooks = <Book>[].obs;
//   final RxBool isLoading = false.obs;
//   final RxString error = ''.obs;

//   final AuthController _authController = Get.find<AuthController>();
//   final StorageService _storageService = Get.find<StorageService>();
//   late int userId = 0; // Initialiser userId à 0



//   @override
//   void onInit() {
//     super.onInit();
//     // Initialisation correcte de la variable de classe
//     final userSession = _storageService.getUserSession();
//     userId = int.tryParse(userSession?['id']?.toString() ?? '') ?? 0; // Assignation à la variable de classe
//     loadAllBooks();
//     loadRecommendations();
    
//     if (_authController.currentUser.value != null) {
//       loadUserBooks();
//     }

//     ever(_authController.currentUser, (user) {
//       if (user != null) {
//         // Mise à jour correcte de la variable de classe
//         final newSession = _storageService.getUserSession();
//         userId = int.tryParse(newSession?['id']?.toString() ?? '') ?? 0;
        
//         loadUserBooks();
//         loadRecommendations();
//       } else {
//         userId = 0;
//         userBooks.clear();
//         recommendedBooks.clear();
//       }
//     });
//   }
//   Future<void> loadAllBooks() async {
//     final currentUserEmail = _authController.currentUser.value?.email;
//     if (currentUserEmail == null) return;

//     try {
//       isLoading.value = true;
//       error.value = '';
//       final loadedBooks = await BookService.getAllBooksWithinEmailOwner(
//         currentUserEmail,
//       );

//       WidgetsBinding.instance.addPostFrameCallback((_) {
//         allBooks.value = loadedBooks;
//         books.value = loadedBooks;
//         _updateSortedLists();
//       });
//     } catch (e) {
//       error.value = 'Error loading books: $e';
//     } finally {
//       isLoading.value = false;
//     }
//   }

//   // Nouvelle méthode pour charger les recommandations personnalisées
//   Future<void> loadRecommendations() async {
//     try {
//       if (_authController.currentUser.value == null) {
//         recommendedBooks.clear();
//         return;
//       }

//       isLoading.value = true;
//       error.value = '';
//       final recommendations = await BookService.getPersonalizedRecommendations(userId);
//       recommendedBooks.value = recommendations;

//       // Si vous voulez quand même garder un fallback avec les meilleures notes
//       // quand il n'y a pas de recommandations personnalisées
//       if (recommendedBooks.isEmpty && allBooks.isNotEmpty) {
//         recommendedBooks.value = List.from(allBooks)
//           ..sort((a, b) => (b.rating ?? 0).compareTo(a.rating ?? 0))
//           ..take(10).toList();
//       }
//     } catch (e) {
//       error.value = 'Error loading recommendations: $e';
//       // Fallback aux livres les mieux notés en cas d'erreur
//       if (allBooks.isNotEmpty) {
//         recommendedBooks.value = List.from(allBooks)
//           ..sort((a, b) => (b.rating ?? 0).compareTo(a.rating ?? 0))
//           ..take(10).toList();
//       }
//     } finally {
//       isLoading.value = false;
//     }
//   }

//   void _updateSortedLists() {
//     // Livres populaires (les plus empruntés)
//     popularBooks.value = List.from(allBooks)
//       ..sort((a, b) => (b.borrowCount ?? 0).compareTo(a.borrowCount ?? 0));

//     if (popularBooks.length > 10) {
//       popularBooks.value = popularBooks.sublist(0, 10);
//     }

//     // Nouveautés (les plus récentes)
//     newBooks.value = List.from(allBooks)..sort((a, b) {
//       final dateA = a.publishedDate ?? '';
//       final dateB = b.publishedDate ?? '';
//       return dateB.compareTo(dateA);
//     });

//     if (newBooks.length > 10) {
//       newBooks.value = newBooks.sublist(0, 10);
//     }

//     // On ne met plus à jour recommendedBooks ici car il est géré par loadRecommendations()
//   }

//   Future<void> loadUserBooks() async {
//     try {
//       final user = _authController.currentUser.value;
//       if (user?.id == null) return;

//       isLoading.value = true;
//       error.value = '';

//       final loadedBooks = await BookService.getBooksByUser(user!.id!);
//       userBooks.value = loadedBooks;
//     } catch (e) {
//       error.value = 'Error loading user books: $e';
//     } finally {
//       isLoading.value = false;
//     }
//   }

//   Future<Book?> updateBook(int bookId, Map<String, dynamic> bookData) async {
//     try {
//       isLoading.value = true;
//       error.value = '';

//       final updatedBook = await BookService.updateBook(bookId, bookData);

//       if (updatedBook != null) {
//         _updateBookInLists(updatedBook);
//         return updatedBook;
//       }
//       error.value = 'Échec de la mise à jour du livre';
//       return null;
//     } catch (e) {
//       error.value = 'Erreur lors de la mise à jour du livre: $e';
//       return null;
//     } finally {
//       isLoading.value = false;
//     }
//   }

//   void _updateBookInLists(Book updatedBook) {
//     final lists = [
//       allBooks,
//       books,
//       userBooks,
//       popularBooks,
//       newBooks,
//       // On ne met pas recommendedBooks car ils viennent d'une source différente
//     ];

//     for (var list in lists) {
//       final index = list.indexWhere((book) => book.id == updatedBook.id);
//       if (index != -1) {
//         list[index] = updatedBook;
//       }
//     }

//     _updateSortedLists();
//   }
// }


import 'package:app/imports.dart';
import 'package:app/models/book.dart';

class BookController extends GetxController {
  final RxList<Book> allBooks = <Book>[].obs;
  final RxList<Book> books = <Book>[].obs;
  final RxList<Book> userBooks = <Book>[].obs;
  final RxList<Book> popularBooks = <Book>[].obs;
  final RxList<Book> recommendedBooks = <Book>[].obs;
  final RxList<Book> newBooks = <Book>[].obs;
  final RxList<Book> filteredBooks = <Book>[].obs;
  
  final RxString currentSearchQuery = ''.obs;
  final Rx<String?> currentCategory = Rx<String?>(null);
  
  final RxBool isLoading = false.obs;
  final RxString error = ''.obs;

  final AuthController _authController = Get.find<AuthController>();
  final StorageService _storageService = Get.find<StorageService>();
  late int userId = 0;

  @override
  void onInit() {
    super.onInit();
    final userSession = _storageService.getUserSession();
    userId = int.tryParse(userSession?['id']?.toString() ?? '') ?? 0;
    loadAllBooks();
    loadRecommendations();
    
    if (_authController.currentUser.value != null) {
      loadUserBooks();
    }

    ever(_authController.currentUser, (user) {
      if (user != null) {
        final newSession = _storageService.getUserSession();
        userId = int.tryParse(newSession?['id']?.toString() ?? '') ?? 0;
        loadUserBooks();
        loadRecommendations();
      } else {
        userId = 0;
        userBooks.clear();
        recommendedBooks.clear();
      }
    });
  }

  Future<void> loadAllBooks() async {
    final currentUserEmail = _authController.currentUser.value?.email;
    if (currentUserEmail == null) return;

    try {
      isLoading.value = true;
      error.value = '';
      final loadedBooks = await BookService.getAllBooksWithinEmailOwner(currentUserEmail);

      allBooks.value = loadedBooks;
      books.value = loadedBooks;
      filteredBooks.value = loadedBooks;
      _updateSortedLists();
    } catch (e) {
      error.value = 'Error loading books: $e';
    } finally {
      isLoading.value = false;
    }
  }

  Future<void> loadRecommendations() async {
    try {
      if (_authController.currentUser.value == null) {
        recommendedBooks.clear();
        return;
      }

      isLoading.value = true;
      error.value = '';
      final recommendations = await BookService.getPersonalizedRecommendations(userId);
      recommendedBooks.value = recommendations;

      if (recommendedBooks.isEmpty && allBooks.isNotEmpty) {
        recommendedBooks.value = List.from(allBooks)
          ..sort((a, b) => (b.rating ?? 0).compareTo(a.rating ?? 0))
          ..take(10).toList();
      }
    } catch (e) {
      error.value = 'Error loading recommendations: $e';
      if (allBooks.isNotEmpty) {
        recommendedBooks.value = List.from(allBooks)
          ..sort((a, b) => (b.rating ?? 0).compareTo(a.rating ?? 0))
          ..take(10).toList();
      }
    } finally {
      isLoading.value = false;
    }
  }

void filterBooks({String? query, String? category}) {
  currentSearchQuery.value = query ?? currentSearchQuery.value;
  currentCategory.value = category; // Pas besoin de ?? ici car on veut pouvoir passer null

  List<Book> filtered = allBooks;

  // Filtre de recherche
  if (currentSearchQuery.value.isNotEmpty) {
    filtered = filtered.where((book) =>
        book.title.toLowerCase().contains(currentSearchQuery.value.toLowerCase()) ||
        book.author.toLowerCase().contains(currentSearchQuery.value.toLowerCase()) ||
        (book.description?.toLowerCase().contains(currentSearchQuery.value.toLowerCase()) ?? false) ||
        (book.category?.toLowerCase().contains(currentSearchQuery.value.toLowerCase()) ?? false)
    ).toList();
  }

  // Filtre de catégorie (seulement si category n'est pas null)
  if (currentCategory.value != null) {
    filtered = filtered.where((book) =>
        book.category?.toLowerCase() == currentCategory.value?.toLowerCase()
    ).toList();
  }

  filteredBooks.value = filtered;
  _updateSortedLists(filtered);
}

  void _updateSortedLists([List<Book>? booksToSort]) {
    final sourceList = booksToSort ?? allBooks;

    // Livres populaires (les plus empruntés)
    popularBooks.value = List.from(sourceList)
      ..sort((a, b) => (b.borrowCount ?? 0).compareTo(a.borrowCount ?? 0));

    if (popularBooks.length > 10) {
      popularBooks.value = popularBooks.sublist(0, 10);
    }

    // Nouveautés (les plus récentes)
    newBooks.value = List.from(sourceList)..sort((a, b) {
      final dateA = a.publishedDate ?? '';
      final dateB = b.publishedDate ?? '';
      return dateB.compareTo(dateA);
    });

    if (newBooks.length > 10) {
      newBooks.value = newBooks.sublist(0, 10);
    }
  }

  Future<void> loadUserBooks() async {
    try {
      final user = _authController.currentUser.value;
      if (user?.id == null) return;

      isLoading.value = true;
      error.value = '';

      final loadedBooks = await BookService.getBooksByUser(user!.id!);
      userBooks.value = loadedBooks;
    } catch (e) {
      error.value = 'Error loading user books: $e';
    } finally {
      isLoading.value = false;
    }
  }

  Future<Book?> updateBook(int bookId, Map<String, dynamic> bookData) async {
    try {
      isLoading.value = true;
      error.value = '';

      final updatedBook = await BookService.updateBook(bookId, bookData);

      if (updatedBook != null) {
        _updateBookInLists(updatedBook);
        return updatedBook;
      }
      error.value = 'Échec de la mise à jour du livre';
      return null;
    } catch (e) {
      error.value = 'Erreur lors de la mise à jour du livre: $e';
      return null;
    } finally {
      isLoading.value = false;
    }
  }

  void _updateBookInLists(Book updatedBook) {
    final lists = [
      allBooks,
      books,
      userBooks,
      popularBooks,
      newBooks,
      filteredBooks,
    ];

    for (var list in lists) {
      final index = list.indexWhere((book) => book.id == updatedBook.id);
      if (index != -1) {
        list[index] = updatedBook;
      }
    }

    _updateSortedLists();
  }

  void resetFilters() {
    currentSearchQuery.value = '';
    currentCategory.value = null;
    filteredBooks.value = List.from(allBooks);
    _updateSortedLists();
  }
}