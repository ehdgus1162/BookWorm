package com.bookworm.application.dto;

import com.bookworm.domain.validation.annotation.ValidAddress;

@ValidAddress(
        street = "street",
        city = "city",
        state = "state",
        country = "country"
)
public record AddressResponse(
        String street,
        String city,
        String state,
        String country
){


}
