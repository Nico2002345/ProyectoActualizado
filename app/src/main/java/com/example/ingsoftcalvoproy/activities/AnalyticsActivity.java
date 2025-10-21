package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

/**
 * Muestra cantidad de guías y distancia promedio.
 */
public class AnalyticsActivity extends AppCompatActivity {

    private Db db;
    private TextView tvGuides, tvAvgDist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        db = new Db(this);
        tvGuides = findViewById(R.id.tvGuides);
        tvAvgDist = findViewById(R.id.tvAvgDist);
        loadAnalytics();
    }

    private void loadAnalytics() {
        Cursor c = db.raw("SELECT COUNT(*) AS cnt, AVG(distance_km) AS avgDist FROM guides", null);
        if (c.moveToFirst()) {
            tvGuides.setText("Cantidad de guías: " + c.getInt(0));
            tvAvgDist.setText("Distancia promedio: " + String.format("%.2f km", c.getDouble(1)));
        }
        c.close();
    }
}
