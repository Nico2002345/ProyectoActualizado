package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

import java.util.ArrayList;

/**
 * Lista de guías de envío.
 */
public class GuidesListActivity extends AppCompatActivity {

    private Db db;
    private ListView lvGuides;
    private ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guides_list);

        db = new Db(this);
        lvGuides = findViewById(R.id.lvGuides);

        loadGuides();
    }

    private void loadGuides() {
        data.clear();
        Cursor c = db.raw("SELECT guide_number, distance_km, shipment_id FROM guides ORDER BY id DESC", null);
        while (c.moveToNext()) {
            data.add("Guía: " + c.getString(0) +
                    " | Distancia: " + c.getDouble(1) + " km | Envío ID: " + c.getInt(2));
        }
        c.close();

        lvGuides.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
    }
}
