package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Spinner spRole;
    private Button btnSave;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializamos ApiService
        apiService = Utils.getApiService();

        // Inicializamos vistas
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spRole = findViewById(R.id.spRole);
        btnSave = findViewById(R.id.btnSave);

        // Configuramos Spinner de roles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.roles_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);

        // Botón registrar usuario
        btnSave.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        // Validaciones
        if (Utils.isEmpty(name) || Utils.isEmpty(email) || Utils.isEmpty(password)) {
            Utils.toast(this, "Por favor completa todos los campos.");
            return;
        }

        if (!Utils.isValidEmail(email)) {
            Utils.toast(this, "Correo electrónico no válido.");
            return;
        }

        // Preparamos el body de la petición con los campos exactos que espera el backend
        Map<String, Object> body = new HashMap<>();
        body.put("name", Utils.capitalize(name)); // ⚠ importante: "name" y no "username"
        body.put("email", email.toLowerCase());
        body.put("password", password);
        body.put("role", role.toUpperCase());

        // Si el backend requiere otros campos obligatorios, agrégalos aquí:
        // body.put("address", "Calle 123");
        // body.put("phone", "1234567890");

        // Debug: imprimir body enviado
        Log.d("RegisterActivity", "Body enviado: " + body.toString());

        // Llamada a la API
        apiService.createUser(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d("RegisterActivity", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Utils.toastLong(RegisterActivity.this, "✅ Usuario registrado como " + role);
                    finish(); // volver al login
                } else {
                    String errorMsg = "❌ Error al registrar el usuario.";
                    try {
                        if (response.errorBody() != null) {
                            String serverError = response.errorBody().string();
                            Log.e("RegisterActivity", "Error del servidor: " + serverError);
                            errorMsg += " " + serverError;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Utils.toast(RegisterActivity.this, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(RegisterActivity.this, "❌ Error de red: " + t.getMessage());
                Log.e("RegisterActivity", "onFailure: ", t);
            }
        });
    }
}
