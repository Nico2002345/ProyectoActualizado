# Generated manually

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('envios', '0007_warehouse_shipment_origin_fields'),
    ]

    operations = [
        migrations.AddField(
            model_name='shipment',
            name='current_latitude',
            field=models.FloatField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='shipment',
            name='current_longitude',
            field=models.FloatField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='shipment',
            name='last_location_update',
            field=models.DateTimeField(blank=True, null=True),
        ),
    ]
