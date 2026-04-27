package ynu.pet.exception;

import lombok.Getter;

@Getter
public class BussinessException extends RuntimeException {

    private final Integer code;

    public BussinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BussinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BussinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}