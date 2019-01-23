package kexie.android.dng.entity.users;

public class User
{
    private String headURL;
    private String username;
    private String name;
    private boolean verified;
    private String idCard;
    private String carNumber;

    public void setCarNumber(String carNumber)
    {
        this.carNumber = carNumber;
    }

    public String getCarNumber()
    {
        return carNumber;
    }

    public void setHeadURL(String headURL)
    {
        this.headURL = headURL;
    }

    public String getHeadURL()
    {
        return headURL;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getName()
    {
        return name;
    }

    public void setVerified(boolean verified)
    {
        this.verified = verified;
    }

    public boolean getVerified()
    {
        return verified;
    }

    public String getUsername()
    {
        return username;
    }
}

