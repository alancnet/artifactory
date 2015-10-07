package org.artifactory.storage;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * class extends Properties in order to enable
 * reading and writing properties file in order with comments
 *
 * @author Chen Keinan
 */
public class LinkedProperties extends Properties {

    private static final Logger log = LoggerFactory.getLogger(LinkedProperties.class);
    private LinkedHashMap<String, String> linkedProps = new LinkedHashMap<>();
    private int commentCount;

    public static String toString(String key, String value) {
        if (key.charAt(0) == '#') {
            return value;
        }
        return key + "=" + value;
    }

    @Override
    public String getProperty(String key) {
        return linkedProps.get(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String propKey = linkedProps.get(key);
        if (propKey != null) {
            return propKey;
        }
        return defaultValue;
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        String trimmed = StringUtils.trimToEmpty(value); // nulls are not allowed
        return linkedProps.put(key, trimmed);
    }

    /**
     * Set properties file data line
     *
     * @param data   - property data
     * @param lineNo - line number
     */
    private void setLine(String data, int lineNo) {
        int i = 0;
        char c;
        int state = 0;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();

        for (; i < data.length(); i++) {
            c = data.charAt(i);
            switch (state) {
                case 0:
                    if (!Character.isWhitespace(c)) {
                        state = 1;
                        i--;
                    }
                    break;
                case 1:
                    if (c == '#' || c == '!') {
                        i = data.length();
                    } else {
                        key.append(c);
                        state = 2;
                    }
                    break;
                case 2:
                    if (Character.isWhitespace(c) || c == '=' || c == ':') {
                        state = 3;
                        i--;
                    } else {
                        key.append(c);
                    }
                    break;
                case 3:
                    if (!Character.isWhitespace(c)) {
                        state = 4;
                        i--;
                    }
                    break;
                case 4:
                    if (c == '=' || c == ':') {
                        state = 5;
                    } else {
                        throw new IllegalArgumentException("Line in properties file is malformed: " + lineNo);
                    }
                    break;
                case 5:
                    if (!Character.isWhitespace(c)) {
                        state = 6;
                        i--;
                    }
                    break;
                case 6:
                    value.append(c);
                    break;
            }
        }

        if (key.length() == 0) {
            addComment(data);
        } else {
            linkedProps.put(key.toString(), value.toString());
        }
    }

    public void addComment(String comment) {
        linkedProps.put("#" + (++commentCount), comment);
    }

    public void load(String fname) throws IOException {
        File file = new File(fname);
        if (file.exists() && file.isFile()) {
            InputStream inputStream = null;
            try {
                load(inputStream = new FileInputStream(file));
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        log.error("Error Loading properties File" + ex.getMessage(), ex, log);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void load(InputStream inputStream) throws IOException {
        load(new InputStreamReader(inputStream));
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        String dataLine;
        int lineNo = 0;
        commentCount = 0;
        BufferedReader in = new BufferedReader(reader);
        while ((dataLine = in.readLine()) != null) {
            setLine(dataLine, ++lineNo);
        }
    }

    public void store(String fName, String comment)
            throws IOException {
        File file = new File(fName);
        if (file.exists() && !file.isFile()) {
            return;
        }
        OutputStream outputStream = null;
        try {
            store(outputStream = new FileOutputStream(file), comment);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    log.error("Error Saving properties File" + ex.getMessage(), ex, log);
                }
            }
        }
    }

    @Override
    public void store(OutputStream out, String comment) throws IOException {
        store(new PrintWriter(out, true), comment);
    }

    @Override
    public void store(Writer writer, String comment) throws IOException {
        store(new PrintWriter(writer, true), comment);
    }

    public void store(PrintWriter out, String comment) throws IOException {
        Set<String> keySet = linkedProps.keySet();
        for (String key : keySet) {
            out.println(toString(key, linkedProps.get(key)));
        }
    }

    @Override
    public void list(PrintStream out) {
        Set<String> keySet = linkedProps.keySet();
        for (String key : keySet) {
            out.println(toString(key, linkedProps.get(key)));
        }
    }

    public Iterator<Entry<String, String>> iterator() {
        return linkedProps.entrySet().iterator();
    }
}