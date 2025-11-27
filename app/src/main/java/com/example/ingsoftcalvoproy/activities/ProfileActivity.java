package com.example.ingsoftcalvoproy.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.network.ApiService;
import com.example.ingsoftcalvoproy.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private TextView tvTitle;
    private EditText etName, etEmail, etPassword, etRole;
    private Button btnUpdate;
    private ApiService apiService;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        apiService = Utils.getApiService();

        tvTitle = findViewById(R.id.tvProfileTitle);
        etName = findViewById(R.id.etProfileName);
        etEmail = findViewById(R.id.etProfileEmail);
        etPassword = findViewById(R.id.etProfilePassword);
        etRole = findViewById(R.id.etProfileRole);
        btnUpdate = findViewById(R.id.btnProfileUpdate);

        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId > 0) {
            btnUpdate.setText("Actualizar perfil");
            loadUserData(userId);
        } else {
            btnUpdate.setText("Crear usuario");
        }

        btnUpdate.setOnClickListener(v -> {
            if (userId > 0) updateUser(); // ✅ Aquí llamamos al método de la actividad
            else createUser();
        });
    }

    private void loadUserData(int id) {
        apiService.getUserById(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> user = response.body();
                    etName.setText(user.get("name").toString());
                    etEmail.setText(user.get("email").toString());
                    etPassword.setText("");
                    etRole.setText(user.get("role") != null ? user.get("role").toString() : "USUARIO");
                    tvTitle.setText("Perfil de " + user.get("name"));
                    Log.d(TAG, "Datos del usuario cargados correctamente.");
                } else {
                    String err = response.errorBody() != null ? response.errorBody().toString() : response.message();
                    Log.e(TAG, "Error cargando usuario: " + err);
                    Utils.toast(ProfileActivity.this, "Usuario no encontrado.");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error de red al cargar usuario", t);
                Utils.toast(ProfileActivity.this, "Error de red: " + t.getMessage());
            }
        });
    }

    private void createUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String role = etRole.getText().toString().trim().toUpperCase();

        if (Utils.isEmpty(name) || Utils.isEmpty(email) || Utils.isEmpty(pass)) {
            Utils.toast(this, "Por favor completa todos los campos.");
            return;
        }
        if (!Utils.isValidEmail(email)) {
            Utils.toast(this, "Correo electrónico no válido.");
            return;
        }
        if (Utils.isEmpty(role)) role = "USUARIO";

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);       // Cambia a "username" si la API lo requiere
        body.put("email", email);
        body.put("password", pass);
        body.put("role", role);

        Log.d(TAG, "Intentando crear usuario con body: " + body);

        apiService.createUser(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Utils.toast(ProfileActivity.this, "✅ Usuario creado correctamente");
                        Utils.saveUserSession(ProfileActivity.this, response.body());
                        finish();
                    } else {
                        String errMsg = "Error desconocido";
                        if (response.errorBody() != null) errMsg = response.errorBody().string();
                        else if (response.message() != null) errMsg = response.message();

                        try {
                            JSONObject json = new JSONObject(errMsg);
                            if (json.has("message")) errMsg = json.getString("message");
                            else if (json.has("error")) errMsg = json.getString("error");
                        } catch (Exception ignored) {}

                        Log.e(TAG, "Error al crear usuario: " + errMsg);
                        Utils.toast(ProfileActivity.this, "❌ Error: " + errMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Excepción inesperada al crear usuario", e);
                    Utils.toast(ProfileActivity.this, "❌ Error inesperado");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error de red al crear usuario", t);
                Utils.toast(ProfileActivity.this, "❌ Error de red: " + t.getMessage());
            }
        });
    }

    private void updateUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String role = etRole.getText().toString().trim().toUpperCase();

        if (Utils.isEmpty(name) || Utils.isEmpty(email)) {
            Utils.toast(this, "Nombre y correo son obligatorios.");
            return;
        }
        if (!Utils.isValidEmail(email)) {
            Utils.toast(this, "Correo electrónico no válido.");
            return;
        }
        if (Utils.isEmpty(role)) role = "USUARIO";

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("role", role);
        if (!Utils.isEmpty(pass)) body.put("password", pass);

        Log.d(TAG, "Actualizando usuario ID " + userId + " con body: " + body);

        apiService.updateUser(userId, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                try {
                    if (response.isSuccessful()) {
                        Utils.toast(ProfileActivity.this, "✅ Perfil actualizado correctamente");
                        Log.d(TAG, "Usuario actualizado correctamente.");
                    } else {
                        String errMsg = "Error desconocido";
                        if (response.errorBody() != null) errMsg = response.errorBody().string();
                        else if (response.message() != null) errMsg = response.message();

                        try {
                            JSONObject json = new JSONObject(errMsg);
                            if (json.has("message")) errMsg = json.getString("message");
                            else if (json.has("error")) errMsg = json.getString("error");
                        } catch (Exception ignored) {}

                        Log.e(TAG, "Error actualización usuario: " + errMsg);
                        Utils.toast(ProfileActivity.this, "❌ Error al actualizar perfil: " + errMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Excepción actualización usuario", e);
                    Utils.toast(ProfileActivity.this, "❌ Error inesperado al actualizar perfil");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error de red al actualizar usuario", t);
                Utils.toast(ProfileActivity.this, "❌ Error de red: " + t.getMessage());
            }
        });
    }
}
