package org.carlspring.beans.utils.doclet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.carlspring.beans.utils.AnnotationDescriptor;
import org.carlspring.beans.utils.BeanDescriptor;
import org.carlspring.beans.utils.BeanProperty;
import org.carlspring.beans.utils.DomainDtoGenerator;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

/**
 * This Doclet prepare context with JPA Entities metadata, on the basis of
 * which the Domain DTO code is generated.
 * 
 * @author Sergey Bespalov
 * 
 * @see DomainDtoGenerator
 */
public class DomainDtoDoclet
{

    private static final String TAG_DOMAIN_EXCLUDE = "@domainExclude";
    private static final String TAG_DOMAIN_PROERTY = "@domainProperty";
    private static final String DTO_SUFFIX = "";
    private static final String DTO_PREFIX = "";
    private static final String BASE_PACKAGE = "-basepackage";
    private static final String OUTPUT_FOLDER = "-outputfolder";
    private static final String PREFEXED_PROPERTIES = "-prefexedproperties";
    private static final String EXCLUDED_PROPERTIES = "-excludedproperties";

    private static final Set<String> skipAnnotations = new HashSet<String>();
    private static final Set<String> nesteedAnnotations = new HashSet<String>();

    static
    {
        skipAnnotations.add("javax.persistence.Transient");
        skipAnnotations.add("javax.persistence.OneToMany");

        nesteedAnnotations.add("javax.persistence.OneToOne");
        nesteedAnnotations.add("javax.persistence.ManyToOne");
        nesteedAnnotations.add("org.hibernate.annotations.Type");
    }

    public static int optionLength(String option)
    {
        if (option.equals(OUTPUT_FOLDER))
        {
            return 2;
        }
        if (option.equals(BASE_PACKAGE))
        {
            return 2;
        }
        if (option.equals(PREFEXED_PROPERTIES))
        {
            return 2;
        }
        if (option.equals(EXCLUDED_PROPERTIES))
        {
            return 2;
        }
        return 0;
    }

    private static String getOutputFolder(String[][] options)
    {
        for (int i = 0; i < options.length; i++)
        {
            String[] opt = options[i];
            if (opt[0].equals(OUTPUT_FOLDER))
            {
                return opt[1];
            }
        }
        return ".";
    }

    private static String getBasePackage(String[][] options)
    {
        for (int i = 0; i < options.length; i++)
        {
            String[] opt = options[i];
            if (opt[0].equals(BASE_PACKAGE))
            {
                return opt[1];
            }
        }
        return "";
    }

    private static Set<String> getPrefexedProperties(String[][] options)
    {
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < options.length; i++)
        {
            String[] opt = options[i];
            if (opt[0].equals(PREFEXED_PROPERTIES))
            {
                StringTokenizer tokenizer = new StringTokenizer(opt[1], ":");
                while (tokenizer.hasMoreTokens())
                {
                    result.add(tokenizer.nextToken().trim());
                }
            }
        }
        return result;
    }

    private static Set<String> getExcludedProperties(String[][] options)
    {
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < options.length; i++)
        {
            String[] opt = options[i];
            if (opt[0].equals(EXCLUDED_PROPERTIES))
            {
                StringTokenizer tokenizer = new StringTokenizer(opt[1], ":");
                while (tokenizer.hasMoreTokens())
                {
                    String token = tokenizer.nextToken().trim();
                    result.add(token);
                }
            }
        }
        return result;
    }

    public static boolean start(RootDoc root)
        throws Throwable
    {
        String outputFolder = getOutputFolder(root.options());
        File file = new File(outputFolder);
        file.mkdirs();
        String basePackage = getBasePackage(root.options());
        Set<String> prefexedProperties = getPrefexedProperties(root.options());
        Set<String> excludedProperties = getExcludedProperties(root.options());
        List<BeanDescriptor> beanDescriptors = new ArrayList<BeanDescriptor>();
        for (ClassDoc classDoc : root.classes())
        {
            boolean isEntity = false;
            for (AnnotationDesc annotationDesc : classDoc.annotations())
            {
                String annotationType = annotationDesc.annotationType().qualifiedName();
                if (annotationType.equals("javax.persistence.Entity"))
                {
                    isEntity = true;
                }
            }
            if (!isEntity)
            {
                continue;
            }
            String entityName = classDoc.simpleTypeName().replace("Entity", "");
            Map<String, BeanProperty> beanProperties = new HashMap<String, BeanProperty>();
            List<MethodDoc> methods = new ArrayList<MethodDoc>();
            ClassDoc localClassDoc = classDoc;
            while (!localClassDoc.qualifiedName().equals("java.lang.Object"))
            {
                methods.addAll(Arrays.asList(localClassDoc.methods()));
                localClassDoc = localClassDoc.superclass();
            }
            outer_loop: for (MethodDoc methodDoc : methods)
            {
                String propertyName = extractPropertyName(methodDoc);
                if (!isGetter(methodDoc) || beanProperties.keySet().contains(propertyName))
                {
                    continue;
                }
                if (excludedProperties.contains(propertyName))
                {

                    continue;
                }
                if (methodDoc.tags("org.carlspring.beans.utils.doclet.DomainDtoDoclet.skipProperty").length > 0)
                {
                    beanProperties.put(propertyName, null);
                    continue outer_loop;
                }
                BeanProperty beanProperty = new BeanProperty();
                beanProperties.put(propertyName, beanProperty);
                AnnotationDesc[] annotations = methodDoc.annotations();
                boolean isNesteed = false;
                for (AnnotationDesc annotationDesc : annotations)
                {
                    String annotationType = annotationDesc.annotationType().qualifiedName();
                    if (skipAnnotations.contains(annotationType))
                    {
                        beanProperties.put(propertyName, null);
                        continue outer_loop;
                    }
                    if (nesteedAnnotations.contains(annotationType))
                    {
                        isNesteed = true;
                        break;
                    }
                }
                AnnotationDescriptor annotation = new AnnotationDescriptor();
                annotation.setClassName("org.carlspring.beans.mapper.markup.MappedProperty");
                annotation.getAttributes().put("value",
                                               Arrays.asList(new String[] { "\"" + propertyName + "\"" }));
                beanProperty.getAnnotations().add(annotation);
                Tag[] tags = methodDoc.tags();
                for (Tag tag : tags)
                {
                    if (TAG_DOMAIN_EXCLUDE.equals(tag.name()))
                    {
                        beanProperties.put(propertyName, null);
                        continue outer_loop;
                    }
                    if (TAG_DOMAIN_PROERTY.equals(tag.name()))
                    {
                        String text = tag.text();
                        Matcher m = Pattern.compile("([a-zA-Z&&[^=]]+)=\"([^ ]*)\"").matcher(text);
                        while (m.find())
                        {
                            String paramName = m.group(1);
                            String paramValue = m.group(2);
                            if ("name".equals(paramName.trim()))
                            {
                                propertyName = paramValue.trim();
                            }
                        }
                    }
                }
                if (isNesteed)
                {
                    beanProperty.setType(Long.class.getName());
                    propertyName += "Id";
                }
                else
                {
                    beanProperty.setType(methodDoc.returnType().qualifiedTypeName());
                }
                if (prefexedProperties.contains(propertyName))
                {
                    propertyName = StringUtils.uncapitalize(entityName) + StringUtils.capitalize(propertyName);
                }

                beanProperty.setName(propertyName);
            }
            BeanDescriptor beanDescriptor = new BeanDescriptor();
            beanDescriptor.setClassName(DTO_PREFIX + entityName + DTO_SUFFIX);
            beanDescriptor.setPackageName(basePackage);
            AnnotationDescriptor annotation = new AnnotationDescriptor();
            annotation.setClassName("org.carlspring.beans.mapper.markup.MappedBean");
            annotation.getAttributes().put("value",
                                           Arrays.asList(new String[] { classDoc.qualifiedName() + ".class" }));
            beanDescriptor.getAnnotations().add(annotation);
            for (BeanProperty beanProperty : beanProperties.values())
            {
                if (beanProperty != null)
                {
                    beanDescriptor.getProperties().add(beanProperty);
                }
            }
            beanDescriptors.add(beanDescriptor);
        }
        DomainDtoGenerator generator = new DomainDtoGenerator();
        generator.setOutput(outputFolder);
        generator.processGenerate(basePackage, beanDescriptors);
        return true;
    }

    private static boolean isGetter(MethodDoc methodDoc)
    {
        String methodName = methodDoc.name();
        return methodName.startsWith("is") || methodName.startsWith("get");
    }

    private static String extractPropertyName(MethodDoc methodDoc)
    {
        String methodName = methodDoc.name();
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

}
