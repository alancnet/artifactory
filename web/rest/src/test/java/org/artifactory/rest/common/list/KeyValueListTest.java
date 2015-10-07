package org.artifactory.rest.common.list;

import com.google.common.collect.Lists;
import org.artifactory.descriptor.property.Property;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Shay Yaakov
 */
public class KeyValueListTest {

    @Test
    public void testToStringMap() throws Exception {
        Map<String, List<String>> map = new KeyValueList("a=1,2,3|b\\=1=1\\=1,2\\,3|c=").toStringMap();
        assertEquals(map.get("a"), Lists.newArrayList("1", "2", "3"));
        assertEquals(map.get("b=1"), Lists.newArrayList("1=1", "2,3"));
        assertEquals(map.get("c"), null);
    }

    @Test
    public void testToPropertyMap() throws Exception {
        Map<Property, List<String>> map = new KeyValueList("a\\,1=1\\=1,2\\,3,4\\5|b=1\\|1,2\\3|c=").toPropertyMap();
        assertEquals(map.get(new Property("a,1")), Lists.newArrayList("1=1", "2,3", "4\\5"));
        assertEquals(map.get(new Property("b")), Lists.newArrayList("1|1", "2\\3"));
        assertEquals(map.get(new Property("c")), Lists.newArrayList(""));
    }
}
