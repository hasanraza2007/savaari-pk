package com.savaari.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*")
public class RatingController {

    @Autowired private JdbcTemplate jdbc;

    @PostMapping
    public Map<String, Object> submitRating(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            int    bookingId  = ((Number) body.get("bookingId")).intValue();
            int    fromUserId = ((Number) body.get("fromUserId")).intValue();
            int    toUserId   = ((Number) body.get("toUserId")).intValue();
            int    stars      = ((Number) body.get("stars")).intValue();
            String comment    = (String) body.getOrDefault("comment", "");

            jdbc.update(
                "INSERT INTO Ratings (BookingID, FromUserID, ToUserID, Stars, Comment) VALUES (?,?,?,?,?)",
                bookingId, fromUserId, toUserId, stars, comment);
            jdbc.update(
                "UPDATE Drivers SET AvgRating = (" +
                "  SELECT AVG(CAST(Stars AS FLOAT)) FROM Ratings WHERE ToUserID = ?" +
                ") WHERE UserID = ?",
                toUserId, toUserId);

            res.put("success", true);
            res.put("message", "Thank you for your rating!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Rating failed: " + e.getMessage());
        }
        return res;
    }

    @GetMapping("/driver/{driverId}")
    public List<Map<String, Object>> getDriverRatings(@PathVariable int driverId) {
        return jdbc.queryForList(
            "SELECT r.Stars, r.Comment, r.RatedAt, u.FullName AS fromName " +
            "FROM Ratings r " +
            "JOIN Users u ON u.UserID = r.FromUserID " +
            "JOIN Drivers d ON d.UserID = r.ToUserID " +
            "WHERE d.DriverID = ? ORDER BY r.RatedAt DESC", driverId);
    }
}