package cloud.localstack.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.val;
import org.apache.commons.io.IOUtils;

/**
 * Utility methods used for the LocalStack unit and integration tests.
 *
 * @author Waldemar Hummer
 */
public class LocalTestUtil {

	public static com.amazonaws.services.lambda.model.FunctionCode createFunctionCode(Class<?> clazz) throws Exception {
		val code = new com.amazonaws.services.lambda.model.FunctionCode();
		code.setZipFile(createFunctionByteBuffer(clazz, false));
		return code;
	}

	public static software.amazon.awssdk.services.lambda.model.FunctionCode createFunctionCodeSDKV2(Class<?> clazz) throws Exception{
		val codeBuilder = software.amazon.awssdk.services.lambda.model.FunctionCode.builder();
		codeBuilder.zipFile(software.amazon.awssdk.core.SdkBytes.fromByteBuffer(createFunctionByteBuffer(clazz, true)));
		return codeBuilder.build();
	}

	private static ByteBuffer createFunctionByteBuffer(Class<?> clazz, boolean sdkv2) throws Exception{
		ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
		ByteArrayOutputStream jarOut = new ByteArrayOutputStream();
		// create zip file
		ZipOutputStream zipStream = new ZipOutputStream(zipOut);
		// create jar file
		JarOutputStream jarStream = new JarOutputStream(jarOut);

		// write class files into jar stream
		addClassToJar(clazz, jarStream);
		if (!sdkv2) {
            addClassToJar(com.amazonaws.services.kinesis.model.Record.class, jarStream);
            addClassToJar(com.amazonaws.services.lambda.runtime.events.SQSEvent.class, jarStream);
        }
		// write MANIFEST into jar stream
		JarEntry mfEntry = new JarEntry("META-INF/MANIFEST.MF");
		jarStream.putNextEntry(mfEntry);
		jarStream.closeEntry();
		jarStream.close();

		// write jar into zip stream
		ZipEntry codeEntry = new ZipEntry("LambdaCode.jar");
		zipStream.putNextEntry(codeEntry);
		zipStream.write(jarOut.toByteArray());
		zipStream.closeEntry();

		zipStream.close();
		return ByteBuffer.wrap(zipOut.toByteArray());
	}

	private static void addClassToJar(Class<?> clazz, JarOutputStream jarStream) throws IOException {
		String resource = clazz.getName().replace(".", File.separator) + ".class";
		JarEntry jarEntry = new JarEntry(resource);
		jarStream.putNextEntry(jarEntry);
		IOUtils.copy(LocalTestUtil.class.getResourceAsStream("/" + resource), jarStream);
		jarStream.closeEntry();
	}

	public static void retry(Runnable r) {
		retry(r, 5, 1);
	}

	public static void retry(Runnable r, int retries, double sleepSecs) {
		for (int i = 0; i < retries; i++) {
			try {
				r.run();
				return;
			} catch (Throwable e) {
				try {
					Thread.sleep((int)(sleepSecs * 1000));
				} catch (InterruptedException e1) {}
                if (i >= retries - 1) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
