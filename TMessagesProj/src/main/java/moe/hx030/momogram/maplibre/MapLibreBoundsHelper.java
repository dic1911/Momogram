package moe.hx030.momogram.maplibre;

import androidx.annotation.NonNull;

import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public final class MapLibreBoundsHelper {
    private MapLibreBoundsHelper() {
    }

    public static boolean fitPoints(@NonNull MapLibreView mapView, @NonNull List<LatLng> points, boolean animated, int padding, double maxZoom, long durationMs, double minSpanMeters) {
        if (points.size() < 2) {
            return false;
        }

        ArrayList<LatLng> adjustedPoints = new ArrayList<>(points);
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (int i = 0, n = points.size(); i < n; i++) {
            LatLng point = points.get(i);
            double latitude = point.getLatitude();
            double longitude = point.getLongitude();
            if (latitude < minLat) minLat = latitude;
            if (latitude > maxLat) maxLat = latitude;
            if (longitude < minLon) minLon = longitude;
            if (longitude > maxLon) maxLon = longitude;
        }

        LatLng center = new LatLng((minLat + maxLat) / 2.0d, (minLon + maxLon) / 2.0d);
        double latSpanMeters = Math.toRadians(maxLat - minLat) * 6366198.0d;
        double lonSpanMeters = Math.toRadians(maxLon - minLon) * 6366198.0d * Math.cos(Math.toRadians(center.getLatitude()));
        if (latSpanMeters < minSpanMeters || lonSpanMeters < minSpanMeters) {
            adjustedPoints.add(GeoUtils.move(center, minSpanMeters / 2.0d, minSpanMeters / 2.0d));
            adjustedPoints.add(GeoUtils.move(center, -minSpanMeters / 2.0d, -minSpanMeters / 2.0d));
        }

        LatLngBounds bounds = GeoUtils.bounds(adjustedPoints);
        if (animated) {
            mapView.fitBounds(bounds, true, padding, maxZoom, durationMs);
        } else {
            mapView.fitBounds(bounds, false, padding);
        }
        return true;
    }
}
