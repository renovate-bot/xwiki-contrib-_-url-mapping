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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import org.xwiki.stability.Unstable;

/**
 * Helps create URL Mapping specification.
 * @since 0.0.1
 * @version $Id$
 */
@Unstable
public class DefaultURLMappingSpecification implements URLMappingSpecification
{
    private final Pattern[] regexes;

    /**
     * Catch-all specification: the specification will match any path.
     */
    DefaultURLMappingSpecification()
    {
        this.regexes = null;
    }

    /**
     * @param regex the regular expression for matching urls
     */
    DefaultURLMappingSpecification(Pattern... regex)
    {
        this.regexes = regex;
    }

    DefaultURLMappingSpecification(String... regex)
    {
        this.regexes = Arrays.stream(regex).map(Pattern::compile).toArray(Pattern[]::new);
    }

    @Override
    public Collection<String> getHandledHTTPMethods()
    {
        return Collections.emptyList();
    }

    @Override
    public Pattern[] getRegexes()
    {
        return this.regexes;
    }
}
