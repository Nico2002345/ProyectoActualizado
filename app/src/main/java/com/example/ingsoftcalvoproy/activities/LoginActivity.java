package com.example.ingsoftcalvoproy.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingsoftcalvoproy.R;
import com.example.ingsoftcalvoproy.database.Db;
import com.example.ingsoftcalvoproy.utils.Utils;

/**
 * Permite iniciar sesión según el rol registrado.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private Db db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            db = new Db(this);
        } catch (Exception e) {
            Utils.toast(this, "Error al inicializar la base de datos.");
            e.printStackTrace();
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // 🔹 Ir a registro
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // 🔹 Intentar iniciar sesión
        btnLogin.setOnClickListener(v -> login());
    }

    /**
     * Verifica las credenciales en la base de datos
     * y redirige según el rol del usuario.
     */
    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // === Validaciones básicas ===
        if (Utils.isEmpty(email) || Utils.isEmpty(pass)) {
            Utils.toast(this, "Por favor ingresa correo y contraseña.");
            return;
        }

        if (!Utils.isValidEmail(email)) {
            Utils.toast(this, "Correo no válido.");
            return;
        }

        Cursor c = null;
        try {
            c = db.raw("""
                SELECT id, name, role 
                FROM users 
                WHERE email = ? AND password = ?
            """, new String[]{email, pass});

            if (c != null && c.moveToFirst()) {
                int userId = c.getInt(0);
                String name = c.getString(1);
                String role = c.getString(2).toUpperCase();

                Utils.toastLong(this, "✅ Bienvenido " + name + " (" + role + ")");

                // 🔹 Redirección según el rol
                Intent intent;
                switch (role.toUpperCase()) { // convertimos a mayúsculas para evitar errores de mayúsculas/minúsculas
                    case "REPARTIDOR":
                        intent = new Intent(this, CourierHomeActivity.class);
                        break;
                    case "CONDUCTOR":
                        intent = new Intent(this, ConductorRutasActivity.class);
                        break;
                    case "ASIGNADOR":
                        intent = new Intent(this, AsignadorActivity.class);
                        break;
                    case "USUARIO":
                        intent = new Intent(this, HomeActivity.class);
                        break;
                    case "RECOLECTOR":
                        intent = new Intent(this, RecolectorHomeActivity.class); // ← redirige a RecolectorHomeActivity
                        break;
                    case "FUNCIONARIO DE LOGÍSTICA":
                    case "FUNCIONARIO_LOGISTICA":
                        intent = new Intent(this, FuncionarioLogisticaActivity.class);
                        break;
                    case "TRABAJADOR":
                        intent = new Intent(this, RegisterMerchandiseActivity.class);
                        break;
                    default:
                        intent = new Intent(this, HomeActivity.class);
                        break;
                }
                startActivity(intent);
                finish(); // Para que no vuelva al login al presionar atrás


                // 🔹 Pasar datos básicos
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_ROLE", role);
                intent.putExtra("USER_NAME", name);

                startActivity(intent);
                finish();
            } else {
                Utils.toast(this, "❌ Credenciales inválidas o usuario no encontrado.");
            }
        } catch (Exception e) {
            Utils.toast(this, "Error al consultar la base de datos.");
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.closeDB();
    }
}
