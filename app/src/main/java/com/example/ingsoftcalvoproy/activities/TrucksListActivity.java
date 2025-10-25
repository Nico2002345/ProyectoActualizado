package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.ArrayList;

/**
 * Lista todos los camiones registrados en la base de datos.
 * Muestra placa, capacidad y estado (activo/inactivo).
 */
public class TrucksListActivity extends AppCompatActivity {

    private Db db;
    private ListView lvTrucks;
    private ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trucks_list);

        db = new Db(this);
        lvTrucks = findViewById(R.id.lvTrucks);

        loadTrucks();
    }

    /**
     * Carga los camiones desde la base de datos y los muestra en la lista.
     */
    private void loadTrucks() {
        data.clear();

        Cursor c = db.raw("""
            SELECT plate, capacity_kg, active 
            FROM trucks 
            ORDER BY id DESC
        """, null);

        if (c.getCount() == 0) {
            data.add("🚫 No hay camiones registrados en el sistema.");
        } else {
            while (c.moveToNext()) {
                String plate = c.getString(0);
                double capacity = c.getDouble(1);
                int active = c.getInt(2);
                String status = active == 1 ? "Activo ✅" : "Inactivo ❌";

                data.add("🚛 Placa: " + plate +
                        "\nCapacidad: " + capacity + " kg" +
                        "\nEstado: " + status);
            }
        }

        c.close();

        lvTrucks.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
