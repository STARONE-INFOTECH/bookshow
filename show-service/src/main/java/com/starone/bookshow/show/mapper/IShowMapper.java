package com.starone.bookshow.show.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.starone.bookshow.show.dto.ShowRequestDto;
import com.starone.bookshow.show.entity.Show;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;
import com.starone.springcommon.response.record.ShowResponse;

@Mapper(
    componentModel = "spring",
    config = CommonMapperConfig.class
)
public interface IShowMapper extends BaseMapper<Show, ShowRequestDto, ShowRequestDto, ShowResponse> {
    // Basic fields auto-mapped (movieId, screenId, showStartTime, etc.)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "showEndTime", ignore = true)
    @Mapping(target = "totalSeats", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    @Mapping(target = "active", constant = "true")
     
    @Override
    Show toEntity(ShowRequestDto createDto);

    // Response: ignore enriched fields (filled in service)
    @Mapping(target = "movieTitle", ignore = true)
    @Mapping(target = "moviePosterUrl", ignore = true)
    @Mapping(target = "screenName", ignore = true)
    @Mapping(target = "theaterId", ignore = true)
    @Mapping(target = "theaterName", ignore = true)
    @Mapping(target = "theaterCity", ignore = true)

    @Override
    ShowResponse toResponseDto(Show entity);

    // Partial update
    @Override
    void updateEntity(ShowRequestDto dto, @MappingTarget Show entity);
}
