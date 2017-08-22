package com.myfitnesspal.nytimes.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tludewig on 8/6/17.
 */

public class Article implements Parcelable {

    private String webUrl;
    private String headline;
    private String thumbnailUrl;

    public String getWebUrl() {
        System.out.println("returning weburl: " + webUrl);
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
     }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(webUrl);
        parcel.writeString(headline);
        parcel.writeString(thumbnailUrl);
    }

    private Article(Parcel in) {
        webUrl = in.readString();
        headline = in.readString();
        thumbnailUrl = in.readString();
    }

    public Article() {}

    public static final Creator<Article> CREATOR
            = new Creator<Article>() {
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        public Article[] newArray(int size) {
            return new Article[size];
        }
    };
}
