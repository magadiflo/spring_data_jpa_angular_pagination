import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, delay } from 'rxjs';

import { ApiResponse, Page } from '../interfaces/api-response.interface';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly _serverUrl = 'http://localhost:8080';
  private readonly _httpClient = inject(HttpClient);

  public getUsers(name: string = '', page: number = 0, size: number = 10): Observable<ApiResponse<Page>> {
    const params = new HttpParams()
      .append('name', name)
      .append('page', page)
      .append('size', size);

    return this._httpClient.get<ApiResponse<Page>>(`${this._serverUrl}/api/v1/users`, { params })
      .pipe(delay(1000));
  }

}
