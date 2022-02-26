package example.service;

import com.amazonaws.services.lambda.runtime.Context;

import java.util.Map;

/*
 * Created by triphon 26.02.22 г.
 */
public interface S3EventService {

  Boolean isSupported(final Map<String, Object> input, final Context context);

  void handle(final Map<String, Object> input, final Context context);
}
