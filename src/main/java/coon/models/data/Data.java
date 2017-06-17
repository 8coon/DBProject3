package coon.models.data;


public class Data {


    public static Object value(Object actualValue, Object defaultValue) {
        return actualValue == null ? defaultValue : actualValue;
    }


    public static String value(String actualValue, String defaultValue) {
        return (String) Data.value((Object) actualValue, (Object) defaultValue);
    }

}
