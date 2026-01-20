package com.starone.bookshow.movie.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;
import com.starone.common.response.record.MovieCreditResponse;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface IMovieCreditMapper
        extends BaseMapper<MovieCredit, MovieCreditRequestDto, MovieCreditRequestDto, MovieCreditResponse> {

    // Basic mapping - fields match directly
    @Override
    MovieCreditResponse toResponseDto(MovieCredit entity);

    // Ignore fields not in request DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "movie", ignore = true) // set manually in service
    @Override
    MovieCredit toEntity(MovieCreditRequestDto createDto);

    // Partial update - ignores nulls automatically
    @Override
    @Mapping(target = "movie", ignore = true)
    void updateEntity(MovieCreditRequestDto updateDto, @MappingTarget MovieCredit entity);
}
