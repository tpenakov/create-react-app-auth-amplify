package example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.List;

/*
 * Created by triphon 26.02.22 г.
 */
@Builder
@Data
@Jacksonized
public class S3EventMessage implements Serializable {

  public static final String RECORDS = "Records";

  @JsonProperty(RECORDS)
  private List<Record> records;

  @Builder
  @Data
  @Jacksonized
  public static class Record implements Serializable {

    private String eventVersion;
    private String eventSource;
    private String awsRegion;
    private String eventTime;
    private String eventName;
    private S3 s3;

    @Builder
    @Data
    @Jacksonized
    public static class S3 implements Serializable {
      private String s3SchemaVersion;
      private String configurationId;
      private Bucket bucket;
      private Object object;

      @Builder
      @Data
      @Jacksonized
      public static class Bucket implements Serializable {
        private String name;
        private String arn;
      }

      @Builder
      @Data
      @Jacksonized
      public static class Object implements Serializable {
        private String key;
        private Integer size;
        private String eTag;
      }
    }
  }
}
