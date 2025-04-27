package com.Dev.Pal.Services;

import com.Dev.Pal.Model.EmailToken;
import com.Dev.Pal.Model.UserEntity;
import com.Dev.Pal.Repositary.EmailTokenRepository;
import com.Dev.Pal.Repositary.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class OtpServices {

    private  GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    private final EmailTokenRepository emailTokenRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    private final UserRepository userRepository;
    private final EmailServices emailService; // ✅ Use EmailService instead of UserController

    public OtpServices(GoogleAuthenticator googleAuthenticator, EmailTokenRepository emailTokenRepository, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, UserRepository userRepository, EmailServices emailService) {
        this.googleAuthenticator = googleAuthenticator;
        this.emailTokenRepository = emailTokenRepository;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public String generateSecretKey(Long id) throws WriterException, MessagingException, IOException {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();
      UserEntity user =  userRepository.findById(id).orElseThrow(()->new RuntimeException("The User is not found "));
      if(user.getSecret_key() == null) {
          user.setSecret_key(secret);

          userRepository.save(user);

          String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                  "DevPal",
                  user.getEmail(),
                  key
          );

          QRCodeWriter qrCodeWriter = new QRCodeWriter();
          BitMatrix bitMatrix = qrCodeWriter.encode(
                  otpAuthUrl,
                  BarcodeFormat.QR_CODE,
                  250,
                  250
          );
          emailService.sendHtmlEmail(user.getEmail(), otpAuthUrl);


          return otpAuthUrl;
      }
      else{
          throw new RuntimeException("Already created QrCode");
      }
    }


    public Map<String, Object> validateQrCode(String code, Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String secretKey = userEntity.getSecret_key();
        if (secretKey == null || secretKey.isEmpty()) {
            throw new RuntimeException("User does not have a secret key configured");
        }

        System.out.println("Secret Key: " + secretKey);
        System.out.println("User entered OTP: " + code);

        boolean isValid = googleAuthenticator.authorize(secretKey, Integer.parseInt(code));

        if (!isValid) {
            throw new RuntimeException("Invalid OTP Code");
        }


        return  createJwtResponse(userEntity.getEmail(),id);
    }

    private Map<String, Object> createJwtResponse(String email,Long id) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(3, ChronoUnit.HOURS))
                .subject(email)
                .claim("Email", email)
                .claim("userId",id)
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(claims);
        String token = jwtEncoder.encode(parameters).getTokenValue();
        //System.out.println(jwtDecoder.decode(token).
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId",id);

        return response;
    }
    public Long getUserIdFromAuthorizationHeader(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        Jwt decodedJwt = jwtDecoder.decode(token);
        return decodedJwt.getClaim("userId");
    }




}
