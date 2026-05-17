package com.savaari.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class FareCalculator {

    @Autowired
    private JdbcTemplate jdbc;

    public double calculate(String vehicleType, double distanceKm) {
        try {
            Map<String, Object> config = jdbc.queryForMap(
                "SELECT BaseFare, PerKmRate FROM FareConfig WHERE VehicleType = ?", vehicleType);
            double base  = ((Number) config.get("BaseFare")).doubleValue();
            double perKm = ((Number) config.get("PerKmRate")).doubleValue();
            return base + (perKm * distanceKm);
        } catch (Exception e) {
            // Fallback to Car rates if vehicleType not found in DB
            return 150 + (45 * distanceKm);
        }
    }

    public double sharedFare(String vehicleType, double distanceKm, int passengers) {
        double total = calculate(vehicleType, distanceKm);
        return total / Math.max(1, passengers);
    }
}