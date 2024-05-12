package dev.magadiflo.pagination.app.repository;

import dev.magadiflo.pagination.app.domain.User;
import dev.magadiflo.pagination.app.projection.UserProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

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
