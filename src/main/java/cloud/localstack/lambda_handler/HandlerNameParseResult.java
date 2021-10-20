package cloud.localstack.lambda_handler;

public class HandlerNameParseResult {
    private final String className;
    private final String handlerMethod;

    public HandlerNameParseResult(String className, String handlerMethod) {
        this.className = className;
        this.handlerMethod = handlerMethod;
    }

    public String getClassName() {
        return className;
    }

    public String getHandlerMethod() {
        return handlerMethod;
    }
}
