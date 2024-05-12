package dev.magadiflo.pagination.app.service.impl;

import dev.magadiflo.pagination.app.projection.UserProjection;
import dev.magadiflo.pagination.app.repository.UserRepository;
import dev.magadiflo.pagination.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
