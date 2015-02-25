package ar.edu.itba.pdc.tp.pop3;

import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.AUTHENTICATION;
import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.EXPECT_PASS_OK;
import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.EXPECT_RETR_DATA;
import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.EXPECT_USER_OK;
import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.TRANSACTION;
import static ar.edu.itba.pdc.tp.util.NIOUtils.append;
import static ar.edu.itba.pdc.tp.util.NIOUtils.nonBlockingSocket;
import static ar.edu.itba.pdc.tp.util.POP3Utils.ERR;
import static ar.edu.itba.pdc.tp.util.POP3Utils.OK;
import static ar.edu.itba.pdc.tp.util.POP3Utils.asErrLine;
import static ar.edu.itba.pdc.tp.util.POP3Utils.asMultilines;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.email.EmailConverter;
import ar.edu.itba.pdc.tp.tcp.TCPEventHandler;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;
import ar.edu.itba.pdc.tp.util.NIOUtils;

class POP3Reader implements TCPEventHandler {
    private static final String CAPA = "CAPA";
    private static final String USER = "USER";
    private static final String PASS = "PASS";
    private static final String RETR = "RETR";
    private static final String QUIT = "QUIT";
    private static final String AUTH = "AUTH";

    private static final String CAPA_TRANSACTION_MSG = asMultilines(CAPA, USER,
            "LIST", RETR, "DELE", "RSET", "STAT", "NOOP");
    private static final String CAPA_AUTHENTICATION_MSG = asMultilines(CAPA,
            USER);

    private static final Logger LOGGER = Logger.getLogger(POP3Reader.class);

    private static final String ERR_MSG = asErrLine("");

    private final TCPReactor reactor;
    private final AdminModule adminModule;
    private final POP3Proxy parent;

    POP3Reader(POP3Proxy parent, TCPReactor reactor, AdminModule adminModule) {
        this.parent = parent;
        this.reactor = reactor;
        this.adminModule = adminModule;
    }

    @Override
    public void handle(SelectionKey key) throws IOException {
        final POP3ProxyState proxyState = (POP3ProxyState) key.attachment();

        final SocketChannel channel = (SocketChannel) key.channel();
        final ByteBuffer readBuffer = proxyState.getReadBuffer(channel);

        if (channel == proxyState.getClientChannel()
                && proxyState.getState().equals(EXPECT_RETR_DATA)) {
            proxyState.getConvertedOriginBuffer().clear();
            proxyState.setState(TRANSACTION);
        }

        String log;

        if (channel == proxyState.getClientChannel()) {
            log = "Client(" + channel.getRemoteAddress() + ")";
        } else {
            log = "Origin(" + channel.getRemoteAddress() + ")";
        }

        log += " wrote to ";
        if (readBuffer == proxyState.getClientBuffer()) {
            log += "client(" + proxyState.getClientChannel().getRemoteAddress()
                    + "): ";
        } else if (readBuffer == proxyState.getOriginBuffer()) {
            log += "origin(" + proxyState.getOriginChannel().getRemoteAddress()
                    + "): ";
        }

        Buffer duplicateReadBuffer = readBuffer.duplicate().flip();
        if (duplicateReadBuffer.limit() - duplicateReadBuffer.position() != 0) {
            LOGGER.info(log
                    + StringEscapeUtils.escapeJava(NIOUtils
                            .getFirstLine((ByteBuffer) duplicateReadBuffer)));
        }
        final long bytesRead = channel.read(readBuffer);

        if (bytesRead == -1) {
            // Did the other end close?
            proxyState.closeChannels();
            reactor.unsubscribeChannel(proxyState.getClientChannel());
            reactor.unsubscribeChannel(proxyState.getOriginChannel());
        } else if (bytesRead > 0) {
            handleNotEmptyRead(key, channel);
            proxyState.updateSubscription(key.selector());
        }
    }

    private void handleNotEmptyRead(SelectionKey key, SocketChannel channel) {
        final POP3ProxyState proxyState = (POP3ProxyState) key.attachment();

        POP3Line line = proxyState.getLine();

        if (!line.isRead()) {
            ByteBuffer readBufferView = proxyState.getReadBuffer(channel)
                    .duplicate();

            // viene de ser escrito, asi que para ser leido tiene que flippearse
            readBufferView.flip();

            line.read(readBufferView);
        }
        if (!line.isRead()) {
            // no se termino la linea, habra que esperar a la siguiente lectura
            return;
        }

        boolean transferingEmailHeaders = false;
        if (proxyState.getClientChannel() == channel) {
            handleClientNotEmptyRead(key, proxyState);
        } else if (proxyState.getOriginChannel() == channel) {
            transferingEmailHeaders = handleOriginNotEmptyRead(proxyState);
        } else {
            throw new IllegalArgumentException("Unknown socket");
        }

        if (transferingEmailHeaders) {
            transferBufferWithEmail(proxyState);
        }
        line.clear();
    }

    private void handleClientNotEmptyRead(SelectionKey key,
            POP3ProxyState proxyState) {
        POP3Line line = proxyState.getLine();
        String[] params;
        try {
            params = line.getWords();
        } catch (UnsupportedEncodingException e) {
            if (!proxyState.isConnectedToOrigin()) {
                // hay que contestarle aca porque no hay quien le responda si no
                sendResponseToClient(proxyState, ERR_MSG);
            }
            return;
        }
        String command = params[0].toUpperCase();

        switch (proxyState.getState()) {
        case AUTHENTICATION:
            switch (command) {
            case CAPA:
                sendResponseToClient(proxyState, CAPA_AUTHENTICATION_MSG);
                break;
            case USER:
                if (params.length == 1) {
                    // solo mando "user", sin user
                    sendResponseToClient(proxyState, ERR_MSG);
                } else {
                    String user = params[1];
                    connectToOrigin(key, proxyState, user);
                    proxyState.setState(EXPECT_USER_OK);
                }
                break;
            case PASS:
                SocketChannel originChannel = proxyState.getOriginChannel();
                if (originChannel != null && originChannel.isOpen()
                        && originChannel.isConnected()) {
                    // si ya esta conectado, no contestar error
                    proxyState.setState(EXPECT_PASS_OK);
                } else {
                    // No esta conectado al origin, commando invalido
                    sendResponseToClient(proxyState, ERR_MSG);
                }
                break;
            case AUTH: // No soportamos el metodo AUTH
                sendResponseToClient(proxyState, ERR_MSG);
                break;
            default: // Dejo pasar el resto de los comandos.
                break;
            }
            break;
        case TRANSACTION:
            switch (command) {
            case CAPA:
                sendResponseToClient(proxyState, CAPA_TRANSACTION_MSG);
                break;
            case RETR:
                proxyState.setState(EXPECT_RETR_DATA);
                break;
            case QUIT:
                // Si el server no cierra la conexion tengo que volver al estado
                // AUTHENTICATION
                proxyState.setState(AUTHENTICATION);
                break;
            default: // Dejo pasar el resto de los comandos.
                break;
            }
            break;
        default:
            LOGGER.warn("Should never happen - all client states are contemplated here");
        }
    }

    private boolean handleOriginNotEmptyRead(POP3ProxyState proxyState) {
        POP3Line line = proxyState.getLine();

        String command;
        try {
            String[] words = line.getWords();
            if (words.length == 0) {
                command = "";
            } else {
                command = line.getWords()[0].toUpperCase();
            }
        } catch (UnsupportedEncodingException e1) {
            return false;
        }
        switch (proxyState.getState()) {
        case AUTHENTICATION:
        case TRANSACTION:
            // Capturo las respuesta de los comandos no
            // contemplados en la maquina de estados.
            break;
        case EXPECT_USER_OK:
            switch (command) {
            case OK:
                proxyState.setState(AUTHENTICATION);
                break;
            case ERR:
                proxyState.setState(AUTHENTICATION);
                break;
            }
            break;
        case EXPECT_PASS_OK:
            switch (command) {
            case OK:
                proxyState.setState(TRANSACTION);
                break;
            case ERR:
                proxyState.setState(AUTHENTICATION);
                break;
            }
            break;
        case EXPECT_RETR_DATA:
            switch (command) {
            case ERR:
                proxyState.setState(TRANSACTION);
                break;
            case OK:
                if (adminModule.getTransform()) {
                    proxyState.startEmailHeaderTransfer();
                }
                return true;
            default:
                EmailConverter converter = proxyState.getEmailConverter();
                return converter != null
                        && !converter.isDoneConvertingHeaders();
            }
            break;
        default:
            LOGGER.warn("Should never happen - all client states are contemplated here");
        }
        return false;
    }

    private void transferBufferWithEmail(POP3ProxyState proxyState) {
        ByteBuffer originBuffer = proxyState.getOriginBuffer();
        originBuffer.flip(); // since it was in write mode

        ByteBuffer convertedOriginBuffer = proxyState
                .getConvertedOriginBuffer();
        // it should be cleared here, so do nothing

        POP3Line line = new POP3Line();
        EmailConverter converter = proxyState.getEmailConverter();
        while (converter != null && originBuffer.hasRemaining()
                && convertedOriginBuffer.hasRemaining()
                && !converter.isDoneConvertingHeaders()) {
            originBuffer.mark();
            line.read(originBuffer);
            if (line.isRead()) {
                String text;
                try {
                    text = line.asText();
                    String convertedText = converter.convertLine(text);
                    if (convertedText.length() <= convertedOriginBuffer
                            .remaining()) {
                        append(convertedOriginBuffer, convertedText.getBytes());
                    } else {
                        originBuffer.reset();
                    }
                } catch (UnsupportedEncodingException e) {
                    // dejarla pasar
                    if (line.length() <= convertedOriginBuffer.remaining()) {
                        append(convertedOriginBuffer, line.getBytes());
                    } else {
                        originBuffer.reset();
                    }
                }
            }
            line.clear();
        }
        originBuffer.compact();
        if (converter != null && converter.isDoneConvertingHeaders()) {
            proxyState.endEmailHeaderTransfer();
        }
    }

    private void connectToOrigin(SelectionKey key, POP3ProxyState proxyState,
            String user) {
        try {
            InetSocketAddress originAddress = adminModule
                    .getOriginAddressForUser(user);
            SocketChannel originChannel = nonBlockingSocket(originAddress);
            proxyState.setOriginChannel(originChannel);
            originChannel.register(key.selector(), SelectionKey.OP_CONNECT,
                    proxyState);
            reactor.subscribeChannel(originChannel, parent);
        } catch (IOException e) {
            sendResponseToClient(proxyState, ERR);
        }
    }

    private void sendResponseToClient(POP3ProxyState proxyState, String msg) {
        ByteBuffer originBuffer = proxyState.getOriginBuffer();
        originBuffer.clear();
        originBuffer.put(msg.getBytes());

        // Limpio este buffer para no enviarle el comando del cliente al
        // origin.
        proxyState.getClientBuffer().clear();
    }
}
