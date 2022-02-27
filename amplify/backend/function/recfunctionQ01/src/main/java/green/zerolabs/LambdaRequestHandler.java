package green.zerolabs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.service.S3EventService;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.function.Function;

@ApplicationScoped
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@Slf4j
public class LambdaRequestHandler implements RequestHandler<Map<String, Object>, String> {

  final Function<Map<String, Object>, String> INPUT_TO_STRING_FN =
      Unchecked.<Map<String, Object>, String>function(
          map -> getObjectMapper().writeValueAsString(map));

  @Inject ObjectMapper objectMapper;
  @Inject S3EventService s3EventService;

  @Override
  public String handleRequest(final Map<String, Object> input, final Context context) {
    if (log.isInfoEnabled()) {
      final String inputAsJson = INPUT_TO_STRING_FN.apply(input);
      log.info("{} input: {}", context.getAwsRequestId(), inputAsJson);
    }

    if (getS3EventService().isSupported(input, context)) {
      return getS3EventService()
          .handle(input, context)
          .map(aBoolean -> "Processed result=" + aBoolean)
          .await()
          .indefinitely();
    }

    return "Not Processed: " + input;
  }
}
