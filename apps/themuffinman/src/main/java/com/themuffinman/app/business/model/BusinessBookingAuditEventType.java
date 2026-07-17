package com.themuffinman.app.business.model;

public enum BusinessBookingAuditEventType {
    CREATED,
    CONFIRMED,
    REJECTED,
    CANCELLED_BY_CUSTOMER,
    CANCELLED_BY_OWNER,
    COMPLETED,
    NO_SHOW,
    RESCHEDULED
}
