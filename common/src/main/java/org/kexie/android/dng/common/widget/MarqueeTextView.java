package org.kexie.android.dng.common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by Rukey7 on 2016/11/14.
 * 跑马灯TextView
 */
public class MarqueeTextView extends AppCompatTextView
{

    public MarqueeTextView(Context context) {
        super(context);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setMarqueeRepeatLimit(-1);
        setSingleLine(true);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
