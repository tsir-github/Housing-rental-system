package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static SecretKey secretKey = Keys.hmacShaKeyFor("Yd58rzddYFJ6jCxH8YYTNtryFpf2DpF7".getBytes());
    public static String createToken(Long userId,String username){

        String jwt= Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))//3600000*24*365L
                .setSubject("LOGIN_USER")
                .claim("userId",userId)
                .claim("username",username)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    public static Claims parseToken(String token){
        if (token==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }
        try{
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();

            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
           return claimsJws.getBody();
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }

    }

    public static void main(String[] args) {
        System.out.println(createToken(8L, "15345728495"));//eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3OTk2NTAzMzMsInN1YiI6IkxPR0lOX1VTRVIiLCJ1c2VySWQiOjgsInVzZXJuYW1lIjoiMTUzNDU3Mjg0OTUifQ.0TyRYYAkRgFz41624tnewM093XVjrAJqmJohMhU2G1g
        //System.out.println(createToken(3L, "tonyu"));//eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3OTk1NTk4NzMsInN1YiI6IkxPR0lOX1VTRVIiLCJ1c2VySWQiOjMsInVzZXJuYW1lIjoidG9ueXUifQ.pm15JmlvlugJwnBAU7ykEcSoEvEQVfMky--xbuavK6M
    }

}
