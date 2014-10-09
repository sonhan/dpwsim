/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.configuration;

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.io.buffered.BufferedInputStream;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.Log;

/**
 * Class provides configuration file/ stream reading and manages to call the
 * different property handlers. Standard handlers will be registered.
 * <p>
 * The file "example.properties" contains a example configuration.
 * </p>
 * Properties have to be initialized with one of the init methods:
 * <ul>
 * <li>{@link #init()}
 * <li>{@link #init(InputStream)}
 * <li>{@link #init(String)}
 * </ul>
 * <p>
 * Properties should be initialized before starting the {@link DPWSFramework}.
 * </p>
 */
public final class Properties {

	public static final String			PROP_HANDLER							= "PropertiesHandler";

	public static final String			PROP_BINDING							= "Binding";

	public static final String			PROP_CONFIGURATION_ID					= "ConfigurationId";

	// SECTIONS
	public final static String[]		SECTION_BINDINGS						= { "Bindings" };

	public final static String[]		SECTION_GLOBAL							= { "Global" };

	public static final String[]		SECTION_DEVICES							= { "Devices" };

	public static final String[]		SECTION_SERVICES						= { "Services" };

	public static final String[]		SECTION_EVENTING						= { "Eventing" };

	public static final String[]		SECTION_DPWS							= { "DPWS" };

	public static final String[]		SECTION_HTTP							= { "HTTP" };

	public static final String[]		SECTION_IP								= { "IP" };

	public static final String[]		SECTION_SECURITY						= { "Security" };

	public final static PropertyHeader	HEADER_SECTION_BINDINGS					= new PropertyHeader(SECTION_BINDINGS);

	public final static PropertyHeader	HEADER_SECTION_GLOBAL					= new PropertyHeader(SECTION_GLOBAL);

	public final static PropertyHeader	HEADER_SECTION_DEVICES					= new PropertyHeader(SECTION_DEVICES);

	public final static PropertyHeader	HEADER_SECTION_SERVICES					= new PropertyHeader(SECTION_SERVICES);

	public final static PropertyHeader	HEADER_SECTION_EVENTING					= new PropertyHeader(SECTION_EVENTING);

	public final static PropertyHeader	HEADER_SECTION_DPWS						= new PropertyHeader(SECTION_DPWS);

	public final static PropertyHeader	HEADER_SECTION_HTTP						= new PropertyHeader(SECTION_HTTP);

	public final static PropertyHeader	HEADER_SECTION_IP						= new PropertyHeader(SECTION_IP);

	public final static PropertyHeader	HEADER_SECTION_SECURITY					= new PropertyHeader(SECTION_SECURITY);

	// DEVICE SUBSECTIONS
	public static final PropertyHeader	HEADER_SUBSECTION_DEVICE				= new PropertyHeader("Device", SECTION_DEVICES);

	// SERVICE SUBSECTIONS
	public static final PropertyHeader	HEADER_SUBSECTION_SERVICE				= new PropertyHeader("Service", SECTION_SERVICES);

	// EVENT SINK SUBSECTIONS
	public static final PropertyHeader	HEADER_SUBSECTION_EVENT_SINK			= new PropertyHeader("EventSink", SECTION_EVENTING);

	// GLOBAL SUBSECTIONS
	public static final PropertyHeader	HEADER_SUBSECTION_DISPATCHING			= new PropertyHeader("Dispatching", SECTION_GLOBAL);

	public static final PropertyHeader	HEADER_SUBSECTION_LOGGING				= new PropertyHeader("Logging", SECTION_GLOBAL);

	public static final PropertyHeader	HEADER_SUBSECTION_FRAMEWORK				= new PropertyHeader("Framework", SECTION_GLOBAL);

	public static final PropertyHeader	HEADER_SUBSECTION_ATTACHMENT			= new PropertyHeader("Attachments", SECTION_GLOBAL);

	public static final int				MAX_SECTION_DEPTH						= 8;

	static final String					DEVICES_PROPERTIES_HANDLER_CLASS		= "org.ws4d.java.configuration.DevicesPropertiesHandler";

	static final String					SERVICES_PROPERTIES_HANDLER_CLASS		= "org.ws4d.java.configuration.ServicesPropertiesHandler";

	static final String					BINDING_PROPERTIES_HANDLER_CLASS		= "org.ws4d.java.configuration.BindingProperties";

	static final String					EVENTING_PROPERTIES_HANDLER_CLASS		= "org.ws4d.java.configuration.EventingProperties";

	static final String					ATTACHMENT_PROPERTIES_HANDLER_CLASS		= "org.ws4d.java.configuration.AttachmentProperties";

	static final String					GLOBAL_PROPERTIES_HANDLER_CLASS			= "org.ws4d.java.configuration.GlobalPropertiesHandler";

	static final String					DISPATCHING_PROPERTIES_HANDLER_CLASS	= "org.ws4d.java.configuration.DispatchingProperties";

	static final String					FRAMEWORK_PROPERTIES_HANDLER_CLASS		= "org.ws4d.java.configuration.FrameworkProperties";

	static final String					SECURITY_PROPERTIES_HANDLER_CLASS		= "org.ws4d.java.configuration.SecurityProperties";

	// classes for the DPWS communication manager

	public static final String			DPWS_PROPERTIES_HANDLER_CLASS			= "org.ws4d.java.configuration.DPWSProperties";

	public static final String			HTTP_PROPERTIES_HANDLER_CLASS			= "org.ws4d.java.configuration.HTTPProperties";

	public static final String			IP_PROPERTIES_HANDLER_CLASS				= "org.ws4d.java.configuration.IPProperties";

	static final HashMap				ownHandlers								= new HashMap();

	// key = qualified class name as String, value = PropertiesHandler instance
	private static HashMap				handlersPerClass						= null;

	// --------- VAR --------------

	private HashMap						loadedhandlerMap						= new HashMap();

	/** Map<PropertyHeader, PropertiesHandler */
	private HashMap						handlerMap								= new HashMap();

	private static Properties			properties								= null;

	public BufferedInputStream			stream									= null;

	public static synchronized PropertiesHandler forClassName(String className) {
		PropertiesHandler handler;
		if (handlersPerClass == null) {
			handler = createHandlerInstance(className);
			handlersPerClass = new HashMap();
			handlersPerClass.put(className, handler);
		} else {
			handler = (PropertiesHandler) handlersPerClass.get(className);
			if (handler == null) {
				handler = createHandlerInstance(className);
				handlersPerClass.put(className, handler);
			}
		}
		return handler;
	}

	/**
	 * @param className
	 * @param handler
	 * @return
	 */
	private static PropertiesHandler createHandlerInstance(String className) {
		try {
			Class handlerClass = Class.forName(className);
			return (PropertiesHandler) handlerClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unknown properties handler class: " + className);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Unable to access properties handler class " + className);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Unbale to instantiate properties handler class " + className);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Not a properties handler class: " + className);
		}
	}

	/**
	 * Constructor. No file will be used to fill properties at init.
	 * 
	 * @param stream
	 */
	private Properties() {
		/*
		 * Prepare default property handlers from the framework implementation.
		 */
		ownHandlers.put(headerString(SECTION_BINDINGS), BINDING_PROPERTIES_HANDLER_CLASS);
		ownHandlers.put(headerString(SECTION_GLOBAL), GLOBAL_PROPERTIES_HANDLER_CLASS);
		ownHandlers.put(headerString(SECTION_DEVICES), DEVICES_PROPERTIES_HANDLER_CLASS);
		ownHandlers.put(headerString(SECTION_SERVICES), SERVICES_PROPERTIES_HANDLER_CLASS);
		ownHandlers.put(headerString(SECTION_EVENTING), EVENTING_PROPERTIES_HANDLER_CLASS);
		ownHandlers.put(headerString(SECTION_DPWS), DPWS_PROPERTIES_HANDLER_CLASS);
		ownHandlers.put(headerString(SECTION_HTTP), HTTP_PROPERTIES_HANDLER_CLASS);
		ownHandlers.put(headerString(SECTION_IP), IP_PROPERTIES_HANDLER_CLASS);
		ownHandlers.put(headerString(SECTION_SECURITY), SECURITY_PROPERTIES_HANDLER_CLASS);
	}

	private String headerString(String[] s) {
		StringBuffer out = new StringBuffer(s.length * 16);
		for (int i = 0; i < s.length; i++) {
			out.append(s[i]);
			if (i < s.length - 1) {
				out.append("/");
			}
		}

		return out.toString();
	}

	/**
	 * Get the one and only instance. To use the properties, one of the init
	 * method must be called.
	 * 
	 * @return properties
	 */
	public static synchronized Properties getInstance() {
		if (properties == null) {
			properties = new Properties();
		}
		return properties;
	}

	private void initHandlers() {
		/*
		 * Register core properties
		 */
		register(HEADER_SECTION_GLOBAL, forClassName(GLOBAL_PROPERTIES_HANDLER_CLASS));
		register(HEADER_SUBSECTION_DISPATCHING, forClassName(DISPATCHING_PROPERTIES_HANDLER_CLASS));
		register(HEADER_SUBSECTION_FRAMEWORK, forClassName(FRAMEWORK_PROPERTIES_HANDLER_CLASS));
		register(HEADER_SECTION_BINDINGS, forClassName(BINDING_PROPERTIES_HANDLER_CLASS));

		/*
		 * Register device and service properties
		 */
		if (DPWSFramework.hasModule(DPWSFramework.SERVICE_MODULE)) {
			register(HEADER_SECTION_DEVICES, forClassName(DEVICES_PROPERTIES_HANDLER_CLASS));
			register(HEADER_SECTION_SERVICES, forClassName(SERVICES_PROPERTIES_HANDLER_CLASS));
		}

		/*
		 * Register eventing properties
		 */
		if (DPWSFramework.hasModule(DPWSFramework.EVENTING_MODULE)) {
			register(HEADER_SECTION_EVENTING, forClassName(EVENTING_PROPERTIES_HANDLER_CLASS));
		}

		/*
		 * Register attachment properties
		 */
		if (DPWSFramework.hasModule(DPWSFramework.ATTACHMENT_MODULE)) {
			register(HEADER_SUBSECTION_ATTACHMENT, forClassName(ATTACHMENT_PROPERTIES_HANDLER_CLASS));
		}

	}

	private void finishHandlers() {
		// copy handlers, as finishing may remove handlers from map
		HashSet handlers = new HashSet(handlerMap.size());
		for (Iterator it = handlerMap.values().iterator(); it.hasNext();) {
			handlers.add(it.next());
		}
		for (Iterator it = handlers.iterator(); it.hasNext();) {
			PropertiesHandler handler = (PropertiesHandler) it.next();
			handler.finishedSection(0);
		}
	}

	/**
	 * Initialize Properties. Default property entries will be used.
	 */
	public void init() {
		init((BufferedInputStream) null);
	}

	/**
	 * Initialize Properties. Property file will be used.
	 * 
	 * @param filename The path within the filename must be relative to the
	 *            working directory of the stack.
	 */
	public void init(String filename) {
		try {
			if (DPWSFramework.hasModule(DPWSFramework.PLATFORM_CLDC_MODULE)) {
				Log.info("Reading via getResourceAsStream() ...");
				init(getClass().getResourceAsStream(filename));
			} else {
				init(DPWSFramework.getLocalFileSystem().readFile(filename));
			}
		} catch (IOException e) {
			Log.warn("Cannot load framework properties from " + filename + ". " + e.getMessage());
		}
	}

	/**
	 * Initialize Properties. Property stream will be used.
	 * 
	 * @param stream
	 */
	public void init(InputStream stream) {
		initHandlers();
		if (stream == null) {
			if (this.stream == null) {
				// no stream to fill properties.
				finishHandlers();
				return;
			}

			stream = this.stream;
		}

		PropertiesHandler[] handlers = new PropertiesHandler[MAX_SECTION_DEPTH];
		PropertiesInputStream in = new PropertiesInputStream(stream);
		PropertiesHandler handler = null;
		PropertyHeader lastHeader;
		PropertyHeader header;
		Property property;
		Class handlerClass = null;

		header = readHeader(in);
		lastHeader = header;
		while (header != null) {

			if (header.depth() > MAX_SECTION_DEPTH) {
				// Section depth to big => ignore section
				Log.error("Properties.init: Section depth too big " + header);
				header = readHeader(in);
				break;
			}

			// Fill header with super headers
			header.initSuperHeaders(lastHeader);

			if (handler != null) {
				/*
				 * Finishing can be started with second header. Only headers
				 * with same or higher depth will be finished.
				 */
				int headerDepth = header.depth() - 1;
				for (int i = lastHeader.depth() - 1; i >= headerDepth; i--) {
					// Close finished section in handlers
					if (handlers[i] != null) {
						handlers[i].finishedSection(i + 1);
						handlers[i] = null;
					}
				}
			}

			lastHeader = header;

			try {
				property = readNextProperty(in);
				String loadClass = (String) ownHandlers.get(header.toString());

				if (property != null && PROP_HANDLER.equals(property.key)) {
					// First Property must be handler class name
					if (property.value != null) {
						handlerClass = Class.forName(property.value);

						if (handlerClass == null) {
							Log.error("Properties.init: unknown handler class " + handlerClass);
							throw new ClassNotFoundException();
						}
						if (!loadedhandlerMap.containsKey(handlerClass.getName())) {
							try {
								PropertiesHandler loadedHandler = (PropertiesHandler) handlerClass.newInstance();
								loadedhandlerMap.put(handlerClass.getName(), loadedHandler);
								handler = loadedHandler;
							} catch (Exception e) {
								Log.printStackTrace(e);
								Log.error("Properties.init: load handler exception " + handlerClass);
								throw new ClassNotFoundException();
							}
						} else {
							handler = (PropertiesHandler) loadedhandlerMap.get(handlerClass.getName());
						}

						handlers[header.depth() - 1] = handler;
					}

					// Read properties until next header
					property = readNextProperty(in);
					while (property != null) {
						handler.setProperties(header, property);
						property = readNextProperty(in);
					}
				} else if (property != null && !PROP_HANDLER.equals(property.key) && loadClass != null) {
					handlerClass = Class.forName(loadClass);

					if (handlerClass == null) {
						Log.error("Properties.init: unknown handler class " + handlerClass);
						throw new ClassNotFoundException();
					}
					if (!loadedhandlerMap.containsKey(handlerClass.getName())) {
						try {
							PropertiesHandler loadedHandler = (PropertiesHandler) handlerClass.newInstance();
							loadedhandlerMap.put(handlerClass.getName(), loadedHandler);
							handler = loadedHandler;
						} catch (Exception e) {
							Log.printStackTrace(e);
							Log.error("Properties.init: load handler exception " + handlerClass);
							throw new ClassNotFoundException();
						}
					} else {
						handler = (PropertiesHandler) loadedhandlerMap.get(handlerClass.getName());
					}

					// Read properties until next header
					property = readNextProperty(in);
					while (property != null) {
						handler.setProperties(header, property);
						property = readNextProperty(in);
					}
				} else {
					if (Log.isDebug()) {
						Log.debug("Properties.init: " + PROP_HANDLER + " is not first in section " + header + ", checking super handlers", Log.DEBUG_LAYER_FRAMEWORK);
					}
					PropertiesHandler superHandler = null;

					for (int i = header.depth() - 2; i >= 0; i--) {
						superHandler = handlers[i];
						if (superHandler != null) {

							for (int j = i + 1; j <= header.depth() - 1; j++) {
								handlers[j] = superHandler;
							}

							break;
						}
					}
					boolean useSuperHandler = superHandler != null;
					PropertiesHandler dedicatedHandler = null;
					if (useSuperHandler) {
						dedicatedHandler = (PropertiesHandler) handlerMap.get(header);
						if (dedicatedHandler != null && dedicatedHandler != handler) {
							useSuperHandler = false;
						}
					}
					if (useSuperHandler) {
						// Read properties until next header
						while (property != null) {
							handler.setProperties(header, property);
							property = readNextProperty(in);
						}
					} else {
						handler = dedicatedHandler == null ? (PropertiesHandler) handlerMap.get(header) : dedicatedHandler;
						if (handler != null) {
							handlers[header.depth() - 1] = handler;
							while (property != null) {
								handler.setProperties(header, property);
								property = readNextProperty(in);
							}
						} else {
							while (handler == null && header.depth() > 1) {
								header = header.superHeader();
								handler = (PropertiesHandler) handlerMap.get(header);
							}
							if (handler != null) {
								for (int i = header.depth() - 1; i < lastHeader.depth(); i++) {
									handlers[i] = handler;
								}
								header = lastHeader;
								while (property != null) {
									handler.setProperties(header, property);
									property = readNextProperty(in);
								}
							} else if (Log.isDebug()) {
								Log.debug("Properties.init: no super handler or registered handler for section " + header, Log.DEBUG_LAYER_FRAMEWORK);
							}
						}
					}
				}
			} catch (ClassNotFoundException e) {
				Log.error("Properties.init: Can't load handler class " + e.getMessage());
				// use the other handlers
			} catch (SectionHeaderFoundException e) {
				// Log.debug("Properties.init: New header found");
			}

			header = readHeader(in);

		}

		for (int i = lastHeader.depth() - 1; i >= 0; i--) {
			// Close finished section in handlers
			if (handlers[i] != null) {
				handlers[i].finishedSection(i + 1);
				handlers[i] = null;
			}
		}

		finishHandlers();
	}

	/**
	 * Get instance of property handler by name of class (Class.getName()).
	 * 
	 * @param className Name of class
	 * @return property handler
	 */
	public PropertiesHandler getLoadedHandler(String className) {
		return (PropertiesHandler) loadedhandlerMap.get(className);
	}

	/**
	 * Returns a DataStructure that contains instances of all loaded property
	 * handlers
	 * 
	 * @return all loaded instances of classes that implement
	 *         {@link #PropertiesHandler}
	 */
	public DataStructure getAllLoadedHandlers() {
		DataStructure values = loadedhandlerMap.values();
		return values;
	}

	// --------------------

	/**
	 * Register handler for specified header.
	 * 
	 * @param header
	 * @param handler
	 */
	void register(PropertyHeader header, PropertiesHandler handler) {
		handlerMap.put(header, handler);
		loadedhandlerMap.put(handler.getClass().getName(), handler);
	}

	/**
	 * Register handler for specified header.
	 * 
	 * @param header
	 * @param handler
	 */
	public void register(PropertyHeader header, String className) {
		register(header, forClassName(className));
	}

	/**
	 * Unregister handler.
	 * 
	 * @param header
	 * @param handler
	 */
	void unregister(PropertyHeader header) {
		PropertiesHandler handler = (PropertiesHandler) handlerMap.remove(header);
		if (handler != null) {
			loadedhandlerMap.remove(handler.getClass().getName());
		}
	}

	/**
	 * Read lines until next header.
	 * 
	 * @param in
	 * @return
	 */
	private static Property readNextProperty(PropertiesInputStream in) throws SectionHeaderFoundException {
		int c;
		String key = null;
		String value;

		c = in.read();
		// XXX Think about CR+LF
		while (c != 1) {
			if (c == '#') {
				// Handle COMMENT

				do {
					c = in.read();
				} while (c != '\n' && c != -1);

				if (c == -1) {
					// Stop to read from stream
					return null;
				}

				// Start to read next line
				c = in.read();
			} else if (c == ' ' || c == '\n') {
				// Ignore ' ' at line begin

				do {
					c = in.read();
				} while ((c == ' ' || c == '\n') && c != -1);

			} else {
				// Handle no COMMENT

				StringBuffer buf = new StringBuffer(64);
				boolean newline = false;
				do {
					switch (c) {
						case '=':
							if (key == null) {
								key = buf.toString().trim();
								buf = new StringBuffer(128);
							} else {
								// second '=', must be part of value;
								buf.append((char) c);
							}
							break;
						case '\r':
							break;
						case -1:
							if (key == null) {
								return null;
							}
							value = buf.toString().trim();
							return new Property(key, value);
						case '\n':
							if (key == null) {
								// CASE: nothing of interest read => read next
								// line
								newline = true;
								break;
							}
							value = buf.toString().trim();
							return new Property(key, value);
						case ' ':
							if (buf.length() < 1) {
								// CASE: ' ' at begin of key or value => do
								// nothing
								break;
							} else {
								buf.append((char) c);
							}
							break;
						case '[':
							if (buf.length() < 1 && key == null) {
								// CASE: '[' at the begin of line should be
								// include a header
								in.buffer = c;
								throw new SectionHeaderFoundException(null);
							}
							// '[' should be appended to buffer if
						default:
							buf.append((char) c);
					}

					c = in.read();
				} while (!newline);
			}
		}

		return null;
	}

	private static PropertyHeader readHeader(PropertiesInputStream in) {
		int depth = 0;
		PropertyHeader header = null;
		StringBuffer buf = new StringBuffer(64);

		int c = in.read();
		while (c != -1) {
			if (c == '#') {
				// Handle COMMENT

				do {
					c = in.read();
				} while (c != '\n' && c != -1);

				if (c == -1) {
					return null;
				}
			} else if (c == ' ' || c == '\n') {
				// Ignore line

				do {
					c = in.read();
				} while ((c == ' ' || c == '\n') && c != -1);

				if (c == -1) {
					// Stop to read from stream
					return null;
				}
			}

			if (c == '[') {
				do {
					switch (c) {
						case '[':
							depth++;
							break;
						case ']':
							if (header == null) {
								header = new PropertyHeader(buf.toString().trim(), depth);
							}
							depth--;
							break;
						case -1:
							return header;
						case '\r':
							// ignore
							break;
						case '\n':
							if (header != null) {
								// read until header found or eof
								return header;
							} else {
								buf = new StringBuffer(64);
							}
							break;
						default:
							if (depth > 0) {
								buf.append((char) c);
							}
					}

					if (c != -1) {
						c = in.read();
					}

				} while (c != -1);
			}
			c = in.read();
		}

		return header;
	}

	// public String toString(){
	// StringBuffer out = new StringBuffer(50*loadedhandlerMap.size());
	//
	// for( Iterator it = loadedhandlerMap.entrySet().iterator(); it.hasNext();
	// ){
	// Map.Entry entry = (Map.Entry) it.next();
	// out.append(entry.getKey() + "=" + entry.getValue() + " | ");
	// }
	//
	// return out.toString();
	// }

	// -------------------------- INNER CLASS --------------------

	private class PropertiesInputStream {

		private InputStream	in;

		private int			buffer	= -1;

		protected PropertiesInputStream(InputStream in) {
			this.in = in;
		}

		public int read() {
			if (buffer != -1) {
				int ret = buffer;
				buffer = -1;
				return ret;
			}
			try {
				int c = in.read();
				return c;
			} catch (IOException e) {
				return -1;
			}

		}
	}
}