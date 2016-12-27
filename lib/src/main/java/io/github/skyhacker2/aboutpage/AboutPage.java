package io.github.skyhacker2.aboutpage;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import io.github.skyhacker2.updater.BuildConfig;
import io.github.skyhacker2.updater.R;

/**
 * Created by eleven on 2016/12/22.
 */

public class AboutPage {
    private Context mContext;
    private View mView;
    private Toolbar mToolbar;

    public AboutPage(Context context) {
        mContext = context;
        mView = LayoutInflater.from(context).inflate(R.layout.aboutpage, null, false);
        setAppIcon(mContext.getApplicationInfo().icon);

        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(mContext.getPackageName(), 0);
            setAppVersion("Version: " + info.versionName + " (" + info.versionCode + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        setAppName(mContext.getApplicationInfo().labelRes);
    }

    public AboutPage setAppIcon(int resId) {
        ImageView imageView = getView(R.id.icon);
        imageView.setImageResource(resId);
        return this;
    }

    public AboutPage setAppName(int resId) {
        TextView appName = getView(R.id.appName);
        appName.setText(resId);
        return this;
    }

    public AboutPage setAppVersion(String versionStr) {
        TextView appVersion = getView(R.id.appVersion);
        appVersion.setText(versionStr);
        return this;
    }

    public AboutPage setAboutText(String text) {
        TextView textView = getView(R.id.aboutText);
        textView.setText(text);
        return this;
    }

    public AboutPage setToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
//        ViewGroup container = (ViewGroup) mView;
//        container.addView(toolbar, 0);

        return this;
    }

    public AboutPage addGroup(String groupName) {
        View groupView = LayoutInflater.from(mContext).inflate(R.layout.about_section, null, false);
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mContext.getResources().getDimensionPixelSize(R.dimen.about_section_height));
        ((ViewGroup)mView).addView(groupView, params);

        TextView textView = (TextView) groupView.findViewById(R.id.title);
        textView.setText(groupName);

        return this;
    }

    public AboutPage addItem(AboutItem item) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.about_item, null, false);
        if (item.title == null) {
            Log.e("AboutPage", "AboutItem title不能为空");
            return this;
        }
        if (item.subtitle == null) {
            itemView.findViewById(R.id.subtitle).setVisibility(View.GONE);
        }
        ImageView iconView = (ImageView) itemView.findViewById(R.id.image);
        TextView titleView = (TextView)itemView.findViewById(R.id.title);
        TextView subtitleView = (TextView) itemView.findViewById(R.id.subtitle);
        titleView.setText(item.title);

        if (item.titleColor != 0) {
            titleView.setTextColor(item.titleColor);
        }
        if (item.icon != null) {
            iconView.setImageBitmap(item.icon);
        }
        if (item.subtitle != null) {
            subtitleView.setText(item.subtitle);
        }
        if (item.subtitleColor != 0) {
            subtitleView.setTextColor(item.subtitleColor);
        }

        if (item.clickListener != null) {
            itemView.setOnClickListener(item.clickListener);
        }

        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mContext.getResources().getDimensionPixelSize(R.dimen.about_item_height));
        ((ViewGroup)mView).addView(itemView, params);

        return this;
    }

    public AboutPage addEmail(String title, final String email) {
        AboutItem item = new AboutItem();
        item.icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_settings_feedback);
        item.title = title;

        item.clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                mContext.startActivity(Intent.createChooser(intent, "Feedback"));
            }
        };

        addItem(item);


        return this;
    }

    public AboutPage addRate(String title) {
        AboutItem item = new AboutItem();
        item.icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_settings_rate);
        item.title = title;
        item.clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("AboutPage", "package " + mContext.getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName()));
                mContext.startActivity(intent);
            }
        };

        addItem(item);

        return this;
    }

    public AboutPage addDeveloper(String title) {
        AboutItem item = new AboutItem();
        item.icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_settings_developer);
        item.title = title;
        item.clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://skyhacker2.github.io/blog/index.html?about.md"));
                mContext.startActivity(intent);
            }
        };

        addItem(item);

        return this;
    }

    public AboutPage addShare(final String title, final String shareText) {
        AboutItem item = new AboutItem();
        item.icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_settings_share);
        item.title = title;
        item.clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, shareText);
                mContext.startActivity(Intent.createChooser(intent, title));
            }
        };
        addItem(item);

        return this;
    }

    public AboutPage addAliPay(final String title) {
        AboutItem item = new AboutItem();
        item.icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_settings_alipay);
        item.title = title;
        item.clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)
                        mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("支付宝账号", "skyhacker@126.com");
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, "已复制支付宝账号", Toast.LENGTH_LONG).show();
//                showDialog("已复制支付宝账号", "账号:skyhacker@126.com\n请打开支付宝粘贴\n");
            }
        };
        addItem(item);

        return this;
    }


    public <T extends View> T getView(int id) {
        return (T)mView.findViewById(id);
    }

    public View build() {

        LinearLayout root = new LinearLayout(mContext);
        root.setOrientation(LinearLayout.VERTICAL);
        if (mToolbar != null) {
            root.addView(mToolbar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mContext.getResources().getDimensionPixelSize(R.dimen.about_action_bar_height)));
        }

        ScrollView container = new ScrollView(mContext);
        container.addView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(container, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return root;
    }
}
