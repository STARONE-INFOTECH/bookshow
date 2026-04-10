package com.starone.bookshow.booking.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.starone.common.enums.BookingStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;                     // from auth-service

    @Column(name = "show_id", nullable = false)
    private UUID showId;                     // from show-service

    @Column(nullable = false)
    private LocalDateTime bookingTime;

    private LocalDateTime paymentTime;       // null until paid

    @Column(nullable = false)
    private double totalAmount;

    private double discountAmount = 0.0;      // optional offers

    private double finalAmount;               // total - discount

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;  // PENDING, CONFIRMED, CANCELLED, FAILED

    private String paymentId;                // from payment-service (gateway transaction ID)

    private String ticketQrCodeUrl;          // generated after confirmation

    private String bookingReference;         // user-facing code e.g., BKN-2025-12345

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookingSeat> bookedSeats = new HashSet<>();

    // Optional fields
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}
