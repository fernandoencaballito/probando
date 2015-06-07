package ar.edu.itba.pdc.tp.util;

public class POP3Utils {
	public static final String CRLF = "\r\n";
	public static final String OK = "+OK";
	public static final String ERR = "-ERR";
	public static final String MULTILINE_END = ".";

	public static String asOkLine(String line) {
		if (line == null) {
			throw new IllegalArgumentException();
		}
		return OK + " " + asLine(line);
	}

	public static String asErrLine(String line) {
		if (line == null) {
			throw new IllegalArgumentException();
		}
		return ERR + " " + asLine(line);
	}

	public static String asLine(String line) {
		if (line == null) {
			throw new IllegalArgumentException();
		}
		return line + CRLF;
	}

	public static String asMultilines(String... lines) {
		if (lines == null || lines.length == 0) {
			throw new IllegalArgumentException();
		}
		String lineStr = (OK + CRLF);
		for (String line : lines) {
			lineStr += (line + CRLF);
		}
		return lineStr + MULTILINE_END + CRLF;
	}
}
