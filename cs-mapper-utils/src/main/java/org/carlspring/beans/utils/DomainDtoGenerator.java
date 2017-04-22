package org.carlspring.beans.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * This class makes Domain DTO code generation using Apache Velocity
 * templates.<br>
 * There are three types of artifacts produced:<br>
 * - plain interfaces, based on JPA Entities, called Domain DTO Interfaces;<br>
 * - generic DTO classes, which implements Domain DTO Interfaces;<br>
 * - Domain 'DomainDtoClasses' summary class, which contains all generated classes
 * as array constant;<br>
 * 
 * @author Sergey Bespalov
 *
 */
public class DomainDtoGenerator
{

    private static final String TEMPLATE_DTO_INTERFACE = "org/carlspring/beans/utils/vm/domainDtoInterface.vm";
    private static final String TEMPLATE_DTO_CLASS = "org/carlspring/beans/utils/vm/donainDtoBaseClass.vm";
    private static final String TEMPLATE_SUMMARY = "org/carlspring/beans/utils/vm/domainSummary.vm";

    private VelocityEngine velocityEngine;
    private String output = ".";

    public DomainDtoGenerator()
    {
        velocityEngine = new VelocityEngine();
        try
        {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader()
                                      .getResourceAsStream("org/carlspring/beans/utils/vm/velocity.properties"));
            velocityEngine.init(properties);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to construct DomainDtoGenerator.", e);
        }
    }

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    public void processGenerate(BeanDescriptor beanDescriptor)
    {
        processGenerate(beanDescriptor.getPackageName(), Arrays.asList(new BeanDescriptor[] { beanDescriptor }));
    }

    public void processGenerate(String packageName,
                                List<BeanDescriptor> beanDescriptors)
    {
        new File(getOutput()).mkdirs();
        String packagePath = getPackagePath(packageName);
        new File(packagePath).mkdirs();

        for (BeanDescriptor beanDescriptor : beanDescriptors)
        {
            VelocityContext context = new VelocityContext();
            context.put("global", context);
            context.put("stringUtils", new StringUtils());
            context.put("beanDescriptor", beanDescriptor);

            generate(packagePath + beanDescriptor.getClassName() + ".java", TEMPLATE_DTO_INTERFACE, context);
            generate(packagePath + beanDescriptor.getClassName() + "BaseDto.java", TEMPLATE_DTO_CLASS, context);
        }

        VelocityContext context = new VelocityContext();
        context.put("global", context);
        context.put("stringUtils", new StringUtils());
        context.put("packageName", packageName);
        context.put("summaryClassName", "DomainDtoClasses");
        context.put("domainDtoList", beanDescriptors);

        generate(packagePath + "DomainDtoClasses.java", TEMPLATE_SUMMARY, context);
    }

    private String getPackagePath(String packageName)
    {
        return getOutput() + File.separator + packageName.replaceAll("\\.", "\\" + File.separator)
                + File.separator;
    }

    private void generate(String className,
                          String templateName,
                          VelocityContext context)
    {
        FileWriter writer = null;
        try
        {
            writer = doGenerate(className, templateName, context);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to generate class [" + className + "]", e);
        } finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to generate class [" + className + "]",
                            e);
                }
            }
        }
    }

    private FileWriter doGenerate(String className,
                                  String templateName,
                                  VelocityContext context)
        throws IOException
    {
        FileWriter writer;
        File classFile = new File(className);
        System.out.println(className);
        classFile.createNewFile();
        writer = new FileWriter(classFile);
        velocityEngine.mergeTemplate(templateName, context, writer);
        return writer;
    }

}
