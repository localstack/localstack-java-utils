package cloud.localstack.awssdkv2;

import cloud.localstack.Constants;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import cloud.localstack.Localstack;

import com.amazon.ion.system.IonSystemBuilder;
import com.fasterxml.jackson.dataformat.ion.IonObjectMapper;
import com.fasterxml.jackson.dataformat.ion.ionvalue.IonValueMapper;
import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.services.qldb.*;
import software.amazon.awssdk.services.qldb.model.*;
import software.amazon.qldb.*;
import software.amazon.awssdk.services.qldbsession.*;
import com.amazon.ion.*;

import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(ignoreDockerRunErrors=true)
public class ProFeaturesSDKV2Test {
    public static final IonSystem SYSTEM = IonSystemBuilder.standard().build();
    public static final IonObjectMapper MAPPER = new IonValueMapper(SYSTEM);

    private static final Logger LOG = Logger.getLogger(ProFeaturesSDKV2Test.class.getName());

    @Test
    public void testCreateListTables() throws Exception {
        if (!isProEnabled()) {
            return;
        }

        String ledgerName = "l123";
        QldbAsyncClient client = TestUtils.getClientQLDBAsyncV2();

        String tableName1 = "table1";
        String tableName2 = "table2";
        createLedgerAndTables(ledgerName, tableName1, tableName2);
        QldbDriver driver = getDriver(ledgerName);

        // list tables
        List<String> tableNames = new ArrayList<String>();
        driver.getTableNames().forEach(tableNames::add);
        Assert.assertTrue(tableNames.contains(tableName1));
        Assert.assertTrue(tableNames.contains(tableName2));

        // list tables via query
        String query = "SELECT VALUE name FROM information_schema.user_tables WHERE status = 'ACTIVE'";
        Result result = driver.execute(txn -> { return txn.execute(query); });
        Assert.assertNotNull(result);

        // list result entries
        List<String> tableNames2 = new ArrayList<>();
        result.forEach(v -> tableNames2.add(((IonString)v).stringValue()));
        Assert.assertTrue(tableNames2.contains(tableName1));
        Assert.assertTrue(tableNames2.contains(tableName2));

        // clean up
        client.deleteLedger(DeleteLedgerRequest.builder().name(ledgerName).build());
    }

    @Test
    public void testCreateListIndexes() throws Exception {
        if (!isProEnabled()) {
            return;
        }
        String ledgerName = "l123";
        String tableName1 = "table1";

        QldbDriver driver = getDriver(ledgerName);
        createLedgerAndTables(ledgerName, tableName1);

        String query1 = "CREATE INDEX on " + tableName1 + "(attr1)";
        driver.execute(txn -> { return txn.execute(query1); });

        String query2 = "SELECT VALUE indexes FROM information_schema.user_tables info, info.indexes indexes";
        Result indexQueryResult = driver.execute(txn -> {
            return txn.execute(query2);
        });

        Set<String> result = StreamSupport.stream(indexQueryResult.spliterator(), false)
                .map(v -> (IonStruct) v)
                .map(s -> ((IonString)s.get("expr")).stringValue())
                .collect(Collectors.toSet());
        Assert.assertEquals(new HashSet<>(Arrays.asList("[attr1]")), result);

        // clean up
        cleanUp(ledgerName);
    }

    @Test
    public void testUpdateQueryDataTypes() throws Exception {
        if (!isProEnabled()) {
            return;
        }
        LOG.info("Running testUpdateQueryDataTypes to check QLDB query data types...");

        String tableName1 = "Wallet";
        String ledgerName = "l123";
        createLedgerAndTables(ledgerName, tableName1);
        QldbDriver driver = getDriver(ledgerName);

        Wallet wallet = new Wallet();
        wallet.setId("1");
        wallet.setDescription("my personal wallet");
        wallet.setBalance(25d);
        wallet.setTags(ImmutableMap.of("meta", "true"));
        wallet.setType(WalletType.PERSONAL);

        driver.execute(txn -> {
            try {
                txn.execute("INSERT INTO Wallet ?", MAPPER.writeValueAsIonValue(wallet));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        wallet.setDescription("my test wallet");
        wallet.setBalance(26.12d);
        wallet.setTags(ImmutableMap.of());
        wallet.setType(WalletType.BUSINESS);

        String query = "UPDATE Wallet \nSET description = ?,\n balance = ?,\n tags = ?,\n type = ?\n WHERE id = ?";
        driver.execute(txn -> {
            try {
                return txn.execute(query,
                    MAPPER.writeValueAsIonValue(wallet.getDescription()),
                    MAPPER.writeValueAsIonValue(wallet.getBalance()),
                    MAPPER.writeValueAsIonValue(wallet.getTags()),
                    MAPPER.writeValueAsIonValue(wallet.getType()),
                    MAPPER.writeValueAsIonValue(wallet.getId()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        Result queryResult = driver.execute(txn -> {
            try {
                return txn.execute("SELECT * FROM Wallet WHERE id = ?", MAPPER.writeValueAsIonValue(wallet.getId()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        Set<String> result = StreamSupport.stream(queryResult.spliterator(), false)
                .map(v -> (IonStruct) v)
                .map(s -> s.get("balance").toString())
                .collect(Collectors.toSet());
        Assert.assertEquals(new HashSet<String>(Arrays.asList("26.12")), result);

        // clean up
        cleanUp(ledgerName);
    }

    @Test
    public void testCreateDropTable() throws Exception {
        if (!isProEnabled()) {
            return;
        }
        LOG.info("Running test testCreateDropTable() ...");
        String ledgerName = "l123";
        QldbDriver driver = createLedgerAndGetDriver(ledgerName);
        driver.execute(txn -> {
            txn.execute("CREATE TABLE A");
        });

        driver.execute(txn -> {
            txn.execute("DROP TABLE A");
        });

        Set<String> tableNames = StreamSupport.stream(driver.getTableNames().spliterator(), false)
                .collect(Collectors.toSet());

        Assert.assertTrue(tableNames.isEmpty());
    }

    // UTIL FUNCTIONS AND CLASSES BELOW

    public static class Wallet {
        String id;
        String description;
        double balance;
        Map<String, String> tags;
        WalletType type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }

        public WalletType getType() {
            return type;
        }

        public void setType(
                WalletType type) {
            this.type = type;
        }
    }

    public enum WalletType {
        PERSONAL,
        BUSINESS
    }


    private QldbDriver createLedgerAndGetDriver(String ledgerName, String ... tableNames) throws Exception {
        createLedgerAndTables(ledgerName, tableNames);
        return getDriver(ledgerName);
    }

    private void createLedgerAndTables(String ledgerName, String ... tableNames) throws Exception {
        QldbAsyncClient client = TestUtils.getClientQLDBAsyncV2();

        CreateLedgerRequest request = CreateLedgerRequest.builder().name(ledgerName).build();
        CreateLedgerResponse ledger = client.createLedger(request).get();
        Assert.assertEquals(ledger.name(), ledgerName);

        QldbDriver driver = getDriver(ledgerName);

        for (String tableName : tableNames) {
            driver.execute(txn -> { return txn.execute("CREATE TABLE " + tableName); });
        }
    }

    private QldbDriver getDriver(String ledgerName) throws Exception {
        return QldbDriver.builder().ledger(ledgerName)
                .sessionClientBuilder(
                        QldbSessionClient.builder().endpointOverride(new URI(Localstack.INSTANCE.getEndpointQLDB()))
                ).build();
    }

    private void cleanUp(String ledgerName) {
        QldbAsyncClient client = TestUtils.getClientQLDBAsyncV2();
        client.deleteLedger(DeleteLedgerRequest.builder().name(ledgerName).build());
    }

    private boolean isProEnabled() {
        return System.getenv(Constants.ENV_LOCALSTACK_API_KEY) != null;
    }
}
