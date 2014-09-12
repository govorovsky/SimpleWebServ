package com.govorovsky.webserver.http.util;

/**
 * Created by Andrew Govorovsky on 10.09.14
 */

public final class HttpUtils {

    public static String decodeStr(String encoded) {
        StringBuilder stringBuilder = new StringBuilder();
        int numChars = encoded.length();
        int i = 0;
        while (i < numChars) {
            char c = encoded.charAt(i);
            switch (c) {
                case '%':
                    try {
                        stringBuilder.append((char) Integer.parseInt(new String(new char[]{encoded.charAt(i + 1), encoded.charAt(i + 2)}), 16));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    i += 3;
                    break;
                case '+':
                    stringBuilder.append(' ');
                    i++;
                    break;
                default:
                    stringBuilder.append(c);
                    i++;
                    break;
            }
        }
        return stringBuilder.toString();
    }
}
