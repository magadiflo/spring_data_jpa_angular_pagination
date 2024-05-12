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
