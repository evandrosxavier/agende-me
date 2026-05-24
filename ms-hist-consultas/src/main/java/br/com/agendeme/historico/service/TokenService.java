package br.com.agendeme.historico.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String getSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("agende-me")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token JWT inválido ou expirado", e);
        }
    }

    public String getClaim(String token, String claim) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("agende-me")
                    .build()
                    .verify(token)
                    .getClaim(claim)
                    .asString();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token JWT inválido ou expirado", e);
        }
    }

    public String getOptionalClaim(String token, String claim) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var decoded = JWT.require(algorithm)
                    .withIssuer("agende-me")
                    .build()
                    .verify(token);
            var c = decoded.getClaim(claim);
            return (c == null || c.isNull() || c.isMissing()) ? null : c.asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}