package com.application.minipay.domains;

import com.application.minipay.enums.Currency;
import com.application.minipay.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String serviceAccount;

    @Column(nullable = false, name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    private String failureReason;

    private Instant failureTimestamp;

    @Column(unique = true,nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

}
