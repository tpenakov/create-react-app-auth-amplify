package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.service.S3EventService;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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

  void startup(
      @Observes final StartupEvent event,
      final ObjectMapper objectMapper,
      final S3EventService s3EventService) {}

  @Override
  public String handleRequest(final Map<String, Object> input, final Context context) {
    if (log.isInfoEnabled()) {
      context.getLogger().log("objectMapper: " + getObjectMapper());
      context.getLogger().log("\ns3EventService: " + getS3EventService());
      final String inputAsJson = INPUT_TO_STRING_FN.apply(input);
      log.info("\ninput: {}", inputAsJson);
      context.getLogger().log("input: " + inputAsJson);
    }

    if (getS3EventService().isSupported(input, context)) {
      getS3EventService().handle(input, context);
    }

    return "Hello " + input;
  }
}
