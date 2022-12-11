package pt.ulisboa.tecnico.cross.utils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {
  public static String getFirstDayOfWeekOffset(Instant date, int offsetDays) {
    return LocalDate.ofInstant(date, ZoneId.systemDefault())
        .with(DayOfWeek.MONDAY)
        .minusDays(offsetDays)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }
}
