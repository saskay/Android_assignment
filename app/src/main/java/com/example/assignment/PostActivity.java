package com.example.assignment;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostActivity extends AppCompatActivity {
    private ArrayList<DetailPost> detailPosts = new ArrayList();
    private ListView listView;
    private String title;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_post);

            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();

            listView = findViewById(R.id.details_post_list);
            TextView post_title = findViewById(R.id.post_title);

            title = getIntent().getExtras().getString("title");
            post_title.setText(title);
            getPostWithTitle(title);
        }

    private void getPostWithTitle(String title){
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder Builder = HttpUrl.parse("https://androidkbackend.herokuapp.com/getPostWithTitle").newBuilder();
        Builder.addQueryParameter("post_title",title);
        String url = Builder.build().toString();
        final Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseString = response.body().string();
                try {
                    final JSONObject result = new JSONObject(responseString);
                    PostActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                JSONArray temp =  result.getJSONArray("post_comments");
                                int len = temp.length();
                                System.out.println("temp: " + temp);
                                for(int i = 0; i < len; i++){
                                    JSONObject current = (JSONObject) temp.getJSONObject(i);
                                    String comment_owner = current.getString("comment_owner");
                                    String comment_content = current.getString("comment_content");
                                    detailPosts.add(new DetailPost(comment_owner, comment_content));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            rowAdapter adapter = new rowAdapter(PostActivity.this, R.layout.row, detailPosts);
                            listView.setAdapter(adapter);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
