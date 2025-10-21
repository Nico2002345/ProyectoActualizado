package com.example.ingsoftcalvoproy.utils;

import android.content.Context;
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

    /**
     * Muestra un Toast corto.
     */
    public static void toast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Muestra un Toast largo.
     */
    public static void toastLong(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Genera un código de envío con formato ENV-YYYYMMDD-XXXX.
     */
    public static String generateShipmentCode() {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int random = new Random().nextInt(9000) + 1000;
        return "ENV-" + date + "-" + random;
    }

    /**
     * Convierte texto a double de forma segura.
     */
    public static double parseDoubleSafe(String input) {
        try {
            if (TextUtils.isEmpty(input)) return 0.0;
            return Double.parseDouble(input);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Devuelve la fecha y hora actual en formato estándar.
     */
    public static String now() {
        return new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(new Date());
    }

    /**
     * Capitaliza la primera letra de un texto.
     */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Verifica si una cadena es nula o vacía.
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Devuelve un estado formateado con un icono (solo para listas, opcional).
     */
    public static String formatStatus(String status) {
        switch (status.toUpperCase()) {
            case "CREADO":
                return "🟡 " + status;
            case "EN_TRÁNSITO":
                return "🔵 " + status;
            case "ENTREGADO":
                return "🟢 " + status;
            case "CANCELADO":
                return "🔴 " + status;
            default:
                return status;
        }
    }
}
