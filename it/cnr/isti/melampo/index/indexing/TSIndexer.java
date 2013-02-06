package it.cnr.isti.melampo.index.indexing;

import it.cnr.isti.melampo.index.TSFieldAdder;
import it.cnr.isti.melampo.index.Parameters;
import it.cnr.isti.melampo.tools.TSSettings;
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

public class TSIndexer extends MelampoIndexerAbstract {

	private IndexWriter m_w;
	private TSFieldAdder m_bfa = null;

	private boolean m_create;
	private String m_lucenePath;

	public void OpenIndex(File propertyFile) throws IOException {
		Directory index = null;
		TSSettings settings = null;
		try {
			settings = new TSSettings(propertyFile);
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WhitespaceAnalyzer wsa = new WhitespaceAnalyzer();
		wrapper.addAnalyzer(Parameters.TS, wsa);

		m_create = settings.isCreateIndex();

		// to be fixed
		m_lucenePath = settings.getLuceneIndexPath();

		File f = new File(m_lucenePath);

		index = FSDirectory.open(f);

		m_w = new IndexWriter(index, wrapper, m_create,
				IndexWriter.MaxFieldLength.UNLIMITED);

		m_bfa = new TSFieldAdder();
	}

	public void addDocument(String doc, String docId) throws BoFException,
			CorruptIndexException, IOException {
		Document document = new Document();

		// BOF
		m_bfa.AddTSStringField(document, doc);

		// ID
		m_bfa.AddTSIDField(document, docId);
		m_w.addDocument(document);

		System.out.println("indexed doc id. " + docId);
	}

	public void closeIndex() throws CorruptIndexException, IOException {
		if (m_w != null) {
			m_w.close();
		}
	}
}
