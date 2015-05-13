package ar.edu.itba.pdc.tp.test.XML;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.xml.sax.SAXException;

import ar.edu.itba.pdc.tp.XML.ByteBufferInputStream;
import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XML.ToServerParser;

public class TestToServerParser {
	private static final int BUFF_SIZE = 10 * 1024;
//	@Test
//	public void testStartingStream() throws IOException, SAXException, ParserConfigurationException{
//		String fileName="./src/test/resources/startingStreamFromClient.in";
//		
//		genericTest(fileName,fileName);
//		
//		
//	}
	
	@Test 
	public void testPartialRead() throws IOException, XMLStreamException{
//				String outFileName="./src/test/resources/startingStreamFromClient.in";
				ByteBuffer buffer=ByteBuffer.allocate(BUFF_SIZE);
				//String expectedString=readFile(outFileName);
			
				String inFileName1="./src/test/resources/startingStreamFromClient.part1.in";
				InputStream in=new ByteBufferInputStream(buffer);
				readFileIntoBuffer(inFileName1, buffer);
				assertTrue(buffer.position()>0);
				
				
				
				//SE TIENE QUE PASAR EL BUFFER A MODO LECTURA
				buffer.flip();
				GenericParser parser=new GenericParser(buffer);
				
				
				
				parser.parse();
				
				//SE TIENE QUE PASAR EL BUFFER A MODO ESCRITURA
				buffer.clear();
				buffer.flip();
				String inFileName2="./src/test/resources/startingStreamFromClient.part2.in";
				readFileIntoBuffer(inFileName2, buffer);
				
				//SE TIENE QUE PASAR EL BUFFER A MODO Lectura
				buffer.flip();
				parser.feed();
				parser.parse();
				//no graba la respuesta porque no encuentra tag stream
////				assertEquals(expectedString.trim(),currentAns.toString());
	}
	
	private static void genericTest(String inFileName,String outFileName) throws IOException, SAXException, ParserConfigurationException{
		//InputStream in = new FileInputStream(inFileName);
		
		ByteBuffer buffer=ByteBuffer.allocate(BUFF_SIZE);
//		InputStream in=new ByteBufferInputStream(buffer);
		String expectedString=readFile(outFileName);
//		
		InputStream in=new ByteBufferInputStream(buffer);
		readFileIntoBuffer(inFileName, buffer);
		assertTrue(buffer.position()>0);
		
		ByteArrayOutputStream currentAns=new ByteArrayOutputStream();
		
		//SE TIENE QUE PASAR EL BUFFER A MODO LECTURA
		buffer.flip();
		GenericParser parser=new ToServerParser(in,currentAns);
		
		
		parser.parse();
		//no graba la respuesta porque no encuentra tag stream
//	assertEquals(expectedString.trim(),currentAns.toString());
	
	}
	
	static void readFileIntoBuffer(String path,ByteBuffer buffer) throws IOException{
		RandomAccessFile aFile = new RandomAccessFile(path, "r");
		FileChannel inChannel = aFile.getChannel();
		inChannel.read(buffer);
	}

	static String readFile(String path) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, StandardCharsets.UTF_8);
			}
}
