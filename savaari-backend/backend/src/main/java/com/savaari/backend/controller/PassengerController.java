package com.savaari.backend.controller;

import com.savaari.backend.service.FareCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/passenger")
@CrossOrigin(origins = "*")
public class PassengerController {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private FareCalculator fareCalc;

    @GetMapping("/search")
    public List<Map<String, Object>> searchRides(
        @RequestParam(defaultValue = "") String from,
        @RequestParam(defaultValue = "") String to,
        @RequestParam(defaultValue = "shared") String type,
        @RequestParam(defaultValue = "Car") String vehicleType
    ) {
        if ("solo".equalsIgnoreCase(type)) {
            return jdbc.queryForList(
                "SELECT d.DriverID, u.FullName AS driverName, u.Phone, " +
                "d.AvgRating, v.VehicleType, v.Make, v.Model, v.LicensePlate, " +
                "fc.BaseFare, fc.PerKmRate " +
                "FROM Drivers d " +
                "JOIN Users u ON u.UserID = d.UserID " +
                "JOIN Vehicles v ON v.DriverID = d.DriverID " +
                "JOIN FareConfig fc ON fc.VehicleType = v.VehicleType " +
                "WHERE d.IsOnline = 1 AND d.ApprovalStatus = 'Approved' " +
                "AND v.VehicleType = ?", vehicleType);
        } else {
            String fromFilter = "%" + from + "%";
            String toFilter   = "%" + to + "%";
            return jdbc.queryForList(
                "SELECT r.RideID, r.OriginCity, r.DestinationCity, r.DepartureTime, " +
                "r.TotalFare, r.SeatsAvailable, r.VehicleType, " +
                "u.FullName AS driverName, d.AvgRating " +
                "FROM Rides r " +
                "JOIN Drivers d ON d.DriverID = r.DriverID " +
                "JOIN Users u ON u.UserID = d.UserID " +
                "WHERE r.Status = 'Posted' AND r.SeatsAvailable > 0 " +
                "AND r.OriginCity LIKE ? AND r.DestinationCity LIKE ?",
                fromFilter, toFilter);
        }
    }

    @PostMapping("/book")
    public Map<String, Object> bookRide(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            int    passengerId = ((Number) body.get("passengerId")).intValue();
            int    rideId      = ((Number) body.get("rideId")).intValue();
            double fare        = ((Number) body.get("fare")).doubleValue();

            Double balance = jdbc.queryForObject(
                "SELECT Balance FROM Wallets WHERE UserID = ?", Double.class, passengerId);
            if (balance == null || balance < fare) {
                res.put("success", false);
                res.put("message", "Insufficient wallet balance. Please top up.");
                return res;
            }

            jdbc.update(
                "UPDATE Wallets SET Balance = Balance - ?, UpdatedAt = GETDATE() WHERE UserID = ?",
                fare, passengerId);
            Integer walletId = jdbc.queryForObject(
                "SELECT WalletID FROM Wallets WHERE UserID = ?", Integer.class, passengerId);
            jdbc.update(
                "INSERT INTO Transactions (WalletID, Type, Amount, Description) VALUES (?,?,?,?)",
                walletId, "RIDE", fare, "Ride payment - RideID " + rideId);

            jdbc.update(
                "INSERT INTO Bookings (RideID, PassengerID, FareShare, Status) VALUES (?,?,?,'Confirmed')",
                rideId, passengerId, fare);
            jdbc.update(
                "UPDATE Rides SET SeatsAvailable = SeatsAvailable - 1 WHERE RideID = ?", rideId);

            Integer bookingId = jdbc.queryForObject(
                "SELECT MAX(BookingID) FROM Bookings WHERE PassengerID = ?", Integer.class, passengerId);

            res.put("success", true);
            res.put("bookingId", bookingId);
            res.put("newBalance", balance - fare);
            res.put("message", "Ride booked successfully!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Booking failed: " + e.getMessage());
        }
        return res;
    }

    @GetMapping("/wallet/{userId}")
    public Map<String, Object> getWallet(@PathVariable int userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Double balance = jdbc.queryForObject(
                "SELECT Balance FROM Wallets WHERE UserID = ?", Double.class, userId);
            List<Map<String, Object>> txns = jdbc.queryForList(
                "SELECT t.Type, t.Amount, t.Description, t.CreatedAt " +
                "FROM Transactions t JOIN Wallets w ON w.WalletID = t.WalletID " +
                "WHERE w.UserID = ? ORDER BY t.CreatedAt DESC", userId);
            res.put("success", true);
            res.put("balance", balance != null ? balance : 0);
            res.put("transactions", txns);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/wallet/topup")
    public Map<String, Object> topUp(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            int    userId = ((Number) body.get("userId")).intValue();
            double amount = ((Number) body.get("amount")).doubleValue();

            jdbc.update(
                "UPDATE Wallets SET Balance = Balance + ?, UpdatedAt = GETDATE() WHERE UserID = ?",
                amount, userId);
            Integer walletId = jdbc.queryForObject(
                "SELECT WalletID FROM Wallets WHERE UserID = ?", Integer.class, userId);
            jdbc.update(
                "INSERT INTO Transactions (WalletID, Type, Amount, Description) VALUES (?,?,?,?)",
                walletId, "TOPUP", amount, "Wallet top-up via JazzCash/EasyPaisa");

            Double newBalance = jdbc.queryForObject(
                "SELECT Balance FROM Wallets WHERE UserID = ?", Double.class, userId);
            res.put("success", true);
            res.put("newBalance", newBalance);
            res.put("message", "Wallet topped up successfully!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Top-up failed: " + e.getMessage());
        }
        return res;
    }

    @GetMapping("/history/{userId}")
    public List<Map<String, Object>> getHistory(@PathVariable int userId) {
        return jdbc.queryForList(
            "SELECT b.BookingID, b.FareShare, b.Status, b.BookedAt, " +
            "r.OriginCity, r.DestinationCity, r.VehicleType, r.RideType " +
            "FROM Bookings b JOIN Rides r ON r.RideID = b.RideID " +
            "WHERE b.PassengerID = ? ORDER BY b.BookedAt DESC", userId);
    }

    @GetMapping("/fare")
    public Map<String, Object> calculateFare(
        @RequestParam String vehicleType,
        @RequestParam double distanceKm,
        @RequestParam(defaultValue = "1") int passengers
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            double total     = fareCalc.calculate(vehicleType, distanceKm);
            double perPerson = passengers > 1 ? fareCalc.sharedFare(vehicleType, distanceKm, passengers) : total;
            res.put("success", true);
            res.put("totalFare", Math.round(total));
            res.put("farePerPerson", Math.round(perPerson));
            res.put("vehicleType", vehicleType);
            res.put("distanceKm", distanceKm);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }
}