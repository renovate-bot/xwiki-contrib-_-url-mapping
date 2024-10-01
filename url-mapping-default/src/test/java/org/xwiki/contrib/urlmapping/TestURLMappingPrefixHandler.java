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

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.entity.EntityResourceAction;
import org.xwiki.resource.entity.EntityResourceReference;

@Component
@Named("test")
public class TestURLMappingPrefixHandler extends AbstractURLMappingPrefixHandler
{
    @Override
    protected URLMapper[] getMappers()
    {
        URLMappingConfiguration configuration = getConfiguration();
        return new URLMapper[] {
            new AbstractURLMapper("^(?<matchedpart>[a-z]+)")
            {
                @Override
                public ResourceReference convert(DefaultURLMappingMatch match)
                {
                    if (match.getMatcher().group("matchedpart").equals("expectedmatch")) {
                        return new EntityResourceReference(
                            new DocumentReference("mywiki", "MySpace", "MyDoc"),
                            EntityResourceAction.VIEW
                        );
                    }
                    return null;
                }

                @Override
                protected Block getSuggestions(DefaultURLMappingMatch match)
                {
                    return new ParagraphBlock(List.of(
                        new WordBlock("My"),
                        new WordBlock("Suggestion")
                    ));
                }
            },
            new AbstractURLMapper(Pattern.compile("^00GETURL$"), Pattern.compile("^0GETURL$"))
            {
                @Override
                public ResourceReference convert(DefaultURLMappingMatch match)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public URLMappingResult convert(URLMappingMatch match)
                {
                    return new URLMappingResult()
                    {
                        @Override
                        public ResourceReference getResourceReference()
                        {
                            return null;
                        }

                        @Override
                        public String getURL()
                        {
                            return "/transformedUrl";
                        }

                        @Override
                        public Block getSuggestions()
                        {
                            return null;
                        }

                        @Override
                        public int getHTTPStatus()
                        {
                            return 200;
                        }

                        @Override
                        public URLMappingConfiguration getConfiguration()
                        {
                            return configuration;
                        }
                    };
                }
            },
            new AbstractURLMapper(new DefaultURLMappingSpecification("^1GETURL(/[A-Z]+)$"))
            {
                @Override
                public ResourceReference convert(DefaultURLMappingMatch match)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public URLMappingResult convert(URLMappingMatch match)
                {
                    return new DefaultURLMappingResult(configuration, match.getMatchResult().group(1));
                }
            },
            new URLMapper()
            {
                @Override
                public URLMappingSpecification getSpecification()
                {
                    return null;
                }

                @Override
                public URLMappingResult convert(URLMappingMatch match)
                {
                    if (match.getMatchedURL().contains("missinglink")) {
                        return new DefaultURLMappingResult(getConfiguration(), "/mycustomnotfound", 404);
                    }

                    return null;
                }
            }
        };
    }
}
