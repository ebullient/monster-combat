/*******************************************************************************
 * Copyright (c) 2017 IBM Corp.
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

var websocket = null;
var websocketUrl = "ws://" + window.document.location.host + "/room";
var healthUrl = "http://" + window.document.location.host + "/actuator/health";
var livenessUrl = "http://" + window.document.location.host + "/actuator/liveness";
var metricsUrl = "http://" + window.document.location.host + "/actuator/metrics";
var promUrl = "http://" + window.document.location.host + "/actuator/prometheus";

document.getElementById("socketUrl").innerHTML = websocketUrl;
document.getElementById("healthUrl").innerHTML = healthUrl;
document.getElementById("livenessUrl").innerHTML = livenessUrl;
document.getElementById("metricsUrl").innerHTML = metricsUrl;
document.getElementById("promUrl").innerHTML = promUrl;

var inputMessage = document.getElementById("inputmessage");
var connectButton = document.getElementById("connectButton");
var disconnectButton = document.getElementById("disconnectButton");
var response = document.getElementById("response");

function connect() {
  console.log("connect %o", websocket);

  if ( websocket === null ) {
    response.innerHTML = "";
    connectButton.disabled = true;

    websocket = new WebSocket(websocketUrl);
    websocket.onerror = function(event) {
      response.innerHTML += "Error: " + event.data + "<br />";
    };

    websocket.onopen = function(event) {
      response.innerHTML += "Connection established<br />";

      disconnectButton.disabled = false;
      inputMessage.disabled = false;
    };

    websocket.onclose = function(event) {
      websocket = null;
      response.innerHTML += "Connection closed: " + event.code + "<br />";
      connectButton.disabled = false;
      disconnectButton.disabled = true;
      inputMessage.disabled = true;
    };

    websocket.onmessage = function(event) {
      response.innerHTML += "&larr; " + event.data + "<br />";
      var isScrolledToBottom = response.scrollHeight - response.clientHeight <= response.scrollTop + 1;
      console.log("isScrolledToBottom: %o", isScrolledToBottom);
      if(!isScrolledToBottom) {
        response.scrollTop = response.scrollHeight - response.clientHeight;
      }
    };
  }
}

function sendSocket(payload) {
  console.log("sendSocket %o, %o", websocket, payload);
  if ( websocket !== null ) {
    response.innerHTML += "&rarr; " + payload + "<br />";
    websocket.send(payload);
  }
}

function disconnect() {
  console.log("disconnect %o", websocket);

  if ( websocket !== null ) {
    websocket.close();

    disconnectButton.disabled = true;
    response.disabled = true;
  }
}

function submit(event) {
    event.preventDefault();
    var txt = inputMessage.value;
    inputMessage.value="";

    if ( txt.indexOf("clear") >= 0) {
      response.innerHTML="";
    } else {
      message.content = txt;
      sendSocket("room," + roomId.value + "," + JSON.stringify(message));
    }
}
document.getElementById("simpleForm").addEventListener("submit", submit, false);
