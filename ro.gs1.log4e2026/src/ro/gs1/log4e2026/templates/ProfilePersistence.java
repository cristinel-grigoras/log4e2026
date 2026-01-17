package ro.gs1.log4e2026.templates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.exceptions.Log4eSystemException;

/**
 * Handles reading and writing profiles to XML files.
 * XML format compatible with original Log4E plugin.
 */
public class ProfilePersistence {

    private static final int CURRENT_DOCUMENT_VERSION = 1;

    // XML element and attribute names
    private static final String XML_NODE_ROOT = "profiles";
    private static final String XML_NODE_PROFILE = "profile";
    private static final String XML_NODE_SETTING = "setting";
    private static final String XML_NODE_LIST = "list";
    private static final String XML_NODE_LISTSETTING = "listsetting";
    private static final String XML_ATTRIBUTE_TYPE = "type";
    private static final String XML_ATTRIBUTE_DOCUMENT_VERSION = "version";
    private static final String XML_ATTRIBUTE_ID = "id";
    private static final String XML_ATTRIBUTE_TITLE = "title";
    private static final String XML_ATTRIBUTE_NAME = "name";

    /**
     * Read profiles from a resource file in the classpath.
     */
    public Profiles readProfiles(String resourcePath) throws Log4eSystemException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new Log4eSystemException("Resource not found: " + resourcePath);
            }
            return parseProfiles(inputStream);
        } catch (Exception e) {
            throw new Log4eSystemException("Failed to read profiles from: " + resourcePath, e);
        }
    }

    /**
     * Read profiles from a file.
     */
    public Profiles readProfiles(File file) throws Log4eSystemException {
        if (!file.exists()) {
            return new Profiles();
        }
        try (InputStream inputStream = file.toURI().toURL().openStream()) {
            return parseProfiles(inputStream);
        } catch (Exception e) {
            throw new Log4eSystemException("Failed to read profiles from: " + file, e);
        }
    }

    /**
     * Parse profiles from an input stream.
     */
    private Profiles parseProfiles(InputStream inputStream) throws Exception {
        Profiles profiles = new Profiles();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);

        Element root = document.getDocumentElement();
        if (root.hasAttribute(XML_ATTRIBUTE_DOCUMENT_VERSION)) {
            profiles.setVersion(root.getAttribute(XML_ATTRIBUTE_DOCUMENT_VERSION));
        }

        NodeList profileNodes = root.getElementsByTagName(XML_NODE_PROFILE);
        for (int i = 0; i < profileNodes.getLength(); i++) {
            Element profileElement = (Element) profileNodes.item(i);
            Profile profile = parseProfile(profileElement);
            profiles.addProfile(profile);
        }

        return profiles;
    }

    /**
     * Parse a single profile from an XML element.
     */
    private Profile parseProfile(Element element) {
        String id = element.getAttribute(XML_ATTRIBUTE_ID);
        String title = element.getAttribute(XML_ATTRIBUTE_TITLE);
        String name = element.getAttribute(XML_ATTRIBUTE_NAME);
        String versionStr = element.getAttribute(XML_ATTRIBUTE_DOCUMENT_VERSION);

        Profile profile = new Profile(id, title);
        if (name != null && !name.isEmpty()) {
            profile.setName(name);
        }
        if (versionStr != null && !versionStr.isEmpty()) {
            try {
                profile.setVersion(Integer.parseInt(versionStr));
            } catch (NumberFormatException e) {
                // Ignore, use default version
            }
        }

        // Parse settings
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element childElement = (Element) child;
            String nodeName = childElement.getNodeName();

            if (XML_NODE_SETTING.equals(nodeName)) {
                parseSetting(profile, childElement);
            } else if (XML_NODE_LIST.equals(nodeName)) {
                parseListSetting(profile, childElement);
            }
        }

        return profile;
    }

    /**
     * Parse a single setting from XML.
     */
    private void parseSetting(Profile profile, Element element) {
        String id = element.getAttribute(XML_ATTRIBUTE_ID);
        String type = element.getAttribute(XML_ATTRIBUTE_TYPE);
        String textContent = element.getTextContent();

        Object value = convertValue(textContent, type);
        profile.put(id, value);
    }

    /**
     * Parse a list setting from XML.
     */
    private void parseListSetting(Profile profile, Element element) {
        String id = element.getAttribute(XML_ATTRIBUTE_ID);
        List<String> list = new ArrayList<>();

        NodeList children = element.getElementsByTagName(XML_NODE_LISTSETTING);
        for (int i = 0; i < children.getLength(); i++) {
            Element listItem = (Element) children.item(i);
            list.add(listItem.getTextContent());
        }

        profile.put(id, list);
    }

    /**
     * Convert a string value to the appropriate type.
     */
    private Object convertValue(String value, String type) {
        if (value == null) {
            return null;
        }
        if ("Integer".equals(type)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        if ("Boolean".equals(type)) {
            return Boolean.parseBoolean(value);
        }
        return value; // String
    }

    /**
     * Write profiles to a file.
     * Only user profiles are written (built-in profiles are skipped).
     */
    public void writeProfiles(Profiles profiles, File file) throws Log4eSystemException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            writeProfiles(profiles.getProfiles(), writer);
        } catch (IOException e) {
            throw new Log4eSystemException("Failed to write profiles to: " + file, e);
        }
    }

    /**
     * Write profiles to a writer.
     */
    private void writeProfiles(Iterator<Profile> profiles, Writer writer) throws Log4eSystemException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element rootElement = document.createElement(XML_NODE_ROOT);
            rootElement.setAttribute(XML_ATTRIBUTE_DOCUMENT_VERSION, Integer.toString(CURRENT_DOCUMENT_VERSION));
            document.appendChild(rootElement);

            while (profiles.hasNext()) {
                Profile profile = profiles.next();
                // Only write user profiles
                if (!Profile.ID_USER.equals(profile.getId())) {
                    continue;
                }
                Element profileElement = createProfileElement(profile, document);
                rootElement.appendChild(profileElement);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(document), new StreamResult(writer));

        } catch (Exception e) {
            throw new Log4eSystemException("Failed to write profiles", e);
        }
    }

    /**
     * Create an XML element for a profile.
     */
    private Element createProfileElement(Profile profile, Document document) {
        Element element = document.createElement(XML_NODE_PROFILE);
        element.setAttribute(XML_ATTRIBUTE_ID, profile.getId());
        element.setAttribute(XML_ATTRIBUTE_TITLE, profile.getTitle());
        element.setAttribute(XML_ATTRIBUTE_NAME, profile.getName());
        element.setAttribute(XML_ATTRIBUTE_DOCUMENT_VERSION, Integer.toString(profile.getVersion()));

        for (String key : profile.getSettings().keySet()) {
            Object value = profile.getSettings().get(key);
            String type = getType(value);

            if ("List".equals(type)) {
                Element listElement = document.createElement(XML_NODE_LIST);
                listElement.setAttribute(XML_ATTRIBUTE_ID, key);
                element.appendChild(listElement);

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) value;
                for (Object listValue : list) {
                    String listType = getType(listValue);
                    Element listSetting = document.createElement(XML_NODE_LISTSETTING);
                    listSetting.setAttribute(XML_ATTRIBUTE_TYPE, listType);
                    Text valueText = listValue != null ?
                            document.createTextNode(listValue.toString()) :
                            document.createTextNode("");
                    listSetting.appendChild(valueText);
                    listElement.appendChild(listSetting);
                }
            } else {
                Element setting = document.createElement(XML_NODE_SETTING);
                setting.setAttribute(XML_ATTRIBUTE_ID, key);
                setting.setAttribute(XML_ATTRIBUTE_TYPE, type);
                Text valueText = value != null ?
                        document.createTextNode(value.toString()) :
                        document.createTextNode("");
                setting.appendChild(valueText);
                element.appendChild(setting);
            }
        }

        return element;
    }

    /**
     * Get the type name for a value.
     */
    private String getType(Object obj) {
        if (obj instanceof List) {
            return "List";
        }
        if (obj instanceof Integer) {
            return "Integer";
        }
        if (obj instanceof Boolean) {
            return "Boolean";
        }
        return "String";
    }
}
