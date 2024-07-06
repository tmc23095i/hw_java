
import HW240523.HW240523_0;
import HW240530.HW240530_1;
import HW240606.HW240606_2;
import HW240613.HW240613_3;
import HW240620.HW240620_4;

@SuppressWarnings("unused")

public class App {
    public static void main(String[] args) throws Exception {
        //
        boolean EXIT_APP = false;
        while (EXIT_APP == false) {
            //
            System.out.print("Type the index of homework to execute it, or `-1` to exit: ");
            //
            int hw_i = ioinValue();
            switch (hw_i) {
                // 未使用反射机制的原因
                // 第一次从老师那里听说反射的时候是很惊讶的, 因为从C/C++的思维模式出发, 这种东西几乎是不可能, 但是这种东西确实在自动化分析处理一些源码的时候很方便.
                // 优点可观但忍不愿使用的原因有三:
                // 1. 感觉不太安全, 因为竟然直接从内码找源码逻辑, 即使Java本身可能作为加密/解密者
                // 2. 感觉浪费性能, 因为执行时需要查找源码(猜测), 所以相当于计算代偿空间, 损失了运行时效率
                // 3. 反射的行为模式很像动态语言, 所以没有对自己程序完全掌控的安全感
                case -1:
                    EXIT_APP = true;
                    break;
                case 0:
                    HW240523_0.main_();
                    break;
                case 1:
                    HW240530_1.main_();
                    break;
                case 2:
                    HW240606_2.main_();
                    break;
                case 3:
                    HW240613_3.main_();
                    break;
                case 4:
                    HW240620_4.main_();
                    break;
                // ... code for the next hw
                default:
                    HW240523_0.main_();
                    HW240530_1.main_();
                    HW240606_2.main_();
                    HW240613_3.main_();
                    HW240620_4.main_();
                    // ... code for the next hw
                    break;
            }
        }
        //
        System.out.println("Exiting...");
    };

    public static int ioinValue() throws Exception {
        //
        int ch_io;//one char of the std-in stream
        int[] digits = { 0, 0, 0, 0, 0, 0, 0, 0 };//the digits of the value user typed
        int digit_cnt = 0;//the digit count of the value

        int value_sign = 1;// if the value is positive or negative
        int prev_ch = 0;//the previous char, used to check if the value begin with `-`

        boolean VALUE_READING = false;//to mark if the value is typing
        //get digits of value
        while (true) {
            // get one char from io stream
            ch_io = System.in.read(); // char to number
            int digit = ch_io - (int) '0';
            // if non-digit char
            if (!((digit >= 0) && (digit <= 9))) {
                //the value completed
                if (VALUE_READING == true) {
                    break;
                }
                //the value has not been typed
                else {
                    prev_ch = ch_io;
                    continue;
                }
            }
            //if digit char
            else {
                //the fitst digit
                if (VALUE_READING == false) {
                    //mark that the value is typing
                    VALUE_READING = true;
                    //check if the value is negative
                    if (prev_ch == '-') {
                        value_sign = -1;
                    }
                }
                //record the digit of the value
                digits[digit_cnt] = digit;
                digit_cnt++;
                // limit value range: +/- 99,999,999
                if (digit_cnt == 8) {
                    break;
                }
            }

        }

        // calc the value
        int value = 0;//the value that user typed
        for (int i = 0; i < digit_cnt; i++) {
            //the index of the current place
            int i_place = digit_cnt - 1 - i;
            //the digit of the current place
            int place_digit = digits[i_place];
            //calc the weight of the current place
            int place_weight = 1;
            for (int i__ = 0; i__ < i; i__++) {
                place_weight *= 10;
            }
            //add the current place value to the final value
            value += place_digit * place_weight;
        }

        //
        return value * value_sign;
    };

    //
}
