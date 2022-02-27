package green.zerolabs.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/*
 * Created by triphon 27.02.22 г.
 */
@Builder
@Data
@Jacksonized
public class ZlContract implements Serializable {
  private static final long serialVersionUID = -8290491811055702078L;

  private String agreementNumber;
  private String agreementDate;
  private String seller;
  private String buyer;
  private String productType;
  private String energySources;
  private String region;
  private String countries;
  private String reportingStartTime;
  private String reportingEndTime;
  private BigDecimal quantity;
  private String units;
  private BigDecimal price;
  private String currency;
  // internal fields

  public enum Status {
    DRAFT,
    OK,
    ERROR
  }

  @Builder.Default private Status status = Status.DRAFT;
  @Builder.Default private Instant createdDate = Instant.now();
  @Builder.Default private Instant updatedData = Instant.now();
  private List<String> errors;
}
