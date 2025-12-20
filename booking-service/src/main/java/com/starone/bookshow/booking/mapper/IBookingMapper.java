package com.starone.bookshow.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.starone.bookshow.booking.dto.BookingRequestDto;
import com.starone.bookshow.booking.dto.BookingResponseDto;
import com.starone.bookshow.booking.entity.Booking;
import com.starone.common.mapper.BaseMapper;
import com.starone.common.mapper.CommonMapperConfig;

@Mapper(
    componentModel = "spring",
    config = CommonMapperConfig.class,
    uses = { IBookingSeatMapper.class }
)
public interface IBookingMapper extends BaseMapper<
        Booking,                     // Entity
        BookingRequestDto,           // Create & Update (same)
        BookingRequestDto,
        BookingResponseDto           // Response
    > {

    // Create: ignore generated/auto fields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookingTime", ignore = true)
    @Mapping(target = "paymentTime", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "finalAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "ticketQrCodeUrl", ignore = true)
    @Mapping(target = "bookingReference", ignore = true)
    @Mapping(target = "bookedSeats", ignore = true)
    @Override
    Booking toEntity(BookingRequestDto dto);

    // Response: ignore enriched fields (filled in service)
    @Mapping(target = "movieTitle", ignore = true)
    @Mapping(target = "theaterName", ignore = true)
    @Mapping(target = "screenName", ignore = true)
    @Mapping(target = "showStartTime", ignore = true)
    @Mapping(target = "showType", ignore = true)
    @Override
    BookingResponseDto toResponseDto(Booking entity);

    // Partial update (for internal status/payment updates if needed)
    @Override
    void updateEntity(BookingRequestDto dto, @MappingTarget Booking entity);
}