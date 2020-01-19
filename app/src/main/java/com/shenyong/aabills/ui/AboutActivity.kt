package com.shenyong.aabills.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.sddy.baseui.BaseBindingActivity
import com.sddy.baseui.dialog.MsgToast
import com.sddy.utils.ViewUtils
import com.shenyong.aabills.R
import com.shenyong.aabills.databinding.ActivityAboutBinding
import com.shenyong.aabills.utils.AppUtils
import com.shenyong.aabills.utils.ShareUtils
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : BaseBindingActivity<ActivityAboutBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setTitle("关于AA账单")
        mBindings.presenter = this
        tv_about_version.text = "${resources.getString(R.string.app_name)} v${AppUtils.getVersionName()}.${AppUtils.getVersionCode()}"
        tv_about_github.setOnClickListener {
            opneGitHubPage()
        }

        tv_about_email.text = ViewUtils.getColoredText("邮箱: shenyong812@hotmail.com", 4, R.color.main_blue)
        tv_about_email.setOnClickListener {
            sendEmail()
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when (v!!.id) {
            R.id.ll_about_share -> shareApp()
            R.id.ll_about_github -> opneGitHubPage()
            else -> {}
        }
    }

    private fun shareApp() {
        ShareUtils.showShareDialog(this, object : ShareUtils.ShareListener {
            override fun onCopyUrl(url: String) {
                sendText(url)
            }
        })
    }

    private fun opneGitHubPage() {
        try {
            val uri = Uri.parse("https://github.com/ChinaStyle812/AABills")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            MsgToast.centerToast("抱歉，无法在你的设备上打开页面")
        }
    }

    private fun sendText(url: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, url)
            intent.type = "text/plain"
            startActivity(intent)
        } catch (e: Exception) {
            MsgToast.centerToast("抱歉，发送失败")
        }
    }

    private fun sendEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:shenyong812@hotmail.com")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            MsgToast.centerToast("抱歉，无法启动邮箱应用")
        }
    }
}
