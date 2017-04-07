package org.carlspring.beans.mapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class BeanInvocationHandler implements InvocationHandler
{

    private Map<String, Object> map = new HashMap<String, Object>();

    public Object invoke(Object proxy,
                         Method method,
                         Object[] args)
        throws Throwable
    {
        String propertyName = CSBeanUtils.extractPopertyName(method);
        if (CSBeanUtils.isGetter(method))
        {
            Object propertyValue = map.get(propertyName);
            if (propertyValue == null && method.getReturnType().isPrimitive())
            {
                Class<?> returnType = method.getReturnType();
                propertyValue = toPrimitive(propertyValue, returnType);
            }
            return propertyValue;
        }
        else if (CSBeanUtils.isSetter(method))
        {
            Object propertyValue = args[0];
            // TODO: there is no return value for setter methods
            if (propertyValue == null && method.getReturnType().isPrimitive())
            {
                Class<?> returnType = method.getReturnType();
                propertyValue = toPrimitive(propertyValue, returnType);
            }
            map.put(propertyName, propertyValue);
            return null;
        }
        // TODO: method.getDeclaringClass().equals(Object.class) -> method.invoke(proxy, args)
        else if (method.getName().equals("toString"))
        {
            return proxy.getClass().getName() + "@" + Integer.toHexString(hashCode());
        }
        else if (method.getName().equals("hashCode"))
        {
            return hashCode();
        }
        else if (method.getName().equals("equals"))
        {
            return proxy.equals(args[0]);
        }
        throw new RuntimeException("Failed to handle method invocation: method-[" + method + "].");
    }

    private Object toPrimitive(Object result,
                               Class<?> returnType)
    {
        if (boolean.class.isAssignableFrom(returnType))
        {
            result = false;
        }
        else if (short.class.isAssignableFrom(returnType))
        {
            result = (short) 0;
        }
        else if (char.class.isAssignableFrom(returnType))
        {
            result = (char) 0;
        }
        else if (int.class.isAssignableFrom(returnType))
        {
            result = 0;
        }
        else if (long.class.isAssignableFrom(returnType))
        {
            result = (long) 0;
        }
        else if (float.class.isAssignableFrom(returnType))
        {
            result = (float) 0;
        }
        else if (double.class.isAssignableFrom(returnType))
        {
            result = (double) 0;
        }
        return result;
    }

}
