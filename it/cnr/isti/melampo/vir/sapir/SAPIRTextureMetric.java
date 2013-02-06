package it.cnr.isti.melampo.vir.sapir;

import it.cnr.isti.vir.features.FeatureClassCollector;
import it.cnr.isti.vir.features.IFeaturesCollector;
import it.cnr.isti.vir.features.mpeg7.vd.ColorLayout;
import it.cnr.isti.vir.features.mpeg7.vd.ColorStructure;
import it.cnr.isti.vir.features.mpeg7.vd.EdgeHistogram;
import it.cnr.isti.vir.features.mpeg7.vd.HomogeneousTexture;
import it.cnr.isti.vir.features.mpeg7.vd.ScalableColor;
import it.cnr.isti.vir.similarity.metric.Metric;

public class SAPIRTextureMetric implements Metric<IFeaturesCollector> {

	
	private static long distCount = 0;
	public static final FeatureClassCollector reqFeatures = new FeatureClassCollector(
			ColorLayout.class,
			ColorStructure.class,
			ScalableColor.class,
			EdgeHistogram.class,
			HomogeneousTexture.class );
	
	protected static final int htOption = HomogeneousTexture.N_OPTION;
	
	public final long getDistCount() {
		return distCount;
	}

	public static final double[] wSAPIR = {
		1.5  * 1.0/300.0,  //CL
		2.5  * 1.0/10200.0, //CS
		4.5  * 1.0/68.0, //EH
		0.5  * 1.0/25.0,  //HT
		2.5  * 1.0/3000.0  //SC
	};
	
	@Override
	public FeatureClassCollector getRequestedFeaturesClasses() {		
		return reqFeatures;
	}
	
/*	public final double distance(Image img1, Image img2) {
		return distance(img1.getFeatures(), img2.getFeatures());
	}*/
	
	public final double distance(IFeaturesCollector f1, IFeaturesCollector f2 ) {
	
		return distance(f1,f2, Double.MAX_VALUE);
	}	
	
	public final double distance(IFeaturesCollector f1, IFeaturesCollector f2, double max ) {
		distCount++;
		double dist = 0;

		//EH
		dist += wSAPIR[2] * EdgeHistogram.mpeg7XMDistance( (EdgeHistogram) f1.getFeature(EdgeHistogram.class), (EdgeHistogram) f2.getFeature(EdgeHistogram.class) );
		if ( dist > max ) return -dist;
		
		//HT
		dist += wSAPIR[3] * HomogeneousTexture.mpeg7XMDistance( (HomogeneousTexture) f1.getFeature(HomogeneousTexture.class), (HomogeneousTexture) f2.getFeature(HomogeneousTexture.class), htOption );
				
		return dist;
	}
	
	public String toString() {
		return this.getClass().toString();
	}
	
}
