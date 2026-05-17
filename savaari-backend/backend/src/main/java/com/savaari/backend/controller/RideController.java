package com.savaari.backend.controller;

import com.savaari.backend.service.FareCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/rides")
@CrossOrigin(origins = "*")
public class RideController {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private FareCalculator fareCalc;

    // Health check
    @GetMapping("/test")
    public String test() {
        return "SavaariPK Backend is running!";
    }

    // Returns all fare configs from DB — Chinchi won't appear since you deleted it from DB
    @GetMapping("/fares")
    public List<Map<String, Object>> getFares() {
        return jdbc.queryForList("SELECT * FROM FareConfig ORDER BY VehicleType");
    }

    @GetMapping("/all")
    public List<Map<String, Object>> getAllRides() {
        return jdbc.queryForList(
            "SELECT r.RideID, r.RideType, r.VehicleType, r.OriginCity, r.DestinationCity, " +
            "r.TotalFare, r.SeatsAvailable, r.Status, r.CreatedAt, " +
            "u.FullName AS driverName " +
            "FROM Rides r LEFT JOIN Drivers d ON d.DriverID = r.DriverID " +
            "LEFT JOIN Users u ON u.UserID = d.UserID " +
            "ORDER BY r.CreatedAt DESC");
    }

    @PostMapping("/cancel/{id}")
    public Map<String, Object> cancelRide(@PathVariable int id) {
        Map<String, Object> res = new HashMap<>();
        jdbc.update("UPDATE Rides SET Status = 'Cancelled' WHERE RideID = ?", id);
        res.put("success", true);
        res.put("message", "Ride " + id + " cancelled");
        return res;
    }
}