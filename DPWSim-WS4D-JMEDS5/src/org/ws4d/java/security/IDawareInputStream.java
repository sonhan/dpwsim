package org.ws4d.java.security;

import java.io.IOException;
import java.io.InputStream;

import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;

/**
 * This class wraps an InputStream and, after the XML document has been read, is
 * able to deliver the parts of the document that have been referenced by ID in
 * the security part of the soap header.
 */
public class IDawareInputStream extends InputStream {

	private InputStream	wrappedStream;

	private ArrayList	bufAList			= new ArrayList();

	private String[]	ids;

	private ArrayList	idArrayList			= null;

	private ArrayList[]	parts;

	private int			currentPart			= -1;

	private int			currStart			= -1;

	private int			currEnd				= -1;

	private char[]		ferengis			= { '<', '/', 'I', 'D', '=', '"', ' ', '\\', '>' };

	private char[]		romulans			= { 'R', 'e', 'f', 's', '=', '"' };

	private int			romulanCounter		= -1;

	private String		waitingForEndofPart	= null;

	private String		buf					= null;

	private int			idFinder			= -1;

	private int			lastStart			= -1;

	private boolean		isAttribute			= false;

	private boolean		idDetectionDone		= false;

	/**
	 * @param is the InputStream from witch to read.
	 * @param ids the IDs (if known) of the signed message parts. If not known
	 *            null.
	 */
	public IDawareInputStream(InputStream is, String[] ids) {
		wrappedStream = is;
		this.ids = ids;
		currentPart = -1;
		currStart = -1;
		currEnd = -1;
		romulanCounter = -1;
		idFinder = -1;
		lastStart = -1;

		parts = ids == null ? null : new ArrayList[ids.length];
		if (parts != null) idDetectionDone = true;
	}

	private void setIDs(ArrayList ids) {
		Iterator it = ids.iterator();
		this.ids = new String[ids.size()];
		for (int i = 0; it.hasNext(); i++) {
			this.ids[i] = (String) it.next();
		}
		parts = new ArrayList[ids.size()];
	}

	/**
	 * @return the name of the current element
	 */
	private String getCurrentElmentsName() {
		String element = new String();

		for (int i = lastStart + 1; true; i++) {
			if ((((Integer) bufAList.get(i)).intValue() == ferengis[6] || ((Integer) bufAList.get(i)).intValue() == ferengis[8]) && element != "") {
				return element;
			} else
				element += (char) ((Integer) bufAList.get(i)).intValue();
		}
	}

	/**
	 * @return the latest position of a startTag element.
	 */
	private int getLastStartTagPos() {
		return bufAList.lastIndexOf(new Integer(ferengis[0]));
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		// discover the referenced parts in the XML documents. If not supplied
		// discover the "Refs:" section as well. Yes, thats a little bit more
		// work.
		int b = wrappedStream.read();
		if (b == ferengis[0]) lastStart = bufAList.size();

		if (bufAList.size() > 0 && b != ferengis[1] && ((Integer) bufAList.get(bufAList.size() - 1)).intValue() == ferengis[0]) {
			isAttribute = true;
			idFinder = -1;
		} else if (waitingForEndofPart == null && isAttribute == true && b == ferengis[2] && ((Integer) bufAList.get(bufAList.size() - 1)).intValue() == ferengis[6]) {
			idFinder = 0;
			buf = new String();
		} else if (!idDetectionDone && idArrayList != null) {
			if (b == ferengis[6]) {
				idArrayList.add(new String());
			} else if (b == ferengis[5] && (char) ((Integer) bufAList.get(bufAList.size() - 1)).intValue() != ferengis[1]) {
				this.setIDs(idArrayList);
				idArrayList = null;
				idDetectionDone = true;
			} else
				idArrayList.set(idArrayList.size() - 1, ((String) idArrayList.get(idArrayList.size() - 1)).concat("" + (char) b));
		} else if (idFinder != -1 && idFinder <= 2 && b == ferengis[3 + idFinder]) {
			idFinder++;
		} else if (idFinder >= 2 && !(b == ferengis[5] && ((Integer) bufAList.get(bufAList.size() - 1)).intValue() != ferengis[7])) {
			buf += ((char) b);
		} else if (idFinder >= 2) {
			idFinder = -1;
			// is it one of the relevant IDs?
			for (int y = 0; ids != null && y < ids.length; y++) {
				if (ids[y].equals(buf)) {
					waitingForEndofPart = getCurrentElmentsName();
					parts[y] = new ArrayList();
					currentPart = y;
					currStart = getLastStartTagPos();
				}
			}
		} else if (currEnd <= -1 && currentPart > -1 && b == ferengis[1] && ((Integer) bufAList.get(bufAList.size() - 1)).intValue() == ferengis[0]) {
			currEnd = 0;
		} else if (currentPart > -1 && currEnd > -1 && waitingForEndofPart.length() - 1 < currEnd && ((Integer) bufAList.get(bufAList.size() - 1)).intValue() == ferengis[8]) {
			int i = bufAList.size();
			List l = bufAList.subList(currStart, i);
			parts[currentPart] = new ArrayList(l);

			// contain the ghosts i called
			currentPart = -1;
			currEnd = -1;
			currStart = -1;
			waitingForEndofPart = null;
		} else if (currEnd > -1 && ((waitingForEndofPart == null && currentPart > -1) || ((currentPart > -1 && waitingForEndofPart != null) && (!(currEnd > waitingForEndofPart.length() - 1) && b != waitingForEndofPart.charAt(currEnd)))))
			currEnd = -1;
		else if (currentPart > -1 && currEnd > -1) {
			currEnd++;
		} else if (waitingForEndofPart == null && !idDetectionDone && isAttribute && ids == null) {
			romulanCounter++;

			if (romulanCounter + 1 >= romulans.length) {
				idArrayList = new ArrayList();
				idArrayList.add(new String());
			} else if (romulans[romulanCounter] != b) {
				romulanCounter = -1;
			}
		}

		bufAList.add(new Integer(b));

		return b;
	}

	/**
	 * @return the byte arrays of the referred message parts. In the same order
	 *         as given as input.
	 */
	public byte[][] getPartsByteArrays() {
		if (parts == null) return null;
		byte[][] reVal = new byte[parts.length][];
		for (int i = 0; i < parts.length; i++) {
			if (parts[i] == null) return null;
			reVal[i] = new byte[parts[i].size()];
			for (int y = 0; y < parts[i].size(); y++) {
				reVal[i][y] = (byte) ((Integer) parts[i].get(y)).intValue();
			}
		}
		return reVal;
	}

	/**
	 * returns the id referenced in the security part of the header
	 * 
	 * @return
	 */
	public String[] getIds() {
		return ids;
	}

}
