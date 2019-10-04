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
package org.gameontext.sample.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Event {

    @JsonProperty("type")
    private final String type = "event";

    private String bookmark = "";

    @JsonProperty("content")
    private final Map<String, String> content = new HashMap<>();

    public void addContent(String target, String text) {
        if (target == null || text == null ) {
            throw new IllegalArgumentException("target and content are required");
        }
        content.put(target, text);
    }

    public void removeContent(String target) {
        content.remove(target);
    }

    public String getBookmark() {
        return bookmark;
    }
    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

	public void verify() {
        assert(!content.isEmpty());
	}
}