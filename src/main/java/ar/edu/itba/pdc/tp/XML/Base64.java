package ar.edu.itba.pdc.tp.XML;

import java.nio.charset.Charset;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.BinaryCodec;

public class Base64 {

	private static final byte SEPARATOR = (byte) 0x00;

	public static User getUser(String base64Coded) {
		byte[] bytes = DatatypeConverter.parseBase64Binary(base64Coded);

		int separatorIndex;
		for (separatorIndex = 1; separatorIndex < bytes.length; separatorIndex++) {
			if (bytes[separatorIndex] == SEPARATOR)
				break;

		}

		String username = new String(bytes, 1, separatorIndex,
				Charset.forName("UTF-8"));

		int passwordLength = bytes.length - separatorIndex;

		String password = new String(bytes, separatorIndex, passwordLength,
				Charset.forName("UTF-8"));
		User user = new User(username.trim(), password.trim(), base64Coded);
		return user;
	}

}
