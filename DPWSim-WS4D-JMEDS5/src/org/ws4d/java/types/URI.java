/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.types;

import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.LinkedList;
import org.ws4d.java.util.StringUtil;

/**
 * This class implements the Uniform Resource Identifier RFC3986. All
 * constructors which use strings a representation of URIs or paths expects
 * encoded strings, if any not allowed characters - specified by the RFC3986 -
 * must be part of this Strings.
 */
public class URI {

	public static final String		URN_SCHEMA_PREFIX	= "urn";

	public static final URI			EMPTY_URI			= new URI("", "", "", "", "", 0);

	private static final String[][]	AVAILABLE_PROTOCOLS	= { { "http", "80" }, { "https", "443" }, { "ftp", "21" } };

	// RFC based stuff
	private String					schema				= null;

	private String					authority			= null;

	private String					path				= null;

	private String					query				= null;

	private String					fragment			= null;

	private String					schemaDecoded		= null;

	private String					pathDecoded			= null;

	private String					queryDecoded		= null;

	private String					fragmentDecoded		= null;

	// ------------ authority parts, lazy initialized, always decoded ---------

	private String					user				= null;

	private String					password			= null;

	private String					host				= null;

	private int						port				= -1;

	// ----------------------- class based stuff-------------------------------

	/** lazy initialized */
	private int						hash				= 0;

	/** false if lazy initialization was done */
	private boolean					notInitialized		= true;

	private boolean					valid				= true;

	private boolean					isIPv6				= false;

	/** <code>true</code>, if URI is relative reference */
	private boolean					relative			= false;

	private int						pathdeepness		= -1;

	private int						protocolNum			= -1;

	private int						defaultPort			= -1;

	/*
	 * General Delimiters
	 */
	private static final char		GD_COLON			= ':';

	private static final char		GD_SLASH			= '/';

	private static final char		GD_QMARK			= '?';

	private static final char		GD_HASH				= '#';

	private static final char		GD_SQ_BR_OPEN		= '[';

	private static final char		GD_SQ_BR_CLOSE		= ']';

	private static final char		GD_AT				= '@';

	// // reserved characters like described in URI(RFC3986) part 2.2
	// private static final char[] gendelim = { ':', '/', '?', '#', '[', ']',
	// '@' };
	//
	// private static final char[] subdelim = { '!', '$', '&', '\'', '(', ')',
	// '*', '+', ',', ';', '=' };
	//
	// private static char[][] reservedChar = { gendelim, subdelim };

	/**
	 * Constructor. Creates an URI from <code>String</code>.
	 * 
	 * @param uri The String representation of an URI. If the URI string must
	 *            contain any not allowed characters, specified by the RFC3986,
	 *            this characters have to be percent encoded.
	 */
	public URI(String uri) {
		int currentIndex = 0;
		int endIndex;
		int idxSlash;

		int idxQMark = uri.indexOf(GD_QMARK, currentIndex);
		if (idxQMark != -1) {
			/*
			 * Query
			 */
			endIndex = idxQMark;
			currentIndex = idxQMark + 1;

			int idxHash = uri.indexOf(GD_HASH, currentIndex + 1);
			if (idxHash != -1) {
				query = uri.substring(currentIndex, idxHash);
				currentIndex = idxHash + 1;

				if (uri.length() > currentIndex) {
					fragment = uri.substring(currentIndex);
				} else {
					fragment = "";
				}
			} // if( idxHash != - 1 )
			else {
				query = uri.substring(currentIndex);
			}
		} // if( idxQMark != -1 ){
		else {
			/*
			 * no Query
			 */
			int idxHash = uri.indexOf(GD_HASH, currentIndex);
			if (idxHash != -1) {
				endIndex = idxHash;
				currentIndex = idxHash + 1;

				if (uri.length() > currentIndex) {
					fragment = uri.substring(currentIndex);
				} else {
					fragment = "";
				}
			} // if( idxHash != - 1 ){
			else {
				endIndex = uri.length();
			}
		}

		uri = uri.substring(0, endIndex);

		/*
		 * Schema
		 */

		idxSlash = uri.indexOf(GD_SLASH);
		int idxColon = uri.indexOf(GD_COLON);

		if (idxColon != -1 && (idxSlash == -1 || idxColon < idxSlash)) {
			/*
			 * <scheme>://<authority> or <scheme>:<path>
			 */
			schema = uri.substring(0, idxColon);
			currentIndex = idxColon + 1;
		} else {
			relative = true;
			currentIndex = 0;
		}

		if (uri.length() == currentIndex) {
			return;
		}

		if (idxSlash == currentIndex) {
			/*
			 * CASE: begins with slash
			 */
			if (uri.length() == currentIndex + 1) {
				path = String.valueOf(GD_SLASH);
				return;
			}

			if (uri.charAt(currentIndex + 1) == GD_SLASH) {
				/*
				 * CASE: begins with "//" (authority)
				 */
				idxSlash = uri.indexOf(GD_SLASH, currentIndex + 2);
				if (idxSlash != -1) {
					authority = uri.substring(currentIndex + 2, idxSlash);
					path = uri.substring(idxSlash);
				} // if( idxSlash != -1 ){
				else {
					/*
					 * CASE: no path
					 */
					authority = uri.substring(currentIndex + 2);
				}

			}// if( uri.charAt(currentIndex+1) == GD_SLASH )
			else {
				/*
				 * CASE: no authority
				 */
				path = uri.substring(currentIndex);
			}
		} else if (uri.length() > 0) {
			/*
			 * CASE: no slash => no authority;
			 */
			path = uri.substring(currentIndex);
		}
		if (path == null) {
			// XXX Added
			if (authority == null) {
				path = "";
			} else {
				/*
				 * Normalize non relative paths, as those paths need not to be
				 * check about emptiness.
				 */
				path = String.valueOf(GD_SLASH);
			}
		}

		if (!relative) {
			removeDotsFromPath();
		}

	}

	/**
	 * Constructor. Merges a base URI together with another URI (URI reference).
	 * Implementation based on <a
	 * href="http://www.apps.ietf.org/rfc/rfc3986.html#sec-5.2.2">Chapter 5.2.2
	 * Transform References</a> of the IETF RFC 3986.
	 * 
	 * @param baseURI Base URI, must be a non relative URI, fragment will be
	 *            ignored.
	 * @param uri
	 */
	public URI(URI baseURI, URI uri) {
		if (baseURI.isRelativeReference()) {
			throw new RuntimeException("Error while merging: " + "Base URI is a relative reference. " + baseURI.toString());
		}

		boolean hasSchema = false;

		if (uri.schema != null && !uri.schema.equals(baseURI.schema)) {
			hasSchema = true;
		}

		if (hasSchema) {
			/*
			 * schema from uri
			 */
			setValues(uri.schema, uri.authority, uri.path, uri.query, uri.fragment, -1);
		} else {
			/*
			 * schema from base uri
			 */
			if (uri.authority != null) {
				/*
				 * authority from uri
				 */
				setValues(baseURI.schema, uri.authority, uri.path, uri.query, uri.fragment, -1);
			} else {
				/*
				 * authority from base uri
				 */
				if (uri.path.length() > 0) {
					if (uri.path.charAt(0) == GD_SLASH) {
						/*
						 * starts with '/'
						 */
						setValues(baseURI.schema, baseURI.authority, uri.path, uri.query, uri.fragment, -1);
					} else {
						String targetPath = mergePaths(baseURI.path, uri.path);
						setValues(baseURI.schema, baseURI.authority, targetPath, uri.query, uri.fragment, -1);
					}
				} else {
					/*
					 * authority from uri.
					 */
					if (uri.query != null) {
						setValues(baseURI.schema, baseURI.authority, baseURI.path, uri.query, uri.fragment, baseURI.pathdeepness);
					} else {
						setValues(baseURI.schema, baseURI.authority, baseURI.path, baseURI.query, uri.fragment, baseURI.pathdeepness);
					}
				}
			}
		}
	}

	/**
	 * Creates an URI from a base URI and a path as which is regarded to be
	 * relative to the given base URI.
	 * <p>
	 * This method allows to change the path of the URI. For instance: <a
	 * href="http://example.org/testing">http://example.org/testing</a> into <a
	 * href="http://example.org/stable">http://example.org/stable</a>
	 * </p>
	 * 
	 * @param baseURI URI to be the base of the newly created URI.
	 * @param path A path relative to the baseURI. If the path string must
	 *            contain any not allowed characters, specified by the RFC3986,
	 *            this characters have to be percent encoded.
	 */
	public URI(URI baseURI, String path) {
		this(baseURI, new URI(null, null, path, null, null, -1));
	}

	/**
	 * Creates an URI from <code>String</code>.
	 * 
	 * @param absoluteURI the String representation of an URI.
	 * @param baseURI if baseURI is set the absoluteURI is handled as relative
	 *            URI in relation to the baseURI.
	 */
	public URI(String absoluteURI, URI baseURI) {
		this(baseURI, new URI(absoluteURI));
	}

	/**
	 * Creates an URI from a base URI as <code>String</code> and a path as
	 * <code>String</code> which is regarded to be relative to the given base
	 * URI.
	 * 
	 * @param baseURI the String representation of an absolute base URI
	 * @param path A path relative to the baseURI. No path with reserved
	 *            characters allowed, those characters must be encoded.
	 */
	public URI(String baseURI, String path) {
		this(new URI(baseURI), path);
	}

	/**
	 * Constructor. Copies given uri fields to new uri.
	 * 
	 * @param uri
	 */
	protected URI(URI uri) {

		schema = uri.schema;
		authority = uri.authority;
		path = uri.path;
		query = uri.query;
		fragment = uri.fragment;

		host = uri.host;
		port = uri.port;
		user = uri.user;
		password = uri.password;

		hash = uri.hash;
		pathdeepness = uri.pathdeepness;
		protocolNum = uri.protocolNum;
		defaultPort = uri.defaultPort;
		relative = uri.relative;
		valid = uri.valid;
		notInitialized = uri.notInitialized;
		isIPv6 = uri.isIPv6;
	}

	/**
	 * Constructor only used to handle merging URIs with a simple path.
	 * 
	 * @param schema
	 * @param authority
	 * @param path
	 * @param query
	 * @param fragment
	 * @param pathDeepness
	 */
	private URI(String schema, String authority, String path, String query, String fragment, int pathDeepness) {
		setValues(schema, authority, path, query, fragment, pathDeepness);
	}

	/**
	 * Lazy initialization of authority elements and pathdeepness.
	 */
	private void initLazy() {
		if (authority != null) {
			String userinfo = null;
			String hostinfo = null;

			int idxAt = authority.indexOf(GD_AT);
			if (idxAt != -1) {

				userinfo = authority.substring(0, idxAt);
				hostinfo = authority.substring(idxAt + 1, authority.length());

				int idxColon = userinfo.indexOf(GD_COLON);
				if (idxColon != -1) {
					user = StringUtil.decodeURL(userinfo.substring(0, idxColon));
					password = StringUtil.decodeURL(userinfo.substring(idxColon + 1, userinfo.length()));
				} else {
					user = StringUtil.decodeURL(userinfo);
				}
			} else {
				hostinfo = authority;
			}

			/*
			 * IPv6 address check.
			 */
			int bracket_begin = hostinfo.substring(0, hostinfo.length()).lastIndexOf(GD_SQ_BR_OPEN);
			int bracket_end = hostinfo.substring(0, hostinfo.length()).lastIndexOf(GD_SQ_BR_CLOSE);

			if (((bracket_begin == -1) != (bracket_end == -1)) || bracket_end < bracket_begin) {
				valid = false;
				notInitialized = false;
				return;
			}

			if (bracket_begin != -1) {
				// IPv6
				isIPv6 = true;

				int idxPercent = hostinfo.indexOf('%', bracket_begin + 3);
				if (idxPercent == -1) {
					host = hostinfo.substring(bracket_begin, bracket_end + 1);
				} else {
					host = hostinfo.substring(bracket_begin, idxPercent).toLowerCase() + hostinfo.substring(idxPercent, bracket_end + 1);
				}

				if (hostinfo.length() > bracket_end + 2 && hostinfo.charAt(bracket_end + 1) == GD_COLON) {
					try {
						port = Integer.parseInt(StringUtil.decodeURL(hostinfo.substring(bracket_end + 2)));
					} catch (NumberFormatException e) {
						valid = false;
						notInitialized = false;
						return;
					}
				} else if (hostinfo.length() > bracket_end + 1) {
					valid = false;
					notInitialized = false;
					return;
				}
			} else {
				// IPv4
				int idxPort = hostinfo.indexOf(GD_COLON);
				if (idxPort != -1) {
					host = StringUtil.decodeURL(hostinfo.substring(0, idxPort)).toLowerCase();
					if (hostinfo.length() > idxPort + 1) {
						try {
							port = Integer.parseInt(StringUtil.decodeURL(hostinfo.substring(idxPort + 1)));
						} catch (NumberFormatException e) {
							valid = false;
							notInitialized = false;
							return;
						}
					}
				} else {
					host = StringUtil.decodeURL(hostinfo).toLowerCase();
				}
			}

			if (schema != null) {
				initSchemaInternal(schema);
				if (port == -1) {
					port = defaultPort;
				}
			}

			if (host != null && host.length() == 0) {
				valid = false;
				notInitialized = false;
				return;
			}
		}

		notInitialized = false;
	}

	/**
	 * Sets values of this URI, used by constructor. Checking of allowed path is
	 * not done by this method.
	 * 
	 * @param schema
	 * @param authority
	 * @param path
	 * @param query
	 * @param fragment
	 * @param pathDeepness
	 */
	private void setValues(String schema, String authority, String path, String query, String fragment, int pathDeepness) {
		this.schema = schema;
		this.authority = authority;

		this.path = (path == null ? "" : path);

		this.query = query;
		this.fragment = fragment;

		if (schema == null) {
			relative = true;
		} else if (pathDeepness != -1) {
			this.pathdeepness = pathDeepness;
		} else {
			removeDotsFromPath();
		}
	}

	/**
	 * Checks schema identifier for default port.
	 * 
	 * @param schemaIdentifier the identifier (like HTTP, HTTPS, FTP, etc.)
	 */
	private void initSchemaInternal(String schemaIdentifier) {
		for (int i = 0; i < AVAILABLE_PROTOCOLS.length; i++) {
			if ((AVAILABLE_PROTOCOLS[i][0] != null) && StringUtil.equalsIgnoreCase((AVAILABLE_PROTOCOLS[i][0]), schemaIdentifier)) {
				protocolNum = i;
				if (AVAILABLE_PROTOCOLS[i][1] != null) {
					defaultPort = Integer.parseInt(AVAILABLE_PROTOCOLS[i][1]);
				}
				return;
			}
		}
	}

	/**
	 * Removes the "." and ".." locations from path according to URI(RFC3986)
	 * part 5.2.4.
	 * 
	 * @param path path to remove the dots from.
	 * @return the path without the dots.
	 */
	private void removeDotsFromPath() {
		String inputBuffer = path;
		LinkedList outputBuffer = new LinkedList();

		while (inputBuffer.length() > 0) {
			int indexPoint = inputBuffer.indexOf('.');
			int indexSlash = inputBuffer.indexOf('/');

			if (indexPoint == 0) {
				if (indexSlash == -1) {
					if (!(inputBuffer.length() == 1 || (inputBuffer.length() == 2 && inputBuffer.charAt(1) == '.'))) {
						/*
						 * E move the first path segment in the input buffer to
						 * the end of the output buffer, including the initial
						 * "/" character (if any) and any subsequent characters
						 * up to, but not including, the next "/" character or
						 * the end of the input buffer.
						 */
						outputBuffer.add(inputBuffer);
					}

					/*
					 * ELSE D: D if the input buffer consists only of "." or
					 * "..", then remove that from the input buffer; otherwise,
					 */
					break;
				} else {
					if (indexSlash == 1 || (indexSlash == 2 && inputBuffer.charAt(1) == '.')) {
						/*
						 * A If the input buffer begins with a prefix of "../"
						 * or "./", then remove that prefix from the input
						 * buffer; otherwise,
						 */
						inputBuffer = inputBuffer.substring(indexSlash);
						continue;
					}
				}
			} // if( indexPoint == 0 )
			else if (indexSlash == 0) {
				if (indexPoint == 1) {
					if (inputBuffer.length() == 2) {
						/*
						 * B if the input buffer begins with a prefix of "/./"
						 * or "/.", where "." is a complete path segment, then
						 * replace that prefix with "/" in the input buffer;
						 * otherwise, && E move the first path segment in the
						 * input buffer to the end of the output buffer,
						 * including the initial "/" character (if any) and any
						 * subsequent characters up to, but not including, the
						 * next "/" character or the end of the input buffer.
						 */
						outputBuffer.add("/");
						break;
					}
					char c = inputBuffer.charAt(2);
					if (c == '/') {
						/*
						 * B if the input buffer begins with a prefix of "/./"
						 * or "/.", where "." is a complete path segment, then
						 * replace that prefix with "/" in the input buffer;
						 * otherwise,
						 */
						inputBuffer = inputBuffer.substring(2);
						continue;
					} else if (c == '.') {
						if (inputBuffer.length() == 3) {
							/*
							 * C if the input buffer begins with a prefix of
							 * "/../" or "/..", where ".." is a complete path
							 * segment, then replace that prefix with "/" in the
							 * input buffer and remove the last segment and its
							 * preceding "/" (if any) from the output buffer;
							 * otherwise, && E move the first path segment in
							 * the input buffer to the end of the output buffer,
							 * including the initial "/" character (if any) and
							 * any subsequent characters up to, but not
							 * including, the next "/" character or the end of
							 * the input buffer.
							 */
							if (outputBuffer.size() > 0) {
								outputBuffer.removeLast();
							}
							outputBuffer.add("/");
							break;
						} else if (inputBuffer.charAt(3) == '/') {
							/*
							 * C if the input buffer begins with a prefix of
							 * "/../" or "/..", where ".." is a complete path
							 * segment, then replace that prefix with "/" in the
							 * input buffer and remove the last segment and its
							 * preceding "/" (if any) from the output buffer;
							 * otherwise,
							 */
							if (outputBuffer.size() > 0) {
								outputBuffer.removeLast();
							}
							inputBuffer = inputBuffer.substring(3);
							continue;
						}
					}

				}
			}

			/*
			 * E move the first path segment in the input buffer to the end of
			 * the output buffer, including the initial "/" character (if any)
			 * and any subsequent characters up to, but not including, the next
			 * "/" character or the end of the input buffer.
			 */
			if (indexSlash == -1) {
				outputBuffer.add(inputBuffer);
				break;
			}
			if (indexSlash == 0) {
				indexSlash = inputBuffer.indexOf('/', 1);
				if (indexSlash == -1) {
					outputBuffer.add(inputBuffer);
					break;
				}
			}
			outputBuffer.add(inputBuffer.substring(0, indexSlash));
			inputBuffer = inputBuffer.substring(indexSlash);
		}

		pathdeepness = outputBuffer.size();
		StringBuffer result = new StringBuffer(inputBuffer.length());

		for (Iterator it = outputBuffer.iterator(); it.hasNext();) {
			result.append((String) it.next());
		}

		path = result.toString();
	}

	/**
	 * Merges 2 URI's paths. Second path must be relative. Implementation based
	 * on Chapter 5.2.3 of IETF RFC 3986.
	 * 
	 * @param basePath
	 * @param relativePath Relative path with no leading '/'.
	 * @return Merged path of the 2 URIs.
	 */
	private static String mergePaths(String basePath, String relativePath) {
		int idxSlash = basePath.lastIndexOf(GD_SLASH);
		if (idxSlash == -1) {
			return relativePath;
		}

		return basePath.substring(0, idxSlash + 1) + relativePath;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(20);
		if (schema != null) {
			buffer.append(schema);
			buffer.append(GD_COLON);
		}
		if (authority != null) {
			buffer.append("//");
			buffer.append(authority);
		}
		if (path != null) {
			buffer.append(path);
		}
		if (query != null) {
			buffer.append(GD_QMARK);
			buffer.append(query);
		}
		if (fragment != null) {
			buffer.append(GD_HASH);
			buffer.append(fragment);
		}
		return buffer.toString();
	}

	/**
	 * Creates a clone of this URI.
	 * 
	 * @return the clone of this URI.
	 */
	public Object clone() {
		return new URI(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (hash != 0) {
			return hash;
		}

		if (notInitialized) {
			initLazy();
		}

		final int prime = 31;
		int result = 1;
		result = prime * result + ((schema == null) ? 0 : getSchema().hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		result = prime * result + ((path == null) ? 0 : getPath().hashCode());
		result = prime * result + ((fragment == null) ? 0 : getFragment().hashCode());
		result = prime * result + ((query == null) ? 0 : getQuery().hashCode());
		return hash = result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		final URI other = (URI) obj;

		if (notInitialized) {
			initLazy();
		}
		if (other.notInitialized) {
			other.initLazy();
		}

		if (schema == null) {
			if (other.schema != null) {
				return false;
			}
		} else if (!getSchema().equals(other.getSchema())) {
			return false;
		}

		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}

		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}

		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}

		if (port != other.port) {
			return false;
		}

		if (!getPath().equals(other.getPath())) {
			return false;
		}

		if (query == null) {
			if (other.query != null) {
				return false;
			}
		} else if (!getQuery().equals(other.getQuery())) {
			return false;
		}

		if (fragment == null) {
			if (other.fragment != null) {
				return false;
			}
		} else if (!getFragment().equals(other.getFragment())) {
			return false;
		}

		return true;
	}

	/**
	 * Case-insensitive comparison of this with the given uri.
	 * 
	 * @param other
	 * @return <code>true</code> only if this URI instance equals
	 *         <code>other</code> in terms of RFC3986 equality
	 */
	public boolean equalsWsdRfc3986(final URI other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}

		if (notInitialized) {
			initLazy();
		}
		if (other.notInitialized) {
			other.initLazy();
		}

		if (schema == null) {
			if (other.schema != null) {
				return false;
			}
		} else if (!getSchema().equals(other.getSchema())) {
			return false;
		}

		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!StringUtil.equalsIgnoreCase(user, other.user)) {
			return false;
		}

		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!StringUtil.equalsIgnoreCase(password, other.password)) {
			return false;
		}

		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}

		if (port != other.port) {
			return false;
		}

		String thisPath = getPath();
		String otherPath = other.getPath();
		int thisLength = thisPath.length();
		int otherLength = otherPath.length();
		int shortestLength = Math.min(thisLength, otherLength);

		for (int i = 0; i < shortestLength; i++) {
			char c = thisPath.charAt(i);
			if (c != otherPath.charAt(i)) {
				return false;
			}
			if (c == '.' && checkDotSegment(thisPath, i)) {
				return false;
			}
		}

		if (thisLength < otherLength) {
			return checkTail(otherPath, shortestLength);
		} else if (thisLength > otherLength) {
			return checkTail(thisPath, shortestLength);
		}

		return true;
	}

	/**
	 * Checks tail of (longer) path. It has to start with '/' and must not
	 * contain any "." or ".." segment.
	 * 
	 * @param path
	 * @param index
	 * @return <code>true</code>, if path is OK.
	 */
	private boolean checkTail(String path, int index) {
		if (path.charAt(index) != GD_SLASH) {
			return false;
		}

		int length = path.length();
		index++;

		for (; index < length; index++) {
			if (checkDotSegment(path, index)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Character at index must be a '.'.
	 * 
	 * @param path Path
	 * @param index Index within the path to check if there is a short segement.
	 * @return <code>true</code>, if path at index is a "." or ".." segment.
	 */
	private boolean checkDotSegment(String path, int index) {
		if (index > 0 && path.charAt(index - 1) != GD_SLASH) {
			return false;
		}

		int length = path.length();
		if (length > ++index) {
			char c = path.charAt(index);
			if (c == GD_SLASH) {
				return true;
			}

			if (c == '.') {
				if (length > ++index) {
					if (path.charAt(index) == GD_SLASH) {
						return true;
					}
				} else {
					return true;
				}
			}

			return false;
		}

		return true;
	}

	/**
	 * Returns <code>true</code> if this is a relative URI, <code>false</code>
	 * if the base part is set.
	 * 
	 * @return <code>true</code> if this is a relative URI, <code>false</code>
	 *         if the base part is set.
	 */
	public boolean isRelativeReference() {
		return relative;
	}

	/**
	 * Returns <code>true</code>, if the URI seams to be correct and there were
	 * no problems while parsing it.
	 * 
	 * @return <code>true</code>, if the URI could be parsed, <code>false</code>
	 *         otherwise.
	 */
	public boolean isValid() {
		if (notInitialized) {
			initLazy();
		}
		return valid;
	}

	/**
	 * Returns <code>true</code> if this URI has the URN schema,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this URI has the URN schema,
	 *         <code>false</code> otherwise.
	 */
	public boolean isURN() {
		return URN_SCHEMA_PREFIX.equals(schema);
	}

	/**
	 * Returns the schema defined in the URI.
	 * 
	 * @return the schema.
	 */
	public String getSchema() {
		if (schema != null && schemaDecoded == null) {
			schemaDecoded = StringUtil.decodeURL(schema).toLowerCase();
		}
		return schemaDecoded;
	}

	/**
	 * Returns the user defined in the URI.
	 * 
	 * @return the user.
	 */
	public String getUser() {
		if (notInitialized) {
			initLazy();
		}
		return user;
	}

	/**
	 * Returns the password defined in the URI.
	 * 
	 * @return the password.
	 */
	public String getPassword() {
		if (notInitialized) {
			initLazy();
		}
		return password;
	}

	/**
	 * Returns the host defined in the URI.
	 * 
	 * @return the host.
	 */
	public String getHost() {
		if (notInitialized) {
			initLazy();
		}
		return host;
	}

	/**
	 * Returns the port for the host part of this URI.
	 * 
	 * @return the port.
	 */
	public int getPort() {
		if (notInitialized) {
			initLazy();
		}
		return port;
	}

	/**
	 * Returns a <code>String</code> containing the host and the port defined in
	 * the URI. e.g. host:port.
	 * 
	 * @return the host:port part.
	 */
	public String getHostWithPort() {
		if (notInitialized) {
			initLazy();
		}

		if (isIPv6) {
			if (port != -1) {
				return GD_SQ_BR_OPEN + host + GD_SQ_BR_CLOSE + GD_COLON + port;
			}
			return GD_SQ_BR_OPEN + host + GD_SQ_BR_CLOSE;
		}

		if (port != -1) {
			return host + GD_COLON + port;
		}
		return host;
	}

	/**
	 * Returns the path defined in the URI. If no path is set, "/" is returned.
	 * 
	 * @return the path.
	 */
	public String getPath() {
		if (path != null && pathDecoded == null) {
			pathDecoded = StringUtil.decodeURL(path);
		}
		return pathDecoded;
	}

	/**
	 * Returns the query defined in the URI. e.g. the part behind the "?".
	 * (http://localhost/index?foobar=yes).
	 * 
	 * @return the query.
	 */
	public String getQuery() {
		if (query != null && queryDecoded == null) {
			queryDecoded = StringUtil.decodeURL(query);
		}
		return queryDecoded;
	}

	public void setQuery(String query) {
		this.queryDecoded = query;
		this.query = StringUtil.encodeURL(query);
		this.hash = 0;
	}

	public void setQueryEncoded(String encodedQuery) {
		this.queryDecoded = null;
		this.query = encodedQuery;
		this.hash = 0;
	}

	/**
	 * Returns the fragment defined in the URI. e.g. the fragment behind the
	 * "#". (http://localhost/index#anchor).
	 * 
	 * @return the fragment
	 */
	public String getFragment() {
		if (fragment != null && fragmentDecoded == null) {
			fragmentDecoded = StringUtil.decodeURL(fragment);
		}
		return fragmentDecoded;
	}

	public void setFragment(String fragment) {
		this.fragmentDecoded = fragment;
		this.fragment = StringUtil.encodeURL(fragment);
		this.hash = 0;
	}

	public void setFragmentEncoded(String encodedFragment) {
		fragment = encodedFragment;
		fragmentDecoded = null;
		this.hash = 0;
	}

	/**
	 * Returns the deepness of the path inside this URI. If URI relative, the
	 * path deepness = -1.
	 * 
	 * @return Path deepness. -1, if URI relative.
	 */
	public int getPathDeepness() {
		return pathdeepness;
	}

	/**
	 * Returns the leading part of the path, calculated by the path deepness.
	 * e.g if the URI is something like
	 * http://somehost.com/somepath/otherpath/file.html:
	 * <ul>
	 * <li>deepness 0 would be "/"
	 * <li>deepness 1 would be "/somepath"
	 * <li>deepness 2 would be "/somepath/otherpath"
	 * <li>deepness 3 would be "/somepath/otherpath/file.html"
	 * </ul>
	 * 
	 * @param uri The URI to use.
	 * @param deepness The path deepness.
	 * @return The path limited by deepness.
	 */
	public String getPath(int deepness) {
		if (isRelativeReference()) {
			throw new RuntimeException("URI.getPath(URI, int) is not supported for relative URIs.");
		}

		if (path == null) {
			return "";
		}

		if (deepness > pathdeepness) {
			return getPath();
		}
		if (deepness < 1) {
			return String.valueOf(GD_SLASH);
		}

		String decPath = getPath();

		int length = decPath.length();
		if (length < 2) {
			return decPath;
		}

		int searchIndex = 0;
		int depth = 0;
		while (depth < deepness) {
			depth++;
			searchIndex = decPath.indexOf(GD_SLASH, searchIndex + 1);

			if (searchIndex == -1) {
				return decPath;
			}
		}

		return decPath.substring(0, searchIndex);
	}

	/**
	 * TODO
	 * 
	 * @param baseUri
	 * @param newUri
	 * @return an absolute URI created from <code>baseUri</code> and
	 *         <code>newUri</code>
	 */
	public static URI absolutize(URI baseUri, String newUri) {
		URI absoluteUri = new URI(newUri);

		if (absoluteUri.isRelativeReference()) {
			absoluteUri = new URI(newUri, baseUri);
		}

		return absoluteUri;
	}

	/**
	 * Check for IPv6 address.
	 * <p>
	 * This will check the string for textual representation of an IPv6 address.
	 * At first, this method will check for the colon which should be between
	 * the hex digits.
	 * </p>
	 * 
	 * @param address the string representation of the address which should be
	 *            checked.
	 * @return <code>true</code> if the string is a IPv6 address,
	 *         <code>false</code> otherwise.
	 */
	public static final boolean isIPv6Address(String address) {
		int idxColon = address.indexOf(GD_COLON);
		if (idxColon > 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isIPv6Address() {
		if (notInitialized) {
			initLazy();
		}
		return isIPv6;
	}

}
