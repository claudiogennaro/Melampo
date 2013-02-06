package it.cnr.isti.melampo.vir.sapir;

import it.cnr.isti.melampo.index.settings.MP7CSettings;
import it.cnr.isti.melampo.tools.Tools;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.vir.features.FeaturesCollectorHT;
import it.cnr.isti.vir.features.IFeaturesCollector;
import it.cnr.isti.vir.features.mpeg7.SAPIRObject;
import it.cnr.isti.vir.id.IDString;
import it.cnr.isti.vir.readers.CoPhIRv2Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public class SapirSearcher {

	private MP7CSettings settings;

	public SapirSearcher() throws IOException, VIRException {
		settings = new MP7CSettings();
	}

	public SapirSearcher(File propertyFile) throws IOException, VIRException {
		settings = new MP7CSettings(propertyFile);
	}
	
	public SapirSearcher(MP7CSettings settings) throws IOException, VIRException {
		this.settings = settings;
	}

	public SAPIRObject getSapirObject(String query, boolean isQueryID)
			throws VIRException {
		IFeaturesCollector featureColl;
		SAPIRObject obj;
		try {
			if (isQueryID) {
				featureColl = settings.getFCArchives().get(new IDString(query));
			} else {
				featureColl = new FeaturesCollectorHT(
						CoPhIRv2Reader.getObj(new BufferedReader(
								new StringReader(query))));
			}
			obj = new SAPIRObject(featureColl);
		} catch (Exception e) {
			throw new VIRException(e);
		}
		return obj;
	}

	public static void main(String[] args) {
		try {
			SapirSearcher searcher = new SapirSearcher();
			SAPIRObject obj = searcher
					.getSapirObject("ACA-F-009623-0000", true);
			System.out.println(obj.getFeatures().toString());

			String xml = Tools.file2String(new File("test/AAE-S-000124-8Z5J.xml"));
			obj = searcher.getSapirObject(xml, false);
			System.out.println(obj.getFeatures().toString());

		} catch (VIRException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public MP7CSettings getSettings() {
		return settings;
	}

}
