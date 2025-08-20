package com.druto.loan.cash.drutocash.http

import android.text.TextUtils
import android.util.Base64
import com.druto.loan.cash.drutocash.DrutoCashApplication
import com.druto.loan.cash.drutocash.utils.NetworkUtil.ipv4
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Collections
import java.util.zip.Deflater
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Util {
    fun makeSignature(
        timestamp: String, token: String,
        md5: String, sessionId: String
    ): String {
        val list: MutableList<String> = ArrayList()
        list.add(timestamp)
        list.add(DrutoCashApplication.SECURITY_KEY)
        list.add(token)
        if (!TextUtils.isEmpty(md5)) list.add(md5)
        if (!TextUtils.isEmpty(sessionId)) list.add(sessionId)
        Collections.sort(list)
        val sb = StringBuilder()
        for (s in list) sb.append(s)
        return md5(sb.toString().trim { it <= ' ' }.toByteArray())
    }

    /**
     * 加密方法
     * @param data  需要加密的数据
     * @param key   接口返回的加密key
     * @return
     */
    fun encrypt(data: ByteArray?, key: String): ByteArray? {
        var result: ByteArray? = null
        try {
            val kgen = KeyGenerator.getInstance("AES")
            kgen.init(128) //设置密钥长度
            val skey = kgen.generateKey() //生成密钥
            val iv = skey.encoded
            val ivSpec = IvParameterSpec(iv)
            val secretKey = SecretKeySpec(key.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encrypted = cipher.doFinal(data) // 加密

            result = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, result, 0, iv.size)
            System.arraycopy(encrypted, 0, result, iv.size, encrypted.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 压缩数据方法
     * @param byteData  需要压缩的数据
     * @return
     */
    fun compress(byteData: ByteArray): ByteArray? {
        var compressed: ByteArray? = null
        try {
            val bos = ByteArrayOutputStream(byteData.size)
            val compressor = Deflater()
            compressor.setLevel(Deflater.BEST_COMPRESSION) // 将当前压缩级别设置为指定值。
            compressor.setInput(byteData, 0, byteData.size)
            compressor.finish() // 调用时，指示压缩应当以输入缓冲区的当前内容结尾。

            // Compress the data
            val buf = ByteArray(1024)
            while (!compressor.finished()) {
                val count = compressor.deflate(buf)
                bos.write(buf, 0, count)
            }
            compressor.end() // 关闭解压缩器并放弃所有未处理的输入。
            compressed = bos.toByteArray()
            bos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return compressed
    }

    /**
     * md5加密
     * @param data
     * @return
     */
    fun md5(data: ByteArray): String {
        var messageDigest: MessageDigest? = null
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(data)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        val byteArray = messageDigest!!.digest()
        val md5StrBuff = StringBuffer()
        for (i in byteArray.indices) {
            if (Integer.toHexString(0xFF and byteArray[i].toInt()).length == 1) md5StrBuff.append("0")
                .append(
                    Integer.toHexString(0xFF and byteArray[i].toInt())
                )
            else md5StrBuff.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
        }
        return md5StrBuff.toString()
    }

    val token: String
        /**
         * 获取其他信息
         * @return
         */
        get() {
            val location =
                DrutoCashApplication.LOCATION_LIVE_DATA.value
            val ip = ipv4
            var ret = ""
            var len = 0
            val bytes = ByteArray(12)
            if (ip != null && !TextUtils.isEmpty(ip)) {
                val num =
                    ip.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (num.size == 4) {
                    var n = 0L
                    for (i in 0..3) n = n shl 8 or num[i].toInt().toLong()
                    addBytes(n, bytes, len)
                    len += 4
                }
            }
            if (null != location) {
                addBytes(
                    Math.round(location.latitude * 10000000),
                    bytes,
                    len
                )
                len += 4
                addBytes(
                    Math.round(location.longitude * 10000000),
                    bytes,
                    len
                )
                len += 4
            }
            if (len > 0) {
                ret = Base64.encodeToString(bytes, 0, len, Base64.DEFAULT)
                    .replace('+', '-')
                    .replace('/', '_')
                    .replace("=", "")
            }
            return ret.trim { it <= ' ' }
        }

    private fun addBytes(number: Long, b: ByteArray, pos: Int) {
        var number = number
        var i = 0
        while (i < 4) {
            b[pos + i] = (0xffL and number.toByte().toLong()).toByte()
            i++
            number = number shr 8
        }
    }
}
