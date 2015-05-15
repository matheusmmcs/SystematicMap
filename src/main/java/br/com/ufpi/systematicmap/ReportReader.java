package br.com.ufpi.systematicmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import br.com.ufpi.systematicmap.enums.ClassificationEnum;
import br.com.ufpi.systematicmap.enums.FieldEnum;

public class ReportReader {

	private static final String PATH_START_REPORT = "docs/unclassified.xls";
	private static final String PATH_GENERATE_REPORT = "docs/classified.xls";
	
	private static final int LIMIAR_TITLE = 0;
	private static final int LIMIAR_ABS = 0;
	private static final int LIMIAR_KEYS = 0;
	private static final int LIMIAR_TOTAL = 4;
	
	private static final int LEVENSHTEIN = 5;//-1 dont calc levenshtein
	
	@SuppressWarnings("serial")
	private static Map<String, String> regexList = new HashMap<String, String>(){{
	    put("automatico", "(automat.*|semiautomati.*|semi-automati.*)");
	    put("web", "(web|website|internet|www)");
	    put("usabilidade", "(usability|usable)");
	    put("tecnica", "(evalu.*|assess.*|measur.*|experiment.*|stud.*|test.*|method.*|techni.*|approach.*)");
	}};
	private static List<Paper> papers = new LinkedList<Paper>();
	
	
	public static void main(String[] args) {
		
		//teste de regex
//		String s = "Teste";
//		Pattern pattern;
//		Matcher regexMatcher;
//		
//		Set<Entry<String, String>> set = regexList.entrySet();
//		for(Entry<String, String> entry : set){
//			String regex = entry.getValue();
//			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
//		    regexMatcher = pattern.matcher(s);
//		    
//		    while (regexMatcher.find()) {
//		    	System.out.println(regexMatcher.group(1));
//		    }
//		}
		
		
		
		boolean populatePapers = populatePapers(6);
		
		if(populatePapers){
			System.out.println("Total de artigos: "+papers.size());
			System.out.println("Probs autores: "+filterAuthors());
			filterRegex(LIMIAR_TITLE, LIMIAR_ABS, LIMIAR_KEYS, LIMIAR_TOTAL);
			System.out.println("Probs palavras: "+countPapers(ClassificationEnum.WORDS_DONT_MATCH));
			if(LEVENSHTEIN != -1){
				calcTitleLevenshteinDistance(LEVENSHTEIN);
			}
			System.out.println("Probs lenshtein: "+countPapers(ClassificationEnum.REPEAT));
			generateReport();
		}
		
	}
	
	@SuppressWarnings("resource")
	private static boolean populatePapers(int skipUnnecessaryRows){
		try{
			FileInputStream file = new FileInputStream(new File(PATH_START_REPORT));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheetAt(1);
			Iterator<Row> rowIterator = sheet.iterator();
			
			for(int i = 0; i < skipUnnecessaryRows; i++){
				rowIterator.next();
			}
			
			while (rowIterator.hasNext()){
				Row row = rowIterator.next();
				double id = row.getCell(0).getNumericCellValue();
				String title = row.getCell(1).getStringCellValue();
				String authors = row.getCell(3).getStringCellValue();
				String abs = row.getCell(4).getStringCellValue();
				String keys = row.getCell(11).getStringCellValue();
				papers.add(new Paper(id, title, abs, authors, keys));
			}
			
			file.close();
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	private static int filterAuthors() {
		int count = 0;
		for(Paper p : papers){
			if(p.getAuthors().equals("")){
				p.setClassification(ClassificationEnum.WITHOUT_AUTHORS);
				p.appendComment(ClassificationEnum.WITHOUT_AUTHORS.toString());
				count++;
			}
		}
		return count;
	}
	
	private static void filterRegex(int limiarTitle, int limiarAbs, int limiarKeys, int limiarTotal) {
		for(Paper p : papers){
			countRegex(p, FieldEnum.TITLE, limiarTitle);
			countRegex(p, FieldEnum.ABS, limiarAbs);
			countRegex(p, FieldEnum.KEYS, limiarKeys);
			
			if(p.getSetOfComments().size() < limiarTotal){
			//if(p.getRegexTotal() < limiarTotal){
				p.setClassification(ClassificationEnum.WORDS_DONT_MATCH);
				p.appendComment(ClassificationEnum.WORDS_DONT_MATCH.toString()+"-total contains only="+p.getStringSetOfComments()+";");
			}
		}
	}
	
	private static void calcTitleLevenshteinDistance(int limiar) {
		double count = 0, size = papers.size();
		for(Paper p : papers){
			int minDist = Integer.MAX_VALUE;
			Paper minDistPaper = null;
			String pTitle = p.getTitle().toLowerCase();
			
			for(Paper p2 : papers){
				if(p.getId() != p2.getId()){
					int dist = Utils.getLevenshteinDistance(pTitle, p2.getTitle().toLowerCase());
					
					if(minDist > dist){
						minDist = dist;
						minDistPaper = p2;
					}
				}
			}
			
			p.setMinLevenshteinDistance(minDist);
			p.setPaperMinLevenshteinDistance(minDistPaper);
			
			
			if(minDist <= limiar){
				p.setClassification(ClassificationEnum.REPEAT);
				p.appendComment(ClassificationEnum.REPEAT.toString());
			}
			count++;
			System.out.println("loading:"+(count/size)*100);
		}
	}
	
	private static void countRegex(Paper p, FieldEnum fieldEnum, int limiar) {
		String s = "";
		
		if (fieldEnum.equals(FieldEnum.ABS)) {
			s = p.getAbs();
		}else if (fieldEnum.equals(FieldEnum.TITLE)) {
			s = p.getTitle();
		}else if (fieldEnum.equals(FieldEnum.KEYS)) {
			s = p.getKeys();
		}
		
		Pattern pattern;
		Matcher regexMatcher;
		String comment = "";
		int count = 0;
		
		if(s != null && !s.equals("")){
			Set<Entry<String, String>> set = regexList.entrySet();
			for(Entry<String, String> entry : set){
				String regex = entry.getValue();
				pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			    regexMatcher = pattern.matcher(s);
			    
			    boolean containRegex = false;
			    while (regexMatcher.find()) {
			    	containRegex = true;
			    	//System.out.println(regexMatcher.group(1));
			    }
			    
			    if(containRegex){
			    	p.getSetOfComments().add(entry.getKey());
			    	count++;
			    }else{
			    	comment += entry.getKey()+", ";
			    }
			}
		}
		
		if (fieldEnum.equals(FieldEnum.ABS)) {
			p.setRegexAbs(count);
		}else if (fieldEnum.equals(FieldEnum.TITLE)) {
			p.setRegexTitle(count);
		}else if (fieldEnum.equals(FieldEnum.KEYS)) {
			p.setRegexKeys(count);
		}
		
		if(count < limiar){
			p.setClassification(ClassificationEnum.WORDS_DONT_MATCH);
			p.appendComment(ClassificationEnum.WORDS_DONT_MATCH.toString()+"-"+fieldEnum.toString()+" DONT contains=("+comment+");");
		}
	}
	
	private static int countPapers(ClassificationEnum ce){
		int count = 0;
		for (Paper paper : papers){
			if (paper.getClassification() != null && paper.getClassification().equals(ce)) {
				count++;
			}
		}
		return count;
	}
	
	@SuppressWarnings("resource")
	private static void generateReport(){
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Papers");
		List<Object[]> data = new LinkedList<Object[]>();
		
		for(Paper p : papers){
			data.add(p.toReport());
		}
		
		int rownum = 0;
		
		//create tile
		if(papers.size() > 0){
			Row row = sheet.createRow(rownum++);
		    int cellnum = 0;
		    for (Object obj : papers.get(0).toTitleReport()){
		       Cell cell = row.createCell(cellnum++);
		       cell.setCellValue(obj.toString());
		    }
		}
		
		//create body
		for (Object[] objArr : data){
		    Row row = sheet.createRow(rownum++);
		    int cellnum = 0;
		    for (Object obj : objArr){
		       Cell cell = row.createCell(cellnum++);
		       
		       if(obj != null){
		    	   if (obj instanceof Integer) {
			    	   cell.setCellValue((Integer)obj);
			       }else{
			    	   cell.setCellValue(obj.toString());
			       }
		       }
		    }
		}
		
		//generate numbers of report
		HSSFSheet sheet2 = workbook.createSheet("Numbers");
		
		//title
		int countCell = 0, rowCount = 0;
		Row row = sheet2.createRow(rowCount++);
		row.createCell(countCell++).setCellValue("Total");
		row.createCell(countCell++).setCellValue(ClassificationEnum.WITHOUT_AUTHORS.toString());
		row.createCell(countCell++).setCellValue(ClassificationEnum.WORDS_DONT_MATCH.toString());
		row.createCell(countCell++).setCellValue(ClassificationEnum.REPEAT.toString());
		row.createCell(countCell++).setCellValue("Final");
		row.createCell(countCell++).setCellValue("");
		row.createCell(countCell++).setCellValue("LimiarTitle");
		row.createCell(countCell++).setCellValue("LimiarAbs");
		row.createCell(countCell++).setCellValue("LimiarKeys");
		row.createCell(countCell++).setCellValue("LimiarTotal");
		row.createCell(countCell++).setCellValue("Levenshtein");
		
		
		//values
		countCell = 0;
		int countWA = countPapers(ClassificationEnum.WITHOUT_AUTHORS),
			countDMW = countPapers(ClassificationEnum.WORDS_DONT_MATCH),
			countR = countPapers(ClassificationEnum.REPEAT);
		
		row = sheet2.createRow(rowCount++);
		row.createCell(countCell++).setCellValue(papers.size());
		row.createCell(countCell++).setCellValue(countWA);
		row.createCell(countCell++).setCellValue(countDMW);
		row.createCell(countCell++).setCellValue(countR);
		row.createCell(countCell++).setCellValue(papers.size()-(countWA+countDMW+countR));
		row.createCell(countCell++).setCellValue("");
		row.createCell(countCell++).setCellValue(LIMIAR_TITLE);
		row.createCell(countCell++).setCellValue(LIMIAR_ABS);
		row.createCell(countCell++).setCellValue(LIMIAR_KEYS);
		row.createCell(countCell++).setCellValue(LIMIAR_TOTAL);
		row.createCell(countCell++).setCellValue(LEVENSHTEIN);
		
		
		try{
		    FileOutputStream out = new FileOutputStream(new File(PATH_GENERATE_REPORT));
		    workbook.write(out);
		    out.close();
		    System.out.println(PATH_GENERATE_REPORT+" written successfully on disk.");
		}catch (Exception e){
		    e.printStackTrace();
		}
	}

}
