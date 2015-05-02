package ar.edu.itba.pdc.tp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.admin.AdminProtocol;
import ar.edu.itba.pdc.tp.pop3.POP3Proxy;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPReactorImpl;
import ar.edu.itba.pdc.tp.util.PropertiesFileLoader;

public class Main {
	// nombre del archivo de propiedades

	private static String PROPERTIES_FILENAME;

	private static short ADMIN_PORT;
	private static short PROXY_SERVER_PORT;
	private static String PROXY_ADDRESS;
	
	private static String DEFAULT_ORIGIN_SERVER;
	private static short ORIGIN_SERVER_PORT;

	private static boolean DEFAULT_MULTIPLEXING;
	private static boolean DEFAULT_TRANSFORMATION;
	
	private static final int BUFFER_SIZE = 4 * 1024; // 4k

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	public static void main(String[] args) throws IOException {
		if (args.length != 0) {
			throw new IllegalArgumentException(
					"Parameter: <path to properties file>");
		}
		// se cargan valores de configuración
		PROPERTIES_FILENAME = args[0];
		loadPropertiesFile(PROPERTIES_FILENAME);
		//

		InetSocketAddress pop3_address = new InetSocketAddress(PROXY_ADDRESS,
				PROXY_SERVER_PORT);
		InetSocketAddress admin_address = new InetSocketAddress(PROXY_ADDRESS,
				ADMIN_PORT);

		Map<Integer, TCPProtocol> protocolHandlers = new HashMap<>();

		AdminModule adminModule = new AdminModule(DEFAULT_ORIGIN_SERVER,
				ORIGIN_SERVER_PORT);

		TCPReactorImpl reactor = new TCPReactorImpl(protocolHandlers,
				DEFAULT_ORIGIN_SERVER);

		POP3Proxy pop3Proxy = new POP3Proxy(reactor, adminModule);
		AdminProtocol admin = new AdminProtocol(reactor, BUFFER_SIZE,
				adminModule);

		protocolHandlers.put(pop3_address.getPort(), pop3Proxy);
		protocolHandlers.put(admin_address.getPort(), admin);

		LOGGER.info("Proxy POP3 started...");

		reactor.start();
	}

	private static void loadPropertiesFile(String fileName)
			throws FileNotFoundException {

		Properties properties = PropertiesFileLoader
				.loadPropertiesFromFile(fileName);
		ADMIN_PORT = Short.parseShort(properties.getProperty("ADMIN_PORT"));
		PROXY_SERVER_PORT = Short.parseShort(properties
				.getProperty("PROXY_SERVER_PORT"));
		PROXY_ADDRESS = properties.getProperty("PROXY_ADDRESS");

		DEFAULT_ORIGIN_SERVER = properties.getProperty("DEFAULT_ORIGIN_SERVER");
		ORIGIN_SERVER_PORT = Short.parseShort(properties
				.getProperty("ORIGIN_SERVER_PORT"));

		boolean multiplexing = Boolean.getBoolean(properties
				.getProperty("multiplexing"));

		boolean message_transformation = Boolean.getBoolean(properties
				.getProperty("message_transformation"));

	}

}