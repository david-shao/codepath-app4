package com.david.simpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.david.simpletweets.MyDatabase;
import com.david.simpletweets.utils.SimpleDateUtils;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David on 3/23/2017.
 */

//parse json and store data, encapsulate state logic or display logic
@Table(database = MyDatabase.class)
@org.parceler.Parcel(analyze = {Tweet.class})
public class Tweet extends BaseModel implements Parcelable {
    //list out attributes
    @Column
    String body;

    @Column
    @PrimaryKey
    long uid; //unique id for tweet

    @Column
    @ForeignKey(saveForeignKeyModel = true)
    User user;

    @Column
    String createdAt;

    @Column
    String mediaUrl;

    @Column
    String mediaType;

    @Column
    String videoUrl;

    @Column
    boolean favorited;

    @Column
    int favoritesCount;

    @Column
    boolean retweeted;

    @Column
    int retweetCount;

    public Tweet() {
    }

    //deserialize json and build Tweet object
    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();

        //extract values from json
        try {
            tweet.body = jsonObject.getString("text");
            tweet.uid = jsonObject.getLong("id");
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
            if (jsonObject.has("extended_entities")) {
                JSONObject extEnt = jsonObject.getJSONObject("extended_entities");
                JSONArray media = extEnt.getJSONArray("media");
                JSONObject firstMedia = media.getJSONObject(0);
                String type = firstMedia.getString("type");
                tweet.mediaType = type;
                //parse image url
                if (type.equals("photo")) {
                    tweet.mediaUrl = firstMedia.getString("media_url");
                } else if (type.equals("video")) {
                    tweet.mediaUrl = firstMedia.getString("media_url");
                    //video url is more deeply nested
                    JSONObject videoInfo = firstMedia.getJSONObject("video_info");
                    JSONArray vars = videoInfo.getJSONArray("variants");
                    for (int i = 0; i < vars.length(); i++) {
                        //just pick the first available video url for now
                        JSONObject vid = vars.getJSONObject(i);
                        if (vid.getString("content_type").equals("video/mp4")) {
                            tweet.videoUrl = vid.getString("url");
                            break;
                        }
                    }
                }
            }
            tweet.favorited = jsonObject.getBoolean("favorited");
            tweet.favoritesCount = jsonObject.getInt("favorite_count");
            tweet.retweeted = jsonObject.getBoolean("retweeted");
            tweet.retweetCount = jsonObject.getInt("retweet_count");
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            tweet.save();
        }

        //return tweet object
        return tweet;
    }

    public static List<Tweet> fromJSONArray(JSONArray jsonArray) {
        List<Tweet> tweets = new ArrayList<>();

        //iterate json array and create tweets
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject tweetJson = jsonArray.getJSONObject(i);
                Tweet tweet = Tweet.fromJSON(tweetJson);
                tweets.add(tweet);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return tweets;
    }

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public User getUser() {
        return user;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public int getFavoritesCount() {
        return favoritesCount;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public void setFavoritesCount(int favoritesCount) {
        this.favoritesCount = favoritesCount;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    private Tweet(Parcel in) {
        this.body = in.readString();
        this.uid = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.createdAt = in.readString();
        this.mediaType = in.readString();
        this.mediaUrl = in.readString();
        this.videoUrl = in.readString();
        this.favorited = in.readByte() != 0;
        this.favoritesCount = in.readInt();
        this.retweeted = in.readByte() != 0;
        this.retweetCount = in.readInt();
    }

    public String getRelativeTimeAgo() {
        String relativeDate = SimpleDateUtils.getRelativeTimeAgo(this.getCreatedAt());
        return relativeDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.body);
        parcel.writeLong(this.uid);
        parcel.writeParcelable(this.user, i);
        parcel.writeString(this.createdAt);
        parcel.writeString(this.mediaType);
        parcel.writeString(this.mediaUrl);
        parcel.writeString(this.videoUrl);
        parcel.writeByte((byte) (this.favorited ? 1 : 0));
        parcel.writeInt(this.favoritesCount);
        parcel.writeByte((byte) (this.retweeted ? 1 : 0));
        parcel.writeInt(this.retweetCount);
    }

    public static final Parcelable.Creator<Tweet> CREATOR = new Parcelable.Creator<Tweet>() {
        @Override
        public Tweet createFromParcel(Parcel parcel) {
            return new Tweet(parcel);
        }

        @Override
        public Tweet[] newArray(int i) {
            return new Tweet[i];
        }
    };
}
