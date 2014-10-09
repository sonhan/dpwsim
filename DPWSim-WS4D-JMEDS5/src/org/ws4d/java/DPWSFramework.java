/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java;

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.attachment.AttachmentException;
import org.ws4d.java.attachment.AttachmentFactory;
import org.ws4d.java.attachment.AttachmentStore;
import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.CommunicationManagerRegistry;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.communication.monitor.MonitorStreamFactory;
import org.ws4d.java.communication.monitor.ResourceLoader;
import org.ws4d.java.concurrency.ThreadPool;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.configuration.Properties;
import org.ws4d.java.constants.FrameworkConstants;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.dispatch.MessageInformer;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.eventing.EventingFactory;
import org.ws4d.java.io.fs.FileSystem;
import org.ws4d.java.presentation.Presentation;
import org.ws4d.java.security.SecurityManager;
import org.ws4d.java.service.ProxyFactory;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.Toolkit;
import org.ws4d.java.util.WS4DIllegalStateException;
import org.ws4d.java.util.WatchDog;

/**
 * <p>
 * This is the main framework class for the Java Multiedition DPWS Stack (JMEDS
 * 2.0).
 * </p>
 * <p>
 * It offers <i>static</i> methods to start and stop the framework.
 * </p>
 * <p>
 * This class verifies the existence of the following modules:
 * <ul>
 * <li>Client support</li>
 * <li>Device and Service support</li>
 * <li>Event support</li>
 * <li>Special platform dependent implementation of the communication and file
 * system</li>
 * <li>Attachment support</li>
 * <li>Security support</li>
 * </ul>
 * </p>
 * <p>
 * Furthermore this class allows access to some special and optional framework
 * components like:
 * <ul>
 * <li>{@link ThreadPool}</li>
 * <li>{@link CommunicationManager} <i>(at least one is necessary)</i></li>
 * <li>{@link MonitorStreamFactory} <i>(optional)</i></li>
 * <li>{@link FileSystem} <i>(optional)</i></li>
 * </ul>
 * </p>
 * <p>
 * <strong>Important:</strong> It is necessary to {@link #start(String[]) start}
 * the framework before anything else can be used!
 * </p>
 * <p>
 * Your code could look something like this:
 * </p>
 * 
 * <pre>
 * DPWSFramework.start(args);
 * 
 * // Your code here
 * 
 * DPWSFramework.stop();
 * </pre>
 */
public final class DPWSFramework {

	/**
	 * Identifier for the client support (Client module).
	 * <p>
	 * This identifier can be used to verify whether the <i>Client module</i>
	 * has been loaded or not. To check this module, use the
	 * {@link #hasModule(int)} method.
	 * </p>
	 * <p>
	 * The <i>Client module</i> includes the classes to create a DPWS client and
	 * the classes which are necessary if the client wants to use the device and
	 * service discovery.
	 * </p>
	 */
	public static final int				CLIENT_MODULE				= 0x01;

	/**
	 * Identifier for the service and device support. (Service module).
	 * <p>
	 * This identifier can be used to verify whether the <i>Service module</i>
	 * has been loaded or not. To check this module, use the
	 * {@link #hasModule(int)} method.
	 * </p>
	 * <p>
	 * The <i>Service module</i> includes the classes to create a DPWS device
	 * and service.
	 * </p>
	 */
	public static final int				SERVICE_MODULE				= 0x02;

	/**
	 * Identifier for the event support. (Eventing module)
	 * <p>
	 * This identifier can be used to verify whether the <i>Eventing module</i>
	 * has been loaded or not. To check this module, use the
	 * {@link #hasModule(int)} method.
	 * </p>
	 * <p>
	 * The <i>Eventing module</i> includes the classes to handle incoming DPWS
	 * events.
	 * </p>
	 */
	public static final int				EVENTING_MODULE				= 0x04;

	/**
	 * Identifier for the SE platform support. (SE module)
	 * <p>
	 * This identifier can be used to verify whether the <i>SE module</i> has
	 * been loaded or not. To check this module, use the {@link #hasModule(int)}
	 * method.
	 * </p>
	 * <p>
	 * The <i>SE module</i> includes the classes which allow networking and file
	 * access for Java SE platforms.
	 * </p>
	 */
	public static final int				PLATFORM_SE_MODULE			= 0x08;

	/**
	 * Identifier for the CLDC platform support. (CLDC module)
	 * <p>
	 * This identifier can be used to verify whether the <i>CLDC module</i> has
	 * been loaded or not. To check this module, use the {@link #hasModule(int)}
	 * method.
	 * </p>
	 * <p>
	 * The <i>CLDC module</i> includes the classes which allow networking for
	 * Java CLDC platforms.
	 * </p>
	 */
	public static final int				PLATFORM_CLDC_MODULE		= 0x10;

	/**
	 * Identifier for the attachment support. (Attachment module)
	 * <p>
	 * This identifier can be used to verify whether the <i>Attachment
	 * module</i> has been loaded or not. To check this module, use the
	 * {@link #hasModule(int)} method.
	 * </p>
	 * <p>
	 * The <i>Attachment module</i> includes the classes to send and receive
	 * attachments.
	 * </p>
	 */
	public static final int				ATTACHMENT_MODULE			= 0x20;

	/**
	 * Identifier for the security support. (Security module)
	 * <p>
	 * This identifier can be used to verify whether the <i>Security module</i>
	 * has been loaded or not. To check this module, use the
	 * {@link #hasModule(int)} method.
	 * </p>
	 * <p>
	 * The <i>Security module</i> includes the classes to secure the DPWS
	 * communication, using WS-Security techniques.
	 * </p>
	 */
	public static final int				SECURITY_MODULE				= 0x40;

	public static final int				PRESENTATION_MODULE			= 0x80;

	public static final int				COMMUNICATION_DPWS_MODULE	= 0x100;

	private static final boolean		HAVE_CLIENT_MODULE;

	private static final boolean		HAVE_SERVICE_MODULE;

	private static final boolean		HAVE_EVENTING_MODULE;

	private static final boolean		HAVE_PLATFORM_SE_MODULE;

	private static final boolean		HAVE_PLATFORM_CLDC_MODULE;

	private static final boolean		HAVE_ATTACHMENT_MODULE;

	private static final boolean		HAVE_PRESENTATION_MODULE;

	private static final boolean		HAVE_COMMUNICATION_DPWS_MODULE;

	private static final boolean		HAVE_SECURITY_MODULE;

	private static final int			KILL_WAIT_TIME				= 2000;

	static {
		boolean result = false;
		try {
			Class.forName("org.ws4d.java.client.DefaultClient");
			result = true;
		} catch (ClassNotFoundException e) {
			// void
		}
		HAVE_CLIENT_MODULE = result;

		result = false;
		try {
			Class.forName("org.ws4d.java.service.DefaultService");
			result = true;
		} catch (ClassNotFoundException e) {
			// void
		}
		HAVE_SERVICE_MODULE = result;

		result = false;
		try {
			Class.forName("org.ws4d.java.eventing.DefaultEventingFactory");
			result = true;
		} catch (ClassNotFoundException e) {
			// void
		}
		HAVE_EVENTING_MODULE = result;

		result = false;
		try {
			Class.forName("org.ws4d.java.communication.connection.tcp.SESocket");
			result = true;
		} catch (ClassNotFoundException e) {
			// void
		}
		HAVE_PLATFORM_SE_MODULE = result;

		result = false;
		try {
			Class.forName("org.ws4d.java.communication.connection.tcp.CLDCSocket");
			result = true;
		} catch (ClassNotFoundException e) {
			// void
		}
		HAVE_PLATFORM_CLDC_MODULE = result;

		result = false;
		try {
			Class.forName("org.ws4d.java.attachment.AbstractAttachment");
			result = true;
		} catch (ClassNotFoundException e) {
			// void
		}
		HAVE_ATTACHMENT_MODULE = result;

		result = false;
		try {
			Class.forName("org.ws4d.java.presentation.DeviceServicePresentation");
			result = true;
		} catch (ClassNotFoundException e) {
			// void
		}
		HAVE_PRESENTATION_MODULE = result;

		result = false;
		try {
			Class.forName("org.ws4d.java.security.DPWSSecurityManagerSE");
			result = true;
		} catch (ClassNotFoundException e) {
			// void
		}
		if (!result) {
			try {
				Class.forName("org.ws4d.java.security.DPWSSecurityManagerCLDC");
				result = true;
			} catch (ClassNotFoundException e) {
				// void
			}
		}
		HAVE_SECURITY_MODULE = result;

		result = false;
		try {
			Class.forName("org.ws4d.java.communication.DPWSCommunicationManager");
			result = true;
		} catch (ClassNotFoundException e) {
			// void
		}
		HAVE_COMMUNICATION_DPWS_MODULE = result;

	}

	private static final Object			LOCAL_FILE_SYSTEM_LOCK		= new Object();

	private static final Object			ATTACHMENT_FACTORY_LOCK		= new Object();

	private static final Object			EVENTING_FACTORY_LOCK		= new Object();

	private static final Object			PROXY_FACTORY_LOCK			= new Object();

	/**
	 * Indicator for framework run state.
	 */
	private static volatile boolean		running						= false;

	/**
	 * The instance thread pool.
	 */
	private static ThreadPool			threadpool					= null;

	private static final Properties		properties					= Properties.getInstance();

	private static MonitorStreamFactory	monitorFactory				= null;

	private static Toolkit				toolkit						= null;

	private static FileSystem			localFileSystem				= null;

	private static AttachmentFactory	attachmentFactory			= null;

	private static EventingFactory		eventingFactory				= null;

	private static ProxyFactory			proxyFactory				= null;

	private static SecurityManager		securityManager				= null;

	private static Presentation			presentation				= null;

	private static String				propertiesPath				= null;

	private static HashSet				subscriptions				= new HashSet();

	private static int					haltPhase					= 0;

	private static boolean				killingThread				= false;

	static {
		boolean succ = false;
		try {
			Class clazz = Class.forName("org.ws4d.java.security.DPWSSecurityManagerSE");
			securityManager = ((SecurityManager) clazz.newInstance());
			succ = true;
		} catch (ClassNotFoundException e) {} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		if (!succ) {
			try {
				Class clazz = Class.forName("org.ws4d.java.security.cldc.DPWSSecurityManagerCLDC");
				securityManager = (SecurityManager) clazz.newInstance();
				succ = true;
			} catch (ClassNotFoundException e) {} catch (InstantiationException e) {} catch (IllegalAccessException e) {
				// void
			}
		}

	}

	/**
	 * Starts the framework.
	 * <p>
	 * This method initializes the necessary framework components.
	 * </p>
	 * <p>
	 * <strong>Important:</strong> It is necessary to {@link #start(String[])
	 * start} the framework before anything else can be used!
	 * </p>
	 * <p>
	 * This method starts the watchdog, loads the properties and initializes the
	 * communications modules.
	 * </p>
	 * 
	 * @param args Here you can pass-through the command-line arguments. the
	 *            first element is interpreted as the location of the properties
	 *            file.
	 */
	public static synchronized void start(String[] args) {
		if (running) return;
		try {
			// MessageInformer.getInstance();
			// load communication managers
			CommunicationManagerRegistry.loadAll();

			// load properties
			if (args != null && args.length >= 1) {
				propertiesPath = args[0];
			}

			if (propertiesPath != null) {
				try {
					properties.init(propertiesPath);
				} catch (Exception e) {
					Log.printStackTrace(e);
				}
			} else {
				properties.init();
			}

			// thread pool
			threadpool = new ThreadPool(FrameworkProperties.getInstance().getThreadPoolSize());

			// platform toolkit
			createToolkit();

			// start watchdog
			boolean watchdog = getThreadPool().executeOrAbort(WatchDog.getInstance());
			if (watchdog == false) {
				throw new RuntimeException("Cannot start the watchdog.");
			}

			// start message informer
			MessageInformer.getInstance().start();

			// start communication managers
			CommunicationManagerRegistry.startAll();

			// DeviceServiceRegistry.init();

			// Mark the framework as up and running.
			running = true;
			Log.info("DPWS Framework ready.");
		} catch (Exception e) {
			Log.info("DPWS Framework not started.");
			Log.printStackTrace(e);
		}
	}

	/**
	 * Stops the framework as soon as possible.
	 * <p>
	 * This method is the counter piece to {@link #start(String[])}. It stops
	 * the framework and the running components. This method will wait until the
	 * opened connection are ready to be closed.
	 * </p>
	 * <p>
	 * If it is necessary to stop the framework immediately the {@link #kill()}
	 * method should be used.
	 * </p>
	 * 
	 * @see #start(String[])
	 * @see #kill()
	 */
	public static synchronized void stop() {
		stopInternal(false, 0);
	}

	/**
	 * Stops the framework immediately!!!!
	 * <p>
	 * This method is the counter piece to {@link #start(String[])}. It stops
	 * the framework and the running components. This method will
	 * <strong>not</strong> wait until the opened connection are ready to be
	 * closed, any existing connection will be closed instant.
	 * </p>
	 * 
	 * @see #start(String[])
	 * @see #stop()
	 */
	public static synchronized void kill() {
		stopInternal(true, 0);
	}

	private static int getHaltPhase() {
		return haltPhase;
	}

	private static void setHaltPhase(int i) {
		if (killingThread) return;
		haltPhase = i;
	}

	private static void killNow() {
		killingThread = true;
	}

	private static void stopInternal(boolean kill, int phase) {
		if (!running) return;

		if (!kill && running) {
			/*
			 * If we should stop ...
			 */
			Thread t = new Thread() {

				public void run() {
					try {
						Thread.sleep(KILL_WAIT_TIME);
						killNow();
						stopInternal(true, getHaltPhase());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			t.start();
		}
		if (kill && running) {
			if (phase == 0) {
				Log.info("Killing DPWS Framework...");
			} else {
				Log.info("Killing DPWS Framework because stop does not work...");
			}
		} else {
			Log.info("Stopping DPWS Framework...");
		}

		/*
		 * unsubscribe
		 */
		if (phase <= 0 && running) {
			if (Log.isDebug()) {
				Log.debug("Unsubscribing from all event sources.", Log.DEBUG_LAYER_FRAMEWORK);
			}
			unsubscribeAll();
			setHaltPhase(1);
		}

		/*
		 * stop devices and services
		 */
		if (phase <= 1 && running) {
			if (supportsConfiguration(SERVICE_MODULE | EVENTING_MODULE)) {
				DeviceServiceRegistry.tearDown();
			}
			setHaltPhase(2);
		}

		/*
		 * stop communication
		 */
		if (phase <= 2 && running) {
			if (kill) {
				if (Log.isDebug()) {
					Log.debug("Killing communication managers.", Log.DEBUG_LAYER_FRAMEWORK);
				}
				CommunicationManagerRegistry.killAll();
			} else {
				if (Log.isDebug()) {
					Log.debug("Stopping communication managers.", Log.DEBUG_LAYER_FRAMEWORK);
				}
				CommunicationManagerRegistry.stopAll();
			}
			setHaltPhase(3);
		}

		/*
		 * stop message informer
		 */
		if (phase <= 3 && running) {
			if (Log.isDebug()) {
				Log.debug("Stopping message informer.", Log.DEBUG_LAYER_FRAMEWORK);
			}
			MessageInformer.getInstance().stop();
			setHaltPhase(4);
		}

		/*
		 * stop watch dog
		 */
		if (phase <= 4 && running) {
			if (Log.isDebug()) {
				Log.debug("Stopping watch dog.", Log.DEBUG_LAYER_FRAMEWORK);
			}
			WatchDog.getInstance().stop();
			setHaltPhase(5);
		}

		/*
		 * clean attachment store
		 */
		if (phase <= 5 && running) {
			if (AttachmentStore.exists()) {
				try {
					AttachmentStore.getInstance().cleanup();
				} catch (AttachmentException e) {
					// void
				}
			}
			setHaltPhase(6);
		}

		/*
		 * stop thread pool
		 */
		if (phase <= 6 && running) {
			if (Log.isDebug()) {
				Log.debug("Shutting down the threadpool.", Log.DEBUG_LAYER_FRAMEWORK);
			}
			threadpool.shutdown();
			setHaltPhase(7);
		}

		if (running) {
			threadpool = null;
			running = false;
			Log.info("DPWS Framework stopped.");
		}
	}

	/**
	 * Indicates whether the framework was started or not.
	 * <p>
	 * This method returns <code>true</code> if the framework is running,
	 * <code>false</code> otherwise.
	 * </p>
	 * 
	 * @return <code>true</code> if the framework is running, <code>false</code>
	 *         otherwise.
	 */
	public static boolean isRunning() {
		return running;
	}

	/**
	 * Adds a event subscription to the framework. This allows the framework to
	 * unsubscribe on shutdown.
	 * 
	 * @param subscription the subscription which the framework should take care
	 *            about.
	 */
	public static void addClientSubscrption(ClientSubscription subscription) {
		synchronized (subscriptions) {
			subscriptions.add(subscription);
		}
	}

	/**
	 * Removes a event subscription.
	 * 
	 * @param subscription the subscription which is not important any more.
	 */
	public static void removeClientSubscrption(ClientSubscription subscription) {
		synchronized (subscriptions) {
			subscriptions.remove(subscription);
		}
	}

	/**
	 * Unsubscribe from all event sources.
	 */
	private static void unsubscribeAll() {
		synchronized (subscriptions) {
			Iterator it = subscriptions.iterator();
			while (it.hasNext()) {
				ClientSubscription cs = (ClientSubscription) it.next();
				it.remove(); // this avoids concurrent modification exceptions
				try {
					cs.unsubscribe();
				} catch (EventingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Allows to verify whether a module has been loaded and can be used or not.
	 * <p>
	 * You can check the modules listed below.
	 * </p>
	 * 
	 * @param module the module identifier.
	 * @return returns <code>true</code> if the module has been loaded,
	 *         <code>false</code> otherwise.
	 * @see #CLIENT_MODULE
	 * @see #SERVICE_MODULE
	 * @see #EVENTING_MODULE
	 * @see #ATTACHMENT_MODULE
	 * @see #PLATFORM_SE_MODULE
	 * @see #PLATFORM_CLDC_MODULE
	 * @see #SECURITY_MODULE
	 * @see #ATTACHMENT_MODULE
	 * @see #PRESENTATION_MODULE
	 */
	public static boolean hasModule(int module) {
		switch (module) {
			case (CLIENT_MODULE): {
				return HAVE_CLIENT_MODULE;
			}
			case (SERVICE_MODULE): {
				return HAVE_SERVICE_MODULE;
			}
			case (EVENTING_MODULE): {
				return HAVE_EVENTING_MODULE;
			}
			case (PLATFORM_SE_MODULE): {
				return HAVE_PLATFORM_SE_MODULE;
			}
			case (PLATFORM_CLDC_MODULE): {
				return HAVE_PLATFORM_CLDC_MODULE;
			}
			case (ATTACHMENT_MODULE): {
				return HAVE_ATTACHMENT_MODULE;
			}
			case (SECURITY_MODULE): {
				return HAVE_SECURITY_MODULE;
			}
			case (PRESENTATION_MODULE): {
				return HAVE_PRESENTATION_MODULE;
			}
			case (COMMUNICATION_DPWS_MODULE): {
				return HAVE_COMMUNICATION_DPWS_MODULE;
			}
		}
		return false;
	}

	/**
	 * Allows to verify whether some modules are loaded or not.
	 * <p>
	 * This method allows to check several modules with one method. If you want
	 * to check only one module, see the {@link #hasModule(int)} method.
	 * <p>
	 * You can check the modules listed below.
	 * </p>
	 * 
	 * @param config the modules to check.
	 *            <p>
	 *            To check more than one module, sum up their values.<br />
	 *            e.g. DPWS_CLIENT_MODULE+DPWS_SERVICE_MODULE
	 *            </p>
	 * @return returns <code>true</code> if all given modules have been loaded,
	 *         <code>false</code> otherwise.
	 * @see #CLIENT_MODULE
	 * @see #SERVICE_MODULE
	 * @see #EVENTING_MODULE
	 * @see #ATTACHMENT_MODULE
	 * @see #PLATFORM_SE_MODULE
	 * @see #PLATFORM_CLDC_MODULE
	 */
	public static boolean supportsConfiguration(int config) {
		if ((config & CLIENT_MODULE) != 0 && !HAVE_CLIENT_MODULE) {
			return false;
		}
		if ((config & SERVICE_MODULE) != 0 && !HAVE_SERVICE_MODULE) {
			return false;
		}
		if ((config & EVENTING_MODULE) != 0 && !HAVE_EVENTING_MODULE) {
			return false;
		}
		if ((config & PLATFORM_SE_MODULE) != 0 && !HAVE_PLATFORM_SE_MODULE) {
			return false;
		}
		if ((config & PLATFORM_CLDC_MODULE) != 0 && !HAVE_PLATFORM_CLDC_MODULE) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the thread pool used by the framework.
	 * <p>
	 * This thread pool is necessary for thread handling, because CLDC does not
	 * have an own thread pool. All threads created by the framework are created
	 * with this thread pool.
	 * </p>
	 * 
	 * @return the thread pool.
	 */
	public static ThreadPool getThreadPool() {
		return threadpool;
	}

	/**
	 * Returns an instance of a communication manager based on the given
	 * identifier.
	 * <p>
	 * The communication manager is a special layer for communication. It allows
	 * the handling of incoming messages for different technologies. e.g. DPWS.
	 * </p>
	 * 
	 * @param comManId the identifier to receive a
	 *            {@link CommunicationManager}.
	 * @return {@link CommunicationManager}
	 */
	public static CommunicationManager getCommunicationManager(String comManId) {
		if (!running) {
			throw new WS4DIllegalStateException("Framework not started correctly or not running.");
		}
		return CommunicationManagerRegistry.getManager(comManId);
	}

	/**
	 * Returns an input stream which allows to read a resource from the given
	 * location.
	 * <p>
	 * The location is a URL. The loaded communication managers can be
	 * registered for different URL schemas. This allows the loading of
	 * resources from different locations.
	 * </p>
	 * 
	 * @param location the location of the resource (e.g.
	 *            http://example.org/test.wsdl).
	 * @return an {@link ResourceLoader} containing input stream for the given
	 *         resource and {@link ProtocolData} for network resources. Returns
	 *         <code>null</code> if no communication manager could find a
	 *         resource at the given location.
	 * @throws IOException throws an exception when the resource could not be
	 *             loaded properly.
	 */
	public static ResourceLoader getResourceAsStream(URI location) throws IOException {
		/*
		 * We can load any file from file system or resource before the
		 * framework is up and running
		 */
		if (location == null) throw new IOException("What?! Cannot find 'null' file. Maybe /dev/null took it.");
		if (location.getSchema().startsWith(FrameworkConstants.SCHEMA_LOCAL)) {
			String file = location.toString().substring(FrameworkConstants.SCHEMA_LOCAL.length() + 1);
			InputStream in = location.getClass().getResourceAsStream(file);
			if (in == null) {
				try {
					FileSystem fs = getLocalFileSystem();
					ResourceLoader rl = new ResourceLoader(fs.readFile(file), null);
					return rl;
				} catch (IOException e) {
					return null;
				}
			}
			ResourceLoader rl = new ResourceLoader(in, null);
			return rl;
		}
		if (location.getSchema().startsWith("file")) {
			try {
				FileSystem fs = getLocalFileSystem();
				ResourceLoader rl = new ResourceLoader(fs.readFile(location.getPath()), null);
				return rl;
			} catch (IOException e) {
				return null;
			}
		}
		/*
		 * This part should be done only if every thing is up and running
		 */
		if (running) {
			for (org.ws4d.java.structures.Iterator it = CommunicationManagerRegistry.getLoadedManagers(); it.hasNext();) {
				CommunicationManager manager = (CommunicationManager) it.next();
				ResourceLoader rl = manager.getResourceAsStream(location);
				if (rl != null) {
					return rl;
				}
			}
		} else {
			Log.warn("Framework could not load the given location before everything is up and running.");
		}
		// no communication manager capable of serving this request, sorry :'(
		return null;
	}

	/**
	 * Get the Toolkit of this framework.
	 * <p>
	 * The retrieved toolkit includes Java Edition specific utility methods.
	 * Framework must have already been started.
	 * </p>
	 * 
	 * @return The specific toolkit used by the framework.
	 */
	public static Toolkit getToolkit() {
		return toolkit;
	}

	/**
	 * Returns the class which allows to create a presentation URL.
	 * 
	 * @return the presentation creator.
	 */
	public static Presentation getPresentation() {
		if (hasModule(PRESENTATION_MODULE)) {
			try {
				Class clazz = Class.forName("org.ws4d.java.presentation.DeviceServicePresentation");
				presentation = (Presentation) clazz.newInstance();
			} catch (Exception e) {
				Log.error(e.getMessage());
				Log.printStackTrace(e);
			}
		}
		return presentation;
	}

	/**
	 * Set the factory for stream monitoring.
	 * <p>
	 * This enables the monitoring of streams for debug purposes. A
	 * <code>MonitorStreamFactory</code> wraps streams to redistribute data. A
	 * communication manager can use the factory to redistribute data to the
	 * streams created by the factory.
	 * </p>
	 * 
	 * @param factory the factory which wraps streams and redistribute data.
	 */
	public static void setMonitorStreamFactory(MonitorStreamFactory factory) {
		monitorFactory = factory;
	}

	/**
	 * Returns the <code>MonitorStreamFactory</code> which allows to wrap
	 * streams and redistribute data.
	 * 
	 * @return the factory to wrap streams and redistribute data.
	 * @see #setMonitorStreamFactory(MonitorStreamFactory)
	 */
	public static MonitorStreamFactory getMonitorStreamFactory() {
		return monitorFactory;
	}

	/**
	 * Returns an implementation of the file system supported by the given
	 * platform.
	 * <p>
	 * It is necessary to load the corresponding module for platform support.
	 * </p>
	 * 
	 * @return an implementation of the file system.
	 * @throws IOException will throw an exception when the module could not be
	 *             loaded correctly or the runtime configuration does not
	 *             support a local file system.
	 * @see #PLATFORM_SE_MODULE
	 * @see #PLATFORM_CLDC_MODULE
	 */
	public static FileSystem getLocalFileSystem() throws IOException {
		synchronized (LOCAL_FILE_SYSTEM_LOCK) {
			if (localFileSystem == null) {
				if (hasModule(PLATFORM_SE_MODULE)) {
					try {
						Class clazz = Class.forName("org.ws4d.java.platform.io.fs.SEFileSystem");
						localFileSystem = (FileSystem) clazz.newInstance();
					} catch (Exception e) {
						throw new IOException(e.toString());
					}
				} else if (hasModule(PLATFORM_CLDC_MODULE)) {
					try {
						Class clazz = Class.forName("org.ws4d.java.platform.io.fs.CLDCFileSystem");
						localFileSystem = (FileSystem) clazz.newInstance();
					} catch (Exception e) {
						throw new IOException(e.toString());
					}
				} else {
					throw new IOException("The current runtime configuration doesn't contain support for a local file system.");
				}
			}
			return localFileSystem;
		}
	}

	public static SecurityManager getSecurityManager() {
		return securityManager;
	}

	public static void setPropertiesPath(String path) {
		propertiesPath = path;
	}

	/**
	 * Returns an implementation of the attachment factory which allows to
	 * handle incoming and outgoing attachments.
	 * <p>
	 * It is necessary to load the corresponding module for attachment support.
	 * </p>
	 * 
	 * @return an implementation of the attachment factory.
	 * @throws IOException
	 */
	public static AttachmentFactory getAttachmentFactory() throws IOException {
		synchronized (ATTACHMENT_FACTORY_LOCK) {
			
			if (attachmentFactory != null)
				return attachmentFactory;
			
			if (hasModule(ATTACHMENT_MODULE)) {
				try {
					Class clazz = Class.forName("org.ws4d.java.attachment.DefaultAttachmentFactory");
					attachmentFactory = (AttachmentFactory) clazz.newInstance();
				} catch (Exception e) {
					throw new IOException(e.toString());
				}
			} else {
				throw new IOException("The current runtime configuration doesn't contain support for a attachments.");
			}
			return attachmentFactory;
		}
	}

	public static EventingFactory getEventingFactory() throws IOException {
		synchronized (EVENTING_FACTORY_LOCK) {
			
			if (eventingFactory != null)
				return eventingFactory;
			
			if (hasModule(EVENTING_MODULE)) {
				try {
					Class clazz = Class.forName("org.ws4d.java.eventing.DefaultEventingFactory");
					eventingFactory = (EventingFactory) clazz.newInstance();
				} catch (Exception e) {
					throw new IOException(e.toString());
				}
			} else {
				throw new IOException("The current runtime configuration doesn't contain support for events.");
			}
			return eventingFactory;
		}
	}

	public static ProxyFactory getProxyFactory() throws IOException {
		synchronized (PROXY_FACTORY_LOCK) {
			
			if (proxyFactory != null)
				return proxyFactory;
			
			if (hasModule(CLIENT_MODULE)) {
				String factoryClassName = FrameworkProperties.getInstance().getProxyServiceFactroryClass();

				final String defaultProxyClass = "org.ws4d.java.service.DefaultProxyFactory";

				if (factoryClassName == null) {
					factoryClassName = defaultProxyClass;
				}
				boolean retry = true;

				while (retry) {
					try {
						Class proxyServiceClass = Class.forName(factoryClassName);
						proxyFactory = (ProxyFactory) proxyServiceClass.newInstance();
						retry = false;
					} catch (ClassNotFoundException e) {
						Log.error("Configured Proxy Factory [" + factoryClassName + "] not found, falling back to default implementation");
					} catch (Exception e) {
						Log.error("Unable to create instance of configured Proxy Factory [" + factoryClassName + "], falling back to default implementation");
						Log.printStackTrace(e);
					}

					if (proxyFactory == null) {
						factoryClassName = defaultProxyClass;
						retry = false;
					}
				}
			} else {
				throw new IOException("The current runtime configuration doesn't contain support for a proxy factory.");
			}
			return proxyFactory;
		}
	}

	/**
	 * checks whether the current framework instance is running on top of the
	 * CLDC library. Returns <code>true</code> if this is the case.
	 * 
	 * @return <code<true</code> in case the framework runs on top of the Java
	 *         CLDC configuration
	 */
	public static boolean onCldcLibrary() {
		try {
			Class.forName("com.sun.cldc.io.ConnectionBase");
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	private static void createToolkit() {
		if (toolkit != null) {
			return;
		}
		if (hasModule(PLATFORM_SE_MODULE) && !onCldcLibrary()) {
			try {
				Class clazz = Class.forName("org.ws4d.java.platform.util.SEToolkit");
				toolkit = (Toolkit) clazz.newInstance();
			} catch (Exception e) {
				Log.error(e.getMessage());
				Log.printStackTrace(e);
			}
		} else if (hasModule(PLATFORM_CLDC_MODULE) && onCldcLibrary()) {
			try {
				Class clazz = Class.forName("org.ws4d.java.platform.util.CLDCToolkit");
				toolkit = (Toolkit) clazz.newInstance();
			} catch (Exception e) {
				Log.error(e.getMessage());
				Log.printStackTrace(e);
			}
		} else {
			Log.info("The current runtime configuration doesn't contain support for a platform toolkit.");
		}
	}

	/**
	 * Hidden default constructor.
	 */
	private DPWSFramework() {
		super();
	}

}
