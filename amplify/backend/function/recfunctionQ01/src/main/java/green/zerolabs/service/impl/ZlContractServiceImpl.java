package green.zerolabs.service.impl;

import com.amazonaws.services.lambda.runtime.Context;
import green.zerolabs.model.S3EventMessage;
import green.zerolabs.service.ZlContractService;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

/*
 * Created by triphon 27.02.22 г.
 */
@ApplicationScoped
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class ZlContractServiceImpl implements ZlContractService {
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
    return null;
  }
}
