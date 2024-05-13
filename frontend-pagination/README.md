# Angular - Pagination

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 17.3.5.

---

## Creando Servicio

Nuestra clase de servicio, define la función que llama al endpoint del backend que nos retorna datos paginados del usuario. Es importante resaltar que para poder hacer uso de la clase `HttpClient` debemos agregar en el archivo `app.config.ts` el provider `provideHttpClient()`:

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(APP_ROUTES),
    provideHttpClient(),
  ]
};
```

Antes de crear la clase de servicio, es importante definir el tipado de la repuesta que nos retorna el backend. A continuación se muestra las interfaces que tiparan la respuesta:

`api-response.interface.ts`
```typescript
export interface ApiResponse<T> {
  timeStamp:  Date;
  statusCode: number;
  status:     string;
  message:    string;
  data:       T;
}

export interface Page {
  content:          User[];
  pageable:         Pageable;
  last:             boolean;
  totalPages:       number;
  totalElements:    number;
  size:             number;
  number:           number;
  sort:             Sort;
  first:            boolean;
  numberOfElements: number;
  empty:            boolean;
}

export interface User {
  name:     string;
  address:  string;
  status:   Status;
  phone:    string;
  email:    string;
  imageUrl: string;
}

export enum Status {
  active = 'ACTIVE',
  banned = 'BANNED',
  pending = 'PENDING'
}

export interface Pageable {
  pageNumber: number;
  pageSize:   number;
  sort:       Sort;
  offset:     number;
  paged:      boolean;
  unpaged:    boolean;
}

export interface Sort {
  empty:    boolean;
  sorted:   boolean;
  unsorted: boolean;
}
```

Ahora que ya tenemos definida las interfaces que tiparán la respuesta, vamos a construir la clase de servicio:

```typescript
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

    return this._httpClient.get<ApiResponse<Page>>(`${this._serverUrl}/api/v1/users`, { params });
  }

}
```

## Creando pipe 

Crearemos un pipe para poder retornar las clases de badge según el status del usuario

```typescript
@Pipe({
  name: 'userStatus',
  standalone: true
})
export class UserStatusPipe implements PipeTransform {

  transform(status: string): string {
    type UserStatus = { [key: string]: string };
    const userStatus: UserStatus = {
      ACTIVE: 'badge text-bg-success',
      BANNED: 'badge text-bg-warning',
      PENDING: 'badge text-bg-danger'
    };
    return userStatus[status];
  }

}
```

## Creando componente users-pagination - Inicio

Antes de codificar nuestro componente, vamos a definir otro archivo donde definiremos una interfaz que estará vinculado con el procesamiento de los usuarios; simplemente es una interfaz que nos permite tipar los datos:

`util-interface.ts`
```typescript
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
```

Ahora, en el componente de typescript vamos a llamar al servicio `UserService` y hacer uso del método que construimos anterioremente:

```typescript
@Component({
  selector: 'app-users-pagination',
  standalone: true,
  imports: [AsyncPipe, JsonPipe, NgClass, UserStatusPipe],
  templateUrl: './users-pagination.component.html',
  styleUrl: './users-pagination.component.scss'
})
export class UsersPaginationComponent implements OnInit {

  private _userService = inject(UserService);

  public userState$!: Observable<ProcessingUsers>;

  ngOnInit(): void {
    this.userState$ = this._userService.getUsers()
      .pipe(
        map((resp: ApiResponse<Page>) => {
          return { appState: State.APP_LOADED, appData: resp } as ProcessingUsers;
        }),
        startWith({ appState: State.APP_LOADING } as ProcessingUsers),
        catchError((error: HttpErrorResponse) => of({ appState: State.APP_ERROR, error } as ProcessingUsers))
      );
  }
}
```

Notar que el contenido que nos retorna la función `getUsers()` del servicio `UserService` lo estamos transformando con el operador `pipe` de rxjs a un objeto del tipo `ProcessingUsers` para finalmente transformarse en un `Observable<ProcessingUsers>`. El resultado lo estamos almacenando en la variable `userState$`.

Otro punto a resaltar es que estamos haciendo uso del operador `startWith` que es útil para emitir un valor inicial justo al suscribirse al observable antes de que comience a emitir los valores normales.

En el código, el observable `this._userService.getUsers()` emite los usuarios después de una llamada al servicio. Sin embargo, antes de que se complete esa llamada y los usuarios se emitan, emitiremos un estado inicial indicando que la aplicación se está cargando. Esto se hace utilizando el operador `startWith`.

Aquí está cómo se aplica en tu código:

```typescript
startWith({ appState: State.APP_LOADING } as ProcessingUsers)
```

Esto significa que antes de que el observable `this._userService.getUsers()` emita cualquier valor, se emitirá primero `{ appState: State.APP_LOADING } as ProcessingUsers`.

Entonces, cuando te suscribes a `this.userState$`, recibirás este valor inicial `{ appState: State.APP_LOADING }`, y a continuación los datos de usuarios o un estado de error, según lo que ocurra después.

`startWith` se usa para emitir un valor inicial en un observable antes de que comience a emitir sus valores normales, lo que puede ser útil para representar estados iniciales o valores predeterminados.

`startWith`, devuelve un observable que, en el momento de la suscripción, emitirá de forma sincrónica todos los valores proporcionados a este operador, luego se suscribirá a la fuente y reflejará todas sus emisiones a los suscriptores.

Ahora que hemos implementado el componente de typescript vamos a implementar su correspondiente html:

```html
@if (userState$ | async; as state ) {
@switch (state.appState) {
@case ('APP_LOADING') {
<div class="d-flex justify-content-center mt-4">
  <div class="container-spinner text-center">
    <div class="spinner-border text-secondary" role="status">
      <span class="visually-hidden"></span>
    </div>
    <span class="d-block">Loading...</span>
  </div>
</div>
}
@case ('APP_LOADED') {
<div class="container">
  <div class="row">
    <div class="col-md-12 mt-3">
      <h3 class="mt-3 mb-5">Lista de usuarios</h3>
      @if (state.appData!.data.content.length > 0) {
      <table class="table table-striped table-hover">
        <thead>
          <tr>
            <th scope="col">#</th>
            <th scope="col">Imagen</th>
            <th scope="col">Nombre</th>
            <th scope="col">Correo</th>
            <th scope="col">Teléfono</th>
            <th scope="col">Estado</th>
            <th scope="col">Dirección</th>
          </tr>
        </thead>
        <tbody class="table-group-divider">
          @for (user of state.appData!.data.content; track $index) {
          <tr>
            <th scope="row">{{ $index + 1 }}</th>
            <td><img [src]="user.imageUrl" [alt]="user.name" class="rounded-circle img-small"></td>
            <td>{{ user.name }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.phone }}</td>
            <td><span [ngClass]="user.status | userStatus">{{ user.status }}</span></td>
            <td>{{ user.address }}</td>
          </tr>
          }
        </tbody>
      </table>
      } @else {
      <div class="alert alert-warning" role="alert">¡No users found!</div>
      }
    </div>
  </div>
</div>
}
@case ('APP_ERROR') {
<div class="alert alert-danger">
  There was an error
</div>
<pre>{{ state.error | json }}</pre>
}
}
}
```
Ojo que aquí estamos haciendo uso de los nuevos flujos de control de `Angular 17 @if @switch @case @for`.

## Verificando ejecución del componente

Hasta este punto estamos mostrando únicamente la lista que nos retorna el backend en nuestro componente de html. Más adelante implementaremos la paginación y búsqueda.

![Vista inicial](./src/assets/01.pagination-start.png)

## Implementando búsqueda por nombre

Para esta implementación agregaremos el html del `nav` dentro del mismo componente de `users-pagination.component.html` con la finalidad de hacer sencilla la implementación.

Para este formulario usaremos el módulo de `FormsModule` que deberá ser importado en los imports del componente de typescript.

Mostraré solo el html del formulario donde se ingresa el término de búsqueda:

```html
<form #searchForm="ngForm" (ngSubmit)="goToPage(searchForm.value.inputSearch.trim())" class="d-flex"
  role="search">
  <input type="search" name="inputSearch" ngModel class="form-control me-2" placeholder="Search"
    aria-label="Search">
  <button class="btn btn-outline-success" type="submit">Search</button>
</form>
```

En el componente de typescript realizaremos la implementación del método `goToPage()`:

```typescript
@Component({
  selector: 'app-users-pagination',
  standalone: true,
  imports: [AsyncPipe, JsonPipe, NgClass, FormsModule, UserStatusPipe],
  templateUrl: './users-pagination.component.html',
  styleUrl: './users-pagination.component.scss'
})
export class UsersPaginationComponent implements OnInit {

  private _userService = inject(UserService);
  private _responseSubject!: BehaviorSubject<ApiResponse<Page>>;                //<----- (1)

  public userState$!: Observable<ProcessingUsers>;

  ngOnInit(): void {
    this.userState$ = this._userService.getUsers()
      .pipe(
        map((resp: ApiResponse<Page>) => {
          this._responseSubject = new BehaviorSubject<ApiResponse<Page>>(resp); //<----- (2)

          return { appState: State.APP_LOADED, appData: resp } as ProcessingUsers;
        }),
        startWith({ appState: State.APP_LOADING } as ProcessingUsers),
        catchError((error: HttpErrorResponse) => of({ appState: State.APP_ERROR, error } as ProcessingUsers))
      );
  }

  public goToPage(name: string, page: number = 0): void {                       //<----- (3)
    this.userState$ = this._userService.getUsers(name, page)
      .pipe(
        map((resp: ApiResponse<Page>) => {
          this._responseSubject.next(resp);                                     //<----- (4)

          return { appState: State.APP_LOADED, appData: resp } as ProcessingUsers;
        }),
        startWith({ appState: State.APP_LOADED, appData: this._responseSubject.value } as ProcessingUsers),
        catchError((error: HttpErrorResponse) => of({ appState: State.APP_ERROR, error } as ProcessingUsers))
      );
  }
}
```

**DONDE:**
- `(1)`, definimos un `BehaviorSubject<ApiResponse<Page>>` que más adelante utilizaremos para retornar el último valor que haya emitido. Importante, a diferencia de un Subject normal, un `BehaviorSubject` mantiene un estado interno que contiene el último valor emitido.
- `(2)`, en el `ngOnInit` se crea el `BehaviorSubject` con el valor inicial devuelto por el observable `getUsers()`. Este valor será el primer valor emitido y el valor inicial que recibirán los nuevos suscriptores.
- `(3)`, cuando se haga uso de la búsqueda de usuarios mediante el formulario y se llame al método `goToPage()`, este método también tiene el operador de rxjs `startWith`. Este operador tiene dos peculiaridades, a diferencia del valor del `startWith` definido en el `ngOnInit`, cuando se llame al método `goToPage`, el atributo `appState` tendrá el valor de `APP_LOADED` dado que en este punto los datos ya están cargados, solo se está haciendo una búsqueda. Por otro lado, el valor de `appData` deberá ser el valor que se almacenó anteriormente en el `BehaviorSubject`, para ser más exactos en la variable `_responseSubject`.
- `(4)`, cada vez que llegue un nuevo valor del método `getUsers()`, sencillamente el BehaviorSubject `_responseSubject` deberá emitir un nuevo valor mediante su método `next()`. Posteriormente, cuando se vuelva a hacer una llamada al método `goToPage()`, el operador `startWith` usará el último valor emitido por el BehaviorSubject en esta parte del código `appData: this._responseSubject.value`. El `BehaviorSubject` retiene el último valor emitido y lo emite inmediatamente a cualquier nuevo suscriptor, incluso si el valor se emitió antes de que se suscribieran.

El `BehaviorSubject` es muy útil en situaciones en las que necesitas asegurarte de que los suscriptores siempre reciban el último estado disponible, incluso si se suscriben después de que el estado haya sido actualizado. Es comúnmente utilizado en aplicaciones donde se necesita mantener un estado global que pueda ser compartido entre varios componentes o servicios.

Un `Subject` es un tipo especial de observable que se comporta al mismo tiempo como `OBSERVABLE` y como `OBSERVER`, es decir no tenemos que crear el observable por un lado y el observer por el otro y luego subscribirnos, sino que directamente la subject en sí tiene todo lo que necesitamos. `BehaviorSubject`, es un tipo especial de Subject, se inicializa con un valor (si después se actualiza, ese valor debe ser del mismo tipo de valor inicial). Siempre devuelve el último valor, es decir el valor actual de la subscripción, no guarda los datos.

A continuación se muestra cómo es que finalmente quedaría la implementación:

![busqueda por nombre](./src/assets/02.busqueda-por-nombre.png)

## Implementando paginación

Vamos a definir la siguiente clase que luego usaremos en el componente de html para deshabilitar los botones.

```scss
/*
pointer-events, permite controlar si un elemento puede o no recibir
los eventos del cursor con independencia del orden de apilación (su valor de z-index).
*/
.disabled {
  pointer-events: none;
  opacity: 0.6;
}
```

En el componente de typescript crearemos un `BehaviorSubject` que nos ayudará a recordar la página actual en la que el usuario se encuentra:

```typescript
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
  private _currentPageSubject: BehaviorSubject<number> = new BehaviorSubject<number>(0);  //<---- (1)

  public userState$!: Observable<ProcessingUsers>;
  public currentPage$: Observable<number> = this._currentPageSubject.asObservable();      //<---- (2)

  ngOnInit(): void {
    this.userState$ = this._userService.getUsers()
      .pipe(
        map((resp: ApiResponse<Page>) => {
          this._responseSubject = new BehaviorSubject<ApiResponse<Page>>(resp);
          this._currentPageSubject.next(resp.data.number);                                //<---- (3)

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
          this._currentPageSubject.next(resp.data.number);                                //<---- (4)

          return { appState: State.APP_LOADED, appData: resp } as ProcessingUsers;
        }),
        startWith({ appState: State.APP_LOADED, appData: this._responseSubject.value } as ProcessingUsers),
        catchError((error: HttpErrorResponse) => of({ appState: State.APP_ERROR, error } as ProcessingUsers))
      );
  }

  public goToNextOrPreviousPage(direction?: string, name?: string): void {                //<---- (5)
    this.goToPage(name, direction === 'forward' ? this._currentPageSubject.value + 1 : this._currentPageSubject.value - 1);
  }
}
```

**DONDE**
- `(1)`, se define el BehaviorSubject con valor inicial de 0. Este valor representa el número de la página cuando recién se inicia la aplicación.
- `(2)`, se crea un observable a partir del BehaviorSubject. Esta variable será usada en el componente de html.
- `(3)` y `(4)`, emitimos el valor de la página actual, es decir, el índice.
- `(5)`, creamos el método que permitirá avanzar o retroceder, dependiendo del botón que se presione.

Finalmente, en el componente de html, implementamos los botones de la paginación y la enumeración de los elementos:

```html
<table class="table table-striped table-hover">
  <thead>
    <tr>
      <th scope="col">#</th>
      <th scope="col">...</th>
    </tr>
  </thead>
  <tbody class="table-group-divider">
    @for (user of state.appData!.data.content; track $index) {
    <tr>
      <th scope="row">{{ state.appData!.data.size * state.appData!.data.number + $index + 1 }}</th>
      <td>...</td>
    </tr>
    }
  </tbody>
</table>

<nav aria-label="Page navigation example">
  <ul class="pagination">
    <li class="page-item" [ngClass]="0 == (currentPage$ | async) ? 'disabled' : ''">
      <a class="page-link" href="#" aria-label="Previous"
        (click)="$event.preventDefault(); goToNextOrPreviousPage('backward', searchForm.value.inputSearch.trim())">
        <span aria-hidden="true">&laquo;</span>
      </a>
    </li>

    <!-- [].constructor(state.appData!.data.totalPages), definimos un arreglo cuyo tamaño está dado por el totalPage -->
    @for (pageNumber of [].constructor(state.appData!.data.totalPages); track $index) {
    <li class="page-item" [ngClass]="$index == (currentPage$ | async) ? 'active' : ''">
      <a class="page-link" href="#"
        (click)="$event.preventDefault(); goToPage(searchForm.value.inputSearch.trim(), $index)">
        {{ $index + 1 }}
      </a>
    </li>
    }

    <li class="page-item"
      [ngClass]="(state.appData!.data.totalPages - 1) == (currentPage$ | async) ? 'disabled' : ''">
      <a class="page-link" href="#" aria-label="Next"
        (click)="$event.preventDefault(); goToNextOrPreviousPage('forward', searchForm.value.inputSearch.trim())">
        <span aria-hidden="true">&raquo;</span>
      </a>
    </li>
  </ul>
</nav>
```

Finalmente, de haber implementado la paginación quedaría tal como se ve en la imagen:

![paginación](./src/assets/03.paginacion.png)
