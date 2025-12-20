package com.starone.bookshow.theater.mapper;

import org.mapstruct.Mapper;

import com.starone.bookshow.theater.dto.ScreenRequestDto;
import com.starone.bookshow.theater.entity.Screen;
import com.starone.common.dto.ScreenResponseDto;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface IScreenMapper extends BaseMapper<Screen, ScreenRequestDto, ScreenRequestDto, ScreenResponseDto> {
    @Override
    ScreenResponseDto toResponseDto(Screen entity);

    @Override
    Screen toEntity(ScreenRequestDto createDto);

    @Override
    void updateEntity(ScreenRequestDto updateDto, Screen entity);
}
