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

public class CommentActivity extends AppCompatActivity {
    private EditText comment;
    private Button btn_leave_comment;
    private String post_title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        comment = findViewById(R.id.edit_comment);
        btn_leave_comment = findViewById(R.id.btn_leave_comment);

        post_title = getIntent().getExtras().getString("title");

        btn_leave_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeaveComment(post_title, comment.getText().toString());
            }
        });
    }

    private void LeaveComment(String post_title, String comment){
        JSONObject jsonObject = new JSONObject();
        JSONObject j_comment = new JSONObject();
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);
        String login_id = auth.getString("login_id", "");
        try{
            jsonObject.put("post_title", post_title);
            j_comment.put("comment_owner", login_id);
            j_comment.put("comment_content", comment);
            jsonObject.put("comments",j_comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jsonObject);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder().url("https://androidkbackend.herokuapp.com/commentPost").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CommentActivity.this, "Fail", Toast.LENGTH_SHORT).show();
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
                            JSONObject result = new JSONObject(responseString);
                            switch(result.getInt("status")){
                                case 201:
                                    Toast.makeText(getApplicationContext(), "Leave comment Successful", Toast.LENGTH_LONG).show();
                                    finish();
                                    return;
                                default:
                                    Toast.makeText(getApplicationContext(), "Leave comment Fail with unknown error", Toast.LENGTH_LONG).show();
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
