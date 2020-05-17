package com.example.assignment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.*;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PrimaryActivity extends AppCompatActivity {
    private TextView location_name;
    private ListView listView;
//        private String[] post = {"Post 1", "Post 2", "Post 3", "Post 4", "Post 5", "Post 6", "Post 7", "Post 8", "Post 9", "Post 10", "Post 11", "Post 12", "Post 13", "Post 14", "Post 15"};
    private List post = new ArrayList();
    AppLocationService appLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primary);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        location_name = (TextView) findViewById(R.id.location_name);
        listView = (ListView) findViewById(R.id.post_list);
        Button btn_refresh = (Button) findViewById(R.id.btn_refresh);
        Button btn_create_post = (Button) findViewById(R.id.btn_create);

        btn_create_post.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent createPost = new Intent(PrimaryActivity.this, CreatePostActivity.class);
                startActivity(createPost);
            }
        });

        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPostWithLocation(location_name.getText().toString());
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item_text = parent.getItemAtPosition(position).toString().trim();
                    Intent intent = new Intent(PrimaryActivity.this, PostActivity.class);
                    intent.putExtra("title", item_text);
                    startActivity(intent);
            }
        });

//        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, post);
//        listView.setAdapter(adapter);

        appLocationService = new AppLocationService(PrimaryActivity.this);
        location_name.setText("Loading . . .");
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PrimaryActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        PrimaryActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            location_name.setText(locationAddress);
            getPostWithLocation(locationAddress);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Location location = appLocationService.getLocation(LocationManager.GPS_PROVIDER);
                    if (location != null){
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        LocationAddress locationAddress = new LocationAddress();
                        locationAddress.getAddressFromLocation(latitude, longitude,
                                getApplicationContext(), new GeocoderHandler());
                    } else{
                        showSettingsAlert();
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void getPostWithLocation(final String location){
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder Builder = HttpUrl.parse("https://androidkbackend.herokuapp.com/areaposts").newBuilder();
        Builder.addQueryParameter("area",location);
        String url = Builder.build().toString();
        final Request request = new Request.Builder().url(url).build();

       client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final JSONArray ja_response;
                final String temp = response.body().string();
                try {
                    ja_response = new JSONArray(temp);
                    PrimaryActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            post.clear();
                            for (int i = 0; i < ja_response.length(); i ++){
                                try {
                                    JSONObject entry = ja_response.getJSONObject(i);
                                    post.add(entry.getString("post_title"));
                                    Log.i("Title",entry.getString("post_title"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);
                            auth.edit().putString("area", location).commit();
                            ArrayAdapter adapter = new ArrayAdapter(PrimaryActivity.this, android.R.layout.simple_list_item_1, post);
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
