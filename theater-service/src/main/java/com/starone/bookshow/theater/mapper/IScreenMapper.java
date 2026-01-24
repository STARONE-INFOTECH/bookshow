package com.starone.bookshow.theater.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.starone.bookshow.theater.dto.ScreenRequestDto;
import com.starone.bookshow.theater.entity.Screen;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;
import com.starone.common.response.record.ScreenResponse;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface IScreenMapper extends
        BaseMapper<Screen, ScreenRequestDto, ScreenRequestDto, ScreenResponse> {

    @Override
    Screen toEntity(ScreenRequestDto createDto);

    @Override
    ScreenResponse toResponseDto(Screen entity);

    @Override
    void updateEntity(ScreenRequestDto updateDto, @MappingTarget Screen entity);

}
