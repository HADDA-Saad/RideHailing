package com.ridehailing.rider.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateRiderRequest(
        @NotBlank String name,
        @NotBlank String phone
) {}
