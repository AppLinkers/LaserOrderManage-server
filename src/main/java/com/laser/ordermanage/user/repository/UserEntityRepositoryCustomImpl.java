package com.laser.ordermanage.user.repository;

import com.laser.ordermanage.customer.domain.QCustomer;
import com.laser.ordermanage.customer.dto.response.CustomerGetUserAccountResponse;
import com.laser.ordermanage.customer.dto.response.QCustomerGetUserAccountResponse;
import com.laser.ordermanage.factory.domain.QFactory;
import com.laser.ordermanage.factory.dto.response.FactoryGetUserAccountResponse;
import com.laser.ordermanage.factory.dto.response.QFactoryGetUserAccountResponse;
import com.laser.ordermanage.user.dto.response.GetUserEmailResponse;
import com.laser.ordermanage.user.dto.response.QGetUserEmailResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.laser.ordermanage.customer.domain.QCustomer.customer;
import static com.laser.ordermanage.factory.domain.QFactory.factory;
import static com.laser.ordermanage.user.domain.QUserEntity.userEntity;

@RequiredArgsConstructor
public class UserEntityRepositoryCustomImpl implements UserEntityRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GetUserEmailResponse> findEmailByNameAndPhone(String name, String phone) {
        List<GetUserEmailResponse> getUserEmailResponseList = queryFactory
                .select(new QGetUserEmailResponse(
                        new CaseBuilder()
                                .when(factory.isNotNull())
                                .then(factory.companyName)
                                .otherwise(customer.name),
                        userEntity.email
                ))
                .from(userEntity)
                .leftJoin(factory).on(factory.user.id.eq(userEntity.id))
                .leftJoin(customer).on(customer.user.id.eq(userEntity.id))
                .where(
                        eqName(factory, customer, name),
                        userEntity.phone.eq(phone)
                )
                .orderBy(userEntity.createdAt.desc())
                .fetch();

        return getUserEmailResponseList;
    }

    @Override
    public FactoryGetUserAccountResponse findUserAccountByFactory(String email) {
        FactoryGetUserAccountResponse factoryGetUserAccountResponse = queryFactory
                .select(new QFactoryGetUserAccountResponse(
                        userEntity.email,
                        factory.companyName,
                        factory.representative,
                        userEntity.phone,
                        factory.fax,
                        userEntity.zipCode,
                        userEntity.address,
                        userEntity.detailAddress,
                        userEntity.emailNotification
                ))
                .from(userEntity)
                .leftJoin(factory).on(factory.user.id.eq(userEntity.id))
                .where(userEntity.email.eq(email))
                .fetchOne();

        return factoryGetUserAccountResponse;
    }

    @Override
    public CustomerGetUserAccountResponse findUserAccountByCustomer(String email) {
        CustomerGetUserAccountResponse customerGetUserAccountResponse = queryFactory
                .select(new QCustomerGetUserAccountResponse(
                        userEntity.email,
                        customer.name,
                        userEntity.phone,
                        userEntity.zipCode,
                        userEntity.address,
                        userEntity.detailAddress,
                        customer.companyName,
                        userEntity.emailNotification
                ))
                .from(userEntity)
                .leftJoin(customer).on(customer.user.id.eq(userEntity.id))
                .where(userEntity.email.eq(email))
                .fetchOne();

        return customerGetUserAccountResponse;
    }

    private BooleanBuilder eqName(QFactory factory, QCustomer customer, String name) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

            booleanBuilder.or(factory.companyName.eq(name));

            booleanBuilder.or(customer.name.eq(name));

        return booleanBuilder;
    }
}
