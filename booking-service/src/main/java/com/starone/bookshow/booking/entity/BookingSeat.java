package com.starone.bookshow.booking.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "booking_seats")
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "show_seat_id")
    private UUID showSeatId;                 // reference to ShowSeat (for cleanup if cancelled)

    private String seatNumber;               // e.g., A1

    private String seatType;                 // NORMAL, PREMIUM, RECLINER

    private String priceCategory;            // SILVER, GOLD, PLATINUM

    private double seatPrice;
}
