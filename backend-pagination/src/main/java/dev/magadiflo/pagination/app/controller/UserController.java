package dev.magadiflo.pagination.app.controller;

import dev.magadiflo.pagination.app.projection.UserProjection;
import dev.magadiflo.pagination.app.service.UserService;
import dev.magadiflo.pagination.app.util.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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
