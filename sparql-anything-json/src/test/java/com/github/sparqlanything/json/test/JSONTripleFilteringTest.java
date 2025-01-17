/*
 * Copyright (c) 2021 Enrico Daga @ http://www.enridaga.net
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.sparqlanything.json.test;

import com.github.sparqlanything.model.TriplifierHTTPException;
import com.github.sparqlanything.json.JSONTriplifier;
import com.github.sparqlanything.model.FacadeXGraphBuilder;
import com.github.sparqlanything.model.IRIArgument;

import com.github.sparqlanything.model.TripleFilteringFacadeXBuilder;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class JSONTripleFilteringTest {
	public static final Logger log = LoggerFactory.getLogger(JSONTripleFilteringTest.class);

	protected FacadeXGraphBuilder getTripleFilteringBuilder(URL url, Op op, Properties p){
		return new TripleFilteringFacadeXBuilder(url.toString(), op, p);
	}

	@Test
	public void friendsSinglePattern() throws IOException, TriplifierHTTPException {
		URL url = getClass().getClassLoader().getResource("./friends.json");
		JSONTriplifier jt = new JSONTriplifier();
		Properties properties = new Properties();
		OpBGP bgp = new OpBGP();
		bgp.getPattern().add(new Triple(new Node_Variable("a"), new Node_Variable("b"),
				ResourceFactory.createPlainLiteral("Romance").asNode()));
		DatasetGraph g1;
		properties.setProperty(IRIArgument.LOCATION.toString(),
				url.toString());
		FacadeXGraphBuilder builder = getTripleFilteringBuilder(url, bgp, properties);
		g1 = jt.triplify(properties, builder);
		// Only two triples matching the BGP
		log.info("Size is: {}", g1.getDefaultGraph().size());
		Iterator<Quad> quads = g1.find();
		while (quads.hasNext()) {
			Quad q = (Quad) quads.next();
			log.info("{} {} {}", q.getSubject(), q.getPredicate(), q.getObject());
		}
		assertTrue(g1.getDefaultGraph().size() == 2);
	}

	@Test
	public void friendsMultiplePatterns() throws IOException, TriplifierHTTPException {
		URL url = getClass().getClassLoader().getResource("./friends.json");
		JSONTriplifier jt = new JSONTriplifier();
		Properties properties = new Properties();
		OpBGP bgp = new OpBGP();
		bgp.getPattern().add(new Triple(new Node_Variable("a"), new Node_Variable("b"),
				ResourceFactory.createPlainLiteral("Romance").asNode()));
		bgp.getPattern().add(new Triple(new Node_Variable("a"), new Node_Variable("b"),
				ResourceFactory.createPlainLiteral("Comedy").asNode()));
		DatasetGraph g1;
		FacadeXGraphBuilder builder = getTripleFilteringBuilder(url, bgp, properties);
		properties.setProperty(IRIArgument.LOCATION.toString(),
				url.toString());
		g1 = jt.triplify(properties, builder);
		// Only four triples matching the BGP
		log.info("Size is: {}", g1.getDefaultGraph().size());
		Iterator<Quad> quads = g1.find();
		while (quads.hasNext()) {
			Quad q = (Quad) quads.next();
			log.info("{} {} {}", q.getSubject(), q.getPredicate(), q.getObject());
		}
		assertTrue(g1.getDefaultGraph().size() == 4);
	}

	@Test
	public void testEquals() {

	}
}
