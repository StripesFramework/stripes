/* Copyright 2008 Aaron Porter
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Resolution for sending HTTP error messages back to the client. errorCode is the HTTP status code
 * to be sent. errorMessage is a descriptive message.
 * </p>
 */
public class ErrorResolution implements Resolution {
    private int errorCode;
    private String errorMessage;

    /**
     * Sends an error response to the client using the specified status code and clearing the buffer.
     * 
     * @param errorCode the HTTP status code
     */
    public ErrorResolution(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @param errorCode the HTTP status code
     * @param errorMessage a descriptive message
     */
    public ErrorResolution(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (errorMessage != null)
            response.sendError(errorCode, errorMessage);
        else
            response.sendError(errorCode);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}