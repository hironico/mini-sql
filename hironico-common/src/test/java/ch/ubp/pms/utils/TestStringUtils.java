package ch.ubp.pms.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestStringUtils {

    @Test
    public void testCountOccurences() {

        String text = "AAABBC\n";

        int count = StringUtils.countOccurences(text, 'A');
        assertEquals(3, count);

        count = StringUtils.countOccurences(text, '\n');
        assertEquals(1, count);
    }
}