package com.laser.ordermanage.order.service;

import com.laser.ordermanage.common.dto.response.PageRes;
import com.laser.ordermanage.customer.dto.response.GetOrderRes;
import com.laser.ordermanage.factory.dto.response.GetNewIssueNewOrderRes;
import com.laser.ordermanage.factory.dto.response.GetReIssueNewOrderRes;
import com.laser.ordermanage.order.repository.OrderRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderReadService {

    private final OrderRepositoryCustom orderRepositoryCustom;

    @Transactional(readOnly = true)
    public PageRes<GetOrderRes> readByCustomer(String userName, Pageable pageable, List<String> stageRequestList, List<String> manufacturingRequestList, String query) {
        Page<GetOrderRes> orderPage = orderRepositoryCustom.findByCustomer(userName, pageable, stageRequestList, manufacturingRequestList, query);

        return new PageRes<>(orderPage);
    }

    public PageRes<GetReIssueNewOrderRes> readReIssueNewByFactory(String userName, Pageable pageable, Boolean hasQuotation, Boolean isUrgent) {
        Page<GetReIssueNewOrderRes> reIssueNewOrderPage = orderRepositoryCustom.findReIssueNewByFactory(userName, pageable, hasQuotation, isUrgent);

        return new PageRes<>(reIssueNewOrderPage);
    }

    public PageRes<GetNewIssueNewOrderRes> readNewIssueNewByFactory(String userName, Pageable pageable, Boolean hasQuotation, Boolean isNewCustomer, Boolean isUrgent) {
        Page<GetNewIssueNewOrderRes> newIssueNewOrderPage = orderRepositoryCustom.findNewIssueNewByFactory(userName, pageable, hasQuotation, isNewCustomer, isUrgent);

        return new PageRes<>(newIssueNewOrderPage);
    }
}
