package org.kexie.android.dng.ai.model.entity;

public class AIRequest
{
    /**
     * reqType : 0
     * perception : {"inputText":{"text":"附近的酒店"},"inputImage":{"url":"imageUrl"},"selfInfo":{"location":{"city":"北京","province":"北京","street":"信息路"}}}
     * userInfo : {"apiKey":"","userId":""}
     */

    private int reqType;
    private Perception perception;
    private UserInfo userInfo;

    public int getReqType()
    {
        return reqType;
    }

    public void setReqType(int reqType)
    {
        this.reqType = reqType;
    }

    public Perception getPerception()
    {
        return perception;
    }

    public void setPerception(Perception perception)
    {
        this.perception = perception;
    }

    public UserInfo getUserInfo()
    {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo)
    {
        this.userInfo = userInfo;
    }

    public static class Perception
    {
        /**
         * inputText : {"text":"附近的酒店"}
         * inputImage : {"url":"imageUrl"}
         * selfInfo : {"location":{"city":"北京","province":"北京","street":"信息路"}}
         */

        private InputText inputText;
        private InputImage inputImage;
        private SelfInfo selfInfo;

        public InputText getInputText()
        {
            return inputText;
        }

        public void setInputText(InputText inputText)
        {
            this.inputText = inputText;
        }

        public InputImage getInputImage()
        {
            return inputImage;
        }

        public void setInputImage(InputImage inputImage)
        {
            this.inputImage = inputImage;
        }

        public SelfInfo getSelfInfo()
        {
            return selfInfo;
        }

        public void setSelfInfo(SelfInfo selfInfo)
        {
            this.selfInfo = selfInfo;
        }

        public static class InputText
        {
            /**
             * text : 附近的酒店
             */

            private String text;

            public String getText()
            {
                return text;
            }

            public void setText(String text)
            {
                this.text = text;
            }
        }

        public static class InputImage
        {
            /**
             * url : imageUrl
             */

            private String url;

            public String getUrl()
            {
                return url;
            }

            public void setUrl(String url)
            {
                this.url = url;
            }
        }

        public static class SelfInfo
        {
            /**
             * location : {"city":"北京","province":"北京","street":"信息路"}
             */

            private Location location;

            public Location getLocation()
            {
                return location;
            }

            public void setLocation(Location location)
            {
                this.location = location;
            }

            public static class Location
            {
                /**
                 * city : 北京
                 * province : 北京
                 * street : 信息路
                 */

                private String city;
                private String province;
                private String street;

                public String getCity()
                {
                    return city;
                }

                public void setCity(String city)
                {
                    this.city = city;
                }

                public String getProvince()
                {
                    return province;
                }

                public void setProvince(String province)
                {
                    this.province = province;
                }

                public String getStreet()
                {
                    return street;
                }

                public void setStreet(String street)
                {
                    this.street = street;
                }
            }
        }
    }

    public static class UserInfo
    {
        /**
         * apiKey :
         * userId :
         */

        private String apiKey;
        private String userId;

        public String getApiKey()
        {
            return apiKey;
        }

        public void setApiKey(String apiKey)
        {
            this.apiKey = apiKey;
        }

        public String getUserId()
        {
            return userId;
        }

        public void setUserId(String userId)
        {
            this.userId = userId;
        }
    }
}
