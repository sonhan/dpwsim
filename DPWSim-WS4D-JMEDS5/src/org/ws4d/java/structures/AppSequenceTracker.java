/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.structures;

import org.ws4d.java.types.AppSequence;
import org.ws4d.java.util.Log;

public class AppSequenceTracker {

	protected static int	MAX_NUMBER_OF_SEQUENCEIDS	= 10;

	private int				oldestSequenceIdIndex		= 0;

	private long			latestInstanceId			= -1;

	private String[]		sequenceIds					= null;

	private long[]			latestMessageNumbers		= null;

	private long			latestNullMessageNumber		= -1;

	/**
	 * Constructor.
	 */
	public AppSequenceTracker() {}

	/**
	 * Constructor.
	 * 
	 * @param appSeq AppSequence to be added to the new AppSequenceTracker
	 */
	public AppSequenceTracker(AppSequence appSeq) {
		// null check, THX @Stefan Schlichting
		if (appSeq != null) {
			latestInstanceId = appSeq.getInstanceId();

			if (appSeq.getSequenceId() != null) {
				sequenceIds = new String[] { appSeq.getSequenceId() };
				latestMessageNumbers = new long[] { appSeq.getMessageNumber() };
			} else
				latestNullMessageNumber = appSeq.getMessageNumber();
		}
	}

	/**
	 * @param appSeq AppSequence to check
	 * @return <code>true</code> if appSeq is not known to be old
	 */
	public synchronized boolean checkAndUpdate(AppSequence appSeq) {
		// null check, THX @Stefan Schlichting
		if (appSeq == null) {
			if (Log.isInfo()) {
				Log.info("Missing AppSequence for checkAndUpdate.");
			}
			return true;
		}
		long instanceId = appSeq.getInstanceId();

		// instanceId < latestInstanceId
		if (instanceId < latestInstanceId) return false;

		String sequenceId = appSeq.getSequenceId();
		long messageNumber = appSeq.getMessageNumber();

		// instanceId > latestInstanceId
		if (instanceId > latestInstanceId) {
			latestInstanceId = instanceId;

			if (sequenceIds != null) {
				oldestSequenceIdIndex = 0;
				for (int i = 0; i < sequenceIds.length; i++) {
					sequenceIds[i] = null;
					latestMessageNumbers[i] = -1;
				}
			}

			if (sequenceId != null) {
				if (sequenceIds == null) {
					sequenceIds = new String[] { sequenceId };
					latestMessageNumbers = new long[] { messageNumber };
				} else {
					sequenceIds[0] = sequenceId;
					latestMessageNumbers[0] = messageNumber;
				}
				latestNullMessageNumber = -1;
			} else {
				latestNullMessageNumber = messageNumber;
			}

			return true;
		}

		// instanceId == latestInstanceId
		if (sequenceId == null) {
			if (messageNumber <= latestNullMessageNumber) return false;

			latestNullMessageNumber = messageNumber;
			return true;
		}

		// sequenceId != null
		if (sequenceIds == null) {
			sequenceIds = new String[] { sequenceId };
			latestMessageNumbers = new long[] { messageNumber };
			return true;
		}

		int i = 0;
		for (; i < sequenceIds.length; i++) {
			if (sequenceIds[i] == null) break;
			if (sequenceIds[i].equals(sequenceId)) {
				if (messageNumber <= latestMessageNumbers[i]) return false;

				latestMessageNumbers[i] = messageNumber;
				return true;
			}
		}

		// sequenceId not found
		if (i >= sequenceIds.length) {
			if (i < MAX_NUMBER_OF_SEQUENCEIDS) {
				i++;
				String[] newSequenceIds = new String[i];
				long[] newLatestMessageNumbers = new long[i];

				System.arraycopy(sequenceIds, 0, newSequenceIds, 0, sequenceIds.length);
				System.arraycopy(latestMessageNumbers, 0, newLatestMessageNumbers, 0, latestMessageNumbers.length);

				sequenceIds = newSequenceIds;
				latestMessageNumbers = newLatestMessageNumbers;
			} else {
				i = oldestSequenceIdIndex++;
				if (oldestSequenceIdIndex == sequenceIds.length) oldestSequenceIdIndex = 0;
			}
		}

		sequenceIds[i] = sequenceId;
		latestMessageNumbers[i] = messageNumber;

		return true;
	}
}
