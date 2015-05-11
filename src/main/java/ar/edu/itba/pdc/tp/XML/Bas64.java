package ar.edu.itba.pdc.tp.XML;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.BinaryCodec;

public class Bas64 {

	
public static void main(String[] args) {
	String coded="AHByb3RvczEAcGFzcw==";
	byte[] bytes=DatatypeConverter.parseBase64Binary(coded);
	System.out.println(bytes);
}
}
