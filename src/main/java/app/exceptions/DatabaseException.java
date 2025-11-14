package app.exceptions;

public class DatabaseException extends RuntimeException {
    private int code;

    public DatabaseException(int code, String message){
        super(message);
        this.code = code;
    }

    public DatabaseException(String message) {
        super(message);
    }


    public int getCode(){
        return this.code;
    }

}
