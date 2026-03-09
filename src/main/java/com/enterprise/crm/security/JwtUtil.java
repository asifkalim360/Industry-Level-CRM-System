package com.enterprise.crm.security;

//Token generate karna | Token sign karna | Token parse karna | Expiry validate karna
import com.enterprise.crm.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// Ye batata hai Spring ko: Is class ka object automatically bana do (Spring Bean)

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.SignatureAlgorithm;

// SecretKey → token sign karne ke liye
import javax.crypto.SecretKey;
import javax.swing.*;
// Date → issue & expiry time ke liye
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import java.util.Date;

// @Component ka matlab: Spring automatically is class ka object banayega
@Component
@RequiredArgsConstructor
public class JwtUtil {
/**
 * Hard code ko hataya hai yahan se.
    //JWT ka pura security isi pe depend karta hai.
    //Token sign karne aur Token verify karne me use hoti hai
    //Production me: Isko hardcode nahi karte
    //application.yml me rakhte | Environment variable use karte
//    // private final String SECRET = "mySecretKeymySecretKeymySecretKey";
    // HS256 ke liye secret length >= 32 chars hona chahiye

    // 1000 = 1 second | 1000 * 60 = 1 minute | 1000 * 60 * 15 = 15 minutes
    // Access token 15 minutes me expire hoga.
//    // private final long ACCESS_EXPIRATION = 1000 * 60 * 15;

//    @Value("${jwt.secret}")
//    private String SECRET;
*/
    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.access-expiration}")
    private long ACCESS_EXPIRATION;

    @Value("${jwt.refresh-expiration}")
    private long REFRESH_EXPIRATION;

    private final JwtProperties jwtProperties;

    // Ye kya karta hai?
    //SECRET string ko bytes me convert karta hai | Usse HMAC SHA key banata hai
    //JWT me HS256 algorithm use ho raha hai | Toh hame HMAC based key chahiye.
    private SecretKey getSigningKey() {

        // Ye method string secret ko proper SecretKey object me convert karta hai
        //   return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());   // Normal 32+ char string

        //Agar tum Base64 secret use kar rahe ho aur decode nahi karoge:
        //Signing key wrong ban jayegi, Token verify error aa sakta hai, Security weak ho sakti hai
//        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());    // Base64 generated secret
//        return Keys.hmacShaKeyFor(keyBytes);
          return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));


//                | Secret Type             | Use                        |
//                | ----------------------- | -------------------------- |
//                | Normal 32+ char string  | `.getBytes()`              |
//                | Base64 generated secret | `Decoders.BASE64.decode()` |

    }

    // Ye kya karta hai?
    //SECRET string ko bytes me convert karta hai | Usse HMAC SHA key banata hai
    //JWT me HS256 algorithm use ho raha hai | Toh hame HMAC based key chahiye.
//    private SecretKey getSigningKey() {
//        // Ye method string secret ko proper SecretKey object me convert karta hai
//        return Keys.hmacShaKeyFor(SECRET.getBytes());
//    }

    // GENERATE-ACCESS-TOKEN METHOD
    public String generateAccessToken(String email)     // // Ye method login ke time call hota hai.
    {

        return Jwts.builder()   // matlab Token banana start kar diya.
                .setSubject(email) // Subject ka matlab: Token kiske liye hai? : Yaha hum email store kar rahe hain.
                                   // JWT structured hota hai: (Header.Payload.Signature) | Payload me subject store hota hai.
                .setIssuedAt(new Date())    // Token kab generate hua.
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExpiration()))    // expity : Current time + 15 minutes.
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Important: Algorithm = HS256 and Key = SECRET based key
                                                                //Ye ensure karta hai: Agar koi token change karega → signature invalid ho jayega.
                .compact(); // Final JWT string generate karta hai.
    }

    // EXTRACT-Email METHOD
    public String extractEmail(String token) // Ye method Token me se email ko nikalta hai.
    {
        return Jwts.parserBuilder() // Parser create kiya.
                .setSigningKey(getSigningKey())     // Verify karne ke liye same secret use karna zaroori hai.
                .build()
                .parseClaimsJwt(token)      // Token parse karega -> Signature verify karega -> Expiry check karega -> Agar invalid hua → exception throw karega
                .getBody()
                .getSubject(); // Payload se subject (email) return karega.
    }

    //  VALIDATE-TOKEN METHOD
    public boolean validateToken(String token)  // Ye method simply check karta hai: Token valid hai ya nahi?
    {
        // Agar: Signature correcY : Expiry valid : Token properly formatted
        //fhr No exception → return true ho jayega.
        try{
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJwt(token);
            return true;
        }
        // Agar: Expired token: Signature wrong: Tampered token: Malformed token.
        //to false return karega.
        catch (JwtException | IllegalArgumentException e)
        {
            return false;
        }
    }

}