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

import java.util.regex.Pattern;

import org.xwiki.rendering.block.Block;
import org.xwiki.resource.ResourceReference;
import org.xwiki.stability.Unstable;

/**
 * Convenient basis to implement a URL mapper, which also lets you use a less generic but more convenient Matcher
 * object instead of a MatchResult.
 * @since 0.0.1
 * @version $Id$
 *
 * Usage:
 *
 * <pre>
 * &#064;Component (roles = MyURLMapper.class)
 * &#064;Singleton
 * public class MyURLMapper extends AbstractURLMapper
 * {
 *     public MyURLMapper()
 *     {
 *         // Provide the regex(es) you want to handle with this URL Mapper
 *         super("^/mypath/(?&lt;pageId&gt;\\d+)$");
 *     }
 *
 *     &#064;Override
 *     public ResourceReference convertURL(DefaultURLMappingMatch match)
 *     {
 *          return new EntityResourceReference(
 *               // do something with match
 *          );
 *     }
 * }
 * </pre>
 */
@Unstable
public abstract class AbstractURLMapper implements URLMapper
{
    private final URLMappingSpecification specification;
    private URLMappingConfiguration configuration;

    protected AbstractURLMapper(URLMappingSpecification specification)
    {
        this.specification = specification;
    }

    protected AbstractURLMapper()
    {
        this.specification = new DefaultURLMappingSpecification();
    }


    protected AbstractURLMapper(Pattern... regex)
    {
        this.specification = new DefaultURLMappingSpecification(regex);
    }

    protected AbstractURLMapper(String... regex)
    {
        this.specification = new DefaultURLMappingSpecification(regex);
    }

    @Override
    public URLMappingSpecification getSpecification()
    {
        return this.specification;
    }

    @Override
    public URLMappingResult convert(URLMappingMatch match)
    {
        if (match instanceof DefaultURLMappingMatch) {
            ResourceReference reference = convert((DefaultURLMappingMatch) match);
            if (reference == null) {
                return null;
            }
            return new DefaultURLMappingResult(this.configuration, reference);
        }

        throw new UnsupportedOperationException();
    }

    protected void setConfiguration(URLMappingConfiguration configuration)
    {
        this.configuration = configuration;
    }

    protected URLMappingConfiguration getConfiguration()
    {
        return this.configuration;
    }

    protected Block getSuggestions(DefaultURLMappingMatch match)
    {
        return null;
    }

    /**
     * @return the result of a conversion, or null if no such conversion is possible, in which case the
     * request will be considered not handled by the converter.
     * @param match (as DefaultURLMappigMatch) the result of the matched regex
     * Note: this version uses a DefaultURLMappingMatch object instead of the generic URLMappingMatch so a Matcher
     * can be
     * user, and not simply a MatchResult object.
     */
    public abstract ResourceReference convert(DefaultURLMappingMatch match);
}
