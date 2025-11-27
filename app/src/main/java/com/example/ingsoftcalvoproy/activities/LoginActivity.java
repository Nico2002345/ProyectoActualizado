package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiClient;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Login usando API y redirección según rol.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        api = ApiClient.getClient().create(ApiService.class);

        // Ir a registro
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // Login
        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // Validaciones básicas
        if (Utils.isEmpty(email) || Utils.isEmpty(pass)) {
            Utils.toast(this, "Por favor ingresa correo y contraseña.");
            return;
        }

        if (!Utils.isValidEmail(email)) {
            Utils.toast(this, "Correo no válido.");
            return;
        }

        // Crear body para la API
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", pass);

        api.loginUser(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Utils.toast(LoginActivity.this, "Credenciales inválidas o error en el servidor.");
                    return;
                }

                Map<String, Object> user = response.body();

                // Log para debug
                android.util.Log.d("LOGIN", "Response body: " + user.toString());

                // Manejo seguro de tipos para ID
                int userId = 0;
                if (user.get("id") instanceof Double) {
                    userId = ((Double) user.get("id")).intValue();
                } else if (user.get("id") instanceof Integer) {
                    userId = (Integer) user.get("id");
                }

                String name = user.get("name") != null ? user.get("name").toString() : "Usuario";
                String roleRaw = user.get("role") != null ? user.get("role").toString() : "USUARIO";

                // Extraer email de forma más explícita
                Object emailObj = user.get("email");
                String userEmail = "";
                if (emailObj != null) {
                    userEmail = emailObj.toString();
                    android.util.Log.d("LOGIN", "Email desde API: " + userEmail);
                } else {
                    userEmail = email;
                    android.util.Log.d("LOGIN", "Email desde form: " + userEmail);
                }

                android.util.Log.d("LOGIN", "Email final que se enviará: " + userEmail);

                Utils.toastLong(LoginActivity.this, "✅ Bienvenido " + name + " (" + roleRaw + ")");

                // Normalizar rol: mayúsculas + guiones bajos
                String role = roleRaw.toUpperCase().replace(" ", "_");

                // Redirección según rol
                Intent intent;
                switch (role) {
                    case "REPARTIDOR":
                        intent = new Intent(LoginActivity.this, CourierHomeActivity.class);
                        break;
                    case "CONDUCTOR":
                        intent = new Intent(LoginActivity.this, ConductorRutasActivity.class);
                        break;
                    case "ASIGNADOR":
                        intent = new Intent(LoginActivity.this, AsignadorActivity.class);
                        break;
                    case "USUARIO":
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                        break;
                    case "Destinatario":
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                        break;
                    case "RECOLECTOR":
                        intent = new Intent(LoginActivity.this, RecolectorHomeActivity.class);
                        break;
                    case "FUNCIONARIO_DE_LOGÍSTICA":
                    case "FUNCIONARIO_LOGISTICA":
                        intent = new Intent(LoginActivity.this, FuncionarioLogisticaActivity.class);
                        break;
                    case "TRABAJADOR":
                        intent = new Intent(LoginActivity.this, RegisterMerchandiseActivity.class);
                        break;
                    default:
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                        break;
                }

                // Pasar datos al siguiente Activity
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_ROLE", roleRaw);
                intent.putExtra("USER_NAME", name);
                intent.putExtra("USER_EMAIL", userEmail);

                // Log antes de enviar
                android.util.Log.d("LOGIN", "Enviando USER_EMAIL: " + userEmail);
                android.util.Log.d("LOGIN", "Enviando USER_NAME: " + name);

                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Utils.toast(LoginActivity.this, "Error conectando al servidor: " + t.getMessage());
            }
        });
    }
}