package ru.practicum.explorewithme.service.adminapi;

import ru.practicum.explorewithme.dto.user.NewUserRequest;
import ru.practicum.explorewithme.dto.user.UserDto;

import java.util.List;

public interface AdminUserService {

    UserDto create(NewUserRequest newUserRequest);

    List<UserDto> findAllByIds(List<Long> ids);

    List<UserDto> findAll(Integer from, Integer size);

    void delete(Long userId);
}
