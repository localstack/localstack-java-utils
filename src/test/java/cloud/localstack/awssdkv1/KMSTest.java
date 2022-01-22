package cloud.localstack.awssdkv1;

import java.nio.ByteBuffer;

import cloud.localstack.docker.annotation.LocalstackDockerProperties;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.CreateKeyRequest;
import com.amazonaws.services.kms.model.CreateKeyResult;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test integration of KMS with LocalStack
 */
@LocalstackDockerProperties(ignoreDockerRunErrors = true)
public class KMSTest extends PowerMockLocalStack {
    private AWSKMS awskms;
    private String keyId = "";

    @Before
    public void mock() {
        PowerMockLocalStack.mockKMS();
        awskms = AWSKMSClientBuilder.defaultClient();
    }

    @Test
    public void createKeyTest() throws Exception {
        CreateKeyRequest request = new CreateKeyRequest().withDescription("test key");
        CreateKeyResult result = awskms.createKey(request);
        keyId = result.getKeyMetadata().getKeyId();
        Assert.assertNotNull(keyId);
    }

    @Test
    public void stringEncriptionTest() throws Exception {
        if(keyId.isEmpty()){
            createKeyTest();
        }

        String testMessage =  "hey, this is a test of encryption";
        byte[] encodedMessage = encrypt(testMessage.getBytes());
        byte[] decodedMessage = decrypt(encodedMessage);
        String resultMessage = new String(decodedMessage);

        Assert.assertEquals(testMessage, resultMessage);
    }

    public byte[] encrypt(byte[] input) {
        ByteBuffer buffer = ByteBuffer.wrap(input);
        EncryptResult result = awskms.encrypt(new EncryptRequest().withKeyId(keyId).withPlaintext(buffer));
        return result.getCiphertextBlob().array();
    }

    public byte[] decrypt(byte[] input) {
        ByteBuffer buffer = ByteBuffer.wrap(input);
        DecryptResult result = awskms.decrypt(new DecryptRequest().withCiphertextBlob(buffer));
        return result.getPlaintext().array();
    }

}
