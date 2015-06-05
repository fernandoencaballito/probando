package ar.edu.itba.pdc.tp.test.XML;

import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;
//TODO borrar!
//BORRAR!!!
public class TestXMLconstructor {
	
	
	@Test
	public void test1(){
		
	}
	
	private void init(String str) throws UnsupportedEncodingException, XMLStreamException{
		AsyncXMLInputFactory inputF = new InputFactoryImpl(); // sub-class of XMLStreamReader2
		// two choices for input feeding: byte[] or ByteBuffer. Here we use former:
		byte[] input_part1 = "<root>val".getBytes("UTF-8"); // would come from File, over the net etc
		// can construct with initial data, or without; here we initialize with it
		AsyncXMLStreamReader<AsyncByteArrayFeeder> parser = inputF.createAsyncFor(input_part1);

		// now can access couple of events
//		assertTokenType(XMLStreamConstants.START_DOCUMENT, parser.next());
//		assertTokenType(XMLStreamConstants.START_ELEMENT, parser.next());
	}
	

}
