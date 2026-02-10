package com.starone.bookshow.show.mapper;

import org.mapstruct.Mapper;

import com.starone.bookshow.show.entity.ShowSeat;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;
import com.starone.springcommon.response.record.ShowSeatResponse;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface IShowSeatMapper extends BaseMapper<ShowSeat, Object, // no request DTO (internal creation)
        Object, ShowSeatResponse> {

    ShowSeatResponse toResponseDto(ShowSeat entity);

    // List mapping automatic via BaseMapper
}
