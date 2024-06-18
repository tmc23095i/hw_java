
package HW240606;

interface Lambda_itos {
    String itos(int i);
}

interface Lambda_printClassNameSearch {
    void printClassNameSearch(String str_input, String str_exception, boolean if_print_regex, boolean if_digits_word);
}

public class HW240606_2 {
    public static void main_(String[] args) throws Exception {
        System.out.println("*********************************");
        System.out.println("***** Homework_2 2024_06_06 *****");
        System.out.println("*********************************");

        System.out.println("\n>>> Print sub-strings matching regex. ");
        // set var
        String txt_test = "";
        {
            txt_test += "String\n";
            txt_test += "Stringgg\n";
            txt_test += "Strin\n";
            txt_test += "StringString\n";
            //
            txt_test += "String000 \n";
            txt_test += "String000Abc \n";
            txt_test += "AbcString000 \n";
            txt_test += "AbcString000Abc \n";
            //
            txt_test += "ABCStringABC\n";
            txt_test += "Abc00\n";
            txt_test += "AbcString01\n";
            txt_test += "StringAbc02\n";
            txt_test += "AbcStringAbc03\n";
            txt_test += "AbcStrin04\n";
            txt_test += "StrinAbc05\n";
            txt_test += "AbcStrinAbc06\n";
            txt_test += "AbcAbcAbc07\n";
            //
            txt_test += "ASA \n";
            txt_test += "AbcSA \n";
            txt_test += "ASAbc \n";
            txt_test += "AbcSAbc \n";
            txt_test += "A1 \n";
            txt_test += "A1B1 \n";
            txt_test += "A1B1a \n";
            txt_test += "A1S \n";
            txt_test += "A1SB \n";
            txt_test += "A1S012 \n";
            txt_test += "A1S012B1 \n";
            //
            txt_test += "---";
        }

        System.out.println("[src]: \n" + txt_test);
        // match and show
        Lambda_printClassNameSearch lambda = (String str_inpute, String str_exception, boolean if_print_regex,
                boolean if_digits_word) -> {
            // get matches
            String classname_arr[] = searchClassName(str_inpute, str_exception, if_print_regex, if_digits_word);
            //print optional info
            System.out.println();
            if (if_print_regex == true) {
                System.out.println("[regex]: " + classname_arr[0]);
            }
            System.out.println("[exception]: '" + str_exception + "'");
            if (if_digits_word == true) {
                System.out.println("[digit]: separated");
            } else {
                System.out.println("[digit]: mixed");
            }
            //print matches
            System.out.println("[result]: ");
            if (if_print_regex == true) {
                printStrArray(classname_arr, 1, 0);
            } else {
                printStrArray(classname_arr, 0, 0);
            }
        };
        String exception_str;
        boolean digits_as_word = true;

        exception_str = "String";
        lambda.printClassNameSearch(txt_test, exception_str, true, digits_as_word);
        lambda.printClassNameSearch(txt_test, exception_str, true, !digits_as_word);

        exception_str = "Strin";
        lambda.printClassNameSearch(txt_test, exception_str, true, digits_as_word);

        exception_str = "S";
        lambda.printClassNameSearch(txt_test, exception_str, true, digits_as_word);
        lambda.printClassNameSearch(txt_test, exception_str, true, !digits_as_word);
        lambda.printClassNameSearch(txt_test, exception_str, false, true);
        lambda.printClassNameSearch(txt_test, exception_str, false, false);

        System.out.println("\n*********************************");
        System.out.println("************** END **************");
        System.out.println("*********************************\n\n\n");
    }

    // search class-name(PascalCase) and return matches array
    // <exception_component_str> to specify what component the name doesnt contain
    // <debug_regex> to specify whether record the regex string at <return[0]>
    static String[] searchClassName(String src_text, String exception_component_str, boolean debug_regex,
            boolean if_digits_word) {
        // default search regex : ClassName without 'String'
        String regex_initialUppercase;
        String regex_remainLowercase;
        if (if_digits_word == true) {// treat letters and digits as separate words: `String711` -> `String` and `711`
            regex_initialUppercase = "(S(?!tring($|[^a-z]))|[A-RT-Z])";
        } else {// treat letter and digits mixes as one word: `String001` or `String002`...
            regex_initialUppercase = "(S(?!tring($|[^0-9a-z]))|[A-RT-Z])";
        }
        regex_remainLowercase = "([0-9a-z]*)";

        // analyse exception regex
        int exception_len = exception_component_str.length();
        char exception_initial = exception_component_str.charAt(0);
        String exception_remain;
        if (exception_len != 0) {
            if (exception_len > 1) {
                exception_remain = exception_component_str.substring(1);
            } else {
                exception_remain = "";
            }
            // modify exception regex
            regex_initialUppercase = regex_initialUppercase.replace('S', exception_initial);
            regex_initialUppercase = regex_initialUppercase.replace("tring", exception_remain);
            if (exception_initial == 'A') {
                regex_initialUppercase = regex_initialUppercase.replace("A-RT", "B");
            } else if (exception_initial == 'Z') {
                regex_initialUppercase = regex_initialUppercase.replace("RT-Z", "Y");
            } else {
                regex_initialUppercase = regex_initialUppercase.replace('R', (char) ((int) exception_initial - 1));
                regex_initialUppercase = regex_initialUppercase.replace('T', (char) ((int) exception_initial + 1));
            }
        } else {
            // modify exception regex
            regex_initialUppercase = "[A-Z]";
        }
        // make regex
        String regex_oneWord = "(" + regex_initialUppercase + regex_remainLowercase + ")";
        String regex_className = "(" + regex_oneWord + "+" + ")";
        String regex_className_left = "(?<=^|[^0-9a-zA-z])";
        String regex_className_right = "(?=$|[^0-9a-zA-z])";
        String regex_className_withBound = "(" + regex_className_left + regex_className + regex_className_right + ")";
        String regex_str = regex_className_withBound;
        // search matches
        String split_arr[] = src_text.split("[^0-9A-Za-z]+");
        String matches_str = "";
        for (int i = 0; i < split_arr.length; i++) {
            if (true == split_arr[i].matches(regex_str)) {
                matches_str += split_arr[i];
                matches_str += "\n";
            }
        }
        // return
        String matches_arr[];
        if (debug_regex == true) {
            matches_str = "regex_str_placeholder\n" + matches_str;
            matches_arr = matches_str.split("\n");
            matches_arr[0] = regex_str;
        } else {
            matches_arr = matches_str.split("\n");
        }
        return matches_arr;
    }

    // print string array
    // print format e.g.:
    // [0]: str0
    // [1]: str1
    // ...
    static void printStrArray(String str_arr[], int index_begin, int max_count) {
        // integer to string
        Lambda_itos lambda = (int num) -> {
            // conver value(+/-999,999,999) to string
            // limit
            if (num > 999999999) {
                num = 999999999;
            }
            if (num < -999999999) {
                num = -999999999;
            }
            // set var
            char ch_arr[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            char ch;
            int ch_count = 0;
            boolean negative = false;
            if (num < 0) {
                negative = true;
                num *= -1;
            }
            // digit to char
            for (int i = 0; true; i++) {
                // get char
                ch = (char) (num % 10 + (int) '0');
                // record
                ch_arr[i] = ch;
                // update num
                num /= 10;
                // finish
                if (num == 0) {
                    ch_count = i + 1;
                    break;
                }
            }
            // reverse
            for (int i = 0; i < ch_count / 2; i++) {
                // arr: i...|...i'
                int i_ = ch_count - 1 - i;
                // change position
                char temp = ch_arr[i];
                ch_arr[i] = ch_arr[i_];
                ch_arr[i_] = temp;
            }
            // return
            String rtn = String.valueOf(ch_arr);
            if (negative == true) {
                // add '-'
                rtn = '-' + rtn;
            }
            return rtn;
        };
        // test;System.out.println(itos.lambda(-1987654321));

        //get ending index
        if (max_count == 0) {
            max_count = str_arr.length;
        }
        int index_end = index_begin + max_count;
        if (index_end >= str_arr.length) {
            index_end = str_arr.length;
        }

        //print each str
        for (int i = index_begin; i < index_end; i++) {
            System.out.println(lambda.itos(i) + ": " + str_arr[i]);
        }
    }
}
