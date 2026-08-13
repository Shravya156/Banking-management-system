package com.shravya.bankingapp.config;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DateUtil {
    public LocalDateTime[] getDateRange(String range, String start, String end) {
        LocalDateTime now = LocalDateTime.now();
        return switch (range.toLowerCase()) {
            case "today" -> new LocalDateTime[]{now.toLocalDate().atStartOfDay(), now};
            case "week" -> new LocalDateTime[]{now.minusDays(7), now};
            case "month" -> new LocalDateTime[]{now.withDayOfMonth(1).toLocalDate().atStartOfDay(), now};
            case "year" -> new LocalDateTime[]{now.withDayOfYear(1).toLocalDate().atStartOfDay(), now};
            case "custom" -> new LocalDateTime[]{LocalDateTime.parse(start), LocalDateTime.parse(end)};
            case "all" -> new LocalDateTime[]{
                    LocalDateTime.of(2000, 1, 1, 0, 0), // Start from a very old date
                    LocalDateTime.now().plusDays(1)      // Up to tomorrow
            };
            default -> throw new RuntimeException("Invalid range");
        };
    }
}