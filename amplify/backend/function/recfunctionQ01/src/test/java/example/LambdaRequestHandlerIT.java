package example;

import example.model.S3EventMessage;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
@Slf4j
public class LambdaRequestHandlerIT {

  @Test
  public void testSimpleLambdaSuccess() throws Exception {
    // you test your lambas by invoking on http://localhost:8081
    // this works in dev mode too

    final String property = System.getProperty("aws.secretAccessKey");
    if (property == null || property.isBlank()) {
      log.error("missing 'aws.secretAccessKey'. will exit");
      return;
    }

    final S3EventMessage body =
        S3EventMessage.builder()
            .records(
                List.of(
                    S3EventMessage.Record.builder()
                        .awsRegion(System.getenv("REGION"))
                        .s3(
                            S3EventMessage.Record.S3
                                .builder()
                                .bucket(
                                    S3EventMessage.Record.S3
                                        .Bucket
                                        .builder()
                                        .name(
                                            "zlamplifypoc0027b2989e5e3384aec95d0349f54a34f3191504-dev")
                                        .build())
                                .object(
                                    S3EventMessage.Record.S3
                                        .Object
                                        .builder()
                                        .key("tmp.txt")
                                        .build())
                                .build())
                        .build()))
            .build();
    final String result =
        given()
            .contentType("application/json")
            .accept("application/json")
            .body(body)
            .when()
            .post()
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();
    log.info("result: {}", result);
  }
}
