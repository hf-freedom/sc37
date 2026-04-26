package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.model.MonthlyReport;
import com.example.fleetmanagement.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public List<MonthlyReport> getAllMonthlyReports() {
        return reportService.getAllMonthlyReports();
    }

    @PostMapping("/generate")
    public MonthlyReport generateMonthlyReport(@RequestBody Map<String, Object> request) {
        int year = Integer.parseInt(request.get("year").toString());
        int month = Integer.parseInt(request.get("month").toString());
        YearMonth yearMonth = YearMonth.of(year, month);
        return reportService.generateMonthlyReport(yearMonth);
    }

    @GetMapping("/current")
    public MonthlyReport getCurrentMonthReport() {
        YearMonth currentMonth = YearMonth.now();
        return reportService.generateMonthlyReport(currentMonth);
    }
}
