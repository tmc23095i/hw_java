package HW240829;

public class HW240829_5 {
    public static void main_() throws Exception {
        //实于240926

        String retn;

        System.out.println("\n\n");
        retn = simpleTest();
        System.out.println(">> simple: " + retn);

        System.out.println("\n\n");
        retn = finallyTest(new MyClss());
        System.out.println(">> finally: " + retn);

        System.out.println("\n\n");
        retn = multExcpsTest();
        System.out.println(">> multiple: " + retn);

    }

    /**
     * 用于抛出一个异常
     * @param b 是否抛出异常
     * @throws Exception
     */
    private static void throwException(boolean b) throws Exception {
        if (b == true) {
            throw new Exception("excp");
        }
    }

    // ... 最简单的try-catch用法
    private static String simpleTest() {
        String s;

        try { // 将可能抛出异常的代码放在try块中
            throwException(true);
        } catch (Exception e) { // 将对异常进程处理的代码放在catch块中
            System.out.println("Exception: " + e.getMessage());
            s = "FAILED";
            return s;
        }

        // try-catch后的代码, 在执行try或catch块后, 如果没有返回则从此往下继续执行
        s = "SUCCESS";
        return s;
    }

    // ... finally分支的用法

    static class MyClss {
        String name;

        public MyClss() {
        }

        public MyClss(String s) {
            name = s;
        }

    }

    private static String finallyTest(MyClss myclss) {
        String retn;
        String orgName = myclss.name;

        String testName = null;
        myclss.name = testName;
        try {
            myclss.name.length();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            retn = "FAILED";
            return retn;
        } finally {
            // 如果异常计划函数中断, 则将try和catch执行后共用的代码放在finally块里
            // finally块将在try或catch块执行后且函数返回前执行, try或catch中的返回值已被临时保存, 因此finally块不会改变返回值
            myclss.name = orgName;
            retn = "FINALLY";
        }

        // other code...
        retn = "SUCCESS";
        return retn;
    }

    // ... 处理多个异常的用法
    static class MyException extends Exception {

    }

    private static void throwMyExcp(boolean isMyExcp) throws Exception, MyException {
        if (isMyExcp == true) {
            throw new MyException();
        } else {
            throw new Exception();
        }
    }

    private static String multExcpsTest() {
        String retn = "SUCCESS";

        try {
            throwMyExcp(false);
            throwMyExcp(true);
        } catch (MyException myE) { // 处理子类异常 (子类catch块必须位于父类catch块之前)
            retn = "MyExcp";
            return retn;
        } catch (Exception e) { // 处理父类异常
            retn = "Exception";
            return retn;
        }

        return retn;
    }

}
