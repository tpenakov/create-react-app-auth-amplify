package green.zerolabs.service.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import green.zerolabs.model.S3EventMessage;
import green.zerolabs.model.ZlContract;
import green.zerolabs.model.db.ZlDbItem;
import green.zerolabs.service.ZlContractService;
import green.zerolabs.utils.JsonUtils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bk.aws.dynamo.util.JsonAttributeValueUtil;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Created by triphon 27.02.22 г.
 */
@ApplicationScoped
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class ZlContractServiceImpl implements ZlContractService {

  public static final String CONTRACT_PREFIX = "contract-";
  public static final String CONTRACT_DATA = CONTRACT_PREFIX + "data";
  public static final String CONTRACT_VERSION = CONTRACT_PREFIX + "version";
  public static final String CONTRACT_STATUS = CONTRACT_PREFIX + "status";
  public static final String CURRENT_CONTRACT_VERSION = "0001";
  // todo - add table name - configurable
  public static final String TABLE_NAME = "zlMvpDataTable-dev";

  private final JsonUtils jsonUtils;
  private final DynamoDbAsyncClient dynamoDB;
  private final Function<Object, JsonNode> objectToJsonNode;

  public ZlContractServiceImpl(final JsonUtils jsonUtils, final DynamoDbAsyncClient dynamoDB) {
    this.jsonUtils = jsonUtils;
    this.dynamoDB = dynamoDB;
    objectToJsonNode =
        Unchecked.<Object, JsonNode>function(
            zlDbItem ->
                getJsonUtils()
                    .getObjectMapper()
                    .readTree(getJsonUtils().toStringLazy(zlDbItem).toString()));
    ;
  }

  @Override
  public Boolean isSupported(
      final S3EventMessage.Record input, final String data, final Context context) {
    return Optional.ofNullable(input)
        .map(S3EventMessage.Record::getS3)
        .map(S3EventMessage.Record.S3::getObject)
        .map(S3EventMessage.Record.S3.Object::getKey)
        .map(s -> s.startsWith("contracts/"))
        .orElse(false);
  }

  @Override
  public Uni<Boolean> handle(
      final S3EventMessage.Record input, final String data, final Context context) {
    return Uni.createFrom()
        .item(toDbContracts(input, data))
        .onItem()
        .invoke(items -> log.info("contracts: {}", getJsonUtils().toStringLazy(items)))
        .map(
            items ->
                items.stream()
                    .map(
                        item -> {
                          final Map<String, AttributeValue> map = new HashMap<>();
                          map.put(ZlDbItem.ID, AttributeValue.builder().s(item.getId()).build());
                          map.put(
                              ZlDbItem.SORT, AttributeValue.builder().s(item.getSort()).build());
                          Optional.ofNullable(item.getGsiSort())
                              .ifPresent(
                                  s ->
                                      map.put(
                                          ZlDbItem.GSI_SORT,
                                          AttributeValue.builder().s(s).build()));
                          Optional.ofNullable(item.getData())
                              .ifPresent(
                                  o ->
                                      map.put(
                                          ZlDbItem.DATA,
                                          JsonAttributeValueUtil.toAttributeValue(
                                              getObjectToJsonNode().apply(item.getData()))));
                          return PutItemRequest.builder().tableName(TABLE_NAME).item(map).build();
                        })
                    .collect(Collectors.toList()))
        .onItem()
        .transformToMulti(putItemRequests -> Multi.createFrom().iterable(putItemRequests))
        .flatMap(
            putItemRequest ->
                Uni.createFrom()
                    .completionStage(() -> getDynamoDB().putItem(putItemRequest))
                    .convert()
                    .toPublisher())
        .map(putItemResponse -> putItemResponse.attributes())
        .map(o -> o != null)
        .collect()
        .asList()
        .replaceWith(true);
  }

  private List<ZlDbItem> toDbContracts(final S3EventMessage.Record input, final String data) {
    final List<String> rows = getRows(data);
    if (rows.isEmpty()) {
      return List.of();
    }

    final String headersAsString = rows.get(0);
    final Map<String, Object> template = new HashMap<>();

    final List<String> keys = splitRow(headersAsString);

    if (keys.isEmpty()) {
      return List.of();
    }

    return rows.stream()
        .skip(1)
        .map(s -> splitRow(s))
        .flatMap(
            strings -> {
              if (strings.size() != keys.size()) {
                return buildErrorContracts(input);
              }

              final Map<String, Object> map = new HashMap<>();
              for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), strings.get(i));
              }

              return buildContracts(input, map);
            })
        .collect(Collectors.toList());
  }

  private Stream<ZlDbItem> buildContracts(
      final S3EventMessage.Record input, final Map<String, Object> map) {
    final ZlContract result = getJsonUtils().fromMap(map, ZlContract.class);
    final String key =
        Optional.ofNullable(result.getAgreementNumber())
            .filter(s -> !s.isBlank())
            .orElseGet(
                () -> {
                  result.setStatus(ZlContract.Status.ERROR);
                  result.setErrors(List.of("Missing agreement number"));
                  return input.getS3().getObject().getKey();
                });
    final String id = CONTRACT_PREFIX + key;
    return List.of(
        ZlDbItem.builder().id(id).sort(CONTRACT_DATA).data(result).build(), getVersionItem(id))
        .stream();
  }

  private Stream<ZlDbItem> buildErrorContracts(final S3EventMessage.Record input) {
    final String key = input.getS3().getObject().getKey();
    final String id = CONTRACT_PREFIX + key;
    return List.of(
        ZlDbItem.builder()
            .id(id)
            .sort(CONTRACT_DATA)
            .data(
                ZlContract.builder()
                    .status(ZlContract.Status.ERROR)
                    .errors(List.of("size of the keys and values differs"))
                    .build())
            .build(),
        getVersionItem(id))
        .stream();
  }

  private ZlDbItem getVersionItem(final String id) {
    return ZlDbItem.builder()
        .id(id)
        .sort(CONTRACT_VERSION)
        .gsiSort(CURRENT_CONTRACT_VERSION)
        .build();
  }

  private List<String> getRows(final String data) {
    if (data == null) {
      return List.of();
    }
    return Arrays.stream(data.split("\n"))
        .map(s -> s.trim())
        .filter(s -> !s.isBlank())
        .collect(Collectors.toList());
  }

  private List<String> splitRow(final String data) {
    if (data == null) {
      return List.of();
    }
    return Arrays.stream(data.split(","))
        .map(s -> s.trim())
        .filter(s -> !s.isBlank())
        .collect(Collectors.toList());
  }
}
