import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:image_picker/image_picker.dart';
import 'package:app/controllers/auth_controller.dart';
import 'package:app/controllers/theme_controller.dart';
import 'package:app/models/user_model.dart';
import 'dart:io';

class ProfileHeader extends StatelessWidget {
  final User user;
  final Future<int?> borrowsCount;
  final Future<int?> booksCount;
  final int toReadCount;

  const ProfileHeader({
    super.key,
    required this.user,
    required this.borrowsCount,
    required this.booksCount,
    required this.toReadCount,
  });

  ImageProvider? _getSafeImage(String? url, bool isDarkMode) {
    if (url == null) return null;
    
    final processedUrl = url
      .replaceFirst('.avif', '')
      .replaceFirst('upload/', 'upload/f_auto,q_auto,c_fill,w_200,h_200/');
    
    try {
      return NetworkImage(
        '$processedUrl&t=${DateTime.now().millisecondsSinceEpoch}',
        headers: {'Cache-Control': 'no-cache'},
      );
    } catch (e) {
      debugPrint('Image processing error: $e');
      return const AssetImage('assets/fallback.png');
    }
  }

  Widget? _buildFallbackChild(User user, bool isDarkMode) {
    if (user.image != null) return null; // L'image est prioritaire
    
    return Text(
      user.firstname.isNotEmpty 
          ? user.firstname.substring(0, 1).toUpperCase()
          : '',
      style: TextStyle(
        fontSize: 40,
        color: isDarkMode ? Colors.blue[200] : Colors.blue,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final themeController = Get.find<ThemeController>();
    final isDarkMode = themeController.isDarkMode;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: isDarkMode ? Colors.grey[800] : Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(isDarkMode ? 0.1 : 0.05),
            spreadRadius: 2,
            blurRadius: 8,
            offset: const Offset(0, 3),
          ),
        ],
      ),
      
      child: Column(
        children: [
          Stack(
            alignment: Alignment.bottomRight,
            children: [
              CircleAvatar(
                radius: 50,
                backgroundColor: isDarkMode ? Colors.blue[900] : Colors.blue.shade100,
                backgroundImage: _getSafeImage(user.image, isDarkMode),
                child: _buildFallbackChild(user, isDarkMode),
              ),
              CircleAvatar(
                radius: 18,
                backgroundColor: isDarkMode ? Colors.grey[700] : Colors.white,
                child: IconButton(
                  icon: Icon(
                    Icons.camera_alt,
                    size: 18,
                    color: isDarkMode ? Colors.blue[200] : Colors.blue,
                  ),
                  onPressed: () => _pickAndUploadImage(),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Text(
            '${user.firstname} ${user.lastname}',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              color: isDarkMode ? Colors.white : Colors.black87,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            '${user.email}',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: isDarkMode ? Colors.grey[400] : Colors.grey[600],
            ),
          ),
          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              FutureBuilder<int?>(
                future: borrowsCount,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return _buildStatItem('Emprunts', '...', isDarkMode);
                  }
                  return _buildStatItem(
                    'Emprunts',
                    snapshot.data?.toString() ?? '0',
                    isDarkMode,
                  );
                },
              ),
              FutureBuilder<int?>(
                future: booksCount,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return _buildStatItem('Mes Livres', '...', isDarkMode);
                  }
                  return _buildStatItem(
                    'Mes Livres',
                    snapshot.data?.toString() ?? '0',
                    isDarkMode,
                  );
                },
              ),
              _buildStatItem('À lire', toReadCount.toString(), isDarkMode),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatItem(String label, String value, bool isDarkMode) {
    return Column(
      children: [
        Text(
          value,
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: isDarkMode ? Colors.blue[200] : Colors.blue,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: TextStyle(
            fontSize: 14,
            color: isDarkMode ? Colors.grey[400] : Colors.grey[600],
          ),
        ),
      ],
    );
  }

  Future<void> _pickAndUploadImage() async {
    final ImagePicker picker = ImagePicker();
    final themeController = Get.find<ThemeController>();
    final isDarkMode = themeController.isDarkMode;

    final XFile? image = await Get.bottomSheet<XFile?>(
      Container(
        decoration: BoxDecoration(
          color: isDarkMode ? Colors.grey[800] : Colors.white,
          borderRadius: const BorderRadius.only(
            topLeft: Radius.circular(16),
            topRight: Radius.circular(16),
          ),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: Icon(
                Icons.camera,
                color: isDarkMode ? Colors.white : Colors.black,
              ),
              title: Text(
                'Prendre une photo',
                style: TextStyle(
                  color: isDarkMode ? Colors.white : Colors.black,
                ),
              ),
              onTap: () async {
                final picked = await picker.pickImage(
                  source: ImageSource.camera,
                );
                Get.back(result: picked);
              },
            ),
            ListTile(
              leading: Icon(
                Icons.photo,
                color: isDarkMode ? Colors.white : Colors.black,
              ),
              title: Text(
                'Choisir depuis la galerie',
                style: TextStyle(
                  color: isDarkMode ? Colors.white : Colors.black,
                ),
              ),
              onTap: () async {
                final picked = await picker.pickImage(
                  source: ImageSource.gallery,
                );
                Get.back(result: picked);
              },
            ),
            ListTile(
              leading: Icon(
                Icons.cancel,
                color: isDarkMode ? Colors.white : Colors.black,
              ),
              title: Text(
                'Annuler',
                style: TextStyle(
                  color: isDarkMode ? Colors.white : Colors.black,
                ),
              ),
              onTap: () => Get.back(),
            ),
          ],
        ),
      ),
    );

    if (image != null) {
      try {
        final AuthController authController = Get.find<AuthController>();
        final String? imageUrl = await authController.uploadProfileImage(
          File(image.path),
        );

        if (imageUrl != null) {
          final updatedUser = User(
            id: user.id,
            firstname: user.firstname,
            lastname: user.lastname,
            email: user.email,
            phone: user.phone,
            job: user.job,
            birthday: user.birthday,
            image: imageUrl,
            role: user.role,
          );
          authController.currentUser.value = updatedUser;

          Get.showSnackbar(
            const GetSnackBar(
              message: 'Photo de profil mise à jour',
              backgroundColor: Colors.green,
              duration: Duration(seconds: 3),
              snackPosition: SnackPosition.BOTTOM,
            ),
          );
        }
      } catch (e) {
        Get.showSnackbar(
          GetSnackBar(
            message: 'Erreur lors de l\'upload: $e',
            backgroundColor: Colors.red,
            duration: const Duration(seconds: 3),
            snackPosition: SnackPosition.BOTTOM,
          ),
        );
      }
    }
  }
}