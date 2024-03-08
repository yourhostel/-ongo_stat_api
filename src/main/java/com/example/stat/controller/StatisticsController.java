package com.example.stat.controller;

import com.example.stat.service.SalesAndTrafficService;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final SalesAndTrafficService service;

    @GetMapping("/by-date")
    public ResponseEntity<List<Document>> getByDateRange(@RequestParam("start")
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                         @RequestParam("end")
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.findByDateRange(start, end));
    }

    @GetMapping("/by-asin")
    public ResponseEntity<List<Document>> getByAsin(@RequestParam List<String> asins) {
        return ResponseEntity.ok(service.findByAsin(asins));
    }

    @GetMapping("/total-by-dates")
    public ResponseEntity<Document> getTotalStatisticsByDates() {
        Document data = service.findTotalStatisticsByDates();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/total-by-asins")
    public ResponseEntity<Document> getTotalStatisticsByAsins() {
        Document data = service.findTotalStatisticsByAsins();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/ordered-and-amount")
    public ResponseEntity<Document> getUnitsOrderedAndAmountTotal() {
        return ResponseEntity.ok(service.findUnitsOrderedAndAmountTotal());
    }

}

