package io.github.skyhacker2.elevenappupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import io.github.skyhacker2.updater.OnlineParams;
import io.github.skyhacker2.updater.Updater;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Updater.getInstance(this).setUpdateUrl("https://raw.githubusercontent.com/skyhacker2/skyhacker2.github.com/master/api/apps/AppUpdateDemo/app.json");
        Updater.getInstance(this).setDebug(false);
        Updater.getInstance(this).checkUpdate();

        mTextView = (TextView) findViewById(R.id.text);
        updateTextView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Updater.ACTION_ONLINE_PARAMS_UPDATED);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void updateTextView()
    {
        String ad = OnlineParams.get("ad", "0");
        String showAD = OnlineParams.get("showAd", "false");
        mTextView.setText("ad: " + ad + " showAD " + showAD );
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Updater.ACTION_ONLINE_PARAMS_UPDATED)) {
                Log.d("onReceive",Updater.ACTION_ONLINE_PARAMS_UPDATED );
                updateTextView();
            }
        }
    };
}
