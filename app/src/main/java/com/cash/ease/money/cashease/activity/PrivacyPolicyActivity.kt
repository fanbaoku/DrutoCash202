package com.cash.ease.money.cashease.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import com.cash.ease.money.cashease.R
import com.cash.ease.money.cashease.ui.base.BaseAppCompatActivity

class PrivacyPolicyActivity: BaseAppCompatActivity() {
    var access:Button?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        setTitle()
        var webView=findViewById<WebView>(R.id.web_view)
        webView.loadUrl("https://apitz.ekascraddlemax.site/privacy-policy.html")
        access=findViewById(R.id.access)
        access!!.setBackgroundColor(R.color.main_text)
        access!!.isEnabled=false
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 每秒回调一次
                val seconds = millisUntilFinished / 1000
                access!!.text = "access $seconds s"
            }

            override fun onFinish() {
                // 倒计时结束回调
                access!!.text = "access"
                access!!.setBackgroundResource(R.drawable.button_major_default)
                access!!.isEnabled=true
            }
        }.start()
        access!!.setOnClickListener{
            finish()
        }
    }

    private fun setTitle() {
        try {
            val titleView = findViewById<TextView>(R.id.page_title)
            titleView.text = "Privacy Policy"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(logTag, e)
        }
    }
}