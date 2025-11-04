package com.example.ingsoftcalvoproy.network;

import com.example.ingsoftcalvoproy.models.Shipment;
import com.example.ingsoftcalvoproy.models.TrackingEvent;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @GET("shipments")
    Call<List<Shipment>> getShipments();

    @POST("shipments")
    Call<Shipment> createShipment(@Body Shipment shipment);

    @PUT("shipments/{id}")
    Call<Shipment> updateShipment(@Path("id") int id, @Body Shipment shipment);

    @POST("tracking-events")
    Call<TrackingEvent> createTrackingEvent(@Body TrackingEvent event);
}

