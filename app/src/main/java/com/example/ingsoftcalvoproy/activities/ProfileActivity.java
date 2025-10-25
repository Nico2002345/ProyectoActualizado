package com.example.ingsoftcalvoproy.activities;

import android.content.ContentValues;
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
 * Muestra y permite actualizar la información del usuario logueado.
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText etName, etEmail, etPassword, etRole;
    private Button btnUpdate;
    private Db db;

    private int userId = -1;
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // === Inicializar base de datos ===
        db = new Db(this);

        // === Referencias a vistas ===
        tvTitle = findViewById(R.id.tvProfileTitle);
        etName = findViewById(R.id.etProfileName);
        etEmail = findViewById(R.id.etProfileEmail);
        etPassword = findViewById(R.id.etProfilePassword);
        etRole = findViewById(R.id.etProfileRole);
        btnUpdate = findViewById(R.id.btnProfileUpdate);

        // === Obtener ID y rol del Intent ===
        userId = getIntent().getIntExtra("USER_ID", -1);
        userRole = getIntent().getStringExtra("USER_ROLE");

        if (userId <= 0) {
            Utils.toast(this, "Error: usuario no identificado.");
            finish();
            return;
        }

        // === Cargar datos del usuario ===
        loadUserData(userId);

        // === Evento de actualización ===
        btnUpdate.setOnClickListener(v -> updateUser());
    }

    /**
     * Carga los datos del usuario desde la base de datos.
     */
    private void loadUserData(int id) {
        Cursor c = db.raw(
                "SELECT name, email, password, role FROM users WHERE id=?",
                new String[]{String.valueOf(id)}
        );

        if (c != null && c.moveToFirst()) {
            etName.setText(c.getString(0));
            etEmail.setText(c.getString(1));
            etPassword.setText(c.getString(2));
            etRole.setText(c.getString(3));
            tvTitle.setText("Perfil de " + c.getString(0));
        } else {
            Utils.toast(this, "Usuario no encontrado.");
            finish();
        }

        if (c != null) c.close();
    }

    /**
     * Actualiza los datos del usuario en la base de datos.
     */
    private void updateUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // === Validaciones ===
        if (Utils.isEmpty(name) || Utils.isEmpty(email) || Utils.isEmpty(pass)) {
            Utils.toast(this, "Por favor completa todos los campos.");
            return;
        }

        if (!Utils.isValidEmail(email)) {
            Utils.toast(this, "Correo electrónico no válido.");
            return;
        }

        // === Actualización ===
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        cv.put("password", pass);

        int rows = db.update("users", cv, "id=?", new String[]{String.valueOf(userId)});

        if (rows > 0) {
            Utils.toastLong(this, "✅ Perfil actualizado correctamente.");
        } else {
            Utils.toast(this, "❌ No se pudo actualizar el perfil.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.closeDB();
    }
}

