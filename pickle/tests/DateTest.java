package pickle.tests;

import org.junit.jupiter.api.Test;
import pickle.Date;
import pickle.PickleException;
import pickle.ResultValue;
import pickle.SubClassif;

import static org.junit.jupiter.api.Assertions.*;

class DateTest {

    private final String[] goodDateStrings = new String[] {
            "2017-04-01",
            "2015-05-01",
            "2017-02-01",
            "2015-02-01",
            "2017-02-01",
            "1957-12-04",
            "1953-12-12",
            "2016-12-31",
            "2016-02-29",
            "1995-07-09"
    } ;



    private final int[] goodDateDiff = new int[] {
            701,
            -642,
            731,
            -731,
            21609,
            1453,
            -23030,
            306,
            7540
    };

    private final int[] goodNumberStrings = new int[] {
            2017,
            2015,
            2017,
            2015,
            2017,
            1957,
            1953,
            2016,
            2016
    };

    private final String[] badDateStrings = new String[] {
            "2015-02-29",
            "2016-06-31",
            "2016-12-31A",
            "2016-12-3100",
            "2016-1212-31"
    };

    private final int[] leapYears = new int[] {
            2016,
            2020,
            2012,
            2008,
            2004
    };

    private final int[] nonLeapYears = new int[] {
            1900,
            2002,
            2010,
            2021,
            2017
    };




    @Test
    void isLeapYear() {

        for (int year : leapYears) {
            assertTrue(Date.isLeapYear(year));
        }

        for (int year : nonLeapYears) {
            assertFalse(Date.isLeapYear(year), "Failed on date " + year);
        }
    }

    @Test
    void validateDate() {

        for (String date : goodDateStrings) {
            ResultValue expectedResult = new ResultValue(date, SubClassif.DATE);



            assertDoesNotThrow(() ->{
                ResultValue receivedResult = Date.validateDate(date);

                assertEquals(expectedResult.strValue, receivedResult.strValue);
                assertEquals(expectedResult.dataType, receivedResult.dataType);
            }, "Error on year: " + date);


        }

        for (String date : badDateStrings) {

            assertThrows(PickleException.class, () -> Date.validateDate(date));

        }
    }


    @Test
    void dateDiff() {
        for (int i = 0; i < goodDateStrings.length - 1; i++) {

            final int index = i;
            ResultValue expected = new ResultValue(String.valueOf(goodDateDiff[index]), SubClassif.INTEGER);


            assertDoesNotThrow(() -> {
                ResultValue date1 = new ResultValue(goodDateStrings[index], SubClassif.DATE);
                ResultValue date2 = new ResultValue(goodDateStrings[index+1], SubClassif.DATE );

                ResultValue received = Date.dateDiff(date1, date2);

                assertEquals(expected.strValue, received.strValue, "Error on dates: " + date1.strValue + " " + date2.strValue + "; Expected: " + expected.strValue + "; Received: " + received.strValue);
                assertEquals(expected.dataType, received.dataType, "Error on dates: " + date1.strValue + " " + date2.strValue + "; Expected: " + expected.dataType.name() + "; Received: " + received.dataType.name());

            });
        }

    }


    @Test
    void dateAdj() {
        for (int i = 0; i < goodDateStrings.length - 1; i++) {

            final int index = i;
            ResultValue expected = new ResultValue(goodDateStrings[index+1], SubClassif.DATE );

            assertDoesNotThrow(() -> {
                ResultValue date = new ResultValue(goodDateStrings[index], SubClassif.DATE);
                ResultValue adjValue = new ResultValue(String.valueOf(goodDateDiff[index]*-1), SubClassif.INTEGER);

                ResultValue received = Date.dateAdj(date, adjValue);

                assertEquals(expected.strValue, received.strValue, "Error on date : " + date.strValue + ", " + expected.strValue + "; Expected: " + expected.strValue + "; Received: " + received.strValue);
                assertEquals(expected.dataType, received.dataType, "Error on date : " + date.strValue + ", " + expected.strValue + "; Expected: " + expected.dataType.name() + "; Received: " + received.dataType.name());

            });
        }
    }


    @Test
    void dateAge() {
        ResultValue expected = new ResultValue("2", SubClassif.INTEGER);

        assertDoesNotThrow(() -> {
            ResultValue date1 = new ResultValue("2017-02-01", SubClassif.STRING);
            ResultValue date2 = new ResultValue("2015-02-01", SubClassif.STRING);

            ResultValue received = Date.dateAge(date1, date2);

            assertEquals(expected.strValue, received.strValue, "Error on dates: " + date1.strValue + " " + date2.strValue + "; Expected: " + expected.strValue + "; Received: " + received.strValue);
            assertEquals(expected.dataType, received.dataType, "Error on dates: " + date1.strValue + " " + date2.strValue + "; Expected: " + expected.dataType.name() + "; Received: " + received.dataType.name());


        });
    }
}