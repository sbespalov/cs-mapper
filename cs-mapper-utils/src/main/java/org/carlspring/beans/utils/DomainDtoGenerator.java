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

public class DomainDtoGenerator
{

    private static final String TEMPLATE_DTO_INTERFACE = "org/carlspring/beans/utils/vm/domainDtoInterface.vm";
    private static final String TEMPLATE_DTO_CLASS = "org/carlspring/beans/utils/vm/donainDtoBaseClass.vm";
    
    private VelocityEngine velocityEngine;
    private String summaryTemplateName = "org/carlspring/beans/utils/vm/domainSummary.vm";
    private String output = ".";

    public DomainDtoGenerator()
    {
        this(null);
    }

    public DomainDtoGenerator(Properties properties)
    {
        velocityEngine = new VelocityEngine();
        try
        {
            if (properties == null)
            {
                properties = new Properties();
                properties.load(getClass().getClassLoader()
                                          .getResourceAsStream("org/carlspring/beans/utils/vm/velocity.properties"));
            }
            velocityEngine.init(properties);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to construct DomainDtoGenerator.", e);
        }
    }

    public VelocityEngine getVelocityEngine()
    {
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine)
    {
        this.velocityEngine = velocityEngine;
    }

    public String getSummaryTemplateName()
    {
        return summaryTemplateName;
    }

    public void setSummaryTemplateName(String summaryTemplateName)
    {
        this.summaryTemplateName = summaryTemplateName;
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
        processGenerate(beanDescriptor.getPackageName(), Arrays.asList(new BeanDescriptor[] {
                                                                                              beanDescriptor
        }));
    }

    public void processGenerate(String packageName,
                                List<BeanDescriptor> beanDescriptors)
    {
        new File(getOutput()).mkdirs();
        String packagePath = getOutput() + File.separator + packageName.replaceAll("\\.", "\\" + File.separator)
                + File.separator;
        new File(packagePath).mkdirs();
        for (BeanDescriptor beanDescriptor : beanDescriptors)
        {
            VelocityContext context = new VelocityContext();
            context.put("global", context);
            context.put("stringUtils", new StringUtils());
            context.put("beanDescriptor", beanDescriptor);
            generate(packagePath + beanDescriptor.getClassName() + ".java", TEMPLATE_DTO_INTERFACE, beanDescriptor, context);
            generate(packagePath + beanDescriptor.getClassName() + "BaseDto.java", TEMPLATE_DTO_CLASS, beanDescriptor, context);
        }
        VelocityContext context = new VelocityContext();
        context.put("global", context);
        context.put("stringUtils", new StringUtils());
        context.put("packageName", packageName);
        context.put("summaryClassName", "DomainDtoClasses");
        context.put("domainDtoList", beanDescriptors);
        FileWriter writer = null;
        try
        {
            File classFile = new File(packagePath + "DomainDtoClasses.java");
            classFile.createNewFile();
            writer = new FileWriter(classFile);
            velocityEngine.mergeTemplate(getSummaryTemplateName(), context, writer);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to generate class [DomainDtoClasses]", e);
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
                    throw new RuntimeException("Failed to generate class [DomainDtoClasses]", e);
                }
            }
        }

    }

    private void generate(String className,
                          String templateName,
                          BeanDescriptor beanDescriptor,
                          VelocityContext context)
    {
        FileWriter writer = null;
        try
        {
            writer = doGenerate(className, templateName, beanDescriptor, context);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to generate class [" + beanDescriptor.getClassName() + "]", e);
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
                    throw new RuntimeException("Failed to generate class [" + beanDescriptor.getClassName() + "]",
                            e);
                }
            }
        }
    }

    private FileWriter doGenerate(String className,
                                  String templateName,
                                  BeanDescriptor beanDescriptor,
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
