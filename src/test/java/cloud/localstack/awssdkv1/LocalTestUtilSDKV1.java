package cloud.localstack.awssdkv1;

import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import cloud.localstack.utils.LocalTestUtil;

public class LocalTestUtilSDKV1 extends LocalTestUtil {

	public static com.amazonaws.services.lambda.model.FunctionCode createFunctionCode(Class<?> clazz) throws Exception {
		com.amazonaws.services.lambda.model.FunctionCode code = new com.amazonaws.services.lambda.model.FunctionCode();
		code.setZipFile(createFunctionByteBuffer(clazz, Record.class, SQSEvent.class));
		return code;
	}

}
