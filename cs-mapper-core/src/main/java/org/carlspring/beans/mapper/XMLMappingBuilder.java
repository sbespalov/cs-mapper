package org.carlspring.beans.mapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <pre class="code">
 * 	&ltbean id="beanHelper"
 *		class="ru.diasoft.fa.xdscall.beans.spring.BeanHelperFactoryBean"&gt
 *		&ltproperty name="mappingLocations"&gt
 *			&ltlist&gt
 *				&ltvalue&gtbeanMappings.xml&lt/value&gt
 *			&lt/list&gt
 *		&lt/property&gt
 *	&lt/bean&gt
 * </pre>
 *
 * @author Sergey Bespalov
 */
public class XMLMappingBuilder implements MappingBuilder
{

    private static Logger logger = Logger.getLogger(XMLMappingBuilder.class.getCanonicalName());

    private String mappingsLocation;

    private ClassLoader classLoader;

    private MappingProfile mappingProfile;

    public XMLMappingBuilder(MappingProfile mappingProfile,
                             String mappingsLocation)
    {
        this(mappingProfile, null, mappingsLocation);
    }

    public XMLMappingBuilder(MappingProfile mappingProfile,
                             ClassLoader classLoader,
                             String mappingsLocation)
    {
        super();
        this.classLoader = classLoader;
        this.mappingsLocation = mappingsLocation;
        this.mappingProfile = mappingProfile;
    }

    public String getMappingsLocation()
    {
        return mappingsLocation;
    }

    public void setMappingsLocation(String mappingsLocation)
    {
        this.mappingsLocation = mappingsLocation;
    }

    public ClassLoader getClassLoader()
    {
        if (classLoader == null)
        {
            return getClass().getClassLoader();
        }
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    protected Digester getDigester()
        throws SAXException
    {
        Digester digester = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        // SAXParser parser = null;
        // try {
        // factory.setFeature("http://apache.org/xml/features/validation/schema",
        // true);
        // factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking",
        // true);
        //
        // parser = factory.newSAXParser();
        // parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
        // getClass().getResource("/commons-serviceutils/src/main/resources/ru/diasoft/fa/gl/commons/serviceutils/beanmapper/bean-mappings.xsd")
        // .toString());
        // } catch (ParserConfigurationException e) {
        // throw new SAXException(e);
        // }
        // digester = new Digester(parser);
        digester = new Digester();
        digester.setErrorHandler(new ErrorHandler()
        {

            public void error(SAXParseException exception)
                throws SAXException
            {
                throw new SAXException(exception);
            }

            public void fatalError(SAXParseException exception)
                throws SAXException
            {
                throw new SAXException(exception);
            }

            public void warning(SAXParseException exception)
                throws SAXException
            {
                throw new SAXException(exception);
            }
        });
        return digester;
    }

    public List<BeanMapping> buildMappings()
    {
        List<BeanMapping> result = new ArrayList<BeanMapping>();
        try
        {
            Digester digester = getDigester();
            digester.addRule("bean-mappings", new BeanMappingsRule());
            digester.addRule("*/bean-mapping", new BeanMappingRule());
            digester.addRule("*/property-mapping", new PropertyMappingRule());
            digester.addRule("*/default-property-mappings", new DefaultBeanMappingsRule());
            InputStream resource = getClassLoader().getResourceAsStream(mappingsLocation);
            result.addAll((List) digester.parse(resource));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Class getBeanClass(String bean)
    {
        try
        {
            return getClassLoader().loadClass(bean);
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Unable to load class [" + bean + "]", e);
        }
    }

    private class BeanMappingsRule extends Rule
    {

        public void begin(String namespace,
                          String name,
                          Attributes attributes)
            throws Exception
        {
            try
            {
                getDigester().push(new ArrayList<BeanMapping>());
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, "Failed to build mappings from XML.", e);
                throw e;
            }
        }

    }

    public class BeanMappingRule extends Rule
    {

        public void begin(String namespace,
                          String name,
                          Attributes attributes)
            throws Exception
        {
            try
            {
                String srcBean = attributes.getValue("aClass");
                String targetBean = attributes.getValue("bClass");
                String mappingId = attributes.getValue("id");
                if (srcBean == null || "".equals(srcBean))
                {
                    throw new RuntimeException("The 'aClass' attribute is required for a <bean-mapping> element");
                }
                if (targetBean == null || "".equals(targetBean))
                {
                    throw new RuntimeException("The 'bClass' attribute is required for a <bean-mapping> element");
                }
                SimpleMappingBuilder mappingBuilder = new SimpleMappingBuilder(mappingProfile, getBeanClass(srcBean),
                        getBeanClass(targetBean));
                if (mappingId != null && mappingId.trim().length() > 0)
                {
                    mappingBuilder.setMappingId(mappingId);
                }
                getDigester().push(mappingBuilder);
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, "Failed to build mappings from XML.", e);
                throw e;
            }
        }

        public void end(String namespace,
                        String name)
            throws Exception
        {
            try
            {
                MappingBuilder mappingBuilder = (MappingBuilder) getDigester().pop();
                List<BeanMapping> list = (List<BeanMapping>) getDigester().peek();
                list.addAll(mappingBuilder.buildMappings());
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, "Failed to build mappings from XML.", e);
                throw e;
            }
        }

    }

    public class PropertyMappingRule extends Rule
    {

        public void begin(String namespace,
                          String name,
                          Attributes attributes)
            throws Exception
        {
            try
            {
                String srcProperty = attributes.getValue("aProperty");
                String targetProperty = attributes.getValue("bProperty");

                if (srcProperty == null || "".equals(srcProperty))
                {
                    throw new RuntimeException("The 'aProperty' attribute is required for a property-mapping element");
                }
                if (targetProperty == null || "".equals(targetProperty))
                {
                    throw new RuntimeException("the 'bProperty' attribute is required for a property-mapping element");
                }
                SimpleMappingBuilder simpleMappingBuilder = (SimpleMappingBuilder) getDigester().peek();
                simpleMappingBuilder.addCustomMapping(srcProperty, targetProperty);

                String srcPropertyType = attributes.getValue("aType");
                String targetPropertyType = attributes.getValue("bType");
                if (srcPropertyType != null && srcPropertyType.trim().length() > 0)
                {
                    Class<?> type = getClassLoader().loadClass(srcPropertyType.trim());
                    simpleMappingBuilder.addSourceCustomType(srcProperty, type);
                }
                if (targetPropertyType != null && targetPropertyType.trim().length() > 0)
                {
                    Class<?> type = getClassLoader().loadClass(targetPropertyType.trim());
                    simpleMappingBuilder.addTargetCustomType(targetProperty, type);
                }
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, "Failed to build mappings from XML.", e);
                throw e;
            }
        }

    }

    public class DefaultBeanMappingsRule extends Rule
    {

        public void begin(String namespace,
                          String name,
                          Attributes attributes)
            throws Exception
        {
            try
            {
                SimpleMappingBuilder mappingBuilder = (SimpleMappingBuilder) getDigester().peek();
                for (int i = 0; i < attributes.getLength(); i++)
                {
                    String localName = attributes.getLocalName(i);
                    if ("".equals(localName))
                    {
                        // this will happen if we're not namespace-aware....
                        localName = attributes.getQName(i);
                    }

                    if ("excludes".equals(localName))
                    {
                        for (StringTokenizer stringTokenizer = new StringTokenizer(attributes.getValue(i),
                                ","); stringTokenizer.hasMoreTokens();)
                        {
                            mappingBuilder.addExcludeProperty(stringTokenizer.nextToken());
                        }
                    }
                }
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, "Failed to build mappings from XML.", e);
                throw e;
            }
        }

    }
}
