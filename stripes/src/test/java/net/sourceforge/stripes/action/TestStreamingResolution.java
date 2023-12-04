/* Copyright 2014 Ben Gunter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.action;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import org.apache.commons.fileupload2.core.ParameterParser;
import org.junit.Assert;
import org.junit.Test;

public class TestStreamingResolution {
  @Test
  public void testContentDisposition() throws Exception {
    doTestContentDisposition(true, UUID.randomUUID().toString());
    doTestContentDisposition(false, UUID.randomUUID().toString());
    doTestContentDisposition(true, null);
    doTestContentDisposition(false, null);
  }

  private void doTestContentDisposition(boolean attachment, String filename) throws Exception {
    byte[] data = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
    ByteArrayInputStream is = new ByteArrayInputStream(data);

    StreamingResolution resolution = new StreamingResolution("application/octet-stream", is);
    resolution.setAttachment(attachment);
    if (filename != null) {
      resolution.setFilename(filename);
    }

    MockHttpServletResponse response = new MockHttpServletResponse();
    resolution.applyHeaders(response);
    resolution.stream(response);
    Assert.assertArrayEquals(data, response.getOutputBytes());

    // Use commons-fileupload to parse the Content-Disposition header
    String disposition = null;
    String cdAttachment = null;
    String cdFilename = null;
    final List<Object> list = response.getHeaderMap().get("Content-Disposition");
    if (list != null && !list.isEmpty()) {
      disposition = list.get(0).toString();
      final ParameterParser parser = new ParameterParser();
      parser.setLowerCaseNames(true);
      final Map<String, String> params = parser.parse(disposition, ';');
      cdAttachment = params.containsKey("attachment") ? "attachment" : null;
      cdFilename = params.getOrDefault("filename", null);
    }
    if (attachment) {
      Assert.assertNotNull(disposition);
      Assert.assertEquals("attachment", cdAttachment);
      if (filename == null) {
        Assert.assertNull(cdFilename);
      } else {
        Assert.assertNotNull(cdFilename);
      }
    } else {
      if (filename == null) {
        Assert.assertNull(disposition);
      } else {
        Assert.assertNotNull(disposition);
        Assert.assertEquals("attachment", cdAttachment);
        Assert.assertNotNull(cdFilename);
      }
    }
  }
}
