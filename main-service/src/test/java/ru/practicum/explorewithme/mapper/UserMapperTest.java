package ru.practicum.explorewithme.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.dto.user.NewUserRequest;
import ru.practicum.explorewithme.dto.user.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void user_roundtrip() {
        User u = userMapper.toEntity(NewUserRequest.builder().name("ann").email("a@a.com").build());
        u.setId(9L);
        UserDto dto = userMapper.toDto(u);
        assertThat(dto.getId()).isEqualTo(9L);
        assertThat(dto.getName()).isEqualTo("ann");
    }

}
