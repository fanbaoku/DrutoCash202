package com.su.request.http;

import android.location.Location;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Util {

    /**
     * 加密方法
     * @param data  需要加密的数据
     * @param key   接口返回的加密key
     * @return
     */
    public static byte[] encrypt(byte[] data, String key) {
        byte[] result = null;
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128); //设置密钥长度
            SecretKey skey = kgen.generateKey(); //生成密钥
            byte[] iv = skey.getEncoded();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(data); // 加密

            result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 压缩数据方法
     * @param byteData  需要压缩的数据
     * @return
     */
    public static byte[] compress(byte[] byteData) {
        byte[] compressed = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(byteData.length);
            Deflater compressor = new Deflater();
            compressor.setLevel(Deflater.BEST_COMPRESSION); // 将当前压缩级别设置为指定值。
            compressor.setInput(byteData, 0, byteData.length);
            compressor.finish(); // 调用时，指示压缩应当以输入缓冲区的当前内容结尾。

            // Compress the data
            final byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
            compressor.end(); // 关闭解压缩器并放弃所有未处理的输入。
            compressed = bos.toByteArray();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return compressed;
    }

    /**
     * md5加密
     * @param data
     * @return
     */
    public static String md5(byte[] data) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }

    /**
     * 获取其他信息
     * @param ip
     * @param location
     * @return
     */
    public static String getToken(String ip, Location location) {
        String ret = "";
        int len = 0;
        byte[] bytes = new byte[12];
        if(ip != null && !TextUtils.isEmpty(ip)) {
            String[] num = ip.split("\\.");
            if(num.length == 4) {
                long n = 0L;
                for (int i = 0; i < 4; ++i) n = n << 8 | Integer.parseInt(num[i]);
                addBytes(n, bytes, len); len += 4;
            }
        }
        if(null != location) {
            addBytes(Math.round(location.getLatitude() * 10000000), bytes, len); len += 4;
            addBytes(Math.round(location.getLongitude() * 10000000), bytes, len); len += 4;
        }
        if(len > 0) {
            ret = Base64.encodeToString(bytes, 0, len, Base64.DEFAULT)
                    .replace('+','-')
                    .replace('/', '_')
                    .replace("=","");
        }
        return ret.trim();
    }
    private static void addBytes(long number, byte[] b, int pos) {
        for (int i = 0; i < 4; i++, number >>= 8) b[pos + i] = Long.valueOf(0xff & number).byteValue();
    }
}
