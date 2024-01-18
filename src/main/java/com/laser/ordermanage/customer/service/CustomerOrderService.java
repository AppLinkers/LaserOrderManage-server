package com.laser.ordermanage.customer.service;

import com.laser.ordermanage.common.cloud.aws.S3Service;
import com.laser.ordermanage.common.exception.CustomCommonException;
import com.laser.ordermanage.common.util.FileUtil;
import com.laser.ordermanage.customer.domain.Customer;
import com.laser.ordermanage.customer.domain.DeliveryAddress;
import com.laser.ordermanage.customer.dto.request.*;
import com.laser.ordermanage.customer.dto.response.CustomerCreateOrUpdateOrderPurchaseOrderResponse;
import com.laser.ordermanage.customer.repository.CustomerRepository;
import com.laser.ordermanage.order.domain.*;
import com.laser.ordermanage.order.exception.OrderErrorCode;
import com.laser.ordermanage.order.repository.DrawingRepository;
import com.laser.ordermanage.order.repository.OrderRepository;
import com.laser.ordermanage.order.repository.PurchaseOrderRepository;
import com.laser.ordermanage.order.service.DrawingService;
import com.laser.ordermanage.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerOrderService {

    private final DrawingRepository drawingRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    private final OrderService orderService;
    private final CustomerDeliveryAddressService customerDeliveryAddressService;
    private final DrawingService drawingService;
    private final S3Service s3Service;

    @Transactional
    public void createOrder(User user, CustomerCreateOrderRequest request) {
        Customer customer = customerRepository.findFirstByUserEmail(user.getUsername());

        OrderDeliveryAddress deliveryAddress = OrderDeliveryAddress.ofRequest(request.deliveryAddress());

        OrderManufacturing orderManufacturing = OrderManufacturing.ofRequest(request.manufacturing());
        OrderPostProcessing orderPostProcessing = OrderPostProcessing.ofRequest(request.postProcessing());

        Order order = Order.builder()
                .customer(customer)
                .deliveryAddress(deliveryAddress)
                .name(request.name())
                .imgUrl(request.getOrderImgUrl())
                .manufacturing(orderManufacturing)
                .postProcessing(orderPostProcessing)
                .request(request.request())
                .isNewIssue(request.isNewIssue())
                .build();

        Order createdOrder = orderRepository.save(order);

        List<Drawing> drawingList = new ArrayList<>();
        request.drawingList().forEach(
                drawingRequest -> {
                    drawingList.add(
                            Drawing.builder()
                                    .order(createdOrder)
                                    .fileName(drawingRequest.fileName())
                                    .fileSize(drawingRequest.fileSize())
                                    .fileType(drawingRequest.fileType())
                                    .fileUrl(drawingRequest.fileUrl())
                                    .thumbnailUrl(drawingRequest.thumbnailUrl())
                                    .count(drawingRequest.count())
                                    .ingredient(drawingRequest.ingredient())
                                    .thickness(drawingRequest.thickness())
                                    .build()
                    );
                }
        );

        drawingRepository.saveAll(drawingList);
    }

    @Transactional
    public void updateOrderDeliveryAddress(Long orderId, CustomerUpdateOrderDeliveryAddressRequest request) {
        Order order = orderService.getOrderById(orderId);

        if (!order.enableUpdateDeliveryAddress()) {
            throw new CustomCommonException(OrderErrorCode.INVALID_ORDER_STAGE, order.getStage().getValue());
        }

        DeliveryAddress deliveryAddress = customerDeliveryAddressService.getDeliveryAddress(request.deliveryAddressId());

        order.updateDeliveryAddress(deliveryAddress);
    }

    @Transactional
    public Long createOrderDrawing(Long orderId, CustomerCreateDrawingRequest request) {
        Order order = orderService.getOrderById(orderId);

        if (!order.enableManageDrawing()) {
            throw new CustomCommonException(OrderErrorCode.INVALID_ORDER_STAGE, order.getStage().getValue());
        }

        Drawing drawing = Drawing.builder()
                .order(order)
                .fileName(request.fileName())
                .fileSize(request.fileSize())
                .fileType(request.fileType())
                .fileUrl(request.fileUrl())
                .thumbnailUrl(request.thumbnailUrl())
                .count(request.count())
                .ingredient(request.ingredient())
                .thickness(request.thickness())
                .build();

        Drawing savedDrawing = drawingRepository.save(drawing);

        return savedDrawing.getId();
    }

    @Transactional
    public void updateOrderDrawing(Long orderId, Long drawingId, CustomerUpdateDrawingRequest request) {
        Order order = orderService.getOrderById(orderId);

        if (!order.enableManageDrawing()) {
            throw new CustomCommonException(OrderErrorCode.INVALID_ORDER_STAGE, order.getStage().getValue());
        }

        Drawing drawing = drawingService.getDrawingByOrderAndId(order, drawingId);

        drawing.updateDrawingProperties(request);
    }

    @Transactional
    public void deleteOrderDrawing(Long orderId, Long drawingId) {
        Order order = orderService.getOrderById(orderId);

        if (!order.enableManageDrawing()) {
            throw new CustomCommonException(OrderErrorCode.INVALID_ORDER_STAGE, order.getStage().getValue());
        }

        if (drawingService.countDrawingByOrder(order).equals(1)) {
            throw new CustomCommonException(OrderErrorCode.LAST_DRAWING_DELETE);
        }

        Drawing drawing = drawingService.getDrawingByOrderAndId(order, drawingId);

        drawingRepository.delete(drawing);
    }

    @Transactional(readOnly = true)
    public void checkAuthorityOfOrder(User user, Long orderId) {
        if (!orderService.getUserEmailByOrder(orderId).equals(user.getUsername())) {
            throw new CustomCommonException(OrderErrorCode.DENIED_ACCESS_TO_ORDER);
        }
    }

    @Transactional
    public void approveQuotation(Long orderId) {
        Order order = orderService.getOrderById(orderId);

        if (!order.enableApproveQuotation()) {
            throw new CustomCommonException(OrderErrorCode.INVALID_ORDER_STAGE, order.getStage().getValue());
        }

        if (!order.hasQuotation()) {
            throw new CustomCommonException(OrderErrorCode.NOT_FOUND_QUOTATION);
        }

        order.approveQuotation();
    }

    @Transactional
    public CustomerCreateOrUpdateOrderPurchaseOrderResponse createOrderPurchaseOrder(Long orderId, MultipartFile file, CustomerCreateOrUpdateOrderPurchaseOrderRequest request) {
        Order order = orderService.getOrderById(orderId);

        // 발주서 파일 유무 확인
        if (file == null || file.isEmpty()) {
            throw new CustomCommonException(OrderErrorCode.REQUIRED_PURCHASE_ORDER_FILE);
        }

        String fileName = file.getOriginalFilename();
        Long fileSize = file.getSize();
        String fileType = FileUtil.getExtension(file);

        // 발주서 파일 업로드
        String purchaseOrderFileUrl = uploadPurchaseOrderFile(file);

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .inspectionPeriod(request.inspectionPeriod())
                .inspectionCondition(request.inspectionCondition())
                .paymentDate(request.paymentDate())
                .fileName(fileName)
                .fileSize(fileSize)
                .fileType(fileType)
                .fileUrl(purchaseOrderFileUrl)
                .build();

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        order.createPurchaseOrder(savedPurchaseOrder);

        return CustomerCreateOrUpdateOrderPurchaseOrderResponse.from(savedPurchaseOrder);
    }

    @Transactional
    public CustomerCreateOrUpdateOrderPurchaseOrderResponse updateOrderPurchaseOrder(Long orderId, MultipartFile file, CustomerCreateOrUpdateOrderPurchaseOrderRequest request) {
        Order order = orderService.getOrderById(orderId);
        PurchaseOrder purchaseOrder = order.getPurchaseOrder();

        // 발주서 파일 유무 확인
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            Long fileSize = file.getSize();
            String fileType = FileUtil.getExtension(file);

            // 발주서 파일 업로드
            String purchaseOrderFileUrl = uploadPurchaseOrderFile(file);

            purchaseOrder.updateFile(fileName, fileSize, fileType, purchaseOrderFileUrl);
        }

        purchaseOrder.updateProperties(request);

        return CustomerCreateOrUpdateOrderPurchaseOrderResponse.from(purchaseOrder);
    }

    private String uploadPurchaseOrderFile(MultipartFile file) {
        return s3Service.upload("purchase-order", file);
    }
}
