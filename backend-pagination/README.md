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

