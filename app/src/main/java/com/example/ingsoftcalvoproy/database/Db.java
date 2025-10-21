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
}
