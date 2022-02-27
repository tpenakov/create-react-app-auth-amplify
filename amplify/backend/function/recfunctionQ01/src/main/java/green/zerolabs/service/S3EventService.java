package green.zerolabs.service;

import com.amazonaws.services.lambda.runtime.Context;
import io.smallrye.mutiny.Uni;

import java.util.Map;

/*
 * Created by triphon 26.02.22 г.
 */
public interface S3EventService {

  Boolean isSupported(final Map<String, Object> input, final Context context);

  Uni<Boolean> handle(final Map<String, Object> input, final Context context);
}
