/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.dispatch;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.concurrency.LockSupport;
import org.ws4d.java.message.Message;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashMap.Entry;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;
import org.ws4d.java.util.Log;

/**
 * This class is the place to register {@link MessageListener listeners} for
 * messages crossing the DPWS framework. This is especially useful for clients
 * interested in receiving specific types of messages or for traffic analyzers
 * willing to get notified about each DPWS message sent or received.
 * <p>
 * When registering, a listener can declare its interest for certain messages by
 * passing a {@link MessageSelector} to the method
 * {@link #addMessageListener(MessageListener, MessageSelector)}. The
 * <code>MessageSelector</code>'s {@link MessageSelector#matches(Message)}
 * method is used to determine whether a given message matches this interest or
 * not. The DPWS framework provides two standard implementations of
 * <code>MessageSelector</code>: {@link AllMessageSelector}, which simply marks
 * every message as interesting, and {@link DefaultMessageSelector} allowing
 * discrimination based on a message's type and/or its target endpoint address
 * (i.e. the WS-Addressing [destination] property of the message).
 * </p>
 */
public class MessageInformer {

	private static MessageInformer	instance;

	// key = MessageListener, value = MessageSelector
	private final HashMap			listeners			= new HashMap();

	// SYNC: this lock support instance protects the listeners map
	private final LockSupport		listenersLock		= new LockSupport();

	private final List				queuedMessages		= new ArrayList();

	// SYNC: this lock support instance protects the messages queue
	private final LockSupport		queuedMessagesLock	= new LockSupport();

	/*
	 * this object is used for notifications of the delivery thread after new
	 * messages have arrived
	 */
	private final Object			notifier			= new Object();

	private volatile boolean		stopRunning			= true;

	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return the singleton message informer
	 */
	public static synchronized MessageInformer getInstance() {
		if (instance == null) {
			instance = new MessageInformer();
		}
		return instance;
	}

	/*
	 * disallow any instances
	 */
	private MessageInformer() {
		super();
	}

	/**
	 * Starts the message delivery loop of this message informer instance. Does
	 * nothing, if the message informer is already running.
	 */
	public void start() {
		if (!stopRunning) {
			return;
		}
		Runnable r = new Runnable() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				while (true) {
					try {
						deliverMessages();
						if (stopRunning) {
							return;
						}
					} catch (Throwable t) {
						Log.error("MessageInformer: uncaught exception broke out: " + t);
					}
				}
			}

		};
		stopRunning = false;
		// get thread from platform toolkit / thread pool
		DPWSFramework.getThreadPool().execute(r);
	}

	/**
	 * Stops the message informer, i.e. terminates the message delivery loop.
	 */
	public void stop() {
		stopRunning = true;
		synchronized (notifier) {
			notifier.notify();
		}
	}

	/**
	 * Adds the specified message listener to this message informer. The
	 * listener will receive notifications about messages running through the
	 * DPWS framework that match the specified message selector. If
	 * <code>sel</code> is <code>null</code>, the listener will be registered
	 * with an {@link AllMessageSelector} associated to it and thus will receive
	 * notifications about <em>every</em> message.
	 * 
	 * @param listener the listener to register
	 * @param sel the selector determining which messages to deliver to the
	 *            newly registered listener
	 */
	public void addMessageListener(MessageListener listener, MessageSelector sel) {
		if (listener == null) {
			return;
		}
		if (sel == null) {
			sel = AllMessageSelector.INSTANCE;
		}
		try {
			listenersLock.exclusiveLock();
			listeners.put(listener, sel);
		} finally {
			listenersLock.releaseExclusiveLock();
		}
	}

	/**
	 * Removes the specified message listener, if it was previously registered
	 * within this message informer.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeMessageListener(MessageListener listener) {
		if (listener == null) {
			return;
		}
		try {
			listenersLock.exclusiveLock();
			listeners.remove(listener);
		} finally {
			listenersLock.releaseExclusiveLock();
		}
	}

	/**
	 * Returns an array containing all currently registered message listeners.
	 * 
	 * @return an array consisting of all currently registered listeners
	 */
	public MessageListener[] getMessageListeners() {
		MessageListener[] result;
		try {
			listenersLock.sharedLock();
			result = (MessageListener[]) listeners.keySet().toArray(new MessageListener[listeners.size()]);
		} finally {
			listenersLock.releaseSharedLock();
		}
		return result;
	}

	/**
	 * Forwards a single message to all registered listeners.
	 * 
	 * @param msg the message to forward
	 * @param protocolData transport-specific addressing information attached to
	 *            the message
	 */
	public void forwardMessage(Message msg, ProtocolData protocolData) {
		forwardMessageInternal(msg, protocolData, true);
	}

	/**
	 * Forwards an array of messages to all registered listeners at once.
	 * 
	 * @param msgs the messages to forward
	 */
	// XXX do we need this API???
	void forwardMessages(Message[] msgs) {
		if (msgs == null) {
			return;
		}

		for (int i = 0; i < msgs.length; i++) {
			forwardMessageInternal(msgs[i], null, false);
		}
		synchronized (notifier) {
			notifier.notify();
		}
	}

	private void forwardMessageInternal(Message msg, ProtocolData protocolData, boolean notifyRunner) {
		if (msg == null) {
			return;
		}
		try {
			queuedMessagesLock.exclusiveLock();
			queuedMessages.add(new MessageEntry(msg, protocolData));
		} finally {
			queuedMessagesLock.releaseExclusiveLock();
		}
		if (notifyRunner) {
			synchronized (notifier) {
				notifier.notify();
			}
		}
	}

	private void deliverMessages() {
		try {
			queuedMessagesLock.sharedLock();
			//(INGO) potentially DANGEROUS because the variable size in class List is not volatile
			while (queuedMessages.size() == 0) {
				queuedMessagesLock.releaseSharedLock();
				try {
					synchronized (notifier) {
						notifier.wait();
					}
				} catch (InterruptedException e) {
					// void
				}
				queuedMessagesLock.sharedLock();
				if (stopRunning) {
					try {
						queuedMessagesLock.exclusiveLock();
						queuedMessages.clear();
					} finally {
						queuedMessagesLock.releaseExclusiveLock();
					}
					return;
				}
			}
		} finally {
			queuedMessagesLock.releaseSharedLock();
		}
		MessageEntry messageEntry;
		try {
			queuedMessagesLock.exclusiveLock();
			messageEntry = (MessageEntry) queuedMessages.remove(0);
		} finally {
			queuedMessagesLock.releaseExclusiveLock();
		}
		try {
			listenersLock.sharedLock();
			for (Iterator it2 = listeners.entrySet().iterator(); it2.hasNext();) {
				Entry ent = (Entry) it2.next();
				MessageSelector sel = (MessageSelector) ent.getValue();
				Message msg = messageEntry.message;
				if (sel.matches(msg)) {
					MessageListener listener = (MessageListener) ent.getKey();
					if (msg.isInbound()) {
						listener.receivedInboundMessage(msg, messageEntry.protocolData);
					} else {
						listener.receivedOutboundMessage(msg, messageEntry.protocolData);
					}
				}
			}
		} finally {
			listenersLock.releaseSharedLock();
		}
	}

	private static final class MessageEntry {

		final Message		message;

		final ProtocolData	protocolData;

		/**
		 * @param message
		 * @param protocolData
		 */
		MessageEntry(Message message, ProtocolData protocolData) {
			super();
			this.message = message;
			this.protocolData = protocolData;
		}

	}
}
