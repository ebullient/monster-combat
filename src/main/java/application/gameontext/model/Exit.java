/*******************************************************************************
 * Copyright (c) 2018 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package application.gameontext.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Exit {

    @JsonProperty("type")
    private final String type = "exit";

    private String content;
    private String exitId;

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getExitId() {
        return exitId;
    }
    public void setExitId(String exitId) {
        this.exitId = exitId;
    }

    public void verify() {
        assert(
            content != null && !content.isEmpty() &&
            exitId != null && !exitId.isEmpty()
        );
    }
}
