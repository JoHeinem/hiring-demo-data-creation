/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.optimize.hiring.data.generation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.optimize.hiring.data.generation.dto.TaskDto;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class UserTaskCompleter {

  private ObjectMapper objectMapper = new ObjectMapper();

  public void completeAllUserTasks() {
    completeUserTasks(null);
  }

  public void completeUserTasks(String taskDefinitionKey) {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpGet get = new HttpGet("http://localhost:8080/engine-rest/task?processDefinitionKey=hiring-demo");
    if (taskDefinitionKey != null) {
      URI uri = null;
      try {
        uri = new URIBuilder(get.getURI())
          .addParameter("taskDefinitionKey", taskDefinitionKey)
          .build();
      } catch (URISyntaxException e) {
        System.out.println("Could not build uri!");
      }
      get.setURI(uri);
    }

    try {
      List<TaskDto> tasks;
      do {
        CloseableHttpResponse response = client.execute(get);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

        tasks = objectMapper.readValue(responseString, new TypeReference<List<TaskDto>>() {
        });
        response.close();
        for (TaskDto task : tasks) {
          claimAndCompleteUserTask(client, task);
        }
      } while (!tasks.isEmpty());
    } catch (IOException e) {
      System.out.println("Error while trying to finish the user task!!");
    } finally {
      try {
        client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void claimAndCompleteUserTask(CloseableHttpClient client, TaskDto task) throws IOException {
    HttpPost claimPost = new HttpPost(getClaimTaskUri(task.getId()));
    claimPost.setEntity(new StringEntity("{ \"userId\" : " + "\"demo\"" + "}"));
    claimPost.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    CloseableHttpResponse response = client.execute(claimPost);
    if (response.getStatusLine().getStatusCode() != 204) {
      throw new RuntimeException("Could not claim user task!");
    }

    HttpPost completePost = new HttpPost(getCompleteTaskUri(task.getId()));
    completePost.setEntity(new StringEntity("{}"));
    completePost.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    response.close();
    response = client.execute(completePost);
    if (response.getStatusLine().getStatusCode() != 204) {
      System.out.println("Warning: Could not complete user task " + task.getId());
    }
    response.close();
  }

  private String getClaimTaskUri(String taskId) {
    return getEngineUrl() + "/task/" + taskId + "/claim";
  }

  private String getCompleteTaskUri(String taskId) {
    return getEngineUrl() + "/task/" + taskId + "/complete";
  }


  private String getEngineUrl() {
    return "http://localhost:8080/engine-rest";
  }
}
