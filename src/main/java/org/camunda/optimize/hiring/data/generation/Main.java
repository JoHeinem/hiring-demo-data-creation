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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.camunda.optimize.hiring.data.generation.dto.MessageCorrelationDto;
import org.camunda.optimize.hiring.data.generation.dto.VariableValue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.camunda.optimize.hiring.data.generation.VariableHelper.createBooleanVariable;
import static org.camunda.optimize.hiring.data.generation.VariableHelper.createIntegerVariable;
import static org.camunda.optimize.hiring.data.generation.VariableHelper.createLongVariable;
import static org.camunda.optimize.hiring.data.generation.VariableHelper.createStringVariable;

public class Main {

  // Task ids
  private static final String ASSIGN_HIRING_MANAGER = "HiringManager";
  private static final String SCREEN_APPLICATION = "ScreenApplication";
  private static final String CONDUCT_PHONE_INTERVIEW = "ConductPhoneInterview";
  private static final String CONDUCT_FIRST_ONSITE_INTERVIEW = "Conduct1OnsiteInterview";
  private static final String CONDUCT_SECOND_ONSITE_INTERVIEW = "Conduct2OnsiteInterview";
  private static final String MAKE_AN_OFFER = "MakeOffer";
  private static final String CANDIDATE_REPLIED = "CandidateReplied";

  private static Random random = new Random();
  private static ObjectMapper objectMapper = new ObjectMapper();

  // variable names
  private static String TASK_AUTOMATICALLY_ASSIGNED = "Task_automatically_assigned";
  private static String TASK_SCREEN_PROCEED = "Task_screen_proceed";
  private static String TASK_PHONE_PROCEED = "Task_phone_proceed";
  private static String TASK_ONSITE_INTERVIEW = "Task_onsite_interview";
  private static String TASK_MAKE_OFFER = "Task_make_offer";
  private static String TASK_OFFER_ACCEPTED = "Task_offer_accepted";

  // durations
  private static long seconds = 1000;
  private static long minutes = 60 * seconds;
  private static long hours = 60 * minutes;
  private static long days = 24 * hours;
  private static long weeks = 7 * days;
//  private static long months = 30 * days;
//  private static long years = 12 * months;


  private static String[] allVariableNames = {TASK_AUTOMATICALLY_ASSIGNED, TASK_SCREEN_PROCEED, TASK_PHONE_PROCEED,
    TASK_ONSITE_INTERVIEW, TASK_MAKE_OFFER, TASK_OFFER_ACCEPTED};


  private static Map<String, VariableValue> createHappyPath() {
    Map<String, VariableValue> variables = new HashMap<>();
    for (String variableName : allVariableNames) {
      variables.put(variableName, createBooleanVariable(true));
    }
    randomizeAutomaticTaskAssignment(variables);
    randomizeSecondOnsiteInterview(variables);
    addHiringInformationVariables(variables);
    candidateNotCancelled(variables);
    addTaskDurations(variables);
    return variables;
  }

  private static void candidateNotCancelled(Map<String, VariableValue> variables) {
    variables.put("CandidateCancelled", createBooleanVariable(false));
  }

  private static void candidateCancelled(Map<String, VariableValue> variables) {
    variables.put("CandidateCancelled", createBooleanVariable(true));
  }

  private static void addHiringInformationVariables(Map<String, VariableValue> variables) {
    String[] positions = {"Software Developer", "Teamlead", "Account Manager", "DevOps Engineer",
      "Sales Representative", "Software Architect", "Recruiting Manager", "Marketing Manager"};
    String[] departments = {"IT", "IT", "Sales", "IT", "Sales", "IT", "HR", "Marketing"};
    int index = random.nextInt(7);
    String position = positions[index];
    String department = departments[index];
    variables.put("Position", createStringVariable(position));
    variables.put("Department", createStringVariable(department));
    String[] jobExperienceLevels = {"Associate", "Mid-Senior", "Beginner", "Senior"};
    index = random.nextInt(4);
    String jobExperienceLevel = jobExperienceLevels[index];
    variables.put("JobExperienceLevel", createStringVariable(jobExperienceLevel));
    int salaryExpectation = ThreadLocalRandom.current().nextInt(20_000, 100_000);
    variables.put("SalaryExpectation", createIntegerVariable(salaryExpectation));
    String domesticType  = random.nextDouble() < 0.8? "national" : "international";
    variables.put("DomesticType", createStringVariable(domesticType));
  }

  private static Map<String, VariableValue> assignHiringManagerPath() {
    Map<String, VariableValue> variables = createHappyPath();
    variables.put(TASK_AUTOMATICALLY_ASSIGNED, createBooleanVariable(false));
    return variables;
  }

  private static Map<String, VariableValue> automaticHiringManagerAssignmentPath() {
    Map<String, VariableValue> variables = createHappyPath();
    variables.put(TASK_AUTOMATICALLY_ASSIGNED, createBooleanVariable(true));
    return variables;
  }

  private static Map<String, VariableValue> rejectCandidateAfterScreenApplication() {
    Map<String, VariableValue> variables = createHappyPath();
    randomizeAutomaticTaskAssignment(variables);
    variables.put(TASK_SCREEN_PROCEED, createBooleanVariable(false));
    return variables;
  }

  private static Map<String, VariableValue> rejectCandidateAfterPhoneInterview() {
    Map<String, VariableValue> variables = createHappyPath();
    randomizeAutomaticTaskAssignment(variables);
    variables.put(TASK_SCREEN_PROCEED, createBooleanVariable(true));
    variables.put(TASK_PHONE_PROCEED, createBooleanVariable(false));
    return variables;
  }

  private static Map<String, VariableValue> rejectCandidateAfterOnsiteInterview() {
    Map<String, VariableValue> variables = createHappyPath();
    randomizeAutomaticTaskAssignment(variables);
    variables.put(TASK_SCREEN_PROCEED, createBooleanVariable(true));
    variables.put(TASK_PHONE_PROCEED, createBooleanVariable(true));
    randomizeSecondOnsiteInterview(variables);
    variables.put(TASK_MAKE_OFFER, createBooleanVariable(false));
    return variables;
  }

  private static Map<String, VariableValue> looseCandidate() {
    Map<String, VariableValue> variables = createHappyPath();
    variables.put(TASK_OFFER_ACCEPTED, createBooleanVariable(false));
    return variables;
  }

  // ----- duration

  private static void addTaskDurations(Map<String, VariableValue> variables) {
    addAssignHiringManagerDuration(variables);
    addScreenApplicationDuration(variables);
    addConductPhoneInterviewDuration(variables);
    addConductFirstOnsiteInterviewDuration(variables);
    addConductSecondOnsiteInterviewDuration(variables);
    addMakeAnOfferDuration(variables);
    addCandidateRepliedDuration(variables);
  }

  private static void randomizeAutomaticTaskAssignment(Map<String, VariableValue> variables) {
    int coin = random.nextInt(2);
    boolean taskAutomaticallyAssigned = coin != 0;
    variables.put(TASK_AUTOMATICALLY_ASSIGNED, createBooleanVariable(taskAutomaticallyAssigned));
  }

  private static void randomizeSecondOnsiteInterview(Map<String, VariableValue> variables) {
    int coin = random.nextInt(2);
    boolean taskSecondOnsiteInterview = coin != 0;
    variables.put(TASK_ONSITE_INTERVIEW, createBooleanVariable(taskSecondOnsiteInterview));
  }

  private static void addAssignHiringManagerDuration(Map<String, VariableValue> variables) {
    long duration = calculateDuration(4 * hours, 2 * hours);
    variables.put(ASSIGN_HIRING_MANAGER, createLongVariable(duration));
  }

  private static void addScreenApplicationDuration(Map<String, VariableValue> variables) {
    long duration = calculateDuration(4 * days, 1 * days);
    variables.put(SCREEN_APPLICATION, createLongVariable(duration));
  }

  private static void addConductPhoneInterviewDuration(Map<String, VariableValue> variables) {
    long duration = calculateDuration(1 * weeks, 1 * days);
    variables.put(CONDUCT_PHONE_INTERVIEW, createLongVariable(duration));
  }

  private static void addConductFirstOnsiteInterviewDuration(Map<String, VariableValue> variables) {
    long duration = calculateDuration(4 * days, 2 * days);
    variables.put(CONDUCT_FIRST_ONSITE_INTERVIEW, createLongVariable(duration));
  }

  private static void addConductSecondOnsiteInterviewDuration(Map<String, VariableValue> variables) {
    long duration = calculateDuration(1 * weeks, 1 * days);
    variables.put(CONDUCT_SECOND_ONSITE_INTERVIEW, createLongVariable(duration));
  }

  private static void addMakeAnOfferDuration(Map<String, VariableValue> variables) {
    long duration = calculateDuration(1 * days + 2 * hours, 2 * hours);
    variables.put(MAKE_AN_OFFER, createLongVariable(duration));
  }

  private static void addCandidateRepliedDuration(Map<String, VariableValue> variables) {
    long duration = calculateDuration(5 * days, 1 * days);
    variables.put(CANDIDATE_REPLIED, createLongVariable(duration));
  }

  private static long calculateDuration(long mean, long deviation) {
    return Math.abs(Math.round(random.nextGaussian() * deviation) + mean);
  }

  // ------

  // ---- cancellation

  private static void sendCandidateCancelEvent() throws IOException {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost("http://localhost:8080/engine-rest/message/");
    post.setHeader("Content-type", "application/json");
    MessageCorrelationDto message = new MessageCorrelationDto();
    message.setAll(true);
    message.setMessageName("candidate_cancelled");
    StringEntity content = new StringEntity(objectMapper.writeValueAsString(message), Charset.defaultCharset());
    post.setEntity(content);
    HttpResponse response = client.execute(post);
    if (response.getStatusLine().getStatusCode() != 204) {
      System.out.println("Warning: Code for send candidate cancel should be 204!");
    }
    client.close();
  }

  private static void startProcessInstance(Map<String, VariableValue> variables) throws IOException {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost("http://localhost:8080/engine-rest/message/");
    post.setHeader("Content-type", "application/json");
    MessageCorrelationDto message = new MessageCorrelationDto();
    message.setAll(true);
    message.setMessageName("candidate_application");
    message.setProcessVariables(variables);
    StringEntity content = new StringEntity(objectMapper.writeValueAsString(message), Charset.defaultCharset());
    post.setEntity(content);
    HttpResponse response = client.execute(post);
    if (response.getStatusLine().getStatusCode() != 204) {
      System.out.println("Warning: Code for starting process instance should be 204!");
    }
    client.close();
  }

  private static void sendCandidateCandidateReplied() throws IOException {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost("http://localhost:8080/engine-rest/message/");
    post.setHeader("Content-type", "application/json");
    MessageCorrelationDto message = new MessageCorrelationDto();
    message.setAll(true);
    message.setMessageName("candidate_replied");
    StringEntity content = new StringEntity(objectMapper.writeValueAsString(message), Charset.defaultCharset());
    post.setEntity(content);
    HttpResponse response = client.execute(post);
    if (response.getStatusLine().getStatusCode() != 204) {
      System.out.println("Warning: Code for send candidate replied should be 204!");
    }
    client.close();
  }

  private static void cancelAtAssignHiringManager(int count) throws IOException {
    for (int i = 0; i < count; i++) {
      Map<String, VariableValue> variables = assignHiringManagerPath();
      candidateCancelled(assignHiringManagerPath());
      startProcessInstance(variables);
    }
    sendCandidateCancelEvent();
  }

  private static void cancelAtScreenApplication(int count) throws IOException {
    for (int i = 0; i < count; i++) {
      Map<String, VariableValue> variables = automaticHiringManagerAssignmentPath();
      candidateCancelled(variables);
      startProcessInstance(variables);
    }
    sendCandidateCancelEvent();
  }

  private static void cancelAtConductPhoneInterview(int count) throws IOException {
    for (int i = 0; i < count; i++) {
      Map<String, VariableValue> variables = createHappyPath();
      candidateCancelled(variables);
      startProcessInstance(variables);
    }
    UserTaskCompleter userTaskCompleter = new UserTaskCompleter();
    userTaskCompleter.completeUserTasks(ASSIGN_HIRING_MANAGER);
    userTaskCompleter.completeUserTasks(SCREEN_APPLICATION);
    sendCandidateCancelEvent();
  }

  private static void cancelAtFirstOnsiteInterview(int count) throws IOException {
    for (int i = 0; i < count; i++) {
      Map<String, VariableValue> variables = createHappyPath();
      candidateCancelled(variables);
      startProcessInstance(variables);
    }
    UserTaskCompleter userTaskCompleter = new UserTaskCompleter();
    userTaskCompleter.completeUserTasks(ASSIGN_HIRING_MANAGER);
    userTaskCompleter.completeUserTasks(SCREEN_APPLICATION);
    userTaskCompleter.completeUserTasks(CONDUCT_PHONE_INTERVIEW);
    sendCandidateCancelEvent();
  }

  private static void cancelAtSecondOnsiteInterview(int count) throws IOException {
    for (int i = 0; i < count; i++) {
      Map<String, VariableValue> variables = createHappyPath();
      candidateCancelled(variables);
      startProcessInstance(variables);
    }
    UserTaskCompleter userTaskCompleter = new UserTaskCompleter();
    userTaskCompleter.completeUserTasks(ASSIGN_HIRING_MANAGER);
    userTaskCompleter.completeUserTasks(SCREEN_APPLICATION);
    userTaskCompleter.completeUserTasks(CONDUCT_PHONE_INTERVIEW);
    userTaskCompleter.completeUserTasks(CONDUCT_FIRST_ONSITE_INTERVIEW);
    sendCandidateCancelEvent();
  }

  private static void cancelAtMakeAnOffer(int count) throws IOException {
    for (int i = 0; i < count; i++) {
      Map<String, VariableValue> variables = createHappyPath();
      candidateCancelled(variables);
      startProcessInstance(variables);
    }
    UserTaskCompleter userTaskCompleter = new UserTaskCompleter();
    userTaskCompleter.completeUserTasks(ASSIGN_HIRING_MANAGER);
    userTaskCompleter.completeUserTasks(SCREEN_APPLICATION);
    userTaskCompleter.completeUserTasks(CONDUCT_PHONE_INTERVIEW);
    userTaskCompleter.completeUserTasks(CONDUCT_FIRST_ONSITE_INTERVIEW);
    userTaskCompleter.completeUserTasks(CONDUCT_SECOND_ONSITE_INTERVIEW);
    sendCandidateCancelEvent();
  }

  // -----

  private static final int HAPPY_PATH_COUNT = 240;
  private static final int REJECT_AFTER_SCREENING_COUNT = 3778;
  private static final int REJECT_AFTER_PHONE_COUNT = 2790;
  private static final int REJECT_AFTER_ONSITE_COUNT = 1282;
  private static final int LOOSE_CANDIDATE_COUNT = 286;

  private static final int ASSIGN_HIRING_MANAGER_CANCEL_COUNT = 208;
  private static final int SCREEN_APPLICATION_CANCEL_COUNT = 1800;
  private static final int CONDUCT_PHONE_INTERVIEW_CANCEL_COUNT = 360;
  private static final int CONDUCT_FIRST_ONSITE_INTERVIEW_CANCEL_COUNT = 78;
  private static final int CONDUCT_SECOND_ONSITE_INTERVIEW_CANCEL_COUNT = 55;
  private static final int MAKE_AN_OFFER_CANCEL_COUNT = 135;

  public static void main(String[] args) throws IOException {
    // start instances
    for (int i = 0; i < HAPPY_PATH_COUNT; i++) {
      startProcessInstance(createHappyPath());
    }
    System.out.println("Finished with happy path count");
    for (int i = 0; i < REJECT_AFTER_SCREENING_COUNT; i++) {
      startProcessInstance(rejectCandidateAfterScreenApplication());
    }
    System.out.println("Finished with reject after screening");
    for (int i = 0; i < REJECT_AFTER_PHONE_COUNT; i++) {
      startProcessInstance(rejectCandidateAfterPhoneInterview());
    }
    System.out.println("Finished with reject after phone interview");
    for (int i = 0; i < REJECT_AFTER_ONSITE_COUNT; i++) {
      startProcessInstance(rejectCandidateAfterOnsiteInterview());
    }
    System.out.println("Finished with reject after onsite interview");
    for (int i = 0; i < LOOSE_CANDIDATE_COUNT; i++) {
      startProcessInstance(looseCandidate());
    }
    System.out.println("Finished with loose candidate");
    // complete all tasks
    UserTaskCompleter userTaskCompleter = new UserTaskCompleter();
    userTaskCompleter.completeAllUserTasks();
    System.out.println("Finished user task completion");

    // send candidate replied
    sendCandidateCandidateReplied();
    System.out.println("Finished candidate replied message");

    // cancel paths
    cancelAtAssignHiringManager(ASSIGN_HIRING_MANAGER_CANCEL_COUNT);
    System.out.println("Finished assign hiring manager cancellation");
    cancelAtScreenApplication(SCREEN_APPLICATION_CANCEL_COUNT);
    System.out.println("Finished screen application cancellation");
    cancelAtConductPhoneInterview(CONDUCT_PHONE_INTERVIEW_CANCEL_COUNT);
    System.out.println("Finished phone interview cancellation");
    cancelAtFirstOnsiteInterview(CONDUCT_FIRST_ONSITE_INTERVIEW_CANCEL_COUNT);
    cancelAtSecondOnsiteInterview(CONDUCT_SECOND_ONSITE_INTERVIEW_CANCEL_COUNT);
    cancelAtMakeAnOffer(MAKE_AN_OFFER_CANCEL_COUNT);
    System.out.println("Finished make an offer cancelation");

  }
}
