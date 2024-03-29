package com.laser.ordermanage.order.repository;

import com.laser.ordermanage.order.dto.response.GetCommentResponse;
import com.laser.ordermanage.order.dto.response.QGetCommentResponse;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.laser.ordermanage.order.domain.QComment.comment;
import static com.laser.ordermanage.order.domain.QOrder.order;
import static com.laser.ordermanage.user.domain.QUserEntity.userEntity;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GetCommentResponse> findCommentByOrder(Long orderId) {

        List<GetCommentResponse> getCommentResponseList = queryFactory
                .select(new QGetCommentResponse(
                        comment.id,
                        userEntity.name,
                        comment.content,
                        comment.createdAt
                ))
                .from(comment)
                .leftJoin(comment.user, userEntity)
                .join(comment.order, order)
                .where(order.id.eq(orderId))
                .orderBy(comment.createdAt.asc())
                .fetch();

        return getCommentResponseList;
    }

    @Override
    public void deleteAllByOrder(Long orderId) {
        queryFactory
                .delete(comment)
                .where(comment.order.id.eq(orderId))
                .execute();
    }

    @Override
    public void deleteAllByOrderList(List<Long> orderIdList) {
        queryFactory
                .delete(comment)
                .where(comment.order.id.in(orderIdList))
                .execute();
    }

    @Override
    public void updateCommentUserAsNullByUserAndOrder(String email, List<Long> orderIdList) {
        queryFactory
                .update(comment)
                .setNull(comment.user)
                .where(
                        comment.order.id.in(orderIdList),
                        comment.user.id.eq(
                                JPAExpressions
                                        .select(userEntity.id)
                                        .from(userEntity)
                                        .where(userEntity.email.eq(email))
                        )
                )
                .execute();
    }
}
