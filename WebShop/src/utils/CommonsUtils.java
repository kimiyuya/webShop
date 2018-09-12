package utils;

import java.util.UUID;

public class CommonsUtils {

    //生成id
    public static String getUUID(){
        return UUID.randomUUID().toString();
    }
}
