// import 'package:app/imports.dart';
// import 'package:app/views/profile/profile.dart';
// import 'package:flutter/material.dart';
// import 'package:get/get_core/src/get_main.dart';

// class AccueilAppBar extends StatelessWidget {
//   const AccueilAppBar({super.key});

//   @override
//   Widget build(BuildContext context) {
//     return Padding(
//       padding: const EdgeInsets.all(16.0),
//       child: Row(
//         mainAxisAlignment: MainAxisAlignment.spaceBetween,
//         children: [
//           Row(
//             children: [
//               Image.asset('assets/images/logo.png', height: 40),
//               const SizedBox(width: 10),
//               const Text(
//                 'SofBiblio',
//                 style: TextStyle(
//                   fontSize: 20,
//                   fontWeight: FontWeight.bold,
//                 ),
//               ),
//             ],
//           ),
//           GestureDetector(
//             onTap: () => {
//               Get.to(() => const ProfilePage())
//             }, // Navigation vers profil
//             child: const CircleAvatar(
//               radius: 20,
//               backgroundImage: AssetImage(
//                 'assets/images/profile.jpg',
//               ),
//               backgroundColor: Colors.grey,
//             ),
//           ),
//         ],
//       ),
//     );
//   }
// }
import 'package:app/imports.dart';
import 'package:app/views/profile/profile.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class AccueilAppBar extends StatelessWidget {
  const AccueilAppBar({super.key});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2.0), // haut/bas seulement
      child: Center(
        child: Container(
          constraints: const BoxConstraints(maxWidth: 600), // 👈 Limite la largeur
          padding: const EdgeInsets.symmetric(horizontal: 16.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Image.asset('assets/images/logo.png', height: 40),
                  const SizedBox(width: 10),
                  const Text(
                    'SofBiblio',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              GestureDetector(
                onTap: () => Get.to(() => const ProfilePage()),
                child: const CircleAvatar(
                  radius: 20,
                  backgroundImage: AssetImage('assets/images/profile.jpg'),
                  backgroundColor: Colors.grey,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
