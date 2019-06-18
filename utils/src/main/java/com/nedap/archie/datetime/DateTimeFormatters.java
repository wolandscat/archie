package com.nedap.archie.datetime;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DateTimeFormatters {
    public static final DateTimeFormatter ISO_8601_DATE_TIME = withISO8601TimeZone(new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ChronoField.YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalEnd()
            .optionalEnd())
            .toFormatter();

    public static final DateTimeFormatter ISO_8601_DATE_TIME_WITH_OPTIONAL_MICROS = withISO8601TimeZone(new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ChronoField.YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .optionalStart() //minutes
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart() //seconds
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            // microseconds, decimal fraction, ISO 31-0: comma [,] or full stop [.]
            .optionalStart() //micro seconds ,
            .appendLiteral(',')
            .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, false)
            .optionalEnd() //micro seconds ,
            .optionalStart() //micro seconds .
            .appendFraction(ChronoField.MICRO_OF_SECOND, 1, 6, true)
            .optionalEnd() //micro seconds .
            .optionalEnd() //seconds
            .optionalEnd() //minutes
            ).toFormatter();


    public static final DateTimeFormatter ISO_8601_DATE_TIME_WITHOUT_MICROS = withISO8601TimeZone(new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ChronoField.YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalEnd()
            .optionalEnd())
            .toFormatter();


    public static final DateTimeFormatter ISO_8601_TIME;
    static {
        ISO_8601_TIME = withISO8601TimeZone(new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .optionalStart()
                .appendLiteral(',')
                .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, false)
                .optionalEnd()
                .optionalEnd()
                .optionalEnd())
                .toFormatter();
    }

    public static final DateTimeFormatter ISO_8601_DATE;
    static {
        ISO_8601_DATE = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(ChronoField.YEAR)
                .optionalStart()
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .optionalStart()
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH)
                .optionalEnd()
                .optionalEnd()
                .toFormatter();
    }

    private static DateTimeFormatterBuilder withISO8601TimeZone(DateTimeFormatterBuilder builder){
        return builder            //time zone stuff
            .optionalStart()
                .appendOffsetId()
                .optionalEnd()
                .optionalStart()
                .appendOffset("+HH:MM", "0000")
                .optionalEnd()
                .optionalStart()
                .appendOffset("+HHMM", "0000")
                .optionalEnd();
        //--end of time zone stuff

    }
}
