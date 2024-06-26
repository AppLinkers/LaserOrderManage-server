package com.laser.ordermanage.user.dto.response;

import com.laser.ordermanage.user.domain.type.SignupMethod;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record GetUserAccountResponse(
        String email,
        String name,
        String phone,
        String zipCode,
        String address,
        String detailAddress,
        Boolean emailNotification,
        SignupMethod signupMethod
) {
    @QueryProjection
    public GetUserAccountResponse(String email, String name, String phone, String zipCode, String address, String detailAddress, Boolean emailNotification, SignupMethod signupMethod) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.zipCode = zipCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.emailNotification = emailNotification;
        this.signupMethod = signupMethod;
    }
}
