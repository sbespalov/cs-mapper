package org.carlspring.beans.mapper.examples.test;

import javax.transaction.Transactional;

import org.carlspring.beans.mapper.examples.CSMapperExamplesConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CSMapperExamplesConfiguration.class)
@Transactional
public class ConfigurationTest
{

    @Test
    public void testConfiguration()
    {
    }

}
