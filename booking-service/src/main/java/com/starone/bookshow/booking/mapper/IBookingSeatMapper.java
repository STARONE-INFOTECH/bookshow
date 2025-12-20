package com.starone.bookshow.booking.mapper;

import org.mapstruct.Mapper;

import com.starone.bookshow.booking.dto.BookingSeatDto;
import com.starone.bookshow.booking.entity.BookingSeat;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;

@Mapper(
    componentModel = "spring",
    config = CommonMapperConfig.class
)
public interface IBookingSeatMapper extends BaseMapper<
        BookingSeat,
        Object,                 // no request DTO (internal)
        Object,
        BookingSeatDto
    > {

    BookingSeatDto toResponseDto(BookingSeat entity);
}