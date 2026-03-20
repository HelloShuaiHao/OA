package cn.iocoder.yudao.module.agentx.service.instance;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * OpenFang API Key 加解密组件。
 */
@Component
public class OpenfangApiKeyCrypto {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;

    public String encrypt(String plainText) {
        if (StrUtil.isBlank(plainText)) {
            return null;
        }
        try {
            byte[] key = decodeKey();
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM), new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            int cipherLength = encrypted.length - 16;
            byte[] cipherText = new byte[cipherLength];
            byte[] tag = new byte[16];
            System.arraycopy(encrypted, 0, cipherText, 0, cipherLength);
            System.arraycopy(encrypted, cipherLength, tag, 0, 16);
            return Base64.encode(iv) + ":" + Base64.encode(cipherText) + ":" + Base64.encode(tag);
        } catch (Exception ex) {
            throw exception(ErrorCodeConstants.OPENFANG_INSTANCE_ENCRYPT_FAILED);
        }
    }

    public String decrypt(String encryptedText) {
        if (StrUtil.isBlank(encryptedText)) {
            return null;
        }
        try {
            String[] parts = StrUtil.splitToArray(encryptedText, ':');
            if (parts == null || parts.length != 3) {
                throw exception(ErrorCodeConstants.OPENFANG_INSTANCE_ENCRYPT_FORMAT_INVALID);
            }
            byte[] iv = Base64.decode(parts[0]);
            byte[] cipherText = Base64.decode(parts[1]);
            byte[] tag = Base64.decode(parts[2]);
            byte[] merged = new byte[cipherText.length + tag.length];
            System.arraycopy(cipherText, 0, merged, 0, cipherText.length);
            System.arraycopy(tag, 0, merged, cipherText.length, tag.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decodeKey(), ALGORITHM), new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            return new String(cipher.doFinal(merged), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw exception(ErrorCodeConstants.OPENFANG_INSTANCE_DECRYPT_FAILED);
        }
    }

    private byte[] decodeKey() {
        String base64Key = System.getenv("AGENTX_ENCRYPTION_KEY");
        if (StrUtil.isBlank(base64Key)) {
            throw exception(ErrorCodeConstants.OPENFANG_INSTANCE_ENCRYPTION_KEY_NOT_CONFIGURED);
        }
        byte[] key = Base64.decode(base64Key);
        if (key.length != 32) {
            throw exception(ErrorCodeConstants.OPENFANG_INSTANCE_ENCRYPTION_KEY_INVALID);
        }
        return key;
    }

}
