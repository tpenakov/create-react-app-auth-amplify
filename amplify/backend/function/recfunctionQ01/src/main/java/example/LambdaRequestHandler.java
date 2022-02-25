package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
public class LambdaRequestHandler implements RequestHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        log.info("input: {}", input);
        context.getLogger().log("input: " + input);
        return "Hello " + input;
    }
}
