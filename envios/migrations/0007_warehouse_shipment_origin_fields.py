# Generated manually

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('envios', '0006_notification'),
    ]

    operations = [
        # Crear modelo Warehouse
        migrations.CreateModel(
            name='Warehouse',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=255)),
                ('address', models.TextField()),
                ('city', models.CharField(max_length=100)),
                ('latitude', models.FloatField()),
                ('longitude', models.FloatField()),
                ('is_main', models.BooleanField(default=False)),
                ('active', models.BooleanField(default=True)),
                ('created_at', models.DateTimeField(auto_now_add=True)),
            ],
        ),
        # Agregar campos de dirección de origen a Shipment
        migrations.AddField(
            model_name='shipment',
            name='origin_address',
            field=models.TextField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='shipment',
            name='origin_latitude',
            field=models.FloatField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='shipment',
            name='origin_longitude',
            field=models.FloatField(blank=True, null=True),
        ),
        # Renombrar campos de destino para mayor claridad
        migrations.AddField(
            model_name='shipment',
            name='receiver_latitude',
            field=models.FloatField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='shipment',
            name='receiver_longitude',
            field=models.FloatField(blank=True, null=True),
        ),
        # Agregar relación de Shipment a Warehouse
        migrations.AddField(
            model_name='shipment',
            name='warehouse',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, related_name='shipments', to='envios.warehouse'),
        ),
    ]
