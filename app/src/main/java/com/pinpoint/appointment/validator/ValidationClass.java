package com.pinpoint.appointment.validator;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

public class ValidationClass {

    private static final String PLUS_SIGN = "+";
    private static final String TAG = ValidationClass.class.getSimpleName();


    /**
     * This method validate string if it is empty or not
     *
     * @param inputString(String) : user input string
     * @return (boolean) : it return true if string is null or zero length
     * @see <a href="https://developer.android.com/reference/android/text/TextUtils.html#isEmpty%28java.lang.CharSequence%29">isEmpty</a>
     */
    public static boolean isEmpty(String inputString) {
        return TextUtils.isEmpty(inputString.trim());
    }


    /**
     * This method validate string with specific patterns
     * e.g. email,alphaNumericPassword,webUrl
     *
     * @param inputString(String) : user input string
     * @param pattern(String)     : patterns to be matched with inputString
     * @return (boolean) : it return false if inputString does not match with required pattern
     * @see Patterns#EMAIL_ADDRESS
     */
    public static boolean matchPattern(String inputString, String pattern) {
        return !(TextUtils.isEmpty(inputString) || pattern.isEmpty()) && Pattern.matches(pattern, inputString);

    }

    /**
     * This method validate the phone number with specific length
     *
     * @param inputPhoneNumber(String) : user input inputPhoneNumber e.g. 9999999999
     * @param phoneNumberLength(int)   : phone number length e.g. 10
     * @return (boolean) : it return false, if and only if, when inputPhoneNumber length is not equal to phoneNumberLength
     */
    public static boolean checkPhoneNumber(String inputPhoneNumber, int phoneNumberLength) {
        return !(TextUtils.isEmpty(inputPhoneNumber) || phoneNumberLength <= 0) && inputPhoneNumber.trim().length() == phoneNumberLength;
    }

    /**
     * This method check string with minimum length
     *
     * @param inputString(String) : input string
     * @param minLength(int)      :  minimum length e.g. 6
     * @return (boolean) : it return false if input string is less than minLength
     */
    public static boolean checkMinLength(String inputString, int minLength) {
        return (!(TextUtils.isEmpty(inputString) || minLength <= 0) && inputString.trim().length() >= minLength);
    }

    /**
     * This method check string with maximum length
     *
     * @param inputString(String) : input string
     * @param maxLength(int)      :  maximum length e.g 10
     * @return (boolean) : it return false if input string is greater than maxLength
     */
    public static boolean checkMaxLength(String inputString, int maxLength) {
        return !(TextUtils.isEmpty(inputString) || maxLength <= 0) && inputString.trim().length() <= maxLength;
    }

    /**
     * This method validate if the number is negative or not
     *
     * @return (boolean) : it return true if the parameter number is less than 0(zero)
     */
    private static boolean isNumberNegative(int number) {
        return number <= 0;
    }

    /**
     * This method check whether inputPhoneNumber starts with +(plus sign) or not.
     *
     * @param inputPhoneNumber(String) : user input inputPhoneNumber
     * @return (boolean) : it return false if and only if inputPhoneNumber does not start with +(plus sign)
     */
    public static boolean checkPlusSign(String inputPhoneNumber) {
        return inputPhoneNumber.trim().startsWith(PLUS_SIGN);
    }

    /**
     * This logic use to check email or phone number in single EditText.
     *
     * @param inputString (String) : user input String
     */
    public static boolean validateEmailOrPhone(String inputString) {
        if (inputString.isEmpty()) {
            return false;
        } else {
            if (!TextUtils.isDigitsOnly(inputString)) {
                // show number related error message
                return false;
            } else {
                if (!matchPattern(inputString, Patterns.EMAIL_ADDRESS.pattern())) {
                    // show email related error message
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    /**
     * Check If Entered Email address is valid or not...
     *
     * @param emailAddress : Email address entered by user.
     * @return true: if email address is valid...
     * false: if email address is not valid...
     */
    public static boolean isValidEmail(CharSequence emailAddress) {
        if (emailAddress == null || emailAddress.length() < 1) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();
        }
    }

    /**
     * TODO stub is generated but developer or programmer need to add code as required
     * This method check country code in prefix.
     *
     * @param inputText(String) : phone number
     * @return (boolean) : return true if  inputPhoneNumber does not start with country code
     */
//    public static boolean checkCountryCode(String inputPhoneNumber) {
//        return !inputPhoneNumber.trim().matches(Constants.PATTERN_PHONE_NUMBER_WITH_COUNTRY_CODE);
//    }
    public static boolean isDigitOnly(String inputText) {
        if (TextUtils.isDigitsOnly(inputText.trim())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidPhoneNumber(CharSequence phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber))
        {
            if(phoneNumber.length()>=10) {
                return Patterns.PHONE.matcher(phoneNumber).matches();
            }
            else {return false;}
        }
        return false;
    }
}