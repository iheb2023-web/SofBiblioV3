// import 'package:flutter/material.dart';
// import 'package:get/get.dart';

// class AccueilSearchBar extends StatelessWidget {
//   const AccueilSearchBar({super.key});

//   @override
//   Widget build(BuildContext context) {
//     return Padding(
//       padding: const EdgeInsets.symmetric(horizontal: 16.0),
//       child: TextField(
//         decoration: InputDecoration(
//           hintText: 'search_book'.tr,
//           prefixIcon: const Icon(Icons.search),
//           border: OutlineInputBorder(
//             borderRadius: BorderRadius.circular(30),
//             borderSide: BorderSide.none,
//           ),
//           filled: true,
//           fillColor: Colors.grey[200],
//         ),
//       ),
//     );
//   }
// }


import 'dart:async';

import 'package:app/controllers/book_controller.dart';
import 'package:app/views/explorer/explorer.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

// class AccueilSearchBar extends StatelessWidget {
//   const AccueilSearchBar({super.key});

//   @override
//   Widget build(BuildContext context) {
//     final TextEditingController searchController = TextEditingController();

//     return Padding(
//       padding: const EdgeInsets.symmetric(horizontal: 16.0),
//       child: TextField(
//         controller: searchController,
//         onSubmitted: (value) {
//           if (value.trim().isNotEmpty) {
//             Get.to(() => ExplorePage(initialQuery: value.trim()));
//           }
//         },
//         decoration: InputDecoration(
//           hintText: 'search_book'.tr,
//           prefixIcon: const Icon(Icons.search),
//           border: OutlineInputBorder(
//             borderRadius: BorderRadius.circular(30),
//             borderSide: BorderSide.none,
//           ),
//           filled: true,
//           fillColor: Colors.grey[200],
//         ),
//       ),
//     );
//   }
// }
class AccueilSearchBar extends StatelessWidget {
  const AccueilSearchBar({super.key});

  @override
  Widget build(BuildContext context) {
    final TextEditingController searchController = TextEditingController();
    final BookController bookController = Get.find<BookController>();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: TextField(
        controller: searchController,
        onChanged: (value) {
          // Recherche en temps réel après un petit délai
          Debouncer(milliseconds: 500).run(() {
            bookController.filterBooks(query: value.trim());
          });
        },
        onSubmitted: (value) {
          bookController.filterBooks(query: value.trim());
        },
        decoration: InputDecoration(
          hintText: 'search_book'.tr,
          prefixIcon: const Icon(Icons.search),
          suffixIcon: Obx(() {
            if (bookController.currentSearchQuery.value.isNotEmpty) {
              return IconButton(
                icon: const Icon(Icons.close),
                onPressed: () {
                  searchController.clear();
                  bookController.filterBooks(query: '');
                },
              );
            }
            return const SizedBox.shrink();
          }),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(30),
            borderSide: BorderSide.none,
          ),
          filled: true,
          fillColor: Colors.grey[200],
          contentPadding: const EdgeInsets.symmetric(vertical: 0, horizontal: 20),
        ),
      ),
    );
  }
}

// Helper class pour le debounce
class Debouncer {
  final int milliseconds;
  Timer? _timer;

  Debouncer({required this.milliseconds});

  void run(VoidCallback action) {
    _timer?.cancel();
    _timer = Timer(Duration(milliseconds: milliseconds), action);
  }
}