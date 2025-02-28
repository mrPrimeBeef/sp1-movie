package app.exceptions;

public class PersistExeception extends RuntimeException{
    public PersistExeception(String msg) {
        super(msg);
    }
}
