package com.starone.bookshow.movie.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.dto.MovieCreditResponseDto;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface IMovieCreditMapper
        extends BaseMapper<MovieCredit, MovieCreditRequestDto, MovieCreditRequestDto, MovieCreditResponseDto> {

    // Basic mapping - fields match directly
    @Override
    MovieCreditResponseDto toResponseDto(MovieCredit entity);
    
    // Ignore fields not in request DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "movie", ignore = true) // set manually in service
    @Override
    MovieCredit toEntity(MovieCreditRequestDto createDto);

    // Partial update - ignores nulls automatically
    @Override
        void updateEntity(MovieCreditRequestDto updateDto, @MappingTarget MovieCredit entity);
}
