package com.starone.bookshow.person.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.starone.bookshow.person.dto.PersonRequestDto;
import com.starone.bookshow.person.entity.Person;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;
import com.starone.springcommon.response.record.MovieCreditPersonResponse;
import com.starone.springcommon.response.record.PersonResponse;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface IPersonMapper extends BaseMapper<Person, PersonRequestDto, PersonRequestDto, PersonResponse> {
    // Automatic mapping for most fields (names match perfectly)
    // Response: include id and active (not in request DTO)
    @Override
    PersonResponse toResponseDto(Person person);

    MovieCreditPersonResponse toMovieCreditResponseDto(Person person);

    List<MovieCreditPersonResponse> toMovieCreditResponseDtos(List<Person> persons);

    // Create: ignore id and active
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true") // default active on create
    @Override
    Person toEntity(PersonRequestDto dto);

    // Update: uses updateEntity() from BaseMapper - ignores nulls automatically
    // No override needed unless special logic
}
