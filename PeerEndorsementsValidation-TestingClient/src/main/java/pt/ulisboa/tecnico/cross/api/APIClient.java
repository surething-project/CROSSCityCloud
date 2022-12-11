package pt.ulisboa.tecnico.cross.api;

import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class APIClient {

  public static RestTemplate getRestTemplate() {
    return new RestTemplate(List.of(new ProtobufHttpMessageConverter()));
  }
}
