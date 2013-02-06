package it.cnr.isti.melampo.index.indexing.main;

import it.cnr.isti.melampo.index.indexing.LireIndexer;
import it.cnr.isti.melampo.index.settings.LireSettings;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.melampo.vir.lire.LireReader;
import it.cnr.isti.vir.features.mpeg7.LireObject;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

public class LireIndexerMain {

	private LireReader m_sr;
	private int m_itemsToIndex;
	private LireIndexer mp7cIndex;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		try {
			File p = new File(args[0]);
			LireIndexerMain builder = new LireIndexerMain();
			builder.setVariables(p);
			builder.StartIndexing();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (VIRException e) {
			e.printStackTrace();
		}
	}

	private void setVariables(File p) throws IOException, VIRException {
		LireSettings settings = new LireSettings(p);
		m_itemsToIndex = settings.getItemsToIndex();
		mp7cIndex = new LireIndexer();
		mp7cIndex.OpenIndex(settings);
		m_sr = new LireReader(settings);
	}

	public void StartIndexing() {
		int k = m_itemsToIndex;
		if (m_itemsToIndex < 0) {
			k = m_sr.size();
		}
		for (int index = 0; index < k; index++) {
			try {
				LireObject s = m_sr.getObject(index);
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
