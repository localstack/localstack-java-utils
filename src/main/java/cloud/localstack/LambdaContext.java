package cloud.localstack;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LambdaContext implements Context {

	private static final int DEFAULT_MEMORY_SIZE_IN_MB = 256;
	private static final String DEFAULT_ACCOUNT_ID = "123456789012";
	private static final String DEFAULT_REGION = "us-east-1";
	private static final String DEFAULT_FUNCTION_NAME = "localstack";
	private static final String DEFAULT_FUNCTION_VERSION = "$LATEST";

	private static final String TODAY = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
	private static final String CONTAINER_ID = UUID.randomUUID().toString();

	private transient final Logger LOG = Logger.getLogger(LambdaContext.class.getName());

	private final String requestId;

	public LambdaContext(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public String getAwsRequestId() {
		return requestId;
	}

	@Override
	public String getFunctionName() {
		String functionName = System.getenv("AWS_LAMBDA_FUNCTION_NAME");
		if (functionName == null) {
			functionName = DEFAULT_FUNCTION_NAME;
		}
		return functionName;
	}

	@Override
	public String getFunctionVersion() {
		String functionVersion = System.getenv("AWS_LAMBDA_FUNCTION_VERSION");
		if (functionVersion == null) {
			functionVersion = DEFAULT_FUNCTION_VERSION;
		}
		return functionVersion;
	}

	@Override
	public String getInvokedFunctionArn() {
		String region = System.getenv("AWS_REGION");
		if (region == null) {
			region = DEFAULT_REGION;
		}
		return String.format("arn:aws:%s:%s:function:%s:%s",
				region, DEFAULT_ACCOUNT_ID, getFunctionName(), getFunctionVersion());
	}

	@Override
	public String getLogGroupName() {
		return String.format("/aws/lambda/%s", getFunctionName());
	}

	@Override
	public String getLogStreamName() {
		return String.format("%s[%s]%s", TODAY, getFunctionVersion(), CONTAINER_ID);
	}

	@Override
	public int getMemoryLimitInMB() {
		String memorySize = System.getenv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE");
		if (memorySize == null) {
			return DEFAULT_MEMORY_SIZE_IN_MB;
		} else {
			try {
				return Integer.parseInt(memorySize);
			} catch (NumberFormatException e) {
				return DEFAULT_MEMORY_SIZE_IN_MB;
			}
		}
	}

	@Override
	public int getRemainingTimeInMillis() {
		return Integer.MAX_VALUE;
	}

	@Override
	public LambdaLogger getLogger() {
		return new LambdaLogger() {
			@Override
			public void log(String msg) {
				LOG.log(Level.INFO, msg);
			}

			@Override
			public void log(byte[] msg) {
				log(new String(msg));
			}
		};
	}

	@Override
	public ClientContext getClientContext() {
		return null;
	}

	@Override
	public CognitoIdentity getIdentity() {
		return null;
	}

}
