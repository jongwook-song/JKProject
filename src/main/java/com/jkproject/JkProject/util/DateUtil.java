package com.jkproject.JkProject.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DateUtil {
    public static Locale s_DefaultLocale = Locale.getDefault();

    public static final String DATE_TO_CONDITION_SUFFIX = " 23:59:59";
    public static final long DATE_TO_CONDITION_SUFFIX_MILLISECONDS = (1000 * 86399); // "23:59:59"
    public static final long DATE_TO_CONDITION_ONE_DAY_MILLSECONDS = (1000 * 86400); // "1day"
    public static final String DATE_FROM_COMMON_MIN_VALUE = "1900-01-01";
    public static final String DATE_TO_COMMON_MAX_VALUE = "2286-01-01";

    public static final Timestamp DATE_TO_MIN_TIMESTAMP = new Timestamp(0);
    public static final Timestamp DATE_TO_MAX_TIMESTAMP = Timestamp.valueOf("9999-12-31" + DATE_TO_CONDITION_SUFFIX);

    public static final SimpleDateFormat SDF_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final SimpleDateFormat SDF_ISO8601_NO_MARK = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    public static final SimpleDateFormat SDF_ISO8601_WINDOWS = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
    public static final SimpleDateFormat SDF_ISO8601_MS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final SimpleDateFormat SDF_YYYY = new SimpleDateFormat("yyyy");
    public static final SimpleDateFormat SDF_YYYYMM = new SimpleDateFormat("yyyyMM");
    public static final SimpleDateFormat SDF_YYYYMM_DASH = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat SDF_YYYYMM_DOT = new SimpleDateFormat("yyyy.MM");
    public static final SimpleDateFormat SDF_YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat SDF_YYYYMMDDHH = new SimpleDateFormat("yyyyMMddHH");
    public static final SimpleDateFormat SDF_YYYYMMDD_DASH = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat SDF_YYYYMMDD_SLASH = new SimpleDateFormat("yyyy/MM/dd");
    public static final SimpleDateFormat SDF_YYYYMMDD_DOT = new SimpleDateFormat("yyyy.MM.dd");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMM = new SimpleDateFormat("yyyyMMddhhmm");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMM_DASH_DASH = new SimpleDateFormat("yyyy-MM-dd HH-mm");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMM_DASH_DOT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMMM_DASH_DOT = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMMSS_NO_MARK = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMMSSM_DASH_DOT = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMMSSM_DASH = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMMSSMS_DASH_DOT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    public static final SimpleDateFormat SDF_YYYYMMDDHHMMSSM_DOT = new SimpleDateFormat("yyyyy.MM.dd a hh:mm");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMMSSSM_DOT = new SimpleDateFormat("yyyyy.MM.dd aaa hh:mm");
    public static final SimpleDateFormat SDF_YYYYMMDDHHMMSSMA_DASH_DOT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");

    public static final SimpleDateFormat SDF_YYYYMMDDHHMMSSMSSS_SLASH_DOT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");

    public static final SimpleDateFormat SDF_MMDDYYYY = new SimpleDateFormat("MMddyyyy");
    public static final SimpleDateFormat SDF_MMDDYYYY_DASH = new SimpleDateFormat("MM/dd/yyyy");
    public static final SimpleDateFormat SDF_MMDDYYYY_DOT = new SimpleDateFormat("MM.dd.yyyy");
    public static final SimpleDateFormat SDF_MMDDYYYYHHMM = new SimpleDateFormat("MMddyyyyhhmm");
    public static final SimpleDateFormat SDF_MMDDYYYYHHMMM_DASH_DOT = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
    public static final SimpleDateFormat SDF_MMDDYYYYHHMMSSM_DASH_DOT = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
    public static final SimpleDateFormat SDF_MMDDYYYYHHMMSSM_DOT = new SimpleDateFormat("MM.dd.yyyy hh:mm a");
    public static final SimpleDateFormat SDF_MMDDYYYYHHMMSSSM_DOT = new SimpleDateFormat("MM.dd.yyyy hh:mm aaa");
    public static final SimpleDateFormat SDF_MMDDYYYYHHMMSSMA_DASH_DOT = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
    public static final int DATE_TYPE_DAY = 1;
    public static final int DATE_TYPE_WEEK = 2;
    public static final int DATE_TYPE_MONTH = 3;
    public static final int DATE_TYPE_HALF_YEAR = 4;
    public static final int DATE_TYPE_YEAR = 5;

    public static final SimpleDateFormat[] defaultDateformats = new SimpleDateFormat[]{
            DateUtil.SDF_YYYYMMDD_DASH,
            DateUtil.SDF_YYYYMMDD_DOT,
            DateUtil.SDF_YYYYMMDD_SLASH,
            DateUtil.SDF_YYYYMMDDHHMM_DASH_DOT,
            DateUtil.SDF_YYYYMMDDHHMMSSM_DASH,
            DateUtil.SDF_YYYYMMDDHHMMSSMS_DASH_DOT
    };

    public static Timestamp getNow() {
        Timestamp tsNow = new Timestamp(System.currentTimeMillis());
        tsNow.setNanos(0);

        return tsNow;
    }

    public static String convertString(String date, SimpleDateFormat sourceFormat, SimpleDateFormat targetFormat) {
        if (ObjectUtil.isEmpty(date)) {
            return "";
        }

        try {
            synchronized (sourceFormat) {
                Date dt = sourceFormat.parse(date);
                return targetFormat.format(dt);
            }
        } catch (Exception e) {
            return "";
        }
    }

    protected static Map<String, SimpleDateFormat> timeZoneDateFormatMap = new HashMap<String, SimpleDateFormat>();
    protected static final String TIMEZONE_DATE_FORMAT_DELIMITER = "||";

    public static SimpleDateFormat getDateFormatTimeZoneSupport(SimpleDateFormat dateFormat, TimeZone timeZone) {
        String key = null;
        SimpleDateFormat sdfTimeZoneSupport = null;

        if (ObjectUtil.isNotEmpty(timeZone)) {
            key = dateFormat.toPattern() + TIMEZONE_DATE_FORMAT_DELIMITER + timeZone.getID();
            sdfTimeZoneSupport = timeZoneDateFormatMap.get(key);
            if (sdfTimeZoneSupport == null) {
                sdfTimeZoneSupport = (SimpleDateFormat) dateFormat.clone();
                sdfTimeZoneSupport.setTimeZone(timeZone);
                timeZoneDateFormatMap.put(key, sdfTimeZoneSupport);
            }
            return sdfTimeZoneSupport;
        }
        return dateFormat;
    }

    public static String convertDate(Date date, SimpleDateFormat dateFormat) {
        return convertDate(date, dateFormat, TimeZone.getDefault());
    }

    public static String convertDate(Date date, SimpleDateFormat dateFormat, TimeZone timeZone) {
        SimpleDateFormat sdfTimeZoneSupport = null;

        if (ObjectUtil.isEmpty(date)) {
            return "";
        }

        sdfTimeZoneSupport = getDateFormatTimeZoneSupport(dateFormat, timeZone);
        try {
            synchronized (sdfTimeZoneSupport) {
                return sdfTimeZoneSupport.format(date);
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static String convertDate(Timestamp timestamp, SimpleDateFormat dateFormat) {
        return convertDate(timestamp, dateFormat, TimeZone.getDefault());
    }

    public static String convertDate(Timestamp timestamp, SimpleDateFormat dateFormat, TimeZone timeZone) {
        if (ObjectUtil.isEmpty(timestamp)) {
            return "";
        }

        return convertDate(new Date(timestamp.getTime()), dateFormat, timeZone);
    }

    public static String lastDayToString(int day, SimpleDateFormat dateFormat) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.DATE, -day);

        try {
            synchronized (dateFormat) {
                return dateFormat.format(rightNow.getTime());
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static Timestamp lastTimestamp(int dateType) {
        Calendar now = Calendar.getInstance();

        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.MILLISECOND, 0);

        switch (dateType) {
            case DATE_TYPE_DAY:
                now.add(Calendar.DATE, -1);
                break;
            case DATE_TYPE_WEEK:
                now.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case DATE_TYPE_MONTH:
                now.add(Calendar.MONTH, -1);
                break;
            case DATE_TYPE_HALF_YEAR:
                now.add(Calendar.MONTH, -6);
                break;
            case DATE_TYPE_YEAR:
                now.add(Calendar.YEAR, -1);
                break;
            default:
                break;
        }

        return new Timestamp(now.getTimeInMillis());
    }

    public static Date getLastDate(long currentTimeMillis, int dayOffset, boolean applyTimeOffset, boolean start) {
        Calendar gc = GregorianCalendar.getInstance();
        gc.setTime(new Date(currentTimeMillis));

        if (applyTimeOffset) {
            if (start) {
                gc.set(Calendar.HOUR_OF_DAY, gc.getMinimum(Calendar.HOUR_OF_DAY));
                gc.set(Calendar.MINUTE, 0);
                gc.set(Calendar.SECOND, 0);
                gc.set(Calendar.MILLISECOND, 0);
            } else {
                gc.set(Calendar.HOUR_OF_DAY, gc.getMaximum(Calendar.HOUR_OF_DAY));
                gc.set(Calendar.MINUTE, gc.getMaximum(Calendar.MINUTE));
                gc.set(Calendar.SECOND, gc.getMaximum(Calendar.SECOND));
                gc.set(Calendar.MILLISECOND, gc.getMaximum(Calendar.MILLISECOND));
            }
        }

        gc.add(Calendar.DATE, -dayOffset);
        return new Date(gc.getTime().getTime());
    }

    public static Date lastDayToDate(int day) {
        return new Date(getLastDate(System.currentTimeMillis(), day, false, true).getTime());
    }

    public static Date getLastDate(long currentTimeMillis, int dayOffset) {
        return getLastDate(currentTimeMillis, dayOffset, false, false);
    }

    public static Date getLastStartDate(long currentTimeMillis, int dayOffset) {
        return getLastDate(currentTimeMillis, dayOffset, true, true);
    }

    public static Date getLastEndDate(long currentTimeMillis, int dayOffset) {
        return getLastDate(currentTimeMillis, dayOffset, true, false);
    }

    public static String lastDayToString(String date, int day, SimpleDateFormat dateFormat) {
        Timestamp timestamp = convertString2Timestamp(date);
        Date dt = getLastDate(timestamp.getTime(), day);
        try {
            synchronized (dateFormat) {
                return dateFormat.format(dt);
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static Timestamp getLastDate(Timestamp timestamp, int day) {
        Date date = getLastDate(timestamp.getTime(), day);
        return new Timestamp(date.getTime());
    }

    public static Date getLastMonth(long currentTimeMillis, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        calendar.add(Calendar.MONTH, -month);
        return calendar.getTime();
    }

    public static String lastMonthToString(int month, SimpleDateFormat dateFormat) {
        Calendar rightNow = Calendar.getInstance();

        rightNow.add(Calendar.MONTH, -month);

        try {
            synchronized (dateFormat) {
                return dateFormat.format(rightNow.getTime());
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static String currentTimeToString(SimpleDateFormat dateFormat) {
        Calendar now = Calendar.getInstance();

        try {
            synchronized (dateFormat) {
                return dateFormat.format(now.getTime());
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static Timestamp getNextYearFromNow(int year) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.YEAR, year);

        return new Timestamp(rightNow.getTime().getTime());
    }

    public static Timestamp getLastHour(long currentTimeMillis, int hourOffset) {
        Calendar now = Calendar.getInstance();
        if (currentTimeMillis > 0) {
            now.setTime(new Date(currentTimeMillis));
        }

        now.add(Calendar.HOUR_OF_DAY, -hourOffset);

        return new Timestamp(now.getTime().getTime());
    }

    public static Timestamp getToTimeSuffix(Timestamp timestamp) {
        if (ObjectUtil.isEmpty(timestamp)) {
            return null;
        }

        return new Timestamp(timestamp.getTime() + DateUtil.DATE_TO_CONDITION_SUFFIX_MILLISECONDS);
    }

    public static Date getLastMonth(int month) {
        return getLastMonth(System.currentTimeMillis(), month);
    }

    public static int compareTimestamp(Timestamp source, Timestamp target) {
        if (ObjectUtil.isEmpty(source)) {
            return ObjectUtil.isEmpty(target) ? 0 : -1;
        }

        if (ObjectUtil.isEmpty(target)) {
            return 1;
        }

        return source.equals(target) ? 0 : source.before(target) ? -1 : 1;
    }

    public static Timestamp convertString2Timestamp(String date) {
        return convertString2Timestamp(date, TimeZone.getDefault());
    }

    public static Timestamp convertString2Timestamp(String date, TimeZone timeZone) {
        return convertString2Timestamp(date, defaultDateformats, timeZone);
    }

    public static Timestamp convertString2Timestamp(String date, SimpleDateFormat dateFormat) {
        return convertString2Timestamp(date, new SimpleDateFormat[]{dateFormat});
    }

    public static Timestamp convertString2Timestamp(String date, SimpleDateFormat[] dateFormats) {
        return convertString2Timestamp(date, dateFormats, TimeZone.getDefault());
    }

    public static Timestamp convertString2Timestamp(String date, SimpleDateFormat dateFormat, TimeZone timeZone) {
        return convertString2Timestamp(date, new SimpleDateFormat[]{dateFormat}, timeZone);
    }

    public static Timestamp convertString2Timestamp(String date, SimpleDateFormat[] dateFormats, TimeZone timeZone) {
        if (ObjectUtil.isEmpty(date)) {
            return null;
        }

        try {
            Date dtDate = null;
            dtDate = parseDate(date, dateFormats, timeZone);
            Timestamp timestamp = new Timestamp(dtDate.getTime());
            return timestamp;
        } catch (Exception e) {
            if (isMilliseconds(date)) {
                return new Timestamp(Long.parseLong(date));
            }
            return null;
        }
    }

    public static boolean isMilliseconds(String date) {
        // 숫자로만 구성되었는지 체크
        String tempDate = date;
        if (ObjectUtil.isNotEmpty(tempDate)) {
            // 음수 부호 제거
            if (tempDate.charAt(0) == '-') {
                tempDate = tempDate.substring(1);
            }
            if (tempDate.length() > 0) {
                return ObjectUtil.isNumeric(tempDate);
            }
        }
        return false;
    }

    public static Date parseDate(String date, SimpleDateFormat dateFormat) throws ParseException {
        return parseDate(date, new SimpleDateFormat[]{dateFormat});
    }

    public static Date parseDate(String date, SimpleDateFormat[] dateFormats) throws ParseException {
        return parseDate(date, dateFormats, TimeZone.getDefault());
    }

    public static Date parseDate(String date, SimpleDateFormat[] dateFormats, TimeZone timeZone) throws ParseException {
        SimpleDateFormat parser = null;
        ParsePosition pos = new ParsePosition(0);
        for (int i = 0; i < dateFormats.length; i++) {
            parser = getDateFormatTimeZoneSupport(dateFormats[i], timeZone);
            synchronized (parser) {
                pos.setIndex(0);

                Date dt = parser.parse(date, pos);
                if (ObjectUtil.isNotEmpty(dt) && pos.getIndex() == date.length()) {
                    return dt;
                }
            }
        }
        throw new ParseException("Unable to parse the date: " + date, -1);
    }

    public static Timestamp checkValidateDate(Timestamp timestamp) {
        if (ObjectUtil.isNotNull(timestamp)) {
            Timestamp tsValidate = new Timestamp(0);
            if (compareTimestamp(timestamp, tsValidate) < 0) {
                return tsValidate;
            }
        }
        return timestamp;
    }

    public static boolean isInvalidDateTime(Timestamp timestamp) {
        return compareTimestamp(timestamp, DATE_TO_MIN_TIMESTAMP) < 0 || compareTimestamp(timestamp, DATE_TO_MAX_TIMESTAMP) > 0;
    }

    public static String getNowYear() {
        Calendar cal = Calendar.getInstance();
        return String.valueOf(cal.get(Calendar.YEAR));
    }

    public static String getNowMonth() {
        Calendar cal = Calendar.getInstance();
        return String.format("%02d", cal.get(Calendar.MONTH) + 1);
    }

    public static LocalDateTime subScribeEndDate(LocalDateTime basePaymentDt, LocalDateTime nowDate) {
        if (nowDate.getDayOfMonth() == nowDate.toLocalDate().lengthOfMonth()) {
            if (basePaymentDt.getDayOfMonth() == basePaymentDt.toLocalDate().lengthOfMonth()) {
                if (nowDate.getDayOfMonth() == basePaymentDt.getDayOfMonth())
                    return nowDate.plusMonths(1);
                return basePaymentDt.plusMonths(ChronoUnit.MONTHS.between(basePaymentDt.toLocalDate(), nowDate.toLocalDate()) + 2);
            }else {
                if(nowDate.getMonth() == Month.FEBRUARY && basePaymentDt.getDayOfMonth() > nowDate.toLocalDate().lengthOfMonth())
                    return basePaymentDt.plusMonths(ChronoUnit.MONTHS.between(basePaymentDt.toLocalDate(), nowDate.toLocalDate()) + 2);

            }

        }
        return  nowDate.plusMonths(1);



    }

    public static int  daysBetweenDates(LocalDate date1, LocalDate date2) {
        return (int)ChronoUnit.DAYS.between(date1, date2);
    }
}


