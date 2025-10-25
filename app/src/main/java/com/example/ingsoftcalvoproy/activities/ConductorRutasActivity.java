package com.example.ingsoftcalvoproy.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

import java.util.ArrayList;
import java.util.List;

public class ConductorRutasActivity extends AppCompatActivity {

    private EditText etConductorName, etConductorEmail, etTruckPlate, etTruckKg, etTruckM3;
    private Spinner spCamiones;
    private ListView lvEnvios;
    private Button btnGuardarConductor, btnAddTruck, btnAsignar;

    private Db db;
    private List<Integer> truckIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_rutas);

        db = new Db(this);

        // Inicializar vistas
        etConductorName = findViewById(R.id.etConductorName);
        etConductorEmail = findViewById(R.id.etConductorEmail);
        etTruckPlate = findViewById(R.id.etTruckPlate);
        etTruckKg = findViewById(R.id.etTruckKg);
        etTruckM3 = findViewById(R.id.etTruckM3);
        spCamiones = findViewById(R.id.spinnerCamiones);
        lvEnvios = findViewById(R.id.lvEnvios);
        btnGuardarConductor = findViewById(R.id.btnGuardarConductor);
        btnAddTruck = findViewById(R.id.btnAddTruck);
        btnAsignar = findViewById(R.id.btnGuardar);

        truckIds = new ArrayList<>();

        loadTrucksSpinner();
        loadEnviosList();

        btnGuardarConductor.setOnClickListener(v -> saveConductor());
        btnAddTruck.setOnClickListener(v -> addTruck());
        btnAsignar.setOnClickListener(v -> assignRoutes());
    }

    private void loadTrucksSpinner() {
        Cursor c = db.raw("SELECT id, plate FROM trucks WHERE active=1", null);
        List<String> trucks = new ArrayList<>();
        truckIds.clear();
        if (c.moveToFirst()) {
            do {
                trucks.add(c.getString(1));
                truckIds.add(c.getInt(0));
            } while (c.moveToNext());
        }
        c.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, trucks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCamiones.setAdapter(adapter);
    }

    private void loadEnviosList() {
        Cursor c = db.raw("SELECT id, address FROM pickups WHERE status='PENDIENTE'", null);
        List<String> envios = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                envios.add(c.getString(1));
            } while (c.moveToNext());
        }
        c.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, envios);
        lvEnvios.setAdapter(adapter);
        lvEnvios.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void saveConductor() {
        String nombre = etConductorName.getText().toString().trim();
        String email = etConductorEmail.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos del conductor", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.exists("users", "email", email)) {
            Toast.makeText(this, "El conductor ya está registrado", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put("name", nombre);
        cv.put("email", email);
        cv.put("password", "1234");
        cv.put("role", "Conductor");

        long id = db.insert("users", cv);
        if (id > 0) {
            Toast.makeText(this, "Conductor guardado correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al guardar conductor", Toast.LENGTH_SHORT).show();
        }
    }

    private void addTruck() {
        String plate = etTruckPlate.getText().toString().trim();
        String kgStr = etTruckKg.getText().toString().trim();
        String m3Str = etTruckM3.getText().toString().trim();

        if (plate.isEmpty() || kgStr.isEmpty() || m3Str.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos del camión", Toast.LENGTH_SHORT).show();
            return;
        }

        double kg, m3;
        try {
            kg = Double.parseDouble(kgStr);
            m3 = Double.parseDouble(m3Str);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put("plate", plate);
        cv.put("capacity_kg", kg);
        cv.put("capacity_m3", m3);
        cv.put("active", 1);

        long id = db.insert("trucks", cv);
        if (id > 0) {
            Toast.makeText(this, "Camión agregado correctamente", Toast.LENGTH_SHORT).show();
            loadTrucksSpinner();
        } else {
            Toast.makeText(this, "Error al agregar camión", Toast.LENGTH_SHORT).show();
        }
    }

    private void assignRoutes() {
        int pos = spCamiones.getSelectedItemPosition();
        if (pos < 0) {
            Toast.makeText(this, "Seleccione un camión", Toast.LENGTH_SHORT).show();
            return;
        }
        int truckId = truckIds.get(pos);

        for (int i = 0; i < lvEnvios.getCount(); i++) {
            if (lvEnvios.isItemChecked(i)) {
                String address = (String) lvEnvios.getItemAtPosition(i);
                ContentValues cv = new ContentValues();
                cv.put("truck_id", truckId);
                cv.put("status", "ASIGNADO");
                db.update("pickups", cv, "address=?", new String[]{address});
            }
        }

        Toast.makeText(this, "Rutas asignadas correctamente", Toast.LENGTH_SHORT).show();
        loadEnviosList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
