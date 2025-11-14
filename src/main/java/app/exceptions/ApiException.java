package app.exceptions;

//Unchecked exception (runtime)
public class ApiException extends RuntimeException {

    private int code;

    public ApiException(String message) {
        super(message);
    }

    public ApiException(int code, String message){
        super(message);
        this.code = code;
    }

    public ApiException(int code, String message, Throwable cause){
        super(message, cause);
        this.code = code;
    }

    public int getCode(){
        return this.code;
    }

}
