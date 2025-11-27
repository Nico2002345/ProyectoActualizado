package com.example.ingsoftcalvoproy.utils;

/**
 * Constantes globales del sistema logístico.
 * Se usa para evitar strings repetidos en Activities y queries.
 */
public class Constants {

    // === Servidor API ===
    public static final String BASE_URL = "https://fakestoreapi.com/";

    // === Roles de usuario ===
    public static final String ROLE_USER = "USUARIO";
    public static final String ROLE_REPARTIDOR = "REPARTIDOR";
    public static final String ROLE_FUNCIONARIO = "FUNCIONARIO_LOGISTICA";
    public static final String ROLE_TRABAJADOR = "TRABAJADOR";
    public static final String ROLE_ASIGNADOR = "ASIGNADOR";

    // === Tablas de la base de datos ===
    public static final String TBL_USERS = "users";
    public static final String TBL_SHIPMENTS = "shipments";
    public static final String TBL_GUIDES = "guides";
    public static final String TBL_TRUCKS = "trucks";
    public static final String TBL_COLLECTORS = "collectors";
    public static final String TBL_PICKUPS = "pickups";
    public static final String TBL_TRACKING = "tracking_events";

    // === Campos comunes ===
    public static final String FIELD_ID = "id";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_CREATED_AT = "created_at";

    // === Estado de envíos ===
    public static final String STATUS_CREATED = "CREADO";
    public static final String STATUS_IN_TRANSIT = "EN_TRÁNSITO";
    public static final String STATUS_DELIVERED = "ENTREGADO";
    public static final String STATUS_CANCELLED = "CANCELADO";

    // === Parámetros genéricos ===
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // === Debug y Logs ===
    public static final String TAG_DB = "DB_LOG";
    public static final String TAG_APP = "APP_LOG";
}
