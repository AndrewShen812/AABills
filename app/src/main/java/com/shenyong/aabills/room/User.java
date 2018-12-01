package com.shenyong.aabills.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

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
        mHeadBg = 0x2A82E4;
        isLastLogin = false;
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

    public String getNickName() {
        if (!TextUtils.isEmpty(mName)) {
            return mName;
        }
        // 取电话前3位吧
        return mPhone.substring(0, 3);
    }
}
