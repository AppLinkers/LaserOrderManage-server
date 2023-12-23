package com.laser.ordermanage.factory.service;

import com.laser.ordermanage.common.exception.CustomCommonException;
import com.laser.ordermanage.common.exception.ErrorCode;
import com.laser.ordermanage.common.paging.ListResponse;
import com.laser.ordermanage.factory.domain.Factory;
import com.laser.ordermanage.factory.domain.OrderManager;
import com.laser.ordermanage.factory.dto.request.FactoryCreateOrUpdateOrderManagerRequest;
import com.laser.ordermanage.factory.dto.response.FactoryGetOrderManagerResponse;
import com.laser.ordermanage.factory.repository.FactoryRepository;
import com.laser.ordermanage.factory.repository.OrderManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FactoryOrderManagerService {

    private final FactoryRepository factoryRepository;
    private final OrderManagerRepository orderManagerRepository;

    private String getUserEmailByOrderManager(Long orderManagerId) {
        return orderManagerRepository.findUserEmailById(orderManagerId).orElseThrow(() -> new CustomCommonException(ErrorCode.NOT_FOUND_ENTITY, "orderManager"));
    }

    @Transactional
    public void createOrderManger(String email, FactoryCreateOrUpdateOrderManagerRequest request) {
        Factory factory = factoryRepository.findFirstByUserEmail(email);

        OrderManager orderManager = OrderManager.builder()
                .factory(factory)
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        orderManagerRepository.save(orderManager);
    }

    @Transactional(readOnly = true)
    public ListResponse<FactoryGetOrderManagerResponse> getOrderManagerList(String email) {
        return new ListResponse<>(orderManagerRepository.findByFactory(email));
    }

    @Transactional(readOnly = true)
    public OrderManager getOrderManager(Long orderManagerId) {
        return orderManagerRepository.findFirstById(orderManagerId).orElseThrow(() -> new CustomCommonException(ErrorCode.NOT_FOUND_ENTITY, "orderManager"));
    }

    @Transactional
    public void updateOrderManager(Long orderManagerId, FactoryCreateOrUpdateOrderManagerRequest request) {
        OrderManager orderManager = this.getOrderManager(orderManagerId);

        orderManager.updateProperties(request);
    }

    @Transactional(readOnly = true)
    public void checkAuthorityFactoryOfOrderManager(User user, Long orderManagerId) {
        if (this.getUserEmailByOrderManager(orderManagerId).equals(user.getUsername())) {
            return;
        }

        throw new CustomCommonException(ErrorCode.DENIED_ACCESS_TO_ENTITY, "orderManager");
    }
}
