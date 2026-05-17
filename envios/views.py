from rest_framework.response import Response
from rest_framework.decorators import api_view
from rest_framework import status
from django.contrib.auth.hashers import make_password, check_password
from django.db.models import Avg, Max, Min
from django.http import JsonResponse

from .models import User, Collector, Pickup, Merchandise, Shipment, TrackingEvent, Truck, Guide, Notification
from .serializers import UserSerializer, CollectorSerializer, PickupSerializer, MerchandiseSerializer, ShipmentSerializer, ShipmentWithRepartidorSerializer, TrackingEventSerializer, TruckSerializer, GuideSerializer, NotificationSerializer

# ------------------------
# Función de utilidad para calcular distancia
# ------------------------
from math import radians, cos, sin, asin, sqrt

def haversine_distance(lat1, lon1, lat2, lon2):
    """
    Calcula la distancia en kilómetros entre dos puntos GPS usando la fórmula de Haversine.

    Args:
        lat1, lon1: Latitud y longitud del primer punto
        lat2, lon2: Latitud y longitud del segundo punto

    Returns:
        Distancia en kilómetros
    """
    # Convertir grados a radianes
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])

    # Fórmula de Haversine
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a))

    # Radio de la Tierra en kilómetros
    radius_km = 6371

    return c * radius_km

# ------------------------
# Usuarios
# ------------------------
@api_view(['POST'])
def create_user(request):
    name = request.data.get('name', '').strip()
    email = request.data.get('email', '').strip()
    password = request.data.get('password', '').strip()
    role = request.data.get('role', 'USUARIO').strip().upper()

    if not name or not email or not password:
        return Response({"error": "Faltan campos obligatorios: name, email o password."}, status=status.HTTP_400_BAD_REQUEST)

    if User.objects.filter(email=email.lower()).exists():
        return Response({"error": "Email ya registrado."}, status=status.HTTP_400_BAD_REQUEST)

    user = User.objects.create(
        name=name,
        email=email.lower(),
        password=make_password(password),
        role=role
    )

    return Response({
        "id": user.id,
        "name": user.name,
        "email": user.email,
        "role": user.role,
        "message": "Usuario creado correctamente ✅"
    }, status=status.HTTP_201_CREATED)


@api_view(['POST'])
def login_user(request):
    email = request.data.get('email', '').strip()
    password = request.data.get('password', '').strip()

    if not email or not password:
        return Response({"error": "Faltan campos email o password."}, status=status.HTTP_400_BAD_REQUEST)

    try:
        user = User.objects.get(email=email.lower())
    except User.DoesNotExist:
        return Response({"error": "Usuario o contraseña incorrectos."}, status=status.HTTP_401_UNAUTHORIZED)

    if not check_password(password, user.password):
        return Response({"error": "Usuario o contraseña incorrectos."}, status=status.HTTP_401_UNAUTHORIZED)

    return Response({
        "id": user.id,
        "name": user.name,
        "email": user.email,
        "role": user.role
    }, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_users(request):
    """Obtener todos los usuarios"""
    users = User.objects.all().order_by('name')
    serializer = UserSerializer(users, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_user_by_id(request, user_id):
    try:
        user = User.objects.get(id=user_id)
    except User.DoesNotExist:
        return Response({"error": "Usuario no existe."}, status=status.HTTP_404_NOT_FOUND)

    serializer = UserSerializer(user)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['PUT'])
def update_user(request, user_id):
    try:
        user = User.objects.get(id=user_id)
    except User.DoesNotExist:
        return Response({"error": "Usuario no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    data = request.data.copy()
    if "password" in data and data["password"].strip():
        data["password"] = make_password(data["password"])
    else:
        data.pop("password", None)

    serializer = UserSerializer(user, data=data, partial=True)
    if serializer.is_valid():
        serializer.save()
        return Response({"message": "Usuario actualizado ✅"}, status=status.HTTP_200_OK)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


# ------------------------
# Recolectores
# ------------------------
@api_view(['GET'])
def get_collectors(request):
    collectors = Collector.objects.filter(active=True).order_by('name')
    serializer = CollectorSerializer(collectors, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_collector_by_id(request, collector_id):
    try:
        collector = Collector.objects.get(id=collector_id)
    except Collector.DoesNotExist:
        return Response({"error": "Collector no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    serializer = CollectorSerializer(collector)
    return Response(serializer.data, status=status.HTTP_200_OK)


# ------------------------
# Pickups
# ------------------------
@api_view(['GET'])
def get_pickups_by_user(request, user_id):
    pickups = Pickup.objects.filter(user_id=user_id).order_by('-created_at')
    serializer = PickupSerializer(pickups, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_pickups_by_collector(request, collector_id):
    pickups = Pickup.objects.filter(collector_id=collector_id).exclude(status='FINALIZADO').order_by('scheduled_at')
    serializer = PickupSerializer(pickups, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_pickups_by_recolector_user(request, user_id):
    """Obtener pickups del recolector usando su user_id
    Parámetros opcionales:
    - include_completed: Si es 'true', incluye pickups completados (COLECTADA, FINALIZADO)
    """
    try:
        # Obtener el usuario recolector
        user = User.objects.get(id=user_id)

        # Buscar el Collector con el mismo email
        collector = Collector.objects.filter(email=user.email).first()

        if not collector:
            return Response([], status=status.HTTP_200_OK)

        # Verificar si debe incluir completados
        include_completed = request.GET.get('include_completed', 'false').lower() == 'true'

        # Obtener los pickups del collector
        if include_completed:
            pickups = Pickup.objects.filter(
                collector_id=collector.id,
                status__in=['COLECTADA', 'FINALIZADO']
            ).order_by('-created_at')
        else:
            # Solo mostrar pickups en estado ASIGNADA (pendientes de recoger)
            pickups = Pickup.objects.filter(
                collector_id=collector.id,
                status='ASIGNADA'
            ).order_by('scheduled_at')

        serializer = PickupSerializer(pickups, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    except User.DoesNotExist:
        return Response({"error": "Usuario no encontrado"}, status=status.HTTP_404_NOT_FOUND)


@api_view(['POST'])
def create_pickup(request):
    serializer = PickupSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save(status='PENDIENTE')
        return Response(serializer.data, status=status.HTTP_201_CREATED)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['PUT'])
def assign_pickup(request, pickup_id):
    try:
        pickup = Pickup.objects.get(id=pickup_id)
    except Pickup.DoesNotExist:
        return Response({"error": "Pickup no existe"}, status=status.HTTP_404_NOT_FOUND)

    collector_id = request.data.get("collector_id")
    if not collector_id:
        return Response({"error": "collector_id es obligatorio"}, status=status.HTTP_400_BAD_REQUEST)

    pickup.collector_id = collector_id
    pickup.status = "ASIGNADA"
    pickup.save()
    return Response(PickupSerializer(pickup).data, status=status.HTTP_200_OK)


@api_view(['PUT'])
def update_pickup_status(request, pickup_id):
    try:
        pickup = Pickup.objects.get(id=pickup_id)
    except Pickup.DoesNotExist:
        return Response({"error": "Pickup no existe"}, status=status.HTTP_404_NOT_FOUND)

    status_value = request.data.get("status")
    if not status_value:
        return Response({"error": "status es obligatorio"}, status=status.HTTP_400_BAD_REQUEST)

    pickup.status = status_value
    pickup.save()
    return Response(PickupSerializer(pickup).data, status=status.HTTP_200_OK)


# ------------------------
# Shipments (Envíos)
# ------------------------
@api_view(['POST'])
def create_shipment(request):
    """Crear un nuevo envío"""
    serializer = ShipmentSerializer(data=request.data)
    if serializer.is_valid():
        shipment = serializer.save()

        # Crear evento de tracking inicial automáticamente
        TrackingEvent.objects.create(
            shipment=shipment,
            status=shipment.status,
            location="Envío registrado en el sistema"
        )

        return Response(serializer.data, status=status.HTTP_201_CREATED)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['GET'])
def get_all_shipments(request):
    """Obtener todos los envíos"""
    shipments = Shipment.objects.all().order_by('-created_at')
    serializer = ShipmentSerializer(shipments, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_assigned_shipments_with_repartidor(request):
    """Obtener envíos ASIGNADOS con información del repartidor"""
    shipments = Shipment.objects.filter(status='ASIGNADO').order_by('-created_at')
    serializer = ShipmentWithRepartidorSerializer(shipments, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_shipments_by_user(request, user_id):
    """Obtener envíos creados por un usuario específico"""
    shipments = Shipment.objects.filter(user_id=user_id).order_by('-created_at')
    serializer = ShipmentSerializer(shipments, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_shipments_assigned_to_user(request, user_id):
    """Obtener envíos asignados a un repartidor específico"""
    shipments = Shipment.objects.filter(assigned_user_id=user_id).order_by('-created_at')
    serializer = ShipmentSerializer(shipments, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_shipment_by_id(request, shipment_id):
    """Obtener un envío por ID"""
    try:
        shipment = Shipment.objects.get(id=shipment_id)
    except Shipment.DoesNotExist:
        return Response({"error": "Envío no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    serializer = ShipmentSerializer(shipment)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_shipment_by_code(request):
    """Obtener un envío por código (query param: ?code=ENV-...)"""
    code = request.query_params.get('code', None)
    if not code:
        return Response({"error": "El parámetro 'code' es obligatorio"}, status=status.HTTP_400_BAD_REQUEST)

    try:
        shipment = Shipment.objects.get(shipment_code=code)
    except Shipment.DoesNotExist:
        return Response({"error": "Envío no encontrado con ese código"}, status=status.HTTP_404_NOT_FOUND)

    serializer = ShipmentSerializer(shipment)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['PUT'])
def update_shipment(request, shipment_id):
    """Actualizar un envío completo"""
    try:
        shipment = Shipment.objects.get(id=shipment_id)
    except Shipment.DoesNotExist:
        return Response({"error": "Envío no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    serializer = ShipmentSerializer(shipment, data=request.data, partial=True)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=status.HTTP_200_OK)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['PUT'])
def reassign_shipment_repartidor(request, shipment_id):
    """Reasignar un envío a otro repartidor"""
    try:
        shipment = Shipment.objects.get(id=shipment_id)
    except Shipment.DoesNotExist:
        return Response({"error": "Envío no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    new_repartidor_id = request.data.get('assigned_user_id')
    if not new_repartidor_id:
        return Response({"error": "assigned_user_id es obligatorio"}, status=status.HTTP_400_BAD_REQUEST)

    try:
        new_repartidor = User.objects.get(id=new_repartidor_id)
    except User.DoesNotExist:
        return Response({"error": "Repartidor no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    # Actualizar el repartidor asignado
    old_repartidor = shipment.assigned_user
    shipment.assigned_user = new_repartidor
    shipment.save()

    # Crear evento de tracking
    location_msg = f'Reasignado de {old_repartidor.name if old_repartidor else "sin asignar"} a {new_repartidor.name}'
    TrackingEvent.objects.create(
        shipment=shipment,
        status=shipment.status,
        location=location_msg
    )

    return Response({
        "message": "Repartidor reasignado correctamente",
        "shipment": ShipmentSerializer(shipment).data
    }, status=status.HTTP_200_OK)


@api_view(['PUT'])
def update_shipment_status(request, shipment_id):
    """Actualizar solo el estado de un envío"""
    try:
        shipment = Shipment.objects.get(id=shipment_id)
    except Shipment.DoesNotExist:
        return Response({"error": "Envío no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    status_value = request.data.get("status")
    if not status_value:
        return Response({"error": "status es obligatorio"}, status=status.HTTP_400_BAD_REQUEST)

    shipment.status = status_value
    shipment.save()

    # Crear notificación cuando el estado cambia a EN_TRANSITO
    if status_value == "EN_TRANSITO" and shipment.user:
        Notification.objects.create(
            user=shipment.user,
            shipment=shipment,
            title="Tu envío está en tránsito",
            message=f"El envío {shipment.shipment_code} ha sido marcado como EN_TRANSITO y está en camino a su destino."
        )

    return Response(ShipmentSerializer(shipment).data, status=status.HTTP_200_OK)


@api_view(['DELETE'])
def delete_shipment(request, shipment_id):
    """Eliminar un envío"""
    try:
        shipment = Shipment.objects.get(id=shipment_id)
    except Shipment.DoesNotExist:
        return Response({"error": "Envío no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    shipment.delete()
    return Response({"message": "Envío eliminado correctamente"}, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_pending_pickups(request):
    """Obtener envíos pendientes (CREADO o PENDIENTE) para asignar a camiones"""
    shipments = Shipment.objects.filter(status__in=['CREADO', 'PENDIENTE']).order_by('-created_at')
    serializer = ShipmentSerializer(shipments, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['PUT'])
def assign_shipment_to_truck(request, shipment_id):
    """Asignar un envío a un camión creando una guía"""
    try:
        shipment = Shipment.objects.get(id=shipment_id)
    except Shipment.DoesNotExist:
        return Response({"error": "Envío no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    truck_id = request.data.get('truck_id')
    if not truck_id:
        return Response({"error": "truck_id es obligatorio"}, status=status.HTTP_400_BAD_REQUEST)

    try:
        truck = Truck.objects.get(id=truck_id)
    except Truck.DoesNotExist:
        return Response({"error": "Camión no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    # Verificar que el envío no tenga ya una guía asignada
    if shipment.guides.exists():
        return Response({"error": "El envío ya tiene una guía asignada"}, status=status.HTTP_400_BAD_REQUEST)

    # Obtener y asignar repartidor si se proporciona
    assigned_user_id = request.data.get('assigned_user_id')
    assigned_user = None
    if assigned_user_id:
        try:
            assigned_user = User.objects.get(id=assigned_user_id)
        except User.DoesNotExist:
            return Response({"error": "Repartidor no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    # Crear guía con el camión asignado
    guide_number = f"GUIA-{shipment.shipment_code}"
    Guide.objects.create(
        shipment=shipment,
        truck=truck,
        guide_number=guide_number,
        distance_km=shipment.distance_km
    )

    # Actualizar estado del envío y asignar repartidor
    shipment.status = 'ASIGNADO'
    if assigned_user:
        shipment.assigned_user = assigned_user
    shipment.save()

    # Crear evento de tracking
    location_msg = f'Asignado a camión {truck.plate}'
    if assigned_user:
        location_msg += f' - Repartidor: {assigned_user.name}'

    TrackingEvent.objects.create(
        shipment=shipment,
        status='ASIGNADO',
        location=location_msg
    )

    return Response({
        "message": "Envío asignado correctamente al camión",
        "shipment": ShipmentSerializer(shipment).data
    }, status=status.HTTP_200_OK)


# ------------------------
# Tracking de Shipments
# ------------------------
@api_view(['GET'])
def get_tracking_events(request, shipment_id):
    """Obtener eventos de seguimiento de un envío"""
    try:
        shipment = Shipment.objects.get(id=shipment_id)
    except Shipment.DoesNotExist:
        return Response({"error": "Envío no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    events = TrackingEvent.objects.filter(shipment_id=shipment_id).order_by('-event_time')
    serializer = TrackingEventSerializer(events, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['POST'])
def create_tracking_event(request):
    """Crear un nuevo evento de seguimiento"""
    serializer = TrackingEventSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=status.HTTP_201_CREATED)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


# ------------------------
# Clasificación y asignación de camiones
# ------------------------
@api_view(['POST'])
def classify_and_assign_trucks(request):
    """Clasificar guías y asignar camiones a los envíos"""
    # Obtener shipments sin guía o pendientes de asignación
    shipments = Shipment.objects.filter(status__in=['CREADO', 'PENDIENTE']).exclude(
        guides__isnull=False
    )

    if not shipments.exists():
        return Response({"message": "No hay envíos pendientes de clasificación", "assigned": 0}, status=status.HTTP_200_OK)

    # Obtener camiones activos
    trucks = Truck.objects.filter(active=True).order_by('-capacity_kg', '-capacity_m3')

    if not trucks.exists():
        return Response({"error": "No hay camiones disponibles"}, status=status.HTTP_400_BAD_REQUEST)

    assigned_count = 0

    # Asignar camiones a envíos según capacidad
    for shipment in shipments:
        # Buscar camión adecuado según peso y volumen
        suitable_truck = None
        for truck in trucks:
            if shipment.weight_kg <= truck.capacity_kg and shipment.volume_m3 <= truck.capacity_m3:
                suitable_truck = truck
                break

        if suitable_truck:
            # Generar número de guía único
            guide_number = f"GUIA-{shipment.shipment_code}"

            # Crear guía asociada al envío con el camión
            Guide.objects.create(
                shipment=shipment,
                truck=suitable_truck,
                guide_number=guide_number,
                distance_km=shipment.distance_km
            )

            # Actualizar estado del envío
            shipment.status = 'ASIGNADO'
            shipment.save()

            # Crear evento de tracking
            TrackingEvent.objects.create(
                shipment=shipment,
                status='ASIGNADO',
                location=f'Asignado a camión {suitable_truck.plate}'
            )

            assigned_count += 1

    return Response({
        "message": f"Se clasificaron y asignaron {assigned_count} envíos exitosamente",
        "assigned": assigned_count,
        "total": shipments.count()
    }, status=status.HTTP_200_OK)


# ------------------------
# Trucks (Camiones)
# ------------------------
@api_view(['GET'])
def get_trucks(request):
    """Obtener todos los camiones"""
    trucks = Truck.objects.all().order_by('plate')
    serializer = TruckSerializer(trucks, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_trucks_by_repartidor(request, user_id):
    """Obtener camiones asignados a un repartidor específico"""
    import logging
    logger = logging.getLogger(_name_)

    # Obtener shipments asignados al repartidor
    shipments = Shipment.objects.filter(assigned_user_id=user_id)
    logger.info(f"Repartidor {user_id}: {shipments.count()} shipments asignados")

    # Obtener las guías de esos shipments que tienen camión asignado
    guides = Guide.objects.filter(shipment_in=shipments, truck_isnull=False)
    logger.info(f"Repartidor {user_id}: {guides.count()} guías con camión asignado")

    # Obtener los camiones únicos
    truck_ids = guides.values_list('truck_id', flat=True).distinct()
    logger.info(f"Repartidor {user_id}: truck_ids={list(truck_ids)}")

    trucks = Truck.objects.filter(id__in=truck_ids)
    logger.info(f"Repartidor {user_id}: {trucks.count()} camiones encontrados")

    serializer = TruckSerializer(trucks, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['POST'])
def create_truck(request):
    """Crear un nuevo camión"""
    serializer = TruckSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=status.HTTP_201_CREATED)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['PUT'])
def update_truck(request, truck_id):
    """Actualizar un camión"""
    try:
        truck = Truck.objects.get(id=truck_id)
    except Truck.DoesNotExist:
        return Response({"error": "Camión no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    serializer = TruckSerializer(truck, data=request.data, partial=True)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=status.HTTP_200_OK)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['DELETE'])
def delete_truck(request, truck_id):
    """Eliminar un camión"""
    try:
        truck = Truck.objects.get(id=truck_id)
    except Truck.DoesNotExist:
        return Response({"error": "Camión no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    truck.delete()
    return Response({"message": "Camión eliminado correctamente"}, status=status.HTTP_200_OK)


# ------------------------
# Collectors (Recolectores)
# ------------------------
@api_view(['POST'])
def create_collector(request):
    """Crear un nuevo recolector"""
    serializer = CollectorSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=status.HTTP_201_CREATED)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


# ------------------------
# Mercancía
# ------------------------
@api_view(['POST'])
def create_merchandise(request):
    serializer = MerchandiseSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save(status="PENDIENTE")
        return Response(serializer.data, status=status.HTTP_201_CREATED)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['GET'])
def get_merchandise_by_user(request, user_id):
    merch = Merchandise.objects.filter(user_id=user_id).order_by('-created_at')
    serializer = MerchandiseSerializer(merch, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['PUT'])
def update_merchandise_status(request, merch_id):
    try:
        merch = Merchandise.objects.get(id=merch_id)
    except Merchandise.DoesNotExist:
        return Response({"error": "No existe"}, status=status.HTTP_404_NOT_FOUND)

    status_value = request.data.get("status")
    if not status_value:
        return Response({"error": "status es obligatorio"}, status=status.HTTP_400_BAD_REQUEST)

    merch.status = status_value
    merch.save()
    return Response(MerchandiseSerializer(merch).data, status=status.HTTP_200_OK)


@api_view(['DELETE'])
def delete_merchandise(request, merch_id):
    try:
        merch = Merchandise.objects.get(id=merch_id)
    except Merchandise.DoesNotExist:
        return Response({"error": "No existe"}, status=status.HTTP_404_NOT_FOUND)

    merch.delete()
    return Response({"message": "Eliminado"}, status=status.HTTP_200_OK)


# ------------------------
# Estadísticas de envíos
# ------------------------
@api_view(['GET'])
def shipment_averages(request):
    data = Shipment.objects.aggregate(
        avg_weight=Avg('weight_kg'),
        avg_volume=Avg('volume_m3'),
        avg_distance=Avg('distance_km'),
    )
    return Response(data, status=status.HTTP_200_OK)


@api_view(['GET'])
def shipment_max(request):
    data = Shipment.objects.aggregate(
        max_weight=Max('weight_kg'),
        max_volume=Max('volume_m3'),
        max_distance=Max('distance_km'),
    )
    return Response(data, status=status.HTTP_200_OK)


@api_view(['GET'])
def shipment_min(request):
    data = Shipment.objects.aggregate(
        min_weight=Min('weight_kg'),
        min_volume=Min('volume_m3'),
        min_distance=Min('distance_km'),
    )
    return Response(data, status=status.HTTP_200_OK)


@api_view(['GET'])
def shipment_count(request, status_name):
    count = Shipment.objects.filter(status=status_name).count()
    return Response({"count": count}, status=status.HTTP_200_OK)


# ------------------------
# Pickups adicionales (para solicitud de recogida H8)
# ------------------------
@api_view(['GET'])
def get_all_pickups_pending(request):
    """Obtener todos los pickups pendientes de asignación"""
    pickups = Pickup.objects.filter(status='PENDIENTE').order_by('-created_at')
    serializer = PickupSerializer(pickups, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_all_pickups_completed(request):
    """Obtener todos los pickups completados"""
    pickups = Pickup.objects.filter(status__in=['COLECTADA', 'FINALIZADO']).order_by('-created_at')
    serializer = PickupSerializer(pickups, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['PUT'])
def assign_collector_and_truck_to_pickup(request, pickup_id):
    """Asignar recolector (usuario) y camión a un pickup"""
    try:
        pickup = Pickup.objects.get(id=pickup_id)
    except Pickup.DoesNotExist:
        return Response({"error": "Pickup no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    # Aceptar tanto collector_id (ID de Collector) como user_id (ID de User RECOLECTOR)
    collector_id = request.data.get('collector_id')
    recolector_user_id = request.data.get('recolector_user_id')
    truck_id = request.data.get('truck_id')

    # Obtener el camión si se proporciona
    truck = None
    if truck_id:
        try:
            truck = Truck.objects.get(id=truck_id)
        except Truck.DoesNotExist:
            return Response({"error": "Camión no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    if recolector_user_id:
        # Si se envía un user_id, buscar o crear el Collector asociado
        try:
            recolector_user = User.objects.get(id=recolector_user_id)

            # Buscar si existe un Collector con el mismo email
            collector, created = Collector.objects.get_or_create(
                email=recolector_user.email,
                defaults={
                    'name': recolector_user.name,
                    'phone': '',
                    'active': True,
                    'vehicle': truck
                }
            )

            # Si ya existía, actualizar el camión
            if not created and truck:
                collector.vehicle = truck
                collector.save()

            pickup.collector = collector

        except User.DoesNotExist:
            return Response({"error": "Usuario recolector no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    elif collector_id:
        # Si se envía collector_id directamente
        try:
            collector = Collector.objects.get(id=collector_id)

            # Actualizar el camión si se proporciona
            if truck:
                collector.vehicle = truck
                collector.save()

            pickup.collector = collector
        except Collector.DoesNotExist:
            return Response({"error": "Collector no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    # Actualizar estado a ASIGNADA
    pickup.status = 'ASIGNADA'
    pickup.save()

    return Response({
        "message": "Recolector y camión asignados correctamente",
        "pickup": PickupSerializer(pickup).data
    }, status=status.HTTP_200_OK)


# ------------------------
# Notificaciones
# ------------------------

@api_view(['GET'])
def get_user_notifications(request, user_id):
    """Obtener todas las notificaciones de un usuario, ordenadas por más recientes"""
    notifications = Notification.objects.filter(user_id=user_id).order_by('-created_at')
    serializer = NotificationSerializer(notifications, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['PUT'])
def mark_notification_as_read(request, notification_id):
    """Marcar una notificación como leída"""
    try:
        notification = Notification.objects.get(id=notification_id)
    except Notification.DoesNotExist:
        return Response({"error": "Notificación no encontrada"}, status=status.HTTP_404_NOT_FOUND)

    notification.is_read = True
    notification.save()
    return Response({"message": "Notificación marcada como leída"}, status=status.HTTP_200_OK)


# ------------------------
# Tracking en Tiempo Real
# ------------------------

@api_view(['PUT'])
def update_shipment_location(request, shipment_id):
    """
    Actualizar la ubicación actual del envío (para repartidor)
    y calcular distancia recorrida incremental (odómetro)
    """
    try:
        shipment = Shipment.objects.get(id=shipment_id)
    except Shipment.DoesNotExist:
        return Response({"error": "Envío no encontrado"}, status=status.HTTP_404_NOT_FOUND)

    latitude = request.data.get('latitude')
    longitude = request.data.get('longitude')
    simulation_step = request.data.get('simulation_step')

    if latitude is None or longitude is None:
        return Response({"error": "Se requieren latitude y longitude"}, status=status.HTTP_400_BAD_REQUEST)

    # 🔹 ODÓMETRO: Calcular distancia incremental
    distance_increment = 0
    if shipment.last_tracking_latitude is not None and shipment.last_tracking_longitude is not None:
        # Calcular distancia desde la última ubicación registrada
        distance_increment = haversine_distance(
            shipment.last_tracking_latitude,
            shipment.last_tracking_longitude,
            latitude,
            longitude
        )
        # Acumular la distancia recorrida
        shipment.distance_traveled_km += distance_increment

    # Actualizar ubicación actual
    shipment.current_latitude = latitude
    shipment.current_longitude = longitude

    # Actualizar última ubicación de tracking (para próximo cálculo)
    shipment.last_tracking_latitude = latitude
    shipment.last_tracking_longitude = longitude

    # 🔹 Actualizar paso de simulación si se proporciona
    if simulation_step is not None:
        shipment.simulation_step = simulation_step

    # Importar timezone para usar la hora actual con zona horaria
    from django.utils import timezone
    shipment.last_location_update = timezone.now()

    shipment.save()

    serializer = ShipmentSerializer(shipment)
    return Response({
        "message": "Ubicación actualizada correctamente",
        "distance_increment_km": round(distance_increment, 3),
        "total_distance_traveled_km": round(shipment.distance_traveled_km, 3),
        "simulation_step": shipment.simulation_step,
        "shipment": serializer.data
    }, status=status.HTTP_200_OK)