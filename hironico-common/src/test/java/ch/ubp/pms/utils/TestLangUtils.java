package ch.ubp.pms.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

public class TestLangUtils {

    @Test
    public void testClassList() {
        try {
            List<Class<? extends Object>> list = LangUtils.getClasses("ch.ubp.pms.utils");

            assertNotNull(list);
            assertFalse("Class list is empty", list.isEmpty());

            list.forEach(clazz -> {
                System.out.println(clazz.getName());
            });            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
}