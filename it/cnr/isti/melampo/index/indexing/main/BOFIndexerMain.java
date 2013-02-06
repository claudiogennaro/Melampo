package it.cnr.isti.melampo.index.indexing.main;

import it.cnr.isti.melampo.index.indexing.BOFIndexer;
import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.vir.bof.BoFReader;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

public class BOFIndexerMain {

	private BoFReader m_bofr;
	private int m_itemsToIndex;
	private BOFIndexer bofIndex;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		try {
			File p = new File(args[0]);
			BOFIndexerMain builder = new BOFIndexerMain();
			builder.setVariables(p);
			builder.StartIndexing();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (VIRException e) {
			e.printStackTrace();
		}
	}

	private void setVariables(File p) throws IOException, VIRException {
		Settings settings = new Settings(p);
		m_itemsToIndex = settings.getItemsToIndex();
		m_bofr = new BoFReader(p);
		bofIndex = new BOFIndexer();
		bofIndex.OpenIndex(p);
	}

	public void StartIndexing() {
		int k = m_itemsToIndex;
		if (m_itemsToIndex < 0) {
			k = m_bofr.size();
		}
		for (int index = 0; index < k; index++) {
			try {
				String bofString = m_bofr.getBof(index);
				String id = m_bofr.getID();

				bofIndex.addDocument(bofString, id);

				System.out.println("indexed doc n. " + (index + 1));
			} catch (Exception e1) {
				System.out.println("error indexing doc n. " + (index + 1));
				e1.printStackTrace();
			}
		}
		try {
			System.out.println("closing index");
			bofIndex.closeIndex();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
