package pickle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Date {
    private static final int[] iDaysPerMonth = new int[]{
        0, 31, 29, 31, 30, 31, 30, 31, 31, 30 , 31, 30, 31
    };
    private static final String numbers = "0123456789";


    private static int validateNumber(String date, int startIndex, int endIndex) throws PickleException {
        String subStr = date.substring(startIndex, endIndex);

        for (char c : subStr.toCharArray()) {
            if (!numbers.contains(Character.toString(c))) {
                throw new PickleException();
            }
        }

        return Integer.parseInt(subStr);

    }

    public static boolean isLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }


    public static ResultValue validateDate(String tokenStr) throws PickleException {
        ResultValue res;

        // first check for number of characters in String

        // TODO: 4/19/2021 throw DateException Error
        if (tokenStr.length() != 10)
            throw new PickleException();

        int year = validateNumber(tokenStr, 0, 4);

        if (tokenStr.charAt(4) != '-' && tokenStr.charAt(7) != '-') {
            throw new PickleException();
        }

        int month = validateNumber(tokenStr, 5, 7);

        int day = validateNumber(tokenStr, 8, 10);

        if (month < 1 || month > 12) {
            throw  new PickleException();
        }

        if (day < 1 || day > iDaysPerMonth[month])
            throw new PickleException();

        res = new ResultValue(tokenStr, SubClassif.DATE);


        if (day == 29 && month == 2) {
            if (!isLeapYear(year)) {
                // not correct date format
                throw new PickleException();
            }
        }

        return res;
    }

    private static int dateToJulian(String dateStr) throws PickleException {
        int year = validateNumber(dateStr, 0, 4);
        int month = validateNumber(dateStr, 5, 7);
        int day = validateNumber(dateStr, 8, 10);

        if (month > 2) {
            month = month - 3;
        } else {
            month = month + 9;
            year = year - 1;
        }

        return 365 * year
                + year / 4 - year / 100
                + year / 400
                + (month * 306 + 5) / 10
                + (day);
    }



    public static ResultValue dateDiff(ResultValue date1, ResultValue date2) throws PickleException {
        if (date1.dataType != SubClassif.DATE) {
            date1 = validateDate(date1.strValue);
        }

        if (date2.dataType != SubClassif.DATE) {
            date2 = validateDate(date2.strValue);
        }

        int julian1 = dateToJulian(date1.strValue);
        int julian2 = dateToJulian(date2.strValue);

        System.out.printf("Date 1 str: %d; Date 2 str: %d\n", julian1, julian2);


        return new ResultValue(String.valueOf(julian1 - julian2), SubClassif.INTEGER);

    }


    public static ResultValue dateAdj(ResultValue date, ResultValue adjValue) throws PickleException {
        if (date.dataType != SubClassif.DATE) {
            date = validateDate(date.strValue);
        }
        if (adjValue.dataType != SubClassif.INTEGER) {
            throw new PickleException();
        }

        SimpleDateFormat sDF = new SimpleDateFormat("yyy-MM-dd");

        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(sDF.parse(date.strValue));
        } catch (ParseException e) {
           throw new PickleException();
        }

        calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(adjValue.strValue));


        String newDate = sDF.format(calendar.getTime());



        return validateDate(newDate);

    }


    public static ResultValue dateAge(ResultValue date1, ResultValue date2) throws PickleException {
        if (date1.dataType != SubClassif.DATE)
            date1 = validateDate(date1.strValue);

        if (date2.dataType != SubClassif.DATE)
            date2 = validateDate(date2.strValue);

        int date1Year = validateNumber(date1.strValue, 0, 4);
        int date1Month = validateNumber(date1.strValue, 5, 7);

        int date2Year = validateNumber(date2.strValue, 0, 4);
        int date2Month = validateNumber(date2.strValue, 5, 7);

        int out = date1Year - date2Year;

        if (date1Month < date2Month) {
            out--;
        }

        return new ResultValue(String.valueOf(out), SubClassif.INTEGER);
    }
}
