/*
 * Copyright (c) 2009 Timothy Bourke
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the "BSD License" which is distributed with the
 * software in the file LICENSE.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the BSD
 * License for more details.
 */

package mnemojojo;

import java.lang.*;
import java.util.Hashtable;
import gr.fire.util.FireConnector;
import gr.fire.browser.util.Request;

class HttpClient
    extends gr.fire.browser.util.HttpClient
{
    protected String prefix = null;

    public HttpClient(FireConnector connector)
    {
	super(connector);
    }

    public void setUrlPrefix(String prefix)
    {
	this.prefix = prefix;
    }

    public Request requestResource(String url,
				   String requestMethod,
				   Hashtable requestProperties,
				   byte[] data,
				   boolean updateCurrentPage)
    {
	String newurl = url;
	if (prefix != null) {
	    newurl = prefix + url;
	}

	return super.requestResource(newurl,
				     requestMethod, requestProperties,
				     data, updateCurrentPage);
    }
}

