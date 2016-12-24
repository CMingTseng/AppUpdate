package io.github.skyhacker2.aboutpage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import io.github.skyhacker2.updater.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AboutPage aboutPage = new AboutPage(this)
                .addGroup(getString(R.string.app_lib_about_us))
                .addDeveloper(getString(R.string.app_lib_developer))
                .addRate(getString(R.string.app_lib_rate))
                .addEmail(getString(R.string.app_lib_feedback), "skyhacker@126.com")
                .addShare(getString(R.string.app_lib_share), "推荐你使用小磁力BT")
                .addAliPay(getString(R.string.app_lib_alipay))
                .addGroup(getString(R.string.app_lib_more_app));

        List<AboutItem> apps = MyAppInfos.getInstance(this).getApps();
        for (AboutItem item : apps) {
            aboutPage.addItem(item);
        }

        setContentView(aboutPage.build());
    }
}
