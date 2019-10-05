/*
 * Copyright © 2019 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package application.witness;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component("WitnessWebSocketHandler")
public class WitnessWebSocketHandler implements WebSocketHandler {

    ObjectMapper om = new ObjectMapper();

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {

        // Flux<String> employeeCreationEvent = Flux.generate(sink -> {
        //     EmployeeCreationEvent event = new EmployeeCreationEvent(randomUUID().toString(), now().toString());
        //     try {
        //         sink.next(om.writeValueAsString(event));
        //     } catch (JsonProcessingException e) {
        //         sink.error(e);
        //     }
        // });

        // return webSocketSession.send(employeeCreationEvent
        //     .map(webSocketSession::textMessage)
        //     .delayElements(Duration.ofSeconds(1)));\
        return webSocketSession.send(Flux.just(webSocketSession.textMessage("HI")));
    }
}
