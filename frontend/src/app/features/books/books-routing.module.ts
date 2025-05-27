import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BookListComponent } from './book-list/book-list.component';
import { BookGridComponent } from './book-grid/book-grid.component';
import { AddBookComponent } from './add-book/add-book.component';
import { BookDetailsComponent } from './book-details/book-details.component';
import { MyBooksComponent } from './my-books/my-books.component';
import { DemandesComponent } from './demandes/demandes.component';
import { LibraryComponent } from './library/library.component';
import { RequestsComponent } from './requests/requests.component';
import { AuthGuard } from 'src/app/core/guards/auth-guard.guard';
import { ColabGuard } from 'src/app/core/guards/colab-guard.guard';
import { ProfileComponent } from '../shared/profile/profile.component';
import { UpdateUserComponent } from '../users/update-user/update-user.component';
import { StatComponent } from '../users/stat/stat.component';
import { UpdateBookComponent } from './update-book/update-book.component';

const routes: Routes = [
  {path:"", component : BookListComponent, canActivate: [AuthGuard,ColabGuard]},
  {path:"grid", component : BookGridComponent, canActivate: [AuthGuard,ColabGuard]},
  {
    path:"add", component: AddBookComponent, canActivate: [AuthGuard,ColabGuard]
  },
  {
    path:"update/:id", component: UpdateBookComponent, canActivate: [AuthGuard,ColabGuard]
  },
  {
    path:"mybooks", component: MyBooksComponent, canActivate: [AuthGuard,ColabGuard]
  },
  {
    path: "details/:id", component: BookDetailsComponent, canActivate: [AuthGuard,ColabGuard]
  },
  {
    path: "library",
    component: LibraryComponent,
    children: [
      { path: "mybooks", component: MyBooksComponent, canActivate: [AuthGuard,ColabGuard] },
      { path: "demands", component: DemandesComponent, canActivate: [AuthGuard,ColabGuard] },
      { path: "requests", component: RequestsComponent, canActivate: [AuthGuard,ColabGuard] },
      {
        path: 'profile',
        component : ProfileComponent
      },
      {
          path: 'profile/:id',
          component : ProfileComponent
        },
         {
            path: 'update/:id',
            component: UpdateUserComponent,
            canActivate: [AuthGuard,ColabGuard]
          }
    ],
  }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BooksRoutingModule { }
