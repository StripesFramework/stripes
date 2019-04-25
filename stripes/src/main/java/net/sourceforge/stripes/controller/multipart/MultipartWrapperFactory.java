/* Copyright 2005-2006 Tim Fennell
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
package net.sourceforge.stripes.controller.multipart;

import net.sourceforge.stripes.controller.ContentTypeRequestWrapperFactory;

/**
 * Factory for classes that implement {@link MultipartWrapper}. The factory may
 * chose to always supply the same kind of wrapper, or vary the implementation
 * request by request as it sees fit.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public interface MultipartWrapperFactory extends ContentTypeRequestWrapperFactory {
}
