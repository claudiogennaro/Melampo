package it.cnr.isti.melampo.vir.bof;

import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.tools.Tools;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.vir.features.FeaturesCollectorHT;
import it.cnr.isti.vir.features.IFeaturesCollector;
import it.cnr.isti.vir.features.bof.BoF_LF_OriAndScale;
import it.cnr.isti.vir.id.IDString;
import it.cnr.isti.vir.readers.CoPhIRv2Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

public class BoFSearcher {

	private Settings settings;

	public BoFSearcher() throws IOException, VIRException {
		settings = new Settings();
	}

	public BoFSearcher(File propertyFile) throws IOException, VIRException {
		settings = new Settings(propertyFile);
	}

	public int[] getBoFArray(String query, boolean isQueryID, int maxBagLength) throws BoFException {
		int[] bofIndexes = null;
		int[] result = null;
		IFeaturesCollector featureColl;
		BoF_LF_OriAndScale bofFeature;
		try {
			if (isQueryID) {
				featureColl = settings.getBofArchives()
						.get(new IDString(query));
				bofFeature = (BoF_LF_OriAndScale) featureColl
						.getFeature(BoF_LF_OriAndScale.class);
			} else {
				featureColl = new FeaturesCollectorHT(CoPhIRv2Reader
						.getObj(new BufferedReader(new StringReader(query))));
				if ((bofFeature = (BoF_LF_OriAndScale) featureColl
						.getFeature(BoF_LF_OriAndScale.class)) == null) {
					bofFeature = new BoF_LF_OriAndScale(featureColl, settings
							.getfWords());
				}
			}
			if (bofFeature != null) {
				if (settings.isBoFScaled()) {
					bofFeature.orderByScale();
					System.out.println("ciao comprati arrapao");
				}
				bofIndexes = bofFeature.getBagIndexes();
			}
			
			if (maxBagLength != -1) {
				result = new int[Math.min(bofIndexes.length, maxBagLength)];
				System.arraycopy(bofIndexes, 0, result, 0, result.length);
			} else {
				result = bofIndexes;
			}
			
		} catch (Exception e) {
			throw new BoFException(e);
		}
		return result;
	}

	public String[] getBoFAsStringArray(String query, boolean isQueryID, int maxBagLength)
			throws BoFException {
		String[] bofIndexesAsString = null;
		int[] bofIndexes = getBoFArray(query, isQueryID, maxBagLength);
		if (bofIndexes != null) {
			bofIndexesAsString = new String[bofIndexes.length];
			for (int i = 0; i < bofIndexesAsString.length; i++) {
				bofIndexesAsString[i] = Tools.int2Str(bofIndexes[i]);
			}
		}
		return bofIndexesAsString;
	}

	public String getBof(String query, boolean isQueryID, int maxBagLength) throws BoFException {
		String bof = null;
		int[] bofIndexes = getBoFArray(query, isQueryID, maxBagLength);
		if (bofIndexes != null) {
			bof = Arrays.toString(bofIndexes);
			// to remove "[" and "]" and commas
			bof = bof.substring(1, bof.length() - 1).replaceAll(",", " ");
		}
		return bof;
	}

	public String getBofAsString(String query, boolean isQueryID, int maxBagLength)
			throws BoFException {
		String bofAsString = null;
		String[] bofIndexes = getBoFAsStringArray(query, isQueryID, maxBagLength);
		if (bofIndexes != null) {
			bofAsString = Arrays.toString(bofIndexes);
			// to remove "[" and "]" and commas
			bofAsString = bofAsString.substring(1, bofAsString.length() - 1)
					.replaceAll(",", " ");
		}
		return bofAsString;
	}

	public static void main(String[] args) {
		try {
			BoFSearcher searcher = new BoFSearcher();
			String bof = searcher.getBof("ACA-F-009623-0000", true, 50);
			System.out.println(bof);
			String bofAsString = searcher.getBofAsString("ACA-F-009623-0000",
					true, 50);
			System.out.println(bofAsString);
			try {
				String xml = Tools
						.file2String(new File("test/AAE-S-000124-8Z5J.xml"));
				bof = searcher.getBof(xml, false, 50);
				System.out.println(bof);
				bofAsString = searcher.getBofAsString(xml, false, 50);
				System.out.println(bofAsString);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (VIRException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public Settings getSettings(){
		return settings;
	}

}
