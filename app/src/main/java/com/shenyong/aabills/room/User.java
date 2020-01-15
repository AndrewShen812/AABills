package com.shenyong.aabills.room;

import android.annotation.SuppressLint;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.sddy.baseui.dialog.MsgToast;
import com.shenyong.aabills.utils.RxUtils;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;

@Entity(tableName = "user")
public class User {

    @SerializedName("uid")
    @NonNull
    @PrimaryKey
    public String mUid;

    @SerializedName("token")
    @Ignore
    public String mToken;

    @NonNull
    @ColumnInfo
    public String mName;

    @NonNull
    @ColumnInfo
    public String mPhone;

    @NonNull
    @ColumnInfo
    public String mPwd;

    @NonNull
    @ColumnInfo
    @ColorInt
    public int mHeadBg;

    @Ignore
    public boolean isLogin;

    @NonNull
    @ColumnInfo
    public boolean isLastLogin;

    public User(String mName) {
        this.mName = mName;
        mPhone = "";
        mPwd = "";
        mHeadBg = 0xFF2A82E4;
        isLastLogin = false;
        mUid = UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return "User{" +
                "mUid='" + mUid + '\'' +
                ", mToken='" + mToken + '\'' +
                ", mName='" + mName + '\'' +
                ", mPhone='" + mPhone + '\'' +
                ", mPwd='" + mPwd + '\'' +
                ", mHeadBg=" + mHeadBg +
                ", isLogin=" + isLogin +
                ", isLastLogin=" + isLastLogin +
                '}';
    }

    public String getName() {
        if (!TextUtils.isEmpty(mName)) {
            return mName;
        }
        return Build.MODEL;
    }

    /**
     * 获取一个短名称，方便再圆圈背景内显示
     * @return
     */
    public String getShortName() {
        String name = getName();
        // 取前3位吧
        return name.length() > 3 ? name.substring(0, 3) : name;
    }

}
