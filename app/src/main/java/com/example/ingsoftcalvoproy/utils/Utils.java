package com.example.ingsoftcalvoproy.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Funciones utilitarias reutilizables en el proyecto.
 */
public class Utils {

    // ===============================
    // 🔹 TOASTS
    // ===============================

    public static void toast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    // ===============================
    // 🔹 FORMATEO Y FECHAS
    // ===============================

    public static String generateShipmentCode() {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int random = new Random().nextInt(9000) + 1000;
        return "ENV-" + date + "-" + random;
    }

    public static double parseDoubleSafe(String input) {
        try {
            if (TextUtils.isEmpty(input)) return 0.0;
            return Double.parseDouble(input);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    // ===============================
    // 🔹 CLASIFICACIÓN SIMPLE (peso, volumen, distancia)
    // ===============================

    public static String classifyWeight(double kg) {
        if (kg < 5) return "LIGERO";
        if (kg < 20) return "MEDIO";
        return "PESADO";
    }

    public static String classifyVolume(double m3) {
        if (m3 < 0.05) return "PEQUEÑO";
        if (m3 < 0.2) return "MEDIO";
        return "GRANDE";
    }

    public static String classifyDistance(double km) {
        if (km < 20) return "CORTA";
        if (km < 100) return "MEDIA";
        return "LARGA";
    }

    // ===============================
    // 🔹 VALIDACIONES
    // ===============================

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && phone.length() >= 7;
    }

    // ===============================
    // 🔹 ESTADOS / FORMATO
    // ===============================

    public static String formatStatus(String status) {
        if (status == null) return "";
        switch (status.toUpperCase()) {
            case "CREADO":
                return "🟡 " + status;
            case "EN_TRÁNSITO":
                return "🔵 " + status;
            case "ENTREGADO":
                return "🟢 " + status;
            case "CANCELADO":
                return "🔴 " + status;
            case "PENDIENTE":
                return "🟠 " + status;
            default:
                return status;
        }
    }

    // ===============================
    // 🔹 ENVÍO DE CORREOS (COMPROBANTES)
    // ===============================

    /**
     * Envía un correo electrónico simple con asunto y cuerpo.
     */
    public static void sendEmail(Context ctx, String to, String subject, String body) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + to));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            ctx.startActivity(Intent.createChooser(intent, "Enviar comprobante..."));
        } catch (Exception e) {
            Toast.makeText(ctx, "No se pudo abrir el cliente de correo.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Genera texto base para comprobantes de envío o recogida.
     */
    public static String buildReceiptText(String code, String name, String address, double weight, double volume, String status) {
        return "📦 Comprobante de Envío\n" +
                "Código: " + code + "\n" +
                "Cliente: " + name + "\n" +
                "Dirección: " + address + "\n" +
                "Peso: " + weight + " kg\n" +
                "Volumen: " + volume + " m³\n" +
                "Estado actual: " + status + "\n" +
                "Fecha: " + now();
    }
}
