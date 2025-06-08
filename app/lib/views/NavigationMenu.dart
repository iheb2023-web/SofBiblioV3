import 'package:app/views/explorer/explorer.dart';
import 'package:flutter/material.dart';
import 'package:app/views/Accueil/AccueilPage.dart';
import 'package:app/views/Ma_Biblio/ma_biblio_page.dart';
import 'package:get/get.dart';
import 'package:app/views/profile/profile.dart';

class NavigationMenu extends StatefulWidget {
  final int initialIndex;
  
  const NavigationMenu({
    super.key,
    this.initialIndex = 0,
  });


  @override
  State<NavigationMenu> createState() => _NavigationMenuState();
}


class _NavigationMenuState extends State<NavigationMenu> {
  int _selectedIndex = 0;

  final List<Widget> _pages = [
    const AccueilPage(),
    const ExplorePage(),
    const MaBiblioPage(),
    const ProfilePage(),
  ];

  @override
  void initState() {
    super.initState();
    _selectedIndex = widget.initialIndex; // ⚠️ C’est ici que tu utilises la valeur passée
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _pages[_selectedIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,
        selectedItemColor: Colors.blue,
        unselectedItemColor: Colors.grey,
        type: BottomNavigationBarType.fixed,
        onTap: (index) {
          setState(() {
            _selectedIndex = index;
          });
        },
        items: [
          BottomNavigationBarItem(
            icon: const Icon(Icons.home),
            label: 'home'.tr,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.explore),
            label: 'explore'.tr,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.library_books),
            label: 'my_library'.tr,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.person),
            label: 'profile'.tr,
          ),
        ],
      ),
    );
  }
}
