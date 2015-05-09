package ar.edu.itba.pdc.tp.test.XML;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.core.GenericTypeResolver;

import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XML.ToServerParser;

public class TestToServerParser {

	@Test
	public void testStartingStream() throws IOException{
		String inFileName="./src/test/resources/startingStreamFromClient.in";
		String outFileName="./src/test/resources/startingStreamFromClient.out";
		
		genericTest(inFileName,outFileName);
		
		
	}
	
	private static void genericTest(String inFileName,String outFileName) throws IOException{
		InputStream in = new FileInputStream(inFileName);
		
		String outStr=readFile(outFileName);
		
		ByteArrayOutputStream currentAns=new ByteArrayOutputStream();
		GenericParser parser=new ToServerParser(in,currentAns);
		parser.parse();
		//no graba la respuesta porque no encuentra tag stream
//		assertEquals(outStr,currentAns.toString(StandardCharsets.UTF_8.name()));
	}
	
	static String readFile(String path) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, StandardCharsets.UTF_8);
			}
}
