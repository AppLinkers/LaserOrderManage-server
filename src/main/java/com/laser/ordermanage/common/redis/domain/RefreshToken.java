package com.laser.ordermanage.common.redis.domain;

import com.laser.ordermanage.common.config.ExpireTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "refresh", timeToLive = ExpireTime.REFRESH_TOKEN_EXPIRE_TIME_FOR_REDIS_AND_COOKIE)
public class RefreshToken {

    @Id
    private String id;

    private String ip;

    private Collection<? extends GrantedAuthority> authorities;

    @Indexed
    private String refreshToken;
}