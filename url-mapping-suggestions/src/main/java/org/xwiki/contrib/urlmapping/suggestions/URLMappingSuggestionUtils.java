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
package org.xwiki.contrib.urlmapping.suggestions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;

/**
 * Component to provide suggestions.
 * @version $Id$
 * @since 0.0.1
 */
@Component (roles = URLMappingSuggestionUtils.class)
@Singleton
@Unstable
public class URLMappingSuggestionUtils
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private Logger logger;

    /**
     * @return a suggestion block for the given fictive reference
     * @param documentReference the fictive reference for which to find suggestions
     */
    public Block getSuggestionsFromDocumentReference(EntityReference documentReference)
    {
        try {
            List<String> names = new ArrayList<>();
            EntityReference ref = documentReference;
            do {
                if (ref.getType() == EntityType.DOCUMENT || ref.getType() == EntityType.SPACE) {
                    names.add(escapeSolr(ref.getName()));
                }
                ref = ref.getParent();
            } while (ref != null);

            if (names.isEmpty()) {
                return null;
            }

            String queryString = "spaces:(" + String.join(" ", names) + ")~";

            Query query = this.queryManager.createQuery(queryString, "solr");
            query.bindValue("fq", "type:DOCUMENT");
            query.bindValue("q.op", "AND");

            QueryResponse response = (QueryResponse) query.execute().get(0);
            SolrDocumentList results = response.getResults();
            if (results.isEmpty()) {
                return null;
            }
            List<Block> items = new ArrayList<>(results.size());
            for (SolrDocument result : results) {
                items.add(new ListItemBlock(Collections.singletonList(new LinkBlock(
                    Collections.emptyList(),
                    new ResourceReference((String) result.get("fullname"), ResourceType.DOCUMENT),
                    false))));
            }

            return new BulletedListBlock(items);
        } catch (Exception e) {
            this.logger.warn("Could not produce suggestions", e);
        }
        return null;
    }

    private String escapeSolr(String solrValue)
    {
        String[] solrSpecialChars = new String[] { "+", "-", "&amp;&amp;", "||", "!", "(", ")", "{", "}", "[",
            "]", "^", "\"", "~", "*", "?", ":", "/", "\\", " " };
        String[] escapedSolrSpecialChars = new String[] {"\\+", "\\-", "\\&amp;&amp;", "\\||", "\\!", "\\(",
            "\\)", "\\{", "\\}", "\\[", "\\]", "\\^", "\\\"", "\\~", "\\*", "\\?", "\\:", "\\/", "\\\\", "\\ " };

        return StringUtils.replaceEach(solrValue, solrSpecialChars, escapedSolrSpecialChars);
    }
}
