/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.urlmapping;

import org.xwiki.rendering.block.Block;
import org.xwiki.resource.ResourceReference;
import org.xwiki.stability.Unstable;

/**
 * Default implementation of URLMappingResult. Provides convenient mechanisms to provide a URLMappingResult
 * with (hopefully) sane defaults.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
public class DefaultURLMappingResult implements URLMappingResult
{
    private final ResourceReference reference;

    private final String url;

    private final Block suggestions;

    private final int httpCode;

    private final URLMappingConfiguration configuration;

    /**
     * @param configuration the configuration to use for the redirection
     * @param reference the resulting reference
     * @param httpCode the HTTP code to use for the response
     */
    public DefaultURLMappingResult(URLMappingConfiguration configuration, ResourceReference reference, int httpCode)
    {
        this.url = "";
        this.reference = reference;
        this.suggestions = null;
        this.httpCode = httpCode;
        this.configuration = configuration;
    }

    /**
     * @param configuration the configuration to use for the redirection
     * @param url the resulting url .
     * @param httpCode the HTTP code to use for the response
     */
    public DefaultURLMappingResult(URLMappingConfiguration configuration, String url, int httpCode)
    {
        this.url = url;
        this.reference = null;
        this.suggestions = null;
        this.httpCode = httpCode;
        this.configuration = configuration;
    }

    /**
     * @param configuration the configuration to use for the redirection
     * @param reference the resulting reference
     */
    public DefaultURLMappingResult(URLMappingConfiguration configuration, ResourceReference reference)
    {
        this.url = "";
        this.reference = reference;
        this.suggestions = null;
        this.httpCode = 0;
        this.configuration = configuration;
    }

    /**
     * @param configuration the configuration to use for the redirection
     * @param suggestions the suggestions to show .
     * @param httpCode the HTTP code to use for the response
     */
    public DefaultURLMappingResult(URLMappingConfiguration configuration, Block suggestions, int httpCode)
    {
        this.url = "";
        this.reference = null;
        this.suggestions = suggestions;
        this.httpCode = httpCode;
        this.configuration = configuration;
    }

    /**
     * @param configuration the configuration to use for the redirection
     * @param url the resulting url
     */
    public DefaultURLMappingResult(URLMappingConfiguration configuration, String url)
    {
        this.url = url;
        this.reference = null;
        this.suggestions = null;
        this.httpCode = 0;
        this.configuration = configuration;
    }

    /**
     * @param configuration the configuration to use for the redirection
     * @param suggestions the suggestions to show .
     */
    public DefaultURLMappingResult(URLMappingConfiguration configuration, Block suggestions)
    {
        this.url = "";
        this.reference = null;
        this.suggestions = suggestions;
        this.httpCode = 0;
        this.configuration = configuration;
    }

    @Override
    public ResourceReference getResourceReference()
    {
        return this.reference;
    }

    @Override
    public String getURL()
    {
        return this.url;
    }

    @Override
    public int getHTTPStatus()
    {
        return this.httpCode;
    }

    @Override
    public Block getSuggestions()
    {
        return suggestions;
    }

    @Override
    public URLMappingConfiguration getConfiguration()
    {
        return this.configuration;
    }
}

