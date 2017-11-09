import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * save操作的合并实体类方法，
 * 查询后把查询结果作为第一个参数传入，
 * 后续需要更新的存在null值的对象当做第二个参数传入
 * @author lhr
 * @create 2017/11/9
 */
public class SaveBeanUtil<T> {

    /**
     * 合并两个对象，同class
     * @param o1    就得对象
     * @param o2    新的更新的对象.
     * @return      合并的实体类
     * @throws NoSuchMethodException
     */
    public static Object fuseBeans(Object o1, Object o2) throws NoSuchMethodException {
        Class classes = o1.getClass();

        //获取所有属性
        Method[] methods = classes.getDeclaredMethods();

        List<Method> getter = new ArrayList<>(), setter =  new ArrayList<>();

        for (Method method : methods){
            //筛选getter setter

            if (method.getName().length() <= 3){
                continue;
            }

            String topThreeStr = method.getName().substring(0,3);
            if ("get".equals(topThreeStr)){
                getter.add(method);
            }
            if ("set".equals(topThreeStr)){
                setter.add(method);
            }
        }


        List<String> notUpdateFields = new ArrayList<>();

        for (Method method : getter){
            try {
                //todo: 如果有需要再次加入判断条件
                if ((method.getReturnType() == String.class
                        || method.getReturnType() == Date.class
                        || method.getReturnType() == Timestamp.class
                        || method.getReturnType() == java.sql.Date.class
                        || method.getReturnType() == Integer.class)
                        && method.invoke(o2) == null) {
                    String setterName = method.getName().substring(3,method.getName().length());
                    notUpdateFields.add(setterName);
                }

                if (method.getReturnType() == int.class
                        && Integer.parseInt(String.valueOf(method.invoke(o2))) == 0) {
                    String setterName = method.getName().substring(3,method.getName().length());
                    notUpdateFields.add(setterName);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }


        for (Method method : setter){
            int continuenum = 0;
            for (String notUpdateField : notUpdateFields){
                if (("set"+notUpdateField).equals(method.getName())){
                    continuenum = 1;
                    continue;
                }
            }
            if (continuenum == 1){
                continue;
            }
            String methorName = method.getName().substring(3,method.getName().length());
            Method get = o1.getClass().getMethod("get"+methorName);
            try {
                method.invoke(o1,get.invoke(o2));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }



        return o1;
    }






//    public static void main(String[] args) {
////        this is a demo
//        try {
//            WxpUser o1 = new WxpUser();
//            WxpUser o2 = new WxpUser();
//
//            o2.setId(1);
//
//            o2.setUserPassword("213");
//
//            o1.setUserName("123nnn");
//
//            System.out.println(SaveBeanUtil.fuseBeans(o1, o2));
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//    }


}

