package cloud.localstack.awssdkv1;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(services = {"iam"}, ignoreDockerRunErrors=true)
public class IAMTest {

	/**
	 * Test the creation of a IAM user.
	 */
	@Test
	public void testUserCreation() throws Exception {
		AmazonIdentityManagement iamClient = TestUtils.getClientIAM();

		String username =  UUID.randomUUID().toString();
		CreateUserRequest createUserRequest = new CreateUserRequest(username);
		iamClient.createUser(createUserRequest);

		ListUsersRequest listUsersRequest = new ListUsersRequest();
		ListUsersResult response = iamClient.listUsers(listUsersRequest);

		boolean userFound = false;
		for (User user : response.getUsers()) {

			if(user.getUserName().equals(username)){
				userFound = true;
				break;
			}
		}

		assertEquals(true, userFound);

	}
    
	@Test
	public void testIAMListUserPagination() throws Exception {
		AmazonIdentityManagement iamClient = TestUtils.getClientIAM();

		String username = UUID.randomUUID().toString();
		CreateUserRequest createUserRequest = new CreateUserRequest(username);
		iamClient.createUser(createUserRequest);

		AtomicBoolean userFound = new AtomicBoolean(false);
		iamClient.listUsers().getUsers().forEach(user->{
			if(user.getUserName().equals(username)){
				userFound.set(true);;
			}
		});
		
		assertTrue(userFound.get());
	}
}
