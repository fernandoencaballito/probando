package ar.edu.itba.pdc.tp.admin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class AdminProtocolState {

    private static final int LINE_SIZE = 512;

    private AdminStateEnum state = AdminStateEnum.EXPECT_PASS;
    private ByteBuffer lastLine = null;
    private int readCount = 0;
    private boolean lineDone;

    public enum BufferStateEnum {
        READING, CARRIAGE_RETURN, NEWLINES
    }

    private BufferStateEnum bufferState;

    private ByteBuffer clientBuffer;

    public AdminProtocolState(int bufSize) {
        clientBuffer = ByteBuffer.allocate(bufSize);
        bufferState = BufferStateEnum.READING;
        lastLine = ByteBuffer.allocate(bufSize);
    }

    public AdminStateEnum getState() {
        return state;
    }

    public void setState(AdminStateEnum state) {
        this.state = state;
    }

    public ByteBuffer getLastLine() {
        readCount = 0;
        lineDone = false;
        ByteBuffer ans = lastLine;
        return ans;
    }

    public void clearLastLine() {
        lastLine.clear();
        lineDone = false;
    }

    public int getReadCount() {
        return readCount;
    }

    public boolean isLineDone() {
        return lineDone;
    }

    ByteBuffer getClientBuffer() {
        return clientBuffer;
    }

    public ByteBuffer getBuffer() {
        return clientBuffer;
    }

    public void putChar(byte b) {
        char c = (char) b;
        lastLine.put(b);
        readCount++;

        if (bufferState == BufferStateEnum.CARRIAGE_RETURN && c == '\n') {
            lineDone = true;
            return;
        } else {
            bufferState = BufferStateEnum.READING;
        }

        if (c == '\r')
            bufferState = BufferStateEnum.CARRIAGE_RETURN;
        lineDone = (readCount == LINE_SIZE);
    }

    public long readFromChannel(SocketChannel clntChan) throws IOException {

        long bytesRead = clntChan.read(clientBuffer);
        clientBuffer.flip();
        while (clientBuffer.hasRemaining() && !isLineDone()) {
            putChar(clientBuffer.get());
        }
        while(clientBuffer.hasRemaining() && isLineDone()){
        	clientBuffer.get();
        }
        
        return bytesRead;
    }
}
