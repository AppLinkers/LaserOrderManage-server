package com.laser.ordermanage.order.repository;

import com.laser.ordermanage.order.domain.Drawing;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DrawingRepository extends CrudRepository<Drawing, Long>, DrawingRepositoryCustom {
    Optional<Drawing> findFirstById(Long id);

    Integer countByOrderId(Long orderId);
}
