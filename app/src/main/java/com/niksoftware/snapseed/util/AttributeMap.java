package com.niksoftware.snapseed.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class AttributeMap {
    private final Map<String, String> values;

    public static AttributeMap fromXml(XmlPullParser parser) {
        Map<String, String> values = new HashMap();
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            values.put(parser.getAttributeName(i), parser.getAttributeValue(i));
        }
        return new AttributeMap(Collections.unmodifiableMap(values));
    }

    private AttributeMap(Map<String, String> values) {
        this.values = values;
    }

    public boolean hasAttribute(String key) {
        return getAttributeValue(key) != null;
    }

    public String getAttributeValue(String key) {
        return (String) this.values.get(key);
    }
}
