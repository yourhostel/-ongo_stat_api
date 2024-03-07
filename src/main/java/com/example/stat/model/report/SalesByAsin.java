package com.example.stat.model.report;

import lombok.Data;

@Data
public class SalesByAsin {
    private Integer unitsOrdered;
    private Integer unitsOrderedB2B;
    private MonetaryAmount orderedProductSales;
    private MonetaryAmount orderedProductSalesB2B;
    private Integer totalOrderItems;
    private Integer totalOrderItemsB2B;
}
