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
import org.xwiki.stability.Unstable;
import javax.servlet.http.HttpServletRequest;

/**
 * Manages a group of URL mappers under a common path prefix.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
@Role
public interface URLMappingPrefixHandler
{
    /**
     * @return the converted request, or null if the request could not be converted in the end
     * @param path the URL-encoded path to convert
     * @param method the method that was used to perform the request
     * @param request the request (can be null)
     */
    URLMappingResult convert(String path, String method, HttpServletRequest request);

    /**
     * @return the prefix to use
     */
    String getPrefix();
}
