package com.modu.soccer.jwt;

import com.modu.soccer.entity.User;
import com.modu.soccer.enums.MDCKey;
import com.modu.soccer.enums.TokenType;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.utils.LocalDateTimeUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

	private final String ISSUER = "modu_soccer";
	private Key jwtSecretKey;

	@Value("${jwt.secret_key}")
	private String jwtSecretKeyString;
	@Value("${jwt.expire_in.access_token}")
	private Integer accessTokenExpireMillis;
	@Value("${jwt.expire_in.refresh_token}")
	private Integer refreshTokenExpireMillis;

	@PostConstruct()
	private void initialize() {
		jwtSecretKey = getDecodedSecretKey();
	}

	public String createTokenOfType(User user, TokenType tokenType) {
		Date issuedDateTime = LocalDateTimeUtil.toDate(LocalDateTimeUtil.now());
		Date expireDate = issuedDateTime;
		switch (tokenType){
			case AUTH_ACCESS_TOKEN -> {
				expireDate = DateUtils.addMilliseconds(issuedDateTime, accessTokenExpireMillis);
			}
			case AUTH_REFRESH_TOKEN -> {
				expireDate = DateUtils.addMilliseconds(issuedDateTime, refreshTokenExpireMillis);
			}
			default -> throw new IllegalArgumentException("invalid token type");
		}
		return Jwts.builder()
			.setClaims(createClaimsFrom(user))
			.setIssuer(ISSUER)
			.setExpiration(expireDate)
			.setIssuedAt(issuedDateTime)
			.signWith(jwtSecretKey)
			.compact();
	}

	public boolean isTokenExpired(String token) {
		try {
			Claims claims = parseJwt(token);
			MDC.put(MDCKey.USER_ID.getKey(), claims.get("user_id").toString());
			return false;
		} catch (ExpiredJwtException e) {
			MDC.put(MDCKey.USER_ID.getKey(), e.getClaims().get("user_id").toString());
		}
		return true;
	}

	public String getJwtTokenFromHeader(String header) {
		if (header == null || !header.startsWith("Bearer ")){
			log.info("header[{}] is invalid", header);
			throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
		}
		return header.substring("Bearer ".length());
	}

	private Key getDecodedSecretKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretKeyString));
	}
	private Map<String, String> createClaimsFrom(User user) {
		Map<String, String> claims = new HashMap<>();
		claims.put("user_id", user.getId().toString());
		claims.put("email", user.getEmail());
		return claims;
	}

	private Claims parseJwt(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(jwtSecretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}
}
