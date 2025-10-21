package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ingsoftcalvoproy.R;

/**
 * Muestra información del usuario (futuro).
 */
public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView tv = findViewById(R.id.tvProfile);
        tv.setText("Perfil del usuario (pendiente de implementación)");
    }
}
