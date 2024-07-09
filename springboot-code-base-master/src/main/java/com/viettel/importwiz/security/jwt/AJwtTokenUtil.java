package com.viettel.importwiz.security.jwt;

import com.viettel.importwiz.entity.Account;
import com.viettel.security.PassTranformer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
@Slf4j
public class AJwtTokenUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String BEARER = "Bearer ";

    @Value("${jwt.secret}")
    private String secret;

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token,
                                   Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(Account account, String ticket,
                                long validTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accountId", account.getAccountId());
        claims.put("username", account.getUsername());
        claims.put("sub", account.getUsername());
        claims.put("email", account.getEmail());
        claims.put("fullName", account.getFullName());
        claims.put("role", account.getRole());
        claims.put("lang", account.getLanguage());
        claims.put("jti", ticket);
        claims.put("auth_token_session_create", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        return doGenerateToken(claims, validTime);
    }

    private String doGenerateToken(Map<String, Object> claims,
                                   long validTimeInSeconds) {
        return AJwtTokenUtil.BEARER + Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + validTimeInSeconds * 1000))
            .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public Boolean validateToken(String token, Account account) {
        final String username = getUsernameFromToken(token);
        return (username.equals(account.getUsername()) && !isTokenExpired(token));
    }

    public Map<String, Object> getClaimsMapFromToken(String token) {
        Map<String, Object> objectMap = new HashMap<>();
        if (token == null)
            return objectMap;

        if (token.startsWith(AJwtTokenUtil.BEARER))
            token = token.replace(AJwtTokenUtil.BEARER, "");

        Claims body =
            Jwts.parser().setSigningKey(getSecretDecrypt()).parseClaimsJws(token).getBody();
        body.forEach((k, v) -> {
            if (!k.equals("role") && !k.equals("exp"))
                objectMap.put(k, v.toString());
            else {
                if (k.equals("role")) {
                    objectMap.put(k, v);
                }
            }
        });
        return objectMap;

    }

    public String getSecretDecrypt() {
        PassTranformer.setInputKey("secret");
        String decrypt = null;
        try {
            decrypt = PassTranformer.decrypt(secret);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        return Optional.ofNullable(decrypt).orElse(secret);
    }
}
