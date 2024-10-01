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

import java.util.Collection;
import java.util.regex.Pattern;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * This is the way for a converter to tell the handler which URLs/requests it manages.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
@Role
public interface URLMappingSpecification
{
    /**
     * @return a list of HTTP methods that the converter accepts. An empty list or null accepts everything.
     */
    Collection<String> getHandledHTTPMethods();

    /**
     * @return an array of regexes that should be matched with the URL-encoded part of the URL after the particle path
     * corresponding to the url mapper's reference handler (i.e. when a request is made to
     * https://xwiki.example.org/xwiki/mapped-url/mylink?id=42, /mylink?id=42 will be matched).
     */
    Pattern[] getRegexes();
}
