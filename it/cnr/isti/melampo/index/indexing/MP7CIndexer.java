package it.cnr.isti.melampo.index.indexing;

import it.cnr.isti.melampo.index.Parameters;
import it.cnr.isti.melampo.index.SAPIRObjectFieldAdder;
import it.cnr.isti.melampo.index.settings.MP7CSettings;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.vir.features.mpeg7.SAPIRObject;
import it.cnr.isti.vir.similarity.metric.SAPIRMetric;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class MP7CIndexer extends MelampoIndexerAbstract {

	private IndexWriter m_w;
	private SAPIRObjectFieldAdder m_sfaALL = null;

	private boolean m_create;
	private String m_lucenePath;
	private int m_toppivs;
	private int m_nPivots;
	private String m_PivFile;

	public void OpenIndex(File propertyFile) throws IOException, VIRException {

		Directory index = null;
		MP7CSettings settings = null;
		try {
			settings = new MP7CSettings(propertyFile);
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WhitespaceAnalyzer wsa = new WhitespaceAnalyzer();
		wrapper.addAnalyzer(Parameters.MP7ALL, wsa);

		m_create = settings.isCreateIndex();

		// to be fixed
		m_lucenePath = settings.getLuceneIndexPathMPG7C();

		File f = new File(m_lucenePath);

		index = FSDirectory.open(f);

		m_w = new IndexWriter(index, wrapper, m_create,
				IndexWriter.MaxFieldLength.UNLIMITED);

		m_nPivots = settings.getnPivots();
		m_PivFile = settings.getPivotsPath();

		m_sfaALL = new SAPIRObjectFieldAdder(new SAPIRMetric());
		m_sfaALL.loadBinPivots(m_PivFile, m_nPivots);

		m_toppivs = settings.getToppivsI();
	}

	public void addDocument(SAPIRObject s, String docId) throws
			CorruptIndexException, IOException, VIRException {
			Document doc = new Document();

			// MPEG-7
			m_sfaALL.addFieldToDoc(doc, s, m_toppivs);

			// ID
			m_sfaALL.AddIDField(doc, docId);
			System.out.println("id " + docId);

			m_w.addDocument(doc);

			System.out.println("indexed doc " + docId);
	}

	public void closeIndex() throws CorruptIndexException, IOException {
		if (m_w != null) {
			m_w.close();
		}
	}

}
