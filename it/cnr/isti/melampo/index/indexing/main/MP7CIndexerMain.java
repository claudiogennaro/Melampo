package it.cnr.isti.melampo.index.indexing.main;

import it.cnr.isti.melampo.index.indexing.MP7CIndexer;
import it.cnr.isti.melampo.index.settings.MP7CSettings;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.melampo.vir.sapir.SapirReader;
import it.cnr.isti.vir.features.mpeg7.SAPIRObject;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

public class MP7CIndexerMain {

	private SapirReader m_sr;
	private int m_itemsToIndex;
	private MP7CIndexer mp7cIndex;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		try {
			File p = new File(args[0]);
			MP7CIndexerMain builder = new MP7CIndexerMain();
			builder.setVariables(p);
			builder.StartIndexing();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (VIRException e) {
			e.printStackTrace();
		}
	}

	private void setVariables(File p) throws IOException, VIRException {
		MP7CSettings settings = new MP7CSettings(p);
		m_itemsToIndex = settings.getItemsToIndex();
		mp7cIndex = new MP7CIndexer();
		mp7cIndex.OpenIndex(p);
		m_sr = new SapirReader(p);
	}

	public void StartIndexing() {
		int k = m_itemsToIndex;
		if (m_itemsToIndex < 0) {
			k = m_sr.size();
		}
		for (int index = 0; index < k; index++) {
			try {
				SAPIRObject s = m_sr.getSapirObject(index);
				String id = m_sr.getID();

				mp7cIndex.addDocument(s, id);

				System.out.println("indexed doc n. " + (index + 1));
			} catch (Exception e1) {
				System.out.println("error indexing doc n. " + (index + 1));
				e1.printStackTrace();
			}
		}
		try {
			System.out.println("closing index");
			mp7cIndex.closeIndex();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
