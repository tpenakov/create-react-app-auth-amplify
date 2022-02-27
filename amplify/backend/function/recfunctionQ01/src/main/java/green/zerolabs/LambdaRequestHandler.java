package green.zerolabs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import green.zerolabs.service.S3EventService;
import green.zerolabs.utils.JsonUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@Slf4j
public class LambdaRequestHandler implements RequestHandler<Map<String, Object>, String> {

  @Inject JsonUtils jsonUtils;
  @Inject S3EventService s3EventService;

  @Override
  public String handleRequest(final Map<String, Object> input, final Context context) {
    if (log.isInfoEnabled()) {
      final String jsonString = getJsonUtils().toString(input);
      log.info("{} input: {}", context.getAwsRequestId(), jsonString);
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
