package com.example.photogallery;

import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;

import java.io.File;

@Aspect
public class AspectLogging {

    //After createImageFile() is called, log the name and directory of file added to images
    @After("execution(File *.createImageFile(..)) && args(filePath)")
    public void afterCreateImageFile(File filePath) {
        Log.d("FILE CREATED", filePath.toString());
    }

    //@AfterReturning("execution(* *.getPhoto(..))")
    @AfterReturning(pointcut="execution(* *.getPhoto(..))", returning="returnValue")
    public void logGetPhotoReturn(String returnValue) {
        Log.d("GOT PHOTO", returnValue);
    }
}
