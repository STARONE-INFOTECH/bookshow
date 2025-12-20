package com.starone.bookshow.booking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentConfirmRequestDto {
    private String paymentGatewayId; // transaction ID from gateway (e.g., Razorpay, Stripe)

    private String gatewayStatus; // SUCCESS, FAILED, PENDING (gateway-specific)

    private String gatewayResponse; // raw JSON/string response from gateway (for debugging)

    private String signature; // optional - for verifying webhook authenticity

    private Double amount; // amount charged (for verification)

    private String currency; // e.g., INR

    private String paymentMethod; // CARD, UPI, WALLET, NETBANKING
}
