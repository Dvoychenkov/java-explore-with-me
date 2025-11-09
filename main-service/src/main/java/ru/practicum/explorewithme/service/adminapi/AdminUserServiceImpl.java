package ru.practicum.explorewithme.service.adminapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.user.NewUserRequest;
import ru.practicum.explorewithme.dto.user.UserDto;
import ru.practicum.explorewithme.mapper.UserMapper;
import ru.practicum.explorewithme.util.QueryUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        User user = userMapper.toEntity(newUserRequest);

        User saved;
        try {
            saved = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Email must be unique");
        }

        return userMapper.toDto(saved);
    }

    @Override
    public List<UserDto> findAllByIds(List<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> findAll(Integer from, Integer size) {
        Sort idAscSort = Sort.by("id").ascending();
        Pageable offsetLimit = QueryUtils.offsetLimit(from, size, idAscSort);

        return userRepository.findAll(offsetLimit)
                .map(userMapper::toDto)
                .getContent();
    }

    @Override
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found by id: " + userId);
        }

        userRepository.deleteById(userId);
    }
}
