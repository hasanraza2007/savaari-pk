package com.savaari.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired private JdbcTemplate jdbc;

    // GET /api/admin/dashboard
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> res = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList("EXEC sp_GetAdminDashboard");
            if (!rows.isEmpty()) {
                res.putAll(rows.get(0));
            }
            res.put("success", true);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    // GET /api/admin/drivers/pending
    @GetMapping("/drivers/pending")
    public List<Map<String, Object>> getPendingDrivers() {
        return jdbc.queryForList(
            "SELECT d.DriverID, u.FullName, u.Phone, u.Email, d.CNIC, d.LicenseNumber, " +
            "d.ApprovalStatus, d.AvgRating, v.VehicleType, v.Make, v.Model, v.LicensePlate " +
            "FROM Drivers d " +
            "JOIN Users u ON u.UserID = d.UserID " +
            "LEFT JOIN Vehicles v ON v.DriverID = d.DriverID " +
            "WHERE d.ApprovalStatus = 'Pending' " +
            "ORDER BY d.DriverID DESC");
    }

    // GET /api/admin/drivers/all
    @GetMapping("/drivers/all")
    public List<Map<String, Object>> getAllDrivers() {
        return jdbc.queryForList(
            "SELECT d.DriverID, u.FullName, u.Phone, d.CNIC, d.LicenseNumber, " +
            "d.ApprovalStatus, d.IsOnline, d.AvgRating, d.TotalRides, d.TotalEarnings " +
            "FROM Drivers d JOIN Users u ON u.UserID = d.UserID " +
            "ORDER BY d.DriverID DESC");
    }

    // POST /api/admin/driver/approve
    @PostMapping("/driver/approve")
    public Map<String, Object> approveDriver(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            int    driverId = ((Number) body.get("driverId")).intValue();
            String decision = (String) body.get("decision"); // "Approved" or "Rejected"
            jdbc.update("EXEC sp_ApproveDriver ?, ?", driverId, decision);
            res.put("success", true);
            res.put("message", "Driver " + decision);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    // GET /api/admin/fares  — reads from DB (Chinchi is already removed from DB, so it won't show)
    @GetMapping("/fares")
    public List<Map<String, Object>> getFares() {
        return jdbc.queryForList("SELECT * FROM FareConfig ORDER BY VehicleType");
    }

    // POST /api/admin/fares
    @PostMapping("/fares")
    public Map<String, Object> updateFare(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            String vehicleType = (String) body.get("vehicleType");
            double baseFare    = ((Number) body.get("baseFare")).doubleValue();
            double perKmRate   = ((Number) body.get("perKmRate")).doubleValue();
            jdbc.update(
                "UPDATE FareConfig SET BaseFare = ?, PerKmRate = ? WHERE VehicleType = ?",
                baseFare, perKmRate, vehicleType);
            res.put("success", true);
            res.put("message", "Fare updated for " + vehicleType);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    // GET /api/admin/users
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return jdbc.queryForList(
            "SELECT u.UserID, u.FullName, u.Phone, u.Email, u.Role, u.Status, u.CreatedAt, " +
            "ISNULL(w.Balance, 0) AS WalletBalance " +
            "FROM Users u LEFT JOIN Wallets w ON w.UserID = u.UserID " +
            "ORDER BY u.UserID DESC");
    }

    // PUT /api/admin/user/{userId}/status
    @PutMapping("/user/{userId}/status")
    public Map<String, Object> updateUserStatus(
        @PathVariable int userId,
        @RequestBody Map<String, Object> body
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            String status = (String) body.get("status");
            jdbc.update("UPDATE Users SET Status = ? WHERE UserID = ?", status, userId);
            res.put("success", true);
            res.put("message", "User status updated to " + status);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    // POST /api/admin/wallet/topup
    @PostMapping("/wallet/topup")
    public Map<String, Object> adminTopUp(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            int    userId = ((Number) body.get("userId")).intValue();
            double amount = ((Number) body.get("amount")).doubleValue();
            jdbc.update("EXEC sp_TopUpWallet ?, ?", userId, amount);
            Double balance = jdbc.queryForObject(
                "SELECT Balance FROM Wallets WHERE UserID = ?", Double.class, userId);
            res.put("success", true);
            res.put("newBalance", balance);
            res.put("message", "Wallet topped up successfully!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }
}