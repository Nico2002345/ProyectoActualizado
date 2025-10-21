package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;

/**
 * Pantalla principal para usuarios (clientes).
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btnShipments).setOnClickListener(v ->
                startActivity(new Intent(this, ShipmentListActivity.class))
        );

        findViewById(R.id.btnAnalytics).setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class))
        );

        findViewById(R.id.btnTracking).setOnClickListener(v ->
                startActivity(new Intent(this, TrackingActivity.class))
        );
    }
}
