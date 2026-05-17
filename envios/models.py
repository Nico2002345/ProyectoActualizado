from django.db import models

class User(models.Model):
    name = models.CharField(max_length=255)
    email = models.EmailField(unique=True)
    password = models.CharField(max_length=255)
    role = models.CharField(max_length=50, default='USUARIO')

    def _str_(self):
        return self.name


class Shipment(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=True, blank=True, related_name='created_shipments')  # Usuario que creó el envío
    assigned_user = models.ForeignKey(User, on_delete=models.SET_NULL, null=True, blank=True, related_name='assigned_shipments')  # Repartidor asignado
    shipment_code = models.CharField(max_length=255, unique=True, null=True, blank=True)
    object_desc = models.TextField(null=True, blank=True)

    # 🔹 Dirección de origen
    origin_address = models.TextField(null=True, blank=True)
    origin_latitude = models.FloatField(null=True, blank=True)
    origin_longitude = models.FloatField(null=True, blank=True)

    # 🔹 Dirección de destino
    receiver_address = models.TextField(null=True, blank=True)
    receiver_latitude = models.FloatField(null=True, blank=True)
    receiver_longitude = models.FloatField(null=True, blank=True)

    weight_kg = models.FloatField(default=0)
    distance_km = models.FloatField(default=0)
    volume_m3 = models.FloatField(default=0)
    status = models.CharField(max_length=50, default='CREADO')
    created_at = models.DateTimeField(auto_now_add=True)

    # 🔹 Campos deprecados (mantener por compatibilidad, pero usar receiver_*)
    latitude = models.FloatField(null=True, blank=True)
    longitude = models.FloatField(null=True, blank=True)

    # 🔹 Ubicación ACTUAL del envío (actualizada por el repartidor en tiempo real)
    current_latitude = models.FloatField(null=True, blank=True)
    current_longitude = models.FloatField(null=True, blank=True)
    last_location_update = models.DateTimeField(null=True, blank=True)  # Última actualización

    # 🔹 Odómetro: Distancia recorrida en tiempo real
    distance_traveled_km = models.FloatField(default=0, help_text="Distancia total recorrida por el repartidor en km")
    last_tracking_latitude = models.FloatField(null=True, blank=True, help_text="Última lat para cálculo de distancia")
    last_tracking_longitude = models.FloatField(null=True, blank=True, help_text="Última lon para cálculo de distancia")

    # 🔹 Simulación de ruta: Paso actual en la simulación
    simulation_step = models.IntegerField(default=0, help_text="Paso actual en la simulación de ruta (0-10)")

    def _str_(self):
        return f"{self.shipment_code} - {self.status}"



class Guide(models.Model):
    shipment = models.ForeignKey(Shipment, on_delete=models.CASCADE, related_name='guides')
    truck = models.ForeignKey('Truck', on_delete=models.SET_NULL, null=True, blank=True)
    guide_number = models.CharField(max_length=255, unique=True, null=True, blank=True)
    distance_km = models.FloatField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)


class Truck(models.Model):
    plate = models.CharField(max_length=50, unique=True)
    capacity_kg = models.FloatField(default=0)
    capacity_m3 = models.FloatField(default=0)
    active = models.BooleanField(default=True)


class Collector(models.Model):
    name = models.CharField(max_length=255)
    phone = models.CharField(max_length=50, null=True, blank=True)
    email = models.CharField(max_length=255, null=True, blank=True)
    vehicle = models.ForeignKey(Truck, on_delete=models.SET_NULL, null=True, blank=True)
    active = models.BooleanField(default=True)


class Pickup(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    collector = models.ForeignKey(Collector, on_delete=models.SET_NULL, null=True, blank=True)
    address = models.TextField()
    scheduled_at = models.DateTimeField(null=True, blank=True)
    volume_m3 = models.FloatField(default=0)
    weight_kg = models.FloatField(default=0)
    status = models.CharField(max_length=50, default='PENDIENTE')
    created_at = models.DateTimeField(auto_now_add=True)


class TrackingEvent(models.Model):
    shipment = models.ForeignKey(Shipment, on_delete=models.CASCADE)
    status = models.CharField(max_length=255, null=True, blank=True)
    location = models.CharField(max_length=255, null=True, blank=True)
    event_time = models.DateTimeField(auto_now_add=True)


class Merchandise(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    description = models.TextField()
    weight_kg = models.FloatField(default=0)
    volume_m3 = models.FloatField(default=0)
    address = models.TextField(null=True, blank=True)
    status = models.CharField(max_length=50, default='PENDIENTE')
    created_at = models.DateTimeField(auto_now_add=True)


class Notification(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='notifications')
    shipment = models.ForeignKey(Shipment, on_delete=models.CASCADE, null=True, blank=True)
    title = models.CharField(max_length=255)
    message = models.TextField()
    is_read = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    def _str_(self):
        return f"{self.title} - {self.user.name}"