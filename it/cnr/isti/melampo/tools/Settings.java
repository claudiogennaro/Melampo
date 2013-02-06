package it.cnr.isti.melampo.tools;

import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.vir.features.FeatureClassCollector;
import it.cnr.isti.vir.features.bof.LFWords;
import it.cnr.isti.vir.file.FeaturesCollectorsArchives;
import it.cnr.isti.vir.readers.CoPhIRv2Reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Settings {

	private FeaturesCollectorsArchives bofArchives;
	private FeaturesCollectorsArchives fcArchives;
	private LFWords fWords;
	private String featureClass;
	private int	m_itemsToIndex=0;
	private boolean m_createIndex=true;
	private String m_luceneIndexPathBOF;
	private String m_luceneIndexPathMPG7;
	private String m_luceneIndexPathMPG7C;
	private String m_luceneIndexPathMetadata;
	private int m_topBofQuery;
	private int m_nPivots;
	private String m_PivotsPath;
	private int m_toppivsI;
	private int m_toppivsQ;
	private String m_metadataDir;
	
	private boolean isScaled = false;

	public Settings() throws IOException, VIRException {
		loadProperties(new File("conf/conf.properties"));
	}

	public int getToppivsI() {
		return m_toppivsI;
	}

	public Settings(File propertyFile) throws IOException, VIRException {
		loadProperties(propertyFile);
	}

	private void loadProperties(File propertyFile) throws IOException,
	VIRException {
		Properties indexprops = new Properties();
		FileInputStream pfis = null;

		ArrayList<File> bofs = new ArrayList<File>();
		ArrayList<File> archives = new ArrayList<File>();
		try {
			pfis = new FileInputStream(propertyFile);
			indexprops.load(pfis);

			//BoFs
			int index = 0;
			String bofFile = null;
			while ((bofFile = indexprops.getProperty("bof_" + index)) != null) {
				bofs.add(new File(bofFile));
				index++;
			}
			
			File[] bofFiles = new File[bofs.size()];
			bofs.toArray(bofFiles);
			
			//archives
			index = 0;
			String archiveFile = null;
			while ((archiveFile = indexprops.getProperty("archive_" + index)) != null) {
				archives.add(new File(archiveFile));
				index++;
			}
			
			File[] archiveFiles = new File[archives.size()];
			archives.toArray(archiveFiles);

			String wordsFile = indexprops.getProperty("words");
			
			//feature classes
			index = 0;
			featureClass = null;
			FeatureClassCollector fcc = null;
			try {
			if ((featureClass = indexprops.getProperty("featureClassImpl_" + index)) != null) {
				fcc = new FeatureClassCollector(Class.forName(featureClass));
				index++;
				while ((featureClass = indexprops.getProperty("featureClassImpl_" + index)) != null) {
					fcc.add(Class.forName(featureClass));
					index++;
				}
			} else {
				throw new VIRException("Error, unable to retrieve featureClassImpl_" +index + " parameter");
			}
			CoPhIRv2Reader.setFeatures(fcc);
			
			String bofScaledParam = indexprops.getProperty("bofScaled");
			if (bofScaledParam != null && !bofScaledParam.trim().equals("")) {
				isScaled = Boolean.parseBoolean(bofScaledParam);
			}

				bofArchives = new FeaturesCollectorsArchives(bofFiles);
				fcArchives = new FeaturesCollectorsArchives(archiveFiles);
				
				fWords = new LFWords(new File(wordsFile));
			} catch (Exception e) {
				throw new BoFException(e);
			}
			
			m_itemsToIndex = Integer.parseInt(indexprops.getProperty("items_to_index"));
			m_luceneIndexPathBOF = indexprops.getProperty("luceneIndexPathBOF");
			m_luceneIndexPathMPG7 = indexprops.getProperty("luceneIndexPathMPG7");
			m_luceneIndexPathMPG7C = indexprops.getProperty("luceneIndexPathMPG7C");
			m_luceneIndexPathMetadata = indexprops.getProperty("luceneIndexPathMetadata");
			
			m_createIndex = Boolean.parseBoolean(indexprops.getProperty("createIndex"));
			m_topBofQuery = Integer.parseInt(indexprops.getProperty("top_bof_query"));
			m_nPivots = Integer.parseInt(indexprops.getProperty("num_of_pivots"));
			m_PivotsPath = indexprops.getProperty("PivotsPath");
			m_toppivsI = Integer.parseInt(indexprops.getProperty("maxpivsIndex"));
			m_toppivsQ = Integer.parseInt(indexprops.getProperty("maxpivsQuery"));
			
			m_metadataDir = indexprops.getProperty("metadata");
			
		} finally {
			if (pfis != null) {
				pfis.close();
			}
		}
	}

	public String getFeatureClass() {
		return featureClass;
	}

	public FeaturesCollectorsArchives getBofArchives() {
		return bofArchives;
	}
	
	public FeaturesCollectorsArchives getFCArchives() {
		return fcArchives;
	}

	public LFWords getfWords() {
		return fWords;
	}

	public int getItemsToIndex() {
		return m_itemsToIndex;
	}

	public boolean isCreateIndex() {
		return m_createIndex;
	}
	
	public boolean isBoFScaled() {
		return isScaled;
	}

	public String getLuceneIndexPathBOF() {
		return m_luceneIndexPathBOF;
	}

	public String getLuceneIndexPathMPG7() {
		return m_luceneIndexPathMPG7;
	}
	
	public int getTopBofQuery() {
		return m_topBofQuery;
	}

	public int getnPivots() {
		return m_nPivots;
	}

	public String getPivotsPath() {
		return m_PivotsPath;
	}

	public int getToppivsQ() {
		return m_toppivsQ;
	}

	public String getLuceneIndexPathMetadata() {
		return m_luceneIndexPathMetadata;
	}
	
	public String getMetadataDir() {
		return m_metadataDir;
	}

	public String getLuceneIndexPathMPG7C() {
		return m_luceneIndexPathMPG7C;
	}
}
