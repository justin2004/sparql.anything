/*
 * Copyright (c) 2023 SPARQL Anything Contributors @ http://github.com/sparql-anything
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.sparqlanything.model;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.net.URI;
import java.util.Properties;

public abstract class BaseFacadeXBuilder implements FacadeXNodeBuilder, FacadeXQuadHandler, FacadeXComponentHandler {
	protected final Properties properties;
	protected final Node mainGraphName;
	//
	protected final boolean p_blank_nodes;
	protected final String p_namespace;
	protected final String p_root;
	protected final boolean p_trim_strings;
	protected final String p_null_string;
	protected final boolean p_use_rdfs_member;
	protected final boolean p_reify_slot_statements;

	public BaseFacadeXBuilder(String resourceId, Properties properties) {
		this.properties = properties;
		this.mainGraphName = NodeFactory.createURI(resourceId);
		this.p_blank_nodes = PropertyUtils.getBooleanProperty(properties, IRIArgument.BLANK_NODES);
		this.p_namespace = Triplifier.getNamespaceArgument(properties);
		this.p_root = Triplifier.getRootArgument(properties);
		this.p_trim_strings = PropertyUtils.getBooleanProperty(properties, IRIArgument.TRIM_STRINGS);
		this.p_null_string = PropertyUtils.getStringProperty(properties, IRIArgument.NULL_STRING);
		this.p_use_rdfs_member = PropertyUtils.getBooleanProperty(properties, IRIArgument.USE_RDFS_MEMBER);
		this.p_reify_slot_statements = PropertyUtils.getBooleanProperty(properties, IRIArgument.ANNOTATE_TRIPLES_WITH_SLOT_KEYS);
	}

	public boolean addContainer(String dataSourceId, String containerId, String slotKey, String childContainerId) {
		return add(NodeFactory.createURI(dataSourceId), container2node(containerId), key2predicate(slotKey), container2node(childContainerId));
	}

	public boolean addContainer(String dataSourceId, String containerId, URI customKey, String childContainerId) {
		return add(NodeFactory.createURI(dataSourceId), container2node(containerId), NodeFactory.createURI(customKey.toString()), container2node(childContainerId));
	}

	public boolean addContainer(String dataSourceId, String containerId, Integer slotKey, String childContainerId) {
		return addSlotStatement(dataSourceId, containerId, slotKey, childContainerId, true);
	}

	public boolean addType(String dataSourceId, String containerId, String typeId) {
		return add(NodeFactory.createURI(dataSourceId), container2node(containerId), RDF.type.asNode(), NodeFactory.createURI(typeId));
	}

	public boolean addType(String dataSourceId, String containerId, URI type) {
		return add(NodeFactory.createURI(dataSourceId), container2node(containerId), RDF.type.asNode(), NodeFactory.createURI(type.toString()));
	}

	public boolean addValue(String dataSourceId, String containerId, String slotKey, Object value) {
		return add(NodeFactory.createURI(dataSourceId), container2node(containerId), key2predicate(slotKey), value2node(value));
	}

	public boolean addValue(String dataSourceId, String containerId, URI customKey, Object value) {
		return add(NodeFactory.createURI(dataSourceId), container2node(containerId), NodeFactory.createURI(customKey.toString()), value2node(value));
	}

	public boolean addValue(String dataSourceId, String containerId, Integer slotKey, Object value) {
		return addSlotStatement(dataSourceId, containerId, slotKey, value, false);
	}

	public boolean addRoot(String dataSourceId, String rootId) {
		return add(NodeFactory.createURI(dataSourceId), container2node(rootId), RDF.type.asNode(), NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT));
	}

	private boolean addSlotStatement(String dataSourceId, String containerId, Integer slotKey, Object object, boolean isObjectContainer) {
		Node g = NodeFactory.createURI(dataSourceId);
		Node s = container2node(containerId);
		Node p = p_use_rdfs_member ? RDFS.member.asNode() : RDF.li(slotKey).asNode();
		Node o = isObjectContainer ? container2node(object.toString()) : value2node(object);
		if (p_reify_slot_statements) {
			add(g, NodeFactory.createTripleNode(s, p, o), NodeFactory.createURI(Triplifier.FACADE_X_SLOT_KEY), NodeFactory.createLiteral(slotKey.toString(), XSDDatatype.XSDinteger));
		}
		return add(g, s, p, o);
	}

	public Node value2node(Object value) {
		// trims_strings == true and if object is string, trim it
		if (p_trim_strings && value instanceof String) {
			value = ((String) value).trim();
		}
		return FacadeXNodeBuilder.super.value2node(value);
	}

	public Node container2node(String container) {
		if (p_blank_nodes) {
			return container2BlankNode(container);
			// return NodeFactory.createBlankNode(container);
		} else {
//			return NodeFactory.createURI(container);
			return container2URI(container);
		}
	}

	public Node key2predicate(String key) {
		return key2predicate(this.p_namespace, key);
	}
}
