package com.freeteam01.androidnewsgroupreader.ServicesTests;

import android.util.Log;

import org.powermock.core.spi.PowerMockPolicy;
import org.powermock.mockpolicies.MockPolicyClassLoadingSettings;
import org.powermock.mockpolicies.MockPolicyInterceptionSettings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LogRedirection implements PowerMockPolicy {

    @Override
    public void applyClassLoadingPolicy(MockPolicyClassLoadingSettings settings) {
        settings.addFullyQualifiedNamesOfClassesToLoadByMockClassloader(Log.class.getName());
    }

    @Override
    public void applyInterceptionPolicy(MockPolicyInterceptionSettings settings) {
        try {

            // Mock Log.v(String tag, String msg)
            settings.proxyMethod(Log.class.getMethod("v", String.class, String.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    return redirect(logLevel, tag, message, null);
                }
            });

            // Mock Log.v(String tag, String msg, Throwable tr)
            settings.proxyMethod(Log.class.getMethod("v", String.class, String.class, Throwable.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    Throwable throwable = (Throwable) args[2];
                    return redirect(logLevel, tag, message, throwable);
                }
            });

            // Mock Log.d(String tag, String msg)
            settings.proxyMethod(Log.class.getMethod("d", String.class, String.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    return redirect(logLevel, tag, message, null);
                }
            });

            // Mock Log.d(String tag, String msg, Throwable tr)
            settings.proxyMethod(Log.class.getMethod("d", String.class, String.class, Throwable.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    Throwable throwable = (Throwable) args[2];
                    return redirect(logLevel, tag, message, throwable);
                }
            });

            // Mock Log.i(String tag, String msg)
            settings.proxyMethod(Log.class.getMethod("i", String.class, String.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    return redirect(logLevel, tag, message, null);
                }
            });

            // Mock Log.i(String tag, String msg, Throwable tr)
            settings.proxyMethod(Log.class.getMethod("i", String.class, String.class, Throwable.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    Throwable throwable = (Throwable) args[2];
                    return redirect(logLevel, tag, message, throwable);
                }
            });

            // Mock Log.w(String tag, String msg)
            settings.proxyMethod(Log.class.getMethod("w", String.class, String.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    return redirect(logLevel, tag, message, null);
                }
            });

            // Mock Log.w(String tag, String msg, Throwable tr)
            settings.proxyMethod(Log.class.getMethod("w", String.class, String.class, Throwable.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    Throwable throwable = (Throwable) args[2];
                    return redirect(logLevel, tag, message, throwable);
                }
            });

            // Mock Log.w(String tag, Throwable tr)
            settings.proxyMethod(Log.class.getMethod("w", String.class, Throwable.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = getStackTraceString((Throwable) args[1]);
                    return redirect(logLevel, tag, message, null);
                }
            });

            // Mock Log.e(String tag, String msg)
            settings.proxyMethod(Log.class.getMethod("e", String.class, String.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    return redirect(logLevel, tag, message, null);
                }
            });

            // Mock Log.e(String tag, String msg, Throwable tr)
            settings.proxyMethod(Log.class.getMethod("e", String.class, String.class, Throwable.class), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String logLevel = method.getName().toUpperCase();
                    String tag = args[0].toString();
                    String message = args[1].toString();
                    Throwable throwable = (Throwable) args[2];
                    return redirect(logLevel, tag, message, throwable);
                }
            });

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static int redirect(String logLevel, String tag, String message, Throwable throwable) {
        if (throwable == null) {
            System.out.println(String.format("%s - %s: %s", logLevel, tag, message));
        } else {
            System.out.println(String.format("%s - %s: %s", logLevel, tag, message + '\n' + getStackTraceString(throwable)));
        }
        return 0;
    }

    private static String getStackTraceString(Throwable tr) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}