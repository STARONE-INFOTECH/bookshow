package com.starone.bookshow.person.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.starone.bookshow.person.dto.PersonRequestDto;
import com.starone.bookshow.person.service.IPersonService;
import com.starone.common.dto.ApiResponse;
import com.starone.common.dto.PersonResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {
    private final IPersonService personService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PersonResponseDto> create(@Valid @RequestBody PersonRequestDto requestDto) {
        PersonResponseDto response = personService.create(requestDto);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<PersonResponseDto> getById(@PathVariable UUID id) {
        PersonResponseDto response = personService.getById(id);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{id}")
    public ApiResponse<PersonResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody PersonRequestDto requestDto) {
        PersonResponseDto response = personService.update(id, requestDto);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<PersonResponseDto> deactivate(@PathVariable UUID id) {
        PersonResponseDto response = personService.deactivate(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<PersonResponseDto> activate(@PathVariable UUID id) {
        PersonResponseDto response = personService.activate(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<Page<PersonResponseDto>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<PersonResponseDto> page = personService.searchByName(name, pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/active")
    public ApiResponse<Page<PersonResponseDto>> getAllActive(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<PersonResponseDto> page = personService.getAllActive(pageable);
        return ApiResponse.success(page);
    }

    @GetMapping
    public ApiResponse<Page<PersonResponseDto>> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<PersonResponseDto> page = personService.getAll(pageable);
        return ApiResponse.success(page);
    }
}
