package it.cnr.isti.melampo.metadata;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;

public class MetadataSeacher  {

	private String m_Path;
	private Settings settings;
	
	private int[] bof;
	private String id;
	
	public MetadataSeacher() throws IOException, VIRException {
		settings = new Settings();
		setPath();
	}

	public MetadataSeacher(File propertyFile) throws IOException, VIRException {
		settings = new Settings(propertyFile);
		setPath();
	}

	private void setPath() {
		m_Path = settings.getMetadataDir();
		if(!m_Path.endsWith("/")){
			m_Path = m_Path +"/";
		}
	}

	public String getMetadata(String queryID){
		
		String ret="";
		String fileName = m_Path + queryID + ".txt";
		System.out.println(fileName);
		BufferedReader lr = null;
		String line;
		
		try
		{
			//lr = new BufferedReader(new FileReader(fileName));
			lr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
		
			while ((line = lr.readLine()) != null) {
				
				String[] strs = getField(line);
				if (strs.length==2){
					String field_name = strs[0];
					String field_content = strs[1];
					
					if(field_name.contains("Title") || 
					   field_name.contains("Keywords") ||
					   field_name.contains("Place") ||
					   field_name.contains("Photographer") ||
					   field_name.contains("Location")){
					
						ret = ret + field_content + " ";
					}
				}
				
			}
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return ret;
	}

	private String[] getField(String in){
		
		return in.split("=");
	}
	
	public Settings getSettings(){
		return settings;
	}
}
