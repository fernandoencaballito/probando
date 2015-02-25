package ar.edu.itba.pdc.tp.email;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QCodec;

public class EmailConverter {
	private String oldHeaderValue;
	private String charset;
	private String encoding;
	private String encodedText;
	private boolean encoded;
	private boolean malFormed;

	public String convert(String line) {
		String headerName;
		String headerValue;
		String transformed;

		int separator = line.indexOf(":");
		if (separator == -1 || separator == line.length() - 1) {
			return line;
		}
		headerName = line.substring(0, separator);
		if (!headerName.equalsIgnoreCase("Subject")) {
			return line;
		} else {
			// Subject header
			headerValue = line.substring(separator + 1, line.length());
			oldHeaderValue = headerValue; // Bad format
			headerValue = headerValue.trim();
			checkEncoding(headerValue);
			if (!encoded) {
				// System.out.println("Not encoded");
				transformed = leetify(headerValue);
			} else if (encoded && !malFormed) {
				// System.out.println("Encoded");
				transformed = decodeTransformAndEncode();
			} else {
				// System.out.println("Malformed");
				transformed = leetify(oldHeaderValue);
			}
			return "Subject: " + transformed + "\r\n";
		}

	}

	private String decodeTransformAndEncode() {
		if (encoding.equalsIgnoreCase("B")) {
			// Decode with base64
			byte[] text = Base64.decodeBase64(encodedText);
			String transformed = leetify(text.toString());
			// System.out.println("Transformed:");
			encodedText = Base64.encodeBase64(transformed.getBytes())
					.toString();
			return "=?" + charset + "?" + encoding + "?" + encodedText + "?=";
		} else if (encoding.equalsIgnoreCase("Q")) {
			// Decode with Q-encoding
			QCodec qCodec = new QCodec();
			String text;
			try {
				text = qCodec.decode("=?" + charset + "?" + encoding + "?"
						+ encodedText + "?=");
				String transformed = leetify(text);
				// System.out.println("Transformed:");
				String newHeaderValue = qCodec.encode(transformed);
				return newHeaderValue;
			} catch (DecoderException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (EncoderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return oldHeaderValue;
	}

	private void checkEncoding(String value) {
		// Min length of 6 because of "=?" "?" "?" "?="
		if (value.length() < 6) {
			encoded = false;
			return;
		} else {
			String aux;
			if (value.startsWith("=?") && value.endsWith("?=")) {
				encoded = true;
				// Save in aux the value without "=?" and "?="
				aux = value.substring(2, value.length() - 2);
				int firstSeparator = aux.indexOf("?");
				int lastSeparator = aux.lastIndexOf("?");
				if (firstSeparator == -1 || lastSeparator == -1
						|| firstSeparator == lastSeparator) {
					// System.out.println("Invalid header: invalid encoding");
					malFormed = true;
					return;
				}
				charset = aux.substring(0, firstSeparator);
				encoding = aux.substring(firstSeparator + 1, lastSeparator);
				if (encoding.length() != 1
						|| (!encoding.equalsIgnoreCase("B") && !encoding
								.equalsIgnoreCase("Q"))) {
					// System.out.println("Invalid header: invalid encoding");
					malFormed = true;
					return;
				} else {
					encodedText = aux
							.substring(lastSeparator + 1, aux.length());
					return;
				}
			} else {
				encoded = false;
				return;
			}
		}
	}

	private String leetify(String message) {
		char[] msg = message.toCharArray();
		for (int i = 0; i < message.length(); i++) {
			switch (msg[i]) {
			case 'a':
				msg[i] = '4';
				break;
			case 'e':
				msg[i] = '3';
				break;
			case 'i':
				msg[i] = '1';
				break;
			case 'o':
				msg[i] = '0';
				break;
			case 'c':
				msg[i] = '<';
				break;
			}
		}
		return String.valueOf(msg);
	}
}
