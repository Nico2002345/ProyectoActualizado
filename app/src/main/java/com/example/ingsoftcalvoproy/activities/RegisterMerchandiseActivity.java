package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterMerchandiseActivity extends AppCompatActivity {

    private EditText etDescription, etWeight, etVolume, etAddress;
    private Button btnRegister;
    private ApiService apiService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_merchandise);

        // Obtener el userId del Intent
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);

        etDescription = findViewById(R.id.etDescription);
        etWeight = findViewById(R.id.etWeight);
        etVolume = findViewById(R.id.etVolume);
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnRegisterMerchandise);

        btnRegister.setOnClickListener(v -> registerMerchandise());
    }

    private void registerMerchandise() {
        String desc = etDescription.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String volumeStr = etVolume.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (desc.isEmpty() || weightStr.isEmpty() || volumeStr.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight, volume;
        try {
            weight = Double.parseDouble(weightStr);
            volume = Double.parseDouble(volumeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("user", userId);
        body.put("description", desc);
        body.put("weight_kg", weight);
        body.put("volume_m3", volume);
        body.put("address", address);

        apiService.createMerch(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterMerchandiseActivity.this, "Mercancía registrada correctamente", Toast.LENGTH_SHORT).show();
                    etDescription.setText("");
                    etWeight.setText("");
                    etVolume.setText("");
                    etAddress.setText("");
                } else {
                    Toast.makeText(RegisterMerchandiseActivity.this, "Error al registrar mercancía", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RegisterMerchandiseActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}