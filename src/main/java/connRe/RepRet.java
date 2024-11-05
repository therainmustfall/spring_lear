package connRe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;

// 参考https://www.cnblogs.com/javaDeveloper/p/13141415.html

/*
 * 根据人员信息表，到数据库匹配相应文献信息
 * args[0] 人员信息文件，默认tab分隔符结构：
 * 入职年份/姓名/状态/部门/性别/出生/邮箱
 * 
 * 
 */
public class RepRet {

	Connection con;
	List<Person> persons;
	List<String> uts;
	String startYear;
	String endYear;

	public RepRet(List<Person> prsns, List<String> uts, String startYear, String endYear) throws IllegalAccessException
	{
		if ((startYear != null && endYear != null && startYear.compareTo(endYear) > 0) || (prsns == null && uts == null && startYear == null && endYear == null))
			throw new IllegalAccessException("年份顺序错误");
		this.uts         = uts;
		this.persons     = prsns;
		this.startYear   = startYear;
		this.endYear     = endYear;
		InitCon();
	}
	private void InitCon()
	{
		try {
			// Establish the connection.
			//String remoteConnectionUrl = "jdbc:sqlserver://ip_addr:1433;databaseName=;userName=sa;Password=;";
			
			Properties prop = new Properties();
			prop.load(getInputStream("connection.properties"));
			String connectionURL = prop.getProperty("url") + ";databaseName=" + prop.getProperty("databaseName") + ";userName=sa;Password=" + prop.getProperty("password") + ";";
			//integratedSecurity
			// String connectionURL = "jdbc:sqlserver://127.0.0.1:1433;databaseName=ir;integratedSecurity=true;";
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection(connectionURL);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	// Unit Test
	public static void main(String[] args) throws FileNotFoundException, IOException, IllegalAccessException {

		String startYear = System.getProperty("startYear");
		String endYear   = System.getProperty("endYear");

		List<Person> psn = null;
		//ut入藏号文件
		List<String> uts = null;

		int argsLength = args.length;
		if (argsLength > 0)
		{
			for (int s = 0; s < argsLength-1; s++)
			{
				if (args[s].indexOf("-person") >= 0)
				{
					Path path = Paths.get(args[s+1]);
					List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
					psn = new ArrayList<Person>();
					for (String rec : lines)
					{
						String[] recs = rec.split("\t");
						Person person = new Person();
						person.setYear(recs[0].trim());
						person.setName(recs[1].trim());
						person.setStat(recs[2].trim());
						person.setDept(recs[3].trim());
						person.setGender(recs[4].trim());
						person.setBirth(recs[5].trim());
						person.setEmail(recs[6].trim());
						psn.add(person);
					}
				}

				if (args[s].contains("-ut"))
				{
					uts = new ArrayList<String>();
					Scanner scanner = new Scanner(new File(args[s+1]));
					while (scanner.hasNext())
						uts.add(scanner.next().trim());	
				}
			}
		}
		System.out.println(psn != null ? "persons number: " + psn.size() : "未读取人员信息");
		if (!(startYear == null && endYear == null))
		{
			RepRet repRet = new RepRet(psn,uts,startYear,endYear);
			repRet.getTable("./");
		}      	
	}

	public void getTable(String pathString)
	{
		String depName = (persons != null ? "人数(" + persons.size() + ")":"全部人员") +
						 (uts != null ? "-收录编号(" + uts.size() + ")":"-编号未定") + 
						 (startYear != null ? "-"+startYear:"") +  
						 (endYear != null ? "-"+endYear:"") + ".txt";
		try {
			// 检查是否有年份限制
			String startAndEndYear = !startYear.isEmpty() && !endYear.isEmpty() ? 
									" where years >= '" + startYear + "' and years <= '" + endYear + "' " : 
									(!startYear.isEmpty() && endYear.isEmpty() ?
									"where years >= '" + startYear + "' " : 
									(startYear.isEmpty() && !endYear.isEmpty() ?
									"where years <= '" + endYear + "' ":
									""
									));
			// 包含年份限制的查询语句，title info表格插入临时表
			String insertTitleToTempTable = "select "+ 
			"distinct a.lngid, a.type,a.doi, a.language, a.woscategory, a.esicategory," + 
			" a.title_e, a.showwriter,a.showorgan,a.media_e, a.issn," + 
			" a.years,a.publishdate,a.vol,a.num,a.pages," + 
			"a.corrauthor, a.corrorgan,a.organs," + 
			"a.firstwriterid,a.firstorganid,a.firstorgan," + 
			"a.writerids,a.organids,a.writers, a.abbautho, a.writer_text," + 
			"a.includeid,a.title_c,a.e_mail,a.media_c,a.impactfactor," + 
			"a.beginpage,a.endpage,a.jumppage,a.pagecount,a.remark_c,a.remark_e, " + 
			"a.keyword_c,a.keyword_e,a.esiclassids,a.ref_cnt "+ 
			" into temp_titleinfo from titleinfo a " + startAndEndYear;
			

			ScriptRunner sr = new ScriptRunner(con);
			sr.setSendFullScript(false);
			sr.setDelimiter(";;");
			sr.setAutoCommit(true);
			
			SqlRunner sqr = new SqlRunner(con);
			
			//开始查询，建立临时人员id对应表， 人员信息表，机构关系表
			sr.runScript(getInputStream("tes.sql"));
//			Path path = Paths.get("tes.sql");
			System.out.println(this.getClass().getClassLoader().getResource("tes.sql").getPath());
			Path path = Path.of(this.getClass().getClassLoader().getResource("tes.sql").getPath().substring(1));
			

			// 导入人员信息
			String cted = "";
			if (!persons.isEmpty()){
				String createPersonTable = "create TABLE temp_tlts (\n" + 
											"year varchar(max),\n"  + 
					"name varchar(max),\n" + 
					"stat varchar(max),\n" +
					"dept varchar(max),\n" + 
					"gender varchar(max),\n" + 
					"birth  varchar(max),\n" + 
					"email  varchar(max));\n" + ";\n\n;;\n\n";
				String insertPerson = createPersonTable + "insert into temp_tlts values ";
				for (Person p : this.persons)
					insertPerson += "('" + p.getYear() + "','" + p.getName() + "','" + p.getStat() + "','" + p.getDept() + "','" + p.getGender() + "','" + p.getBirth() + "','" + p.getEmail() +  "'),";
				
				insertPerson = insertPerson.substring(0, insertPerson.length()-1);
				
				// 匹配名称各种形式
				cted = insertPerson + 
				";\n\n;;\n\n" + 
				"  select UPPER(a.name) as name, a.year, a.dept, a.email, UPPER(b.othername) as othername into temp_name_var from temp_tlts a inner join writerdic b on UPPER(a.name)=UPPER(b.writer)" + 
				";\n\n;;\n\n";
			}

			if (!uts.isEmpty()) {
				String insertUts = "CREATE TABLE temp_wosid (id varchar(22)); \n\n;;\n\n ";							
				int utsLen = uts.size();
				int lpConter = (utsLen / 1000) + 1;
				for (int j = 0; j < lpConter; j++)
				{
					insertUts = insertUts + ";;\n\n insert into temp_wosid values ";
					for (int i = j*1000; i < (j+1)*1000 && i < utsLen; i++)
					{
						insertUts += "('" + uts.get(i) + "'),";
					}	
					insertUts = insertUts.substring(0, insertUts.length()-1) + "; \n\n ;; \n\n";
				}
				cted = cted + insertUts;
				cted = cted.substring(0, cted.length()-1);
			}
			
			// titleinfo 添加到临时表
			File tempFile = new File("tempFile.txt");
			tempFile.createNewFile();
			OutputStream tpbw = new FileOutputStream("tempFile.txt");
			tpbw.write(java.nio.file.Files.readAllBytes(path));
			cted = "\n\n;;\n\n" + cted + insertTitleToTempTable +  ";\n\n;;\n\n";
			tpbw.write(cted.getBytes());
			tpbw.close();
			BufferedReader br = new BufferedReader(new FileReader(tempFile));
			sr.runScript(br);
			br.close();
			tempFile.delete();
			
			// 机构作者归属
			List<Map<String,Object>> orgs = sqr.selectAll("select * from temp_deptstd;");
			List<Map<String,Object>> writers = sqr.selectAll("select * from temp_writer_table;");
			// List<Map<String,Object>> writer_id_dept = sqr.selectAll("select writerid, organ from writerinfo where len(organidlevel) - len(replace(organidlevel,';','')) = 2;");

			// 判断ut和人员是否为空
			String titleSubSet = "";
			if (!persons.isEmpty() && uts.isEmpty())
			{
				titleSubSet = "select "+ 
				"distinct a.lngid 机构库文献编号,a.doi DOI, a.type 文献类型,a.language 语言, a.woscategory WoS类别, a.esicategory ESI类别," + 
				" a.title_e 题名, c.name 所属人,c.year 年度, c.dept 所在学院,c.email 参考邮箱地址," + 
				"a.showwriter 作者,a.showorgan 机构,a.media_e 来源期刊, a.issn," + 
				" a.years 发表年份,a.publishdate 年月,a.vol 卷,a.num 期,a.pages 起始页码," + 
				"a.corrauthor 通讯作者识别,a.corrorgan 通讯机构, a.corrorgan 通讯机构识别," + 
				"a.firstwriterid 第一作者识别,a.firstorgan 第一机构, a.firstorganid 第一机构识别,"
				+ "a.writerids 其他作者识别,a.organids  其他机构识别,"
				+ "a.includeid 数据库收录编号,a.ref_cnt 被引次数,a.title_c 中文标题,a.e_mail 电子邮件地址,a.media_c 中文来源,a.impactfactor 影响因子," + 
		 		"a.beginpage 开始页,a.endpage 结束页,a.jumppage 跳转页,a.pagecount 页数,a.remark_c 中文摘要,a.remark_e 英文摘要, " + 
				"a.keyword_c 中文关键词,a.keyword_e 英文关键词,a.esiclassids ESI分类ID"+ 
				" from temp_tlts c inner join temp_titleinfo a " + 
				"on ( " + 
				"charindex(c.email, a.e_mail) > 0 or " + 
				"(charindex(upper(c.name), UPPER(a.writers))> 0 and charindex(c.dept,a.organs) > 0)" + 
				") ";
			}
			if (!persons.isEmpty() && !uts.isEmpty())
			{
				titleSubSet = "select " + 
				"distinct b.id 收录号, a.doi DOI,a.lngid 机构库文献编号,a.type 文献类型, a.language 语言, a.woscategory WoS类别, a.esicategory ESI类别," + 
				" a.title_e 题名, c.name 所属人,c.year 年度, c.dept 所在学院,c.email 参考邮箱地址," + 
				"a.showwriter 作者,a.showorgan 机构,a.media_e 来源期刊, a.issn," + 
				" a.years 发表年份,a.publishdate 年月,a.vol 卷,a.num 期,a.pages 起始页码," + 
				"a.corrauthor 通讯作者识别, a.corrorgan 通讯机构识别,a.corrorgan 通讯机构," + 
				"a.firstwriterid 第一作者识别,a.firstorgan 第一机构,a.firstorganid 第一机构识别,"
				+ "a.writerids 其他作者识别,a.organids  其他机构识别,"
				+ "a.includeid 数据库收录编号,a.ref_cnt 被引次数,a.title_c 中文标题,a.e_mail 电子邮件地址,a.media_c 中文来源,a.impactfactor 影响因子," + 
		 		"a.beginpage 开始页,a.endpage 结束页,a.jumppage 跳转页,a.pagecount 页数,a.remark_c 中文摘要,a.remark_e 英文摘要, " + 
				"a.keyword_c 中文关键词,a.keyword_e 英文关键词,a.esiclassids ESI分类ID"+ 
				" from temp_tlts c inner join temp_titleinfo a " + 
				"on ( " + 
				"charindex(c.email, a.e_mail) > 0 or" + 
				" (charindex(upper(c.name), UPPER(a.writers))> 0 and charindex(c.dept,a.organs) > 0)" + 
				") inner join temp_wosid b " + 
				"on (charindex(b.id, a.includeid) > 0) ";
			}

			if (persons.isEmpty() && !uts.isEmpty())
			{
				titleSubSet = "select "+ 
				"distinct c.id 收录号, a.doi DOI,a.lngid 机构库文献编号,a.type 文献类型, a.language 语言, a.woscategory WoS类别, a.esicategory ESI类别," + 
				" a.title_e 题名, " + 
				"a.showwriter 作者,a.showorgan 机构,a.media_e 来源期刊, a.issn," + 
				" a.years 发表年份,a.publishdate 年月,a.vol 卷,a.num 期,a.pages 起始页码," + 
				"a.corrauthor 通讯作者识别, a.corrorgan 通讯机构, a.corrorgan 通讯机构识别," + 
				"a.firstwriterid 第一作者识别,a.firstorgan 第一机构,a.firstorganid 第一机构识别,"
				+ "a.writerids 其他作者识别,a.organids  其他机构识别,"
				+ "a.includeid 数据库收录编号,a.ref_cnt 被引次数,a.title_c 中文标题,a.e_mail 电子邮件地址,a.media_c 中文来源,a.impactfactor 影响因子," + 
		 		"a.beginpage 开始页,a.endpage 结束页,a.jumppage 跳转页,a.pagecount 页数,a.remark_c 中文摘要,a.remark_e 英文摘要, " + 
				"a.keyword_c 中文关键词,a.keyword_e 英文关键词,a.esiclassids ESI分类ID"+ 
				" from temp_wosid c inner join temp_titleinfo a " + 
				"on (charindex(c.id, a.includeid) > 0) ";
			}

			if (persons.isEmpty() && uts.isEmpty())
			{
				titleSubSet = "select "+ 
				"distinct a.lngid 机构库文献编号,a.doi DOI, a.type 文献类型, a.language 语言,a.woscategory WoS类别, a.esicategory ESI类别," + 
				" a.title_e 题名, " + 
				"a.showwriter 作者,a.showorgan 机构,a.media_e 来源期刊, a.issn," + 
				" a.years 发表年份,a.publishdate 年月,a.vol 卷,a.num 期,a.pages 起始页码," + 
				"a.corrauthor 通讯作者识别, a.corrorgan 通讯机构识别," + 
				"a.firstwriterid 第一作者识别,a.corrorgan 通讯机构, a.firstorgan 第一机构,a.firstorganid 第一机构识别,"
				+ "a.writerids 其他作者识别,a.organids  其他机构识别,"
				+ "a.includeid 数据库收录编号,a.ref_cnt 被引次数,a.title_c 中文标题,a.e_mail 电子邮件地址,a.media_c 中文来源,a.impactfactor 影响因子," + 
		 		"a.beginpage 开始页,a.endpage 结束页,a.jumppage 跳转页,a.pagecount 页数,a.remark_c 中文摘要,a.remark_e 英文摘要, " + 
				"a.keyword_c 中文关键词,a.keyword_e 英文关键词,a.esiclassids ESI分类ID"+ 
				" from temp_titleinfo a  ";
			}

			System.out.println("full selection: " + titleSubSet);
			List<Map<String, Object>> rsq = sqr.selectAll(titleSubSet); 
			
			System.out.println("条数: " + rsq.size());
			
			//jdk 17
			String dfName = pathString + depName;
			File df = new File(dfName);
			File parent = new File(df.getParent());
			if (!parent.exists()) parent.mkdirs();
				
			System.out.println(dfName);
			df.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(pathString + depName, StandardCharsets.UTF_8));
			/*
			var fields = new String[] {"机构库文献编号", "WoS类别","ESI类别","题名","所属人",
										"年度", "所在学院","参考邮箱地址","作者","机构",
										"来源期刊","issn", "发表年份","年月","卷","期",
										"起始页码","通讯作者识别","通讯机构识别","第一作者识别",
										"第一机构识别","其他作者识别","其他机构识别","作者类型","数据库收录编号","被引次数",
										"中文标题","电子邮件地址","中文来源","影响因子","开始页","结束页", 
										"跳转页","页数","中文摘要","英文摘要","中文关键词","英文关键词","ESI分类ID"};
			 */
			List<String> fields = new ArrayList<String>();
			if (!rsq.isEmpty()) {
				fields.addAll(rsq.get(0).keySet());
				for (int i = 0; i < fields.size(); i++)
				{
					if (i == 0) bw.write(fields.get(0));
					else        bw.write("\t" + fields.get(i));
				}
			}

			bw.write(!persons.isEmpty() ? "\t作者类型\t\n" : "\n");
			// var ab = new BufferedWriter(new FileWriter(pathString + "enabs.txt", StandardCharsets.UTF_8));

			for(Map<String,Object> rec: rsq) {
				
				String publishType = "";
				String name = "";
				if (rec.containsKey("所属人")) name = rec.get("所属人").toString();

				String coauthor =  (String) rec.get("通讯作者识别");
				List<String> coids = extractIds(coauthor);
				List<String> aus = new ArrayList<String>();
				//--------------------------------
				
				String coads = (String) rec.get("通讯机构识别");
				List<String> cogids = extractIds(coads);
				List<String> ogs = new ArrayList<String>();
				
				//--------------------------------
				 String firstAu = (String) rec.get("第一作者识别");
				 
				 List<String> firstaus = splitIds(firstAu);
				 List<String> firstausl = new ArrayList<String>();
				 
				 //--------------------------------
				 String firstOg = (String) rec.get("第一机构识别");
				 List<String> firstOgs = splitIds(firstOg);
				 List<String> ftogs = new ArrayList<String>();

				Pattern sp = Pattern.compile("\\s{2,}|\t|\r|\n");

				for (String field: fields)
				{	
					String recString = rec.get(field.toUpperCase()).toString();
					Matcher sm = sp.matcher(recString);

					String content = sm.replaceAll("");
					// if (field.equals("英文摘要")) ab.write(content + "---------" + "\n");
					
					if (field.equals("作者") || field.equals("机构"))  {
						bw.write(content.substring(0,Math.min(content.length(),2000)) + "\t"); 
					} else if (field.equals("通讯作者识别"))
					{
						if (coids != null)
						{
							for (String id: coids)
							{
								for (Map<String, Object> writer: writers) {
									if (writer.get("WRITERID") != null && 
											writer.get("WRITERID").toString().equals(id)) {
										String ename = writer.get("WRITER").toString();
										aus.add(ename);
										if (ename.equals(name)) publishType += "通讯作者";
									}
								}
							}
							coauthor = joinList(aus);
							if (aus.size() > 1 && publishType.length() > 0) publishType += "(共同)";
						}
						bw.write(coauthor+ "\t");
					} else if (field.equals("通讯机构识别"))
					{
						if (cogids != null)
						{
							for (String id: cogids)
							{
								for (Map<String, Object> cogid: orgs) {
									if (cogid.get("ORGANID") != null && 
									  cogid.get("ORGANID").toString().equals(id)) ogs.add(cogid.get("ORGAN").toString());
								}
							}
							coads = joinList(ogs);		
						}
						bw.write(coads + "\t");				
					} else if (field.equals("第一作者识别"))
					{
						 if (firstaus != null)
						 {
							 for (String id: firstaus)
							 {
								 for (Map<String, Object> writer: writers)
								 {
									 if (writer.get("WRITERID") != null && 
									   writer.get("WRITERID").toString().equals(id)) {
										 String ename = writer.get("WRITER").toString();
										 firstausl.add(ename);
										 if (ename.equals(name)) {
											 if (publishType.length() == 0) publishType += "第一作者";
											 else 						    publishType += "，第一作者";
										 }
									 }
								 }
						 }
						}
						 if (firstausl.size() > 1 && publishType.indexOf("第一作者") > 0) publishType += "(共同)";
						 bw.write(joinList(firstausl)+ "\t");
					} else if (field.equals("第一机构识别"))
					{
							if (firstOgs != null)
							{
								for (String ogid: firstOgs)
								{
									for (Map<String, Object> gid: orgs)
									{
										if (gid.get("ORGANID") != null && 
										  gid.get("ORGANID").toString().equals(ogid)) 
										{
											// System.out.println("first organization: " + firstOg);
											// System.out.println("ogids: " + firstOgs +" , identified ogid ：" + ogid + ", matches " + gid.get("ORGAN").toString());
											ftogs.add(gid.get("ORGAN").toString());
										}
									}
								}
							}
							bw.write(joinList(ftogs) + "\t");
					} else if (field.equals("其他作者识别"))
					{
						//--------------------------------
							String otheraus = (String) rec.get("其他作者识别");
							
							List<String> otherausl = splitIds(otheraus);
							List<String> otherault = new ArrayList<String>();
							if (otherausl != null)
							{
								for (String id: otherausl)
								{
									String realId = id.split("@")[0].trim();
									for (Map<String, Object> writer: writers)
									{
										if (writer.get("WRITERID") != null && writer.get("WRITERID").toString().equals(realId))
										{
											if ((firstaus == null || 
											  !firstaus.contains(realId)) && 
											  (coids == null || !coids.contains(realId))) {
												String ename = writer.get("WRITER").toString();
												otherault.add(ename);
												if (ename.equals(name) && publishType.length() == 0) publishType="合作者"; 
											}
										}
									}
								}
							}
							bw.write(joinList(otherault) + "\t");
					} else if (field.equals("其他机构识别"))
					{
						//--------------------------------
							
							String otherog = (String)rec.get("其他机构识别");
							List<String> otherOgs = splitIds(otherog);
							List<String> otogs = new ArrayList<String>();
							if (otherOgs != null)
							{
								for (String ogid: otherOgs)
								{
									for (Map<String, Object> gid: orgs)
									{
										if (gid.get("ORGANID") != null && gid.get("ORGANID").toString().equals(ogid))
										{
											if ((firstOgs == null || 
											  !firstOgs.contains(ogid)) && 
											  (cogids == null || !cogids.contains(ogid) )) otogs.add(gid.get("ORGAN").toString());
										}
									}
								}
							}
							bw.write(joinList(otogs) + "\t");
					}else bw.write(content.replace("\n","").replace("\"","").replace("\t","") + "\t");
				}
				bw.write(!persons.isEmpty() ? publishType + "\t\n" : "\n");				
			}
			
			bw.close();
			// ab.close();
			sr.runScript(getInputStream("drop.sql"));
		 	System.out.println("Done.");
	 }
	  catch (Exception e) {
		  e.printStackTrace();
	  } finally {
		  if (con != null)  try { con.close();  } catch(Exception e) {}
	  }
	}
	private InputStreamReader getInputStream(String s)
	{
		return  new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(s));
	}
	private static List<String> extractIds(String ids)
	{
		if (ids.equals("null") || ids == null || ids.equals("") || ids.length() == 0) return null;
		List<String> ls = new ArrayList<String>();
		
		Pattern p = Pattern.compile("\\[[a-z0-9]+\\]");
		Matcher m = p.matcher(ids);
		while(m.find()) {
			ls.add(ids.substring(m.start(), m.end()).replace("[", "").replace("]", ""));
		}
		return ls;
	}
	private static List<String> splitNumIds(String ids)
	{
		if (ids.equals("null") || ids == null || ids.equals("") || ids.length() == 0) return null;
		List<String> ls = new ArrayList<>();
		
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(ids);
		while(m.find())
		{
			ls.add(ids.substring(m.start(), m.end()));
		}
		return ls;
	}
	private static List<String> splitIds(String ids)
	{
		if (ids.equals("null") || ids == null || ids.equals("") || ids.length() == 0)  return null;
		return Arrays.asList(ids.split(";"));
	}
	private static String joinList(List<String> ls)
	{
		if (ls == null || ls.size() == 0) return "";
		ls = ls.stream().distinct().collect(Collectors.toList());
		StringBuilder sr = new StringBuilder();
		for (int i = 0; i < ls.size(); i++)
		{
			if (i == 0) sr.append(ls.get(i)); 
			else sr.append(","+ls.get(i));
		}
		return sr.toString();
	}
}
