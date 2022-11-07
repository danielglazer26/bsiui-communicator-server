package kopaczewski.glazer.bsiui.server.encryption;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Encryption {

    public static final int encodeWindowSize = 64;
    public static final int decodeWindowSize = 172;
    public static final String AES = "AES";
    public static final String RSA = "RSA";

    public static String encryptHugeTextRSA(String text, Key publicKey) throws Exception {
        var textBytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (text.getBytes().length > encodeWindowSize * (i + 1)) {
            var toEncrypt = Arrays.copyOfRange(textBytes, i * encodeWindowSize, (i + 1) * encodeWindowSize);
            result.append(encryptTextRSA(toEncrypt, publicKey));
            i++;
        }
        result.append(encryptTextRSA(Arrays.copyOfRange(textBytes, encodeWindowSize * (i), textBytes.length), publicKey));
        return result.toString();
    }

    public static String decryptHugeTextRSA(String text, Key privateKey) throws Exception {
        var textBytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (textBytes.length > decodeWindowSize * i + 1) {
            var toDecrypt = Arrays.copyOfRange(textBytes, i * decodeWindowSize, (i + 1) * decodeWindowSize);
            result.append(decryptTextRSA(toDecrypt, privateKey));
            i++;
        }
        return result.toString();
    }

    private static String encryptTextRSA(byte[] contentBytes, Key pubKey) throws Exception {
        Cipher cipher = getCipher(RSA, Cipher.ENCRYPT_MODE, pubKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(contentBytes));
    }

    private static String decryptTextRSA(byte[] cipherContent, Key privKey) throws Exception {
        Cipher cipher = getCipher(RSA, Cipher.DECRYPT_MODE, privKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(cipherContent)));
    }

    public static String encryptAES(String data, Key key) throws Exception {
        Cipher encryptionCipher = getCipher(AES, Cipher.ENCRYPT_MODE, key);
        byte[] encryptedMessageBytes = encryptionCipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    }

    public static String decryptAES(String encryptedData, Key key) throws Exception {
        Cipher decryptionCipher = getCipher(AES, Cipher.DECRYPT_MODE, key);
        byte[] decryptedMessageBytes = decryptionCipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedMessageBytes);
    }

    private static Cipher getCipher(String transformation, int decryptMode, Key key)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(decryptMode, key);
        return cipher;
    }

    public static SecretKey secretKeyFromStringAES(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, AES);
    }
}
