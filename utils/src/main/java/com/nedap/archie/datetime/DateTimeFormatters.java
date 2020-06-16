package com.nedap.archie.datetime;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DateTimeFormatters {

    public static final DateTimeFormatter ISO_8601_DATE = new DateTimeFormatterBuilder()
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

    public static final DateTimeFormatter ISO8601_TIME_ZONE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .optionalStart()
            .appendOffsetId()
            .optionalEnd()
            .optionalStart()
            .appendOffset("+HH:MM", "0000")
            .optionalEnd()
            .optionalStart()
            .appendOffset("+HHMM", "0000")
            .optionalEnd()
            .toFormatter();

    public static final DateTimeFormatter ISO8601_OPTIONAL_MICROSECONDS = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            // microseconds, decimal fraction, ISO 31-0: comma [,] or full stop [.]
            .optionalStart() //micro seconds ,
            .appendLiteral(',')
            .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, false)
            .optionalEnd() //micro seconds ,
            .optionalStart() //micro seconds .
            .appendFraction(ChronoField.MICRO_OF_SECOND, 1, 6, true)
            .optionalEnd() //micro seconds .
            .toFormatter();

    public static final DateTimeFormatter ISO_8601_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ChronoField.HOUR_OF_DAY)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .append(ISO8601_OPTIONAL_MICROSECONDS)
            .optionalEnd()
            .optionalEnd()
            .append(ISO8601_TIME_ZONE)
            .toFormatter();

    public static final DateTimeFormatter ISO_8601_DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_8601_DATE)
            .appendLiteral('T')
            .append(ISO_8601_TIME)
            .toFormatter();

    public static final DateTimeFormatter ISO_8601_DATE_COMPACT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ChronoField.YEAR, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .toFormatter();

    public static final DateTimeFormatter ISO_8601_TIME_COMPACT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .append(ISO8601_OPTIONAL_MICROSECONDS)
            .append(ISO8601_TIME_ZONE)
            .toFormatter();

    public static final DateTimeFormatter ISO_8601_DATE_TIME_COMPACT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_8601_DATE_COMPACT)
            .appendLiteral('T')
            .append(ISO_8601_TIME_COMPACT)
            .toFormatter();
}
