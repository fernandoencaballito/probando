package ar.edu.itba.pdc.tp.XMPP;
public interface XMPPlistener {
	
	public  void connectToOrigin(XMPPproxyState state);
	public void closeConnection(XMPPproxyState state);
	public void writeToOrigin(byte[] array,XMPPproxyState state);
	public void writeToClient(byte[] array,XMPPproxyState state);
	

}
