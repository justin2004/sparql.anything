package com.github.spiceh2020.sparql.anything.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.escape.UnicodeEscaper;
import com.google.common.net.PercentEscaper;

public interface Triplifier {

	static final String METADATA_GRAPH_IRI = "http://sparql.xyz/facade-x/data/metadata";
	static final String AUDIT_GRAPH_IRI = "http://sparql.xyz/facade-x/data/audit";
	static final String XYZ_NS = "http://sparql.xyz/facade-x/data/";
	static final String FACADE_X_CONST_NAMESPACE_IRI = "http://sparql.xyz/facade-x/ns/";
	static final String FACADE_X_TYPE_ROOT = FACADE_X_CONST_NAMESPACE_IRI + "root";
	static final String FACADE_X_TYPE_PROPERTIES = FACADE_X_CONST_NAMESPACE_IRI + "properties";

	static final Logger log = LoggerFactory.getLogger(Triplifier.class);

	public DatasetGraph triplify(Properties properties) throws IOException;

	default public DatasetGraph triplify(Properties properties, Op subOp) throws IOException {
		return triplify(properties);
	}

	public Set<String> getMimeTypes();

	public Set<String> getExtensions();

	static boolean getBlankNodeArgument(Properties properties) {
		boolean blank_nodes = true;
		if (properties.containsKey(IRIArgument.BLANK_NODES.toString())) {
			blank_nodes = Boolean.parseBoolean(properties.getProperty(IRIArgument.BLANK_NODES.toString()));
		}
		return blank_nodes;
	}

	static Charset getCharsetArgument(Properties properties) {
		Charset charset = null;
		try {
			charset = Charset.forName(properties.getProperty(IRIArgument.CHARSET.toString(), "UTF-8"));
		} catch (Exception e) {
			log.warn("Unsupported charset format: '{}', using UTF-8.",
					properties.getProperty(IRIArgument.CHARSET.toString()));
			charset = StandardCharsets.UTF_8;
		}
		return charset;
	}

	static String getRootArgument(Properties properties, URL url) {
		return getRootArgument(properties, url.toString());
	}

	static String getRootArgument(Properties properties, String url) {
		String root = null;
		try {
			root = properties.getProperty(IRIArgument.ROOT.toString());
			if (root != null && !root.trim().equals("")) {
				return root;
			}
		} catch (Exception e) {
			log.warn("Unsupported parameter value for 'root': '{}', using default (location + '#').", root);
		}
		return url + "#";
	}

	static String getNamespaceArgument(Properties properties) {
		String namespace = null;
		try {
			namespace = properties.getProperty(IRIArgument.NAMESPACE.toString());
			if (namespace != null && !namespace.trim().equals("")) {
				return namespace;
			}
		} catch (Exception e) {
			log.warn("Unsupported parameter value for 'namespace': '{}', using default ({}}).", namespace, XYZ_NS);
		}
		return XYZ_NS;
	}

	static UnicodeEscaper basicEscaper = new PercentEscaper("%", false);

	public static String toSafeURIString(String s) {
		return basicEscaper.escape(s);
	}

	public static URL instantiateURL(String urlLocation) throws MalformedURLException {
		URL url;
		try {
			url = new URL(urlLocation);
		} catch (MalformedURLException u) {
			log.trace("Malformed url interpreting as file");
			url = new File(urlLocation).toURI().toURL();
		}
		return url;
	}

	static URL getLocation(Properties properties) throws MalformedURLException {
		if (properties.containsKey(IRIArgument.LOCATION.toString())) {
			return instantiateURL(properties.getProperty(IRIArgument.LOCATION.toString()));
		}
		return null;
	}

	public static InputStream getInputStream(URL url, Properties properties, Charset charset)
			throws IOException, ArchiveException {
			URLConnection con = url.openConnection();
			con.setRequestProperty("SomeHeader","HeaderValue");
		if (!properties.containsKey(IRIArgument.FROM_ARCHIVE.toString()))
			return con.getInputStream();
		URL urlArchive = instantiateURL(properties.getProperty(IRIArgument.FROM_ARCHIVE.toString()));
		return ResourceManager.getInstance().getInputStreamFromArchive(urlArchive,
				properties.getProperty(IRIArgument.LOCATION.toString()), charset);
	}

}
