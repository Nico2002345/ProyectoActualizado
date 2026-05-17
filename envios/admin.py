from django.contrib import admin
from .models import User, Shipment, Guide, Truck, Collector, Pickup, TrackingEvent, Merchandise

admin.site.register(User)
admin.site.register(Shipment)
admin.site.register(Guide)
admin.site.register(Truck)
admin.site.register(Collector)
admin.site.register(Pickup)
admin.site.register(TrackingEvent)
admin.site.register(Merchandise)
