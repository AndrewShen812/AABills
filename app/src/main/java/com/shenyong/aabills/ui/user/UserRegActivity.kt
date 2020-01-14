package com.shenyong.aabills.ui.user

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import com.sddy.baseui.BaseBindingActivity
import com.sddy.baseui.dialog.MsgToast
import com.shenyong.aabills.R
import com.shenyong.aabills.databinding.ActivityUserRegBinding
import com.shenyong.aabills.utils.RxTimer


class UserRegActivity : BaseBindingActivity<ActivityUserRegBinding>() {

    companion object {
        const val GET_CODE_COUNT_DOWN = 60
        const val CHINA = "86"
    }

    lateinit var viewModel: UserRegViewModel
    private val timer = RxTimer()
    private var timerSec: Int = GET_CODE_COUNT_DOWN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_reg)
        setTitle("注册")
        viewModel = ViewModelProviders.of(this).get(UserRegViewModel::class.java)
        mBindings.model = viewModel
        viewModel.regPhone.observe(this, Observer<String> {
            mBindings.btnRegGetCode.isEnabled = !viewModel.regPhone.value.isNullOrEmpty()
            textChangeObserver.onChanged("")
        })
        viewModel.regPwd.observe(this, textChangeObserver)
        viewModel.regConfirmPwd.observe(this, textChangeObserver)
        viewModel.regCode.observe(this, Observer<String> {
            textChangeObserver.onChanged("")
        })
        mBindings.btnRegGetCode.setOnClickListener {
            // todo 请求验证码，其中country表示国家代码，如“86”；phone表示手机号码，如“13800138000”
            mBindings.btnRegGetCode.isEnabled = false
            timer.interval(1000) {
                timerSec--
                if (timerSec > 0) {
                    mBindings.btnRegGetCode.text = "${timerSec}秒"
                } else {
                    timer.cancel()
                    timerSec = GET_CODE_COUNT_DOWN
                    mBindings.btnRegGetCode.isEnabled = true
                    mBindings.btnRegGetCode.text = "获取验证码"
                }
            }
        }
        mBindings.btnRegOk.setOnClickListener {
            viewModel.regSuccess.value = true
        }
        viewModel.regSuccess.observe(this, Observer<Boolean> {
            if (it == true) {
                MsgToast.shortToast("注册成功")
                val data = Intent()
                data.putExtra("regPhone", viewModel.regPhone.value)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    private val textChangeObserver = Observer<String> {
        mBindings.btnRegOk.isEnabled = !viewModel.regPhone.value.isNullOrEmpty()
                && !viewModel.regCode.value.isNullOrEmpty()
                && !viewModel.regPwd.value.isNullOrEmpty()
                && !viewModel.regConfirmPwd.value.isNullOrEmpty()
    }

}
