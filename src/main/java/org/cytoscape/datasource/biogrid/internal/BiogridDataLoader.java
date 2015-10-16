package org.cytoscape.datasource.biogrid.internal;

/*
 * #%L
 * Cytoscape BioGrid Datasource Impl (datasource-biogrid-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DefaultDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiogridDataLoader {

	private static final Logger logger = LoggerFactory.getLogger(BiogridDataLoader.class);

	private static final String TAG = "<meta>preset,interactome</meta>";
	private static final String SAMPLE_DATA_DIR = "sampleData";
	private File dataDir;
	
	private String version = null;

	private static final Map<String, String[]> FILTER = new HashMap<String, String[]>();
	private final Set<DataSource> sources;

	static {
		FILTER.put("Homo_sapiens", new String[]{"H. sapiens", "BioGRID", "Human Interactome from BioGRID database"});
		FILTER.put("Saccharomyces_cerevisiae", new String[]{"S. cerevisiae", "BioGRID", "Yeast Interactome from BioGRID database"});
		FILTER.put("Drosophila_melanogaster", new String[]{"D. melanogaster", "BioGRID","Fly Interactome from BioGRID database"} );
		FILTER.put("Mus_musculus", new String[]{"M. musculus", "BioGRID", "Mouse Interactome from BioGRID database"});
		FILTER.put("Arabidopsis_thaliana", new String[]{"A. thaliana", "BioGRID", "Arabidopsis Interactome from BioGRID database"});
		FILTER.put("Caenorhabditis_elegans", new String[]{"C. elegans", "BioGRID", "Caenorhabditis elegans Interactome from BioGRID database"});
		FILTER.put("Escherichia_coli", new String[]{"E. coli", "BioGRID", "Escherichia coli Interactome from BioGRID database"});
		FILTER.put("Danio_rerio", new String[]{"D. rerio", "BioGRID", "Zebrafish Interactome from BioGRID database"});
	}

	public BiogridDataLoader(final File cytoscapeInstallationDir) {
		this.sources = new HashSet<DataSource>();
		this.dataDir = new File(cytoscapeInstallationDir, SAMPLE_DATA_DIR);
	}

	public void extractVersionNumber(final String fileName) {
		final String[] parts = fileName.split("-");
		if(parts == null || parts.length < 3)
			version = "Unknown";
		else {
			final String[] nextPart = parts[parts.length-1].split(".mitab");
			if(nextPart == null || nextPart.length != 1)
				version = "Unknown";
			else
				version = nextPart[0];
		}
		
		logger.info("BioGRID release version is: " + version);
	}
	
	/**
	 * Check local file is available or not.
	 * 
	 * @return true if files are already exists.
	 */
	private boolean isExist() {
		if(dataDir.isDirectory()) {
			final String[] fileNames = dataDir.list();
			if(fileNames == null || fileNames.length == 0)
				return false;
			else {
				for(String fileName: fileNames) {
					if(fileName.contains("mitab"))
						return true;
				}
			}
		}
		return false;
	}


	public void processFiles() throws IOException {
		if(!isExist())
			return;
		// Just need to create from existing files.
		final File[] dataFiles = dataDir.listFiles();

		for (File file : dataFiles) {
			final String[] data = createName(file.getName());
			if(data == null) continue;
			logger.info("* Processing local organism network file: " + file.getName());
			if(version == null) {
				extractVersionNumber(file.getName());
			}
			final DataSource ds = new DefaultDataSource(
					data[0], data[1], TAG + data[2] + " Release " + version, DataCategory.NETWORK, file.toURI().toURL());
			sources.add(ds);
		}
		return;
	}

	private String[] createName(String name) {
		for (String key : FILTER.keySet()) {
			if (name.contains(key)) {
				return FILTER.get(key);
			}
		}

		return null;
	}
	
	public Set<DataSource> getDataSources() {
		return this.sources;
	}
	
	public String getVersion() {
		return this.version;
	}
}