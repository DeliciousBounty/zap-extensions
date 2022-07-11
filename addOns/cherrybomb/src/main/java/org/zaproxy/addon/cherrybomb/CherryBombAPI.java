/*
 * Zed Attack Proxy (ZAP) and its related class files.

 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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

package org.zaproxy.addon.cherrybomb;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableEntry;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel;

public class CherryBombAPI extends ApiImplementor {
  private static final String PREFIX = "Hey welcome ";

  private static final String ACTION_HELLO_WORLD = "helloWorld";

  private static final Logger LOGGER = LogManager.getLogger(CherryBombAPI.class);

  public CherryBombAPI() {
    this.addApiAction(new ApiAction(ACTION_HELLO_WORLD));
  }

  @Override
  public String getPrefix() {
    return PREFIX;
  }

  @Override
  public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
    switch (name) {
    case ACTION_HELLO_WORLD:
      LOGGER.debug("hello world called");
      break;

    default:
      throw new ApiException(ApiException.Type.BAD_ACTION);
    }

    return ApiResponseElement.OK;
  }
  public JSONObject CreateJsonFromLogs(String website) {
    JSONObject[] session = new JSONObject[0];
    JSONObject json = new JSONObject();
    ExtensionHistory history1 = new ExtensionHistory();
    int lastRef = history1.getLastHistoryId();
    JSONArray array = new JSONArray();
    for (int x = 1; x <= lastRef; x++) {
      try {
        JSONObject item = new JSONObject();
        HistoryReference history = new HistoryReference(x);
        HttpMessage http_mess = new HttpMessage(history.getHttpMessage());
        DefaultHistoryReferencesTableEntry table = new DefaultHistoryReferencesTableEntry(history, HistoryReferencesTableModel.Column.values());
        if (table.getHostName().equals(website)) {
          ExtensionCherryBomb.website_exist = true;
          if (!table.getUri().contains(".jsp") && !table.getUri().contains(".css") && !table.getUri().contains(".js") && !table.getUri().contains(".html") && !table.getUri().contains(".ico") && !table.getUri().contains(".png")) {
            if (!http_mess.getResponseHeader().toString().contains("Content-Type: text/html")) {
              item.put("request", http_mess.getRequestHeader().toString());
              item.put("response", http_mess.getResponseHeader().toString());
              array.add(item);
            }

          }
        }
      } catch (HttpMalformedHeaderException | DatabaseException | JSONException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

      try {
        json.put("session", array);
        ExtensionCherryBomb.message = json.toString();
      } catch (JSONException e1) {
        // TODO Auto-generated catch block
        //	e1.printStackTrace();
      }

    }
    return json;

  }

  public String SendtoTheServer(String hostname, JSONObject CompressLogs, String token) {
    System.out.println("Sending ..." + CompressLogs.toString());
    JSONObject json = new JSONObject();
    json.put("data", "[" + CompressLogs.toString() + "] ");
    json.put("file", "files.txt");
    json.put("domain", "https://" + hostname);
    json.put("access_token", token);
    String url = "https://saas.blstsecurity.com/zap/upload_map";
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .POST(BodyPublishers.ofString(json.toString()))
      .uri(URI.create(url))
      .setHeader("Authorization", token)
      .header("Content-Type", "application/json")
      .build();

    HttpResponse < String > response;
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println("response:" + response);
      //System.out.println("finished! :");
      return response.toString();

    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return "null";

  }

}