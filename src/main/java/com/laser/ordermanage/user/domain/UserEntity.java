package com.laser.ordermanage.user.domain;

import com.laser.ordermanage.common.converter.BooleanToYNConverter;
import com.laser.ordermanage.common.entity.CreatedAtEntity;
import com.laser.ordermanage.user.domain.type.Role;
import com.laser.ordermanage.user.dto.request.UpdateUserAccountRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "user_table")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserEntity extends CreatedAtEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role", nullable = false, updatable = false)
    private Role role;

    @Column(name = "phone", nullable = false, length = 11)
    private String phone;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "email_notification", nullable = false, length = 1)
    private Boolean emailNotification = Boolean.TRUE;

    @Builder
    public UserEntity(String email, String password, Role role, String phone, String zipCode, String address, String detailAddress) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.zipCode = zipCode;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> response = new ArrayList<>();
        response.add(new SimpleGrantedAuthority(role.name()));

        return response;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeEmailNotification(Boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    public void updateProperties(UpdateUserAccountRequest request) {
        this.phone = request.phone();
        this.zipCode = request.zipCode();
        this.address = request.address();
        this.detailAddress = request.detailAddress();
    }
}
