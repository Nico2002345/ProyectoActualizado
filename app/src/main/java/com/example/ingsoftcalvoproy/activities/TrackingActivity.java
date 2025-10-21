package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

/**
 * Permite consultar eventos de seguimiento (tracking) por ID o código de envío.
 */
public class TrackingActivity extends AppCompatActivity {

    private Db db;
    private EditText etShipment;
    private TextView tvEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        db = new Db(this);
        etShipment = findViewById(R.id.etShipment);
        tvEvents = findViewById(R.id.tvEvents);

        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> search());
    }

    private void search() {
        String input = etShipment.getText().toString().trim();
        String id = input;

        if (input.startsWith("ENV-")) {
            Cursor c1 = db.raw("SELECT id FROM shipments WHERE shipment_code=?", new String[]{input});
            if (c1.moveToFirst()) id = String.valueOf(c1.getInt(0));
            c1.close();
        }

        Cursor c = db.raw("SELECT status, location, event_time FROM tracking_events WHERE shipment_id=? ORDER BY event_time DESC", new String[]{id});
        StringBuilder sb = new StringBuilder();

        while (c.moveToNext()) {
            sb.append("[").append(c.getString(2)).append("] ")
                    .append(c.getString(0)).append(" - ")
                    .append(c.getString(1)).append("\n");
        }
        c.close();

        if (sb.length() == 0)
            sb.append("No hay eventos registrados.");
        tvEvents.setText(sb.toString());
    }
}
