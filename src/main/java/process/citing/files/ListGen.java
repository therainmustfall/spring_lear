package process.citing.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.springframework.stereotype.Component;

public class ListGen {
	
	String word_file_folder;
	PnRc report_data;
	WordprocessingMLPackage template;

	public ListGen() {}
	public ListGen(PnRc pnrc, String wff) throws FileNotFoundException, Docx4JException{
		this.word_file_folder = wff;
		this.report_data = pnrc;
		//this.template = DocM.getTemplate(this.word_file_folder + "/" + "template.docx");
	}
	
	public static void main(String[] args) throws FileNotFoundException, Docx4JException {
		// TODO Auto-generated method stub
		// PnRc m = new PnRc("jif.txt", "savedrecs.txt", "citing");
		// m.getData();
		// System.out.println(m.getSelf_citation_counter());
		// System.out.println(m.getFles_citation_counter());
		// System.out.println(m.getCitation_counter());
		// System.out.println("Done.");
	}
	public void generateList(PnRc pnrc, Map<String, String> reportType) throws IOException
	{

		File listFile = new File(word_file_folder + "/" + "report_content.txt");
		if(!listFile.exists()) {
			listFile.createNewFile();
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(listFile,true));
		
		if(reportType.containsKey("jif")  && reportType.containsKey("detail_list")){
			List<Map<String, String>> pList = pnrc.paper_list;
            int number_of_papers = pList.size();
			int report_paper_counter = 0;
			
			for(Map<String, String> one_paper : pList){
				report_paper_counter += 1;
				bw.write(report_paper_counter + "、\n" + getFields(one_paper, "TI"));
				bw.write(getFields(one_paper, "AU"));
				bw.write(getFields(one_paper, "SO"));
				bw.write(getFields(one_paper, "UT"));
				bw.write(getFields(one_paper, "JIF"));
				if(pnrc.pure_citation_list.containsKey(one_paper.get("UT").split(":")[1])){
					System.out.println(one_paper.get("UT").split(":")[1]);
					bw.write(getFields(one_paper, "CTT"));
					List<Map<String, String>> ones_citing_papers = pnrc.pure_citation_list.get(one_paper.get("UT").split(":")[1]);
					int cited_times = ones_citing_papers.size();
					System.out.println("cited times: " + cited_times);
					int cited_counter = 0;
					for(Map<String, String> citing_map : ones_citing_papers){
						cited_counter += 1;
						bw.write(report_paper_counter + "-" + cited_counter + "." + getFields(citing_map, "TI"));
						bw.write(getFields(citing_map, "AU"));
						bw.write(getFields(citing_map, "SO"));
					}
					
				}else{
					bw.write("他引次数:0\n");
				}
			
			}
		}else if(reportType.containsKey("jif")  && !reportType.containsKey("detail_list")){
			List<Map<String, String>> pList = pnrc.paper_list;
            int number_of_papers = pList.size();
			int report_paper_counter = 0;
			try {
			
			for(Map<String, String> one_paper : pList){
				report_paper_counter += 1;
				bw.write(report_paper_counter + "、\n" + getFields(one_paper, "TI"));
				bw.write(getFields(one_paper, "AU"));
				bw.write(getFields(one_paper, "SO"));
				bw.write(getFields(one_paper, "UT"));
				bw.write(getFields(one_paper, "JIF"));
				if(pnrc.pure_citation_list.containsKey(one_paper.get("UT").split(":")[1])){
					System.out.println(one_paper.get("UT").split(":")[1]);
					bw.write(getFields(one_paper, "CTT"));
				}else{
					//((ContentAccessor)timesP.getParent()).getContent().remove(timesP); 
					bw.write("他引次数:0\n");
				}
			}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				System.out.println("here.");
			}
		}else if(!reportType.containsKey("jif")  && !reportType.containsKey("detail_list")){
			List<Map<String, String>> pList = pnrc.paper_list;
            int number_of_papers = pList.size();
			int report_paper_counter = 0;
			
			for(Map<String, String> one_paper : pList){
				report_paper_counter += 1;
				bw.write(report_paper_counter + "、\n" + getFields(one_paper, "TI"));
				bw.write(getFields(one_paper, "AU"));
				bw.write(getFields(one_paper, "SO"));
				bw.write(getFields(one_paper, "UT"));
				if(pnrc.pure_citation_list.containsKey(one_paper.get("UT").split(":")[1])){
					System.out.println(one_paper.get("UT").split(":")[1]);
					bw.write(getFields(one_paper, "CTT"));
				}else{
					bw.write("他引次数:0\n");
				}
			
			}
		}

		bw.flush();
		bw.close();
		generateSummary();
		
		
	}
	public void updateReport(WordprocessingMLPackage teMlPackage, PnRc pnrc, Map<String, String> reportType){
		
		Tc foucusTc = DocM.focusOnReplaceTc(teMlPackage, "authorlistline");
		if(reportType.containsKey("jif")  && reportType.containsKey("detail_list")){
			List<Map<String, String>> pList = pnrc.paper_list;
			int number_of_papers = pList.size();
			int report_paper_counter = 0;
			
			for(Map<String, String> one_paper : pList){
				report_paper_counter += 1;
				
				Text ti = DocM.focusOnRelaceHolder(teMlPackage, "papertitle");
				P tiP = DocM.focusOnReplaceHolderPara(teMlPackage, "papertitle");
				Text author = DocM.focusOnRelaceHolder(teMlPackage, "authorlistline");
				P authorP = DocM.focusOnReplaceHolderPara(teMlPackage, "authorlistline");
				Text tso = DocM.focusOnRelaceHolder(teMlPackage, "papersourceline");
				P tsoP = DocM.focusOnReplaceHolderPara(teMlPackage, "papersourceline");
				Text utNum = DocM.focusOnRelaceHolder(teMlPackage, "utnumberline");
				P utNumP = DocM.focusOnReplaceHolderPara(teMlPackage, "utnumberline");
				Text jift = DocM.focusOnRelaceHolder(teMlPackage, "jifline");
				P jifP = DocM.focusOnReplaceHolderPara(teMlPackage, "jifline");
				Text times = DocM.focusOnRelaceHolder(teMlPackage, "purecitationline");
				P timesP = DocM.focusOnReplaceHolderPara(teMlPackage, "purecitationline");
				Text ct_paper_ti = DocM.focusOnRelaceHolder(teMlPackage, "citingpapertitleline");
				P ct_paper_tiP = DocM.focusOnReplaceHolderPara(teMlPackage, "citingpapertitleline");
				Text ct_paper_au = DocM.focusOnRelaceHolder(teMlPackage, "citingpaperauthorline");
				P ct_paper_auP = DocM.focusOnReplaceHolderPara(teMlPackage, "citingpaperauthorline");
				Text ct_paper_so = DocM.focusOnRelaceHolder(teMlPackage, "citingpapersourceline");
				P ct_paper_soP = DocM.focusOnReplaceHolderPara(teMlPackage, "citingpapersourceline");
				
				
				P copy_tiP = XmlUtils.deepCopy(tiP);
				P copy_authorP = XmlUtils.deepCopy(authorP);
				P copy_tsoP = XmlUtils.deepCopy(tsoP);
				P copy_utNumP = XmlUtils.deepCopy(utNumP);
				P copy_jifP = XmlUtils.deepCopy(jifP);
				P copy_timesP = XmlUtils.deepCopy(timesP);
				P copy_ct_paper_tiP = XmlUtils.deepCopy(ct_paper_tiP);
				P copy_ct_paper_auP = XmlUtils.deepCopy(ct_paper_auP);
				P copy_ct_paper_soP = XmlUtils.deepCopy(ct_paper_soP);
				
				ti.setValue(report_paper_counter + "、\n" + getFields(one_paper, "TI"));
				author.setValue(getFields(one_paper, "AU"));
				tso.setValue(getFields(one_paper, "SO"));
				utNum.setValue(getFields(one_paper, "UT"));
				jift.setValue(getFields(one_paper, "JIF"));
				if(pnrc.pure_citation_list.containsKey(one_paper.get("UT").split(":")[1])){
					System.out.println(one_paper.get("UT").split(":")[1]);
					times.setValue(getFields(one_paper, "CTT"));
					List<Map<String, String>> ones_citing_papers = pnrc.pure_citation_list.get(one_paper.get("UT").split(":")[1]);
					int cited_times = ones_citing_papers.size();
					System.out.println("cited times: " + cited_times);
					int cited_counter = 0;
					for(Map<String, String> citing_map : ones_citing_papers){
						cited_counter += 1;
						ct_paper_ti.setValue(report_paper_counter + "-" + cited_counter + "." + getFields(citing_map, "TI"));
						ct_paper_au.setValue(getFields(citing_map, "AU"));
						ct_paper_so.setValue(getFields(citing_map, "SO"));
						
						if(cited_counter < cited_times){
							
							 foucusTc.getContent().add(copy_ct_paper_tiP);
							 foucusTc.getContent().add(copy_ct_paper_auP);
							 foucusTc.getContent().add(copy_ct_paper_soP);
							 
							 ct_paper_ti = DocM.focusOnRelaceHolder(teMlPackage, "citingpapertitleline");
							 System.out.println(ct_paper_ti.getValue());
							 ct_paper_au = DocM.focusOnRelaceHolder(teMlPackage, "citingpaperauthorline");
							 System.out.println(ct_paper_au.getValue());
							 ct_paper_so = DocM.focusOnRelaceHolder(teMlPackage, "citingpapersourceline");
							 System.out.println(ct_paper_so.getValue());
							 
							 copy_ct_paper_tiP = XmlUtils.deepCopy(DocM.focusOnReplaceHolderPara(teMlPackage, "citingpapertitleline"));
							 copy_ct_paper_auP = XmlUtils.deepCopy(DocM.focusOnReplaceHolderPara(teMlPackage, "citingpaperauthorline"));
							 copy_ct_paper_soP = XmlUtils.deepCopy(DocM.focusOnReplaceHolderPara(teMlPackage, "citingpapersourceline"));
						}
					}
					
				}else{
					//((ContentAccessor)timesP.getParent()).getContent().remove(timesP); 
					times.setValue("他引次数:0");
					((ContentAccessor)ct_paper_tiP.getParent()).getContent().remove(ct_paper_tiP); 
					((ContentAccessor)ct_paper_auP.getParent()).getContent().remove(ct_paper_auP); 
					((ContentAccessor)ct_paper_soP.getParent()).getContent().remove(ct_paper_soP); 
				}
			
				if(report_paper_counter < number_of_papers){
					foucusTc.getContent().add(copy_tiP);
					foucusTc.getContent().add(copy_authorP);
					foucusTc.getContent().add(copy_tsoP);
					foucusTc.getContent().add(copy_utNumP);
					foucusTc.getContent().add(copy_jifP);
					foucusTc.getContent().add(copy_timesP);
					foucusTc.getContent().add(copy_ct_paper_tiP);
					foucusTc.getContent().add(copy_ct_paper_auP);
					foucusTc.getContent().add(copy_ct_paper_soP);
				}
			}
		}else if(reportType.containsKey("jif")  && !reportType.containsKey("detail_list")){
			List<Map<String, String>> pList = pnrc.paper_list;
			int number_of_papers = pList.size();
			int report_paper_counter = 0;
			
			P ct_paper_tiP = DocM.focusOnReplaceHolderPara(teMlPackage, "citingpapertitleline");
			P ct_paper_auP = DocM.focusOnReplaceHolderPara(teMlPackage, "citingpaperauthorline");
			P ct_paper_soP = DocM.focusOnReplaceHolderPara(teMlPackage, "citingpapersourceline");
			try {
				foucusTc.getContent().remove(ct_paper_tiP);
				foucusTc.getContent().remove(ct_paper_auP);
				foucusTc.getContent().remove(ct_paper_soP);
				
			
			
			for(Map<String, String> one_paper : pList){
				report_paper_counter += 1;
				
				Text ti = DocM.focusOnRelaceHolder(teMlPackage, "papertitle");
				P tiP = DocM.focusOnReplaceHolderPara(teMlPackage, "papertitle");
				Text author = DocM.focusOnRelaceHolder(teMlPackage, "authorlistline");
				P authorP = DocM.focusOnReplaceHolderPara(teMlPackage, "authorlistline");
				Text tso = DocM.focusOnRelaceHolder(teMlPackage, "papersourceline");
				P tsoP = DocM.focusOnReplaceHolderPara(teMlPackage, "papersourceline");
				Text utNum = DocM.focusOnRelaceHolder(teMlPackage, "utnumberline");
				P utNumP = DocM.focusOnReplaceHolderPara(teMlPackage, "utnumberline");
				Text jift = DocM.focusOnRelaceHolder(teMlPackage, "jifline");
				P jifP = DocM.focusOnReplaceHolderPara(teMlPackage, "jifline");
				Text times = DocM.focusOnRelaceHolder(teMlPackage, "purecitationline");
				P timesP = DocM.focusOnReplaceHolderPara(teMlPackage, "purecitationline");
				
				
				
				P copy_tiP = XmlUtils.deepCopy(tiP);
				P copy_authorP = XmlUtils.deepCopy(authorP);
				P copy_tsoP = XmlUtils.deepCopy(tsoP);
				P copy_utNumP = XmlUtils.deepCopy(utNumP);
				P copy_jifP = XmlUtils.deepCopy(jifP);
				P copy_timesP = XmlUtils.deepCopy(timesP);
				
				
				ti.setValue(report_paper_counter + "、\n" + getFields(one_paper, "TI"));
				author.setValue(getFields(one_paper, "AU"));
				tso.setValue(getFields(one_paper, "SO"));
				utNum.setValue(getFields(one_paper, "UT"));
				jift.setValue(getFields(one_paper, "JIF"));
				if(pnrc.pure_citation_list.containsKey(one_paper.get("UT").split(":")[1])){
					System.out.println(one_paper.get("UT").split(":")[1]);
					times.setValue(getFields(one_paper, "CTT"));
				}else{
					//((ContentAccessor)timesP.getParent()).getContent().remove(timesP); 
					times.setValue("他引次数:0");
				}
				if(report_paper_counter < number_of_papers){
					foucusTc.getContent().add(copy_tiP);
					foucusTc.getContent().add(copy_authorP);
					foucusTc.getContent().add(copy_tsoP);
					foucusTc.getContent().add(copy_utNumP);
					foucusTc.getContent().add(copy_jifP);
					foucusTc.getContent().add(copy_timesP);
				}
			}
			
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				System.out.println("here.");
			}
		}else if(!reportType.containsKey("jif")  && !reportType.containsKey("detail_list")){
			List<Map<String, String>> pList = pnrc.paper_list;
			int number_of_papers = pList.size();
			int report_paper_counter = 0;
			
			P ct_paper_tiP = DocM.focusOnReplaceHolderPara(teMlPackage, "citingpapertitleline");
			P ct_paper_auP = DocM.focusOnReplaceHolderPara(teMlPackage, "citingpaperauthorline");
			P ct_paper_soP = DocM.focusOnReplaceHolderPara(teMlPackage, "citingpapersourceline");
			P jifP = DocM.focusOnReplaceHolderPara(teMlPackage, "jifline");
			foucusTc.getContent().remove(ct_paper_tiP);
			foucusTc.getContent().remove(ct_paper_auP);
			foucusTc.getContent().remove(ct_paper_soP);
			foucusTc.getContent().remove(jifP);
			
			for(Map<String, String> one_paper : pList){
				report_paper_counter += 1;
				
				Text ti = DocM.focusOnRelaceHolder(teMlPackage, "papertitle");
				P tiP = DocM.focusOnReplaceHolderPara(teMlPackage, "papertitle");
				Text author = DocM.focusOnRelaceHolder(teMlPackage, "authorlistline");
				P authorP = DocM.focusOnReplaceHolderPara(teMlPackage, "authorlistline");
				Text tso = DocM.focusOnRelaceHolder(teMlPackage, "papersourceline");
				P tsoP = DocM.focusOnReplaceHolderPara(teMlPackage, "papersourceline");
				Text utNum = DocM.focusOnRelaceHolder(teMlPackage, "utnumberline");
				P utNumP = DocM.focusOnReplaceHolderPara(teMlPackage, "utnumberline");
				Text times = DocM.focusOnRelaceHolder(teMlPackage, "purecitationline");
				P timesP = DocM.focusOnReplaceHolderPara(teMlPackage, "purecitationline");
				
				
				
				P copy_tiP = XmlUtils.deepCopy(tiP);
				P copy_authorP = XmlUtils.deepCopy(authorP);
				P copy_tsoP = XmlUtils.deepCopy(tsoP);
				P copy_utNumP = XmlUtils.deepCopy(utNumP);
				P copy_timesP = XmlUtils.deepCopy(timesP);
				
				
				ti.setValue(report_paper_counter + "、\n" + getFields(one_paper, "TI"));
				author.setValue(getFields(one_paper, "AU"));
				tso.setValue(getFields(one_paper, "SO"));
				utNum.setValue(getFields(one_paper, "UT"));
				if(pnrc.pure_citation_list.containsKey(one_paper.get("UT").split(":")[1])){
					System.out.println(one_paper.get("UT").split(":")[1]);
					times.setValue(getFields(one_paper, "CTT"));
				}else{
					//((ContentAccessor)timesP.getParent()).getContent().remove(timesP); 
					times.setValue("他引次数:0");
				}
			
				if(report_paper_counter < number_of_papers){
					foucusTc.getContent().add(copy_tiP);
					foucusTc.getContent().add(copy_authorP);
					foucusTc.getContent().add(copy_tsoP);
					foucusTc.getContent().add(copy_utNumP);
					foucusTc.getContent().add(copy_timesP);
				}
			}
		}
		
		try {
			DocM.writeDocxToStream(teMlPackage, word_file_folder);
			generateSummary();
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void generateSummary() throws IOException {
		// TODO Auto-generated method stub
		List<Map<String, String>> paper_list = this.report_data.paper_list;
		Map<String, List<Map<String, String>>> pure_ut_ct = this.report_data.pure_citation_list;
		Map<String, List<Map<String, String>>> erup_ut_ct = this.report_data.erup_citation_list;
		
		File summary_file = new File(word_file_folder + "/" + "total_citation.txt");
		if(!summary_file.exists()) {
			summary_file.createNewFile();
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(summary_file,true));
		bw.write("WoS入藏号" + "\t" + "出版年" + "\t" + "总被引次数" + "\t" + "他引次数" + "\t" + "自引次数" + "\n");
		
		File self_citation = new File(word_file_folder + "/" + "self_citation.txt");
		if(!self_citation.exists()) {
			self_citation.createNewFile();
		}
		BufferedWriter sbw = new BufferedWriter(new FileWriter(self_citation,true));
		
		File other_citation = new File(word_file_folder + "/" + "without_self_citation.txt");
		if(!other_citation.exists()) {
			other_citation.createNewFile();
		}
		BufferedWriter obw = new BufferedWriter(new FileWriter(other_citation,true));
		
		for(Map<String, String> m : paper_list) {
			String p_ut = m.get("UT").split(":")[1];
				int pure_ut_ct_num = pure_ut_ct.get(p_ut) == null ? 0 : pure_ut_ct.get(p_ut).size();
				int erup_ut_ct_num = erup_ut_ct.get(p_ut) == null ? 0 : erup_ut_ct.get(p_ut).size();
				bw.write(p_ut + "\t" + m.get("PY") + "\t" + String.valueOf(pure_ut_ct_num + erup_ut_ct_num)
				+ "\t" + pure_ut_ct_num + "\t" + erup_ut_ct_num + "\n"); 
				
				if(erup_ut_ct_num != 0) {
					sbw.write(p_ut + "\n"); 
					for(Map<String, String> self_citation_map : erup_ut_ct.get(p_ut)) {
						sbw.write(self_citation_map.get("TI") + "\n");
						sbw.write("Author(s):" + self_citation_map.get("AU").replace("-AAUU", "") + "\n");
						sbw.write("Source:" + self_citation_map.get("SO").trim() + ", " 
									+ (self_citation_map.containsKey("VL")&&!self_citation_map.get("VL").trim().isEmpty()?"volume:" 
									+ self_citation_map.get("VL").trim() + ",":"") + " " + "issue:" 
									+ (self_citation_map.containsKey("IS")&&!self_citation_map.get("IS").trim().isEmpty()?self_citation_map.get("IS").trim() 
									+ ",":" ")+ " " + "pages:" 
									+ (self_citation_map.containsKey("BP")&&!self_citation_map.get("BP").trim().isEmpty()&&self_citation_map.containsKey("EP")&&!self_citation_map.get("EP").trim().isEmpty()?self_citation_map.get("BP").trim() + 
									"-" + self_citation_map.get("EP").trim() + ".":" ") + " " 
									+ "Published:" + (self_citation_map.containsKey("PD")&&!self_citation_map.get("PD").trim().isEmpty()?self_citation_map.get("PD").trim():"") 
									+ (self_citation_map.containsKey("PY")&&!self_citation_map.get("PY").trim().isEmpty()?" " + self_citation_map.get("PY").trim():"") + "\n");
						sbw.write("Document Number " + self_citation_map.get("UT").trim() + "\n");
						sbw.write("\n");
					}
				}
				
				if(pure_ut_ct_num != 0) {
					obw.write(p_ut + "\n"); 
					for(Map<String, String> other_citation_map : pure_ut_ct.get(p_ut)) {
						obw.write(other_citation_map.get("TI") + "\n");
						obw.write("Author(s):" + other_citation_map.get("AU").replace("-AAUU", "") + "\n");
						obw.write("Source:" + other_citation_map.get("SO").trim() + ", " 
									+ (other_citation_map.containsKey("VL")&&!other_citation_map.get("VL").trim().isEmpty()?"volume:" 
									+ other_citation_map.get("VL").trim() + ",":"") + " " + "issue:" 
									+ (other_citation_map.containsKey("IS")&&!other_citation_map.get("IS").trim().isEmpty()?other_citation_map.get("IS").trim() 
									+ ",":" ")+ " " + "pages:" 
									+ (other_citation_map.containsKey("BP")&&!other_citation_map.get("BP").trim().isEmpty()&&other_citation_map.containsKey("EP")&&!other_citation_map.get("EP").trim().isEmpty()?other_citation_map.get("BP").trim() + 
									"-" + other_citation_map.get("EP").trim() + ".":" ") + " " 
									+ "Published:" + (other_citation_map.containsKey("PD")&&!other_citation_map.get("PD").trim().isEmpty()?other_citation_map.get("PD").trim():"") 
									+ (other_citation_map.containsKey("PY")&&!other_citation_map.get("PY").trim().isEmpty()?" " + other_citation_map.get("PY").trim():"") + "\n");
						obw.write("Document Number " + other_citation_map.get("UT").trim() + "\n");
						obw.write("\n");
					}
				}
		}
		obw.flush();
		obw.close();
		sbw.flush();
		sbw.close();
		bw.flush();
		bw.close();
	}

	public String zipAuthors(String leftauthors, String rightauthors)
	{
		List<String> arr1 = Arrays.asList(leftauthors.split(";"));
		List<String> arr2 = Arrays.asList(rightauthors.split(";"));

		String[] authors = IntStream
			.range(0, arr1.size())
			.mapToObj(i -> arr1.get(i).replace(",","::") + "(" + arr2.get(i).replace(",","::") + ")").toArray(String[]::new);
		return Arrays.toString(authors).replace("[","").replace("]","").replace(",",";").replace("::",",");
	}

	public String getFields(Map<String, String> map, String field){
		
		switch (field) {
		case "TI":
			return "标题:" + map.get("TI") + "\n";
		case "AU":
			return map.containsKey("AU") && map.containsKey("AF") ?"作者:" + zipAuthors(map.get("AU").replace("-AAUU", ""), map.get("AF").replace("-AAUU", "")) + "\n":"作者:\n";
		case "SO":
			return "来源出版物:" + map.get("SO").trim() + " " + 
			(map.containsKey("VL")&&!map.get("VL").trim().isEmpty()?"卷:" + map.get("VL").trim() + " ":"") + " " + 
			"期:" + (map.containsKey("IS")&&!map.get("IS").trim().isEmpty()?map.get("IS").trim() + " ":" ")+ " " + 
			"页:" + (map.containsKey("BP")&&!map.get("BP").trim().isEmpty()&&map.containsKey("EP")&&!map.get("EP").trim().isEmpty()?map.get("BP").trim() + 
			"-" + map.get("EP").trim() + ".":" ") + " " + 
			"DOI:" + map.get("DI") + " " + 
			"出版年:" + (map.containsKey("PD")&&!map.get("PD").trim().isEmpty()?map.get("PD").trim():"")  + 
			(map.containsKey("PY")&&!map.get("PY").trim().isEmpty()?" " + map.get("PY").trim():"") + "\n";
		
		case "UT":
			return "入藏号:" + map.get("UT").trim() + "\n";
		case "JIF":
			return "期刊影响因子:" + this.report_data.jifMap.get(map.get("SO").trim().toUpperCase()) + "\n";
		case "CTT":
			return "他引次数:" + String.valueOf(this.report_data.pure_citation_list.get(map.get("UT").split(":")[1]).size()) + "\n";
		default:
			break;
		}
		
		return null;
	}
	
	public PnRc getReport_data() {
		return report_data;
	}

	public void setReport_data(PnRc report_data) {
		this.report_data = report_data;
	}

	public WordprocessingMLPackage getTemplate() {
		return template;
	}

	public void setTemplate(WordprocessingMLPackage template) {
		this.template = template;
	}
}
