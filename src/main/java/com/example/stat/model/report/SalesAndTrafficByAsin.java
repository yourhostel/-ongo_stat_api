package com.example.stat.model.report;

import lombok.Data;

import java.util.List;

@Data
public class SalesAndTrafficByAsin {
    private String parentAsin;
    private SalesByAsin salesByAsin;
    private TrafficByAsin trafficByAsin;
}
