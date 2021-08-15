package process.citing.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

public class DocM {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WordprocessingMLPackage template;
		try {
			
			template = getTemplate("tp.docx");
			String placeholder = "TITLE";
			String toAdd = "Big Dictator \n The Script";
			replaceParagraph(placeholder, toAdd, template, template.getMainDocumentPart());
			
			
			writeDocxToStream(template, "tp.docx");
			
			
		} catch (FileNotFoundException | Docx4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

}
	
	static WordprocessingMLPackage getTemplate(String name) throws FileNotFoundException, Docx4JException {
		WordprocessingMLPackage template = WordprocessingMLPackage.load(new FileInputStream(new File(name)));
		return template;
	} 
	
	
	static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch){
		List<Object> result = new ArrayList<>();
		if(obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();
		
		if(obj.getClass().equals(toSearch)){
			result.add(obj);
		}else if(obj instanceof ContentAccessor){
			List<?> children = ((ContentAccessor) obj).getContent();
			for(Object child:children){
				result.addAll(getAllElementFromObject(child, toSearch));
			}
		}
		return result;
	}
	
	static Text focusOnRelaceHolder(WordprocessingMLPackage template, String placeholder){
		List<Object> texts = getAllElementFromObject(template.getMainDocumentPart(), Text.class);
		for(Object text: texts){
			Text textElement = (Text) text;
			if (textElement.getValue().equals(placeholder)){
				return textElement;
			}
		}
		return null;
	}
	
	static P focusOnReplaceHolderPara(WordprocessingMLPackage template, String placeholder){
		List<Object> paras = getAllElementFromObject(template.getMainDocumentPart(), P.class);
		for(Object p: paras){
			P para = (P) p;
			List<Object> paraTexts = getAllElementFromObject(para, Text.class);
			for(Object text: paraTexts){
				Text texts = (Text) text;
				if (texts.getValue().equals(placeholder)){
					return para;
				}
			}
		}
		return null;
	}
	@SuppressWarnings("unused")
	private static void replacePlaceHolder(WordprocessingMLPackage template, String name, String placeholder){
		List<Object> texts = getAllElementFromObject(template, Text.class);
		
		for(Object text:texts){
			Text textElement = (Text) text;
			if(textElement.getValue().equals(placeholder)){
				textElement.setValue(name);
			}
		}
	}
	
	static void writeDocxToStream(WordprocessingMLPackage template, String target) throws Docx4JException{
		String time_stamp = String.format("%tF", new Date());
		File f = new File(target + "/" + time_stamp + "-" +  "report" + ".docx");
		template.save(f);
	}
	
	private static void replaceParagraph(String placeholder, String textToAdd, WordprocessingMLPackage template, ContentAccessor addTo){
		List<Object> paragraphs = getAllElementFromObject(template.getMainDocumentPart(), P.class);
		
		P toReplace = null;
		for(Object p:paragraphs){
			List<Object> texts = getAllElementFromObject(p, Text.class);
			for(Object t:texts){
				Text content = (Text) t;
				if(content.getValue().equals(placeholder)){
					toReplace = (P)p;
					System.out.println(toReplace.equals(null));
					break;
				}
			}
		}
		
		String[] as = StringUtils.splitPreserveAllTokens(textToAdd, "\n");
		
		for(int i = 0; i < as.length; i ++){
			String ptext = as[i];
			P copy = (P) XmlUtils.deepCopy(toReplace);
			
			List<?> texts = getAllElementFromObject(copy, Text.class);
			if(texts.size() > 0){
				Text textToReplace = (Text)texts.get(0);
				textToReplace.setValue(ptext);
			}
			addTo.getContent().add(copy);
		}
		
		((ContentAccessor) toReplace.getParent()).getContent().remove(toReplace);
		
		
	}
	
	static Tc focusOnReplaceTc(WordprocessingMLPackage template, String placeholder){
		
		List<Object> rows = getAllElementFromObject(template.getMainDocumentPart(), Tr.class);
		for(Object row : rows){
			Tr tcrow = (Tr) row;
			List<Object> tcs = getAllElementFromObject(tcrow, Tc.class);
			for(Object tc : tcs){
				Tc cell = (Tc) tc;
				List<Object> texts = getAllElementFromObject(cell, Text.class);
				for(Object text:texts){
					Text tt = (Text)text;
					if(tt.getValue().equals(placeholder)){
						return cell;
					}
				}
			}
		}
		System.out.println("not found.");
		return null;
		
	}
	
	@SuppressWarnings("unused")
	private static void replaceTable(String[] placeholders, List<Map<String, String>> textsToAdd, 
			WordprocessingMLPackage template){
		List<Object> tables = getAllElementFromObject(template, Tbl.class);
		
		// 1. find the table
		Tbl tempTable = getTempTable(tables,placeholders[0]);
		List<Object> rows = getAllElementFromObject(template, Tr.class);
		
		if(rows.size() == 2){
			Tr templateRow = (Tr) rows.get(1);
			
			for(Map<String, String> placement:textsToAdd){
				addRowToTable(tempTable, templateRow,placement);
			}
			
			
			tempTable.getContent().remove(templateRow);
		}
		
	}

	private static void addRowToTable(Tbl tempTable, Tr templateRow, Map<String, String> placement) {
		// TODO Auto-generated method stub
		Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
		List<?> textElements = getAllElementFromObject(workingRow, Text.class);
		
		for(Object object:textElements){
			Text text = (Text) object;
			String replacementValue = placement.get(text.getValue());
			
			if(replacementValue != null){
				text.setValue(replacementValue);
			}
		}
		
		tempTable.getContent().add(workingRow);
		
	}

	private static Tbl getTempTable(List<Object> tables, String string) {
		// TODO Auto-generated method stub
		
		for(Iterator<Object> iter = tables.iterator();iter.hasNext();){
			Object tbl = iter.next();
			List<?> textElements = getAllElementFromObject(tbl, Text.class);
			
			for(Object text:textElements){
				Text textElement = (Text) text;
				if(textElement.getValue() != null && textElement.getValue().equals(string)){
					return (Tbl)tbl;
				}
			}
		}
		return null;
		
	}
	
}
