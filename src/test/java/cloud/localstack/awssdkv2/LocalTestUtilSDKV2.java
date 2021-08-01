package cloud.localstack.awssdkv2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import software.amazon.awssdk.services.lambda.model.FunctionCode;
import software.amazon.awssdk.core.SdkBytes;

import org.apache.commons.io.IOUtils;
import cloud.localstack.utils.LocalTestUtil;

public class LocalTestUtilSDKV2 extends LocalTestUtil {

	public static FunctionCode createFunctionCode(Class<?> clazz) throws Exception{
		FunctionCode.Builder codeBuilder = FunctionCode.builder();
		codeBuilder.zipFile(SdkBytes.fromByteBuffer(createFunctionByteBuffer(clazz)));
		return codeBuilder.build();
	}

}
