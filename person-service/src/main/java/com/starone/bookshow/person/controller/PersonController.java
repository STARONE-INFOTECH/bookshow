package com.starone.bookshow.person.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.starone.common.request.ApiResponses;
import com.starone.common.response.record.ApiResponse;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.PersonProfessionAddition;
import com.starone.common.response.record.PersonResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {
    private static final Logger log = LoggerFactory.getLogger(PersonController.class);
    private final IPersonService personService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PersonResponse> create(@Valid @RequestBody PersonRequestDto requestDto) {
        log.info("Received DTO - pAddress: {}, cAddress: {}", requestDto.getPAddress(), requestDto.getCAddress());
        PersonResponse response = personService.create(requestDto);
        return ApiResponses.success(response);
    }

    @PostMapping("/professions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> addProfessionsToPersons(@Valid @RequestBody List<PersonProfessionAddition> bulkUpdateDto) {
        log.info("Received DTO with :{} no of ids", bulkUpdateDto.size());
        personService.addProfessionsToPersons(bulkUpdateDto);
        return ApiResponses.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<PersonResponse> getById(@PathVariable("id") UUID id) {
        PersonResponse response = personService.getById(id);
        return ApiResponses.success(response);
    }

    @GetMapping("/credit-info/{id}")
    public ApiResponse<MovieCreditPersonResponse> getPersonById(@PathVariable("id") UUID id) {
        MovieCreditPersonResponse existingPerson = personService.getPersonById(id);
        return ApiResponses.success(existingPerson);
    }

    @PostMapping("/by-ids")
    public ApiResponse<List<MovieCreditPersonResponse>> getAllByIds(@RequestBody Set<UUID> ids) {
        List<MovieCreditPersonResponse> existingPersons = personService.getAllByIds(ids);
        return ApiResponses.success(existingPersons);
    }

    @PatchMapping("/{id}")
    public ApiResponse<PersonResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody PersonRequestDto requestDto) {
        PersonResponse response = personService.update(id, requestDto);
        return ApiResponses.success(response);
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<PersonResponse> deactivate(@PathVariable("id") UUID id) {
        PersonResponse response = personService.deactivate(id);
        return ApiResponses.success(response);
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<PersonResponse> activate(@PathVariable("id") UUID id) {
        PersonResponse response = personService.activate(id);
        return ApiResponses.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<Page<PersonResponse>> searchByName(
            @RequestParam("name") String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<PersonResponse> page = personService.searchByName(name, pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/active")
    public ApiResponse<Page<PersonResponse>> getAllActive(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<PersonResponse> page = personService.getAllActive(pageable);
        return ApiResponses.success(page);
    }

    @GetMapping
    public ApiResponse<Page<PersonResponse>> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<PersonResponse> page = personService.getAll(pageable);
        return ApiResponses.success(page);
    }

    /*
     * =====================================================================
     * ------ Internal Service usable endpoints by using Feign client ------
     * =====================================================================
     */

    @PostMapping("/internal/professions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addProfessionsToPersonsInternal(@Valid @RequestBody List<PersonProfessionAddition> personProfessions) {
        log.info("Received DTO with :{} no of ids", personProfessions.size());
        personService.addProfessionsToPersons(personProfessions);
        log.info("New profession(s) added successfully with :{} no. of person ids", personProfessions.size());
    }

    @PostMapping("/internal/by-ids")
    public List<MovieCreditPersonResponse> getAllByIdsInternal(@RequestBody Set<UUID> ids) {
        return personService.getAllByIds(ids);
    }

    @GetMapping("/internal/credit-info/{id}")
    public MovieCreditPersonResponse getPersonByIdInternal(@PathVariable("id") UUID id) {
        return personService.getPersonById(id);
    }
}
