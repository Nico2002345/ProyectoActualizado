package com.example.ingsoftcalvoproy.activities;

import android.content.ContentValues;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

/**
 * Permite registrar nuevos usuarios con su tipo de rol.
 * Inserta los datos directamente en la tabla real `users`.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Spinner spRole;
    private Button btnSave;
    private Db db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new Db(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spRole = findViewById(R.id.spRole);
        btnSave = findViewById(R.id.btnSave);

        // === Cargar roles desde strings.xml ===
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.roles_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);

        btnSave.setOnClickListener(v -> register());
    }

    /**
     * Registra un nuevo usuario en la base de datos real.
     */
    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        // === Validaciones ===
        if (Utils.isEmpty(name) || Utils.isEmpty(email) || Utils.isEmpty(password)) {
            Utils.toast(this, "Por favor completa todos los campos.");
            return;
        }

        if (!Utils.isValidEmail(email)) {
            Utils.toast(this, "Correo electrónico no válido.");
            return;
        }

        // === Verificar duplicado de email ===
        if (db.exists("users", "email", email)) {
            Utils.toast(this, "Ya existe un usuario registrado con este correo.");
            return;
        }

        // === Insertar en BD real ===
        ContentValues cv = new ContentValues();
        cv.put("name", Utils.capitalize(name));
        cv.put("email", email.toLowerCase());
        cv.put("password", password);
        cv.put("role", role.toUpperCase());

        long id = db.insert("users", cv);

        if (id > 0) {
            Utils.toastLong(this, "✅ Usuario registrado como " + role);
            finish(); // Cierra y regresa al login
        } else {
            Utils.toast(this, "❌ Error al registrar el usuario.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
    }
}
