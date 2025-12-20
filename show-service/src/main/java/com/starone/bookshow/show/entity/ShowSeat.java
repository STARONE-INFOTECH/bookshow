package com.starone.bookshow.show.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.starone.common.enums.SeatStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "show_seats")
public class ShowSeat {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    private String seatNumber;            // e.g., "A1", "B12"

    private String seatType;              // NORMAL, PREMIUM, RECLINER

    private String priceCategory;         // SILVER, GOLD, PLATINUM

    private double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;  // AVAILABLE, LOCKED, BOOKED

    private LocalDateTime lockedAt;       // for temporary lock (10 min timeout)
    private UUID lockedByUserId;          // optional, for lock owner
    private LocalDateTime lockedUntil;    // expiry time (e.g., +10 minutes)

    // Optional: hold reference to booking if booked
    private UUID bookingId;
}
