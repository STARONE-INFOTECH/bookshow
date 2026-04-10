package com.starone.bookshow.movie.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;
import com.starone.springcommon.response.record.MovieResponse;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface IMovieMapper extends BaseMapper<Movie, MovieRequestDto, MovieRequestDto, MovieResponse> {

    // Response: include id and active (not in request DTO)
    @Override
    MovieResponse toResponseDto(Movie movie);

    // Create: ignore id and active
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true") // default active on create
    @Mapping(target = "movieCredits",ignore = true)
    Movie toEntity(MovieRequestDto requestDto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "movieCredits",ignore = true)
    void updateEntity(MovieRequestDto requestDto, @MappingTarget Movie entity);

}
