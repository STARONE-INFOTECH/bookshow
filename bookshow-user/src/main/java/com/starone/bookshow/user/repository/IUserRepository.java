package com.starone.bookshow.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.starone.bookshow.user.entity.User;

public interface IUserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNo(String phoneNo);
}
