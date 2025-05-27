import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, debounceTime, map, switchMap } from 'rxjs/operators';
import { Observable, of, Subject } from 'rxjs';
import { BookService } from 'src/app/core/services/books.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-update-book',
  templateUrl: './update-book.component.html',
  styleUrls: ['./update-book.component.scss'],
})
export class UpdateBookComponent implements OnInit {
  // Book details fields
  bookTitle: string = '';
  bookAuthor: string = '';
  bookDescription: string = '';
  publishedDate: string = '';
  isbn: string = '';
  category: string = '';
  pageCount: number = 0;
  language: string = '';
  bookCover: string | null = null;
  bookId: any | null = null; // To store the book ID

  private searchSubject = new Subject<void>();

  constructor(
    private bookService: BookService,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {
    
  }

  ngOnInit(): void {
    // Get the book ID from route parameters
    this.bookId = this.route.snapshot.paramMap.get('id');
    if (this.bookId) {
      // Fetch the existing book details
      this.bookService.getBookById(this.bookId).subscribe(
        (book : any) => {
         this.bookTitle =book.title
         this.bookAuthor =book.author
         this.bookDescription =book.description
         this.publishedDate = book.publishedDate
         this.isbn = book.isbn
         this.category = book.category
         this.pageCount = book.pageCount
         this.language = book.language
         this.bookCover = book.coverUrl
         this.bookId = book.id



        },
        (error) => {
          console.error('Error fetching book:', error);
        }
      );
    }
  }



  // Trigger search when title or author changes
  onTitleOrAuthorChange(): void {
    this.searchSubject.next();
  }

  // Handle form submission
  onSubmit(): void {
    if (!this.bookId) {
      console.error('Book ID is missing');
      return;
    }

    const updatedBook = {
      id: this.bookId,
      title: this.bookTitle,
      author: this.bookAuthor,
      description: this.bookDescription,
      publishedDate: this.publishedDate,
      isbn: this.isbn,
      category: this.category,
      pageCount: this.pageCount,
      language: this.language,
      coverUrl: this.bookCover,
    };
    console.log(updatedBook)

      this.bookService.updateBook(updatedBook, updatedBook.id).subscribe(
        (response) => {
          this.router.navigate(['/home/books/library/mybooks']);
        },
        (error) => {
          console.error('Error updating book:', error);
        }
      );
  
  }
}