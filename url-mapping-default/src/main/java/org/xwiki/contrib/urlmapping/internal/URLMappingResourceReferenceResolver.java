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
package org.xwiki.contrib.urlmapping.internal;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.slf4j.Logger;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceResolver;
import org.xwiki.resource.ResourceType;
import org.xwiki.url.ExtendedURL;

import static org.apache.commons.lang3.StringUtils.strip;

/**
 * The URL mapping resource reference resolver.
 * @since 0.0.1
 * @version $Id$
 */
public class URLMappingResourceReferenceResolver implements ResourceReferenceResolver<ExtendedURL>
{

    private static final Logger LOGGER = LoggerFactory.getLogger(URLMappingResourceReferenceResolver.class);

    private static final String SLASH = "/";

    private final String prefix;
    private final String name;

    URLMappingResourceReferenceResolver(String prefix, String name)
    {
        this.prefix = prefix;
        this.name = name;
    }

    @Override
    public ResourceReference resolve(ExtendedURL representation, ResourceType resourceType,
        Map<String, Object> parameters)
    {
        String path = StringUtils.stripStart(representation.toString(), SLASH);
        return new URLMappingResourceReference(this.prefix, this.name, path);
    }

    /**
     * Register a resource reference resolver for a URL prefix handler.
     * We manually register the resolver for each handler, so it has the right hint corresponding to the prefix
     * set in the configuration.
     * @param name the name of the URL prefix handler
     * @param prefix the prefix to use
     * @param componentManager the component manager to which to register the resolver
     */
    public static void register(String name, String prefix, ComponentManager componentManager)
    {
        String strippedPrefix = strip(prefix, SLASH);
        if (strippedPrefix == null || strippedPrefix.isEmpty()) {
            return;
        }
        DefaultComponentDescriptor<URLMappingResourceReferenceResolver> descriptor = new DefaultComponentDescriptor<>();
        descriptor.setImplementation(URLMappingResourceReferenceResolver.class);
        descriptor.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);
        descriptor.setRoleHint(strippedPrefix);
        descriptor.setRoleType(new DefaultParameterizedType(null, ResourceReferenceResolver.class, ExtendedURL.class));

        try {
            componentManager.registerComponent(descriptor,
                new URLMappingResourceReferenceResolver(strippedPrefix, name));
        } catch (ComponentRepositoryException e) {
            LOGGER.error("Could not register URL Mapping resolver for prefix [{}]", strippedPrefix, e);
        }
    }
}
