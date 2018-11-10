package com.shenyong.aabills.ui.user

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import cn.smssdk.EventHandler
import cn.smssdk.SMSSDK
import com.alibaba.fastjson.JSONObject
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
            // 请求验证码，其中country表示国家代码，如“86”；phone表示手机号码，如“13800138000”
            SMSSDK.getVerificationCode(CHINA, viewModel.regPhone.value)
            mBindings.btnRegGetCode.isEnabled = false
            timer.interval(1000) {
                timerSec--
                if (timerSec > 0) {
                    mBindings.btnRegGetCode.text = timerSec.toString() + "秒"
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
            // 提交验证码，其中的code表示验证码，如“1357”
//            SMSSDK.submitVerificationCode(CHINA, viewModel.regPhone.value, viewModel.regCode.value)
        }
        // 注册一个事件回调，用于处理SMSSDK接口请求的结果
        SMSSDK.registerEventHandler(eventHandler)
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
        SMSSDK.unregisterEventHandler(eventHandler)
    }

    private val textChangeObserver = Observer<String> {
        mBindings.btnRegOk.isEnabled = !viewModel.regPhone.value.isNullOrEmpty()
                && !viewModel.regCode.value.isNullOrEmpty()
                && !viewModel.regPwd.value.isNullOrEmpty()
                && !viewModel.regConfirmPwd.value.isNullOrEmpty()
    }

    private val eventHandler = object : EventHandler() {
        override fun afterEvent(event: Int, result: Int, data: Any?) {
            // afterEvent会在子线程被调用，因此如果后续有UI相关操作，需要将数据发送到UI线程
            val msg = Message()
            msg.arg1 = event
            msg.arg2 = result
            msg.obj = data
            Handler(Looper.getMainLooper(), Handler.Callback { msg ->
                val event = msg.arg1
                val result = msg.arg2
                val data = msg.obj
                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    if (result == SMSSDK.RESULT_COMPLETE) {
                        // 请注意，此时只是完成了发送验证码的请求，验证码短信还需要几秒钟之后才送达
                        MsgToast.shortToast("验证码已发送至您的手机")
                    } else {
                        MsgToast.shortToast("验证码发送失败了")
                        (data as Throwable).printStackTrace()
                    }
                } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    if (result == SMSSDK.RESULT_COMPLETE) {
                        // 验证码验证通过
                        viewModel.register()
                    } else {
                        val e = data as Throwable
                        e.printStackTrace()
                        try {
                            //{"httpStatus":400,"description":"需要校验的验证码错误","detail":"用户提交校验的验证码错误。","error":"Illegal check request.","status":468}
                            val json = JSONObject.parseObject(e.message)
                            val desc = json["description"] as String
                            if (!desc.isNullOrEmpty()) {
                                MsgToast.shortToast(desc)
                            } else {
                                MsgToast.shortToast("验证失败")
                            }
                        } catch (e: Exception) {
                            MsgToast.shortToast("验证失败")
                        }
                    }
                }
                false
            }).sendMessage(msg)
        }
    }
}
