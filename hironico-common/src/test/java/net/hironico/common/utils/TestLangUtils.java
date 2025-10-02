package net.hironico.common.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TestLangUtils {

    private static final Logger LOG = Logger.getLogger(TestLangUtils.class.getName());

    @Test
    public void testClassList() {
        try {
            List<Class<?>> list = LangUtils.getClasses("net.hironico.common.utils");

            assertNotNull(list);
            assertFalse("Class list is empty", list.isEmpty());

            list.stream().map(Class::getName).forEach(LOG::info);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Cannot test class list.", ex);
            fail(ex.getMessage());
        }
    }
}