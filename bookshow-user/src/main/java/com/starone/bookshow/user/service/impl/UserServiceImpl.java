package com.starone.bookshow.user.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.starone.bookshow.user.dto.UserDto;
import com.starone.bookshow.user.entity.User;
import com.starone.bookshow.user.exception.custom.InvalidInputException;
import com.starone.bookshow.user.exception.custom.ResourceNotFoundException;
import com.starone.bookshow.user.repository.IUserRepository;
import com.starone.bookshow.user.service.IUserService;
import com.starone.bookshow.user.utils.UserConstants;
import com.starone.bookshow.user.utils.UserMapper;
import com.starone.bookshow.user.utils.UserUtils;

@Service("userService")
public class UserServiceImpl implements IUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper mapper;
    private final IUserRepository userRepository;

    public UserServiceImpl(UserMapper mapper, IUserRepository userRepository) {
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (UserUtils.isNull(userDto)) {
            LOGGER.warn("Failed to save User! User must not be null");
            throw new InvalidInputException("User");
        }
        User savedUser = userRepository.save(mapper.toUser(userDto));

        LOGGER.info("User saved successfully");
        return mapper.toUserDto(savedUser);
    }

    @Override
    public UserDto updateUserById(String userId, UserDto userDto) {
        if (UserUtils.isNullOrEmpty(userId) || UserUtils.isNull(userDto)) {
            LOGGER.warn("Failed to update User! userId or User must not be null");
            throw new InvalidInputException("User or userId");
        }
        User existingUser = userRepository.findById(userId).orElseThrow(() -> {
            LOGGER.warn("Failed to get User! User not found.");
            return new ResourceNotFoundException("User", userId);
        });
        LOGGER.debug("userDto mapping with existing User");
        mapper.updateExistingUser(userDto, existingUser);
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);

        LOGGER.info("User updated successfully.");
        return mapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        if (UserUtils.isNullOrEmpty(email)) {
            LOGGER.warn("Failed to get User! Email must not be null");
            throw new InvalidInputException("Email");
        }
        User existingUser = userRepository.findByEmail(email).orElseThrow(() -> {
            LOGGER.warn(UserConstants.USER_NOT_FOUND_FAILED_TO_GET);
            return new ResourceNotFoundException("User", email);
        });
        LOGGER.info(UserConstants.USER_FETCHED_SUCCESS);
        return mapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserByPhoneNo(String phoneNo) {
        if (UserUtils.isNullOrEmpty(phoneNo)) {
            LOGGER.warn("Failed to get User! Phone Number must not be null");
            throw new InvalidInputException("UserId");
        }
        User existingUser = userRepository.findByPhoneNo(phoneNo).orElseThrow(() -> {
            LOGGER.warn(UserConstants.USER_NOT_FOUND_FAILED_TO_GET);
            return new ResourceNotFoundException("User", phoneNo);
        });
        LOGGER.info(UserConstants.USER_FETCHED_SUCCESS);
        return mapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(String userId) {
        if (UserUtils.isNullOrEmpty(userId)) {
            LOGGER.warn("Failed to get User! UserId must not be null");
            throw new InvalidInputException("UserId");
        }
        User existingUser = userRepository.findById(userId).orElseThrow(() -> {
            LOGGER.warn(UserConstants.USER_NOT_FOUND_FAILED_TO_GET);
            return new ResourceNotFoundException("User", userId);
        });
        LOGGER.info(UserConstants.USER_FETCHED_SUCCESS);
        return mapper.toUserDto(existingUser);
    }

    @Override
    public List<UserDto> getAllUser() {
        return userRepository.findAll()
                .stream()
                .map(mapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUserById(String userId) {
        if (UserUtils.isNullOrEmpty(userId)) {
            LOGGER.warn("Failed to delete User! UserId must not be null");
            throw new InvalidInputException("UserId");
        }
        userRepository.deleteById(userId);
        LOGGER.info("User deleted successfully!");
    }

}
