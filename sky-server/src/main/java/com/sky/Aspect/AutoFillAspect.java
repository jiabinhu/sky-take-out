package com.sky.Aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution (* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知进行公共字段的赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段的填充...");
        //获得方法签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获得方法上的注解
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //获得注解上的操作类型
        OperationType operationType = autoFill.value();
        //获得当前目标方法的参数
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
           return;
       }
        //实体对象
        Object entity = args[0];
        //准备赋值的数据
        LocalDateTime time = LocalDateTime.now();
        long empId = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT){
            //当前执行的是insert操作，为4个字段赋值
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);

                //通过反射调用目标对象的方法
                setCreateTime.invoke(entity,time);
                setCreateUser.invoke(entity,empId);
                setUpdateTime.invoke(entity,time);
                setUpdateUser.invoke(entity,empId);
            } catch (Exception ex) {
                log.error("公共字段自动填充失败：{}",ex.getMessage());
            }
        }
        else{
            //当前执行的是update操作，为2个字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,LocalDateTime.class);
                //通过反射调用目标对象的方法
                setUpdateTime.invoke(entity,time);
                setUpdateUser.invoke(entity,empId);
            } catch (Exception ex) {
                //TODO 有个error情况
                log.error("公共字段自动填充失败：{}",ex.getMessage());
            }
        }
        }


}
