package com.company.SpringSec.security.jwt;

import com.company.SpringSec.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private static final String JWT_TOKEN_PREFIX = "Bearer";
    private static final String JWT_HEADER_STRING = "Authorization";

    private final PrivateKey jwtPrivateKey;
    private final PublicKey jwtPublicKey;
    private final UserDetailsService userDetailsService;

    public JwtProvider(@Value("${authentication.jwt.private-key}") String jwtPrivateKeyStr,
                       @Value("${authentication.jwt.public-key}") String jwtPublicKeyStr,
                       UserDetailsService userDetailsService){
        this.userDetailsService = userDetailsService;
        KeyFactory keyFactory = getKeyFactory();
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decoder.decode(jwtPrivateKeyStr.getBytes()));
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decoder.decode(jwtPublicKeyStr.getBytes()));

            jwtPrivateKey = keyFactory.generatePrivate(privateKeySpec);
            jwtPublicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e){
            throw new RuntimeException("Invalid key specification",e);
        }
    }



    public Authentication getAuthentication(HttpServletRequest request){
        String token = resolveToken(request);
        if (token == null){
            return null;
        }
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtPublicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return username != null ?
                new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities()) : null;
    }

    public boolean isTokenValid(HttpServletRequest request){
        String token = resolveToken(request);
        if (token == null){
            return false;
        }
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtPublicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return !claims.getExpiration().before(new Date());
    }

    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(JWT_HEADER_STRING);
        if (bearerToken !=null && bearerToken.startsWith(JWT_TOKEN_PREFIX)){
            return bearerToken.substring(7);
        }
        return null;
    }

    private KeyFactory getKeyFactory(){
        try {
            return KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e){
            throw  new RuntimeException("Unknown key generation algorithm",e);
        }
    }

}

