package com.example.photogallery;

import android.util.Log;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AspectLogging {
    @Before("checkPermission()")
    public void LogPermission() {
        Log.d("LOGGING", "________________________________________________________________________________________\"");
    }
}
