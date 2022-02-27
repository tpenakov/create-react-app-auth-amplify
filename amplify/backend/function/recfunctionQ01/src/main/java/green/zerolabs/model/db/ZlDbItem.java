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

  public static final String ID = "id";
  public static final String SORT = "sort";
  public static final String GSI_SORT = "gsiSort";
  public static final String DATA = "data";

  // DynamoDb partition key
  private String id;
  // DynamoDb partition sort key and GSI key
  private String sort;
  // DynamoDb GSI sort key
  private String gsiSort;
  // The non indexed data. E.g - ZlContract
  private Object data;
}
