package it.cnr.isti.melampo.tools;

import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.vir.file.FeaturesCollectorsArchives;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class TSSettings {

	private FeaturesCollectorsArchives fcArchives;
	private String featureClass;
	private int	m_itemsToIndex=0;
	private boolean m_createIndex=true;
	private String m_luceneIndexPath;
	
	public TSSettings() throws IOException, VIRException {
		loadProperties(new File("conf/TSconf.properties"));
	}

	public TSSettings(File propertyFile) throws IOException, VIRException {
		loadProperties(propertyFile);
	}

	private void loadProperties(File propertyFile) throws IOException,
	VIRException {
		Properties indexprops = new Properties();
		FileInputStream pfis = null;

		ArrayList<File> archives = new ArrayList<File>();
		try {
			pfis = new FileInputStream(propertyFile);
			indexprops.load(pfis);

			int index = 0;
			String archiveFile = null;
			while ((archiveFile = indexprops.getProperty("archive_" + index)) != null) {
				archives.add(new File(archiveFile));
				index++;
			}
			
			File[] archiveFiles = new File[archives.size()];
			archives.toArray(archiveFiles);
			
			//feature classes
			index = 0;
			featureClass = null;
			try {
				fcArchives = new FeaturesCollectorsArchives(archiveFiles);
			} catch (Exception e) {
				throw new BoFException(e);
			}
			
			m_itemsToIndex = Integer.parseInt(indexprops.getProperty("items_to_index"));
			m_luceneIndexPath = indexprops.getProperty("luceneIndexPath");
			
			m_createIndex = Boolean.parseBoolean(indexprops.getProperty("createIndex"));
		} finally {
			if (pfis != null) {
				pfis.close();
			}
		}
	}

	public String getFeatureClass() {
		return featureClass;
	}
	
	public FeaturesCollectorsArchives getFCArchives() {
		return fcArchives;
	}

	public int getItemsToIndex() {
		return m_itemsToIndex;
	}

	public boolean isCreateIndex() {
		return m_createIndex;
	}

	public String getLuceneIndexPath() {
		return m_luceneIndexPath;
	}
}
