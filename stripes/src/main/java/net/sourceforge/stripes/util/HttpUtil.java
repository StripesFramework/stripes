/* Copyright 2008 Ben Gunter
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
package net.sourceforge.stripes.util;

import jakarta.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.controller.StripesConstants;

/**
 * Provides helper methods for working with HTTP requests and responses.
 *
 * @author Ben Gunter
 * @since Stripes 1.5.1
 */
public class HttpUtil {
  /**
   * Get the path from the given request. This method is different from {@link
   * HttpServletRequest#getRequestURI()} in that it concatenates and returns the servlet path plus
   * the path info from the request. These are usually the same, but in some cases they are not.
   *
   * <p>One case where they are known to differ is when a request for a directory is forwarded by
   * the servlet container to a welcome file. In that case, {@link
   * HttpServletRequest#getRequestURI()} returns the path that was actually requested (e.g., {@code
   * "/"}), whereas the servlet path plus path info is the path to the welcome file (e.g. {@code
   * "/index.jsp"}).
   */
  public static String getRequestedPath(HttpServletRequest request) {
    String servletPath, pathInfo;

    // Check to see if the request is processing an include, and pull the path
    // information from the appropriate source.
    // only request attributes need decoding, not servletPath and pathInfo
    // see http://www.stripesframework.org/jira/browse/STS-899

    servletPath =
        urlDecodeNullSafe((String) request.getAttribute(StripesConstants.REQ_ATTR_INCLUDE_PATH));
    if (servletPath != null) {
      pathInfo =
          urlDecodeNullSafe(
              (String) request.getAttribute(StripesConstants.REQ_ATTR_INCLUDE_PATH_INFO));
    } else {
      servletPath = request.getServletPath();
      pathInfo = request.getPathInfo();
    }

    if (servletPath == null) return pathInfo == null ? "" : pathInfo;
    else if (pathInfo == null) return servletPath;
    else return servletPath + pathInfo;
  }

  /**
   * Get the servlet path of the current request. The value returned by this method may differ from
   * {@link HttpServletRequest#getServletPath()}. If the given request is an include, then the
   * servlet path of the included resource is returned.
   */
  public static String getRequestedServletPath(HttpServletRequest request) {
    // Check to see if the request is processing an include, and pull the path
    // information from the appropriate source.
    String path = (String) request.getAttribute(StripesConstants.REQ_ATTR_INCLUDE_PATH);
    if (path == null) {
      path = request.getServletPath();
    }
    return path == null ? "" : path;
  }

  /** No instances */
  private HttpUtil() {}

  private static String urlDecodeNullSafe(String url) {
    if (url == null) {
      return null;
    }
    return StringUtil.urlDecode(url);
  }
}
