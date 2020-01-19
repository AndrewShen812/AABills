package com.shenyong.aabills.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.Base64
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import com.sddy.baseui.BaseBindingActivity
import com.sddy.baseui.dialog.MsgToast
import com.sddy.utils.log.Log
import com.shenyong.aabills.AABilsApp
import com.shenyong.aabills.R
import com.shenyong.aabills.rx.RxExecutor
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


object ShareUtils {

    private const val ACCESS_KEY = "1c120hroXU4XL32VNWgF"
    private const val SECRET_KEY = "2b32645383654fc1773b86383c5e0969b304eaf6"

    private fun genDownloadUrl(): String {
        val ca = Calendar.getInstance()
        ca.add(Calendar.DAY_OF_MONTH, 1)
        // 秒
        var expires = ca.timeInMillis / 1000
        /**
         * 签名文档：http://open.sinastorage.com/doc/scs/guide/sign
         * StringToSign = HTTP-Verb + "\n" +
         *                Content-MD5 + "\n" +
         *                Content-Type + "\n" +
         *                Date + "\n" +
         *                CanonicalizedAmzHeaders +
         *                CanonicalizedResource;
         */
        val path = urlEncode("/china-style/apks/aabills_release.apk", true)
        val strToSign = "GET\n\n\n$expires\n$path"
        try {
            val encData = HmacSHA1Encrypt(String(strToSign.toByteArray(StandardCharsets.UTF_8)), SECRET_KEY)
            val base64Data = Base64.encodeToString(encData, Base64.DEFAULT)

            var ssig = urlEncode(base64Data.substring(5, 15), true)
            return "http://sinacloud.net$path?" +
                    "KID=sina,$ACCESS_KEY" +
                    "&Expires=$expires" +
                    "&ssig=$ssig"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private const val MAC_NAME = "HmacSHA1"
    /**
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     *
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun HmacSHA1Encrypt(encryptText: String, encryptKey: String): ByteArray {
        val mac = Mac.getInstance(MAC_NAME)
        val keyArray = encryptKey.toByteArray(StandardCharsets.UTF_8)
        mac.init(SecretKeySpec(keyArray, MAC_NAME))
        val text = encryptText.toByteArray(StandardCharsets.UTF_8)
        return mac.doFinal(text)
    }

    /**
     * Regex which matches any of the sequences that we need to fix up after
     * URLEncoder.encode().
     */
    private val ENCODED_CHARACTERS_PATTERN: Pattern by lazy {
        val pattern = StringBuilder()

        pattern
                .append(Pattern.quote("+"))
                .append("|")
                .append(Pattern.quote("*"))
                .append("|")
                .append(Pattern.quote("%7E"))
                .append("|")
                .append(Pattern.quote("%2F"));
        Pattern.compile(pattern.toString())
    }

    private fun urlEncode(value: String?, path: Boolean): String {
        if (value == null) {
            return ""
        }

        try {
            val encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.name())

            val matcher = ENCODED_CHARACTERS_PATTERN.matcher(encoded)
            val buffer = StringBuffer(encoded.length)

            while (matcher.find()) {
                var replacement = matcher.group(0)

                if ("+" == replacement) {
                    replacement = "%20"
                } else if ("*" == replacement) {
                    replacement = "%2A"
                } else if ("%7E" == replacement) {
                    replacement = "~"
                } else if (path && "%2F" == replacement) {
                    replacement = "/"
                }

                matcher.appendReplacement(buffer, replacement)
            }

            matcher.appendTail(buffer)
            return buffer.toString()

        } catch (ex: UnsupportedEncodingException) {
            throw RuntimeException(ex)
        }

    }

    private fun getShareQr(qrText: String, size: Int): Bitmap {
        val icon = BitmapFactory.decodeResource(AABilsApp.getInstance().resources, R.drawable.qr_icon)
        return QRCodeEncoder.encodeAsBitmap(qrText, size, icon)
    }

    private fun getQrCodeDimension(context: Context): Int {
        val manager = context.getSystemService(BaseBindingActivity.WINDOW_SERVICE) as WindowManager
        val displaySize = Point()
        manager.defaultDisplay.getSize(displaySize)
        val width = displaySize.x
        val height = displaySize.y
        return Math.min(width, height) * 2 / 3
    }

    @SuppressLint("CheckResult")
    fun showShareDialog(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_share_dialog, null, false)
        val ivQr = view.findViewById<ImageView>(R.id.iv_share_qr)
        val btnSend = view.findViewById<Button>(R.id.btn_share_send)
        var url = ""
        RxExecutor.backgroundWork {
            url = genDownloadUrl()
            Log.Ui.d("DownloadUrl: $url")
            return@backgroundWork getShareQr(url, getQrCodeDimension(context))
        }.subscribe {
            ivQr.setImageBitmap(it)
        }
        val dialog = AlertDialog.Builder(context)
                .setView(view)
                .create()
        btnSend.setOnClickListener {
            dialog.dismiss()
            sendText(context, url)
        }
        dialog.show()
    }

    private fun sendText(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, url)
            intent.type = "text/plain"
            context.startActivity(intent)
        } catch (e: Exception) {
            MsgToast.centerToast("抱歉，发送失败")
        }
    }
}