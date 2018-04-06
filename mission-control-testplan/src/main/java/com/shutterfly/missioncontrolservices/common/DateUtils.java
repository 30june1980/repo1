package com.shutterfly.missioncontrolservices.common;

import com.shutterfly.missioncontrolservices.util.AppConstants;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private DateUtils() {
    }

    public static String convertLocalDateToString(@Nonnull LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(AppConstants.DATE_PATTERN));
    }

}
