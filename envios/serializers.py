from rest_framework import serializers
from .models import User, Shipment, Guide, Truck, Collector, Pickup, TrackingEvent, Merchandise, Notification

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'name', 'email', 'password', 'role']


class ShipmentSerializer(serializers.ModelSerializer):
    class Meta:
        model = Shipment
        fields = ['id', 'user', 'assigned_user', 'shipment_code', 'object_desc',
                  'origin_address', 'origin_latitude', 'origin_longitude',
                  'receiver_address', 'receiver_latitude', 'receiver_longitude',
                  'current_latitude', 'current_longitude', 'last_location_update',
                  'weight_kg', 'distance_km', 'volume_m3',
                  'status', 'created_at', 'latitude', 'longitude',
                  'distance_traveled_km', 'last_tracking_latitude', 'last_tracking_longitude',
                  'simulation_step']


class ShipmentWithRepartidorSerializer(serializers.ModelSerializer):
    assigned_user_name = serializers.SerializerMethodField()

    class Meta:
        model = Shipment
        fields = ['id', 'user', 'assigned_user', 'assigned_user_name', 'shipment_code',
                  'object_desc', 'origin_address', 'origin_latitude', 'origin_longitude',
                  'receiver_address', 'receiver_latitude', 'receiver_longitude',
                  'current_latitude', 'current_longitude', 'last_location_update',
                  'weight_kg', 'distance_km', 'volume_m3',
                  'status', 'created_at', 'latitude', 'longitude',
                  'distance_traveled_km', 'last_tracking_latitude', 'last_tracking_longitude',
                  'simulation_step']

    def get_assigned_user_name(self, obj):
        if obj.assigned_user:
            return obj.assigned_user.name
        return "Sin asignar"


class GuideSerializer(serializers.ModelSerializer):
    class Meta:
        model = Guide
        fields = ['id', 'shipment', 'truck', 'guide_number', 'distance_km', 'created_at']


class TruckSerializer(serializers.ModelSerializer):
    class Meta:
        model = Truck
        fields = ['id', 'plate', 'capacity_kg', 'capacity_m3', 'active']


class CollectorSerializer(serializers.ModelSerializer):
    class Meta:
        model = Collector
        fields = ['id', 'name', 'phone', 'email', 'vehicle', 'active']


class PickupSerializer(serializers.ModelSerializer):
    collector = serializers.PrimaryKeyRelatedField(
        queryset=Collector.objects.all(),
        required=False,
        allow_null=True
    )
    scheduled_at = serializers.DateTimeField(required=False, allow_null=True)

    class Meta:
        model = Pickup
        fields = ['id', 'user', 'collector', 'address', 'scheduled_at',
                  'volume_m3', 'weight_kg', 'status', 'created_at']


class TrackingEventSerializer(serializers.ModelSerializer):
    class Meta:
        model = TrackingEvent
        fields = ['id', 'shipment', 'status', 'location', 'event_time']


class MerchandiseSerializer(serializers.ModelSerializer):
    class Meta:
        model = Merchandise
        fields = ['id', 'user', 'description', 'weight_kg', 'volume_m3',
                  'address', 'status', 'created_at']


class NotificationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Notification
        fields = ['id', 'user', 'shipment', 'title', 'message', 'is_read', 'created_at']