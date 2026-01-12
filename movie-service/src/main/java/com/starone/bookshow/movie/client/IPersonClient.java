package com.starone.bookshow.movie.client;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.PersonProfessionSync;

@FeignClient(name = "bookshow-person-service", url = "${person.service.url:http://localhost:8081}")
public interface IPersonClient {

    @GetMapping("/api/v1/persons/by-ids")
    List<MovieCreditPersonResponse> getAllPersonByIds(@RequestBody Set<UUID> ids);

    @GetMapping("/api/v1/persons/credit-info/{id}")
    MovieCreditPersonResponse getPersonById(@PathVariable("id") UUID id);

    @PatchMapping("/api/v1/persons/bulk-professions")
    void addProfessionsBulk(@RequestBody List<PersonProfessionSync> bulkUpdateDto);

}
