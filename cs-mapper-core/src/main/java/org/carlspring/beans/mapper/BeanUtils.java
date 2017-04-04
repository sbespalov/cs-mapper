package org.carlspring.beans.mapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;

public class BeanUtils
{

    private static Logger logger = Logger.getLogger(BeanUtils.class.getCanonicalName());

    public static List wrapCollection(final Class targetClass,
                                      final Class decoratorClass,
                                      Collection targetCollection)
    {
        if (targetCollection == null)
        {
            return new ArrayList();
        }
        Class[] interfaces = new Class[] {
                                           Transformer.class
        };
        InvocationHandler h = new InvocationHandler()
        {

            public Object invoke(
                                 Object proxy,
                                 Method method,
                                 Object[] args)
                throws Throwable
            {
                if (method.getName().equals("transform"))
                {
                    Object decorator = decoratorClass.newInstance();
                    decoratorClass.getMethod("setTarget", new Class[] {
                                                                        targetClass
                    }).invoke(decorator, args[0]);
                    return decorator;
                }
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
                    return proxy == args[0];
                }
                throw new UnsupportedOperationException(method.toString());
            }
        };
        ClassLoader classLoader = Transformer.class.getClassLoader();
        Transformer transformer = (Transformer) Proxy.newProxyInstance(classLoader, interfaces, h);
        return (List) CollectionUtils.collect(targetCollection, transformer);
    }

    public static List unwrapCollection(
                                        final Class targetClass,
                                        final Class decoratorClass,
                                        Collection decoratedCollection)
    {
        Class[] interfaces = new Class[] {
                                           Transformer.class
        };
        InvocationHandler h = new InvocationHandler()
        {

            public Object invoke(
                                 Object proxy,
                                 Method method,
                                 Object[] args)
                throws Throwable
            {
                if (method.getName().equals("transform"))
                {
                    Object result = decoratorClass.getMethod("getTarget").invoke(args[0]);
                    if (!targetClass.isAssignableFrom(result.getClass()))
                    {
                        throw new ClassCastException(targetClass.toString());
                    }
                    return result;
                }
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
                    return proxy == args[0];
                }
                throw new UnsupportedOperationException(method.toString());
            }
        };
        ClassLoader classLoader = Transformer.class.getClassLoader();
        Transformer transformer = (Transformer) Proxy.newProxyInstance(classLoader, interfaces, h);
        return (List) CollectionUtils.collect(decoratedCollection, transformer);
    }

    public static PropertyDescriptor findPropertyDescriptor(
                                                            Class<?> targetClass,
                                                            String targetProperty)
        throws PropertyNotFoundException
    {
        PropertyDescriptor result = null;
        StringTokenizer tokenizer = new StringTokenizer(targetProperty, ".");
        Class<?> nesteedClass = targetClass;
        outer_loop: while (tokenizer.hasMoreTokens())
        {
            String nesteedProperty = tokenizer.nextToken();
            List<PropertyDescriptor> propertyDescriptors;
            try
            {
                propertyDescriptors = BeanUtils.getPropertyDescriptors(nesteedClass);
            }
            catch (IntrospectionException e)
            {
                throw new RuntimeException("Failed to extract bean info: beanClass-[" + nesteedClass + "].", e);
            }
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
            {
                if (propertyDescriptor.getName().equals(nesteedProperty))
                {
                    nesteedClass = propertyDescriptor.getPropertyType();
                    result = propertyDescriptor;
                    continue outer_loop;
                }
            }
            throw new PropertyNotFoundException("Property not found: property-[" + targetProperty + "]; class-["
                    + targetClass + "]");
        }
        return result;
    }

    public static String extractPopertyName(
                                            Method method)
    {
        String methodName = method.getName();
        if (methodName.startsWith("is"))
        {
            return StringUtils.uncapitalize(methodName.substring(2));
        }
        if (methodName.startsWith("get"))
        {
            return StringUtils.uncapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("set"))
        {
            return StringUtils.uncapitalize(methodName.substring(3));
        }
        return methodName;
    }

    public static boolean isGetter(
                                   Method method)
    {
        String methodName = method.getName();
        return methodName.startsWith("is") || methodName.startsWith("get");
    }

    public static boolean isSetter(
                                   Method method)
    {
        String methodName = method.getName();
        return methodName.startsWith("set");
    }

    public static List<PropertyDescriptor> getPropertyDescriptors(
                                                                  Class<?> parameterType)
        throws IntrospectionException
    {
        List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
        BeanInfo beanInfo = Introspector.getBeanInfo(parameterType, parameterType.isInterface() ? null : Object.class);
        propertyDescriptors.addAll(Arrays.asList(beanInfo.getPropertyDescriptors()));
        if (parameterType.isInterface())
        {
            for (Class clazz : parameterType.getInterfaces())
            {
                propertyDescriptors.addAll(getPropertyDescriptors(clazz));
            }
        }
        return propertyDescriptors;
    }

    public static Object getProperty(
                                     Object object,
                                     String propertyName)
        throws NoSuchMethodException
    {
        if (object == null)
        {
            return null;
        }
        else if (Map.class.isAssignableFrom(object.getClass()))
        {
            return ((Map) object).get(propertyName);
        }
        else
        {
            try
            {
                return PropertyUtils.getProperty(object, propertyName);
            }
            catch (NestedNullException e)
            {
                return null;
            }
            catch (NoSuchMethodException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to get property value: object-[" + object + "]; proeprtyName-["
                        + propertyName + "]", e);
            }
        }
    }

    public static void setProperty(
                                   Object object,
                                   String propertyName,
                                   Object value)
    {
        if (Map.class.isAssignableFrom(object.getClass()))
        {
            ((Map) object).put(propertyName, value);
        }
        else
        {
            try
            {
                PropertyUtils.setProperty(object, propertyName, value);
            }
            catch (NoSuchMethodException e)
            {
                logger.log(Level.WARNING, "Failed to set property value. Probably property is read only.", e);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to set property value: object-[" + object + "]; proeprtyName-["
                        + propertyName + "]; propertyValue-[" + value + "]", e);
            }
        }
    }

    public static <T> T createInstance(
                                       Class<T> type)
    {
        if (Collection.class.isAssignableFrom(type))
        {
            return createCollectionInstance(type);
        }
        else if (Map.class.isAssignableFrom(type))
        {
            return createMapInstance(type);
        }
        else
        {
            return createBeanInstance(type);
        }
    }

    public static <T> T createBeanInstance(
                                           Class<T> targetClass)
    {
        if (Map.class.isAssignableFrom(targetClass))
        {
            return (T) new HashMap<String, Object>();
        }
        if (targetClass.isInterface())
        {
            return createBeanProxyInstance(targetClass);
        }
        try
        {
            return targetClass.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to instanciate bean: class-[" + targetClass + "]", e);
        }
    }

    public static <T> T createBeanProxyInstance(
                                                Class<T> targetClass)
    {
        Class<T> beanProxyClass = (Class<T>) Proxy.getProxyClass(targetClass.getClassLoader(), targetClass);
        try
        {
            return beanProxyClass.getConstructor(new Class[] {
                                                               InvocationHandler.class
            }).newInstance(new Object[] {
                                          new BeanInvocationHandler()
            });
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create bean proxy instance: beanClass-[" + targetClass + "]", e);
        }
    }

    public static <T> T createCollectionInstance(
                                                 Class<T> collectionType)
    {
        if (Collection.class.isAssignableFrom(collectionType))
        {
            try
            {
                if (collectionType.isInterface())
                {
                    if (List.class.isAssignableFrom(collectionType))
                    {
                        return (T) ArrayList.class.newInstance();
                    }
                    else if (Set.class.isAssignableFrom(collectionType))
                    {
                        return (T) new HashSet();
                    }
                    else
                    {
                        return (T) new ArrayList();
                    }
                }
                else
                {
                    return (T) collectionType.newInstance();
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to instanciate collection of type-[" + collectionType + "].");
            }
        }
        throw new RuntimeException("Failed to instanciate collection of type-[" + collectionType + "].");
    }

    public static <T> T createMapInstance(
                                          Class<T> mapType)
    {
        try
        {
            if (mapType.isInterface())
            {
                return (T) HashMap.class.newInstance();
            }
            else
            {
                return (T) mapType.newInstance();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to instanciate collection of type-[" + mapType + "].");
        }
    }

    public static Class[] extractGenericTypeArguments(
                                                      Method method,
                                                      int i)
    {
        List<Class> result = new ArrayList<Class>();
        Type genericType = null;
        if (i < 0)
        {
            genericType = method.getGenericReturnType();
        }
        else
        {
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            if (i >= genericParameterTypes.length)
            {
                throw new RuntimeException("Failed to extract generic type arguments: method-[" + method
                        + "]; argument-[" + i + "]");
            }
            genericType = genericParameterTypes[i];
        }
        if (ParameterizedType.class.isAssignableFrom(genericType.getClass()))
        {
            Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            for (Type type : typeArguments)
            {
                if (Class.class.isAssignableFrom(type.getClass()))
                {
                    result.add((Class) type);
                }
                else if (ParameterizedType.class.isAssignableFrom(type.getClass()))
                {
                    result.add((Class) ((ParameterizedType) type).getRawType());
                }
                else
                {
                    logger.log(Level.SEVERE,
                               "Failed to extract type arguments class: method-[" + method + "]; genericArgument-["
                                       + type + "]");
                    result.add(Object.class);
                }
            }
        }
        return result.toArray(new Class[] {});
    }

}
