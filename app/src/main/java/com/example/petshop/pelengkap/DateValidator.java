package com.example.petshop.pelengkap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateValidator {
    int day, month, year;

    public static Date String2Date(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            return inputFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Calendar getCalendarFromDate(String inputDate) {
        String[] dateParts = inputDate.split("-");
        day = Integer.parseInt(dateParts[0]);
        month = Integer.parseInt(dateParts[1]);
        year = Integer.parseInt(dateParts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        return calendar;
    }

    public boolean isTanggalValid(String inputDate, String specifiedDate) {
        Calendar inputCalendar = getCalendarFromDate(inputDate);
        Calendar currentCalendar = Calendar.getInstance();
        Calendar specifiedCalendar = getCalendarFromDate(specifiedDate);

        return specifiedCalendar.compareTo(inputCalendar) <= 0
                && inputCalendar.compareTo(currentCalendar) >= 0;
    }

    public boolean isdateValid(String inputDate) {
        Calendar inputCalendar = getCalendarFromDate(inputDate);
        Calendar currentCalendar = Calendar.getInstance();

        // Periksa apakah tanggal yang diinput valid dan lebih besar atau sama dengan tanggal sekarang
        return day != inputCalendar.get(Calendar.DAY_OF_MONTH) ||
                month - 1 != inputCalendar.get(Calendar.MONTH) ||
                year != inputCalendar.get(Calendar.YEAR) ||
                inputCalendar.compareTo(currentCalendar) < 0;
    }

    // untuk mengatasi parameter default untuk metode pada java
    public static String convertDateFormat(String inputDate) {
        return convertDateFormat(inputDate, "dd-MM-yyyy", "yyyy-MM-dd");
    }

    public static String convertDateFormat(String inputDate, String formatInput, String formatOutput) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(formatInput, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(formatOutput, Locale.forLanguageTag("id"));

            Date date = inputFormat.parse(inputDate);
            assert date != null;
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}