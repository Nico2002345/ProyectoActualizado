package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ingsoftcalvoproy.R;

public class CourierHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_home);

        findViewById(R.id.btnVerGuias).setOnClickListener(v ->
                startActivity(new Intent(this, GuidesListActivity.class))
        );

        findViewById(R.id.btnRegistrarEntrega).setOnClickListener(v ->
                startActivity(new Intent(this, TrackingActivity.class))
        );
    }
}
