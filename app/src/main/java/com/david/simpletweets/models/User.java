package com.david.simpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.david.simpletweets.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by David on 3/23/2017.
 */
@Table(database = MyDatabase.class)
@org.parceler.Parcel(analyze = {User.class})
public class User extends BaseModel implements Parcelable {

    @Column
    private String name;

    @Column
    @PrimaryKey
    private long uid;

    @Column
    private String screenName;

    @Column
    private String profileImageUrl;

    @Column
    private boolean isLoggedInUser;

    @Column
    private String tagLine;

    @Column
    private int followersCount;

    @Column
    private int followingsCount;

    public User() {
    }

    public static User fromJSON(JSONObject jsonObject) {
        User user = null;
        try {
            long uid = jsonObject.getLong("id");
            User existingUser = SQLite.select()
                    .from(User.class)
                    .where(User_Table.uid.eq(uid))
                    .querySingle();

            if (existingUser != null) {
                user = existingUser;
            } else {
                user = new User();
                user.isLoggedInUser = false;
            }

            user.name = jsonObject.getString("name");
            user.uid = uid;
            user.screenName = "@" + jsonObject.getString("screen_name");
            user.profileImageUrl = jsonObject.getString("profile_image_url");
            user.tagLine = jsonObject.getString("description");
            user.followersCount = jsonObject.getInt("followers_count");
            user.followingsCount = jsonObject.getInt("friends_count");
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            user.save();
        }

        return user;
    }

    public String getName() {
        return name;
    }

    public long getUid() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isLoggedInUser() {
        return isLoggedInUser;
    }

    public void setLoggedInUser(boolean loggedInUser) {
        isLoggedInUser = loggedInUser;
    }

    public String getTagLine() {
        return tagLine;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFollowingsCount() {
        return followingsCount;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public void setFollowingsCount(int followingsCount) {
        this.followingsCount = followingsCount;
    }

    private User(Parcel in) {
        this.name = in.readString();
        this.uid = in.readLong();
        this.screenName = in.readString();
        this.profileImageUrl = in.readString();
        this.isLoggedInUser = in.readByte() != 0;
        this.tagLine = in.readString();
        this.followersCount = in.readInt();
        this.followingsCount = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeLong(this.uid);
        parcel.writeString(this.screenName);
        parcel.writeString(this.profileImageUrl);
        parcel.writeByte((byte) (this.isLoggedInUser ? 1 : 0));
        parcel.writeString(this.tagLine);
        parcel.writeInt(this.followersCount);
        parcel.writeInt(this.followingsCount);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int i) {
            return new User[i];
        }
    };
}
