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
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.entity.EntityResourceAction;
import org.xwiki.resource.entity.EntityResourceReference;

@Component
@Named("testcustomintro")
public class TestCustomIntroURLMappingPrefixHandler extends AbstractURLMappingPrefixHandler
{
    @Override
    protected void initializeConfigurationDefaults(DefaultURLMappingConfiguration configuration)
    {
        configuration.setDefault(DefaultURLMappingConfiguration.Key.DELAY, 3);
    }

    @Override
    protected URLMapper[] getMappers()
    {
        return new URLMapper[] {
            new AbstractURLMapper(new URLMappingSpecification() {
                @Override
                public Collection<String> getHandledHTTPMethods()
                {
                    return List.of("get");
                }

                @Override
                public Pattern[] getRegexes()
                {
                    return null;
                }
            })
            {
                @Override
                public ResourceReference convert(DefaultURLMappingMatch match)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public URLMappingResult convert(URLMappingMatch match)
                {
                    return new DefaultURLMappingResult(
                        getConfiguration(),
                        new EntityResourceReference(
                            new DocumentReference("xwiki", "Main", "RedirectTarget"),
                            EntityResourceAction.VIEW
                        ),
                        429
                    );
                }
            },
            new AbstractURLMapper()
            {
                @Override
                public URLMappingResult convert(URLMappingMatch match)
                {
                    return new DefaultURLMappingResult(
                        new URLMappingConfiguration()
                        {
                            @Override
                            public int getDelay()
                            {
                                return 0;
                            }

                            @Override
                            public String getPrefix()
                            {
                                return "";
                            }

                            @Override
                            public String getRedirectScreenTemplateName()
                            {
                                return "";
                            }

                            @Override
                            public String getNotFoundScreenTemplateName()
                            {
                                return "";
                            }

                            @Override
                            public String getIntroMessage()
                            {
                                return "";
                            }

                            @Override
                            public String getNotFoundIntroMessage()
                            {
                                return "";
                            }

                            @Override
                            public String getTitle()
                            {
                                return "";
                            }
                        },
                        new EntityResourceReference(
                            new DocumentReference("xwiki", "Main", "RedirectWrongMethod"),
                            EntityResourceAction.VIEW
                        )
                    );
                }

                @Override
                public ResourceReference convert(DefaultURLMappingMatch match)
                {
                    return null;
                }
            }
        };
    }
}
