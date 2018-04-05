package com.example.fidelmomolo.blog;

import com.google.firebase.Timestamp;

import java.util.Date;

/**
 * Created by Fidel M Omolo on 3/31/2018.
 */

public class BlogPost_model_class extends BlogPostId{

    public String description,imageUri,thumbUri,user_id;
    public Date timestamp;

    public BlogPost_model_class() {
    }

    public BlogPost_model_class(String description, String imageUri, String thumbUri, String user_id, Date timestamp) {
        this.description = description;
        this.imageUri = imageUri;
        this.thumbUri = thumbUri;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(String thumbUri) {
        this.thumbUri = thumbUri;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
