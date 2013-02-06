package it.cnr.isti.melampo.index.indexing;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

public abstract class MelampoIndexerAbstract {
	
	protected PerFieldAnalyzerWrapper wrapper;
	
	public MelampoIndexerAbstract() {
		StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_30);
		wrapper = new PerFieldAnalyzerWrapper(sa);
	}

}
