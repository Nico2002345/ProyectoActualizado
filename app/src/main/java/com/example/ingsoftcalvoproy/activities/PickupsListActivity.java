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
 * Lista de solicitudes de recogida (pickups).
 */
public class PickupsListActivity extends AppCompatActivity {

    private Db db;
    private ListView lvPickups;
    private ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickups_list);

        db = new Db(this);
        lvPickups = findViewById(R.id.lvPickups);

        loadPickups();
    }

    private void loadPickups() {
        data.clear();
        Cursor c = db.raw("""
            SELECT id, address, status, datetime(created_at) 
            FROM pickups ORDER BY id DESC
        """, null);

        while (c.moveToNext()) {
            data.add("ID: " + c.getInt(0) + " | " + c.getString(1) +
                    " | Estado: " + c.getString(2) +
                    " | Fecha: " + c.getString(3));
        }
        c.close();

        lvPickups.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
    }
}
