package ar.edu.itba.pdc.tp.XML;

public interface XMPPlistener {
	
	public void connectToOrigin();
	public void closeConnection();
	public void writeToOrigin(byte[] array);
	public void writeToClient(byte[] array);
	

}
