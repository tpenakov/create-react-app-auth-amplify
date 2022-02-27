package green.zerolabs.model.db;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

/*
 * Created by triphon 27.02.22 г.
 */
@Builder
@Data
@Jacksonized
public class ZlDbItem implements Serializable {
  private static final long serialVersionUID = -5436379863867419767L;
  // DynamoDb partition key
  private String id;
  // DynamoDb partition sort key and GSI key
  private String sort;
  // DynamoDb GSI sort key
  private String gsiSort;
  // The non indexed data. E.g - ZlContract
  private Object data;
}
