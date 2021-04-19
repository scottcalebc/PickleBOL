package pickle;

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

        return 365 * year + year / 4 - year / 100 + year / 400 + (month * 306 + 5) / 10 + (day);
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


        return new ResultValue(String.valueOf(julian1 - julian2), SubClassif.INTEGER);

    }
}
