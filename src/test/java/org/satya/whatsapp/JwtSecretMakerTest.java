package org.satya.whatsapp;

import io.jsonwebtoken.Jwts;
import jakarta.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

public class JwtSecretMakerTest {

    @Test
    public void generateJwtSecretKey() {
        SecretKey key = Jwts.SIG.HS512.key().build();
        String encodeKey = DatatypeConverter.printHexBinary(key.getEncoded());
        System.out.printf("\nkey=[%s]\n", encodeKey);
    }
}
