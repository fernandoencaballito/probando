package ar.edu.itba.pdc.tp.admin;

import java.awt.List;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Clase que contiene objetos que comparten los protocolos de proxy y de administración.
 */
public class AdminModule {
    private final InetSocketAddress defaultAddress;
    private final short defaultPort;

    private Map<String, InetSocketAddress> originAddressesByUser = new HashMap<>();
    private Map <String,String>destinyServerByUser= new HashMap<>();
    private Set<String> SilencedUsers=new HashSet();

    private Metrics metrics;
    private boolean transform;
    private boolean multiplexing;

    public AdminModule(String defaultAddress, short defaultPort) {
        this.defaultAddress = new InetSocketAddress(defaultAddress, defaultPort);
        this.defaultPort = defaultPort;
        this.metrics = new Metrics();
        this.transform = false;
        this.multiplexing = false;
    }

    public void addAccess() {
        metrics.addAccess();
    }

    public void addBytesTransfered(long amount) {
        metrics.addBytesTransfered(amount);
    }

    public long getAccesses() {
        return metrics.getAccesses();
    }

    public long getBytesTransfered() {
        return metrics.getBytesTransfered();
    }

    public void setOriginForUser(String user, String originUrl) {
        originAddressesByUser.put(user, new InetSocketAddress(originUrl,
                defaultPort));
    }

    public InetSocketAddress getOriginAddressForUser(String user) {
        if (multiplexing) {
            InetSocketAddress originAddress = originAddressesByUser.get(user);
            if (originAddress != null) {
                return originAddress;
            }
        }
        return defaultAddress;
    }

    public void transformationOff() {
        transform = false;
    }

    public void transformationOn() {
        transform = true;
    }

    public boolean isTransformationOn() {
        return transform;
    }

    public void multiplexingOn() {
        multiplexing = true;
    }

    public void multiplexingOff() {
        multiplexing = false;
    }

    public Boolean getTransform() {
        return transform;
    }
    
    public String silence(String user){
       SilencedUsers.add(user);
    	return "ok";
    }

	public void changeUserDestinyServer(String user) {
	 destinyServerByUser.put(user,"destinyServer");
		
	}

}
