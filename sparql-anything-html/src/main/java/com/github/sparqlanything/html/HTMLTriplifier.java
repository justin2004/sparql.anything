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

package com.github.sparqlanything.html;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Set;

import com.github.sparqlanything.model.TriplifierHTTPException;
import com.github.sparqlanything.model.FacadeXGraphBuilder;
import org.apache.jena.ext.com.google.common.collect.Sets;
//import org.apache.jena.graph.NodeFactory;
//import org.apache.jena.query.DatasetFactory;
//import org.apache.jena.rdf.model.AnonId;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
//import org.apache.jena.rdf.model.Resource;
//import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
//import org.apache.jena.vocabulary.RDF;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import com.github.sparqlanything.model.Triplifier;

public class HTMLTriplifier implements Triplifier {

	private static final Logger log = LoggerFactory.getLogger(HTMLTriplifier.class);
	private static final String PROPERTY_SELECTOR = "html.selector";
	private static final String PROPERTY_BROWSER = "html.browser";
	private static final String HTML_NS = "http://www.w3.org/1999/xhtml#";
	private static final String DOM_NS = "https://html.spec.whatwg.org/#";

	@Override
	public DatasetGraph triplify(Properties properties, FacadeXGraphBuilder builder) throws IOException, TriplifierHTTPException {

		URL url = Triplifier.getLocation(properties);

		if (url == null)
			return DatasetGraphFactory.create();


		String root = Triplifier.getRootArgument(properties, url);
		Charset charset = Triplifier.getCharsetArgument(properties);
		boolean blank_nodes = Triplifier.getBlankNodeArgument(properties);
		String namespace = Triplifier.getNamespaceArgument(properties);

		String selector = properties.getProperty(PROPERTY_SELECTOR, ":root");

		log.trace("namespace {}\n root {}\ncharset {}\nselector {}", namespace, root, charset, selector);

		Document doc;
		// If location is a http or https, raise exception if status is not 200
		log.debug("Loading URL: {}", url);


		if(properties.containsKey(PROPERTY_BROWSER)){
			doc = Jsoup.parse(useBrowserToNavigate(url.toString(), properties.getProperty(PROPERTY_BROWSER)));
		} else {
			doc = Jsoup.parse(Triplifier.getInputStream(url, properties), charset.toString(), url.toString());
		}

//		Model model = ModelFactory.createDefaultModel();
		// log.info(doc.title());
		Elements elements = doc.select(selector);
//		Resource rootResource = null;
		String rootResourceId = null;
		String dataSourceId = url.toString();
		if (elements.size() > 1) {
			// Create a root container
			rootResourceId = root;
			builder.addRoot(dataSourceId, rootResourceId);
//			rootResource = model.createResource(root);
//			rootResource.addProperty(RDF.type, model.createResource(Triplifier.FACADE_X_TYPE_ROOT));
		}
		int counter = 0;
		for (Element element : elements) {
			counter++;
			if (elements.size() > 1) {
				// link to root container
//				Resource elResource = toResource(model, element, blank_nodes, namespace);
//				rootResource.addProperty(RDF.li(counter), elResource);
				builder.addContainer(dataSourceId, rootResourceId, counter, toResourceId(element, blank_nodes));
			} else {
				// Is root container
//				model.add(toResource(model, element, blank_nodes, namespace), RDF.type,
//						model.createResource(Triplifier.FACADE_X_TYPE_ROOT));
				rootResourceId = toResourceId(element, blank_nodes);
				builder.addRoot(dataSourceId, rootResourceId);
			}
			try {
				populate(builder, dataSourceId, element, blank_nodes, namespace);
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
		}
//		DatasetGraph dg = DatasetFactory.create(model).asDatasetGraph();
//		dg.addGraph(NodeFactory.createURI(url.toString()), model.getGraph());
//		return dg;
		return builder.getDatasetGraph();
	}

	private void populate(FacadeXGraphBuilder builder, String dataSourceId, Element element, boolean blank_nodes, String namespace) throws URISyntaxException {

		String tagName = element.tagName(); // tagname is the type
//		Resource resource = toResource(model, element, blank_nodes, namespace);
		String resourceId = toResourceId(element, blank_nodes);
		String innerHtml = element.html();
		if (!innerHtml.trim().equals("")) {
			builder.addValue(dataSourceId, resourceId, new URI(DOM_NS + "innerHTML"), innerHtml);
//			resource.addProperty(ResourceFactory.createProperty(DOM_NS + "innerHTML"), innerHtml);
		}
		String innerText = element.select("*").text();
		if (!innerText.trim().equals("")) {
			builder.addValue(dataSourceId, resourceId, new URI(DOM_NS + "innerText"), innerText);
//			resource.addProperty(ResourceFactory.createProperty(DOM_NS + "innerText"), innerText);
		}
//		resource.addProperty(RDF.type, ResourceFactory.createResource(HTML_NS + tagName));
		builder.addType(dataSourceId,resourceId, new URI(HTML_NS + tagName));
		// attributes
		for (Attribute attribute : element.attributes()) {
			String key = attribute.getKey();
			String value = attribute.getValue();
//			resource.addProperty(ResourceFactory.createProperty(HTML_NS + key), model.createLiteral(value));
			builder.addValue(dataSourceId, resourceId, new URI(HTML_NS + key), value);
		}
		// Children
		int counter = 0;

		for (Node child : element.childNodes()) {
			if (child.outerHtml().trim().equals(""))
				continue;
			counter++;
			if (child instanceof Element) {
//				resource.addProperty(RDF.li(counter), toResource(model, (Element) child, blank_nodes, namespace));
				builder.addContainer(dataSourceId, resourceId, counter, toResourceId((Element)  child, blank_nodes));
				populate(builder, dataSourceId, (Element) child, blank_nodes, namespace);
			} else {
//				resource.addProperty(RDF.li(counter), child.outerHtml());
				builder.addValue(dataSourceId, resourceId, counter, child.outerHtml());
			}
		}

	}

	private static final String localName(Element element) {
		String tagName = element.tagName().replace(':', '|');
		StringBuilder selector = new StringBuilder(tagName);
		String classes = StringUtil.join(element.classNames(), ".");
		if (classes.length() > 0) {
			selector.append('.').append(classes);
		}

		if (element.parent() != null && !(element.parent() instanceof Document)) {
			selector.insert(0, " > ");
			if (element.parent().select(selector.toString()).size() > 1) {
				selector.append(String.format(":nth-child(%d)", element.elementSiblingIndex() + 1));
			}

			selector.insert(0, localName(element.parent()));
		}
		return selector.toString().replaceAll(" > ", "/").replaceAll(":nth-child\\(([0-9]+)\\)", ":$1");
	}

//	private Resource toResource(Model model, Element element, boolean blankNodes, String namespace) {
//		if (blankNodes == true) {
//			return model.createResource(new AnonId(Integer.toHexString(element.hashCode())));
//		} else {
//			String ln = localName(element);
//			log.info(ln);
//			return model.createResource(namespace + ln);
//		}
//	}

	private String toResourceId(Element element, boolean blankNodes) {
		if (blankNodes == true) {
			return Integer.toHexString(element.hashCode());
		} else {
			String ln = localName(element);
			log.info(ln);
			return ln;
		}
	}

	private String useBrowserToNavigate(String url, String browserProperty){
		// navigate to the page at url and return the HTML as a string 
		Playwright playwright = Playwright.create() ;
		Browser browser ;
		switch (browserProperty){
			case "chromium":
				log.info("using chromium");
				browser = playwright.chromium().launch();
				break;
			case "firefox":
				log.info("using firefox");
				browser = playwright.firefox().launch();
				break;
			case "webkit":
				log.info("using webkit");
				browser = playwright.webkit().launch();
				break;
			default:
				log.warn("\"" + browserProperty + "\"" + " is not a valid browser -- defaulting to chromium");
				browser = playwright.chromium().launch();
				break;
		}
		BrowserContext context = browser.newContext() ;
		Page page = context.newPage() ;
		page.navigate(url);
		String htmlFromBrowser = page.content();
		browser.close();
		return htmlFromBrowser;
	}

	@Override
	public Set<String> getMimeTypes() {
		return Sets.newHashSet("text/html");
	}

	@Override
	public Set<String> getExtensions() {
		return Sets.newHashSet("html");
	}
}
