# Introdution

There is a common view, that the **_DTO (Data Transfer Object)_**  is an anti-pattern, but **_CSMapper_** targets to dispel this prejudice.

If we imagine a standard **_JavaEE_** application, then, as a rule, it has a "Layered architecture" with an API on top (which is **_Presensation Layer_**), and JPA domain model at the bottom (which is **_Persistence Layer_**). Also we can say that API parameters depends on **_JPA Entity Model_** at most, and is a kind of its projection. So here is the dummy copy paste come from, which is the reason that makes DTO called ~~antipattern~~. But this copy paste problem can be very simply solved, which is what the **_CSMapper_** makes, and turn the DTO back into a very cool pattern.

We can divide the solution into three parts:
* generate plane POJOs, based on JPA Entity model;
* construct API parameters with those plane POJOs;
* merge values from Entity to POJO and vice versa;

That's it! 

There are usage examples below, which will help to make some details clearer.

# Usage

All features, that **_CSMapper_** provides, can be used out of the box with well known Java technology stack, such as Spring and Hibernate. The configuration is very simple and takes about 15 minutes:

#### 1. Generate **_JPA Entity_** based **_POJOs_**

The code generation process occurs during the project build. This is done using the Maven plugin:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-javadoc-plugin</artifactId>
            <executions>
                <execution>
                    <id>domaindto</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>javadoc</goal>
                    </goals>
                    <configuration>
                        <doclet>org.carlspring.beans.utils.doclet.DomainDtoDoclet</doclet>
                        <subpackages>org.carlspring.beans.mapper.examples.domain</subpackages>
                        <docletArtifacts>
                            <docletArtifact>
                                <groupId>org.carlspring.beans</groupId>
                                <artifactId>cs-mapper-utils</artifactId>
                                <version>1.0</version>
                            </docletArtifact>
                        </docletArtifacts>
                        <additionalparam>
                            -basepackage
                            org.carlspring.beans.mapper.examples.domain
                            -outputfolder
                            ${project.build.directory}/generated-sources
                            -prefexedproperties
                            id,name
                        </additionalparam>
                        <useStandardDocletOptions>false</useStandardDocletOptions>
                        <maxmemory>512</maxmemory>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>build-helper-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>add-source</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>add-source</goal>
                    </goals>
                    <configuration>
                        <sources>
                            <source>${project.build.directory}/generated-sources</source>
                        </sources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
> Note that this configuration must be placed in module, that contains JPA Entities

We using the `DomainDtoDoclet` to get **_JPA Entities_** metadata from `.java` sources. Also there will be a dedicated `cs-mapper-plugin` instead of `maven-javadoc-plugin` and `build-helper-maven-plugin` in future versions.

#### 2. **_BeanMapper_** instace configuration

**_BeanMapper_** can be easily configured inside **_Spring Application Context_** using **_BeanMapperFactoryBean_** like this:

```java
@Configuration
public class AppConfiguration {

   ...

    @Bean
    public BeanMapperFactoryBean beanMapper(EntityManagerFactory emf)
    {
        BeanMapperFactoryBean result = new BeanMapperFactoryBean(emf);
        result.setMappedClasses(Arrays.asList(DomainDtoClasses.domainDtoClasses));
        result.setPackagesToScan("your.application.package.name.api.dto");
        return result;
    }
}

```

#### 3. Merge POJOs with **_JPA Entities_** with **_BeanMapper_**

```java
@Component
public class SampleApiService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BeanMapper beanMapper;

    public PetStoreReadDto getPetStore(Long petStoreId) {
        PetStoreEntity petStoreEntity = entityManager.find(PetStoreEntity.class, petStoreId);  
        PetStoreReadDto petStoreDto = (PetStoreReadDto) beanMapper.convertObject(petStoreEntity, PetStoreReadDto.class);
        return petStoreDto;
    }

}
```

## Common Use Cases

Usage examples can be found [here](https://github.com/sbespalov/cs-mapper/tree/master/cs-mapper-examples)
