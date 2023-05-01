package com.gzhu.funai.utils;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @Author :wuxiaodong
 * @Date: 2022/9/13 16:53
 * @Description:
 */
public class JwtUtil {

    private JwtUtil(){ }

    // token过期时间
    private static final int TOKEN_EXPIRATION = 1000*60*60*3;
    // token签名密钥
    private static final String TOKEN_SIGN_KEY = "funai";

    /**
     * 创建前端token
     */
    public static String createToken(String userId, String userName, Integer userLevel) {
        return Jwts.builder()
                .setSubject("ChatGPT")
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .claim("userId", userId)
                .claim("userName", userName)
                .claim("userLevel", userLevel)
                .signWith(SignatureAlgorithm.HS512, TOKEN_SIGN_KEY)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
    }

    /**
     * 获得token的userid信息
     */
    public static String getUserId(String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(TOKEN_SIGN_KEY).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get("userId");
    }

    /**
     * 获得token的username信息
     */
    public static String getUserName(String token) {
        if(StringUtils.isEmpty(token)) {
            return "";
        }
        Jws<Claims> claimsJws
                = Jwts.parser().setSigningKey(TOKEN_SIGN_KEY).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get("userName");
    }

    /**
     * 获得token的用户级别信息
     */
    public static Integer getUserLevel(String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        Jws<Claims> claimsJws
                = Jwts.parser().setSigningKey(TOKEN_SIGN_KEY).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return Integer.valueOf(String.valueOf(claims.get("userLevel")));
    }

    /**
     * 验证JWT token是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            // 创建一个JWT解析器
            JwtParser parser = Jwts.parser().setSigningKey(TOKEN_SIGN_KEY);
            // 解析JWT token
            Jws<Claims> jws = parser.parseClaimsJws(token);
            // 从JWT token中获取到过期时间
            Long expiration = jws.getBody().getExpiration().getTime();
            // 获取当前时间
            Long now = System.currentTimeMillis();
            // 判断过期时间是否已经过期
            return expiration < now;
        } catch (Exception e) {
            // 解析失败或者token已经过期，都会抛出异常
            return true;
        }
    }
}

