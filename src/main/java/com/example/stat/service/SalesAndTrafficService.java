package com.example.stat.service;

import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.cache.annotation.Cacheable;
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

    /**
     * @param startDate
     * @param endDate
     * @return
     * Кешуємо за діапазоном дат. Ключ кеша включає рядки обох дат,
     * щоб результати були унікальні для кожного діапазону.
     */
    @Cacheable(value = "findByDateRangeCache", key = "#startDate.toString() + #endDate.toString()")
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

    /**
     * @param asins
     * @return
     * Кешуємо за списком ASIN, тому що запити по одному
     * або декільком ASIN можуть бути повторними.
     * Ключ кешу заснований на списках ASIN, об'єднаних у рядок,
     * щоб кожен унікальний набір ASIN мав свій результат у кеші.
     */
    @Cacheable(value = "findByAsinCache", key = "#asins.toString()")
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

    /**
     * @return
     * Кешуємо загальну статистику за замовленими одиницями та сумою продажів.
     * Тут можна використовувати фіксований ключ,
     * оскільки метод не набирає параметрів.
     */
    @Cacheable(value = "totalUnitsAndSalesCache")
    public Document findUnitsOrderedAndAmountTotal() {

//          Штучна затримка в 1 секунду (1000 мілісекунд)
//          Для помітної затримки запитів до бази даних щоб побачити різницю роботи
//          при закоментованій інструкції Cacheable (без кешування) при навантажувальном тестуванні
//          наприклад у Postman Runner.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

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

        return Optional.ofNullable(results.getUniqueMappedResult()).orElseGet(() -> {
                    Document doc = new Document();
                    doc.put("totalSalesAmount", 0);
                    doc.put("totalUnitsOrdered", 0);
                    return doc;
                });
    }

    /**
     * @return
     * Кешуємо загальну статистику за датами. Використовуємо фіксований ключ.
     */
    @Cacheable(value = "totalStatsByDatesCache")
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

    /**
     * @return
     * Кешуємо загальну статистику з ASIN. Використовуємо фіксований ключ.
     */
    @Cacheable(value = "totalStatsByAsinsCache")
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

