package it.cnr.isti.melampo.index.indexing.main;

import it.cnr.isti.melampo.index.Parameters;
import it.cnr.isti.melampo.index.searching.MelampoSearcherHub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class SearcherTestMain {
	
	public MelampoSearcherHub m_lucignoloSearcher;

	
	public SearcherTestMain() {
		try {			
			m_lucignoloSearcher = new MelampoSearcherHub();
			m_lucignoloSearcher.openIndices(new File("conf"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
			SearcherTestMain testing = new SearcherTestMain();
			testing.search("0000A405824C6D23FE61759A312CDD1B36DF33CDBB4C67A81F3BE95CA8106D01");
	}
	
	public void search(String queryId) {
		ArrayList<String> values = new ArrayList<String>();
		values.add(queryId);
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(Parameters.LIRE_MP7ALL);
		
		try {
			m_lucignoloSearcher.query(values, fields, true);
			String[][] results = m_lucignoloSearcher.getResults(0, 3);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
