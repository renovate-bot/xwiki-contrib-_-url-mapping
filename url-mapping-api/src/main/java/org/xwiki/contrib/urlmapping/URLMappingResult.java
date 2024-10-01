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

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.Block;
import org.xwiki.resource.ResourceReference;
import org.xwiki.stability.Unstable;

/**
 * This is the result of a conversion.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
@Role
public interface URLMappingResult
{
    /**
     * @return a resource reference with an action (a document, an attachment...) which corresponds to the request. If
     * null, and both #getURL() and getSuggestions() also returns null, the request will be considered not handled by
     * the converter.
     * NOTE: Only resource references of type EntityResourceReference are handled for now.
     */
    ResourceReference getResourceReference();

    /**
     * @return a URL which corresponds to the request. If null, and #getResourceReference() also returns null,
     * the request will be considered not handled by the converter.
     */
    String getURL();

    /**
     * @return a suggestions block when the URL is handled but the target was not found, or null.
     */
    Block getSuggestions();

    /**
     * @return the HTTP status to send. 0 to use a sensible, default status.
     */
    int getHTTPStatus();

    /**
     * @return the configuration
     */
    URLMappingConfiguration getConfiguration();
}
