/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import org.ws4d.java.DPWSFramework;

/**
 * Debug class provides logging mechanism and control over the output messages.<br />
 * Use this instead of System.out.println().<br />
 * <br />
 * To change the debug level and the message output, use setDebugLevel(int
 * level).<br />
 * <br />
 * e.g.<br />
 * <code>setDebugLevel(Debug.DEBUG_LEVEL_WARNING);</code><br />
 * <br />
 * DEBUG_LEVEL_DEBUG = highest output level, every message shown.<br />
 * DEBUG_LEVEL_INFO = show information, log, warn and error messages only.<br />
 * DEBUG_LEVEL_LOG = show log, warn and error messages only.<br />
 * DEBUG_LEVEL_WARN = show warn and error messages only.<br />
 * DEBUG_LEVEL_ERROR = show error messages only.<br />
 * DEBUG_LEVEL_NO_LOGGING = show nothing.<br />
 */
public final class Log {

	/**
	 * Should be used to show all debug messages.
	 */
	public static final int		DEBUG_LAYER_ALL				= Integer.MAX_VALUE;

	/**
	 * Should be used to show general framework debug messages.
	 */
	public static final int		DEBUG_LAYER_FRAMEWORK		= 4;

	/**
	 * Should be used to show application layer (device+service) debug messages
	 */
	public static final int		DEBUG_LAYER_APPLICATION		= 2;

	/**
	 * Should be used to show communication layer (DPWS) debug messages.
	 */
	public static final int		DEBUG_LAYER_COMMUNICATION	= 1;

	/** Sets highest output level, every message shown. */
	public static final int		DEBUG_LEVEL_DEBUG			= 4;

	/** Sets level to show information, warn and error messages only. */
	public static final int		DEBUG_LEVEL_INFO			= 3;

	/** Sets level to show warn and error messages only. */
	public static final int		DEBUG_LEVEL_WARN			= 2;

	/** Sets level to show error messages only. */
	public static final int		DEBUG_LEVEL_ERROR			= 1;

	/** Sets level to show error messages only. */
	public static final int		DEBUG_LEVEL_NO_LOGGING		= 0;

	/** Prefix setup * */
	private static final String	PREFIX_DEBUG				= "DEBUG";

	private static final String	PREFIX_INFO					= "INFO ";

	private static final String	PREFIX_WARN					= "WARN ";

	private static final String	PREFIX_ERROR				= "ERROR";

	/** Disables/Enables time stamp. */
	private static boolean		showTimestamp				= false;

	/** Default debug level for this object. */
	private static int			logLevel					= DEBUG_LEVEL_DEBUG;

	private static int			defaultLayers				= DEBUG_LAYER_ALL;

	private static int			activeLayers				= defaultLayers;

	/** Notification list for debug subscribers. */
	private static Vector		subscribers					= new Vector();

	private static PrintStream	out							= System.out;

	private static PrintStream	err							= System.err;

	private static boolean		logStackTrace				= true;

	/**
	 * Sets time stamp on or off.
	 * 
	 * @param set <code>true</code> if Debug should show time stamps,
	 *            <code>false</code> if not.
	 */
	public static void setShowTimestamp(boolean set) {
		showTimestamp = set;
	}

	/**
	 * Sets logging of stack traces on or off.
	 * 
	 * @param logStackTrace <code>true</code> if Debug should log stack traces,
	 *            <code>false</code> if not.
	 */
	public static void setLogStackTrace(boolean logStackTrace) {
		Log.logStackTrace = logStackTrace;
	}

	/**
	 * Sets the internal debug level for this logger.
	 * 
	 * @param level the debug level to set.
	 */
	public static void setLogLevel(int level) {
		setLogLevel(level, defaultLayers);
	}

	/**
	 * Sets the internal debug level for this logger.
	 * <p>
	 * The set layers could be a single layer or a combination of them
	 * </p>
	 * 
	 * @param level the debug level to set.
	 * @param layers the layers which debug messages should be shown for.
	 * @see #DEBUG_LAYER_ALL
	 * @see #DEBUG_LAYER_APPLICATION
	 * @see #DEBUG_LAYER_COMMUNICATION
	 * @see #DEBUG_LAYER_FRAMEWORK
	 */
	public static void setLogLevel(int level, int layers) {
		logLevel = level;
		activeLayers = layers;
	}

	/**
	 * Gets the internal debug level for this logger.
	 * 
	 * @return the debug level.
	 */
	public static int getLogLevel() {
		return logLevel;
	}

	/**
	 * Checks whether the current log level is at least
	 * {@link #DEBUG_LEVEL_DEBUG}.
	 * 
	 * @return whether the current log level is at least
	 *         {@link #DEBUG_LEVEL_DEBUG} or not
	 */
	public static boolean isDebug() {
		return logLevel >= DEBUG_LEVEL_DEBUG;
	}

	/**
	 * Checks whether the current log level is at least
	 * {@link #DEBUG_LEVEL_INFO}.
	 * 
	 * @return whether the current log level is at least
	 *         {@link #DEBUG_LEVEL_INFO} or not
	 */
	public static boolean isInfo() {
		return logLevel >= DEBUG_LEVEL_INFO;
	}

	/**
	 * Checks whether the current log level is at least
	 * {@link #DEBUG_LEVEL_WARN}.
	 * 
	 * @return whether the current log level is at least
	 *         {@link #DEBUG_LEVEL_WARN} or not
	 */
	public static boolean isWarn() {
		return logLevel >= DEBUG_LEVEL_WARN;
	}

	/**
	 * Checks whether the current log level is at least
	 * {@link #DEBUG_LEVEL_ERROR}.
	 * 
	 * @return whether the current log level is at least
	 *         {@link #DEBUG_LEVEL_ERROR} or not
	 */
	public static boolean isError() {
		return logLevel >= DEBUG_LEVEL_ERROR;
	}

	/**
	 * Generates time stamp (hour:minute:second.millisecond).
	 * 
	 * @return time stamp generated time stamp.
	 */
	protected static String showTimestamp() {
		if (!showTimestamp) return "";

		Calendar cal = Calendar.getInstance();
		int tmp = cal.get(Calendar.HOUR_OF_DAY);
		String h = (tmp < 10 ? "0" : "") + String.valueOf(tmp);
		tmp = cal.get(Calendar.MINUTE);
		String m = (tmp < 10 ? "0" : "") + String.valueOf(tmp);
		tmp = cal.get(Calendar.SECOND);
		String s = (tmp < 10 ? "0" : "") + String.valueOf(tmp);
		tmp = cal.get(Calendar.MILLISECOND);
		String ms = (tmp < 10 ? "00" : (tmp < 100 ? "0" : "")) + String.valueOf(tmp);

		return "|" + h + ":" + m + ":" + s + "." + ms;
	}

	/**
	 * Logs a debug message.<br />
	 * Use this for debugging messages.<br />
	 * The message is logged only when the current debug level is >=
	 * DEBUG_LEVEL_DEBUG.
	 * 
	 * @param msg the message to log.
	 */
	public static void debug(String msg) {
		debug(msg, DEBUG_LAYER_ALL);
	}

	/**
	 * Logs a debug message.<br />
	 * Use this for debugging messages.<br />
	 * The message is logged only when the current debug level is >=
	 * DEBUG_LEVEL_DEBUG.
	 * 
	 * @param msg the message to log.
	 */
	public static void debug(String msg, int layer) {
		if ((layer & activeLayers) == activeLayers || (layer & activeLayers) == layer) {
			msgout(DEBUG_LEVEL_DEBUG, "[" + PREFIX_DEBUG + showTimestamp() + "] " + msg);
		}
	}

	/**
	 * Logs an info message.<br />
	 * Use this for informational messages.<br />
	 * The message is logged only when the current debug level is >=
	 * DEBUG_LEVEL_INFO.
	 * 
	 * @param msg the message to log.
	 */
	public static void info(String msg) {
		msgout(DEBUG_LEVEL_INFO, "[" + PREFIX_INFO + showTimestamp() + "] " + msg);
	}

	/**
	 * Logs an error message.<br />
	 * Use this for messages regarding critical errors.<br />
	 * The message is logged only when the current debug level is >=
	 * DEBUG_LEVEL_ERROR.
	 * 
	 * @param msg the message to log.
	 */
	public static void error(String msg) {
		msgout(DEBUG_LEVEL_ERROR, "[" + PREFIX_ERROR + showTimestamp() + "] " + msg);
	}

	/**
	 * Logs a warning message.<br />
	 * Use this for messages about malfunctions which are not so severe to be
	 * treated as errors.<br />
	 * The message is logged only when the current debug level is >=
	 * DEBUG_LEVEL_WARN.
	 * 
	 * @param msg the message to log.
	 */
	public static void warn(String msg) {
		msgout(DEBUG_LEVEL_WARN, "[" + PREFIX_WARN + showTimestamp() + "] " + msg);
	}

	/**
	 * Prints messages on System.out depending on debug level.
	 * 
	 * @param level in the range from 1..5, or use one of the predefined
	 *            constants.
	 * @param msg the message to log.
	 */
	protected static void msgout(int level, String msg) {
		if (level <= logLevel) {
			if (level == DEBUG_LEVEL_ERROR || level == DEBUG_LEVEL_WARN) {
				err.println(msg);
				err.flush();
			} else {
				out.println(msg);
				out.flush();
			}
			notifySubscribers(msg);
		}
	}

	/**
	 * Prints stack trace, if debug level is "Error" or higher and logging of
	 * stack traces is on. Will be printed to error output if possible. Within
	 * CLDC environment log will be printed to System.err.
	 * 
	 * @param t the throwable.
	 */
	public static void printStackTrace(Throwable t) {
		if (logLevel != DEBUG_LEVEL_NO_LOGGING && logStackTrace) {
			Toolkit toolkit = DPWSFramework.getToolkit();
			if (toolkit != null) {
				toolkit.printStackTrace(err, t);
			} else {
				// fall back to common interface
				t.printStackTrace();
			}
		}
	}

	/**
	 * Returns the Java VM stack trace if possible.
	 * <p>
	 * Can return <code>null</code> if the platform does not support access to
	 * the stack trace!
	 * </p>
	 * 
	 * @param t stack trace
	 * @return stack trace as array of <code>String</code>.
	 */
	public static String[] getStackTrace(Throwable t) {
		Toolkit toolkit = DPWSFramework.getToolkit();
		if (toolkit != null) {
			return toolkit.getStackTrace(t);
		} else {
			// fall back to common interface
			return null;
		}
	}

	/**
	 * Notifies all subscribers.
	 * 
	 * @param message the debug message.
	 */
	private static void notifySubscribers(String message) {
		for (Enumeration subEnum = subscribers.elements(); subEnum.hasMoreElements();) {
			((LogSubscriber) subEnum.nextElement()).notify(message);
		}
	}

	/**
	 * Subscribes to notification list for receiving debug messages. This is
	 * especially useful when you want to stay informed about new debug messages
	 * on devices without a console output (like CLDC/MIDP devices).
	 * 
	 * @param ds an object wants to subscribe for debug messages.
	 * @see org.ws4d.java.util.LogSubscriber
	 */
	public static void subscribe(LogSubscriber ds) {
		subscribers.addElement(ds);
	}

	/**
	 * Unsubscribes from notification list.
	 * 
	 * @param ds an object wants to unsubscribe form debug messages
	 * @see org.ws4d.java.util.LogSubscriber
	 */
	public static void unsubscribe(LogSubscriber ds) {
		subscribers.removeElement(ds);
	}

	public static void setNormalOutput(OutputStream newout) {
		out = new PrintStream(newout);
	}

	public static void setErrorOutput(OutputStream newout) {
		err = new PrintStream(newout);
	}
}
