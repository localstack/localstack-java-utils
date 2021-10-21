package cloud.localstack.lambda_handler;

public class NoMatchingHandlerException extends Exception {
    public NoMatchingHandlerException(String message) {
        super(message);
    }
}
