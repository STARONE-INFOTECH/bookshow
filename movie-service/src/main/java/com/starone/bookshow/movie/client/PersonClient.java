package com.starone.bookshow.movie.client;

import java.util.Set;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.starone.common.dto.ApiResponse;
import com.starone.common.dto.PersonResponseDto;

@FeignClient(name = "bookshow-person-service", url = "${person.service.url:http://localhost:8081}")
public interface PersonClient {

    @GetMapping("/api/v1/persons/{id}")
    ApiResponse<PersonResponseDto> getPersonById(@PathVariable("id") UUID id);

    @PostMapping("/api/v1/persons/validate")
    ApiResponse<Set<UUID>> validatePersonIds(@RequestBody Set<UUID> ids);
}
