/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.structures.HashMap;
import org.ws4d.java.types.InternetMediaType;
import org.ws4d.java.types.URI;

/**
 * This is the interface for resources which can be deployed by a given
 * communication manager.
 * <p>
 * Every implementation of this interface should return correct values for
 * content type and size.
 * </p>
 * <p>
 * If it is not possible to determinate the content type of the resource, the
 * {@link #getContentType()} method should return at least the
 * application/octet-stream content type.
 * </p>
 */
public interface Resource {

	/**
	 * Returns the content type of this resource.
	 * <p>
	 * If it is not possible to determinate the content type of the resource,
	 * the {@link #getContentType()} method should return at least the
	 * application/octet-stream content type.
	 * </p>
	 * 
	 * @return the content type of this resource.
	 */
	public InternetMediaType getContentType();

	/**
	 * Serializes this resource to the given output stream <code>out</code>.
	 * 
	 * @param request the request URI
	 * @param requestHeader the header of the request
	 * @param requestBody makes the content of the request body available
	 * @param out the outputs stream to serialize this resource over
	 * @throws IOException if an IO failure occurs during serialization
	 */
	public void serialize(URI request, RequestHeader requestHeader, InputStream requestBody, OutputStream out) throws IOException;

	/**
	 * Returns additional header fields for this resource.
	 * <p>
	 * The additional header fields allow to add header fields to the response
	 * which will be created to send this resource.
	 * </p>
	 * <p>
	 * <strong>NOTICE:</strong> The map must contain a <code>String</code>,
	 * <code>String</code> mapping.
	 * </p>
	 * 
	 * @return a map which contains the header=>value mapping.
	 */
	public HashMap getHeaderFields();

	/**
	 * The size of the resource.
	 * <p>
	 * If it is not possible to determinate the size of the resource. The
	 * implementation should return <code>-1</code>. This will allow to send the
	 * resource as chunked content.
	 * </p>
	 * 
	 * @return the size of the resource.
	 */
	public long size();

	/**
	 * Returns the last time where this resource was modified as UNIX timestamp.
	 * 
	 * @return last time where this resource was modified.
	 */
	public long getLastModifiedDate();

	/**
	 * A short string representation of this resource.
	 * 
	 * @return a short string representation.
	 */
	public String shortDescription();

}
