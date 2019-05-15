package com.wy.rpc.common.util;

import java.lang.annotation.Annotation;

/**
 * @Author: 0x4096.peng@gmail.com
 * @Project: Wy-Rpc
 * @DateTime: 2019/5/11 23:39
 * @Description: 注解工具类,用于判断某个类是否含有某个注解
 */
public class AnnotationUtil {


    /**
     * 判断某个类是否含有某个注解
     *
     * @param clazz
     * @param annotationClass
     * @return
     */
    public static boolean isAnnotation(Class<?> clazz, Class annotationClass){
        Annotation annotation = clazz.getAnnotation(annotationClass);
        return annotation != null;
    }


}
