package com.savaari.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/driver")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired private JdbcTemplate jdbc;

    @PostMapping("/register")
    public Map<String, Object> registerDriver(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            int    userId      = ((Number) body.get("userId")).intValue();
            String cnic        = (String) body.get("cnic");
            String license     = (String) body.get("licenseNumber");
            String vehicleType = (String) body.getOrDefault("vehicleType", "Car");
            String make        = (String) body.getOrDefault("make", "Toyota");
            String model       = (String) body.getOrDefault("model", "Corolla");
            String plate       = (String) body.get("licensePlate");
            int    capacity    = vehicleType.equalsIgnoreCase("Bike") ? 1
                              : vehicleType.equalsIgnoreCase("Van")  ? 8 : 4;

            jdbc.update(
                "INSERT INTO Drivers (UserID, CNIC, LicenseNumber, ApprovalStatus) VALUES (?,?,?,'Pending')",
                userId, cnic, license);
            Integer driverId = jdbc.queryForObject(
                "SELECT DriverID FROM Drivers WHERE UserID = ?", Integer.class, userId);
            jdbc.update(
                "INSERT INTO Vehicles (DriverID, VehicleType, Make, Model, LicensePlate, Capacity) VALUES (?,?,?,?,?,?)",
                driverId, vehicleType, make, model, plate, capacity);

            res.put("success", true);
            res.put("driverId", driverId);
            res.put("message", "Driver registered! Pending admin approval.");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Driver registration failed: " + e.getMessage());
        }
        return res;
    }

    @PutMapping("/{driverId}/status")
    public Map<String, Object> toggleStatus(
        @PathVariable int driverId,
        @RequestBody Map<String, Object> body
    ) {
        Map<String, Object> res = new HashMap<>();
        boolean online = (Boolean) body.get("isOnline");
        jdbc.update("UPDATE Drivers SET IsOnline = ? WHERE DriverID = ?", online ? 1 : 0, driverId);
        res.put("success", true);
        res.put("isOnline", online);
        res.put("message", online ? "You are now online!" : "You are now offline.");
        return res;
    }

    @PostMapping("/rides/post")
    public Map<String, Object> postSharedRide(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            int    driverId    = ((Number) body.get("driverId")).intValue();
            String from        = (String) body.get("originCity");
            String to          = (String) body.get("destinationCity");
            String vehicleType = (String) body.getOrDefault("vehicleType", "Car");
            int    seats       = ((Number) body.getOrDefault("seats", 3)).intValue();
            double fare        = ((Number) body.get("totalFare")).doubleValue();

            jdbc.update(
                "INSERT INTO Rides (DriverID, RideType, VehicleType, OriginCity, DestinationCity, " +
                "DepartureTime, TotalFare, SeatsTotal, SeatsAvailable, Status) " +
                "VALUES (?, 'SHARED', ?, ?, ?, DATEADD(HOUR, 1, GETDATE()), ?, ?, ?, 'Posted')",
                driverId, vehicleType, from, to, fare, seats, seats);

            Integer rideId = jdbc.queryForObject(
                "SELECT MAX(RideID) FROM Rides WHERE DriverID = ?", Integer.class, driverId);

            res.put("success", true);
            res.put("rideId", rideId);
            res.put("message", "Shared ride posted! Passengers can now find it.");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Failed to post ride: " + e.getMessage());
        }
        return res;
    }

    @GetMapping("/{driverId}/earnings")
    public Map<String, Object> getEarnings(@PathVariable int driverId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Map<String, Object> driver = jdbc.queryForMap(
                "SELECT TotalEarnings, TotalRides, AvgRating FROM Drivers WHERE DriverID = ?", driverId);
            Double balance = jdbc.queryForObject(
                "SELECT w.Balance FROM Wallets w JOIN Drivers d ON d.UserID = w.UserID WHERE d.DriverID = ?",
                Double.class, driverId);
            List<Map<String, Object>> txns = jdbc.queryForList(
                "SELECT t.Type, t.Amount, t.Description, t.CreatedAt " +
                "FROM Transactions t " +
                "JOIN Wallets w ON w.WalletID = t.WalletID " +
                "JOIN Drivers d ON d.UserID = w.UserID " +
                "WHERE d.DriverID = ? ORDER BY t.CreatedAt DESC", driverId);
            res.put("success", true);
            res.put("totalEarnings", driver.get("TotalEarnings"));
            res.put("totalRides", driver.get("TotalRides"));
            res.put("avgRating", driver.get("AvgRating"));
            res.put("walletBalance", balance);
            res.put("transactions", txns);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @GetMapping("/{driverId}/rides")
    public List<Map<String, Object>> getDriverRides(@PathVariable int driverId) {
        return jdbc.queryForList(
            "SELECT r.RideID, r.OriginCity, r.DestinationCity, r.Status, " +
            "r.TotalFare, r.SeatsTotal, r.SeatsAvailable, r.VehicleType, r.CreatedAt " +
            "FROM Rides r WHERE r.DriverID = ? ORDER BY r.CreatedAt DESC", driverId);
    }
}