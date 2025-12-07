package com.starone.bookshow.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.starone.bookshow.user.dto.UserDto;
import com.starone.bookshow.user.entity.User;
import com.starone.bookshow.user.enums.EProvider;
import com.starone.bookshow.user.utils.UserMapper;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setup() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void test_mapping() {
        assertEquals(1, 1);
    }

    @Test
    void givenUser_whenMappedToDto_thenReturnsCorrectDto() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .userId("12345")
                .name("John Doe")
                .email("john.doe@example.com")
                .phoneNo("9876543210")
                .password("securePassword123")
                .provider(EProvider.GOOGLE)
                .providerId("google-12345")
                .emailVerified(true)
                .phoneVerified(false)
                .favoriteGenres(Arrays.asList("Action", "Sci-Fi"))
                .preferredCinemas(Arrays.asList("Cinema 1", "Cinema 2"))
                .createdAt(now)
                .updatedAt(now)
                .build();
        System.out.println("Before :"+user.isEmailVerified());
        // Act
        UserDto userDto = userMapper.toUserDto(user);
        System.out.println("Before :"+userDto.isEmailVerified());
        // Assert
        assertThat(userDto.getUserId()).isEqualTo("12345");
        assertThat(userDto.getName()).isEqualTo("John Doe");
        assertThat(userDto.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(userDto.getPhoneNo()).isEqualTo("9876543210");
        assertThat(userDto.getPassword()).isEqualTo("securePassword123");
        assertThat(userDto.getProvider()).isEqualTo(EProvider.GOOGLE);
        assertThat(userDto.getProviderId()).isEqualTo("google-12345");
        assertThat(userDto.isEmailVerified()).isTrue();
        assertThat(userDto.isPhoneVerified()).isFalse();
        assertThat(userDto.getFavoriteGenres()).containsExactly("Action", "Sci-Fi");
        assertThat(userDto.getPreferredCinemas()).containsExactly("Cinema 1", "Cinema 2");
        assertThat(userDto.getCreatedAt()).isEqualTo(now);
        assertThat(userDto.getUpdatedAt()).isEqualTo(now);
    }
}
