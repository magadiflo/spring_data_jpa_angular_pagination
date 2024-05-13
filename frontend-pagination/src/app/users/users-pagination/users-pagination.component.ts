import { Component, OnInit, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AsyncPipe, JsonPipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { BehaviorSubject, Observable, catchError, map, of, startWith } from 'rxjs';

import { State, ProcessingUsers } from '../interfaces/util.interface';
import { ApiResponse, Page } from '../interfaces/api-response.interface';
import { UserService } from '../services/user.service';
import { UserStatusPipe } from '../pipe/user-status.pipe';

@Component({
  selector: 'app-users-pagination',
  standalone: true,
  imports: [AsyncPipe, JsonPipe, NgClass, FormsModule, UserStatusPipe],
  templateUrl: './users-pagination.component.html',
  styleUrl: './users-pagination.component.scss'
})
export class UsersPaginationComponent implements OnInit {

  private _userService = inject(UserService);
  private _responseSubject!: BehaviorSubject<ApiResponse<Page>>;
  private _currentPageSubject: BehaviorSubject<number> = new BehaviorSubject<number>(0);

  public userState$!: Observable<ProcessingUsers>;
  public currentPage$: Observable<number> = this._currentPageSubject.asObservable();

  ngOnInit(): void {
    this.userState$ = this._userService.getUsers()
      .pipe(
        map((resp: ApiResponse<Page>) => {
          this._responseSubject = new BehaviorSubject<ApiResponse<Page>>(resp);
          this._currentPageSubject.next(resp.data.number);

          return { appState: State.APP_LOADED, appData: resp } as ProcessingUsers;
        }),
        startWith({ appState: State.APP_LOADING } as ProcessingUsers),
        catchError((error: HttpErrorResponse) => of({ appState: State.APP_ERROR, error } as ProcessingUsers))
      );
  }

  public goToPage(name?: string, page: number = 0): void {
    this.userState$ = this._userService.getUsers(name, page)
      .pipe(
        map((resp: ApiResponse<Page>) => {
          this._responseSubject.next(resp);
          this._currentPageSubject.next(resp.data.number);

          return { appState: State.APP_LOADED, appData: resp } as ProcessingUsers;
        }),
        startWith({ appState: State.APP_LOADED, appData: this._responseSubject.value } as ProcessingUsers),
        catchError((error: HttpErrorResponse) => of({ appState: State.APP_ERROR, error } as ProcessingUsers))
      );
  }

  public goToNextOrPreviousPage(direction?: string, name?: string): void {
    this.goToPage(name, direction === 'forward' ? this._currentPageSubject.value + 1 : this._currentPageSubject.value - 1);
  }
}
