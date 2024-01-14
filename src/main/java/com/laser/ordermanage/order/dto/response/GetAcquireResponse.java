package com.laser.ordermanage.order.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record GetAcquireResponse(
        Long id,
        String name,
        String phone,
        String signatureFileName,
        String signatureFileUrl
) {

    @QueryProjection
    public GetAcquireResponse(Long id, String name, String phone, String signatureFileName, String signatureFileUrl) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.signatureFileName = signatureFileName;
        this.signatureFileUrl = signatureFileUrl;
    }
}
