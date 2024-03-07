package com.example.stat.model.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class SalesReport {
    @JsonProperty("reportSpecification")
    private ReportSpecification reportSpecification;

    @JsonProperty("salesAndTrafficByDate")
    private List<SalesAndTrafficByDate> salesAndTrafficByDate;

    @JsonProperty("salesAndTrafficByAsin")
    private List<SalesAndTrafficByAsin> salesAndTrafficByAsin;

}
