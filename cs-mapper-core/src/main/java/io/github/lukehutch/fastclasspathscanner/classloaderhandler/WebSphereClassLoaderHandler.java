package io.github.lukehutch.fastclasspathscanner.classloaderhandler;

import io.github.lukehutch.fastclasspathscanner.classloaderhandler.ClassLoaderHandler;
import io.github.lukehutch.fastclasspathscanner.scanner.ClasspathFinder;
import io.github.lukehutch.fastclasspathscanner.utils.LogNode;
import io.github.lukehutch.fastclasspathscanner.utils.ReflectionUtils;

public class WebSphereClassLoaderHandler implements ClassLoaderHandler
{

    public boolean handle(ClassLoader classloader,
                          ClasspathFinder classpathFinder,
                          LogNode log)
        throws Exception
    {
        for (Class<?> c = classloader.getClass(); c != null; c = c.getSuperclass())
        {
            if ("com.ibm.ws.classloader.CompoundClassLoader".equals(c.getName()))
            {
                final String classpath = (String) ReflectionUtils.invokeMethod(classloader, "getClassPath");
                return classpathFinder.addClasspathElements(classpath, log);
            }
        }
        return false;
    }

}
