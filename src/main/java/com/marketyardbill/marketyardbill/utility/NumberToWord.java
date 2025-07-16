
package com.marketyardbill.marketyardbill.utility;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NumberToWord {
    private static final String[] units = {
            "", "one", "two", "three", "four", "five", "six",
            "seven", "eight", "nine", "ten", "eleven", "twelve",
            "thirteen", "fourteen", "fifteen", "sixteen",
            "seventeen", "eighteen", "nineteen"
    };

    private static final String[] tens = {
            "", "", "twenty", "thirty", "forty", "fifty",
            "sixty", "seventy", "eighty", "ninety"
    };

    public  String convert(BigDecimal number) {
        BigDecimal rounded = number.setScale(2, BigDecimal.ROUND_HALF_UP);
        int integerPart = rounded.intValue();
        int decimalPart = rounded.remainder(BigDecimal.ONE)
                .movePointRight(2)
                .intValue();

        StringBuilder result = new StringBuilder();
        result.append(convertToWords(integerPart));

        if (decimalPart > 0) {
            result.append(" point");
            for (char digit : String.valueOf(decimalPart).toCharArray()) {
                result.append(" ").append(units[digit - '0']);
            }
        }

        return result.toString().trim();
    }

    private static String convertToWords(int number) {
        if (number == 0) return "zero";

        StringBuilder result = new StringBuilder();

        if (number >= 10000000) {
            result.append(convertToWords(number / 10000000)).append(" crore ");
            number %= 10000000;
        }

        if (number >= 100000) {
            result.append(convertToWords(number / 100000)).append(" lakh ");
            number %= 100000;
        }

        if (number >= 1000) {
            result.append(convertToWords(number / 1000)).append(" thousand ");
            number %= 1000;
        }

        if (number >= 100) {
            result.append(units[number / 100]).append(" hundred");
            number %= 100;
            if (number > 0) {
                result.append(" and ");
            } else {
                result.append(" ");
            }
        }

        if (number >= 20) {
            result.append(tens[number / 10]);
            if (number % 10 > 0) {
                result.append(" ").append(units[number % 10]);
            }
        } else if (number > 0) {
            result.append(units[number]);
        }

        return result.toString().trim();
    }
}
