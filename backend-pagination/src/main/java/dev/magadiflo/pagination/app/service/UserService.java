package dev.magadiflo.pagination.app.service;

import dev.magadiflo.pagination.app.projection.UserProjection;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserProjection> getUsers(String name, int pageNumber, int pageSize);
}
