package com.ridehailing.driver.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateDriverRequest(
        @NotBlank String name,
        @NotBlank String phone
) {}
