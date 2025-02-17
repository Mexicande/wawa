package com.deerlive.zhuawawa.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.deerlive.zhuawawa.MainActivity;
import com.deerlive.zhuawawa.R;
import com.deerlive.zhuawawa.base.BaseActivity;
import com.deerlive.zhuawawa.common.Api;
import com.deerlive.zhuawawa.common.WebviewActivity;
import com.deerlive.zhuawawa.intf.OnRequestDataListener;
import com.deerlive.zhuawawa.utils.ActivityUtils;
import com.deerlive.zhuawawa.utils.LogUtils;
import com.deerlive.zhuawawa.utils.SPUtils;
import com.deerlive.zhuawawa.utils.ToastUtils;
import com.hss01248.dialog.StyledDialog;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    Platform mPlatForm;
    Dialog mLoadingDialog;
    MyHandler mHandler;
    @Bind(R.id.checkbox_login)
    CheckBox checkboxLogin;
    @Bind(R.id.weChat_login)
    ImageView weChatLogin;
    private Map<String,String> params;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlatForm = ShareSDK.getPlatform(Wechat.NAME);
        mHandler = new MyHandler();
        weChatLogin.setOnClickListener(this);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_login;
    }



    public void xieyi(View v) {
        Bundle temp = new Bundle();
        temp.putString("title", getResources().getString(R.string.xieyi));
        temp.putString("jump", Api.URL_GAME_XIEYI);
        ActivityUtils.startActivity(temp, WebviewActivity.class);
    }

    private PlatformActionListener mPlatListener = new PlatformActionListener() {
        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            //mLoadingDialog.dismiss();


            PlatformDb db = platform.getDb();
            String name = db.getUserName();

            String from = "Wechat";
            String head_img = db.getUserIcon();
            String openid = db.getUserId();
            String access_token = db.getToken();
            String expires_date = db.getExpiresTime() + "";
            params = new HashMap<>();
            params.put("name", name);
            params.put("from", from);
            params.put("head_img", head_img);
            params.put("openid", openid);
            params.put("access_token", access_token);
            params.put("expires_date", expires_date);
            params.put("qudao", Api.QUDAO);
            mHandler.sendEmptyMessage(1);
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            mLoadingDialog.dismiss();
        }

        @Override
        public void onCancel(Platform platform, int i) {
            mLoadingDialog.dismiss();
        }

    };

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.weChat_login:
                if(checkboxLogin.isChecked()){
                    mPlatForm.setPlatformActionListener(mPlatListener);
                    mPlatForm.authorize();
                    mLoadingDialog = StyledDialog.buildLoading().setActivity(this).show();
                }else {
                    ToastUtils.showShort("请先选择并同意下面条款");
                }
                break;
        }

    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Api.doLogin(LoginActivity.this, params, new OnRequestDataListener() {
                    @Override
                    public void requestSuccess(int code, JSONObject data) {
                        mLoadingDialog.dismiss();

                        SPUtils.getInstance().put("token", data.getString("token"));
                        JSONObject userinfo = data.getJSONObject("data");
                        SPUtils.getInstance().put("balance", userinfo.getString("balance"));
                        SPUtils.getInstance().put("id", userinfo.getString("id"));
                        SPUtils.getInstance().put("avatar", userinfo.getString("avatar"));
                        SPUtils.getInstance().put("user_nicename", userinfo.getString("user_nicename"));
                        SPUtils.getInstance().put("signaling_key", userinfo.getString("signaling_key"));

                        SPUtils.getInstance().put("bgm", "1");
                        SPUtils.getInstance().put("yinxiao", "1");
                        ActivityUtils.startActivity(MainActivity.class);
                        finish();
                    }

                    @Override
                    public void requestFailure(int code, String msg) {
                        toast(msg);
                        mLoadingDialog.dismiss();
                    }
                });
            }
        }
    }
}
