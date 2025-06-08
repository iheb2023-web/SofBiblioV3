import 'package:app/views/Accueil/active_members.dart';
import 'package:app/views/Accueil/app_bar.dart';
import 'package:app/views/Accueil/book_section.dart';
import 'package:app/views/Accueil/category_chips.dart';
import 'package:app/views/Accueil/chatbot_button.dart';
import 'package:app/views/Accueil/search_bar.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:app/controllers/book_controller.dart';

// class AccueilPage extends StatefulWidget {
//   const AccueilPage({super.key});

//   @override
//   State<AccueilPage> createState() => _AccueilPageState();
// }

// class _AccueilPageState extends State<AccueilPage>
//     with SingleTickerProviderStateMixin {
//   late BookController _bookController;
//   late AnimationController _animationController;

//   @override
//   void initState() {
//     super.initState();
//     _bookController = Get.find<BookController>();
//     _animationController = AnimationController(
//       duration: const Duration(seconds: 2),
//       vsync: this,
//     )..repeat(reverse: true);

//     // Charge les livres après le premier frame
//     WidgetsBinding.instance.addPostFrameCallback((_) {
//       _bookController.loadAllBooks();
//     });
//   }

//   @override
//   void dispose() {
//     _animationController.dispose();
//     super.dispose();
//   }

//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       body: SafeArea(
//         child: SingleChildScrollView(
//           child: Column(
//             crossAxisAlignment: CrossAxisAlignment.start,
//             children: [
//               const AccueilAppBar(),
//               const SizedBox(height: 16),
//               const AccueilSearchBar(),
//               const SizedBox(height: 20),
//               const CategoryChips(),
//               const SizedBox(height: 20),
//               const ActiveMembersSection(),
//               const SizedBox(height: 20),
//               BookSection(
//                 title: 'popular_books'.tr,
//                 books: _bookController.popularBooks,
//                 isHorizontal: true,
//               ),
//               BookSection(
//                 title: 'recommended'.tr,
//                 books: _bookController.recommendedBooks,
//                 isHorizontal: true,
//               ),
//               BookSection(
//                 title: 'new_arrivals'.tr,
//                 books: _bookController.newBooks,
//                 isHorizontal: false,
//               ),
//               const SizedBox(height: 20),
//             ],
//           ),
//         ),
//       ),

//       floatingActionButton: ChatbotButton(
//         animationController: _animationController,
//       ),
//     );
//   }
// }

// class AccueilPage extends StatefulWidget {
//   const AccueilPage({super.key});

//   @override
//   State<AccueilPage> createState() => _AccueilPageState();
// }

// class _AccueilPageState extends State<AccueilPage> 
//     with SingleTickerProviderStateMixin {
//   late BookController _bookController;
//   late AnimationController _animationController;

//   @override
//   void initState() {
//     super.initState();
//     _bookController = Get.find<BookController>();
//     _animationController = AnimationController(
//       duration: const Duration(seconds: 2),
//       vsync: this,
//     )..repeat(reverse: true);
//   }

//   @override
//   void dispose() {
//     _animationController.dispose();
//     super.dispose();
//   }

//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       body: SafeArea(
//         child: Column(
//           children: [
//             // Section recherche et filtres
//             const AccueilSearchBar(),
//             const CategoryChips(),
            
//             // Affichage conditionnel
//             Obx(() {
//               if (_bookController.isLoading.value) {
//                 return const Expanded(
//                   child: Center(child: CircularProgressIndicator()),
//                 );
//               }

//               if (_bookController.error.value.isNotEmpty) {
//                 return Expanded(
//                   child: Center(child: Text(_bookController.error.value)),
//                 );
//               }

//               return _buildContent();
//             }),
//           ],
//         ),
//       ),
//       floatingActionButton: ChatbotButton(
//         animationController: _animationController,
//       ),
//     );
//   }

//   Widget _buildContent() {
//     return Expanded(
//       child: SingleChildScrollView(
//         child: Column(
//           crossAxisAlignment: CrossAxisAlignment.start,
//           children: [
            
//             // Section membres actifs
//             const ActiveMembersSection(),
//             const SizedBox(height: 20),
            
//             // Sections de livres avec filtrage
//             Obx(() {
//               final showFilteredResults = 
//                   _bookController.currentSearchQuery.value.isNotEmpty || 
//                   _bookController.currentCategory.value != null;
              
//               return Column(
//                 children: [
//                   if (showFilteredResults) ...[
//                     _buildFilterHeader(),
//                     BookSection(
//                       title: 'search_results'.tr,
//                       books: _bookController.filteredBooks,
//                       isHorizontal: false,
//                     ),
//                   ] else ...[
//                     BookSection(
//                       title: 'popular_books'.tr,
//                       books: _bookController.popularBooks,
//                       isHorizontal: true,
//                     ),
//                     BookSection(
//                       title: 'recommended'.tr,
//                       books: _bookController.recommendedBooks,
//                       isHorizontal: true,
//                     ),
//                     BookSection(
//                       title: 'new_arrivals'.tr,
//                       books: _bookController.newBooks,
//                       isHorizontal: false,
//                     ),
//                   ],
//                 ],
//               );
//             }),
//           ],
//         ),
//       ),
//     );
//   }

//   Widget _buildFilterHeader() {
//     return Padding(
//       padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
//       child: Row(
//         mainAxisAlignment: MainAxisAlignment.spaceBetween,
//         children: [
//           Obx(() {
//             final count = _bookController.filteredBooks.length;
//             return Text(
//               '${count} ${'results_found'.tr}',
//               style: TextStyle(
//                 fontSize: 14,
//                 color: Colors.grey[600],
//               ),
//             );
//           }),
//           TextButton(
//             onPressed: _bookController.resetFilters,
//             child: Text('reset_filters'.tr),
//           ),
//         ],
//       ),
//     );
//   }
// }

class AccueilPage extends StatefulWidget {
  const AccueilPage({super.key});

  @override
  State<AccueilPage> createState() => _AccueilPageState();
}

class _AccueilPageState extends State<AccueilPage> 
    with SingleTickerProviderStateMixin {
  late BookController _bookController;
  late AnimationController _animationController;

  @override
  void initState() {
    super.initState();
    _bookController = Get.find<BookController>();
    _animationController = AnimationController(
      duration: const Duration(seconds: 2),
      vsync: this,
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            // Ajout de l'AppBar ici
            const AccueilAppBar(),
            const SizedBox(height: 16),
            
            // Section recherche et filtres
            const AccueilSearchBar(),
            const CategoryChips(),
            
            // Affichage conditionnel
            Obx(() {
              if (_bookController.isLoading.value) {
                return const Expanded(
                  child: Center(child: CircularProgressIndicator()),
                );
              }

              if (_bookController.error.value.isNotEmpty) {
                return Expanded(
                  child: Center(child: Text(_bookController.error.value)),
                );
              }

              return _buildContent();
            }),
          ],
        ),
      ),
      floatingActionButton: ChatbotButton(
        animationController: _animationController,
      ),
    );
  }

  Widget _buildContent() {
    return Expanded(
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            
            // Section membres actifs
            const ActiveMembersSection(),
            const SizedBox(height: 20),
            
            // Sections de livres avec filtrage
            Obx(() {
              final showFilteredResults = 
                  _bookController.currentSearchQuery.value.isNotEmpty || 
                  _bookController.currentCategory.value != null;
              
              return Column(
                children: [
                  if (showFilteredResults) ...[
                    _buildFilterHeader(),
                    BookSection(
                      title: 'search_results'.tr,
                      books: _bookController.filteredBooks,
                      isHorizontal: false,
                    ),
                  ] else ...[
                    BookSection(
                      title: 'popular_books'.tr,
                      books: _bookController.popularBooks,
                      isHorizontal: true,
                    ),
                    BookSection(
                      title: 'recommended'.tr,
                      books: _bookController.recommendedBooks,
                      isHorizontal: true,
                    ),
                    BookSection(
                      title: 'new_arrivals'.tr,
                      books: _bookController.newBooks,
                      isHorizontal: false,
                    ),
                  ],
                ],
              );
            }),
          ],
        ),
      ),
    );
  }

  Widget _buildFilterHeader() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Obx(() {
            final count = _bookController.filteredBooks.length;
            return Text(
              '${count} ${'results_found'.tr}',
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[600],
              ),
            );
          }),
          TextButton(
            onPressed: _bookController.resetFilters,
            child: Text('reset_filters'.tr),
          ),
        ],
      ),
    );
  }
}