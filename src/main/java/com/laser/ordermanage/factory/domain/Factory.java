package com.laser.ordermanage.factory.domain;

import com.laser.ordermanage.factory.dto.request.FactoryUpdateFactoryAccountRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "factory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "company_name", nullable = false, length = 20)
    private String companyName;

    @Column(name = "representative", nullable = false, length = 10)
    private String representative;

    @Column(name = "fax", length = 11)
    private String fax;

    @Builder
    public Factory(String companyName, String representative, String fax) {
        this.companyName = companyName;
        this.representative = representative;
        this.fax = fax;
    }

    public void updateProperties(FactoryUpdateFactoryAccountRequest request) {
        this.companyName = request.companyName();
        this.representative = request.representative();
        this.fax = request.fax();
    }
}
