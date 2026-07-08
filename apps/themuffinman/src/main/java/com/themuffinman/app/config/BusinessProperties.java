package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.business")
public class BusinessProperties {

    private Booking booking = new Booking();
    private Dashboard dashboard = new Dashboard();
    private Lists lists = new Lists();
    private Calendar calendar = new Calendar();

    @Getter
    @Setter
    public static class Booking {
        private int defaultLeadTimeMinutes = 120;
        private int defaultMaxAdvanceDays = 60;
        private int defaultCustomerCancellationWindowMinutes = 1440;
    }

    @Getter
    @Setter
    public static class Dashboard {
        private int staleThresholdMinutes = 30;
    }

    @Getter
    @Setter
    public static class Lists {
        private int defaultPageSize = 20;
        private int maxPageSize = 100;
    }

    @Getter
    @Setter
    public static class Calendar {
        private int defaultProjectionDays = 7;
        private int maxProjectionDays = 31;
    }
}
