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
import java.io.IOException;
import java.io.InterruptedIOException;
import gr.fire.util.FireConnector;
import gr.fire.browser.util.Response;

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
        if (prefix.indexOf(' ') == -1) {
            this.prefix = prefix;

        } else {
            StringBuffer strb = new StringBuffer();
            char c;

            for (int i=0; i < prefix.length(); ++i) {
                c = prefix.charAt(i);

                if (c == ' ') {
                    strb.append("%20");
                } else {
                    strb.append(c);
                }
            }

            this.prefix = strb.toString();
        }
    }

    public Response requestResource(String url,
                                    String requestMethod,
                                    Hashtable requestProperties,
                                    byte[] data,
                                    boolean updateCurrentPage)
        throws InterruptedIOException, SecurityException, IOException,
               IllegalStateException, Exception
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

