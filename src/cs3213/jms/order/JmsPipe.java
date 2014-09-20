package cs3213.jms.order;

import static cs3213.jms.queue.QueueSend.JMS_FACTORY;
import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Matric 1: Chan Xian Chen Edward
 * Name   1: A0097865H
 *
 * Matric 2: Koh Zheng Kang
 * Name   2: A0097973H
 *
 * This file implements a pipe that transfer messages using JMS.
 */

public class JmsPipe implements IPipe {

    private QueueConnectionFactory qconFactory;
    private QueueConnection qcon;
    private QueueSession qsession;
    private QueueSender qsender;
    private QueueReceiver qreceiver;
    private Queue queue;
    private TextMessage msg;

    public JmsPipe(String factoryName, String queueName) throws NamingException, JMSException {
        InitialContext ctx = getInitialContext();
        qconFactory = (QueueConnectionFactory) ctx.lookup(factoryName);
        qcon = qconFactory.createQueueConnection();
        qcon.start();
        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) ctx.lookup(queueName);
        msg = qsession.createTextMessage();
    }

    public void write(Order s) {
        try {
            if (qsender == null)
                qsender = qsession.createSender(queue);
            msg.setText(s.toString());
            qsender.send(msg);
        }
        catch (Exception e) {
            System.err.println("[JmsPipe]Error sending message!");
            e.printStackTrace();
        }
    }

    public Order read() {
        try {
            if (qreceiver == null)
                qreceiver = qsession.createReceiver(queue);
            TextMessage receivedMsg = (TextMessage)qreceiver.receive();
            return new Order("", "").fromString(receivedMsg.getText());
        }
        catch (Exception e) {
            System.err.println("[JmsPipe]Error receiving message!");
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            if (qsender != null) qsender.close();
            if (qreceiver != null) qreceiver.close();
            qsession.close();
            qcon.close();
        }
        catch (Exception e) {
            System.err.println("[JmsPipe]Error closing JMS pipe!");
            e.printStackTrace();
        }
    }

    private static InitialContext getInitialContext() throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.PROVIDER_URL, "jnp://localhost:1099");
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        return new InitialContext(props);
    }
}
