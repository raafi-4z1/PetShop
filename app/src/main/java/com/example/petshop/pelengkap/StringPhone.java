package com.example.petshop.pelengkap;

public class StringPhone {
    public static String phoneNumber(String str) {
        return str.charAt(0) == '0' ? str.substring(1) : str.startsWith("62") ? str.substring(2) : str;
    }

    public static String formatPhone(String str) {
        str = phoneNumber(str);

        if (str.length() > 11) {
            return "Invalid phone number length";
        }

        // Split the phone number into three parts using substring
        String part1 = str.substring(0, 3);
        String part2 = str.substring(3, 7);
        String part3 = str.substring(7, 11);

        return part1 + "-" + part2 + "-" + part3;
    }
}
