package ar.edu.itba.pdc.tp.test.XML;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XML.ToServerParser;

public class TestToServerParser {

	@Test
	public void testStartingStream() throws IOException, SAXException, ParserConfigurationException{
		String fileName="./src/test/resources/startingStreamFromClient.in";
		
		genericTest(fileName,fileName);
		
		
	}
	
	private static void genericTest(String inFileName,String outFileName) throws IOException, SAXException, ParserConfigurationException{
		InputStream in = new FileInputStream(inFileName);
		
		String outStr=readFile(outFileName);
		
		ByteArrayOutputStream currentAns=new ByteArrayOutputStream();
		GenericParser parser=new ToServerParser(in,currentAns);
		parser.parse();
		//no graba la respuesta porque no encuentra tag stream
		assertEquals(outStr.trim(),currentAns.toString());
//		checkXML(outStr, currentAns.toString(StandardCharsets.UTF_8.name()));
	
	}
//	static void checkXML(String expected, String actual) throws SAXException, IOException, ParserConfigurationException{
//		
//		
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
//	    DocumentBuilder builder;  
//	     
//	        builder = factory.newDocumentBuilder();  
//	        Document document_expected = builder.parse( new InputSource( new StringReader( expected ) ) );  
//	        Document actual_document = builder.parse( new InputSource( new StringReader( actual) ) );  
//	     assertTrue(document_expected.equals(actual_document));
//		
//	}
	static String readFile(String path) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, StandardCharsets.UTF_8);
			}
}
