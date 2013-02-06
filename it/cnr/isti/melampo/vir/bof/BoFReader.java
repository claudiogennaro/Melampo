package it.cnr.isti.melampo.vir.bof;

import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.tools.Tools;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.vir.features.IFeaturesCollector;
import it.cnr.isti.vir.features.bof.BoF_LF_OriAndScale;
import it.cnr.isti.vir.file.ArchiveException;
import it.cnr.isti.vir.file.FeaturesCollectorsArchives;
import it.cnr.isti.vir.id.IHasID;
import it.cnr.isti.vir.id.IID;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class BoFReader  {

	private FeaturesCollectorsArchives bofArchives;
	private Settings settings;
	
	private int[] bof;
	private String id;
	
	public BoFReader() throws IOException, VIRException {
		settings = new Settings();
		setBof();
	}

	public BoFReader(File propertyFile) throws IOException, VIRException {
		settings = new Settings(propertyFile);
		setBof();
	}

	private void setBof() {
		bofArchives = settings.getBofArchives();
	}

	public int size() {
		return bofArchives.size();
	}

	private void retrieveBoF(int index) throws BoFException {
		IFeaturesCollector featureColl;
		try {
			featureColl = bofArchives.get(index);
			BoF_LF_OriAndScale bofFeature = (BoF_LF_OriAndScale) featureColl
					.getFeature(BoF_LF_OriAndScale.class);
			bof = bofFeature.getBagIndexes();
			
			IID id = ((IHasID) featureColl).getID();
			this.id = id.toString();
			
		} catch (ArchiveException e) {
			throw new BoFException(e);
		}
	}
	
	public int[] getBoFArray(int index) throws BoFException {
		retrieveBoF(index);
		return this.bof;
	}
		
	public String getID() {
		return id;
	}

	public String[] getBoFAsStringArray(int index) throws BoFException {
		String[] bofIndexesAsString = null;
		int[] bofIndexes = getBoFArray(index);
		if (bofIndexes != null) {
			bofIndexesAsString = new String[bofIndexes.length];
			for (int i = 0; i < bofIndexesAsString.length; i++) {
				bofIndexesAsString[i] = Tools.int2Str(bofIndexes[i]);
			}
		}
		return bofIndexesAsString;
	}

	public String getBof(int index) throws BoFException {
		String bof = null;
		int[] bofIndexes = getBoFArray(index);
		if (bofIndexes != null) {
			bof = Arrays.toString(bofIndexes);
			// to remove "[" and "]" and commas
			bof = bof.substring(1, bof.length() - 1).replaceAll(",", " ");
		}
		return bof;
	}

	public String getBofAsString(int index) throws BoFException {
		String bofAsString = null;
		String[] bofIndexes = getBoFAsStringArray(index);
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
			StringBuilder sb = new StringBuilder();
			BoFReader reader = new BoFReader();
			int size = reader.size();
			int counter = 14;
			for (int index = 0; index < size; index++) {
				String bof;
				try {
					bof = reader.getBof(index);
					sb.append("D:/backup/software/wget-1.11.4-1-bin/bin/wget -O " + reader.getID() + ".html \"http://www.myarchives.it/internal/?photocode=" + reader.getID() + "&languageID=en\"");
					sb.append("\n");
					if (index%20000 == 0) {
						Tools.string2File(sb.toString(), new File("archive" + counter + ".bat"));
						sb = new StringBuilder();
						counter++;
					}
					//System.out.println(bof);
					//String bofAsString = reader.getBofAsString(index);
					//System.out.println(bofAsString);
				} catch (BoFException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Tools.string2File(sb.toString(), new File("archive" + counter + ".bat"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (VIRException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public Settings getSettings(){
		return settings;
	}
}
