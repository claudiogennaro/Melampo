package it.cnr.isti.melampo.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AdHocMetadataMaker {
	
	public static void main(String[] args) {
		if (args.length != 1) {
			usage();
			return;
		}
		File imgDir = new File(args[0]);
		if (imgDir.isDirectory()) {
			AdHocMetadataMaker extractor = new AdHocMetadataMaker();
			extractor.recursiveDirClassification(imgDir);
		}
		else {
			usage();
			return;
		}
		
		
		
	}
	
	private String extractData(File htmlFile) throws IOException {
		StringBuilder metadata = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(htmlFile), "UTF8"));
		// 		BufferedReader reader = new BufferedReader(new FileReader(htmlFile));

		String line = null;
		boolean isMetadata = false;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.equals("<td class=\"details\" valign=\"top\">")) {
				isMetadata = true;
			} else if (isMetadata == true && line.equals("</td>")) {
				isMetadata = false;
			}
			if (isMetadata == true) {
				line = line.replaceFirst("<span[^>]*>", "\n");
				line = line.replaceFirst("</span[^>]*>", "=");
				line = line.replaceFirst(":=", "=");
				line = line.replaceAll("<[^>]*>", "");
				line = line.replaceAll("&nbsp;-&nbsp;", ";");
				line = line.replaceAll("&nbsp;", "");
				metadata.append(line);
			}
		}
		return metadata.toString();
	}
	
	private void recursiveDirClassification(File testDir) {
        System.out.println("evaluating folder: " + testDir.getPath());
        // retrieve all files in testDir dir
        File[] dirFiles = testDir.listFiles();
        // number of files
        int numFiles = dirFiles.length;
        int startIndex = 0;

        int numImgFiles = 0;

        // index
        for (int index = startIndex; index < numFiles; index++) {
            File imgFile = null;

            if (dirFiles[index].isDirectory()) {
                recursiveDirClassification(dirFiles[index]);
            } else {
                    imgFile = dirFiles[index];
                    String features;
    				try {
    					features = extractData(imgFile);
        				Tools.string2File(features, new File("D:/demo/adhoc/metadatatxt/" + imgFile.getName().substring(0, imgFile.getName().lastIndexOf(".")) +  ".txt"));
        				System.out.println("img file " + imgFile.getPath());
                        numImgFiles++;
    				} catch (IOException e) {
    					System.out.println("error saving features file " + "D:/demo/adhoc/metadatatxt/" + imgFile.getName().substring(0, imgFile.getPath().lastIndexOf(".")) +  ".txt");
						e.printStackTrace();
					}
                    }
                }
        System.out.println("evaluated " +numFiles + " files");
            }

	private static void usage() {
		System.out.println("Usage: AdhocMetadataMaker img_file_dir [-r]");

	}
}
