package com.xingshijie.android.update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;



/**
 *
 * 参考友盟的UpdateDialogActivity实现
 */
public class UpdateDialogActivity extends Activity implements View.OnClickListener {

    private static final String CONFIG = "config";
    private static final String IS_FORCE_UPDATE = "force update";
    Button updateCancel;
    Button updateOk;
    TextView updateContent;
    CheckBox updateCheck;
    Button updateClose;
    ImageView updateWifiIndicator;
    Button updateIgnore;
    private OnlineConfig config;
    private boolean isForceUpdate;

    public static Intent getIntent(Context context, OnlineConfig config, boolean isForceUpdate){
        Intent intent = new Intent(context, UpdateDialogActivity.class);
        intent.putExtra(IS_FORCE_UPDATE, isForceUpdate);
        intent.putExtra(CONFIG, config);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_update_dialog);
        config = (OnlineConfig) getIntent().getSerializableExtra(CONFIG);
        isForceUpdate = getIntent().getBooleanExtra(IS_FORCE_UPDATE, false);

        updateWifiIndicator = (ImageView)findViewById(R.id.umeng_update_wifi_indicator);
        updateClose = (Button)findViewById(R.id.umeng_update_id_close);
        updateClose.setOnClickListener(this);
        updateContent = (TextView)findViewById(R.id.umeng_update_content);
        updateCheck = (CheckBox)findViewById(R.id.umeng_update_id_check);
        updateCheck.setOnClickListener(this);
        updateOk = (Button)findViewById(R.id.umeng_update_id_ok);
        updateOk.setOnClickListener(this);
        updateCancel = (Button)findViewById(R.id.umeng_update_id_cancel);
        updateCancel.setOnClickListener(this);
        updateIgnore = (Button)findViewById(R.id.umeng_update_id_ignore);
        updateIgnore.setOnClickListener(this);

        updateContent.setText(config.getReleaseNotes());
        if (isForceUpdate) {
            updateCheck.setVisibility(View.GONE);
        }

        if (CommonUtil.isWifiConnect(this)) {
            updateWifiIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.umeng_update_id_ok) {
            UpdateUtil.downloadApkAndInstall(this, config);
//            if (isForceUpdate) {
//                updateOk.setVisibility(View.GONE);
//                updateCancel.setEnabled(false);
//                updateCancel.setText("正在下载应用程序");
//            } else {
//                finish();
//            }
            finish();

        } else if (i == R.id.umeng_update_id_cancel) {
//            if (isForceUpdate) {
//                Toast.makeText(this, "不更新将无法使用app", Toast.LENGTH_LONG).show();
//            } else {
//                if (updateCheck.isChecked()) {
//                    UpdateUtil.saveIgnoreVersion(this, config.getLastVersionCode());
//                }
//                finish();
//            }
            finish();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (!isForceUpdate) {
            super.onBackPressed();
        }
    }
}
