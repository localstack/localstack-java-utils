package cloud.localstack.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Utility methods used for the LocalStack unit and integration tests.
 *
 * @author Waldemar Hummer
 */
public class LocalTestUtil {

	protected static ByteBuffer createFunctionByteBuffer(Class<?> clazz, Class<?> ... additionalClasses) throws Exception{
		ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
		ByteArrayOutputStream jarOut = new ByteArrayOutputStream();
		// create zip file
		ZipOutputStream zipStream = new ZipOutputStream(zipOut);
		// create jar file
		JarOutputStream jarStream = new JarOutputStream(jarOut);

		// write class files into jar stream
		addClassToJar(clazz, jarStream);
		for (Class<?> _class : additionalClasses) {
            addClassToJar(_class, jarStream);
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
