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

/* See the NOTICE file distributed with this work for additional
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

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.rendering.block.Block;

/**
 * Convenient basis for implementing a URLMappingPrefixHandler which automatically manages configuration and provides
 * common logic to use provided URLMapper objects.
 * Implementers only need to implement a named component extending this abstract class, providing the url mappers to
 * use by implementing the getMappers method, like this:
 *
 * Usage:
 *
 * <pre>
 * &#064;Component
 * &#064;Singleton
 * &#064;Named("my")
 * public class MyURLPrefixHandler extends AbstractURLMappingPrefixHandler
 * {
 *     &#064;Inject
 *     private MyURLMapper1 myURLMapper1;
 *
 *     &#064;Inject
 *     private MyURLMapper2 myURLMapper2;
 *
 *     &#064;Override
 *     protected URLMapper[] getMappers()
 *     {
 *         return new URLMapper[] {
 *             myURLMapper1,
 *             myURLMapper2
 *         };
 *     }
 *  }
 *</pre>
 *
 * @version $Id$
 * @since 0.0.1
 */
public abstract class AbstractURLMappingPrefixHandler implements URLMappingPrefixHandler
{
    private static final int HTTP_ERROR_CODE = 404;

    @Inject
    private ComponentDescriptor<URLMappingPrefixHandler> descriptor;

    private URLMappingConfiguration configuration;

    @Inject
    private ConfigurationSource configurationSource;

    @Inject
    private ComponentManager componentManager;

    @Override
    public URLMappingResult convert(String path, String method, HttpServletRequest request)
    {
        for (URLMapper mapper : getMappers()) {
            if (mapper instanceof AbstractURLMapper) {
                AbstractURLMapper m = (AbstractURLMapper) mapper;
                if (m.getConfiguration() == null) {
                    m.setConfiguration(this.getConfiguration());
                }
            }

            URLMappingResult result = convert(mapper, path, method, request);

            if (result != null) {
                return result;
            }
        }

        // We don't return null so custom configurations are not lost
        return new DefaultURLMappingResult(getConfiguration(), "", HTTP_ERROR_CODE);
    }

    @Override
    public String getPrefix()
    {
        return this.getConfiguration().getPrefix();
    }

    /**
     * @return the URL mappers that should be used to convert URLs in this prefix
     */
    protected abstract URLMapper[] getMappers();

    private URLMappingResult convert(URLMapper mapper, String path, String method, HttpServletRequest request)
    {
        URLMappingSpecification spec = mapper.getSpecification();
        if (isHTTPMethodCompatible(method, spec)) {
            return null;
        }

        Pattern[] regexes = spec == null ? null : spec.getRegexes();
        Matcher matcher = null;
        if (regexes != null && regexes.length != 0) {
            // if no regex is given, we consider that the mapper handles the URL (catch-all)
            matcher = matchOneRegex(path, regexes);
            if (matcher == null) {
                return null;
            }
        }

        DefaultURLMappingMatch m = new DefaultURLMappingMatch(path, method, matcher, request);
        URLMappingResult r = mapper.convert(m);

        if (r == null && mapper instanceof AbstractURLMapper) {
            Block suggestions = ((AbstractURLMapper) mapper).getSuggestions(m);
            if (suggestions != null) {
                return new DefaultURLMappingResult(getConfiguration(), suggestions, HTTP_ERROR_CODE);
            }
        }

        return r;
    }

    private static boolean isHTTPMethodCompatible(String method, URLMappingSpecification spec)
    {
        Collection<String> handledHTTPMethods = spec == null ? null : spec.getHandledHTTPMethods();
        return handledHTTPMethods != null && !handledHTTPMethods.isEmpty() && !handledHTTPMethods.contains(method);
    }

    private static Matcher matchOneRegex(String pathWithoutPrefix, Pattern[] regexes)
    {
        for (Pattern regex : regexes) {
            Matcher matcher = regex.matcher(pathWithoutPrefix);
            if (matcher.matches()) {
                return matcher;
            }
        }
        return null;
    }

    protected URLMappingConfiguration getConfiguration()
    {
        if (this.configuration == null) {
            DefaultURLMappingConfiguration conf =
                new DefaultURLMappingConfiguration(configurationSource, descriptor.getRoleHint());
            initializeConfigurationDefaults(conf);
            this.configuration = conf;
        }

        return this.configuration;
    }

    protected void initializeConfigurationDefaults(DefaultURLMappingConfiguration configuration)
    {
        // ignore
    }
}
