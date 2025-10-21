package com.example.ingsoftcalvoproy.activities;

import android.content.ContentValues;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;

/**
 * Permite registrar nuevos usuarios con su tipo de rol.
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

        // Inicializar base de datos
        db = new Db(this);

        // Referencias a vistas
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spRole = findViewById(R.id.spRole);
        btnSave = findViewById(R.id.btnSave);

        // 🔹 Llenar el Spinner con los roles definidos en strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.roles_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);

        // Evento de guardado
        btnSave.setOnClickListener(v -> register());
    }

    private void register() {
        // Obtener valores de los campos
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        // 🔸 Validaciones básicas
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Insertar en base de datos
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        cv.put("password", password);
        cv.put("role", role);

        long id = db.insert("users", cv);
        if (id > 0) {
            Toast.makeText(this, "Usuario registrado como " + role, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show();
        }
    }
}
