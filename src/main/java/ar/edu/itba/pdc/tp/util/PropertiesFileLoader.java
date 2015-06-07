package ar.edu.itba.pdc.tp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFileLoader {

    static public Properties loadPropertiesFromFile(String propFileName) throws FileNotFoundException {
        validatePropertyFileName(propFileName);

        Properties property = new Properties();
        property = preparePropertyForReading(property, propFileName);
        return property;
    }

    private static Properties preparePropertyForReading(Properties property, String resourcePath) throws FileNotFoundException {
//        InputStream propertyFileInputStream = PropertiesFileLoader.class
//                .getResourceAsStream(resourcePath);
    	File file = new File(resourcePath);
    	
    	InputStream propertyFileInputStream = new FileInputStream(file);
    	
        try {
            validateFileInputStream(propertyFileInputStream, resourcePath);
            loadProperties(property, propertyFileInputStream);
        } catch (FileNotFoundException e) {
            //Do nothing
        }
        return property;
    }

    private static void validateFileInputStream(InputStream fileInputStream, String resourcePath)
            throws FileNotFoundException {
        if (fileInputStream == null) {
            String msg = "Could not find property file in path: " + resourcePath;
            throw new FileNotFoundException(msg);
        }
    }

    private static void loadProperties(Properties property, InputStream propertyFileInputStream) {
        try {
            property.load(propertyFileInputStream);
            propertyFileInputStream.close();
        } catch (IOException e) {
            //Do nothing
        }
    }

    private static void validatePropertyFileName(String propFileName) {
        assert propFileName.getClass() == "string".getClass() && propFileName != null;
    }
}