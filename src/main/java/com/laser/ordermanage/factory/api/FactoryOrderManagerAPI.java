package com.laser.ordermanage.factory.api;

import com.laser.ordermanage.factory.dto.request.FactoryCreateOrUpdateOrderManagerRequest;
import com.laser.ordermanage.factory.service.FactoryOrderManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/factory/order-manager")
@RestController
public class FactoryOrderManagerAPI {

    private final FactoryOrderManagerService factoryOrderManagerService;

    /**
     * 공장 회원의 거래 담당자 생성
     * - 공장 회원의 이메일 기준으로 공장 조회 및 거래 담당자 데이터와 연관관계 매핑
     * - 거래 담당자 데이터 생성
     */
    @PostMapping("")
    public ResponseEntity<?> createOrderManager(@RequestBody @Valid FactoryCreateOrUpdateOrderManagerRequest request) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        factoryOrderManagerService.createOrderManger(user.getUsername(), request);

        return ResponseEntity.ok().build();
    }
}
