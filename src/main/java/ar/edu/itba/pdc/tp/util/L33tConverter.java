package ar.edu.itba.pdc.tp.util;

import org.apache.commons.lang3.StringUtils;


public class L33tConverter {
	private static final String[] searchList={"a","e","i","o","c"};
	 private static final String[] replacementList={"4","3","1","0",">"};
	
	public static String leetify(String message) {

		 
		return StringUtils.replaceEach(message, searchList, replacementList);
	}

}
