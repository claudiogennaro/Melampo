package it.cnr.isti.melampo.index.indexing;

import it.cnr.isti.melampo.index.BoFFieldAdder;
import it.cnr.isti.melampo.index.Parameters;
import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class BOFIndexer extends MelampoIndexerAbstract {

	private IndexWriter m_w;
	private BoFFieldAdder m_bfa = null;

	private boolean m_create;
	private String m_lucenePath;

	public void OpenIndex(File propertyFile) throws IOException {
		Directory index = null;
		Settings settings = null;
		try {
			settings = new Settings(propertyFile);
		} catch (VIRException e) {
			e.printStackTrace();
		}

		WhitespaceAnalyzer wsa = new WhitespaceAnalyzer();
		wrapper.addAnalyzer(Parameters.BOFFIELD, wsa);

		m_create = settings.isCreateIndex();

		// to be fixed
		m_lucenePath = settings.getLuceneIndexPathBOF();

		File f = new File(m_lucenePath);

		index = FSDirectory.open(f);

		m_w = new IndexWriter(index, wrapper, m_create,
				IndexWriter.MaxFieldLength.UNLIMITED);

		m_bfa = new BoFFieldAdder();
	}

	public void addDocument(String doc, String docId) throws BoFException,
			CorruptIndexException, IOException {
		Document document = new Document();

		// BOF
		m_bfa.AddBofStringField(document, doc);

		// ID
		m_bfa.AddBofIDField(document, docId);
		m_w.addDocument(document);

		System.out.println("indexed doc id. " + docId);
	}

	public void closeIndex() throws CorruptIndexException, IOException {
		if (m_w != null) {
			m_w.close();
		}
	}
}
