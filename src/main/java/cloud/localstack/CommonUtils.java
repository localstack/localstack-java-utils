package cloud.localstack;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.stream.Stream;

/**
 * Common utility methods
 */
public class CommonUtils {

    private static final String[] EXCLUDED_DIRECTORIES = {
        ".github", ".git", ".idea", ".venv", "target", "node_modules"
    };

    public static void disableSslCertChecking() {
        System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");
    }

    public static void setEnv(String key, String value) {
        Map<String, String> newEnv = new HashMap<String, String>(System.getenv());
        newEnv.put(key, value);
        setEnv(newEnv);
    }

    protected static void setEnv(Map<String, String> newEnv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newEnv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newEnv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newEnv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void copyFolder(Path src, Path dest) throws IOException {
        try(Stream<Path> stream = Files.walk(src)) {
            stream.forEach(source -> {
                boolean isExcluded = Arrays.stream(EXCLUDED_DIRECTORIES)
                    .anyMatch( excluded -> source.toAbsolutePath().toString().contains(excluded));
                if (!isExcluded) {
                    copy(source, dest.resolve(src.relativize(source)));
                }
            });
        }
    }

    public static void copy(Path source, Path dest) {
        try {
            CopyOption[] options = new CopyOption[] {StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING};
            if(Files.isDirectory(dest)) {
                // continue without copying
                return;
            }
            if (Files.exists(dest)) {
                try(FileChannel sourceFile = FileChannel.open(source)) {
                    try (FileChannel destFile = FileChannel.open(dest)) {
                        if (!Files.getLastModifiedTime(source).equals(Files.getLastModifiedTime(dest))
                                || sourceFile.size() != destFile.size()
                        ) {
                            Files.copy(source, dest, options);
                        }
                    }
                }
            } else {
                Files.copy(source, dest, options);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
