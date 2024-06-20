package HW240620;

import HW240620.animal.Animal;
import HW240620.animal.Dog;

public class HW240620_4 {
    public static void main_(String[] args) throws Exception {

        //-- 访问权限修饰符
        //public 所有
        //default 同包
        //protected 子类
        //private 自己

        //-- 上下转型
        // 子类才能向上转型
        // 由子类向上转型来的才能向下转型
        // 子类的实际数据是囊括了父类类型的数据的 所以子类可以向上转型
        // 但是父类的数据是无法凭空创造出子类需要而父类没有的数据的 所以原生父类无法向下转型
        // 上下转型不过是以此时类类型的接口去访问读写实际原生类的数据罢了

        //-- this.getClass()
        // 类的相关信息应该是被java作为类的局部字段储存了
        // 在类实例化的时候记录了这些信息
        // 因为即使向上下转型 信息也不会改变 依旧是初始化时的类信息
        // 同样的子类调用父类的此方法时得到的依旧是子类的信息

        System.err.println();
        Animal a = new Animal("petto");
        a.whoAmI();
        a.eat();
        a.sleep();

        System.err.println();
        Dog d = new Dog("inu~", 1, 20, 35, 3);
        d.whoAmI();
        d.eat("bone");
        d.sleep(5);
        d.getWeight();
        d.getHeight();
        d.getGender();
        d.eatPoop();

        System.err.println();
        Animal dog_aninal = new Dog("chakko", 99);
        dog_aninal.whoAmI();
        ((Dog) dog_aninal).whoAmI();
        ((Dog) dog_aninal).eatPoop();

    }
}
