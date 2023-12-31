package com.laser.ordermanage.customer.service;

import com.laser.ordermanage.common.paging.PageResponse;
import com.laser.ordermanage.customer.dto.response.CustomerGetOrderCreateInformationResponse;
import com.laser.ordermanage.customer.dto.response.CustomerGetOrderHistoryResponse;
import com.laser.ordermanage.customer.dto.response.CustomerGetOrderIsCompletedHistoryResponse;
import com.laser.ordermanage.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerOrderHistoryService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PageResponse<CustomerGetOrderHistoryResponse> getOrderHistory(String email, Pageable pageable, List<String> stageRequestList, List<String> manufacturingRequestList, String query) {
        Page<CustomerGetOrderHistoryResponse> customerGetOrderHistoryResponsePage = orderRepository.findByCustomer(email, pageable, stageRequestList, manufacturingRequestList, query);

        return new PageResponse<>(customerGetOrderHistoryResponsePage);
    }

    @Transactional
    public PageResponse<CustomerGetOrderIsCompletedHistoryResponse> getOrderIsCompletedHistory(String email, Pageable pageable, String query) {
        Page<CustomerGetOrderIsCompletedHistoryResponse> customerGetOrderIsCompletedHistoryResponsePage = orderRepository.findIsCompletedByCustomer(email, pageable, query);

        return new PageResponse<>(customerGetOrderIsCompletedHistoryResponsePage);
    }

    @Transactional
    public CustomerGetOrderCreateInformationResponse getOrderCreateInformation(Long orderId) {
        return orderRepository.findCreateInformationByOrder(orderId);
    }
}
