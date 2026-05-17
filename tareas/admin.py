from django.contrib import admin
from .models import Task

@admin.register(Task)
class TaskAdmin(admin.ModelAdmin):
    list_display = ('titulo', 'estado', 'fecha_hora', 'creado_en')
    list_filter = ('estado', 'fecha_hora')
    search_fields = ('titulo', 'descripcion')
    ordering = ('-fecha_hora',)
