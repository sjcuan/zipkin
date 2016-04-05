/**
 * Copyright 2015-2016 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin.server.brave;

import java.util.List;
import org.junit.Test;
import zipkin.BinaryAnnotation;
import zipkin.Constants;
import zipkin.Endpoint;
import zipkin.InMemoryStorage;
import zipkin.Span;

import static org.assertj.core.api.Assertions.assertThat;

public class SpanStoreSpanCollectorTest {

  InMemoryStorage mem = new InMemoryStorage();
  SpanStoreSpanCollector collector = new SpanStoreSpanCollector(mem);

  Span.Builder builder = new Span.Builder()
      .traceId(1234L)
      .id(1235L)
      .parentId(1234L)
      .name("md5")
      .timestamp(System.currentTimeMillis() * 1000)
      .duration(150L)
      .addBinaryAnnotation(BinaryAnnotation.create(Constants.LOCAL_COMPONENT, "digest",
          Endpoint.create("service", 127 << 24 | 1, 8080)));

  @Test
  public void addOne() {
    collector.collect(builder.build());
    collector.flush();
    assertThat(mem.spanStore().getServiceNames()).containsExactly("service");
  }

  @Test
  public void addMany() {
    for (int i = 0; i < 500; i++) {
      collector.collect(builder.id(1234L + i).build());
    }
    collector.flush();
    List<Span> result = mem.spanStore().getTrace(1234L);
    assertThat(result).hasSize(500);
  }
}
