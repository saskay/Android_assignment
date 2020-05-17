package com.example.assignment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreatePostActivity extends AppCompatActivity {
    private EditText post_title;
    private EditText post_content;
    private Button btn_create_post;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        post_title = (EditText) findViewById(R.id.post_title);
        post_content = (EditText) findViewById(R.id.post_content);
        btn_create_post = (Button) findViewById(R.id.btn_create_post);

        btn_create_post.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CreatePost(post_title.getText().toString(), post_content.getText().toString());
            }
        });
    }

    private void CreatePost(String post_title, String post_content) {
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);
        String area = auth.getString("area", "");
        String login_id = auth.getString("login_id", "");
        System.out.println("area: " + area + "login_id" + login_id);
        if(area == "" || login_id == ""){
            Toast.makeText(CreatePostActivity.this, "Cannot get current area.", Toast.LENGTH_LONG).show();
        }else{
            if(post_title == null || post_title == "" || post_content == null || post_content == ""){
                System.out.println("Post Title: " + post_title);
                System.out.println("Post Content: " + post_content);
                Toast.makeText(CreatePostActivity.this, "Title and content cannot empty.", Toast.LENGTH_LONG).show();
            }else{
                JSONObject jsonObject = new JSONObject();
                JSONObject j_login_id = new JSONObject();
                JSONObject j_post_content = new JSONObject();
                JSONArray comments = new JSONArray();
                try{
                    jsonObject.put("post_area", area);
                    jsonObject.put("post_title", post_title);
                    j_login_id.put("comment_owner", login_id);
                    j_login_id.put("comment_content", post_content);
                    comments.put(j_login_id);
                    jsonObject.put("post_comments", comments);
                    System.out.println("Pass to DB object: " + jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder().url("https://androidkbackend.herokuapp.com/newposts").post(body).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("IOException", e.getMessage());
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CreatePostActivity.this, "Create fail, maybe due to network problem.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String responseString = response.body().string();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    System.out.println(responseString);
                                    JSONObject result = new JSONObject(responseString);

                                    switch(result.getInt("status")){
                                        case 201:
                                            Toast.makeText(CreatePostActivity.this, "Create post successful", Toast.LENGTH_SHORT).show();
                                            finish();
                                            return;
                                        default:
                                            Toast.makeText(CreatePostActivity.this, "Create post fail with unknown error", Toast.LENGTH_SHORT).show();
                                            return;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        }
    }

}
