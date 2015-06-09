package ar.edu.itba.pdc.tp;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.XMPP.XMPproxy;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.admin.AdminProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPReactorImpl;
import ar.edu.itba.pdc.tp.util.PropertiesFileLoader;

public class Main {
	// nombre del archivo de propiedades

	private static String PROPERTIES_FILENAME = "proxyServer.properties";
	private static String PROPERTIES_PATH="./properties/";
	private static short ADMIN_PORT;
	private static short PROXY_SERVER_PORT;
	private static String PROXY_ADDRESS;

	private static String DEFAULT_ORIGIN_SERVER;
	private static short ORIGIN_SERVER_PORT;

	private static boolean DEFAULT_MULTIPLEXING;
	private static boolean DEFAULT_TRANSFORMATION;

	private static final int BUFFER_SIZE = 4 * 1024; // 4k

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	// opcionalmente como primer parámetro, se puede indicar el archivo de
	// configuración.Por defecto se lo busca en la carpeta actual.

	public static void main(String[] args) {
		if (args.length == 1) {
			PROPERTIES_FILENAME = args[0];
		}
		String propertiesFileFullPath=PROPERTIES_PATH+PROPERTIES_FILENAME;

		try {
			

			loadPropertiesFile(propertiesFileFullPath);
			LOGGER.info("Properties file "+propertiesFileFullPath + " was read correctly.");
			//
			
			InetSocketAddress pop3_address = new InetSocketAddress(
					PROXY_ADDRESS, PROXY_SERVER_PORT);
			InetSocketAddress admin_address = new InetSocketAddress(
					PROXY_ADDRESS, ADMIN_PORT);

			Map<Integer, TCPProtocol> protocolHandlers = new HashMap<Integer, TCPProtocol>();

			AdminModule adminModule = new AdminModule(DEFAULT_ORIGIN_SERVER,
					ORIGIN_SERVER_PORT,DEFAULT_MULTIPLEXING,DEFAULT_TRANSFORMATION);
			//TODO borrar aca adentro
		adminModule.silence("protos1");
			//
			
			TCPReactorImpl reactor = new TCPReactorImpl(protocolHandlers,
					DEFAULT_ORIGIN_SERVER);

			XMPproxy xmppProxy = new XMPproxy(reactor, adminModule,BUFFER_SIZE);
			AdminProtocol admin = new AdminProtocol(reactor, BUFFER_SIZE,
					adminModule);

			protocolHandlers.put(pop3_address.getPort(), xmppProxy);
			protocolHandlers.put(admin_address.getPort(), admin);

			LOGGER.info("Proxy XMPP started...");
			
		reactor.start();
		} catch (FileNotFoundException | MissingPropertyException e) {
			LOGGER.error("Unable to read properties file: "
					+ propertiesFileFullPath);
		}catch(Exception e){
//			e.printStackTrace();
//			LOGGER.error(e.printStackTrace(););
		}
	}

	private static void loadPropertiesFile(String fileName) throws FileNotFoundException,MissingPropertyException {
		Properties properties = PropertiesFileLoader
				.loadPropertiesFromFile(fileName);

		String admin_port_str=properties.getProperty("ADMIN_PORT");
		if(admin_port_str==null){
			throw new MissingPropertyException();
		}
		ADMIN_PORT = Short.parseShort(admin_port_str);
		
		
		
		
		String proxy_server_port_str=properties
				.getProperty("PROXY_SERVER_PORT");
		if(proxy_server_port_str==null)
			throw new MissingPropertyException();
		PROXY_SERVER_PORT = Short.parseShort(proxy_server_port_str);
		
	
		
		PROXY_ADDRESS = properties.getProperty("PROXY_ADDRESS");
		if(PROXY_ADDRESS==null)
			throw new MissingPropertyException();
		
		
		
		DEFAULT_ORIGIN_SERVER = properties.getProperty("DEFAULT_ORIGIN_SERVER");
		if(DEFAULT_ORIGIN_SERVER==null)
			throw new MissingPropertyException();
		
		
		
		String origin_server_port_str=properties
				.getProperty("ORIGIN_SERVER_PORT");
		if(origin_server_port_str==null)
			throw new MissingPropertyException();
		ORIGIN_SERVER_PORT = Short.parseShort(origin_server_port_str);

		
		
		String default_multiplexing_str = properties.getProperty("multiplexing");
		if(default_multiplexing_str==null)
			throw new MissingPropertyException();
		DEFAULT_MULTIPLEXING =Boolean.valueOf(default_multiplexing_str);

		
		String default_transformation_str=properties.getProperty("message_transformation");
		if(default_transformation_str==null)
			throw new MissingPropertyException();
		DEFAULT_TRANSFORMATION =Boolean.valueOf(default_transformation_str);

	}

}