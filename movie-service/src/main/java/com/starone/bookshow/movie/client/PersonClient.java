package com.starone.bookshow.movie.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.starone.common.dto.PersonResponseDto;

@FeignClient(name = "person-service", url = "${person.service.url:http://localhost:8082}")
public interface PersonClient {

    @GetMapping("/api/v1/persons/{id}")
    PersonResponseDto getPersonById(@PathVariable UUID id);
}
