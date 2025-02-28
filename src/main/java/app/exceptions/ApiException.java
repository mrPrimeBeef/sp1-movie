package app.exceptions;

public class ApiException extends RuntimeException {
    private int code;

    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }
    public ApiException(String msg) {
        super(msg);
    }
    public int getCode(){
        return code;
    }
}
