package com.abhishesh.trying.maps;

import java.util.Date;

import twitter4j.GeoLocation;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * Plain Old Java Object class
 * @author Abhishesh
 *
 */
public class TweetBean{
    String userName;
    String body;
    String imageURL;
    GeoLocation location;
    Date date;
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public TweetBean() {
        // TODO Auto-generated constructor stub
    }
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public String getImageURL() {
        return imageURL;
    }
    
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    
    public GeoLocation getLocation() {
        return location;
    }
    
    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        String s = "Name: " + this.userName + "text: " + this.body;
        return s;
    }
}
