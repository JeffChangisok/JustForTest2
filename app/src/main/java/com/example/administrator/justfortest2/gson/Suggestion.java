package com.example.administrator.justfortest2.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("drsg")
    public Cloth cloth;

    @SerializedName("trav")
    public Travel travel;

    public Sport sport;

    public Air air;

    public class Air{

        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;
    }

    public class Comfort {

        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;

    }

    public class Cloth {

        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;

    }

    public class Travel{

        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;
    }

    public class Sport {

        @SerializedName("brf")
        public String title;

        @SerializedName("txt")
        public String info;

    }

}
