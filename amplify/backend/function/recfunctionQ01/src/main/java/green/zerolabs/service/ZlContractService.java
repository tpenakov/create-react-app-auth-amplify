package green.zerolabs.service;

import com.amazonaws.services.lambda.runtime.Context;
import green.zerolabs.model.S3EventMessage;
import io.smallrye.mutiny.Uni;

/*
 * Created by triphon 27.02.22 г.
 */
public interface ZlContractService {
  Boolean isSupported(S3EventMessage.Record input, String data, final Context context);

  Uni<Boolean> handle(S3EventMessage.Record input, String data, final Context context);
}
