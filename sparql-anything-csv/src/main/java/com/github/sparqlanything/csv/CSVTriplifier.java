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

package com.github.sparqlanything.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

import com.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sparqlanything.model.FacadeXGraphBuilder;
import com.github.sparqlanything.model.Triplifier;

public class CSVTriplifier implements Triplifier {
	private static final Logger log = LoggerFactory.getLogger(CSVTriplifier.class);
	public final static String PROPERTY_FORMAT = "csv.format", PROPERTY_HEADERS = "csv.headers";
	public final static String PROPERTY_DELIMITER = "csv.delimiter";
	public final static String PROPERTY_NULLSTRING = "csv.null-string";

	@Override
	public DatasetGraph triplify(Properties properties, FacadeXGraphBuilder builder) throws IOException, TriplifierHTTPException{

		URL url = Triplifier.getLocation(properties);

		if (url == null)
			return DatasetGraphFactory.create();

		// TODO Support all flavour of csv types
		CSVFormat format;
		try {
			format = CSVFormat.valueOf(properties.getProperty(PROPERTY_FORMAT, "DEFAULT"));
		} catch (Exception e) {
			log.warn("Unsupported csv format: '{}', using default.", properties.getProperty(PROPERTY_FORMAT));
			format = CSVFormat.DEFAULT;
		}
		if(properties.containsKey(PROPERTY_NULLSTRING)){
			format = format.withNullString(properties.getProperty(PROPERTY_NULLSTRING)) ;
		}
		if(properties.containsKey(PROPERTY_DELIMITER)){
			System.out.println("Setting delimiter to " + properties.getProperty(PROPERTY_DELIMITER));
			if(properties.getProperty(PROPERTY_DELIMITER).length() != 1){
				throw new IOException("Bad value for property " + PROPERTY_DELIMITER + ": string length must be 1, " + Integer.toString(properties.getProperty(PROPERTY_DELIMITER).length()) + " given");
			}
			format = format.withDelimiter(properties.getProperty(PROPERTY_DELIMITER).charAt(0)) ;
		}

		String root = Triplifier.getRootArgument(properties, url);
		Charset charset = Triplifier.getCharsetArgument(properties);
		boolean blank_nodes = Triplifier.getBlankNodeArgument(properties);
		String namespace = Triplifier.getNamespaceArgument(properties);

		boolean headers;
		try {
			headers = Boolean.valueOf(properties.getProperty(PROPERTY_HEADERS, "false"));
		} catch (Exception e) {
			log.warn("Unsupported value for csv.headers: '{}', using default (false).",
					properties.getProperty(PROPERTY_HEADERS));
			headers = false;
		}
		Reader in = null;
		
		String dataSourceId = url.toString();
		String containerRowPrefix = url.toString() + "#row";
		// Add type Root
		builder.addRoot(dataSourceId, root);
		try {

			final InputStream is = Triplifier.getInputStream(url, properties);
			in = new InputStreamReader(new BOMInputStream(is), charset);

			Iterable<CSVRecord> records = format.parse(in);
			int rown = 0;
			LinkedHashMap<Integer, String> headers_map = new LinkedHashMap<Integer, String>();
			Iterator<CSVRecord> recordIterator = records.iterator();
			while (recordIterator.hasNext()) {
				// Header
				if (headers && rown == 0) {
					CSVRecord record = recordIterator.next();
					Iterator<String> columns = record.iterator();
					int colid = 0;
					while (columns.hasNext()) {
						colid++;
						String colstring = columns.next();
						String colname = colstring.strip();

						int c = 0;
						while (headers_map.containsValue(colname)) {
							c++;
							colname += "_" + String.valueOf(c);
						}
						log.trace("adding colname >{}<", colname);
						headers_map.put(colid, colname);
					}

				}
				// Data
				if (recordIterator.hasNext()) {
					// Rows
					rown++;
					String rowContainerId = containerRowPrefix + rown;
					builder.addContainer(dataSourceId, root, rown, rowContainerId);
					CSVRecord record = recordIterator.next();
					Iterator<String> cells = record.iterator();
					int cellid = 0;
					while (cells.hasNext()) {
						String value = cells.next();
						cellid++;
						if (headers && headers_map.containsKey(cellid)) {
							String colname = URLEncodedUtils.formatSegments(headers_map.get(cellid)).substring(1);
							if(value != null){
								builder.addValue(dataSourceId, rowContainerId, colname, value);
							}
						} else {
							if(value != null){
								builder.addValue(dataSourceId, rowContainerId, cellid, value);
							}
						}
					}
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("{} :: {}", e.getMessage(), url);
			throw new IOException(e);
		}
		return builder.getDatasetGraph();
	}

	@Override
	public Set<String> getMimeTypes() {
		return Sets.newHashSet("text/csv");
	}

	@Override
	public Set<String> getExtensions() {
		return Sets.newHashSet("csv");
	}
}
