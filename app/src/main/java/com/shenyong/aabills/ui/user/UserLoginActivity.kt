package com.shenyong.aabills.ui.user

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.sddy.baseui.BaseBindingActivity
import com.sddy.baseui.Presenter
import com.sddy.baseui.dialog.MsgToast
import com.shenyong.aabills.R
import com.shenyong.aabills.databinding.ActivityUserLoginBinding
import com.shenyong.aabills.ui.MainActivity

class UserLoginActivity : BaseBindingActivity<ActivityUserLoginBinding>(), Presenter {

    companion object {
        const val REQ_CODE_REG = 1000
    }

    lateinit var viewModel: UserLoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)
        setTitle("登录")
        viewModel = ViewModelProviders.of(this).get(UserLoginViewModel::class.java)
        mBindings.model = viewModel
        mBindings.presenter = this@UserLoginActivity
        viewModel.phoneText.observe(this, Observer<String> {
            checkBtnEnable()
        })
        viewModel.pwdText.observe(this, Observer<String> {
            checkBtnEnable()
        })
        viewModel.loginSuccess.observe(this, Observer<Boolean> {
            if (it == true) {
                MsgToast.shortToast("登录成功")
                startActivity(MainActivity::class.java)
                finish()
            }
        })
    }

    private fun checkBtnEnable() {
        mBindings.btnLoginOk.isEnabled = !viewModel.phoneText.value.isNullOrEmpty()
                && !viewModel.pwdText.value.isNullOrEmpty()
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.btn_login_ok -> {
                viewModel.userLogin()
            }
            R.id.tv_login_reg -> {
                startActivityForResult(UserRegActivity::class.java, REQ_CODE_REG)
            }
            R.id.tv_login_retrieve_pwd -> {
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_REG -> {
                val phone = data?.getStringExtra("regPhone") ?: ""
                mBindings.etLoginPhone.setText(phone)
            }
        }
    }
}
