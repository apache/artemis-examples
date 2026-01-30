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
package org.apache.artemis.jms.example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import java.io.File;

import org.apache.activemq.artemis.util.ServerUtil;
import org.apache.activemq.artemis.utils.FileUtil;

/**
 * Example of live and replicating backup pair using mirroring and a distributed lock from the Lock Coordinator
 */
public class HAWithMirroringExample {

   private static Process server0;

   private static Process server1;

   public static void main(final String[] args) throws Exception {
      final int numMessages = 30;

      // Configure the locks folder. The broker.xml needs to have the proper path in place.
      // also the locks folder needs to be created before the server starts
      configureLocksFolder(args);


      try {

         // Start the two servers
         server0 = ServerUtil.startServer(args[0], HAWithMirroringExample.class.getSimpleName() + "-peer0", 0, 0);
         Thread.sleep(2_000);
         server1 = ServerUtil.startServer(args[1], HAWithMirroringExample.class.getSimpleName() + "-peer1", 1, 0);

         // We connect to the broker holding the lock on the distributed lock
         ConnectionFactory factory = new org.apache.qpid.jms.JmsConnectionFactory(
            "failover:(amqp://localhost:61616,amqp://localhost:61617)?failover.maxReconnectAttempts=-1");


         try (Connection connection = factory.createConnection()) {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("exampleQueue");
            MessageProducer producer = session.createProducer(queue);

            // Send messages in one of the brokers
            for (int i = 0; i < numMessages; i++) {
               producer.send(session.createTextMessage("hello " + i));
            }
            session.commit();

            // kill the server that was probably holding the lock:
            ServerUtil.killServer(server0);


            // now we consume messages after the broker is killed, the client should reconnect to the correct broker

            MessageConsumer consumer = session.createConsumer(queue);
            connection.start();
            for (int i = 0; i < numMessages; i++) {
               TextMessage message = (TextMessage) consumer.receive(5000);
               System.out.println("Received message " + message.getText());
            }
            session.commit();
         }


      } finally {
         ServerUtil.killServer(server0);
         ServerUtil.killServer(server1);
      }
   }

   private static void configureLocksFolder(String[] args) throws Exception {
      File lockFolder = new File("./target/locks");
      lockFolder.mkdirs();
      FileUtil.findReplace(new File(args[0] + "/etc/broker.xml"), "CHANGEME", lockFolder.getAbsolutePath());
      FileUtil.findReplace(new File(args[1] + "/etc/broker.xml"), "CHANGEME", lockFolder.getAbsolutePath());
   }
}
