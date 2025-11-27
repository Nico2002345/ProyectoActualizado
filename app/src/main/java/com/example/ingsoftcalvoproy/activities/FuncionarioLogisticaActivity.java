package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.dto.ShipmentDTO;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FuncionarioLogisticaActivity extends AppCompatActivity {

    private Button btnObtenerGuias, btnBalancearCargas;
    private ListView lvGuias;
    private TextView tvGuiasTitle;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funcionario_logistico);

        api = ApiClient.getClient().create(ApiService.class);

        btnObtenerGuias = findViewById(R.id.btnObtenerGuias);
        btnBalancearCargas = findViewById(R.id.btnBalancearCargas);
        lvGuias = findViewById(R.id.lvGuias);
        tvGuiasTitle = findViewById(R.id.tvGuiasTitle);

        btnObtenerGuias.setOnClickListener(v -> obtenerGuias());
        btnBalancearCargas.setOnClickListener(v -> balancearCargas());
    }

    /** Obtiene todas las guías (shipments) */
    private void obtenerGuias() {
        api.getAllShipmentsFromAPI().enqueue(new Callback<List<ShipmentDTO>>() {
            @Override
            public void onResponse(Call<List<ShipmentDTO>> call, Response<List<ShipmentDTO>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    Utils.toast(FuncionarioLogisticaActivity.this, "No hay guías disponibles.");
                    tvGuiasTitle.setVisibility(View.GONE);
                    lvGuias.setVisibility(View.GONE);
                    return;
                }

                // Preparar los datos para el ListView
                List<String> guiasData = new ArrayList<>();
                for (ShipmentDTO s : response.body()) {
                    String guiaInfo = String.format(
                            "📦 %s\n" +
                                    "Estado: %s\n" +
                                    "Destino: %s\n" +
                                    "Peso: %.2f kg | Volumen: %.3f m³ | Distancia: %.1f km",
                            s.getShipmentCode(),
                            s.getStatus(),
                            s.getReceiverAddress(),
                            s.getWeightKg(),
                            s.getVolumeM3(),
                            s.getDistanceKm()
                    );
                    guiasData.add(guiaInfo);
                }

                // Configurar el adaptador del ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        FuncionarioLogisticaActivity.this,
                        android.R.layout.simple_list_item_1,
                        guiasData
                );
                lvGuias.setAdapter(adapter);

                // Mostrar el título y la lista
                tvGuiasTitle.setVisibility(View.VISIBLE);
                lvGuias.setVisibility(View.VISIBLE);

                Utils.toast(FuncionarioLogisticaActivity.this,
                        "✅ Se encontraron " + response.body().size() + " guías");
            }

            @Override
            public void onFailure(Call<List<ShipmentDTO>> call, Throwable t) {
                Utils.toast(FuncionarioLogisticaActivity.this, "Error cargando guías: " + t.getMessage());
                tvGuiasTitle.setVisibility(View.GONE);
                lvGuias.setVisibility(View.GONE);
            }
        });
    }

    /** Balancea cargas abriendo la actividad de reasignación */
    private void balancearCargas() {
        Intent intent = new Intent(this, BalancearCargasActivity.class);
        startActivity(intent);
    }
}