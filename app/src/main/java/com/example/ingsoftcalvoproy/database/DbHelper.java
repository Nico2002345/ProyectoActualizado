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
    public static final int DB_VERSION = 2; // 🔹 Incrementa versión si cambias el esquema

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // === Tabla de Usuarios ===
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT UNIQUE,
                password TEXT,
                role TEXT NOT NULL DEFAULT 'USUARIO'
            );
        """);

        // === Tabla de Envíos ===
        db.execSQL("""
    CREATE TABLE shipments (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        shipment_code TEXT UNIQUE,
        object_desc TEXT,
        height_cm REAL,
        width_cm REAL,
        length_cm REAL,
        weight_kg REAL,
        sender_name TEXT,
        sender_contact TEXT,
        receiver_name TEXT,
        receiver_contact TEXT,
        receiver_address TEXT,
        distance_km REAL,
        status TEXT DEFAULT 'CREADO',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );
""");

        // === Tabla de Guías ===
        db.execSQL("""
            CREATE TABLE guides (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                shipment_id INTEGER REFERENCES shipments(id) ON DELETE CASCADE,
                guide_number TEXT UNIQUE,
                distance_km REAL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """);

        // === Tabla de Camiones ===
        db.execSQL("""
            CREATE TABLE trucks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                plate TEXT UNIQUE,
                capacity_kg REAL,
                active INTEGER DEFAULT 1
            );
        """);

        // === Tabla de Recolectores ===
        db.execSQL("""
            CREATE TABLE collectors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                phone TEXT
            );
        """);

        // === Tabla de Recogidas ===
        db.execSQL("""
            CREATE TABLE pickups (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER REFERENCES users(id),
                collector_id INTEGER REFERENCES collectors(id),
                address TEXT,
                scheduled_at DATETIME,
                status TEXT DEFAULT 'PENDIENTE',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """);

        // === Tabla de Eventos de Rastreo ===
        db.execSQL("""
            CREATE TABLE tracking_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                shipment_id INTEGER REFERENCES shipments(id) ON DELETE CASCADE,
                status TEXT,
                location TEXT,
                event_time DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """);

        // === Índices útiles ===
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_guides_shipment ON guides(shipment_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_events_shipment ON tracking_events(shipment_id);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS tracking_events;");
        db.execSQL("DROP TABLE IF EXISTS pickups;");
        db.execSQL("DROP TABLE IF EXISTS collectors;");
        db.execSQL("DROP TABLE IF EXISTS trucks;");
        db.execSQL("DROP TABLE IF EXISTS guides;");
        db.execSQL("DROP TABLE IF EXISTS shipments;");
        db.execSQL("DROP TABLE IF EXISTS users;");
        onCreate(db);
    }
}
