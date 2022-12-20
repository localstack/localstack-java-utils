package cloud.localstack;

import cloud.localstack.awssdkv1.lambda.DDBEventParser;
import cloud.localstack.awssdkv1.lambda.KinesisEventParser;
import cloud.localstack.awssdkv1.lambda.S3EventParser;

import cloud.localstack.lambda_handler.HandlerNameParseResult;
import cloud.localstack.lambda_handler.MultipleMatchingHandlersException;
import cloud.localstack.lambda_handler.NoMatchingHandlerException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Simple implementation of a Java Lambda function executor.
 *
 * @author Waldemar Hummer
 */
@SuppressWarnings("restriction")
public class LambdaExecutor {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		if (args.length != 1 && args.length != 2) {
			System.err.println("Usage: java " + LambdaExecutor.class.getSimpleName() +
					" [<lambdaClass>] <recordsFilePath>");
			System.exit(1);
		}

		String fileContent = args.length == 1 ? readFile(args[0]) : readFile(args[1]);
		ObjectMapper reader = new ObjectMapper();
		reader.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		reader.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Map<String,Object> map = reader.readerFor(Map.class).readValue(fileContent);

		List<Map<String,Object>> records = (List<Map<String, Object>>) get(map, "Records");
		Object inputObject = map;

		String handlerName;
		if (args.length == 2) {
			handlerName = args[0];
		} else {
			String handlerEnvVar = System.getenv("_HANDLER");
			if (handlerEnvVar == null) {
				System.err.println("Handler must be provided by '_HANDLER' environment variable");
				System.exit(1);
			}
			handlerName = handlerEnvVar;
		}
		HandlerNameParseResult parseResult = parseHandlerName(handlerName);
		Object handler = getHandler(parseResult.getClassName());
		String handlerMethodName = parseResult.getHandlerMethod();
		Method handlerMethod = handlerMethodName != null ? getHandlerMethodByName(handler, handlerMethodName) : null;
		if (records == null) {
			Optional<Object> deserializedInput = getInputObject(reader, fileContent, handler, handlerMethod);
			if (deserializedInput.isPresent()) {
				inputObject = deserializedInput.get();
			}
		} else {
			if (records.stream().anyMatch(record -> record.containsKey("kinesis") || record.containsKey("Kinesis"))) {
				inputObject = KinesisEventParser.parse(records);
			} else if (records.stream().anyMatch(record -> record.containsKey("Sns"))) {
				SNSEvent snsEvent = new SNSEvent();
				inputObject = snsEvent;
				snsEvent.setRecords(new LinkedList<>());
				for (Map<String, Object> record : records) {
					SNSEvent.SNSRecord r = new SNSEvent.SNSRecord();
					snsEvent.getRecords().add(r);
					SNSEvent.SNS snsRecord = new SNSEvent.SNS();
					Map<String, Object> sns = (Map<String, Object>) get(record, "Sns");
					snsRecord.setMessage((String) get(sns, "Message"));
					snsRecord.setMessageAttributes((Map<String, SNSEvent.MessageAttribute>) get(sns, "MessageAttributes"));
					snsRecord.setType("Notification");
					snsRecord.setTimestamp(new DateTime());
					r.setSns(snsRecord);
				}
			} else if (records.stream().anyMatch(record -> record.containsKey("dynamodb"))) {
				inputObject = DDBEventParser.parse(records);
			} else if (records.stream().anyMatch(record -> record.containsKey("s3"))) {
				inputObject = S3EventParser.parse(records);
			} else if (records.stream().anyMatch(record -> Objects.equals(record.get("eventSource"), "aws:sqs"))) {
				inputObject = reader.readValue(fileContent, SQSEvent.class);
			}
		}

		Context ctx = new LambdaContext(UUID.randomUUID().toString());
		if (handlerMethod != null || handler instanceof RequestHandler) {
			Object result;
			if (handlerMethod != null) {
				// use reflection to load handler method from class
				result = handlerMethod.invoke(handler, inputObject, ctx);
			} else {
				result = ((RequestHandler<Object, ?>) handler).handleRequest(inputObject, ctx);
			}
			try {
				result = new ObjectMapper().writeValueAsString(result);
			} catch (JsonProcessingException jsonException) {
				// continue with results as it is
			}
			// The contract with lambci is to print the result to stdout, whereas logs go to stderr
			System.out.println(result);
		} else if (handler instanceof RequestStreamHandler) {
			OutputStream os = new ByteArrayOutputStream();
			((RequestStreamHandler) handler).handleRequest(
					new StringInputStream(fileContent), os, ctx);
			System.out.println(os);
		}
	}

	/**
	 * Returns the method matching the specified name implemented in the given handler object class
	 * @param handler Handler the method in question belongs to
	 * @param handlerMethodName Name of the method we are looking for in the handler
	 * @return Method object for the method with the given method name
	 * @throws MultipleMatchingHandlersException Thrown when multiple methods in the given handler exist for the given name
	 * @throws NoMatchingHandlerException Thrown if no method in the handler is matching the given name
	 */
	private static Method getHandlerMethodByName(Object handler, String handlerMethodName) throws MultipleMatchingHandlersException, NoMatchingHandlerException {
		List<Method> handlerMethods = Arrays.stream(handler.getClass().getMethods())
				.filter(method -> method.getName().equals(handlerMethodName) && !method.isBridge()) // we do not want bridge methods here
				.collect(Collectors.toList());
		if (handlerMethods.size() > 1) {
			throw new MultipleMatchingHandlersException("Multiple matching handlers: " + handlerMethods);
		} else if (handlerMethods.isEmpty()) {
			throw new NoMatchingHandlerException("No matching handlers for method name: "
					+ handlerMethodName);
		}
		return handlerMethods.get(0);
	}

	/**
	 * Getting the input object for the handler function.
	 * @param mapper ObjectMapper that maps the objectString into the target parameter type
	 * @param objectString Object we got from the lambda invocation
	 * @param handler Handler object we need to get the correct input type
	 * @param handlerMethod Handler method we need to get the correct input type
	 * @return Optional of the input object for the lambda handler
	 */
	private static Optional<Object> getInputObject(ObjectMapper mapper, String objectString, Object handler, Method handlerMethod) {
		Optional<Object> inputObject = Optional.empty();
		try {
			if (handlerMethod != null) {
				Class<?> handlerInputType = Class.forName(handlerMethod.getParameterTypes()[0].getName());
				inputObject = Optional.of(mapper.readerFor(handlerInputType).readValue(objectString));
			} else {
				Optional<Type> handlerInterface = Arrays.stream(handler.getClass().getGenericInterfaces())
						.filter(genericInterface ->
								((ParameterizedType) genericInterface).getRawType().equals(RequestHandler.class))
						.findFirst();
				if (handlerInterface.isPresent()) {
					Class<?> handlerInputType = Class.forName(((ParameterizedType) handlerInterface.get())
							.getActualTypeArguments()[0].getTypeName());
					inputObject = Optional.of(mapper.readerFor(handlerInputType).readValue(objectString));
				}
			}
		} catch (Exception genericException) {
			// do nothing
		}
		return inputObject;
	}

	/**
	 * Parses the handler name
	 * Depending on the string, the result handlerMethod can be null
	 * @param handlerName Handler name in the format "java.package.class::handlerMethodName" or "java.package.class"
	 * @return Result containing the class name, and the handler method if specified
	 */
	private static HandlerNameParseResult parseHandlerName(String handlerName) {
		String[] split = handlerName.split("::", 2);
		String className = split[0];
		String handlerMethod = split.length > 1 ? split[1] : null;
		return new HandlerNameParseResult(className, handlerMethod);
	}


	/**
	 * Returns a instance of the class specified by handler name
	 * @param handlerName name (including package information) of the class to load and instantiate
	 * @return New object of handlerName class
	 */
	private static Object getHandler(String handlerName) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException, ClassNotFoundException {
		Class<?> clazz = Class.forName(handlerName);
		return clazz.getConstructor().newInstance();
	}

	public static <T> T get(Map<String, T> map, String key) {
		T result = map.get(key);
		if (result != null) {
			return result;
		}
		key = StringUtils.uncapitalize(key);
		result = map.get(key);
		if (result != null) {
			return result;
		}
		return map.get(key.toLowerCase());
	}

	public static String readFile(String file) throws Exception {
		if (!file.startsWith("/")) {
			file = System.getProperty("user.dir") + "/" + file;
		}
		return Files.lines(Paths.get(file), StandardCharsets.UTF_8).collect(Collectors.joining());
	}

}
