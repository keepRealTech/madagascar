package com.keepreal.madagascar.vanga.wechatPay;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class WXPayDecodeUtil {
    private String algorithm = "AES";
    private String algorithmModePadding = "AES/ECB/PKCS7Padding";
    private String key;
    private SecretKeySpec secretKey;
    private boolean initialized = false;

    /**
     * AES解密
     * @param base64Data
     * @return
     * @throws Exception
     */
    public String decryptData(String base64Data) throws Exception {
        this.initialize();

        // 获取解码器实例，"BC"指定Java使用BouncyCastle库里的加/解密算法。
        Cipher cipher = Cipher.getInstance(algorithmModePadding, "BC");
        // 使用秘钥并指定为解密模式初始化解码器
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        // cipher.doFinal(byte[] b)在单部分操作中加密或解密数据，或完成多部分操作。 根据此秘钥的初始化方式，对数据进行加密或解密。
        return new String(cipher.doFinal(Base64.decode(base64Data)));
    }

    /**
     * 安全提供者列表中注册解密算法提供者，这个加载过程还挺慢的，有时候要好几秒，只需要加载一次就能一直使用。
     */
    private void initialize() {
        if (this.initialized) {
            return;
        }

        Security.addProvider(new BouncyCastleProvider());
        this.initialized = true;
    }


    /**
     * 构造方法(容器初始化时从配置文件中获取key，在全局中维护一个唯一的SecretKeySpec)
     * @param key
     */
    @SneakyThrows
    public WXPayDecodeUtil(String key) {
        this.key = key;
        // 转化成JAVA的密钥格式
        this.secretKey = new SecretKeySpec(WXPayUtil.MD5(key).toLowerCase().getBytes(), this.algorithm);
        this.initialize();
    }

}
