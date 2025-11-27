package com.example.ingsoftcalvoproy.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private ListView lvNotifications;
    private TextView tvNotificationsTitle;
    private ApiService api;

    private int userId;
    private final List<Map<String, Object>> notificationsList = new ArrayList<>();

    private final String CHANNEL_ID = "my_notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        api = ApiClient.getClient().create(ApiService.class);

        lvNotifications = findViewById(R.id.lvNotifications);
        tvNotificationsTitle = findViewById(R.id.tvNotificationsTitle);

        setTitle("Notificaciones");

        // -----------------------------
        // Permiso Android 13+
        // -----------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        android.Manifest.permission.POST_NOTIFICATIONS
                }, 100);
            }
        }

        // -----------------------------
        // Crear canal
        // -----------------------------
        createNotificationChannel();

        // -----------------------------
        // Recibir userId
        // -----------------------------
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Utils.toast(this, "Error: Usuario no identificado");
            finish();
            return;
        }

        // Cargar notificaciones
        loadNotifications();

        // Marcar como leída al hacer clic
        lvNotifications.setOnItemClickListener((parent, view, position, id) -> {
            if (position < notificationsList.size()) {
                Map<String, Object> notification = notificationsList.get(position);
                Object idObj = notification.get("id");
                if (idObj instanceof Number) {
                    int notificationId = ((Number) idObj).intValue();
                    markAsRead(notificationId, position);
                }
            }
        });
    }

    // ---------------------------------------------------------
    // Crear canal de notificación
    // ---------------------------------------------------------
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Notificaciones",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription("Canal de notificaciones de la app");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // ---------------------------------------------------------
    // Mostrar notificación en el sistema
    // ---------------------------------------------------------
    private void showSystemNotification(String title, String message) {

        // Crear canal en Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal de notificaciones de la app");

            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        // Intent con USER_ID (ARREGLO IMPORTANTE)
        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.putExtra("USER_ID", userId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Verificar icono
        int iconId = R.drawable.ic_notification;
        try {
            getResources().getResourceName(iconId);
        } catch (Exception e) {
            iconId = android.R.drawable.ic_dialog_info;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    // ---------------------------------------------------------
    // Cargar notificaciones desde la API
    // ---------------------------------------------------------
    private void loadNotifications() {
        api.getUserNotifications(userId).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvNotificationsTitle.setText("No se pudieron cargar las notificaciones");
                    return;
                }

                notificationsList.clear();
                List<String> displayList = new ArrayList<>();

                for (Map<String, Object> notification : response.body()) {
                    notificationsList.add(notification);

                    String title = String.valueOf(notification.get("title"));
                    String message = String.valueOf(notification.get("message"));
                    boolean isRead = notification.get("is_read") instanceof Boolean &&
                            (Boolean) notification.get("is_read");

                    // Mostrar notificación del sistema si NO está leída
                    if (!isRead) {
                        showSystemNotification(
                                title != null ? title : "Nueva notificación",
                                message != null ? message : "Tienes un nuevo mensaje"
                        );
                    }

                    // Mostrar en ListView
                    String readIndicator = isRead ? "" : "🔵 ";
                    displayList.add(readIndicator + title + "\n" + message);
                }

                int unreadCount = 0;
                for (Map<String, Object> n : notificationsList) {
                    if (n.get("is_read") instanceof Boolean && !(Boolean) n.get("is_read"))
                        unreadCount++;
                }

                tvNotificationsTitle.setText("Notificaciones (" + notificationsList.size() +
                        ") - " + unreadCount + " sin leer");

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        NotificationsActivity.this,
                        android.R.layout.simple_list_item_1,
                        displayList
                );
                lvNotifications.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Utils.toast(NotificationsActivity.this,
                        "Error cargando notificaciones: " + t.getMessage());
            }
        });
    }

    // ---------------------------------------------------------
    // Marcar notificación como leída
    // ---------------------------------------------------------
    private void markAsRead(int notificationId, int position) {
        api.markNotificationAsRead(notificationId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    if (position < notificationsList.size()) {
                        notificationsList.get(position).put("is_read", true);
                    }
                    loadNotifications();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(NotificationsActivity.this, "Error: " + t.getMessage());
            }
        });
    }
}
