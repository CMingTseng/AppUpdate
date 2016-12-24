package io.github.skyhacker2.aboutpage;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;

/**
 * Created by eleven on 2016/12/22.
 */

public class AboutItem {
    public String title;
    public String subtitle;
    public Bitmap icon;
    public int titleColor;
    public int subtitleColor;
    public View.OnClickListener clickListener;
    public String packageName;

    public AboutItem(Bitmap icon, String title, int color) {
        this.icon = icon;
        this.title = title;
        this.titleColor = color;
    }

    public AboutItem() {
    }
}
