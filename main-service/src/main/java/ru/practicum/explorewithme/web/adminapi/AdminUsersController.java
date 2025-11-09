package ru.practicum.explorewithme.web.adminapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.user.NewUserRequest;
import ru.practicum.explorewithme.dto.user.UserDto;
import ru.practicum.explorewithme.service.adminapi.AdminUserService;

import java.util.List;

/*
    Admin: Пользователи
    API для работы с пользователями
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUsersController {

    private final AdminUserService adminUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(
            @Valid @RequestBody NewUserRequest newUserRequest
    ) {
        log.info("Admin create user : {}", newUserRequest);

        return adminUserService.create(newUserRequest);
    }

    @GetMapping
    public List<UserDto> findAll(
            @RequestParam(name = "ids", required = false) List<Long> ids,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size
    ) {
        log.info("Admin find users ids: {}, from: {}, size: {}", ids, from, size);

        if (ids != null && !ids.isEmpty()) {
            return adminUserService.findAllByIds(ids);
        }
        return adminUserService.findAll(from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long userId
    ) {
        log.info("Admin delete user id: {}", userId);

        adminUserService.delete(userId);
    }
}
