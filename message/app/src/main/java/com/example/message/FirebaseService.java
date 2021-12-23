package com.example.message;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
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
}
