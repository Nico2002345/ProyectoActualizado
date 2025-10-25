package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

/**
 * Mide tendencia de los envíos (solo con columnas existentes en la BD):
 * - Promedio, mediana y moda de: peso (kg) y distancia (km)
 * - Cantidad de guías
 */
public class AnalyticsActivity extends AppCompatActivity {

    private Db db;

    private TextView tvCountGuides;
    private TextView tvAvgWeight, tvAvgDist;
    private TextView tvMedianWeight, tvMedianDist;
    private TextView tvModeWeight, tvModeDist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        db = new Db(this);

        // === Referencias a vistas (IDs EMPAREJADOS con el XML) ===
        tvCountGuides   = findViewById(R.id.tvCountGuides);

        tvAvgWeight     = findViewById(R.id.tvAvgWeight);
        tvAvgDist       = findViewById(R.id.tvAvgDist);

        tvMedianWeight  = findViewById(R.id.tvMedianWeight);
        tvMedianDist    = findViewById(R.id.tvMedianDist);

        tvModeWeight    = findViewById(R.id.tvModeWeight);
        tvModeDist      = findViewById(R.id.tvModeDist);

        loadAnalytics();
    }

    private void loadAnalytics() {
        // === Promedios (desde BD) ===
        Cursor cAvg = db.raw(
                "SELECT AVG(weight_kg) AS avg_weight, AVG(distance_km) AS avg_distance FROM shipments",
                null
        );
        if (cAvg.moveToFirst()) {
            double avgWeight = cAvg.isNull(cAvg.getColumnIndexOrThrow("avg_weight"))
                    ? 0 : cAvg.getDouble(cAvg.getColumnIndexOrThrow("avg_weight"));
            double avgDist = cAvg.isNull(cAvg.getColumnIndexOrThrow("avg_distance"))
                    ? 0 : cAvg.getDouble(cAvg.getColumnIndexOrThrow("avg_distance"));

            tvAvgWeight.setText(String.format("Peso promedio: %.2f kg", avgWeight));
            tvAvgDist.setText(String.format("Distancia promedio: %.2f km", avgDist));
        }
        cAvg.close();

        // === Medianas ===
        double medWeight = getMedianFromColumn("weight_kg");
        double medDist   = getMedianFromColumn("distance_km");
        tvMedianWeight.setText(String.format("Mediana peso: %.2f kg", medWeight));
        tvMedianDist.setText(String.format("Mediana distancia: %.2f km", medDist));

        // === Modas (valor más frecuente) ===
        tvModeWeight.setText(String.format("Moda peso: %.2f kg", getMode("weight_kg")));
        tvModeDist.setText(String.format("Moda distancia: %.2f km", getMode("distance_km")));

        // === Cantidad de guías ===
        int countGuides = db.count("guides");
        tvCountGuides.setText("Cantidad de guías: " + countGuides);
    }

    /** Calcula mediana de una columna numérica de shipments usando SQL y posición media. */
    private double getMedianFromColumn(String column) {
        double median = 0.0;
        Cursor c = db.raw(
                "SELECT " + column + " FROM shipments " +
                        "WHERE " + column + " IS NOT NULL " +
                        "ORDER BY " + column,
                null
        );
        int n = c.getCount();
        if (n > 0) {
            // mover al elemento central (para n par tomamos el superior; simple y suficiente)
            c.moveToPosition(n / 2);
            median = c.getDouble(0);
        }
        c.close();
        return median;
    }

    /** Obtiene la moda (valor más frecuente) de una columna numérica de shipments. */
    private double getMode(String column) {
        double mode = 0.0;
        Cursor c = db.raw(
                "SELECT " + column + ", COUNT(*) AS cnt " +
                        "FROM shipments " +
                        "WHERE " + column + " IS NOT NULL " +
                        "GROUP BY " + column + " " +
                        "ORDER BY cnt DESC LIMIT 1",
                null
        );
        if (c.moveToFirst()) mode = c.getDouble(0);
        c.close();
        return mode;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
