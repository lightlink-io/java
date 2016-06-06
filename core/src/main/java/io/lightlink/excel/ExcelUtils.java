package io.lightlink.excel;

public class ExcelUtils {

    public static String toExcelColumnName(int number) {
        StringBuilder sb = new StringBuilder();
        number++;
        while (number-- > 0) {
            sb.append((char) ('A' + (number % 26)));
            number /= 26;
        }
        return sb.reverse().toString();
    }


    public static int toExcelColumnNumber(String name) {
        int number = 0;
        for (int i = 0; i < name.length(); i++) {
            number = number * 26 + (name.charAt(i) - ('A' - 1));
        }
        return number-1;
    }

}
