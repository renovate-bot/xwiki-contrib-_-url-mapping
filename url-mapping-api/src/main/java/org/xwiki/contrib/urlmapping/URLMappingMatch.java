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

import java.util.regex.MatchResult;

import javax.servlet.http.HttpServletRequest;

import org.xwiki.stability.Unstable;

/**
 * This is the result of a URL matching.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
public interface URLMappingMatch
{
    /**
     * @return the matched URL, excluding the link preference's resource particle.
     */
    String getMatchedURL();

    /**
     * @return the HTTP method
     */
    String getHTTPMethod();

    /**
     * @return the MatchResult object resulting from matching the URL with the specified regex
     */
    MatchResult getMatchResult();

    /**
     * @return the restlet HTTP request, or null if no request is available.
     */
    HttpServletRequest getRequest();
}
