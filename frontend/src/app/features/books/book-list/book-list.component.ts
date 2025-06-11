import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BookService } from 'src/app/core/services/books.service';
import { RecommendationsServices } from 'src/app/core/services/recommendationsService';

@Component({
  selector: 'app-book-list',
  templateUrl: './book-list.component.html',
  styleUrls: ['./book-list.component.scss']
})
export class BookListComponent implements OnInit {
  recommanded_books: any[] = [];
  groupedBooks: any[][] = [];
  books: any[] = [];
  filteredBooks: any[] = [];
  currentPage = 1;
  itemsPerPage = 5;
  totalItems = 0;
  maxVisiblePages = 5; // Maximum number of visible page buttons
  searchTerm: string = ''; // New property for search term
  
  // Filter properties
  selectedCategories: string[] = [];
  selectedPageRanges: string[] = [];
  selectedLanguages: string[] = [];
  selectedRatings: number[] = [];

  // Available categories will be populated dynamically
  availableCategories: string[] = ['Tous']; // 'Tous' is always available
  availableLanguages: string[] = [];

  constructor(
    private bookService: BookService,
    private recommendationsServices: RecommendationsServices,
    private router: Router
  ) {}

  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('user') || '{}'); 
    const email = user?.email; 
    const id = user?.id;
    if(id){
      this.getRecommendations(id)
    }
    if(email) {
      this.allBookWithinEmailOwner(email);
    }
    
  }

  getRecommendations(iduser: any) {
    this.recommendationsServices.getRecommendations(iduser).subscribe(
      (books: any) => {
        this.recommanded_books = books;
        this.groupBooks();
      }
    );
  }

  groupBooks() {
    // Group books into chunks of 3 for carousel slides
    this.groupedBooks = [];
    for (let i = 0; i < this.recommanded_books.length; i += 3) {
      this.groupedBooks.push(this.recommanded_books.slice(i, i + 3));
    }
  }
  

  allBookWithinEmailOwner(email: any): void {
    this.bookService.allBookWithinEmailOwner(email).subscribe(
      (books: any) => {
        this.books = books;
        this.filteredBooks = [...books];
        this.totalItems = books.length;
        
        // Extract unique categories from books
        this.extractUniqueCategories();
        // Extract unique languages from books
        this.extractUniqueLanguages();
        
        this.applyFilters();
      },
      (error) => {
        console.error('Error fetching books:', error);
      }
    );
  }

  private extractUniqueCategories(): void {
    const categories = new Set<string>();
    
    // Add all categories from books
    this.books.forEach(book => {
      if (book.category) {
        categories.add(book.category);
      }
    });
    
    // Convert Set to array and sort alphabetically
    this.availableCategories = ['Tous', ...Array.from(categories).sort()];
  }

  private extractUniqueLanguages(): void {
    const languages = new Set<string>();
    
    // Add all languages from books
    this.books.forEach(book => {
      if (book.language) {
        languages.add(book.language);
      }
    });
    
    // Convert Set to array and sort alphabetically
    this.availableLanguages = Array.from(languages).sort();
  }

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.itemsPerPage);
  }

  get paginatedBooks(): any[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredBooks.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get visiblePages(): number[] {
    const pages: number[] = [];
    const half = Math.floor(this.maxVisiblePages / 2);
    let start = Math.max(1, this.currentPage - half);
    let end = Math.min(this.totalPages, start + this.maxVisiblePages - 1);
    
    // Adjust if we're at the end
    if (end - start + 1 < this.maxVisiblePages) {
      start = Math.max(1, end - this.maxVisiblePages + 1);
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    return pages;
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  isCategorySelected(category: string): boolean {
    return this.selectedCategories.includes(category);
  }

  isPageRangeSelected(range: string): boolean {
    return this.selectedPageRanges.includes(range);
  }

  isLanguageSelected(language: string): boolean {
    return this.selectedLanguages.includes(language);
  }

  isRatingSelected(rating: number): boolean {
    return this.selectedRatings.includes(rating);
  }

  toggleCategory(category: string, event: any): void {
    if (category === 'Tous') {
      if (event.target.checked) {
        this.selectedCategories = ['Tous'];
      } else {
        this.selectedCategories = [];
      }
    } else {
      if (event.target.checked) {
        // Remove 'Tous' if it was selected
        this.selectedCategories = this.selectedCategories.filter(c => c !== 'Tous');
        this.selectedCategories.push(category);
      } else {
        const index = this.selectedCategories.indexOf(category);
        if (index > -1) {
          this.selectedCategories.splice(index, 1);
        }
      }
    }
    this.applyFilters();
  }

  togglePageRange(range: string, event: any): void {
    if (event.target.checked) {
      this.selectedPageRanges.push(range);
    } else {
      const index = this.selectedPageRanges.indexOf(range);
      if (index > -1) {
        this.selectedPageRanges.splice(index, 1);
      }
    }
    this.applyFilters();
  }

  toggleLanguage(language: string, event: any): void {
    if (event.target.checked) {
      this.selectedLanguages.push(language);
    } else {
      const index = this.selectedLanguages.indexOf(language);
      if (index > -1) {
        this.selectedLanguages.splice(index, 1);
      }
    }
    this.applyFilters();
  }

  toggleRating(rating: number, event: any): void {
    if (event.target.checked) {
      this.selectedRatings.push(rating);
    } else {
      const index = this.selectedRatings.indexOf(rating);
      if (index > -1) {
        this.selectedRatings.splice(index, 1);
      }
    }
    this.applyFilters();
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

  applyFilters(): void {
    this.filteredBooks = this.books.filter(book => {
      // Search filter
      if (this.searchTerm.trim()) {
        const searchLower = this.searchTerm.toLowerCase().trim();
        const matchesTitle = book.title?.toLowerCase().includes(searchLower);
        const matchesAuthor = book.author?.toLowerCase().includes(searchLower);
        if (!matchesTitle && !matchesAuthor) {
          return false;
        }
      }

      // Category filter
      if (this.selectedCategories.length > 0) {
        if (this.selectedCategories.includes('Tous')) {
          // If "Tous" is selected, show all books
        } else if (!book.category || !this.selectedCategories.includes(book.category)) {
          return false;
        }
      }

      // Page range filter
      if (this.selectedPageRanges.length > 0) {
        const pageCount = book.pageCount || 0;
        let pageRangeMatch = false;
        
        for (const range of this.selectedPageRanges) {
          if (range === '0-100' && pageCount <= 100) pageRangeMatch = true;
          else if (range === '100-300' && pageCount > 100 && pageCount <= 300) pageRangeMatch = true;
          else if (range === '300-500' && pageCount > 300 && pageCount <= 500) pageRangeMatch = true;
          else if (range === '500-700' && pageCount > 500 && pageCount <= 700) pageRangeMatch = true;
          else if (range === '700+' && pageCount > 700) pageRangeMatch = true;
        }
        
        if (!pageRangeMatch) return false;
      }

      // Language filter
      if (this.selectedLanguages.length > 0 && 
          (!book.language || !this.selectedLanguages.includes(book.language))) {
        return false;
      }

      // Rating filter
      if (this.selectedRatings.length > 0) {
        const bookRating = book.rating || 0;
        let ratingMatch = false;
        
        for (const rating of this.selectedRatings) {
          if (bookRating >= rating) ratingMatch = true;
        }
        
        if (!ratingMatch) return false;
      }

      return true;
    });

    this.totalItems = this.filteredBooks.length;
    this.currentPage = 1; // Reset to first page when filters change
  }

  clearFilters(): void {
    this.selectedCategories = [];
    this.selectedPageRanges = [];
    this.selectedLanguages = [];
    this.selectedRatings = [];
    this.searchTerm = '';
    
    // Uncheck all checkboxes
    const checkboxes = document.querySelectorAll('input[type="checkbox"]');
    checkboxes.forEach((checkbox: any) => {
      checkbox.checked = false;
    });
    
    this.applyFilters();
  }
}