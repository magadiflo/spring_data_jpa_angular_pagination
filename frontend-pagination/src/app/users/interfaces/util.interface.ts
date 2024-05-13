import { HttpErrorResponse } from "@angular/common/http";

import { ApiResponse, Page } from "./api-response.interface";

export interface ProcessingUsers {
  appState: State;
  appData?: ApiResponse<Page>;
  error?: HttpErrorResponse;
}

export enum State {
  APP_LOADED = 'APP_LOADED',
  APP_LOADING = 'APP_LOADING',
  APP_ERROR = 'APP_ERROR',
}
