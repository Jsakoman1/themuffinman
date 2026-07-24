package com.themuffinman.app.things.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ThingWishlistRequestDTO(@NotNull List<Long> sharedCircleIds) {
}
