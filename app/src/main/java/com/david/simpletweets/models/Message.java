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
 * Created by David on 4/2/2017.
 */

@Table(database = MyDatabase.class)
@org.parceler.Parcel(analyze = {Message.class})
public class Message extends BaseModel implements Parcelable {
    @Column
    @PrimaryKey
    long uid; //unique id for message

    @Column
    @ForeignKey(saveForeignKeyModel = true)
    User recipient;

    @Column
    @ForeignKey(saveForeignKeyModel = true)
    User sender;

    @Column
    String createdAt;

    @Column
    String body;

    public Message() {
    }

    public static Message fromJSON(JSONObject jsonObject) {
        Message msg = new Message();

        //extract values from json
        try {
            msg.uid = jsonObject.getLong("id");
            msg.body = jsonObject.getString("text");
            msg.createdAt = jsonObject.getString("created_at");
            msg.recipient = User.fromJSON(jsonObject.getJSONObject("recipient"));
            msg.sender = User.fromJSON(jsonObject.getJSONObject("sender"));
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            msg.save();
        }

        return msg;
    }

    public static List<Message> fromJSONArray(JSONArray jsonArray) {
        List<Message> msgs = new ArrayList<>();

        //iterate json array and create messages
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject msgJson = jsonArray.getJSONObject(i);
                Message msg = Message.fromJSON(msgJson);
                msgs.add(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return msgs;
    }

    public String getRelativeTimeAgo() {
        String relativeDate = SimpleDateUtils.getRelativeTimeAgo(this.getCreatedAt());
        return relativeDate;
    }

    public long getUid() {
        return uid;
    }

    public User getRecipient() {
        return recipient;
    }

    public User getSender() {
        return sender;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getBody() {
        return body;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setBody(String body) {
        this.body = body;
    }

    private Message(Parcel in) {
        this.uid = in.readLong();
        this.recipient = in.readParcelable(User.class.getClassLoader());
        this.sender = in.readParcelable(User.class.getClassLoader());
        this.createdAt = in.readString();
        this.body = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.uid);
        parcel.writeParcelable(this.recipient, i);
        parcel.writeParcelable(this.sender, i);
        parcel.writeString(this.createdAt);
        parcel.writeString(this.body);
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel parcel) {
            return new Message(parcel);
        }

        @Override
        public Message[] newArray(int i) {
            return new Message[i];
        }
    };
}
