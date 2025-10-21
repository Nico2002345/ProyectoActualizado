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
 * Lista de recolectores (collectors).
 */
public class CollectorsListActivity extends AppCompatActivity {

    private Db db;
    private ListView lvCollectors;
    private ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collectors_list);

        db = new Db(this);
        lvCollectors = findViewById(R.id.lvCollectors);

        loadCollectors();
    }

    private void loadCollectors() {
        data.clear();
        Cursor c = db.raw("SELECT name, phone FROM collectors ORDER BY id DESC", null);
        while (c.moveToNext()) {
            data.add("Nombre: " + c.getString(0) + " | Tel: " + c.getString(1));
        }
        c.close();

        lvCollectors.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
    }
}
