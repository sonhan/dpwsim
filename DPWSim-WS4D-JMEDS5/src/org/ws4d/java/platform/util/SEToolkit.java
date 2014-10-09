/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.platform.util;

import java.io.PrintStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.util.Log;
import org.ws4d.java.util.Toolkit;

/**
 * 
 */
public final class SEToolkit implements Toolkit {

	private volatile boolean	shutdownAdded	= false;

	/**
	 * 
	 */
	public SEToolkit() {
		super();
		addShutdownHook();
	}

	private synchronized void addShutdownHook() {
		if (shutdownAdded) {
			return;
		}
		Thread t = new Thread() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				if (FrameworkProperties.getInstance().getKillOnShutdownHook()) {
					DPWSFramework.kill();

					/*
					 * Allow the framework to do its job for one second. After
					 * that time the framework and the JavaVM is killed.
					 */
					if (DPWSFramework.isRunning()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (Log.isDebug() && DPWSFramework.isRunning()) {
						Log.debug("Killing DPWS Framework and JavaVM");
					}
					Runtime.getRuntime().halt(0);
				} else {
					DPWSFramework.stop();
				}
			}

		};
		Runtime.getRuntime().addShutdownHook(t);
		shutdownAdded = true;
	}

	public void printStackTrace(PrintStream err, Throwable t) {
		t.printStackTrace(err);
	}

	public String[] getStackTrace(Throwable t) {
		StackTraceElement[] elements = t.getStackTrace();
		String[] result = new String[elements.length];
		for (int a = 0; a < elements.length; a++) {
			result[a] = elements[a].getClassName() + "." + elements[a].getMethodName() + " at " + elements[a].getLineNumber();
		}
		return result;
	}

}
