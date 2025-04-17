package UserService.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtility {
    public static LocalDateTime getCurrentDateTime() {
//      TODO : Will update this method with timezone support
        return LocalDateTime.now();
    }

    public static ZonedDateTime addDeltaToTime(LocalDateTime datetime, String delta, int deltaValue) {
        ZoneId zoneId = ZoneId.systemDefault(); // Or specify a timezone: ZoneId.of("Asia/Kolkata")
        ZonedDateTime zonedDateTime = datetime.atZone(zoneId);

        switch (delta.toLowerCase()) {
            case "second":
                return zonedDateTime.plusSeconds(deltaValue);
            case "minute":
                return zonedDateTime.plusMinutes(deltaValue);
            case "hour":
                return zonedDateTime.plusHours(deltaValue);
            case "day":
                return zonedDateTime.plusDays(deltaValue);
            case "week":
                return zonedDateTime.plusWeeks(deltaValue);
            case "month":
                return zonedDateTime.plusMonths(deltaValue);
            case "year":
                return zonedDateTime.plusYears(deltaValue);
            default:
                throw new IllegalArgumentException("Invalid delta unit: " + delta);
        }
    }
}
