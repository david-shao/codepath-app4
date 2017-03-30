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
    private String body;

    @Column
    @PrimaryKey
    private long uid; //unique id for tweet

    @Column
    @ForeignKey(saveForeignKeyModel = true)
    private User user;

    @Column
    private String createdAt;

    @Column
    private String mediaUrl;

    @Column
    private String mediaType;

    @Column
    private String videoUrl;

    private static long oldestId = Long.MAX_VALUE;
    private static long newestId = Long.MIN_VALUE;

    public Tweet() {
    }

    //deserialize json and build Tweet object
    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();

        //extract values from json
        try {
            tweet.body = jsonObject.getString("text");
            tweet.uid = jsonObject.getLong("id");
            if (tweet.uid > newestId) {
                newestId = tweet.uid;
            }
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
                //if the id is the same as what we have already, it's a dupe
                long uid = tweet.getUid();
                if (uid == oldestId) {
                    continue;
                }
                //keep oldest and newest ids around for pagination
                if (uid < oldestId) {
                    oldestId = uid;
                }
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

    public static long getOldestId() {
        return oldestId;
    }

    public static long getNewestId() {
        return newestId;
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

    private Tweet(Parcel in) {
        this.body = in.readString();
        this.uid = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.createdAt = in.readString();
        this.mediaType = in.readString();
        this.mediaUrl = in.readString();
        this.videoUrl = in.readString();
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
