from django.urls import path
from .views import (
    get_collectors, get_collector_by_id,
    get_pickups_by_user, get_pickups_by_collector, get_pickups_by_recolector_user, get_all_pickups_pending, get_all_pickups_completed,
    create_pickup, assign_pickup, update_pickup_status, assign_collector_and_truck_to_pickup,
    create_user, login_user, get_users,
    create_merchandise, get_merchandise_by_user, update_merchandise_status, delete_merchandise,
    create_shipment, get_all_shipments, get_assigned_shipments_with_repartidor, get_shipments_by_user, get_shipments_assigned_to_user, get_shipment_by_id, get_shipment_by_code,
    update_shipment, update_shipment_status, reassign_shipment_repartidor, delete_shipment,
    get_pending_pickups, assign_shipment_to_truck,
    shipment_averages, shipment_max, shipment_min, shipment_count,
    get_tracking_events, create_tracking_event,
    classify_and_assign_trucks,
    get_trucks, get_trucks_by_repartidor, create_truck, update_truck, delete_truck,
    create_collector,
    get_user_notifications, mark_notification_as_read,
    update_shipment_location
)
urlpatterns = [
    # === Collectors ===
    path('collectors/', get_collectors),
    path('collectors/<int:collector_id>/', get_collector_by_id),
    path('collectors/create/', create_collector),

    # === Trucks ===
    path('trucks/', get_trucks),
    path('trucks/repartidor/<int:user_id>/', get_trucks_by_repartidor),
    path('trucks/create/', create_truck),
    path('trucks/update/<int:truck_id>/', update_truck),
    path('trucks/delete/<int:truck_id>/', delete_truck),

    # === Pickups ===
    path('pickups/user/<int:user_id>/', get_pickups_by_user),
    path('pickups/collector/<int:collector_id>/', get_pickups_by_collector),
    path('pickups/recolector-user/<int:user_id>/', get_pickups_by_recolector_user),
    path('pickups/all-pending/', get_all_pickups_pending),
    path('pickups/all-completed/', get_all_pickups_completed),
    path('pickups/create/', create_pickup),
    path('pickups/assign/<int:pickup_id>/', assign_pickup),
    path('pickups/assign-collector-truck/<int:pickup_id>/', assign_collector_and_truck_to_pickup),
    path('pickups/status/<int:pickup_id>/', update_pickup_status),
    path('pickups/pending/', get_pending_pickups),
    path('pickups/assign-truck/<int:shipment_id>/', assign_shipment_to_truck),

    # === Shipments ===
    path('shipments/create/', create_shipment),
    path('shipments/', get_all_shipments),
    path('shipments/assigned-with-repartidor/', get_assigned_shipments_with_repartidor),
    path('shipments/user/<int:user_id>/', get_shipments_by_user),
    path('shipments/assigned/<int:user_id>/', get_shipments_assigned_to_user),
    path('shipments/<int:shipment_id>/', get_shipment_by_id),
    path('shipments/by-code/', get_shipment_by_code),
    path('shipments/update/<int:shipment_id>/', update_shipment),
    path('shipments/status/<int:shipment_id>/', update_shipment_status),
    path('shipments/reassign/<int:shipment_id>/', reassign_shipment_repartidor),
    path('shipments/delete/<int:shipment_id>/', delete_shipment),

    # === Users ===
    path('users/', get_users),
    path('users/create/', create_user),
    path('users/login/', login_user),

    # === Merchandise ===
    path('merch/create/', create_merchandise),
    path('merch/user/<int:user_id>/', get_merchandise_by_user),
    path('merch/status/<int:merch_id>/', update_merchandise_status),
    path('merch/delete/<int:merch_id>/', delete_merchandise),

    # === Tracking de Shipments ===
    path('shipments/tracking/<int:shipment_id>/', get_tracking_events),
    path('shipments/tracking/create/', create_tracking_event),

    # === Clasificación y asignación ===
    path('shipments/classify/', classify_and_assign_trucks),

    # === Estadísticas de Shipments ===
    path('shipments/avg/', shipment_averages),
    path('shipments/max/', shipment_max),
    path('shipments/min/', shipment_min),
    path('shipments/count/<str:status_name>/', shipment_count),

    # === Notificaciones ===
    path('notifications/user/<int:user_id>/', get_user_notifications),
    path('notifications/read/<int:notification_id>/', mark_notification_as_read),

    # === Tracking en Tiempo Real ===
    path('shipments/<int:shipment_id>/update-location/', update_shipment_location),
]