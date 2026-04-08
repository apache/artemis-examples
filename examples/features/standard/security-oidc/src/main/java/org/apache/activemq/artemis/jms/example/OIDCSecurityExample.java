/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.artemis.jms.example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Queue;
import javax.naming.InitialContext;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.artemis.api.core.JsonUtil;
import org.apache.activemq.artemis.json.JsonObject;
import org.apache.activemq.artemis.json.JsonString;
import org.apache.activemq.artemis.utils.Waiter;

public class OIDCSecurityExample {

   public static void main(final String[] args) throws Exception {

      Connection connection = null;

      InitialContext initialContext = null;

      try {
         // Step 0. Wait for artemis-keycloak-demo
         Waiter.waitFor(() -> {
            int responseCode = 0;
            try {
               URL url = new URL("http://localhost:8080/realms/artemis-keycloak-demo/.well-known/openid-configuration");
               HttpURLConnection con = (HttpURLConnection) url.openConnection();
               responseCode = con.getResponseCode();
               con.disconnect();
            } catch (Exception expectedTillInfraStarted) {
               System.out.println("---- expected error on startup till artemis-keycloak-demo starts: " + expectedTillInfraStarted + ", retry in 5s");
            }
            return responseCode == 200;
         }, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, 5);

         // Step 1. Create an initial context to perform the JNDI lookup.
         initialContext = new InitialContext();

         // Step 2. perform lookup on the topics
         Queue genericTopic = (Queue) initialContext.lookup("queue/Info");

         // Step 3. perform a lookup on the Connection Factory
         ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

         HttpClient client = HttpClient.newBuilder().build();

         // client_id should be an OIDC/Keycloak client with
         // "Service account roles" (OAuth2 "") Authentication flow (grant_type=client_credentials) enabled.
         // such Keycloak client gets new tab "Service Account Roles" where we can add realm/client roles
         // assigned to this client. Here there's only one entity accessing the resource on its own behalf.
         //
         // With other grants (including not recommended grant_type=password == "Direct access grants"
         // == OAuth2 "Resource Owner Password Credentials Grant") the client is an entity which acts on behalf
         // of another entity ("resource owner")
         //
         // https://redhat.atlassian.net/browse/KEYCLOAK-8482 ensures that the client_id used when accessing the
         // token is NOT added to the "aud" claim (audience), which effectively means that this client doesn't
         // want to "access itself" - only other "services" which may be represent as _other_ clients configured
         // in Keycloak.
         //
         // See https://datatracker.ietf.org/doc/html/rfc6749#section-4

         HttpRequest request = HttpRequest.newBuilder()
                 .uri(URI.create("http://localhost:8080/realms/artemis-keycloak-demo/protocol/openid-connect/token"))
                 .header("Content-Type", "application/x-www-form-urlencoded")
                 .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=artemis-client&client_secret=oXeoAhe24pKk5GDE9nmukbp3cnoIWxem&scope=openid+email")).build();
         HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

         JsonObject data = JsonUtil.readJsonObject(resp.body());

         String token = ((JsonString) data.get("access_token")).getString();
         System.out.println(token);

         // Step 4. block till we make a connection
         System.out.println("------------------------blocking on connection creation----------------");

         while (connection == null) {
            try {
               // username is not used at the server side for authentication
               connection = createConnection("artemis-client", token, cf);
               connection.start();
            } catch (JMSException expectedTillInfraStarted) {
               System.out.println("---- expected error on connect till broker starts: " + expectedTillInfraStarted + ", retry in 10s");
               TimeUnit.SECONDS.sleep(10);
            }
         }

         // Step 5. block till we get a message
         System.out.println("------------------------blocking on message receipt from console----------------");
         System.out.println("------------------------send to address Info as user with JWT token----------------");

         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageConsumer consumer = session.createConsumer(genericTopic);

         Message receivedMsg = null;
         while (receivedMsg == null) {
            receivedMsg = consumer.receive(10000);
            if (receivedMsg != null) {
               System.out.println("---------------------received: " + receivedMsg);
               System.out.println("---------------------received: " + receivedMsg.getBody(String.class));
               System.out.println("---------------------all done!------------------------------------------");
            }
         }

         session.close();

         System.out.println("-------------------------------------------------------------------------------------");

      } finally {
         if (connection != null) {
            connection.close();
         }
      }
   }

   private static Connection createConnection(final String username,
                                              final String password,
                                              final ConnectionFactory cf) throws JMSException {
      return cf.createConnection(username, password);
   }
}
