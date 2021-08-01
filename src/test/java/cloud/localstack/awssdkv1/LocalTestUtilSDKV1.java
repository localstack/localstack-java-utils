package cloud.localstack.awssdkv2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import org.apache.commons.io.IOUtils;

import cloud.localstack.utils.LocalTestUtil;

public class LocalTestUtilSDKV1 extends LocalTestUtil {

	public static com.amazonaws.services.lambda.model.FunctionCode createFunctionCode(Class<?> clazz) throws Exception {
		com.amazonaws.services.lambda.model.FunctionCode code = new com.amazonaws.services.lambda.model.FunctionCode();
		code.setZipFile(createFunctionByteBuffer(clazz, Record.class, SQSEvent.class));
		return code;
	}

}
