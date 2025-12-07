package com.starone.bookshow.user.service;

import java.util.List;

import com.starone.bookshow.user.dto.UserDto;

public interface IUserService {

    public UserDto createUser(UserDto userDto);

    public UserDto updateUserById(String userId, UserDto userDto);

    public UserDto getUserByEmail(String email);

    public UserDto getUserByPhoneNo(String phoneNo);

    public UserDto getUserById(String userId);

    public List<UserDto> getAllUser();

    public void deleteUserById(String userId);
}
