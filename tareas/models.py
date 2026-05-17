from django.db import models
from django.utils import timezone

class Task(models.Model):
    ESTADO_CHOICES = [
        ('CREADO', 'Creado'),
        ('PENDIENTE', 'Pendiente'),
        ('HECHO', 'Hecho'),
    ]

    titulo = models.CharField(max_length=255)
    descripcion = models.TextField(blank=True, null=True)
    fecha_hora = models.DateTimeField(default=timezone.now)
    estado = models.CharField(max_length=20, choices=ESTADO_CHOICES, default='CREADO')
    creado_en = models.DateTimeField(auto_now_add=True)
    actualizado_en = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ['-fecha_hora']

    def __str__(self):
        return f"{self.titulo} - {self.estado}"

    def get_color(self):
        """Retorna el color según el estado"""
        colores = {
            'CREADO': '#dc3545',      # Rojo
            'PENDIENTE': '#ffc107',   # Amarillo
            'HECHO': '#28a745',       # Verde
        }
        return colores.get(self.estado, '#6c757d')
