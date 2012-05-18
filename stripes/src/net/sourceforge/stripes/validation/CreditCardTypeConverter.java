/* Copyright 2007 Aaron Porter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package net.sourceforge.stripes.validation;

import java.util.Collection;
import java.util.Locale;

/**
 * <p>A faux TypeConverter that validates that the String supplied is a valid credit card number.
 * The Luhn algorithm is used in addition to valid credit card prefixes to determine if the string
 * being converted qualifies as a credit card number. This DOES NOT check to see if an actual card
 * with the specified number exists, only that it appears to be a valid card number.</p>
 *
 * <p>If the credit card number is not valid a single error message will be generated.  The error
 * message is a scoped message with a default scope of <tt>converter.creditCard</tt> and name
 * <tt>invalidCreditCard</tt>.  As a result error messages will be looked for in the following
 * order:</p>
 *
 * <ul>
 *   <li>beanClassFQN.fieldName.invalidCreditCard</li>
 *   <li>actionPath.fieldName.invalidCreditCard</li>
 *   <li>fieldName.invalidCreditCard</li>
 *   <li>beanClassFQN.invalidCreditCard</li>
 *   <li>actionPath.invalidCreditCard</li>
 *   <li>converter.creditCard.invalidCreditCard</li>
 * </ul>
 *
 * @author Aaron Porter
 * @since Stripes 1.5
 */
public class CreditCardTypeConverter implements TypeConverter<String> {
     // Recognized card types
     public enum Type {
         AMEX,DinersClub,Discover,enRoute,JCB,MasterCard,VISA
     }

    /** Accepts the Locale provided, but does nothing with it since credit card numbers are Locale-less. */
    public void setLocale(Locale locale) { /** Doesn't matter for credit cards. */ }

    /**
     * Validates the user input to ensure that it looks like a valid credit card number.
     *
     * @param input the String input, always a non-null non-empty String
     * @param targetType realistically always String since java.lang.String is final
     * @param errors a non-null collection of errors to populate in case of error
     * @return the credit card with any non-numeric characters removed or null
     */
    public String convert(String input, Class<? extends String> targetType, Collection<ValidationError> errors) {
        // Remove any non-numeric characters
        String cardNumber = input.replaceAll("\\D", "");
        
        if (getCardType(cardNumber) != null)
            return cardNumber;
        
        errors.add( new ScopedLocalizableError("converter.creditCard", "invalidCreditCard") );
        return null;
    }
    /**
     * Determines the type of card from the card number passed in.
     * 
     * @param cardNumber the card number to check
     * @return the type of card or null if the card number is invalid for all known card types
     */
    public static Type getCardType(String cardNumber)
    {
        if (!isLuhnValid(cardNumber))
            return null;
        
        if (checkCard(cardNumber, 15, "34", "37"))
            return Type.AMEX;
        
        if (checkCard(cardNumber, 14, "30", "36", "38"))
            return Type.DinersClub;
            
        if (checkCard(cardNumber, 16, "6011"))
            return Type.Discover;
        
        if (checkCard(cardNumber, 15, "2014", "2149"))
            return Type.enRoute;
        
        if (checkCard(cardNumber, 16, "3088","3096","3112","3158","3337","3528"))
            return Type.JCB;
        
        if (checkCard(cardNumber, 16, "51", "52", "53", "54", "55"))
            return Type.MasterCard;
        
        if (checkCard(cardNumber, 13, "4") || checkCard(cardNumber, 16, "4"))
            return Type.VISA;
        
        return null;
    }
    
    /**
     * Checks cardNumber to see if it is the correct length and starts with
     * one of the specified prefixes.
     * 
     * @param cardNumber the card number to check
     * @param length the correct string length
     * @param prefixes possible prefixes
     * @return true when all conditions are met
     */
    private static boolean checkCard(String cardNumber, int length, String...prefixes)
    {
        if (cardNumber.length() != length)
            return false;
        
        for (String prefix : prefixes)
            if (cardNumber.startsWith(prefix))
                return true;
        
        return false;
    }

    /**
     * Performs the Luhn algorithm on the card number to determine if it is valid
     * as a credit card number.
     * 
     * @param cardNumber the card number to check
     * @return true if cardNumber looks like a valid credit card number
     */
    private static boolean isLuhnValid(String cardNumber)
    {
        if (cardNumber.length() < 13 || cardNumber.length() > 16)
            return false;
    
        int sum = 0;
        
        for (int i = 0, length = cardNumber.length(); i < length; i++)
        {
            int pos = length - i - 1;
            
            int v = Integer.parseInt(cardNumber.substring(pos, pos + 1));
            
            if (i % 2 == 1)
                v *= 2;
    
            sum += v / 10 + v % 10;
        }
        
        return sum % 10 == 0;
    }
}