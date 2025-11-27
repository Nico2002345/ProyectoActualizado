package com.example.ingsoftcalvoproy.network;
import com.example.ingsoftcalvoproy.dto.ShipmentDTO;
import com.example.ingsoftcalvoproy.dto.UserShipmentsDTO;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ===================== LOGIN / USUARIOS =====================
    @POST("users/login/")
    Call<Map<String, Object>> loginUser(@Body Map<String, Object> body);

    @POST("users/create/")
    Call<Map<String, Object>> createUser(@Body Map<String, Object> body);

    @PUT("users/update/{id}/")
    Call<Map<String, Object>> updateUser(@Path("id") int userId, @Body Map<String, Object> body);

    @DELETE("users/delete/{id}/")
    Call<Void> deleteUser(@Path("id") int userId);

    @GET("users/")
    Call<List<Map<String, Object>>> getUsers();

    @GET("users/{id}/")
    Call<Map<String, Object>> getUserById(@Path("id") int userId);

    @GET("users/by-email/")
    Call<Map<String, Object>> getUserByEmail(@Query("email") String email);

    @GET("users/shipments/")
    Call<List<UserShipmentsDTO>> getUsersWithShipments();

    // ===================== PICKUPS / ENVÍOS =====================
    @GET("pickups/user/{id}/")
    Call<List<ShipmentDTO>> getPickupsByUser(@Path("id") int userId);

    @GET("pickups/collector/{id}/")
    Call<List<ShipmentDTO>> getPickupsByCollector(@Path("id") int collectorId);

    @GET("pickups/")
    Call<List<ShipmentDTO>> getAllShipments();

    @GET("pickups/pending/")
    Call<List<ShipmentDTO>> getPendingPickups();

    @GET("pickups/all-pending/")
    Call<List<Map<String, Object>>> getAllPickupsPending();

    @GET("pickups/all-completed/")
    Call<List<Map<String, Object>>> getAllPickupsCompleted();

    @GET("pickups/history/{id}/")
    Call<List<ShipmentDTO>> getPickupHistory(@Path("id") int userId);

    @GET("pickups/filter/")
    Call<List<ShipmentDTO>> getPickupsByStatus(@Query("status") String status);

    @POST("pickups/create/")
    Call<Map<String, Object>> createPickup(@Body Map<String, Object> body);

    @PUT("pickups/assign/{id}/")
    Call<Map<String, Object>> assignPickup(@Path("id") int pickupId, @Body Map<String, Object> body);

    @PUT("pickups/status/{id}/")
    Call<Map<String, Object>> updatePickupStatus(@Path("id") int pickupId, @Body Map<String, Object> body);

    @PUT("pickups/assign-collector-truck/{id}/")
    Call<Map<String, Object>> assignCollectorAndTruckToPickup(@Path("id") int pickupId, @Body Map<String, Object> body);

    @DELETE("pickups/delete/{id}/")
    Call<Void> deletePickup(@Path("id") int pickupId);

    @GET("pickups/by-code/")
    Call<Map<String, Object>> getPickupByCode(@Query("code") String code); // Para TrackingActivity

    @PUT("pickups/assign-truck/{id}/")
    Call<Map<String, Object>> assignShipmentToTruck(@Path("id") int shipmentId, @Body Map<String, Object> body);

    // ===================== SHIPMENTS (ENVÍOS) =====================
    @POST("shipments/create/")
    Call<Map<String, Object>> createShipment(@Body Map<String, Object> body);

    @GET("shipments/")
    Call<List<ShipmentDTO>> getAllShipmentsFromAPI();

    @GET("shipments/assigned-with-repartidor/")
    Call<List<Map<String, Object>>> getAssignedShipmentsWithRepartidor();

    @GET("shipments/user/{id}/")
    Call<List<ShipmentDTO>> getShipmentsByUser(@Path("id") int userId);

    @GET("shipments/assigned/{id}/")
    Call<List<ShipmentDTO>> getShipmentsByAssignedUser(@Path("id") int userId);

    @GET("shipments/{id}/")
    Call<Map<String, Object>> getShipmentById(@Path("id") int shipmentId);

    @GET("shipments/by-code/")
    Call<Map<String, Object>> getShipmentByCode(@Query("code") String code);

    @PUT("shipments/update/{id}/")
    Call<Map<String, Object>> updateShipment(@Path("id") int shipmentId, @Body Map<String, Object> body);

    @PUT("shipments/status/{id}/")
    Call<Map<String, Object>> updateShipmentStatus(@Path("id") int shipmentId, @Body Map<String, Object> body);

    @PUT("shipments/reassign/{id}/")
    Call<Map<String, Object>> reassignShipmentRepartidor(@Path("id") int shipmentId, @Body Map<String, Object> body);

    @DELETE("shipments/delete/{id}/")
    Call<Void> deleteShipment(@Path("id") int shipmentId);

    // ===================== TRACKING =====================
    @GET("shipments/tracking/{id}/")
    Call<List<Map<String, Object>>> getTrackingEvents(@Path("id") int shipmentId);

    @POST("shipments/tracking/create/")
    Call<Map<String, Object>> addTrackingEvent(@Body Map<String, Object> body);

    // ===================== COLLECTORS =====================
    @GET("collectors/")
    Call<List<Map<String, Object>>> getCollectors();

    @GET("collectors/{id}/")
    Call<Map<String, Object>> getCollectorById(@Path("id") int collectorId);

    @POST("collectors/create/")
    Call<Map<String, Object>> createCollector(@Body Map<String, Object> body);

    @PUT("collectors/update/{id}/")
    Call<Map<String, Object>> updateCollector(@Path("id") int collectorId, @Body Map<String, Object> body);

    @DELETE("collectors/delete/{id}/")
    Call<Void> deleteCollector(@Path("id") int collectorId);

    // ===================== GEOCODING / RUTAS =====================
    @GET("geocode/")
    Call<Map<String, Object>> geocodeAddress(@Query("address") String address, @Query("key") String apiKey);

    @GET("reverse-geocode/")
    Call<Map<String, Object>> reverseGeocode(@Query("lat") double latitude, @Query("lng") double longitude, @Query("key") String apiKey);

    @POST("routes/calculate/")
    Call<Map<String, Object>> calculateRoute(@Body Map<String, Object> body);

    @POST("routes/assign-driver/")
    Call<Map<String, Object>> assignDriverToRoute(@Body Map<String, Object> body);

    // ===================== BALANCEO / CLASIFICACIÓN =====================
    @POST("shipments/classify/")
    Call<Map<String, Object>> classifyGuides();

    // ===================== GUÍAS =====================
    @GET("guides/")
    Call<List<Map<String, Object>>> getGuides();

    @POST("guides/create/")
    Call<Map<String, Object>> createGuide(@Body Map<String, Object> body);

    @PUT("guides/update/{id}/")
    Call<Map<String, Object>> updateGuide(@Path("id") int guideId, @Body Map<String, Object> body);

    @DELETE("guides/delete/{id}/")
    Call<Void> deleteGuide(@Path("id") int guideId);

    @POST("guides/assign/")
    Call<Map<String, Object>> assignGuideToPickup(@Body Map<String, Object> body);

    // ===================== MERCANCÍA =====================
    @POST("merch/create/")
    Call<Map<String, Object>> createMerch(@Body Map<String, Object> body);

    @GET("merch/user/{id}/")
    Call<List<Map<String, Object>>> getMerchByUser(@Path("id") int userId);

    @PUT("merch/update/{id}/")
    Call<Map<String, Object>> updateMerch(@Path("id") int merchId, @Body Map<String, Object> body);

    @PUT("merch/status/{id}/")
    Call<Map<String, Object>> updateMerchStatus(@Path("id") int merchId, @Body Map<String, Object> body);

    @DELETE("merch/delete/{id}/")
    Call<Void> deleteMerch(@Path("id") int merchId);

    // ===================== CAMIONES / TRUCKS =====================
    @POST("trucks/create/")
    Call<Map<String, Object>> createTruck(@Body Map<String, Object> body);

    @GET("trucks/")
    Call<List<Map<String, Object>>> getTrucks();

    @GET("trucks/repartidor/{id}/")
    Call<List<Map<String, Object>>> getTrucksByRepartidor(@Path("id") int userId);

    @GET("trucks/active/")
    Call<List<Map<String, Object>>> getActiveTrucks();

    @GET("trucks/{id}/")
    Call<Map<String, Object>> getTruckById(@Path("id") int truckId);

    @PUT("trucks/update/{id}/")
    Call<Map<String, Object>> updateTruck(@Path("id") int truckId, @Body Map<String, Object> body);

    @PUT("trucks/activate/{id}/")
    Call<Map<String, Object>> activateTruck(@Path("id") int truckId);

    @PUT("trucks/deactivate/{id}/")
    Call<Map<String, Object>> deactivateTruck(@Path("id") int truckId);

    @DELETE("trucks/delete/{id}/")
    Call<Void> deleteTruck(@Path("id") int truckId);

    // ===================== ESTADÍSTICAS =====================
    @GET("shipments/avg/")
    Call<Map<String, Object>> getAverages();

    @GET("shipments/max/")
    Call<Map<String, Object>> getMax();

    @GET("shipments/min/")
    Call<Map<String, Object>> getMin();

    @GET("shipments/count/{status}/")
    Call<Map<String, Object>> getCountByStatus(@Path("status") String status);

    @GET("shipments/weight-distribution/")
    Call<Map<String, Object>> getWeightDistribution();

    @GET("shipments/volume-distribution/")
    Call<Map<String, Object>> getVolumeDistribution();

    @GET("shipments/distance-distribution/")
    Call<Map<String, Object>> getDistanceDistribution();

    // ===================== REPORTES =====================
    @GET("reports/deliveries-per-user/")
    Call<List<Map<String, Object>>> getDeliveriesPerUser();

    @GET("reports/truck-utilization/")
    Call<List<Map<String, Object>>> getTruckUtilization();

    @GET("reports/pending-pickups/")
    Call<List<Map<String, Object>>> getPendingPickupsReport();

    @GET("reports/deliveries-per-truck/")
    Call<List<Map<String, Object>>> getDeliveriesPerTruck();

    @GET("reports/average-delivery-time/")
    Call<Map<String, Object>> getAverageDeliveryTime();

    @GET("reports/delayed-shipments/")
    Call<List<Map<String, Object>>> getDelayedShipments();

    // ===================== NOTIFICACIONES =====================
    @GET("notifications/user/{id}/")
    Call<List<Map<String, Object>>> getUserNotifications(@Path("id") int userId);

    @PUT("notifications/read/{id}/")
    Call<Map<String, Object>> markNotificationAsRead(@Path("id") int notificationId);


    // ================================================================
    // TRACKING POR CÓDIGO DEL ENVÍO (NUEVO)
    // ================================================================

    // 1. Obtener los datos del envío (incluye lat/lng)
    @GET("shipments/location/by-code/")
    Call<Map<String, Object>> getShipmentLocationByCode(@Query("code") String code);

    // 2. Obtener los eventos del envío por código
    @GET("shipments/tracking/by-code/")
    Call<List<Map<String, Object>>> getTrackingEventsByCode(@Query("code") String code);
}