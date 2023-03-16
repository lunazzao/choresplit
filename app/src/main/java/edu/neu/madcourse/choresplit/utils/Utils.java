package edu.neu.madcourse.choresplit.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern INVALID_PATH_REGEX = Pattern.compile("[\\[\\]\\.#$]");
    private static final Pattern INVALID_KEY_REGEX =
            Pattern.compile("[\\[\\]\\.#\\$\\/\\u0000-\\u001F\\u007F]");

    public static boolean isValidKey(String key) {
        return !INVALID_PATH_REGEX.matcher(key).find();
    }

    public static String shortDayOfWeek(DayOfWeek day) {
        switch (day) {
            case MONDAY:
                return "Mon";
            case TUESDAY:
                return "Tue";
            case WEDNESDAY:
                return "Wed";
            case THURSDAY:
                return "Thur";
            case FRIDAY:
                return "Fri";
            case SATURDAY:
                return "Sat";
            case SUNDAY:
                return "Sun";
        }
        return "BAD";
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            byte[] bytes = md.digest();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            // Get complete hashed password in hex format
            return sb.toString();

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return null;
        }

    }

    public static boolean passwordCorrect(String password, String correctHash) {
        return correctHash.equals(hashPassword(password));
    }

}
