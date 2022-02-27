package green.zerolabs.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Some of the code is copied from here:
 *
 * <p>https://github.com/tpenakov/otaibe-commons-quarkus/blob/master/otaibe-commons-quarkus-core/src/main/java/org/otaibe/commons/quarkus/core/utils/JsonUtils.java
 *
 * <p>Created by triphon 27.02.22 г.
 */
@ApplicationScoped
@Getter
@Slf4j
public class JsonUtils {

  public static final String EMPTY = "";
  private final Function<Map<String, Object>, String> MAP_TO_STRING_FN =
      Unchecked.<Map<String, Object>, String>function(
          input -> getObjectMapper().writeValueAsString(input));

  private final Function<String, Map<String, Object>> STRING_TO_MAP_FN =
      Unchecked.<String, Map<String, Object>>function(
          input -> getObjectMapper().readValue(input, Map.class));

  private final ObjectMapper objectMapper;

  public JsonUtils(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
  }

  public String toString(final Map<String, Object> input) {
    return MAP_TO_STRING_FN.apply(input);
  }

  public Map<String, Object> toMap(final String input) {
    return STRING_TO_MAP_FN.apply(input);
  }

  public <T> T fromMap(final Map input, final Class<T> outputClass) {
    return fromMap(input, getObjectMapper(), outputClass);
  }

  public <T> T fromMap(
      final Map input, final ObjectMapper objectMapper, final Class<T> outputClass) {
    try {
      final T value = objectMapper.readValue(objectMapper.writeValueAsBytes(input), outputClass);
      return value;
    } catch (final Exception e) {
      log.error("unable to transform to outputClass", e);
      throw new RuntimeException(e);
    }
  }

  public Map toMap(final Object input) {
    return toMap(input, getObjectMapper());
  }

  public Map toMap(final Object input, final ObjectMapper objectMapper) {
    return toMap(input, objectMapper, new HashMap<>());
  }

  public Map<String, Object> toMap(
      final Object input, final Map<String, Function<Object, Object>> valueChangeMap) {
    return toMap(input, getObjectMapper(), valueChangeMap);
  }

  public Map<String, Object> toMap(
      final Object input,
      final ObjectMapper objectMapper,
      final Map<String, Function<Object, Object>> valueChangeMap) {
    try {
      final Map<String, Object> map =
          objectMapper.readValue(objectMapper.writeValueAsBytes(input), Map.class);
      return map.keySet().stream()
          .filter(s -> s != null)
          .filter(s -> null != map.get(s))
          // fix for unicode null char - it is shown as "\u0000" string
          .filter(
              s ->
                  false == map.get(s).toString().isEmpty()
                      && 0x0 != map.get(s).toString().charAt(0))
          .collect(
              Collectors.toMap(
                  o -> o,
                  o -> {
                    final Object val = map.get(o);
                    if (valueChangeMap.containsKey(o)) {
                      return valueChangeMap.get(o).apply(val);
                    }
                    return val;
                  }));
    } catch (final Exception e) {
      log.error("unable to transform to Map", e);
      throw new RuntimeException(e);
    }
  }

  public <T> T deepClone(final Object input, final Class<T> resultType) {
    return deepClone(input, getObjectMapper(), resultType);
  }

  public <T> T deepClone(
      final Object input, final ObjectMapper objectMapper, final Class<T> resultType) {
    if (input == null) {
      return null;
    }
    try {
      return objectMapper.readValue(objectMapper.writeValueAsBytes(input), resultType);
    } catch (final Exception e) {
      log.error("unable to serialize", e);
      throw new RuntimeException(e);
    }
  }

  public Object toStringLazy(final Object input) {
    return toStringLazy(input, getObjectMapper());
  }

  public Object toStringLazy(final Object input, final ObjectMapper objectMapper) {
    return new ToStringLazy(input, objectMapper);
  }

  public <T> Optional<T> readValue(final String value, final Class<T> clazz) {
    return readValue(value, clazz, getObjectMapper());
  }

  public <T> Optional<T> readValue(final byte[] value, final Class<T> clazz) {
    return readValue(value, clazz, getObjectMapper());
  }

  public <T> Optional<T> readValue(final InputStream value, final Class<T> clazz) {
    return readValue(value, clazz, getObjectMapper());
  }

  public <T> Optional<T> readValue(
      final String value, final Class<T> clazz, final ObjectMapper objectMapper1) {
    return Optional.ofNullable(objectMapper1)
        .map(
            objectMapper -> {
              try {
                return objectMapper.readValue(value, clazz);
              } catch (final Exception e) {
                log.error("unable to deserialize", e);
              }
              return null;
            });
  }

  public <T> Optional<T> readValue(
      final byte[] value, final Class<T> clazz, final ObjectMapper objectMapper1) {
    return Optional.ofNullable(objectMapper1)
        .map(
            objectMapper -> {
              try {
                return objectMapper.readValue(value, clazz);
              } catch (final Exception e) {
                log.error("unable to deserialize", e);
              }
              return null;
            });
  }

  public <T> Optional<T> readValue(
      final InputStream value, final Class<T> clazz, final ObjectMapper objectMapper1) {
    return Optional.ofNullable(objectMapper1)
        .map(
            objectMapper -> {
              try {
                return objectMapper.readValue(value, clazz);
              } catch (final Exception e) {
                log.error("unable to deserialize", e);
              }
              return null;
            });
  }

  @AllArgsConstructor
  private static class ToStringLazy {
    private Object input;
    private ObjectMapper objectMapper;

    @Override
    public String toString() {
      if (objectMapper == null || input == null) {
        return EMPTY;
      }
      try {
        final String value = objectMapper.writeValueAsString(input);
        return value;
      } catch (final Exception e) {
        log.error("unable to serialize to json", e);
        return EMPTY;
      }
    }
  }
}
