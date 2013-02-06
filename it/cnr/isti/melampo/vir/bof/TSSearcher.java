package it.cnr.isti.melampo.vir.bof;

import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.tools.TSSettings;
import it.cnr.isti.melampo.tools.Tools;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.vir.features.FeaturesCollectorHT;
import it.cnr.isti.vir.features.IFeaturesCollector;
import it.cnr.isti.vir.features.bof.BoF_LF_OriAndScale;
import it.cnr.isti.vir.features.localfeatures.BoFLFGroup;
import it.cnr.isti.vir.id.IDString;
import it.cnr.isti.vir.readers.CoPhIRv2Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

public class TSSearcher {

	private TSSettings settings;

	public TSSearcher() throws IOException, VIRException {
		settings = new TSSettings();
	}

	public TSSearcher(File propertyFile) throws IOException, VIRException {
		settings = new TSSettings(propertyFile);
	}

	public int[] getBoFArray(String query, boolean isQueryID, int maxBagLength) throws BoFException {
		int[] bofIndexes = null;
		int[] result = null;
		IFeaturesCollector featureColl;
		BoFLFGroup bofFeature = null;
		try {
			if (isQueryID) {
				featureColl = settings.getFCArchives()
						.get(new IDString(query));
				bofFeature = (BoFLFGroup) featureColl
						.getFeature(BoFLFGroup.class);
			} else {
				featureColl = new FeaturesCollectorHT(CoPhIRv2Reader
						.getObj(new BufferedReader(new StringReader(query))));
				bofFeature = (BoFLFGroup) featureColl
				.getFeature(BoFLFGroup.class);
				
			}
			if (bofFeature != null) {
				bofIndexes = bofFeature.getWords();
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
			TSSearcher searcher = new TSSearcher();
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
	
	public TSSettings getSettings(){
		return settings;
	}

}
