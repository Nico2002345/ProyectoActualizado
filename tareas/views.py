from django.shortcuts import render, redirect, get_object_or_404
from django.contrib import messages
from .models import Task
from datetime import datetime

def lista_tareas(request):
    tareas = Task.objects.all()
    return render(request, 'tareas/lista_tareas.html', {'tareas': tareas})

def crear_tarea(request):
    if request.method == 'POST':
        titulo = request.POST.get('titulo')
        descripcion = request.POST.get('descripcion')
        fecha_hora_str = request.POST.get('fecha_hora')
        estado = request.POST.get('estado', 'CREADO')

        if titulo and fecha_hora_str:
            fecha_hora = datetime.fromisoformat(fecha_hora_str)
            Task.objects.create(
                titulo=titulo,
                descripcion=descripcion,
                fecha_hora=fecha_hora,
                estado=estado
            )
            messages.success(request, 'Tarea creada exitosamente!')
            return redirect('lista_tareas')

    return render(request, 'tareas/crear_tarea.html')

def editar_tarea(request, pk):
    tarea = get_object_or_404(Task, pk=pk)

    if request.method == 'POST':
        tarea.titulo = request.POST.get('titulo')
        tarea.descripcion = request.POST.get('descripcion')
        fecha_hora_str = request.POST.get('fecha_hora')
        tarea.estado = request.POST.get('estado')

        if fecha_hora_str:
            tarea.fecha_hora = datetime.fromisoformat(fecha_hora_str)

        tarea.save()
        messages.success(request, 'Tarea actualizada exitosamente!')
        return redirect('lista_tareas')

    return render(request, 'tareas/editar_tarea.html', {'tarea': tarea})

def eliminar_tarea(request, pk):
    tarea = get_object_or_404(Task, pk=pk)

    if request.method == 'POST':
        tarea.delete()
        messages.success(request, 'Tarea eliminada exitosamente!')
        return redirect('lista_tareas')

    return render(request, 'tareas/eliminar_tarea.html', {'tarea': tarea})

def cambiar_estado(request, pk):
    tarea = get_object_or_404(Task, pk=pk)

    if request.method == 'POST':
        nuevo_estado = request.POST.get('estado')
        if nuevo_estado in ['CREADO', 'PENDIENTE', 'HECHO']:
            tarea.estado = nuevo_estado
            tarea.save()
            messages.success(request, f'Estado cambiado a {nuevo_estado}!')

    return redirect('lista_tareas')
