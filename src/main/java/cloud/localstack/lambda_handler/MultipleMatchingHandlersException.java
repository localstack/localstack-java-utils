package cloud.localstack.lambda_handler;

public class MultipleMatchingHandlersException extends Exception {
    public MultipleMatchingHandlersException(String message) {
        super(message);
    }
}
