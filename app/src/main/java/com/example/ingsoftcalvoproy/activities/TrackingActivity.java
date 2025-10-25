package com.example.ingsoftcalvoproy.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

/**
 * Permite consultar los eventos de seguimiento (tracking)
 * por ID o por código de envío (ENV-...).
 * Dirigido principalmente al destinatario.
 */
public class TrackingActivity extends AppCompatActivity {

    private Db db;
    private EditText etShipment;
    private TextView tvEvents;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        db = new Db(this);
        etShipment = findViewById(R.id.etShipment);
        tvEvents = findViewById(R.id.tvEvents);
        btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> searchShipment());
    }

    /**
     * Realiza la búsqueda de eventos de seguimiento.
     */
    private void searchShipment() {
        String input = etShipment.getText().toString().trim();

        if (Utils.isEmpty(input)) {
            Utils.toast(this, "Por favor ingresa un ID o código de envío.");
            return;
        }

        // 🔹 Si el usuario ingresa un código ENV-XXXX, obtener su ID interno
        String shipmentId = input;
        if (input.toUpperCase().startsWith("ENV-")) {
            Cursor c1 = db.raw("SELECT id FROM shipments WHERE shipment_code = ?", new String[]{input});
            if (c1.moveToFirst()) {
                shipmentId = String.valueOf(c1.getInt(0));
            } else {
                Utils.toast(this, "No se encontró el envío con ese código.");
                c1.close();
                return;
            }
            c1.close();
        }

        // 🔹 Consultar eventos
        Cursor c = db.raw("""
            SELECT status, location, datetime(event_time)
            FROM tracking_events
            WHERE shipment_id = ?
            ORDER BY event_time DESC
        """, new String[]{shipmentId});

        StringBuilder sb = new StringBuilder();
        int counter = 0;

        while (c.moveToNext()) {
            counter++;
            sb.append(counter).append(". ")
                    .append(c.getString(0)).append(" - ")
                    .append(c.getString(1)).append("\n🕒 ")
                    .append(c.getString(2)).append("\n\n");
        }
        c.close();

        if (counter == 0) {
            tvEvents.setText("No hay eventos registrados para este envío.");
        } else {
            tvEvents.setText(sb.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
