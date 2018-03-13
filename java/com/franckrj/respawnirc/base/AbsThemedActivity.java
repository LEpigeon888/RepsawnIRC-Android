package com.franckrj.respawnirc.base;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.ThemeManager;

public abstract class AbsThemedActivity extends AppCompatActivity {
    protected static ActivityManager.TaskDescription generalTaskDesc = null;
    protected static @ColorInt int colorUsedForGenerateTaskDesc = 0;
    protected ThemeManager.ThemeName lastThemeUsed = null;
    protected boolean toolbarTextColorIsInverted = false;
    protected int lastPrimaryColorUsed = -1;
    protected int lastTopicNameAndLinkColorUsed = -1;
    protected @StyleRes int colorAccentStyle = 0;
    protected boolean windowDrawStatusBar = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeManager.changeActivityTheme(this);
        ThemeManager.changeActivityToolbarTextColor(this);
        ThemeManager.changeActivityThemedColorsIfNeeded(this);
        lastThemeUsed = ThemeManager.getThemeUsed();
        toolbarTextColorIsInverted = ThemeManager.getToolbarTextColorIsInvertedForThemeLight();
        lastPrimaryColorUsed = ThemeManager.getPrimaryColorIdUsedForThemeLight();
        lastTopicNameAndLinkColorUsed = ThemeManager.getTopicNameAndLinkColorIdUsedForThemeLight();

        if (colorAccentStyle != 0) {
            getTheme().applyStyle(colorAccentStyle, true);
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            if (generalTaskDesc == null || colorUsedForGenerateTaskDesc != ThemeManager.getColorInt(R.attr.colorPrimary, this)) {
                Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rirc);
                colorUsedForGenerateTaskDesc = ThemeManager.getColorInt(R.attr.colorPrimary, this);
                generalTaskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), appIcon, colorUsedForGenerateTaskDesc);
            }
            setTaskDescription(generalTaskDesc);

            if (windowDrawStatusBar) {
                getWindow().setStatusBarColor(ThemeManager.getColorInt(R.attr.colorPrimaryDark, this));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if ((lastThemeUsed != ThemeManager.getThemeUsed()) || (lastPrimaryColorUsed != ThemeManager.getPrimaryColorIdUsedForThemeLight()) ||
                (lastTopicNameAndLinkColorUsed != ThemeManager.getTopicNameAndLinkColorIdUsedForThemeLight()) ||
                (toolbarTextColorIsInverted != ThemeManager.getToolbarTextColorIsInvertedForThemeLight())) {
            recreate();
        }
    }
}
