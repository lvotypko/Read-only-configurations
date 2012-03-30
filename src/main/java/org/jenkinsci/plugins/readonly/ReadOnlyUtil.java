package org.jenkinsci.plugins.readonly;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Transform html formular code to read-only
 * 
 * @author Lucie Votypkova
 */
public class ReadOnlyUtil {


    public static String transformInputsToReadOnly(String source) throws IOException, SAXException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
        
        StringBuffer buffer = new StringBuffer(source);
        int position=0;
        while (true) {
            position = buffer.indexOf("<", position);
            if (position == -1) {
                break;
            }
            int end = buffer.indexOf(">", position);
            String tag = buffer.substring(position, end);
            if (tag.startsWith("<input")) {
                String replacement = readonlyInput(tag);
                buffer.replace(position, end, replacement);
            }
            if (tag.startsWith("<textarea")) {
                String replacement = readonlyTextArea(tag);
                buffer.replace(position, end, replacement);
            }
            if (tag.startsWith("<option")) {
                String replacement = readonlyOption(tag);
                buffer.replace(position, end, replacement);
            }
            position=end;
        }
        return buffer.toString();
    }

    
    public static String readonlyInput(String tag) {
        if (!(tag.contains("class=\"advancedButton\"") || tag.contains("id=\"search-box\""))) {
            if (tag.contains("type=\"text\"")) {
                tag = tag.replace("<input", "<input readonly=\"readonly\"");
            } else {
                tag = tag.replace("<input", "<input disabled=\"disabled\"");
            }
        }
        return tag;
    }

    public static String readonlyTextArea(String tag) {
        if (tag.contains("setting-input  codemirror")) {

            tag = tag.replace("setting-input  codemirror", "setting-input");
        }
        tag = tag.replace("<textarea", "<textarea readonly=\"readonly\"");

        return tag;
    }

    public static String readonlyOption(String tag) {
        tag = tag.replace("<option", "<option disabled=\"disabled\"");
        return tag;
    }
}
