/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myRegex;

/**
 *
 * @author KOCMOC
 */
public class MyRegex {

    public static String INGRED_REGEX = "\\d{5}"; //5 digits no spaces: 00000
    public static String RECIPE_REGEX = "(\\d{2})(-)(\\d{1})(-)(\\w{1})(\\d{3})"; // 00-8-N752: two digits - one digit - digit or letter - 3 digits

    public static boolean check(String toCheck, String regex) {
        if (toCheck.matches(regex)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("" + check("12345", INGRED_REGEX));
    }
}
