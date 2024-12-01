package com.weolbu.assignment.util;

public class PhoneNumberUtils {
    public static String normalizePhoneNumber(String phoneNumber){
        if (phoneNumber == null)
            return null;
        return phoneNumber.replaceAll("[^0-9]","");
    }

}
