package process.citing.files;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

@Component
public class PnRc {
	
	File   PAPER_LIST_FILE;
	String JIF_FILE;
	String CITING_FOLDER;
	
    List<Map<String, String>> paper_list;
	Map<String, List<Map<String, String>>> citing_files_list;
	Map<String, List<Map<String, String>>> pure_citation_list;
	Map<String, List<Map<String, String>>> erup_citation_list;
	Map<String, String> jifMap;
	
	int cited_papers;
	int citation_counter;
	int self_citation_counter;
	int fles_citation_counter;

	public PnRc() {
		this.paper_list = new ArrayList<>();
		this.citing_files_list = new HashMap<>();
		this.pure_citation_list =  new HashMap<>();
		this.erup_citation_list = new HashMap<>();
		this.jifMap = new HashMap<>();

		this.cited_papers = 0;
		this.citation_counter = 0;
		this.self_citation_counter = 0;
		this.fles_citation_counter = 0;
	}
	public PnRc(String jif, File paper_list_file, String citing_folder) {
		// TODO Auto-generated constructor stub
		this.JIF_FILE = jif;
		this.PAPER_LIST_FILE = paper_list_file;
		this.CITING_FOLDER = citing_folder;
		
		this.paper_list = new ArrayList<>();
		this.citing_files_list = new HashMap<>();
		this.pure_citation_list =  new HashMap<>();
		this.erup_citation_list = new HashMap<>();
		this.jifMap = new HashMap<>();
		
		this.cited_papers = 0;
		this.citation_counter = 0;
		this.self_citation_counter = 0;
		this.fles_citation_counter = 0;
	}




	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	public void getData(Map<String, String> requirements) {
		
		paper_list = normRis(PAPER_LIST_FILE);
		File citing_folder = new File(CITING_FOLDER);
		getCitingPapers(citing_folder);
		cited_papers = citing_files_list.size();
		
		try {
			getJifMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(Map<String, String> paper : paper_list){
			String ut = paper.get("UT").split(":")[1];
			for(Iterator<String> iter = citing_files_list.keySet().iterator();iter.hasNext();){
				String ut_key = iter.next();
				if(ut_key.contains(ut)){
					List<Map<String, String>> ut_self_citation = new ArrayList<>();
					List<Map<String, String>> ut_fles_citation = new ArrayList<>();
					List<Map<String, String>> citing_papers = citing_files_list.get(ut_key);
					for(Map<String, String> one_citation : citing_papers){
						citation_counter += 1;
						if(!selfCitation(paper, one_citation, requirements)){
							fles_citation_counter += 1;
							ut_fles_citation.add(one_citation);
						}else if(selfCitation(paper, one_citation, requirements)){
							self_citation_counter += 1;
							ut_self_citation.add(one_citation);
						}
					}
					if(!ut_fles_citation.isEmpty()){
						pure_citation_list.put(ut, pure_citation_list.get(ut)==null?ut_fles_citation:ListUtils.union(pure_citation_list.get(ut), ut_fles_citation));
					}
					if(!ut_self_citation.isEmpty()){
						erup_citation_list.put(ut, erup_citation_list.get(ut)==null?ut_self_citation:ListUtils.union(erup_citation_list.get(ut), ut_self_citation));
					}
				}
			}
		}
		
	}

	private void getJifMap() throws IOException {
		// TODO Auto-generated method stub
		File jif = new File(JIF_FILE);
		BufferedReader mbr = new BufferedReader(new FileReader(jif));
		String jline;
		int mter = 0;
		while((jline = mbr.readLine()) != null){
			mter ++;
			if(mter == 1){continue;}
			String[] rec = jline.split("\t");
			try{
				jifMap.put(rec[0].trim().toUpperCase(), rec[1].trim());
			}catch(Exception e){
				continue;
			}
		}
		mbr.close();
	}

	private boolean selfCitation(Map<String, String> paper, Map<String, String> one_citation, Map<String, String> requirements) {
		// TODO Auto-generated method stub
		boolean apply_self_citation = false;
		boolean first_author_self_citation = false;
		boolean resear_group_self_citaion = false;

		boolean author_self_citation = false;
		boolean oi_self_citation = false;
		
		String aus = paper.containsKey("AF")?paper.get("AF"):"";
		String ois = paper.containsKey("OI")?paper.get("OI"):"";

		String citingAuthors =  one_citation.containsKey("AF")?one_citation.get("AF"):"";
		
		if (requirements.containsKey("apply_author") && requirements.get("apply_author").length() != 0)
		{
			for (String aux : requirements.get("apply_author").split(";"))
			{
				if (aux != null && !aux.equals("") && citingAuthors.contains(aux.trim())){
					apply_self_citation = true;
					System.out.println("apply_self_citation");
					return apply_self_citation;
				} else {
					return apply_self_citation;
				}
			}
		}

		if (requirements.containsKey("first_author"))
		{
			String firstAuthorName = paper.get("AF").split(";")[0];
			if (firstAuthorName != null && !firstAuthorName.equals("") && citingAuthors.contains(firstAuthorName.trim())){
				first_author_self_citation = true;
				System.out.println(paper.get("UT") + "\t" + "first_author selfCitation");
				return first_author_self_citation;
			} else {
				return first_author_self_citation;
			}
		}

		if (requirements.containsKey("resear_group") && requirements.get("resear_group").length() != 0)
		{
			for (String aux : requirements.get("resear_group").split(";"))
			{
				if (aux != null && !aux.equals("") && citingAuthors.contains(aux.trim())){
					resear_group_self_citaion = true;
					System.out.println("Group selfCitation");
					return resear_group_self_citaion;
				} else {
					return resear_group_self_citaion;
				}
			}
		}

		for(String aux : aus.split(";")){
			if (aux != null && !aux.equals("") && citingAuthors.contains(aux.trim())){
				author_self_citation = true;
				System.out.println("Cooper selfCitation");
				break;
			}
		}
		if(ois != null){
			for(String oi : ois.split(";")){
				if(one_citation.containsKey("OI")){
					String citing_ois = one_citation.get("OI");
					if ((oi.split("/")).length > 1 && citing_ois.contains(oi.split("/")[1].trim())){
						oi_self_citation = true;
						System.out.println("OI selfCitation");
					}
				}
			}
		}
		return author_self_citation || oi_self_citation;
	}

	private void getCitingPapers(File citing_folder) {
		// TODO Auto-generated method stub
		for(File file : citing_folder.listFiles()){
			String cited_ut = file.getName().replace(".txt", "");
			List<Map<String, String>> ut_citing_list = normRis(new File(citing_folder.getAbsolutePath() + "/" + file.getName()));
			citing_files_list.put(cited_ut, ut_citing_list);
		}
	}
	
	private List<Map<String, String>> normRis(File file) {
		// TODO Auto-generated method stub
		try {
			List<Map<String, String>> recsToRt = new ArrayList<>();
			Map<String, String> map = new HashMap<>();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while(!line.startsWith("EF")){
				if(line.trim().isEmpty()){
					line = br.readLine();
					continue;
				}else{
					String idf = line.substring(0, 2);
					if (!idf.trim().isEmpty()){
						switch (idf){
						case "FN":
						case "VR":
							line = br.readLine();
							break;
						case "ER":
							map.put("AU", map.get("AU").trim() + "-AAUU");
							recsToRt.add(map);
							line = br.readLine();
							map = new HashMap<>();
							break;
						default:
							map.put(idf, line.substring(3).trim().toString());
							line = br.readLine();
							while(line.startsWith("  ")){
								switch (idf){
								case "AU":
									map.put(idf, map.get(idf) + "-AAUU;" + line.trim());
									line = br.readLine();
									break;
								case "AF":
								case "C1":
								case "CR":
									map.put(idf, map.get(idf) + ";" + line.trim());
									line = br.readLine();
									break;
								default:
									map.put(idf, map.get(idf) + " " + line.trim());
									line = br.readLine();
									break;
								}}}}}}
			br.close();
			return recsToRt;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public List<Map<String, String>> getPaper_list() {
		return paper_list;
	}
	public Map<String, List<Map<String, String>>> getCiting_files_list() {
		return citing_files_list;
	}

	public void setCiting_files_list(Map<String, List<Map<String, String>>> citing_files_list) {
		this.citing_files_list = citing_files_list;
	}

	public Map<String, List<Map<String, String>>> getPure_citation_list() {
		return pure_citation_list;
	}

	public void setPure_citation_list(Map<String, List<Map<String, String>>> pure_citation_list) {
		this.pure_citation_list = pure_citation_list;
	}

	public Map<String, List<Map<String, String>>> getErup_citation_list() {
		return erup_citation_list;
	}

	public void setErup_citation_list(Map<String, List<Map<String, String>>> erup_citation_list) {
		this.erup_citation_list = erup_citation_list;
	}

	public int getCited_papers() {
		return cited_papers;
	}

	public void setCited_papers(int cited_papers) {
		this.cited_papers = cited_papers;
	}

	public int getCitation_counter() {
		return citation_counter;
	}

	public void setCitation_counter(int citation_counter) {
		this.citation_counter = citation_counter;
	}

	public int getSelf_citation_counter() {
		return self_citation_counter;
	}

	public void setSelf_citation_counter(int self_citation_counter) {
		this.self_citation_counter = self_citation_counter;
	}

	public int getFles_citation_counter() {
		return fles_citation_counter;
	}

	public void setFles_citation_counter(int fles_citation_counter) {
		this.fles_citation_counter = fles_citation_counter;
	}
	
}
