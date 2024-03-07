package com.example.stat.model.report;

import lombok.Data;

@Data
public class TrafficByAsin {
    private Integer browserSessions;
    private Integer browserSessionsB2B;
    private Integer mobileAppSessions;
    private Integer mobileAppSessionsB2B;
    private Integer sessions;
    private Integer sessionsB2B;
    private Double browserSessionPercentage;
    private Double browserSessionPercentageB2B;
    private Double mobileAppSessionPercentage;
    private Double mobileAppSessionPercentageB2B;
    private Double sessionPercentage;
    private Double sessionPercentageB2B;
    private Integer browserPageViews;
    private Integer browserPageViewsB2B;
    private Integer mobileAppPageViews;
    private Integer mobileAppPageViewsB2B;
    private Integer pageViews;
    private Integer pageViewsB2B;
    private Double browserPageViewsPercentage;
    private Double browserPageViewsPercentageB2B;
    private Double mobileAppPageViewsPercentage;
    private Double mobileAppPageViewsPercentageB2B;
    private Double pageViewsPercentage;
    private Double pageViewsPercentageB2B;
    private Double buyBoxPercentage;
    private Double buyBoxPercentageB2B;
    private Double unitSessionPercentage;
    private Double unitSessionPercentageB2B;

}
