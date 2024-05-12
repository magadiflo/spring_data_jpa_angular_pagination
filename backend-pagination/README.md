# Spring Data JPA - Pagination

---

## Dependencias

````xml
<!--Spring Boot 3.2.5-->
<!--Java 21-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Dominio User

````java

@ToString
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String status;
    private String address;
    private String phone;
    private String imageUrl;

}
````

## Repositorio con projections

Nuestro repositorio implementará la interfaz de Spring Data `PagingAndSortingRepository` que nos permitirá realizar
paginación. Pero no utilizaremos la entidad de dominio `User` para retornar al cliente, sino más bien utilizaremos
los `projections`. Entonces, para retornar información al cliente crearemos una interfaz de proyección cerrada
llamada `UserProjection`.

Recordar que las proyecciones nos permiten seleccionar solo las columnas que queremos recuperar de nuestro modelo de
datos, es decir, de nuestro dominio `User` únicamente queremos recuperar ciertas columnas y esas columnas serán
mapeadas a nuestra proyección.

Las proyecciones son una característica poderosa que te permite seleccionar solo una parte de los datos de tus entidades
en consultas de base de datos, en lugar de cargar toda la entidad.

````java
public interface UserProjection {
    String getName();

    String getEmail();

    String getStatus();

    String getAddress();

    String getPhone();

    String getImageUrl();
}
````

Como vamos a retornar una proyección, necesitamos crear nuestra consulta JPQL con las columnas que queremos retornar.
En este caso vamos a omitir la columna `id` de la entidad `User`.

````java
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    @Query("""
            SELECT u.name AS name,
                    u.email AS email,
                    u.status AS status,
                    u.address AS address,
                    u.phone AS phone,
                    u.imageUrl AS imageUrl
            FROM User AS u
            WHERE u.name LIKE %:name%
            """)
    Page<UserProjection> findByNameContaining(String name, Pageable pageable);
}
````

## Capa de servicio

En nuestra capa de servicio tendremos únicamente el método `getUsers()` para recuperar la proyección de los
usuarios paginados.

````java
public interface UserService {
    Page<UserProjection> getUsers(String name, int pageNumber, int pageSize);
}
````

````java

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserProjection> getUsers(String name, int pageNumber, int pageSize) {
        log.info("Recuperando usuarios por page {} de size {}", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return this.userRepository.findByNameContaining(name, pageable);
    }
}
````

## Capa controller

Antes de implementar el controller necesitamos definir una clase que será la que unificará la información que
devolvamos al cliente para tener simpre un mismo formato:

````java

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HttpResponse<T> {

    private String timeStamp;
    private int statusCode;
    private HttpStatus status;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

}
````

`@JsonInclude(JsonInclude.Include.NON_NULL)`, es una anotación que se coloca en una clase o en un campo de una clase
para indicar cómo se deben manejar los valores nulos durante la serialización a JSON. En este caso,
`JsonInclude.Include.NON_NULL` significa que los campos nulos no se incluirán en la salida JSON. Es decir, si un campo
de un objeto es nulo, al convertir ese objeto a JSON, el campo nulo no aparecerá en el JSON resultante.

Finalmente, implementamos el controlador haciendo uso de la clase genérica creada anteriormente.

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<HttpResponse<?>> getUsersWithPagination(@RequestParam(value = "name", defaultValue = "", required = false) String name,
                                                                  @RequestParam(value = "page", defaultValue = "0", required = false) Integer pageNumber,
                                                                  @RequestParam(value = "size", defaultValue = "10", required = false) Integer pageSize) {
        Page<UserProjection> usersPage = this.userService.getUsers(name, pageNumber, pageSize);
        HttpResponse<Page<UserProjection>> response = HttpResponse.<Page<UserProjection>>builder()
                .timeStamp(LocalDateTime.now().toString())
                .data(usersPage)
                .message("Usuarios recuperados")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

}
````

## Configurando filtro CORS para permitir solicitudes del frontend

Configuraremos `CORS` para permitir el acceso a nuestro recurso desde diferentes dominios, en nuestro caso específico,
desde la dirección donde se ejecute Angular (http://localhost:4200) y controlar las solicitudes.

````java

@Configuration
public class AppConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
        corsConfiguration.setAllowedHeaders(
                Arrays.asList(
                        HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT, HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        HttpHeaders.AUTHORIZATION, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS
                )
        );
        corsConfiguration.setExposedHeaders(
                Arrays.asList(
                        HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT, HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        HttpHeaders.AUTHORIZATION, HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS
                )
        );
        corsConfiguration.setAllowedMethods(
                Arrays.asList(
                        HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()
                )
        );

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration); // "/**", representa todas las rutas del backend

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

}
````

## Agregando configuraciones

En el `application.yml` agregaremos las siguientes configuraciones:

````yml
server:
  port: 8080
  error:
    include-message: always

spring:
  application:
    name: backend-pagination

  datasource:
    url: jdbc:mysql://localhost:3306/db_spring_data_jpa
    username: admin
    password: magadiflo

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    defer-datasource-initialization: true

  sql:
    init:
      mode: always

logging:
  level:
    org.hibernate.SQL: DEBUG
````

**DONDE**

- `spring.jpa.hibernate.ddl-auto=update`, esta configuración nos permitirá crear las tablas a partir de las anotaciones
  de `jpa/hibernate` que tiene nuestro modelo de dominio `User`.
- `spring.jpa.defer-datasource-initialization=true`, permite aplazar la inicialización de la fuente de datos, o en
  nuestro caso, aplazar la ejecución del archivo `data.sql` que tenemos en el directorio `/resources`. Esto ocurre
  porque, de forma predeterminada el script `data.sql` se ejecuta antes de que hibernate se inicialice y nosotros
  necesitamos Hibernate para crear las tablas antes de insertar los datos en ellas, al menos la primera vez que
  ejecutamos el proyecto, dado que estamos trabajando con `ddl-auto: update`. La primera vez se creará, posteriormente
  las tablas ya estarán creadas.
- `spring.jpa.sql.init.mode=always`, para cualquier inicialización basada en scripts, es decir, insertar datos a través
  de `data.sql` o crear un esquema a través de `schema.sql`, debemos establecer esta propiedad.

## Probando paginación

Realizamos una petición al nuestro endpoint para solicitar información del nombre de usuarios que tengan en el nombre
el término `da`; solicitamos la página `1` y de tamaño `5`.

````bash
$ curl -v -G --data "name=da&page=1&size=5" http://localhost:8080/api/v1/users | jq
>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "timeStamp": "2024-05-12T00:04:21.869292700",
  "statusCode": 200,
  "status": "OK",
  "message": "Usuarios recuperados",
  "data": {
    "content": [
      {
        "name": "Merrily Geldard",
        "address": "583 Paget Plaza",
        "status": "ACTIVE",
        "phone": "997-295-0403",
        "email": "mgeldard1g@de.vu",
        "imageUrl": "https://randomuser.me/api/portraits/men/53.jpg"
      },
      {
        "name": "Almeda Ebdin",
        "address": "10 Menomonie Avenue",
        "status": "ACTIVE",
        "phone": "640-989-8930",
        "email": "aebdin26@usatoday.com",
        "imageUrl": "https://randomuser.me/api/portraits/women/31.jpg"
      },
      {
        "name": "Daisie Tipple",
        "address": "7102 Stone Corner Lane",
        "status": "ACTIVE",
        "phone": "973-203-2497",
        "email": "dtipple2e@bing.com",
        "imageUrl": "https://randomuser.me/api/portraits/women/84.jpg"
      },
      {
        "name": "Giralda Powrie",
        "address": "25 Thierer Circle",
        "status": "BANNED",
        "phone": "904-344-3695",
        "email": "gpowrie2r@ebay.com",
        "imageUrl": "https://randomuser.me/api/portraits/men/84.jpg"
      }
    ],
    "pageable": {
      "pageNumber": 1,
      "pageSize": 5,
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "offset": 5,
      "paged": true,
      "unpaged": false
    },
    "last": true,
    "totalPages": 2,
    "totalElements": 9,
    "size": 5,
    "number": 1,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "first": false,
    "numberOfElements": 4,
    "empty": false
  }
}
````

Si observamos el log mostrado en el ide de IntelliJ IDEA, veremos lo siguiente:

````bash
 INFO 10000 --- [backend-pagination] d.m.p.app.service.impl.UserServiceImpl   : Recuperando usuarios por page 1 de size 5
DEBUG 10000 --- [backend-pagination] org.hibernate.SQL                        : 
    select
        u1_0.name,
        u1_0.email,
        u1_0.status,
        u1_0.address,
        u1_0.phone,
        u1_0.image_url 
    from
        users u1_0 
    where
        u1_0.name like replace(?, '\\', '\\\\') 
    limit
        ?, ?
````

Vemos que la consulta que hemos construido en el repositorio está aplicándose y con él la proyección, es decir, estamos
recuperando únicamente las columnas de la tabla `users` que le hemos dicho que recupere y no todas las columnas.