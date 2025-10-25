package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

public class RegisterMerchandiseActivity extends AppCompatActivity {

    private EditText etDescription, etWeight, etVolume, etAddress;
    private Button btnRegister;
    private Db db;
    private int userId = 1; // ⚠️ Temporal: reemplazar con ID real del usuario logueado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_merchandise);

        db = new Db(this);

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

        long id = db.createMerchandise(userId, desc, weight, volume, address);
        if (id > 0) {
            Toast.makeText(this, "Mercancía registrada correctamente", Toast.LENGTH_SHORT).show();
            etDescription.setText("");
            etWeight.setText("");
            etVolume.setText("");
            etAddress.setText("");
        } else {
            Toast.makeText(this, "Error al registrar mercancía", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
