package com.example.assignment;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DetailPost {
    String comment_owner;
    String comment_contents;
    public DetailPost(String comment_owner, String comment_contents){
        this.comment_owner = comment_owner;
        this.comment_contents = comment_contents;
    }

    public String getComment_owner(){
        return comment_owner;
    }

    public String getComment_contents(){
        return comment_contents;
    }
}
