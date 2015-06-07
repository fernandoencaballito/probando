package ar.edu.itba.pdc.tp.email;

public class EmailConverter {
	private boolean doneConvertingHeaders = false;

	public String convertLine(String line) {
		if (doneConvertingHeaders) {
			return line;
		}
		if ("\r\n".equals(line)) {
			// No more headers to come
			doneConvertingHeaders = true;
			return line;
		}
		SubjectHeaderConverter converter = new SubjectHeaderConverter();
		String convertedLine = converter.convertLine(line);
		if (convertedLine != null) {
			// Subject header done
			doneConvertingHeaders = true;
			return convertedLine;
		}
		// Either +OK or another header (not Subject *yet*)
		return line;
	}

	public boolean isDoneConvertingHeaders() {
		return doneConvertingHeaders;
	}
}
