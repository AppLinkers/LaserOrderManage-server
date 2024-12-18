package com.laser.ordermanage.common.security.jwt.component;

import com.laser.ordermanage.common.cache.redis.repository.BlackListRedisRepository;
import com.laser.ordermanage.common.constants.ExpireTime;
import com.laser.ordermanage.common.exception.CustomCommonException;
import com.laser.ordermanage.user.domain.UserEntity;
import com.laser.ordermanage.user.dto.response.TokenInfoResponse;
import com.laser.ordermanage.user.exception.UserErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TYPE = "Bearer";
    private static final String AUTHORITY_KEY = "authority";
    private static final String TYPE_KEY = "type";

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String TYPE_CHANGE_PASSWORD = "changePassword";

    private final BlackListRedisRepository blackListRedisRepository;
    private final Key key;

    public JwtProvider(@Value("${jwt.secret.key}") String secretKey, BlackListRedisRepository blackListRedisRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.blackListRedisRepository = blackListRedisRepository;
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * email, authorityList 을 가지고 AccessToken, RefreshToken 을 생성
     */
    public TokenInfoResponse generateToken(String email, List<String> authorityList) {

        Date now = new Date();

        // Access JWT Token 생성
        String accessToken = generateJWT(email, authorityList, TYPE_ACCESS, now, ExpireTime.ACCESS_TOKEN_EXPIRE_TIME);

        // Refresh JWT Token 생성
        String refreshToken = generateJWT(email, authorityList, TYPE_REFRESH, now, ExpireTime.REFRESH_TOKEN_EXPIRE_TIME);

        return TokenInfoResponse.builder()
                .authorityList(authorityList)
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpirationTime(ExpireTime.ACCESS_TOKEN_EXPIRE_TIME)
                .refreshToken(refreshToken)
                .refreshTokenExpirationTime(ExpireTime.REFRESH_TOKEN_EXPIRE_TIME)
                .build();
    }

    /**
     * 사용자 정보를 활용하여 비밀번호 변경 인증 토큰 생성
     */
    public String generateChangePasswordToken(UserEntity user) {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<String> authorityList = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return generateJWT(user.getEmail(), authorityList, TYPE_CHANGE_PASSWORD, new Date(), ExpireTime.CHANGE_PASSWORD_TOKEN_EXPIRE_TIME);
    }

    /**
     * JWT 생성
     */
    public String generateJWT(String subject, List<String> authorityList, String type, Date issuedAt, long expireTime) {
        return Jwts.builder()
                .setSubject(subject)
                .claim(AUTHORITY_KEY, authorityList)
                .claim(TYPE_KEY, type)
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + expireTime)) //토큰 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Jwt 토큰을 복호화
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * JWT 토큰을 복호화하여 토큰에 들어있는 정보를 추출하여 Authentication 생성
     */
    public Authentication getAuthentication(String jwtToken) {
        // 토큰 복호화
        Claims claims = parseClaims(jwtToken);

        // 클레임에서 권한 정보 가져오기
        List<String> authorityList = claims.get(AUTHORITY_KEY, List.class);
        List<GrantedAuthority> authorities = authorityList.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * JWT 검증 수행
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            List<String> authorityList = claims.get(AUTHORITY_KEY, List.class);
            if (authorityList.isEmpty()) {
                throw new CustomCommonException(UserErrorCode.UNAUTHORIZED_JWT);
            }

            // access token 이 black list 에 저장되어 있는지 확인
            if (getType(token).equals(TYPE_ACCESS) && blackListRedisRepository.findByAccessToken(token).isPresent()) {
                throw new CustomCommonException(UserErrorCode.INVALID_ACCESS_TOKEN);
            }

            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomCommonException(UserErrorCode.EXPIRED_JWT);
        } catch (UnsupportedJwtException e) {
            throw new CustomCommonException(UserErrorCode.UNSUPPORTED_JWT);
        } catch (CustomCommonException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomCommonException(UserErrorCode.INVALID_JWT);
        }
    }

    /**
     * JWT 타입 검증
     */
    public void validateTokenType(String token, String tokenType) {
        if (!getType(token).equals(tokenType)) {
            throw new CustomCommonException(UserErrorCode.INVALID_TOKEN_TYPE);
        }
    }

    /**
     * JWT 잔여 유효기간
     */
    public Long getExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    /**
     * JWT 타입 추출
     */
    public String getType(String token) {
        return (String) parseClaims(token).get(TYPE_KEY);
    }

    /**
     * Request Header 에서 토큰 정보 추출
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TYPE)) {
            try {
                return bearerToken.substring(7);
            } catch (StringIndexOutOfBoundsException e) {
                throw new CustomCommonException(UserErrorCode.MISSING_JWT);
            }
        }

        return null;
    }
}
