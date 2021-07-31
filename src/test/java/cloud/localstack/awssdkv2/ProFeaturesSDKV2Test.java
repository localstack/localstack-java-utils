package cloud.localstack.awssdkv2;

import cloud.localstack.Constants;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import cloud.localstack.sample.LambdaHandler;
import cloud.localstack.utils.LocalTestUtil;
import cloud.localstack.Localstack;

import software.amazon.awssdk.services.qldb.*;
import software.amazon.awssdk.services.qldb.model.*;
import software.amazon.qldb.*;
import software.amazon.awssdk.services.qldbsession.*;

import org.junit.*;
import org.junit.runner.RunWith;

import java.net.*;
import java.util.*;

@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(ignoreDockerRunErrors=true)
public class ProFeaturesSDKV2Test {

    @Test
    public void testQueryQLDBLedger() throws Exception {
        if (System.getenv(Constants.ENV_LOCALSTACK_API_KEY) == null) {
            return;
        }

        QldbAsyncClient client = TestUtils.getClientQLDBAsyncV2();

        String ledgerName = "l123";
        CreateLedgerRequest request = CreateLedgerRequest.builder().name(ledgerName).build();
        CreateLedgerResponse ledger = client.createLedger(request).get();
        Assert.assertEquals(ledger.name(), ledgerName);

        QldbDriver driver = QldbDriver.builder().ledger(ledgerName)
            .sessionClientBuilder(
                QldbSessionClient.builder().endpointOverride(new URI(Localstack.INSTANCE.getEndpointQLDB()))
            ).build();

        // create tables
        String tableName1 = "table1";
        String tableName2 = "table2";
        driver.execute(txn -> { return txn.execute("CREATE TABLE " + tableName1); });
        driver.execute(txn -> { return txn.execute("CREATE TABLE " + tableName2); });

        // list tables
        List<String> tableNames = new ArrayList<String>();
        driver.getTableNames().forEach(tableNames::add);
        System.out.println("tableNames " + tableNames);
        Assert.assertTrue(tableNames.contains(tableName1));
        Assert.assertTrue(tableNames.contains(tableName2));

        String query = "SELECT VALUE name FROM information_schema.user_tables WHERE status = 'ACTIVE'";
        Result result = driver.execute(txn -> { return txn.execute(query); });
        System.out.println("result " + result);
    }

}
