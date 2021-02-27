package cloud.localstack.docker.annotation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public interface IBindMountProvider extends Supplier<Map<String, String>> {

    class EmptyBindMountProvider implements IBindMountProvider {

        @Override
        public Map<String, String> get() {
            return Collections.emptyMap();
        }
    }

    abstract class BaseBindMountProvider implements IBindMountProvider {

        private Map<String, String> mounts = new HashMap<>();

        protected BaseBindMountProvider() {
            initValues(mounts);
        }

        protected abstract void initValues(Map<String, String> mounts);

        @Override
        public final Map<String, String> get() {
            return mounts;
        }
    }
}
