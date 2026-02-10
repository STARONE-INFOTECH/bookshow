package com.starone.bookshow.theater.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.starone.bookshow.theater.dto.TheaterRequestDto;
import com.starone.bookshow.theater.entity.Theater;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;
import com.starone.springcommon.response.record.TheaterResponse;

@Mapper(
    componentModel = "spring",
    config = CommonMapperConfig.class
)
public interface ITheaterMapper extends BaseMapper<Theater, TheaterRequestDto, TheaterRequestDto, TheaterResponse> {
    // Screens are mapped separately (enriched or lazy)
    @Mapping(target = "screens", ignore = true)  // handled in service enrichment
    @Mapping(target = "id", ignore = true)
    @Override
    Theater toEntity(TheaterRequestDto dto);

    @Override
    TheaterResponse toResponseDto(Theater entity);

    // Partial update
    @Override
    void updateEntity(TheaterRequestDto dto, @MappingTarget Theater entity);
}
