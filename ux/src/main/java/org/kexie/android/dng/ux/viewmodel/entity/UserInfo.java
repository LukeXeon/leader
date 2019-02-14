package org.kexie.android.dng.ux.viewmodel.entity;

import android.graphics.drawable.Drawable;

public class UserInfo
{
    public final Drawable headImage;
    public final String username;
    public final String name;
    public final boolean verified;
    public final String idCard;
    public final String carNumber;
    public final String phone;

    private UserInfo(Builder builder)
    {
        headImage = builder.headImage;
        username = builder.username;
        name = builder.name;
        verified = builder.verified;
        idCard = builder.idCard;
        carNumber = builder.carNumber;
        phone = builder.phone;
    }

    public static final class Builder
    {
        private String headURL;
        private String username;
        private String name;
        private boolean verified;
        private String idCard;
        private String carNumber;
        private String phone;
        private Drawable headImage;

        public Builder phone(String phone)
        {
            this.phone = phone;
            return this;
        }

        public String getHeadURL()
        {
            return headURL;
        }

        public Builder()
        {
        }

        public Builder username(String val)
        {
            username = val;
            return this;
        }

        public Builder name(String val)
        {
            name = val;
            return this;
        }

        public Builder verified(boolean val)
        {
            verified = val;
            return this;
        }

        public Builder idCard(String val)
        {
            idCard = val;
            return this;
        }

        public Builder carNumber(String val)
        {
            carNumber = val;
            return this;
        }

        public UserInfo build()
        {
            return new UserInfo(this);
        }

        public Builder headImage(Drawable val)
        {
            headImage = val;
            return this;
        }
    }
}
