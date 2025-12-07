package com.starone.bookshow.user.utils;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.starone.bookshow.user.dto.UserDto;
import com.starone.bookshow.user.entity.User;

@Mapper (componentModel = "spring") // Enables Spring integration
public interface UserMapper {

    //UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Mapping Methods
    //@Mapping(target = "emailVerified", source = "emailVerified")
    UserDto toUserDto(User user);

    User toUser(UserDto userDto);

    // Mapping to an existing entity

    @Mapping(target = "userId", ignore = true) // Ignore userId when updating
    void updateExistingUser(UserDto userDto, @MappingTarget User existingUser);

}
