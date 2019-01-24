package kexie.android.dng.entity.users;

public class SimpleUser
{
    public final String username;
    public final String name;
    public final boolean verified;
    public final String idCard;
    public final String carNumber;
    public final String phone;

    private SimpleUser(Builder builder)
    {
        username = builder.username;
        name = builder.name;
        verified = builder.verified;
        idCard = builder.idCard;
        carNumber = builder.carNumber;
        this.phone = builder.phone;
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

        public void phone(String phone)
        {
            this.phone = phone;
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

        public SimpleUser build()
        {
            return new SimpleUser(this);
        }
    }
}
