package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.dto.ShipmentDTO;
import com.example.ingsoftcalvoproy.dto.UserShipmentsDTO;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersShipmentsActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_shipments);

        webView = findViewById(R.id.webViewUsersShipments); // ✅ usar R.id
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Habilitar JS si tu HTML lo necesita
        webView.setWebViewClient(new WebViewClient());

        fetchUsersShipments();
    }

    private void fetchUsersShipments() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<UserShipmentsDTO>> call = apiService.getUsersWithShipments();

        call.enqueue(new Callback<List<UserShipmentsDTO>>() {
            @Override
            public void onResponse(Call<List<UserShipmentsDTO>> call, Response<List<UserShipmentsDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserShipmentsDTO> users = response.body();
                    String html = generateHtml(users);
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                } else {
                    Utils.toast(UsersShipmentsActivity.this, "No se recibieron datos");
                }
            }

            @Override
            public void onFailure(Call<List<UserShipmentsDTO>> call, Throwable t) {
                Utils.toast(UsersShipmentsActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private String generateHtml(List<UserShipmentsDTO> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>")
                .append("<style>")
                .append("body { font-family: sans-serif; padding: 16px; background: #E3F2FD; }")
                .append(".user { margin-bottom: 24px; padding: 12px; border: 1px solid #90CAF9; border-radius: 8px; background: #FFFFFF; }")
                .append(".user h2 { margin: 0; color: #1565C0; }")
                .append(".shipment { margin-left: 12px; padding: 6px; border-bottom: 1px solid #B3E5FC; }")
                .append("</style>")
                .append("</head><body>");

        for (UserShipmentsDTO user : users) {
            sb.append("<div class='user'>")
                    .append("<h2>").append(user.getName()).append(" (").append(user.getEmail()).append(")</h2>");

            if (user.getShipments() != null && !user.getShipments().isEmpty()) {
                for (ShipmentDTO s : user.getShipments()) {
                    sb.append("<div class='shipment'>")
                            .append("Código: ").append(s.getShipmentCode()).append("<br>")
                            .append("Objeto: ").append(s.getObjectDesc()).append("<br>")
                            .append("Dirección: ").append(s.getReceiverAddress()).append("<br>")
                            .append("Peso: ").append(s.getWeightKg()).append(" kg | Vol: ").append(s.getVolumeM3()).append(" m³<br>")
                            .append("Distancia: ").append(s.getDistanceKm()).append(" km<br>")
                            .append("Estado: ").append(s.getStatus())
                            .append("</div>");
                }
            } else {
                sb.append("<div class='shipment'>No tiene envíos.</div>");
            }

            sb.append("</div>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }
}

