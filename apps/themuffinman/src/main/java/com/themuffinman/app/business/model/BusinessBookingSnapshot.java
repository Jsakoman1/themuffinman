package com.themuffinman.app.business.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "business_booking_snapshot")
public class BusinessBookingSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_booking_id", nullable = false, unique = true)
    private BusinessBooking businessBooking;

    @Column(name = "offering_schema_version", nullable = false)
    private int offeringSchemaVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_mode", nullable = false, length = 40)
    private BusinessOfferingFulfillmentMode fulfillmentMode;

    @Column(nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnTransformer(write = "?::jsonb")
    private String demand = "{}";

    @Column(name = "selected_options", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnTransformer(write = "?::jsonb")
    private String selectedOptions = "{}";

    @Column(name = "price_lines", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnTransformer(write = "?::jsonb")
    private String priceLines = "[]";

    @Column(name = "resource_assignments", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnTransformer(write = "?::jsonb")
    private String resourceAssignments = "[]";

    @Column(name = "capacity_consumption", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnTransformer(write = "?::jsonb")
    private String capacityConsumption = "{}";

    @Column(name = "conditions_snapshot", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnTransformer(write = "?::jsonb")
    private String conditionsSnapshot = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
