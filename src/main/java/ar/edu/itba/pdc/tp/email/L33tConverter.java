package ar.edu.itba.pdc.tp.email;

public class L33tConverter {
	
	public static String leetify(String message) {
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
