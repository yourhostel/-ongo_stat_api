package com.example.stat.service;

import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@AllArgsConstructor
public class SalesAndTrafficService {

    private static final String SALES_BY_ASIN = "salesAndTrafficByAsin.salesByAsin";

    private static final String TRAFFIC_BY_ASIN = "salesAndTrafficByAsin.trafficByAsin";

    private static final String SALES_BY_DATE = "salesAndTrafficByDate.salesByDate";

    private static final String TRAFFIC_BY_DATE = "salesAndTrafficByDate.trafficByDate";

    private final MongoTemplate mongoTemplate;

    public List<Document> findByDateRange(LocalDate startDate, LocalDate endDate) {
        Aggregation aggregation = newAggregation(
                unwind("salesAndTrafficByDate"),
                match(Criteria.where("salesAndTrafficByDate.date")
                        .gte(startDate.toString())
                        .lte(endDate.toString())),
                project("salesAndTrafficByDate")
                        .andExpression("{'$toDate':'$salesAndTrafficByDate.date'}")
                        .as("dateConverted"),
                sort(Sort.by(Sort.Direction.DESC, "dateConverted")),
                replaceRoot("salesAndTrafficByDate")
        );

        AggregationResults<Document> results = mongoTemplate
                .aggregate(aggregation, "report", Document.class);
        return results.getMappedResults();
    }

    public List<Document> findByAsin(List<String> asins) {
        Aggregation aggregation = newAggregation(
                unwind("$salesAndTrafficByAsin"),
                match(Criteria.where("salesAndTrafficByAsin.parentAsin").in(asins)),
                group("$salesAndTrafficByAsin.parentAsin")
                        .first("salesAndTrafficByAsin")
                        .as("salesAndTrafficByAsin"),
                replaceRoot("$salesAndTrafficByAsin")
        );

        return mongoTemplate
                .aggregate(aggregation, "report", Document.class)
                .getMappedResults();
    }

    public Document findUnitsOrderedAndAmountTotal() {
        Aggregation aggregation = newAggregation(
                unwind("$salesAndTrafficByAsin"),
                group()
                        .sum(SALES_BY_ASIN + ".unitsOrdered")
                        .as("totalUnitsOrdered")
                        .sum(SALES_BY_ASIN + ".orderedProductSales.amount")
                        .as("totalSalesAmount"),
                project().andExclude("_id")
        );

        AggregationResults<Document> results = mongoTemplate
                .aggregate(aggregation, "report", Document.class);

        return Optional.ofNullable(results.getUniqueMappedResult())
                .orElseGet(() -> {
                    Document doc = new Document();
                    doc.put("totalSalesAmount", 0);
                    doc.put("totalUnitsOrdered", 0);
                    return doc;
                });
    }

    public Document findTotalStatisticsByDates() {
        Aggregation aggregation = newAggregation(
                unwind("$salesAndTrafficByDate"),
                group()
                        .sum(SALES_BY_DATE + ".unitsOrdered").as("totalUnitsOrdered")
                        .sum(SALES_BY_DATE + ".orderedProductSales.amount").as("totalSalesAmount")
                        .sum(TRAFFIC_BY_DATE + ".sessions").as("totalSessions")
                        .sum(TRAFFIC_BY_DATE + ".pageViews").as("totalPageViews"),
                project().andExclude("_id")
        );

        AggregationResults<Document> results = mongoTemplate
                .aggregate(aggregation, "report", Document.class);

        return Optional.ofNullable(results.getUniqueMappedResult()).orElseGet(() -> {
            Document doc = new Document();
            doc.put("totalUnitsOrdered", 0);
            doc.put("totalSalesAmount", 0);
            doc.put("totalSessions", 0);
            doc.put("totalPageViews", 0);
            return doc;
        });
    }

    public Document findTotalStatisticsByAsins() {
        Aggregation aggregation = newAggregation(
                unwind("$salesAndTrafficByAsin"),
                group()
                        .sum(SALES_BY_ASIN + ".unitsOrdered").as("totalUnitsOrdered")
                        .sum(SALES_BY_ASIN + ".orderedProductSales.amount").as("totalSalesAmount")
                        .sum(TRAFFIC_BY_ASIN + ".sessions").as("totalSessions")
                        .sum(TRAFFIC_BY_ASIN + ".pageViews").as("totalPageViews"),
                project().andExclude("_id")
        );

        AggregationResults<Document> results = mongoTemplate
                .aggregate(aggregation, "report", Document.class);

        return Optional.ofNullable(results.getUniqueMappedResult()).orElseGet(() -> {
            Document doc = new Document();
            doc.put("totalUnitsOrdered", 0);
            doc.put("totalSalesAmount", 0);
            doc.put("totalSessions", 0);
            doc.put("totalPageViews", 0);
            return doc;
        });
    }

}

