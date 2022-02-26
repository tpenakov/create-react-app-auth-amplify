package example.service.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.model.S3EventMessage;
import example.service.S3EventService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/*
 * Created by triphon 26.02.22 г.
 */
@ApplicationScoped
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class S3EventServiceImpl implements S3EventService {
  final Function<Map<String, Object>, S3EventMessage> INPUT_TO_EVENT_FN =
      Unchecked.function(
          map ->
              getObjectMapper()
                  .readValue(getObjectMapper().writeValueAsString(map), S3EventMessage.class));

  private final ObjectMapper objectMapper;
  final S3AsyncClient s3AsyncClient;

  public S3EventServiceImpl(final ObjectMapper objectMapper, final S3AsyncClient s3AsyncClient) {
    this.objectMapper = objectMapper;
    this.s3AsyncClient = s3AsyncClient;
  }

  @Override
  public Boolean isSupported(final Map<String, Object> input, final Context context) {
    return Optional.ofNullable(input)
        .filter(map -> map.containsKey(S3EventMessage.RECORDS))
        .filter(map -> null != map.get(S3EventMessage.RECORDS))
        .map(map -> map.get(S3EventMessage.RECORDS) instanceof List)
        .orElse(false);
  }

  @Override
  public void handle(final Map<String, Object> input, final Context context) {
    final S3EventMessage eventMessage = INPUT_TO_EVENT_FN.apply(input);

    eventMessage
        .getRecords()
        .forEach(
            record -> {
              final String result =
                  Uni.createFrom()
                      .completionStage(
                          getS3AsyncClient()
                              .getObject(
                                  builder ->
                                      builder
                                          .bucket(record.getS3().getBucket().getName())
                                          .key(record.getS3().getObject().getKey()),
                                  AsyncResponseTransformer.toBytes()))
                      .map(bytes -> bytes.asUtf8String())
                      .await()
                      .indefinitely();

              if (log.isInfoEnabled()) {
                log.info("result: {}", result);
                context.getLogger().log("result: " + result);
              }
            });
  }
}
