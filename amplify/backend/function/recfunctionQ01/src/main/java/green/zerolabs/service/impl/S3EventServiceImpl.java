package green.zerolabs.service.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.model.S3EventMessage;
import green.zerolabs.service.S3EventService;
import green.zerolabs.service.ZlContractService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
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
  private final S3AsyncClient s3AsyncClient;
  private final ZlContractService zlContractService;

  public S3EventServiceImpl(
      final ObjectMapper objectMapper,
      final S3AsyncClient s3AsyncClient,
      final ZlContractService zlContractService) {
    this.objectMapper = objectMapper;
    this.s3AsyncClient = s3AsyncClient;
    this.zlContractService = zlContractService;
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
  public Uni<Boolean> handle(final Map<String, Object> input, final Context context) {
    final S3EventMessage eventMessage = INPUT_TO_EVENT_FN.apply(input);

    return Multi.createFrom()
        .iterable(eventMessage.getRecords())
        .flatMap(
            record ->
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
                    .onItem()
                    .invoke(o -> log.info("{} file: {}", context.getAwsRequestId(), o))
                    .map(s -> Tuple2.of(record, s))
                    .convert()
                    .toPublisher())
        .flatMap(
            objects ->
                (getZlContractService().isSupported(objects.getItem1(), objects.getItem2(), context)
                        ? getZlContractService()
                            .handle(objects.getItem1(), objects.getItem2(), context)
                        : Uni.createFrom().item(true))
                    .convert()
                    .toPublisher())
        .collect()
        .asList()
        .map(
            booleans ->
                booleans.stream()
                    .reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2)
                    .orElse(true))
        .onFailure()
        .recoverWithItem(false);
  }
}
