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
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import org.xwiki.stability.Unstable;

/**
 * Default implementation of a link preservation result match, which allows using a Matcher, which, contrary to a simple
     * MatchResult object, also has the capability to handle named regex groups.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
public class DefaultURLMappingMatch implements URLMappingMatch
{
    private final String matchedURL;

    private final String httpMethod;

    private final MatchResult matchResult;

    private final HttpServletRequest request;

    /**
     * @param matchedURL the URL that matched
     * @param method the http method used for this request
     * @param matchResult the result of the match
     * @param request the request
     */
    public DefaultURLMappingMatch(String matchedURL, String method, MatchResult matchResult, HttpServletRequest request)
    {
        this.matchedURL = matchedURL;
        this.httpMethod = method;
        this.matchResult = matchResult;
        this.request = request;
    }

    @Override
    public String getMatchedURL()
    {
        return matchedURL;
    }

    @Override
    public String getHTTPMethod()
    {
        return httpMethod;
    }

    @Override
    public MatchResult getMatchResult()
    {
        return matchResult;
    }

    /**
     * @return the result as a Matcher object. Basically a checked cast of getMatchResult().
     */
    public Matcher getMatcher()
    {
        // Unfortunately, MatchResult doesn't have named groups.
        if (matchResult instanceof Matcher) {
            return (Matcher) matchResult;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServletRequest getRequest()
    {
        return request;
    }
}
