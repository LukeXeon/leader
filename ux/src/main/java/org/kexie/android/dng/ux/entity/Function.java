package org.kexie.android.dng.ux.entity;

import android.graphics.drawable.Drawable;
import android.view.View;

public final class Function
{
    public final Drawable icon;
    public final String name;
    public final View.OnClickListener action;

    private Function(Builder builder)
    {
        icon = builder.icon;
        name = builder.name;
        action = builder.action;
    }

    public static final class Builder
    {
        private Drawable icon;
        private String name;
        private View.OnClickListener action;

        public Builder()
        {
        }

        public Builder icon(Drawable val)
        {
            icon = val;
            return this;
        }

        public Builder name(String val)
        {
            name = val;
            return this;
        }

        public Builder action(View.OnClickListener val)
        {
            action = val;
            return this;
        }

        public Function build()
        {
            return new Function(this);
        }
    }
}
