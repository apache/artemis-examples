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
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * This class will be running as long as nobody interrupts it.
 * The purpose is for users to sense how mirroring can be used with HA. */
public class Consumer extends Thread {

   String uri;

   public Consumer(String uri) {
      this.uri = uri;
   }

   @Override
   public void run() {

      while (true) {
         try {
            ConnectionFactory factory = new org.apache.qpid.jms.JmsConnectionFactory(uri);
            try (Connection connection = factory.createConnection()) {
               Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
               Queue queue = session.createQueue("exampleQueue");
               MessageConsumer consumer = session.createConsumer(queue);
               connection.start();
               while (true) {
                  TextMessage jmsMessage = (TextMessage) consumer.receive();
                  System.out.println("Consumer " + uri + ": Received '" + jmsMessage.getText() + "'");
               }
            }
         } catch (Exception e) {
            System.err.println("Consumer " + uri + ": Error: " + e.getMessage());
            try {
               Thread.sleep(5000);
            } catch (Throwable ignored0) {
            }
         }
      }
   }


   public static void main(String arg[]) {
      Consumer consumer = new Consumer("failover:(amqp://localhost:61616,amqp://localhost:61617,amqp://localhost:61618)?failover.maxReconnectAttempts=-1");
      consumer.start();
      try {
         while (true) {
            Thread.sleep(1000);
         }
      } catch (InterruptedException e) {
         System.exit(-1);
      }
   }

}
