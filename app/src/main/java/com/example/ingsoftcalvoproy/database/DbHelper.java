package com.example.ingsoftcalvoproy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Clase que gestiona la creación, actualización y estructura de la base de datos SQLite.
 * Define todas las tablas necesarias para el sistema logístico.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "logistics.db";
    public static final int DB_VERSION = 10; // 🔹 Incrementar versión por nuevos campos y FKs

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Habilita las claves foráneas en SQLite (por defecto están desactivadas)
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // === Tabla de Usuarios ===
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL DEFAULT 'USUARIO'" +
                ");");

        // === Tabla de Envíos ===
        db.execSQL("CREATE TABLE IF NOT EXISTS shipments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "shipment_code TEXT UNIQUE," +
                "object_desc TEXT," +
                "receiver_address TEXT," +
                "weight_kg REAL DEFAULT 0," +
                "distance_km REAL DEFAULT 0," +
                "volume_m3 REAL DEFAULT 0," +
                "status TEXT DEFAULT 'CREADO'," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");");

        // === Tabla de Guías ===
        db.execSQL("CREATE TABLE IF NOT EXISTS guides (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "shipment_id INTEGER NOT NULL," +
                "guide_number TEXT UNIQUE," +
                "distance_km REAL DEFAULT 0," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE" +
                ");");

        // === Tabla de Camiones ===
        db.execSQL("CREATE TABLE IF NOT EXISTS trucks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "plate TEXT UNIQUE NOT NULL," +
                "capacity_kg REAL DEFAULT 0," +
                "capacity_m3 REAL DEFAULT 0," +
                "active INTEGER DEFAULT 1" +
                ");");

        // === Tabla de Recolectores ===
        db.execSQL("CREATE TABLE IF NOT EXISTS collectors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "phone TEXT," +
                "email TEXT," +
                "vehicle_id INTEGER," +
                "active INTEGER DEFAULT 1," +
                "FOREIGN KEY (vehicle_id) REFERENCES trucks(id) ON UPDATE CASCADE ON DELETE SET NULL" +
                ");");

        // === Tabla de Recogidas ===
        db.execSQL("CREATE TABLE IF NOT EXISTS pickups (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "collector_id INTEGER," +
                "address TEXT NOT NULL," +
                "scheduled_at DATETIME," +
                "volume_m3 REAL DEFAULT 0," +
                "weight_kg REAL DEFAULT 0," +
                "status TEXT DEFAULT 'PENDIENTE'," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (collector_id) REFERENCES collectors(id) ON DELETE SET NULL" +
                ");");

        // === Tabla de Eventos de Rastreo ===
        db.execSQL("CREATE TABLE IF NOT EXISTS tracking_events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "shipment_id INTEGER NOT NULL," +
                "status TEXT," +
                "location TEXT," +
                "event_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE" +
                ");");

        // === Tabla de Merchandise ===
        db.execSQL("CREATE TABLE IF NOT EXISTS merchandise (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "description TEXT NOT NULL," +
                "weight_kg REAL DEFAULT 0," +
                "volume_m3 REAL DEFAULT 0," +
                "address TEXT," +
                "status TEXT DEFAULT 'PENDIENTE'," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ");");

        // === Índices útiles ===
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_guides_shipment ON guides(shipment_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_events_shipment ON tracking_events(shipment_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_pickups_user ON pickups(user_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_merchandise_user ON merchandise(user_id);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS tracking_events;");
        db.execSQL("DROP TABLE IF EXISTS pickups;");
        db.execSQL("DROP TABLE IF EXISTS collectors;");
        db.execSQL("DROP TABLE IF EXISTS trucks;");
        db.execSQL("DROP TABLE IF EXISTS guides;");
        db.execSQL("DROP TABLE IF EXISTS shipments;");
        db.execSQL("DROP TABLE IF EXISTS merchandise;");
        db.execSQL("DROP TABLE IF EXISTS users;");
        onCreate(db);
    }
}
