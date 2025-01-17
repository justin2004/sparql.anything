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

package com.github.sparqlanything.docs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.github.sparqlanything.model.FacadeXGraphBuilder;
import com.github.sparqlanything.model.Triplifier;

public class DocxTriplifier implements Triplifier {

	public final static String KEEP_PARAGRAPH = "docs.preserve-paragraphs";
	public final static String TABLE_HEADERS = "docs.table-headers";

	@Override
	public DatasetGraph triplify(Properties properties, FacadeXGraphBuilder builder) throws IOException {
		DatasetGraph dg = DatasetGraphFactory.create();

		URL url = Triplifier.getLocation(properties);
		if (url == null)
			return dg;
		String root = Triplifier.getRootArgument(properties, url);
		String dataSourceId = builder.getMainGraphName().getURI();
		String namespace = Triplifier.getNamespaceArgument(properties);
		boolean keepParagraph = Boolean.parseBoolean(properties.getProperty(KEEP_PARAGRAPH, "false"));
		boolean headers = Boolean.parseBoolean(properties.getProperty(TABLE_HEADERS, "false"));

		builder.addRoot(dataSourceId, root);

		InputStream is = url.openStream();
		try (XWPFDocument document = new XWPFDocument(is)) {
			List<XWPFParagraph> paragraphs = document.getParagraphs();

			int count = 1;
			if (keepParagraph) {
				for (XWPFParagraph para : paragraphs) {
					builder.addValue(dataSourceId, root, count,
							NodeFactory.createLiteral(para.getText(), XSDDatatype.XSDstring));
					count++;
				}

			} else {
				StringBuilder sb = new StringBuilder();
				for (XWPFParagraph para : paragraphs) {
					sb.append(para.getText());
				}
				builder.addValue(dataSourceId, root, count,
						NodeFactory.createLiteral(sb.toString(), XSDDatatype.XSDstring));
				count++;
			}

			Iterator<XWPFTable> it = document.getTables().iterator();
			while (it.hasNext()) {
				XWPFTable xwpfTable = (XWPFTable) it.next();

				String tableId = namespace + "Table_" + count;
				builder.addContainer(dataSourceId, root, count, tableId);

				LinkedHashMap<Integer, String> headers_map = new LinkedHashMap<Integer, String>();
				int rown = 0;
				Iterator<XWPFTableRow> itrows = xwpfTable.getRows().iterator();
				while (itrows.hasNext()) {
					// Header
					if (headers && rown == 0) {
						XWPFTableRow xwpfTableRow = (XWPFTableRow) itrows.next();
						Iterator<XWPFTableCell> cellIterator = xwpfTableRow.getTableCells().iterator();
						int colid = 0;
						while (cellIterator.hasNext()) {
							colid++;
							XWPFTableCell xwpfTableCell = (XWPFTableCell) cellIterator.next();
							String colstring = xwpfTableCell.getText();
							String colname = colstring.strip();
							int c = 0;
							while (headers_map.containsValue(colname)) {
								c++;
								colname += "_" + String.valueOf(c);
							}
							headers_map.put(colid, colname);
						}
					}

					// Data
					if (itrows.hasNext()) {
						XWPFTableRow xwpfTableRow = (XWPFTableRow) itrows.next();
						rown++;
						String rowId = namespace + "Table_" + count + "_Row_" + rown;
						builder.addContainer(dataSourceId, tableId, rown, rowId);
						Iterator<XWPFTableCell> cellIterator = xwpfTableRow.getTableCells().iterator();
						int colid = 0;
						while (cellIterator.hasNext()) {
							colid++;
							XWPFTableCell xwpfTableCell = (XWPFTableCell) cellIterator.next();
							String value = xwpfTableCell.getText();
							if (headers && headers_map.containsKey(colid)) {
								builder.addValue(dataSourceId, rowId, headers_map.get(colid), value);
							} else {
								builder.addValue(dataSourceId, rowId, colid, value);
							}
						}
					}

				}

				count++;
			}
		}

		is.close();

		return builder.getDatasetGraph();
	}

	@Override
	public Set<String> getMimeTypes() {
		return Sets.newHashSet("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
	}

	@Override
	public Set<String> getExtensions() {
		return Sets.newHashSet("docx");
	}

}
