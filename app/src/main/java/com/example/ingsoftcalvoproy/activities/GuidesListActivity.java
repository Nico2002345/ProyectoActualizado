package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.ArrayList;

/**
 * Lista de guías de envío, con opción de clasificación por peso, volumen y distancia.
 */
public class GuidesListActivity extends AppCompatActivity {

    private Db db;
    private ListView lvGuides;
    private Button btnClassify;
    private ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guides_list);

        db = new Db(this);
        lvGuides = findViewById(R.id.lvGuides);
        btnClassify = findViewById(R.id.btnClassify); // 🔹 Agrega este botón en el XML

        loadGuides();

        // 🔹 Botón para clasificar guías
        btnClassify.setOnClickListener(v -> {
            classifyGuides();
            loadGuides();
            Utils.toast(this, "Guías clasificadas correctamente ✅");
        });
    }

    /**
     * Carga las guías desde la base de datos.
     */
    private void loadGuides() {
        data.clear();
        Cursor c = db.raw("""
            SELECT g.guide_number, 
                   s.weight_kg, s.volume_m3, s.distance_km, 
                   s.id AS shipment_id
            FROM guides g
            JOIN shipments s ON s.id = g.shipment_id
            ORDER BY g.id DESC
        """, null);

        while (c.moveToNext()) {
            String guide = c.getString(0);
            double weight = c.getDouble(1);
            double volume = c.getDouble(2);
            double distance = c.getDouble(3);
            int shipmentId = c.getInt(4);

            String weightClass = Utils.classifyWeight(weight);
            String volumeClass = Utils.classifyVolume(volume);
            String distanceClass = Utils.classifyDistance(distance);

            data.add("Guía: " + guide +
                    "\nPeso: " + weight + " kg (" + weightClass + ")" +
                    " | Vol: " + volume + " m³ (" + volumeClass + ")" +
                    "\nDistancia: " + distance + " km (" + distanceClass + ")" +
                    "\nEnvío ID: " + shipmentId);
        }
        c.close();

        lvGuides.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
    }

    /**
     * Clasifica las guías en la BD (actualiza buckets de peso, volumen y distancia).
     */
    private void classifyGuides() {
        db.classifyGuides();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
