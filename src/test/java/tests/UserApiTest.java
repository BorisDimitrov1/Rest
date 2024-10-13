package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.CreateUserResponse;
import model.User;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UserApiTest {

    private static final String USERS_END_POINT = "/users";
    private static final String GET_USERS_LIST = USERS_END_POINT + "?page=2";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = System.getProperty("BaseURL");
    }

    @Test
    public void getUsersList() {
        Response response = RestAssured.given()
                .when()
                .get(GET_USERS_LIST)
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertNotNull(response.jsonPath().get("data"));
        Assert.assertTrue(response.jsonPath().getList("data").size() > 0);
    }

    @Test
    public void createUser() {
        User user = new User("Georgi", "Banker");

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(user)
                .when()
                .post(USERS_END_POINT)
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 201);
        Assert.assertEquals(response.jsonPath().get("name"), "Georgi");
        Assert.assertEquals(response.jsonPath().get("job"), "Banker");
        Assert.assertNotNull(response.jsonPath().get("id"));
        Assert.assertNotNull(response.jsonPath().get("createdAt"));
    }

    /**
     * Normally I would implement the negative cases with missing name or job.
     * Too long name or job, special characters in the name or job. Field that is not expected, malformed json
     * Wrong contentType, empty body etc... but the user is being created no matter what
     * This is the only way for me to get response other than 201 - no content type and no body
     */
    @Test
    public void createUserWithoutContentTypeAndBody() {
        Response response = RestAssured.given()
                .when()
                .post(USERS_END_POINT)
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 415);
        Assert.assertEquals(response.getStatusLine(), "HTTP/1.1 415 Unsupported Media Type");
    }


    @Test
    public void updateUser() {
        User user = new User("Georgi", "Cleaning guy");

        //Create the user that is going to be updated and get the id
        CreateUserResponse createdUser = RestAssured.given()
                .contentType("application/json")
                .body(user)
                .when()
                .post(USERS_END_POINT)
                .then()
                .extract()
                .as(CreateUserResponse.class);

        //Update the user
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(user)
                .when()
                .put(USERS_END_POINT + "/" + createdUser.getId())
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("name"), "Georgi");
        Assert.assertEquals(response.jsonPath().get("job"), "Cleaning guy");
        Assert.assertNotNull(response.jsonPath().get("updatedAt"));
    }


    /**
     * Again the negative cases for PUT /users/{id} would be - id that is not created, missing name or job.
     * Too long name or job,special characters in the name or job. Field that is not expected, malformed json
     * Wrong contentType, empty body etc... but the user is being updated no matter what
     */
}
