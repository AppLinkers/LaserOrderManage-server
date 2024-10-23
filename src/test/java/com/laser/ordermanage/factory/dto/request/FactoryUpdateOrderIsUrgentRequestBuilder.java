package com.laser.ordermanage.factory.dto.request;

public class FactoryUpdateOrderIsUrgentRequestBuilder {
    public static FactoryUpdateOrderIsUrgentRequest build() {
        return new FactoryUpdateOrderIsUrgentRequest(Boolean.TRUE);
    }

    public static FactoryUpdateOrderIsUrgentRequest nullIsUrgentBuild() {
        return new FactoryUpdateOrderIsUrgentRequest(null);
    }
}
