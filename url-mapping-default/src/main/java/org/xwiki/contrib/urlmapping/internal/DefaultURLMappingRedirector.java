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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.script.ScriptContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletResponse;
import org.xwiki.contrib.urlmapping.URLMappingException;
import org.xwiki.contrib.urlmapping.URLMappingRedirector;
import org.xwiki.contrib.urlmapping.URLMappingResult;
import org.xwiki.contrib.urlmapping.URLMappingConfiguration;
import org.xwiki.rendering.block.Block;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.entity.EntityResourceReference;
import org.xwiki.script.ScriptContextManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

/**
 * Default redirector.
 * @since 0.0.1
 * @version $Id$
 */
@Component
@Singleton
public class DefaultURLMappingRedirector implements URLMappingRedirector
{
    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ScriptContextManager scriptContextManager;

    @Inject
    private Logger logger;

    @Inject
    private Container container;

    @Override
    public void redirect(URLMappingResult conversion) throws URLMappingException
    {
        String url = getURL(conversion, contextProvider.get());

        URLMappingConfiguration configuration = conversion.getConfiguration();
        if (configuration == null) {
            logger.error("A configuration for handling this redirect is missing");
            return;
        }

        HttpServletResponse response = ((ServletResponse) this.container.getResponse()).getHttpServletResponse();

        int delay = configuration.getDelay();
        boolean isURLPresent = url != null && !url.isEmpty();
        String templateName = isURLPresent
            ? configuration.getRedirectScreenTemplateName()
            : configuration.getNotFoundScreenTemplateName();

        int httpStatus = conversion.getHTTPStatus();
        if (isURLPresent && (delay == 0 || templateName == null || templateName.isEmpty())) {
            sendRedirect(response, url, getRedirectHTTPStatus(httpStatus, configuration.getRedirectHTTPStatus()));
        } else {
            Block suggestions = conversion.getSuggestions();
            Map<String, Object> m = new HashMap<>(4);
            m.put("url", url);
            m.put("suggestions", suggestions);
            m.put("conversion", conversion);
            m.put("configuration", configuration);
            this.scriptContextManager.getScriptContext().setAttribute("urlmapper", m, ScriptContext.ENGINE_SCOPE);
            response.setStatus(getHTTPStatus(httpStatus, isURLPresent));
            response.setContentType("text/html; charset=utf-8");
            try {
                if (templateName.endsWith(".vm")) {
                    templateName = templateName.substring(0, templateName.length() - 3);
                }
                XWikiContext context = this.contextProvider.get();
                if (context.getDoc() == null) {
                    // This avoids rendering errors
                    context.setDoc(new XWikiDocument(null));
                }
                Utils.parseTemplate(templateName, context);
            } catch (Exception e) {
                logger.error(
                    "An error occurred while rendering the redirection screen, falling back to a simple redirection",
                    e
                );
                sendRedirect(response, url, conversion.getHTTPStatus());
            }
        }
    }

    private static int getHTTPStatus(int httpStatus, boolean isURLPresent)
    {
        if (httpStatus == 0) {
            return isURLPresent ? 200 : 404;
        }

        return httpStatus;
    }

    private static int getRedirectHTTPStatus(int httpStatus, int conversion)
    {
        return httpStatus == 0 ? conversion : httpStatus;
    }

    private static void sendRedirect(HttpServletResponse response, String url, int status) throws URLMappingException
    {
        try {
            if (status == 0 || status == 302) {
                response.sendRedirect(url);
            } else {
                response.setStatus(status);
                response.setHeader("Location", url);
            }
        } catch (IOException e) {
            throw new URLMappingException("Failed to redirect", e);
        }
    }

    private static String getURL(URLMappingResult conversion, XWikiContext context)
        throws URLMappingException
    {
        String url = conversion.getURL();

        if (url == null || url.isEmpty()) {
            ResourceReference targetResourceReference = conversion.getResourceReference();
            if (targetResourceReference != null) {
                if (targetResourceReference instanceof EntityResourceReference) {
                    EntityResourceReference target = (EntityResourceReference) targetResourceReference;
                    url = context.getWiki().getURL(
                        target.getEntityReference(),
                        target.getAction().toString(),
                        context
                    );
                } else {
                    throw new URLMappingException("Unhandled ResourceReference type");
                }
            }
        }
        return url;
    }
}
