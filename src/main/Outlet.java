package main;

final public class Outlet {

    /**
     * Changes its status based on whether it is currently used or not
     */
    private final int id;

    public enum Status {
        AVAILABLE, BUSY
    }
    private Status status;

    public Outlet(int id) {
        this.id = id;
        this.status = Status.AVAILABLE;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
