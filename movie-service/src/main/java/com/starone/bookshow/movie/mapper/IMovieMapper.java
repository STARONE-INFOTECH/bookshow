package com.starone.bookshow.movie.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.dto.MovieResponseDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface IMovieMapper extends BaseMapper<Movie, MovieRequestDto, MovieRequestDto, MovieResponseDto> {

    // Response: include id and active (not in request DTO)
    @Override
    MovieResponseDto toResponseDto(Movie movie);

    // Create: ignore id and active
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true") // default active on create
    @Override
    Movie toEntity(MovieRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active ", ignore = true)
    @Override
    void updateEntity(MovieRequestDto requestDto, @MappingTarget Movie entity);

}
