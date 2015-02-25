package ar.edu.itba.pdc.tp.test.admin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

import ar.edu.itba.pdc.tp.admin.SetUserValidator;

public class TestUserCommand {
	private static String urlRegex = "[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static Pattern urlPattern = Pattern.compile(urlRegex);
	private static final String usernameRegex = "[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*(@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,}))?";
	private static Pattern usernamePattern = Pattern.compile(usernameRegex);
	private static String CRLF_REGEX="\\r\\n";
	private static String CRLF="\r\n";
	//private static String setUserRegex = "SET\\s"+usernameRegex+"\\s"+urlRegex+CRLF;
	private static String setUserRegex = "SET\\s"+usernameRegex+CRLF_REGEX;
	private static Pattern setUserPattern = Pattern.compile(setUserRegex);
	
	private static SetUserValidator setUserValidator=new SetUserValidator();
	
	
	@Test
	public void testUrlPattern(){
		String url1="localhost";
		String url2= "pop3.example.org";
		String url3="pop3.backoffice.example.org";
		String invalid="hólá";
		String url4="sample.11";
		assertTrue(urlPattern.matcher(url1).matches());
		assertTrue(urlPattern.matcher(url2).matches());
		assertTrue(urlPattern.matcher(url3).matches());
		assertTrue(urlPattern.matcher(url4).matches());
		assertFalse(urlPattern.matcher(invalid).matches());
	}
	
	@Test 
	public void testUsernamePattern(){
		String u1="default";
		String u2="correo@example.org";
		String invalid1="pepe@*";
		String invalid2="A@b@c@example.com";
		assertTrue(usernamePattern.matcher(u1).matches());
		assertTrue(usernamePattern.matcher(u2).matches());
		assertFalse(usernamePattern.matcher(invalid1).matches());
		assertFalse(usernamePattern.matcher(invalid2).matches());

	}
	
	@Test 
	public void testSetUserCommand(){
		
		String s1="SET user originUrl"+CRLF;
		String s2="SET default originUrl"+CRLF;
		String s3="SET default pop3.example.org"+CRLF;
		String s4="SET jperez pop3.backoffice.example.org"+CRLF;
		String s5="SET vm localhost"+CRLF;
		String invalid1="SET PEPE"+CRLF;
		String invalid2="SET @ localhost"+CRLF;
		
		assertNotNull(setUserValidator.validate(s1));
		assertNotNull(setUserValidator.validate(s2));
		assertNotNull(setUserValidator.validate(s3));
		assertNotNull(setUserValidator.validate(s4));
		assertNotNull(setUserValidator.validate(s5));
		assertNull(setUserValidator.validate(invalid1));
		assertNull(setUserValidator.validate(invalid2));
		
		
		
		
	}
	
	
	
	
	
}
