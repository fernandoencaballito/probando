package ar.edu.itba.pdc.tp.test.XML;

import static org.junit.Assert.*;

import org.junit.Test;

import ar.edu.itba.pdc.tp.XML.Base64;
import ar.edu.itba.pdc.tp.XML.User;

/*clase que permite obtener el nombre de usuario y la contraseña a partir del texto en base64
 * que se obtiene durante la autenticación plana
 */
public class TestBase64 {

	
	@Test
	public void test1(){
		String coded="AG1iZWQAbWlycm9y";
		User user=Base64.getUser(coded);
		assertEquals("mbed",user.getUsername());
		assertEquals("mirror",user.getPassword());
		
	}
}
