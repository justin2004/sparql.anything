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

package com.github.sparqlanything.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.sparqlanything.model.TriplifierHTTPException;
import com.github.sparqlanything.model.FacadeXGraphBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.sparql.core.DatasetGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sparqlanything.model.IRIArgument;
import com.github.sparqlanything.model.Triplifier;

public class TextTriplifier implements Triplifier {

	private static Logger logger = LoggerFactory.getLogger(TextTriplifier.class);

	public static final String REGEX = "txt.regex", GROUP = "txt.group", SPLIT = "txt.split";

	@Override
	public DatasetGraph triplify(Properties properties, FacadeXGraphBuilder builder)
			throws IOException, TriplifierHTTPException {

		String value;
		String root;
		String dataSourceId;
		URL url = Triplifier.getLocation(properties);
		if (url == null) {
			value = properties.getProperty(IRIArgument.CONTENT.toString(), "");
			root = Triplifier.getRootArgument(properties, Integer.toString(value.hashCode()));
			dataSourceId = builder.getMainGraphName().getURI();
		} else {
			value = readFromURL(url, properties);
			root = Triplifier.getRootArgument(properties, url.toString());
			dataSourceId = builder.getMainGraphName().getURI();
		}

//		boolean blank_nodes = Triplifier.getBlankNodeArgument(properties);

		String rootResourceId = root;

		if (logger.isTraceEnabled()) {
			logger.trace("Content:\n{}\n", value);
		}

		builder.addRoot(dataSourceId, rootResourceId);

		Pattern pattern = null;
		if (properties.containsKey(REGEX)) {
			String regexString = properties.getProperty(REGEX);
			try {
				pattern = Pattern.compile(regexString);
				// TODO flags
			} catch (Exception e) {
//				e.printStackTrace();
				logger.error(e.getMessage(), e);
				pattern = null;
			}

		}

		int group = -1;
		if (properties.contains(GROUP) && pattern != null) {
			try {
				int gr = Integer.parseInt(properties.getProperty(GROUP));
				if (gr >= 0) {
					group = gr;
				} else {
					logger.warn("Group number is supposed to be a positive integer, using default (group 0)");
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		if (pattern != null) {
			Matcher m = pattern.matcher(value);
			int count = 1;
			while (m.find()) {
				if (group > 1) {
					builder.addValue(dataSourceId, rootResourceId, count, m.group(group));
				} else {
					builder.addValue(dataSourceId, rootResourceId, count, m.group());
				}
				count++;
			}
		} else {
			logger.trace("No pattern set");

			if (properties.containsKey(SPLIT)) {

				logger.trace("Splitting regex: {}", properties.getProperty(SPLIT));
				String[] split = value.split(properties.getProperty(SPLIT));
				for (int i = 0; i < split.length; i++) {
					builder.addValue(dataSourceId, rootResourceId, i + 1, split[i]);
				}

			} else {
				builder.addValue(dataSourceId, rootResourceId, 1, value);
			}

		}
		return builder.getDatasetGraph();
	}

	private static String readFromURL(URL url, Properties properties) throws IOException, TriplifierHTTPException {
		StringWriter sw = new StringWriter();
		InputStream is = Triplifier.getInputStream(url, properties);
		IOUtils.copy(is, sw, Triplifier.getCharsetArgument(properties));
		return sw.toString();

	}

	@Override
	public Set<String> getMimeTypes() {
		return Sets.newHashSet("text/plain");
	}

	@Override
	public Set<String> getExtensions() {
		return Sets.newHashSet("txt");
	}
}
