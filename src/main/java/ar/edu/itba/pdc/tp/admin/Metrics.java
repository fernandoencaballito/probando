package ar.edu.itba.pdc.tp.admin;

public class Metrics {

    private long accesses;
    private long bytesTransfered;

    public Metrics() {
        this.accesses = 0;
        this.bytesTransfered = 0;
    }

    public void addAccess() {
        this.accesses++;
    }

    public void addBytesTransfered(long amount) {
        bytesTransfered += amount;
    }

    public long getAccesses() {
        return accesses;
    }

    public void setAccesses(long accesses) {
        this.accesses = accesses;
    }

    public long getBytesTransfered() {
        return bytesTransfered;
    }

    public void setBytesTransfered(long bytesTransfered) {
        this.bytesTransfered = bytesTransfered;
    }
}
