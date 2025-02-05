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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.contrib.urlmapping.DefaultURLMappingResult;
import org.xwiki.contrib.urlmapping.DefaultURLMappingConfiguration;
import org.xwiki.contrib.urlmapping.URLMappingException;
import org.xwiki.contrib.urlmapping.URLMappingPrefixHandler;
import org.xwiki.contrib.urlmapping.URLMappingRedirector;
import org.xwiki.contrib.urlmapping.URLMappingResult;
import org.xwiki.resource.AbstractResourceReferenceHandler;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceHandlerChain;
import org.xwiki.resource.ResourceReferenceHandlerException;
import org.xwiki.resource.ResourceType;
import org.xwiki.resource.annotations.Authenticate;

/**
 * The URL mapping resource reference handler.
 * @since 0.0.1
 * @version $Id$
 */
@Component
@Named("urlmapping")
@Singleton
@Authenticate
public class URLMappingResourceReferenceHandler extends AbstractResourceReferenceHandler<ResourceType>
{
    @Inject
    private Container container;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private URLMappingRedirector redirector;

    @Inject
    private ConfigurationSource configurationSource;

    @Inject
    private Logger logger;

    private URLMappingResult notFound;

    private List<ResourceType> supportedTypes = Collections.emptyList();

    @Override
    public List<ResourceType> getSupportedResourceReferences()
    {
        return this.supportedTypes;
    }

    void setSupportedTypes(Collection<String> types)
    {
        this.supportedTypes = types.stream().map(ResourceType::new).collect(Collectors.toList());
    }

    @Override
    public void handle(ResourceReference reference, ResourceReferenceHandlerChain chain)
        throws ResourceReferenceHandlerException
    {
        if (reference instanceof URLMappingResourceReference) {
            try {
                logger.debug("Handling reference [{}]", reference);
                handle((URLMappingResourceReference) reference);
            } catch (URLMappingException e) {
                throw new ResourceReferenceHandlerException("Failed to redirect", e);
            }
        } else {
            logger.debug("Reference [{}] is not an instance of URLMappingResourceReference, ignoring.", reference);
        }
        chain.handleNext(reference);
    }

    private void handle(URLMappingResourceReference ref) throws URLMappingException
    {
        HttpServletRequest request = ((ServletRequest) this.container.getRequest()).getHttpServletRequest();
        String method = request.getMethod();

        URLMappingResult conversion = null;

        try {
            URLMappingPrefixHandler handler = componentManagerProvider.get()
                .getInstance(URLMappingPrefixHandler.class, ref.getName());
            String path = ref.getPath();
            logger.debug("Converting path [{}], method [{}] using prefix handler [{}]", path, method,
                handler.getClass().getName());
            conversion = handler.convert(path, method, request);
        } catch (ComponentLookupException e) {
            this.logger.error("Could not get the URL prefix handler named [{}]", ref.getName(), e);
        }

        if (conversion == null) {
            logger.debug("The handler did not convert [{}], will use the not found configuration", ref);
            conversion = getNotFoundConfiguration();
        }
        logger.debug("Converted [{}] to [{}]", ref, conversion);
        redirector.redirect(conversion);
    }

    private URLMappingResult getNotFoundConfiguration()
    {
        if (this.notFound == null) {
            this.notFound = new DefaultURLMappingResult(
                new DefaultURLMappingConfiguration(configurationSource, null), "", 404);
        }

        return this.notFound;
    }
}
