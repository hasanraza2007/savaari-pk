package com.savaari.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private BCryptPasswordEncoder encoder;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            String phone    = (String) body.get("phone");
            String name     = (String) body.get("fullName");
            String email    = (String) body.getOrDefault("email", "");
            String password = (String) body.get("password");
            String role     = ((String) body.getOrDefault("role", "PASSENGER")).toUpperCase();

            int count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Users WHERE Phone = ?", Integer.class, phone);
            if (count > 0) {
                res.put("success", false);
                res.put("message", "Phone number already registered. Please login.");
                return res;
            }

            String hash = password;
            jdbc.update(
                "INSERT INTO Users (FullName, Phone, Email, PasswordHash, Role) VALUES (?,?,?,?,?)",
                name, phone, email, hash, role);

            Integer userId = jdbc.queryForObject(
                "SELECT UserID FROM Users WHERE Phone = ?", Integer.class, phone);
            jdbc.update("INSERT INTO Wallets (UserID, Balance) VALUES (?, 0)", userId);

            res.put("success", true);
            res.put("userId", userId);
            res.put("message", "Account created successfully!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Registration failed: " + e.getMessage());
        }
        return res;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            String phone    = (String) body.get("phone");
            String password = (String) body.get("password");

            List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT u.UserID, u.FullName, u.Phone, u.Email, u.PasswordHash, u.Role, u.Status, " +
                "ISNULL(w.Balance, 0) AS Balance " +
                "FROM Users u LEFT JOIN Wallets w ON w.UserID = u.UserID " +
                "WHERE u.Phone = ?", phone);

            if (rows.isEmpty()) {
                res.put("success", false);
                res.put("message", "Phone number not found. Please register first.");
                return res;
            }

            Map<String, Object> user = rows.get(0);
            String storedHash = (String) user.get("PasswordHash");

            if (!password.equals(storedHash)) {
                res.put("success", false);
                res.put("message", "Wrong password. Try again.");
                return res;
            }

            if ("Suspended".equals(user.get("Status"))) {
                res.put("success", false);
                res.put("message", "Your account has been suspended.");
                return res;
            }

            String token = "savaari_" + user.get("UserID") + "_" + user.get("Role") + "_" + System.currentTimeMillis();

            Integer driverId = null;
            String role = (String) user.get("Role");
            if ("DRIVER".equals(role)) {
                List<Map<String, Object>> driverRows = jdbc.queryForList(
                    "SELECT DriverID FROM Drivers WHERE UserID = ?", user.get("UserID"));
                if (!driverRows.isEmpty()) {
                    driverId = ((Number) driverRows.get(0).get("DriverID")).intValue();
                }
            }

            res.put("success", true);
            res.put("token", token);
            res.put("userId", user.get("UserID"));
            res.put("driverId", driverId);
            res.put("name", user.get("FullName"));
            res.put("phone", user.get("Phone"));
            res.put("email", user.get("Email"));
            res.put("role", role.toLowerCase());
            res.put("walletBalance", user.get("Balance"));
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Login error: " + e.getMessage());
        }
        return res;
    }
}