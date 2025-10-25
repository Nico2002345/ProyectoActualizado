package com.example.ingsoftcalvoproy.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Clase de acceso a datos.
 * Simplifica las operaciones CRUD y consultas sobre la base de datos.
 */
public class Db extends DbHelper {

    private final SQLiteDatabase db;

    public Db(Context context) {
        super(context);
        db = getWritableDatabase();
    }

    // === INSERTAR REGISTRO ===
    public long insert(String table, ContentValues values) {
        return db.insert(table, null, values);
    }

    // === ACTUALIZAR REGISTRO ===
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return db.update(table, values, whereClause, whereArgs);
    }

    // === ELIMINAR REGISTRO ===
    public int delete(String table, String whereClause, String[] whereArgs) {
        return db.delete(table, whereClause, whereArgs);
    }

    // === CONSULTA SIMPLE ===
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String orderBy) {
        return db.query(table, columns, selection, selectionArgs, null, null, orderBy);
    }

    // === CONSULTA RAW SQL ===
    public Cursor raw(String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }

    // === OBTENER CANTIDAD DE REGISTROS ===
    public int count(String table) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + table, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    // === VERIFICAR EXISTENCIA DE REGISTRO ===
    public boolean exists(String table, String column, String value) {
        Cursor c = db.rawQuery("SELECT 1 FROM " + table + " WHERE " + column + " = ? LIMIT 1", new String[]{value});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    // === CERRAR CONEXIÓN ===
    public void closeDB() {
        if (db != null && db.isOpen()) db.close();
    }

    // -----------------------------------------------------------------
    // 🔹 MÉTODOS PERSONALIZADOS PARA CADA MÓDULO
    // -----------------------------------------------------------------

    // === RECOLECTORES ===
    public Cursor getCollectors() {
        return db.rawQuery("SELECT id, name, phone, email FROM collectors WHERE active = 1 ORDER BY name ASC", null);
    }

    // === PICKUPS ===
    public Cursor getPickupsByUser(int userId) {
        return db.rawQuery("SELECT * FROM pickups WHERE user_id = ? ORDER BY created_at DESC", new String[]{String.valueOf(userId)});
    }

    public Cursor getPickupsByCollector(int collectorId) {
        return db.rawQuery("SELECT * FROM pickups WHERE collector_id = ? AND status != 'FINALIZADO' ORDER BY scheduled_at ASC", new String[]{String.valueOf(collectorId)});
    }

    public long createPickup(int userId, String address, String date, double weight, double volume) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("address", address);
        cv.put("scheduled_at", date);
        cv.put("weight_kg", weight);
        cv.put("volume_m3", volume);
        cv.put("status", "PENDIENTE");
        return db.insert("pickups", null, cv);
    }

    public int assignPickup(int pickupId, int collectorId) {
        ContentValues cv = new ContentValues();
        cv.put("collector_id", collectorId);
        cv.put("status", "ASIGNADO");
        return db.update("pickups", cv, "id=?", new String[]{String.valueOf(pickupId)});
    }

    public int updatePickupStatus(int pickupId, String newStatus) {
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        return db.update("pickups", cv, "id=?", new String[]{String.valueOf(pickupId)});
    }

    // === CLASIFICACIÓN DE GUÍAS ===
    public void classifyGuides() {
        Cursor c = db.rawQuery("""
            SELECT g.id, s.weight_kg, s.volume_m3, s.distance_km
            FROM guides g
            JOIN shipments s ON g.shipment_id = s.id
        """, null);

        if (c.moveToFirst()) {
            do {
                double w = c.getDouble(1);
                double v = c.getDouble(2);
                double d = c.getDouble(3);

                String wB = (w < 5) ? "LIGERO" : (w < 20 ? "MEDIO" : "PESADO");
                String vB = (v < 0.05) ? "PEQUEÑO" : (v < 0.2 ? "MEDIO" : "GRANDE");
                String dB = (d < 20) ? "CORTA" : (d < 100 ? "MEDIA" : "LARGA");

                ContentValues cv = new ContentValues();
                cv.put("distance_km", d);
                cv.put("weight_bucket", wB);
                cv.put("volume_bucket", vB);
                cv.put("distance_bucket", dB);

                db.update("guides", cv, "id=?", new String[]{String.valueOf(c.getInt(0))});
            } while (c.moveToNext());
        }
        c.close();
    }

    // === MEDIDAS DE TENDENCIA PARA REPARTIDOR ===
    public Cursor getAverages() {
        return db.rawQuery("""
            SELECT 
                AVG(weight_kg) AS avg_weight,
                AVG(volume_m3) AS avg_volume,
                AVG(distance_km) AS avg_distance
            FROM shipments
        """, null);
    }

    // Mediana (básica) - se calcula al vuelo
    public double getMedian(String column) {
        Cursor c = db.rawQuery(
                "SELECT " + column + " FROM shipments WHERE " + column + " IS NOT NULL ORDER BY " + column,
                null
        );
        int n = c.getCount();
        double median = 0;
        if (n > 0) {
            int mid = n / 2;
            if (c.moveToPosition(mid)) median = c.getDouble(0);
        }
        c.close();
        return median;
    }

    // === MERCHANDISE ===

    // Registrar nueva mercancía
    public long createMerchandise(int userId, String description, double weight, double volume, String address) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("description", description);
        cv.put("weight_kg", weight);
        cv.put("volume_m3", volume);
        cv.put("address", address);
        cv.put("status", "PENDIENTE");
        return db.insert("merchandise",null ,cv);
    }

    // Obtener todas las mercancías de un usuario
    public Cursor getMerchandiseByUser(int userId) {
        return db.rawQuery(
                "SELECT id, description, weight_kg, volume_m3, address, status, created_at FROM merchandise WHERE user_id=? ORDER BY created_at DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    // Actualizar estado de la mercancía
    public int updateMerchandiseStatus(int merchandiseId, String status) {
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        return db.update("merchandise", cv, "id=?", new String[]{String.valueOf(merchandiseId)});
    }

    // Eliminar mercancía
    public int deleteMerchandise(int merchandiseId) {
        return db.delete("merchandise", "id=?", new String[]{String.valueOf(merchandiseId)});
    }
}
