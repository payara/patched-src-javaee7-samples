/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.javaee7.jms.send.receive.simple;

import java.util.concurrent.TimeoutException;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;

import org.javaee7.jms.send.receive.Resources;

/**
 * Synchronous message reception with container-managed JMSContext.
 *
 * @author Arun Gupta
 */
@Stateless
public class MessageReceiverSync {

    @Inject
    private JMSContext context;

    @Resource(mappedName = Resources.SYNC_CONTAINER_MANAGED_QUEUE)
    Queue myQueue;

    /**
     * Waits to receive a message from the JMS queue. Times out after a given
     * number of milliseconds.
     *
     * @param timeoutInMillis The number of milliseconds this method will wait
     * before throwing an exception.
     * @return The contents of the message.
     * @throws JMSRuntimeException if an error occurs in accessing the queue.
     * @throws TimeoutException if the timeout is reached.
     */
    public String receiveMessage(int timeoutInMillis) throws JMSRuntimeException, TimeoutException {
        String message = context.createConsumer(myQueue).receiveBody(String.class, timeoutInMillis);
        if (message == null) {
            throw new TimeoutException("No message received after " + timeoutInMillis + "ms");
        }
        return message;
    }

    public void receiveAll(int timeoutInMillis) throws JMSException {
        System.out.println("--> Receiving redundant messages ...");
        QueueBrowser browser = context.createBrowser(myQueue);
        while (browser.getEnumeration().hasMoreElements()) {
            System.out.println("--> here is one");
            context.createConsumer(myQueue).receiveBody(String.class, timeoutInMillis);
        }
    }
}
