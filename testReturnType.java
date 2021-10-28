
import java.io.File;
import java.io.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.sql.ResultSetMetaData;

public class testReturnTypeT4 {
	static Connection c;
	static PreparedStatement ps;
	static Statement st;
	static String query;
	
	static String url;
	static String usr;
	static String pwd;
	static String catalog;
	static String schema;
	static String machine_name;
	static String port_no;
	static String dataSource;
	
	public static Properties props;
	
	public static void main(String[] args) throws Exception {
		if(args.length < 8) {
			System.out.println("\nPlease specify the sql file name that contains the queries to be tested, machine name, port no., user, pwd, catalog, schema, serverDataSource");
			System.out.println("\njava testReturnTypeT2 test.sql super.super atpr372 cat1 sch1");
			System.out.println("Exiting program ... \n\n");
			System.exit(0);
		}
		
		machine_name = args[1];
		port_no = args[2];
		usr = args[3];
		pwd = args[4];
		catalog = args[5];
		schema = args[6];
		dataSource = args[7];
		
		url = "jdbc:t4sqlmx://"+machine_name+".in.rdlabs.hpecorp.net:"+port_no+"/:user="+usr+";password="+pwd+";catalog="+catalog+";schema="+schema+";serverDataSource="+dataSource;
		System.out.println("URL for getting connection: " +url);
		System.out.println();

		try {
			Class.forName("com.tandem.t4jdbc.SQLMXDriver");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(0);
		}


		c = DriverManager.getConnection(url);
		System.out.println("Connection established, beginning tests...\n");
		st = c.createStatement();
		System.out.println("Catalog : "+catalog);
		System.out.println("Schema : "+schema);
		
		try{
			st.executeUpdate("drop schema "+catalog+"."+schema+" cascade;");
		} catch(SQLException e) {
			System.out.println(e.getMessage());
		}
		try{
			st.executeUpdate("drop catalog "+catalog+" ");
		} catch(SQLException e) {
			System.out.println(e.getMessage());
		}
		try{
			st.executeUpdate("create catalog "+catalog);
		} catch(SQLException e) {
			System.out.println(e.getMessage());
		}
		try{
			st.executeUpdate("create schema "+catalog+"."+schema);
		} catch(SQLException e) {
			System.out.println(e.getMessage());
		}
		try{
			st.executeUpdate("set schema "+catalog+"."+schema);
		} catch(SQLException e) {
			System.out.println(e.getMessage());
		}
			
		
		File file = new File(args[0]);
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			
			String query = br.readLine();
			while (query != null) {
				query = query.trim();
				query = query.toUpperCase();
				if(query.length() == 0) {
					System.out.println();
					query = br.readLine();
					continue;
				}
				if(query.startsWith("--") == true){
					System.out.println(query);
					query = br.readLine();
					continue;
				}
				if(query.startsWith("SELECT") == true){
					System.out.println("\nQUERY --> "+query);
				
					try{
						PreparedStatement pstmt = c.prepareStatement(query);
						ResultSet rs = pstmt.executeQuery();
						ResultSetMetaData rsmd=rs.getMetaData();
						int col_count = rsmd.getColumnCount();
						while(rs.next()){
							for(int cc=1;cc<=col_count;cc++){
								System.out.print("\tReturn value"+cc+" --> "+rs.getObject(cc));
							}
							System.out.println();
						}
						
						for (int i = 1; i <= rsmd.getColumnCount(); i++) {
							System.out.print("\tRETURN PRECISION --> "+rsmd.getPrecision(i) + " ");
							System.out.print("\tRETURN NULLABILITY --> "+rsmd.isNullable(i) + " ");
							System.out.print("\tRETURN SIGNED --> "+rsmd.isSigned(i) + " ");
							System.out.print("\tRETURN COL SIZE --> "+rsmd.getColumnDisplaySize(i) + " ");
							System.out.print("\tRETURN SCALE --> "+rsmd.getScale(i) + " ");
							System.out.print("\tRETURN CLASS --> "+rsmd.getColumnClassName(i) + " ");  
							System.out.print("\tRETURN TYPE --> "+rsmd.getColumnType(i) + " ");
							System.out.println("\tRETURN TYPE NAME --> "+rsmd.getColumnTypeName(i));  
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
				
				if(query.startsWith("INSERT") == true || query.startsWith("UPDATE") == true || query.startsWith("DELETE") == true || query.startsWith("CREATE") == true || query.startsWith("DROP") == true || query.startsWith("EXPLAIN") == true){ 
					System.out.println("\nQUERY --> "+query);
				
					try{
						Statement stmt = c.createStatement();
						stmt.executeUpdate(query);
						
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
				if(query.startsWith("EXECUTE") == true || query.startsWith("SET") == true ){ 
					System.out.println("\nQUERY --> "+query);
					System.out.println("\tExecute and Set statements are not supported");
					query = br.readLine();
					continue;
				}
				
				if(query.startsWith("PREPARE") == true && query.contains("? ")==false && query.contains("?,")==false && query.contains("?)")==false){ 
					System.out.println("\nQUERY --> "+query);
					System.out.println("\tQueries with named parameters are not supported");
					query = br.readLine();
					continue;
				}
				if(query.startsWith("PREPARE") == true && (query.contains("? ")==true || query.contains("?,")==true || query.contains("?)")==true ) && (query.contains("INSERT")==true || query.contains("SELECT")==true)){
					
					System.out.println("\nQUERY --> "+query);
					if(query.contains("INSERT") == true){
						query = query.substring(query.indexOf("INSERT"));
						System.out.println("\nMODIFIED QUERY --> "+query);
						executeUnnamedParamsInsert(query);
					}
					else if(query.contains("SELECT") == true) {
						query = query.substring(query.indexOf("SELECT"));
						System.out.println("\nMODIFIED QUERY --> "+query);
						executeUnnamedParamsSelect(query);
					}
					
				}
				
		
		
				query = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		

	}
	
	
	private static void executeUnnamedParamsSelect(String query) throws SQLException {
        
        ArrayList<String> lookup_arr = new ArrayList<String>();
		ArrayList<String> lookup_arr_type = new ArrayList<String>();
		
		int queryCount = 0;
		int errorqueryCount = 0;
				
		boolean isTypeInterval = false;
		try{
			lookup_arr.clear();
			lookup_arr_type.clear();
			PreparedStatement pstmt = c.prepareStatement(query);
			
			ParameterMetaData prmd = pstmt.getParameterMetaData();
			System.out.println("\tPARAMETER COUNT --> "+prmd.getParameterCount());
			for (int i = 1; i <= prmd.getParameterCount(); i++) { 
				
				String classname = prmd.getParameterClassName(i);
				String typename = prmd.getParameterTypeName(i);
		
				classname = classname.substring(classname.lastIndexOf(".")+1);
				System.out.println("\t\tCLASS --> "+classname+", TYPE --> "+typename);
				String temp = i + ","+classname;
				lookup_arr.add(temp);
				lookup_arr_type.add(typename);
				
			}
			
			System.out.println();
			int size = lookup_arr.size();
			
			System.out.println("Execute xx using values:");
			System.out.println("-------------------------");
			if(size==1) {
				ResultSet rs = null;
				ResultSetMetaData rsmd = null;
				String temp1 = lookup_arr.get(0) ;
				String class1 = temp1.substring(temp1.indexOf(",")+1);
				String type1 = lookup_arr_type.get(0);
				int i1 = 1;
				
				if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == false){
					for(int k=0; k<=string_dt.length;k++){
						System.out.print("PASSING --> "+string_dt[k]+"\t");
						try {
							queryCount++;
							pstmt.setString(i1,string_dt[k]);
							rs = pstmt.executeQuery();
							rsmd=rs.getMetaData();
							int col_count = rsmd.getColumnCount();
							while(rs.next()) {
								for(int cc=1;cc<=col_count;cc++){ 
									System.out.print("RETURN VALUE"+cc+" --> "+rs.getObject(cc) + "\t");
								}
								System.out.println();
							}
							
							for (int rscol = 1; rscol <= rsmd.getColumnCount(); rscol++) {
								System.out.print("\tRETURN PRECISION --> "+rsmd.getPrecision(rscol) + " ");
								System.out.print("\tRETURN NULLABILITY --> "+rsmd.isNullable(rscol) + " ");
								System.out.print("\tRETURN SIGNED --> "+rsmd.isSigned(rscol) + " ");
								System.out.print("\tRETURN COL SIZE --> "+rsmd.getColumnDisplaySize(rscol) + " ");
								System.out.print("\tRETURN SCALE --> "+rsmd.getScale(rscol) + " ");
								System.out.print("\tRETURN CLASS --> "+rsmd.getColumnClassName(rscol) + " ");  
								System.out.print("\tRETURN TYPE --> "+rsmd.getColumnType(rscol) + " ");
								System.out.println("\tRETURN TYPE NAME --> "+rsmd.getColumnTypeName(rscol));   
								            
							}
						} catch (Exception e) {
								errorqueryCount++;
								System.out.println("ERROR --> "+e.getMessage());
						}
					}
				} else if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == true){
					for(int k=0; k<=interval_dt.length;k++){
						System.out.print("PASSING --> "+interval_dt[k]+"\t");
						try {
							queryCount++;
							pstmt.setString(i1,interval_dt[k]);
							rs = pstmt.executeQuery();
							rsmd=rs.getMetaData();
							int col_count = rsmd.getColumnCount();
							while(rs.next()) {
								for(int cc=1;cc<=col_count;cc++){ 
									System.out.print("RETURN VALUE"+cc+" --> "+rs.getObject(cc) + "\t");
								}
								System.out.println();
							}
						
							for (int rscol = 1; rscol <= rsmd.getColumnCount(); rscol++) {
								System.out.print("\tRETURN PRECISION --> "+rsmd.getPrecision(rscol) + " ");
								System.out.print("\tRETURN NULLABILITY --> "+rsmd.isNullable(rscol) + " ");
								System.out.print("\tRETURN SIGNED --> "+rsmd.isSigned(rscol) + " ");
								System.out.print("\tRETURN COL SIZE --> "+rsmd.getColumnDisplaySize(rscol) + " ");
								System.out.print("\tRETURN SCALE --> "+rsmd.getScale(rscol) + " ");
								System.out.print("\tRETURN CLASS --> "+rsmd.getColumnClassName(rscol) + " ");  
								System.out.print("\tRETURN TYPE --> "+rsmd.getColumnType(rscol) + " ");
								System.out.println("\tRETURN TYPE NAME --> "+rsmd.getColumnTypeName(rscol));                
							}
						} catch (Exception e) {
								System.out.println("ERROR --> "+e.getMessage());
								errorqueryCount++;
						}
					}
				} else if(class1.equals("Integer") == true || class1.equals("Long") == true || class1.equals("BigDecimal") == true || class1.equals("Double") == true || class1.equals("Float") == true) {
					for(int k=0; k<=number_array.length;k++){
						System.out.print("PASSING --> "+number_array[k]+"\t");
						try{
							queryCount++;
							pstmt.setObject(i1,number_array[k]);
							rs = pstmt.executeQuery();
							rsmd=rs.getMetaData();
							int col_count = rsmd.getColumnCount();
							while(rs.next()) {
								for(int cc=1;cc<=col_count;cc++){ 
									System.out.print("RETURN VALUE"+cc+" --> "+rs.getObject(cc) + "\t");
								}
								System.out.println();
							}
							for (int rscol = 1; rscol <= rsmd.getColumnCount(); rscol++) {
								System.out.print("\tRETURN PRECISION --> "+rsmd.getPrecision(rscol) + " ");
								System.out.print("\tRETURN NULLABILITY --> "+rsmd.isNullable(rscol) + " ");
								System.out.print("\tRETURN SIGNED --> "+rsmd.isSigned(rscol) + " ");
								System.out.print("\tRETURN COL SIZE --> "+rsmd.getColumnDisplaySize(rscol) + " ");
								System.out.print("\tRETURN SCALE --> "+rsmd.getScale(rscol) + " ");
								System.out.print("\tRETURN CLASS --> "+rsmd.getColumnClassName(rscol) + " ");  
								System.out.print("\tRETURN TYPE --> "+rsmd.getColumnType(rscol) + " ");
								System.out.println("\tRETURN TYPE NAME --> "+rsmd.getColumnTypeName(rscol));                 
							}
						} catch (Exception e) {
								errorqueryCount++;
								System.out.println("ERROR --> "+e.getMessage());
						}
					}
				} else if(class1.equals("Date") == true || class1.equals("Time") == true || class1.equals("Timestamp") == true ) {
					for(int k=0; k<=date_dt.length;k++){
						System.out.print("PASSING --> "+date_dt[k]+"\t");
						try{
							queryCount++;
							pstmt.setObject(i1,date_dt[k]);
							rs = pstmt.executeQuery();
							rsmd=rs.getMetaData();
							int col_count = rsmd.getColumnCount();
							while(rs.next()) {
								for(int cc=1;cc<=col_count;cc++){ 
									System.out.print("RETURN VALUE"+cc+" --> "+rs.getObject(cc) + "\t");
								}
								System.out.println();
							}
							for (int rscol = 1; rscol <= rsmd.getColumnCount(); rscol++) {
								System.out.print("\tRETURN PRECISION --> "+rsmd.getPrecision(rscol) + " ");
								System.out.print("\tRETURN NULLABILITY --> "+rsmd.isNullable(rscol) + " ");
								System.out.print("\tRETURN SIGNED --> "+rsmd.isSigned(rscol) + " ");
								System.out.print("\tRETURN COL SIZE --> "+rsmd.getColumnDisplaySize(rscol) + " ");
								System.out.print("\tRETURN SCALE --> "+rsmd.getScale(rscol) + " ");
								System.out.print("\tRETURN CLASS --> "+rsmd.getColumnClassName(rscol) + " ");  
								System.out.print("\tRETURN TYPE --> "+rsmd.getColumnType(rscol) + " ");
								System.out.println("\tRETURN TYPE NAME --> "+rsmd.getColumnTypeName(rscol));                
							}
						} catch (Exception e) {
								errorqueryCount++;
								System.out.println("ERROR --> "+e.getMessage());
						}
					}
				}
			
				
			} else if (size == 2) {
				ResultSet rs = null;
				ResultSetMetaData rsmd = null;
				String temp1 = lookup_arr.get(0) ;
				String temp2 = lookup_arr.get(1) ;
				String class1 = temp1.substring(temp1.indexOf(",")+1);
				String type1 = lookup_arr_type.get(0);
				int i1 = 1;
				String class2 = temp2.substring(temp2.indexOf(",")+1);
				String type2 = lookup_arr_type.get(1);
				int i2 = 2;
				Object[] temp_obj1 = null;
				Object[] temp_obj2 = null;
				
				if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj1 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj1 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class1.equals("Integer") == true || class1.equals("Long") == true || class1.equals("BigDecimal") == true || class1.equals("Double") == true || class1.equals("Float") == true) 
					temp_obj1 = Arrays.copyOf(number_array,number_array.length); 
				else if(class1.equals("Date") == true || class1.equals("Time") == true || class1.equals("Timestamp") == true ) 
					temp_obj1 = Arrays.copyOf(date_dt,date_dt.length); 
				
				if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj2 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj2 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class2.equals("Integer") == true || class2.equals("Long") == true || class2.equals("BigDecimal") == true || class2.equals("Double") == true || class2.equals("Float") == true) 
					temp_obj2 = Arrays.copyOf(number_array,number_array.length); 
				else if(class2.equals("Date") == true || class2.equals("Time") == true || class2.equals("Timestamp") == true ) 
					temp_obj2 = Arrays.copyOf(date_dt,date_dt.length); 
				
				for(int l=0; l< temp_obj1.length; l++) {
					for(int m=0; m < temp_obj2.length; m++) {
						System.out.print("PASSING --> "+temp_obj1[l]+", "+temp_obj2[m]+"\t");
						try {
							queryCount++;									
							pstmt.setObject(i1,temp_obj1[l]);
							pstmt.setObject(i2,temp_obj2[m]);
							rs = pstmt.executeQuery();
							rsmd=rs.getMetaData();
							int col_count = rsmd.getColumnCount();
							while(rs.next()) {
								for(int cc=1;cc<=col_count;cc++){ 
									System.out.print("RETURN VALUE"+cc+" --> "+rs.getObject(cc) + "\t");
								}
								System.out.println();
							}
							for (int rscol = 1; rscol <= rsmd.getColumnCount(); rscol++) {
								System.out.print("\tRETURN PRECISION --> "+rsmd.getPrecision(rscol) + " ");
								System.out.print("\tRETURN NULLABILITY --> "+rsmd.isNullable(rscol) + " ");
								System.out.print("\tRETURN SIGNED --> "+rsmd.isSigned(rscol) + " ");
								System.out.print("\tRETURN COL SIZE --> "+rsmd.getColumnDisplaySize(rscol) + " ");
								System.out.print("\tRETURN SCALE --> "+rsmd.getScale(rscol) + " ");
								System.out.print("\tRETURN CLASS --> "+rsmd.getColumnClassName(rscol) + " ");  
								System.out.print("\tRETURN TYPE --> "+rsmd.getColumnType(rscol) + " ");
								System.out.println("\tRETURN TYPE NAME --> "+rsmd.getColumnTypeName(rscol));                 
							}
						} catch (Exception e) {
								errorqueryCount++;
								System.out.println("ERROR --> "+e.getMessage());
						}
					
					
					
					}
				}
				
			} else if (size ==3) {
				ResultSet rs = null;
				ResultSetMetaData rsmd = null;
				String temp1 = lookup_arr.get(0) ;
				String temp2 = lookup_arr.get(1) ;
				String temp3 = lookup_arr.get(2) ;
				String type1 = lookup_arr_type.get(0);
				String type2 = lookup_arr_type.get(1);
				String type3 = lookup_arr_type.get(2);
				String class1 = temp1.substring(temp1.indexOf(",")+1);
				int i1 = 1;
				String class2 = temp2.substring(temp2.indexOf(",")+1);
				int i2 = 2;
				String class3 = temp3.substring(temp3.indexOf(",")+1);
				int i3 = 3;
				Object[] temp_obj1 = null;
				Object[] temp_obj2 = null;
				Object[] temp_obj3 = null;
				
				if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj1 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj1 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class1.equals("Integer") == true || class1.equals("Long") == true || class1.equals("BigDecimal") == true || class1.equals("Double") == true || class1.equals("Float") == true) 
					temp_obj1 = Arrays.copyOf(number_array,number_array.length);
				else if(class1.equals("Date") == true || class1.equals("Time") == true || class1.equals("Timestamp") == true ) 
					temp_obj1 = Arrays.copyOf(date_dt,date_dt.length); 						
				
				if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj2 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj2 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class2.equals("Integer") == true || class2.equals("Long") == true || class2.equals("BigDecimal") == true || class2.equals("Double") == true || class2.equals("Float") == true) 
					temp_obj2 = Arrays.copyOf(number_array,number_array.length); 
				else if(class2.equals("Date") == true || class2.equals("Time") == true || class2.equals("Timestamp") == true ) 
					temp_obj2 = Arrays.copyOf(date_dt,date_dt.length); 	
				
				if(class3.equals("String") == true && type3.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj3 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class3.equals("String") == true && type3.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj3 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class3.equals("Integer") == true || class3.equals("Long") == true || class3.equals("BigDecimal") == true || class3.equals("Double") == true || class3.equals("Float") == true) 
					temp_obj3 = Arrays.copyOf(number_array,number_array.length); 
				else if(class3.equals("Date") == true || class3.equals("Time") == true || class3.equals("Timestamp") == true ) 
					temp_obj3 = Arrays.copyOf(date_dt,date_dt.length); 	
				
				for(int l=0; l< temp_obj1.length; l++) {
					for(int m=0; m < temp_obj2.length; m++) {
						for(int n=0; n < temp_obj3.length; n++) {
							System.out.print("PASSING --> "+temp_obj1[l]+", "+temp_obj2[m]+", "+temp_obj3[n]+"\t");
							try {
								queryCount++;
								pstmt.setObject(i1,temp_obj1[l]);
								pstmt.setObject(i2,temp_obj2[m]);
								pstmt.setObject(i3,temp_obj3[n]);
								rs = pstmt.executeQuery();
								rsmd=rs.getMetaData();
								int col_count = rsmd.getColumnCount();
								while(rs.next()) {
									for(int cc=1;cc<=col_count;cc++){ 
										System.out.print("RETURN VALUE"+cc+" --> "+rs.getObject(cc) + "\t");
									}
									System.out.println();
								}
								for (int rscol = 1; rscol <= rsmd.getColumnCount(); rscol++) {
									System.out.print("\tRETURN PRECISION --> "+rsmd.getPrecision(rscol) + " ");
									System.out.print("\tRETURN NULLABILITY --> "+rsmd.isNullable(rscol) + " ");
									System.out.print("\tRETURN SIGNED --> "+rsmd.isSigned(rscol) + " ");
									System.out.print("\tRETURN COL SIZE --> "+rsmd.getColumnDisplaySize(rscol) + " ");
									System.out.print("\tRETURN SCALE --> "+rsmd.getScale(rscol) + " ");
									System.out.print("\tRETURN CLASS --> "+rsmd.getColumnClassName(rscol) + " ");  
									System.out.print("\tRETURN TYPE --> "+rsmd.getColumnType(rscol) + " ");
									System.out.println("\tRETURN TYPE NAME --> "+rsmd.getColumnTypeName(rscol));                
								}
							} catch (Exception e) {
									errorqueryCount++;
									System.out.println("ERROR --> "+e.getMessage());
							}
					
					
						}
					}
				}
			} else if (size ==4) {
				ResultSet rs = null;
				ResultSetMetaData rsmd = null;
				String temp1 = lookup_arr.get(0) ;
				String temp2 = lookup_arr.get(1) ;
				String temp3 = lookup_arr.get(2) ;
				String temp4 = lookup_arr.get(3) ;
				
				String type1 = lookup_arr_type.get(0);
				String type2 = lookup_arr_type.get(1);
				String type3 = lookup_arr_type.get(2);
				String type4 = lookup_arr_type.get(3);
				
				String class1 = temp1.substring(temp1.indexOf(",")+1);
				int i1 = 1;
				String class2 = temp2.substring(temp2.indexOf(",")+1);
				int i2 = 2;
				String class3 = temp3.substring(temp3.indexOf(",")+1);
				int i3 = 3;
				String class4 = temp4.substring(temp4.indexOf(",")+1);
				int i4 = 4;
				Object[] temp_obj1 = null;
				Object[] temp_obj2 = null;
				Object[] temp_obj3 = null;
				Object[] temp_obj4 = null;
				
				if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj1 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj1 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class1.equals("Integer") == true || class1.equals("Long") == true || class1.equals("BigDecimal") == true || class1.equals("Double") == true || class1.equals("Float") == true) 
					temp_obj1 = Arrays.copyOf(number_array,number_array.length);
				else if(class1.equals("Date") == true || class1.equals("Time") == true || class1.equals("Timestamp") == true ) 
					temp_obj1 = Arrays.copyOf(date_dt,date_dt.length); 						
				
				if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj2 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj2 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class2.equals("Integer") == true || class2.equals("Long") == true || class2.equals("BigDecimal") == true || class2.equals("Double") == true || class2.equals("Float") == true) 
					temp_obj2 = Arrays.copyOf(number_array,number_array.length); 
				else if(class2.equals("Date") == true || class2.equals("Time") == true || class2.equals("Timestamp") == true ) 
					temp_obj2 = Arrays.copyOf(date_dt,date_dt.length); 	
				
				if(class3.equals("String") == true && type3.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj3 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class3.equals("String") == true && type3.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj3 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class3.equals("Integer") == true || class3.equals("Long") == true || class3.equals("BigDecimal") == true || class3.equals("Double") == true || class3.equals("Float") == true) 
					temp_obj3 = Arrays.copyOf(number_array,number_array.length); 
				else if(class3.equals("Date") == true || class3.equals("Time") == true || class3.equals("Timestamp") == true ) 
					temp_obj3 = Arrays.copyOf(date_dt,date_dt.length); 	
				
				if(class4.equals("String") == true && type4.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj4 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class4.equals("String") == true && type4.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj4 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class4.equals("Integer") == true || class4.equals("Long") == true || class4.equals("BigDecimal") == true || class4.equals("Double") == true || class4.equals("Float") == true) 
					temp_obj4 = Arrays.copyOf(number_array,number_array.length); 
				else if(class4.equals("Date") == true || class4.equals("Time") == true || class4.equals("Timestamp") == true ) 
					temp_obj4 = Arrays.copyOf(date_dt,date_dt.length); 	
				
				for(int l=0; l< temp_obj1.length; l++) {
					for(int m=0; m < temp_obj2.length; m++) {
						for(int n=0; n < temp_obj3.length; n++) {
							for(int o=0; o < temp_obj4.length; o++) {
								System.out.print("PASSING --> "+temp_obj1[l]+", "+temp_obj2[m]+", "+temp_obj3[n]+", "+temp_obj4[o]+"\t");
								try {
									queryCount++;
									pstmt.setObject(i1,temp_obj1[l]);
									pstmt.setObject(i2,temp_obj2[m]);
									pstmt.setObject(i3,temp_obj3[n]);
									pstmt.setObject(i4,temp_obj4[o]);
									rs = pstmt.executeQuery();
									rsmd=rs.getMetaData();
									int col_count = rsmd.getColumnCount();
									while(rs.next()) {
										for(int cc=1;cc<=col_count;cc++){ 
											System.out.print("RETURN VALUE"+cc+" --> "+rs.getObject(cc) + "\t");
										}
										System.out.println();
									}
									for (int rscol = 1; rscol <= rsmd.getColumnCount(); rscol++) {
										System.out.print("\tRETURN PRECISION --> "+rsmd.getPrecision(rscol) + " ");
										System.out.print("\tRETURN NULLABILITY --> "+rsmd.isNullable(rscol) + " ");
										System.out.print("\tRETURN SIGNED --> "+rsmd.isSigned(rscol) + " ");
										System.out.print("\tRETURN COL SIZE --> "+rsmd.getColumnDisplaySize(rscol) + " ");
										System.out.print("\tRETURN SCALE --> "+rsmd.getScale(rscol) + " ");
										System.out.print("\tRETURN CLASS --> "+rsmd.getColumnClassName(rscol) + " ");  
										System.out.print("\tRETURN TYPE --> "+rsmd.getColumnType(rscol) + " ");
										System.out.println("\tRETURN TYPE NAME --> "+rsmd.getColumnTypeName(rscol));                
									}
								} catch (Exception e) {
										errorqueryCount++;
										System.out.println("ERROR --> "+e.getMessage());
								}
							}
					
						}
					}
				}
			}
					
		
		
		
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("Executed "+queryCount+" queries, "+errorqueryCount+" queries failed.");
		System.out.println("===============================================================================");
		
			
			
    }
	
	private static void executeUnnamedParamsInsert(String query) throws SQLException {
        
        ArrayList<String> lookup_arr = new ArrayList<String>();
		ArrayList<String> lookup_arr_type = new ArrayList<String>();
		
		int queryCount = 0;
		int errorqueryCount = 0;
				
		boolean isTypeInterval = false;
		try{
			lookup_arr.clear();
			lookup_arr_type.clear();
			PreparedStatement pstmt = c.prepareStatement(query);
			
			ParameterMetaData prmd = pstmt.getParameterMetaData();
			System.out.println("\tPARAMETER COUNT --> "+prmd.getParameterCount());
			for (int i = 1; i <= prmd.getParameterCount(); i++) { 
				
				String classname = prmd.getParameterClassName(i);
				String typename = prmd.getParameterTypeName(i);
		
				classname = classname.substring(classname.lastIndexOf(".")+1);
				System.out.println("\t\tCLASS --> "+classname+", TYPE --> "+typename);
				String temp = i + ","+classname;
				lookup_arr.add(temp);
				lookup_arr_type.add(typename);
				
			}
			
			System.out.println();
			int size = lookup_arr.size();
			
			System.out.println("Execute xx using values:");
			System.out.println("-------------------------");
			if(size==1) {
				ResultSet rs = null;
				ResultSetMetaData rsmd = null;
				String temp1 = lookup_arr.get(0) ;
				String class1 = temp1.substring(temp1.indexOf(",")+1);
				String type1 = lookup_arr_type.get(0);
				int i1 = 1;
				
				if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == false){
					for(int k=0; k<=string_dt.length;k++){
						System.out.print("PASSING --> "+string_dt[k]+"\t");
						try {
							queryCount++;
							pstmt.setString(i1,string_dt[k]);
							pstmt.executeUpdate();
							
						} catch (Exception e) {
								errorqueryCount++;
								System.out.println("ERROR --> "+e.getMessage());
						}
					}
				} else if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == true){
					for(int k=0; k<=interval_dt.length;k++){
						System.out.print("PASSING --> "+interval_dt[k]+"\t");
						try {
							queryCount++;
							pstmt.setString(i1,interval_dt[k]);
							pstmt.executeUpdate();
							
						} catch (Exception e) {
								System.out.println("ERROR --> "+e.getMessage());
								errorqueryCount++;
						}
					}
				} else if(class1.equals("Integer") == true || class1.equals("Long") == true || class1.equals("BigDecimal") == true || class1.equals("Double") == true || class1.equals("Float") == true) {
					for(int k=0; k<=number_array.length;k++){
						System.out.print("PASSING --> "+number_array[k]+"\t");
						try{
							queryCount++;
							pstmt.setObject(i1,number_array[k]);
							pstmt.executeUpdate();
							
						} catch (Exception e) {
								errorqueryCount++;
								System.out.println("ERROR --> "+e.getMessage());
						}
					}
				} else if(class1.equals("Date") == true || class1.equals("Time") == true || class1.equals("Timestamp") == true ) {
					for(int k=0; k<=date_dt.length;k++){
						System.out.print("PASSING --> "+date_dt[k]+"\t");
						try{
							queryCount++;
							pstmt.setObject(i1,date_dt[k]);
							pstmt.executeUpdate();
							
						} catch (Exception e) {
								errorqueryCount++;
								System.out.println("ERROR --> "+e.getMessage());
						}
					}
				}
			
				
			} else if (size == 2) {
				ResultSet rs = null;
				ResultSetMetaData rsmd = null;
				String temp1 = lookup_arr.get(0) ;
				String temp2 = lookup_arr.get(1) ;
				String class1 = temp1.substring(temp1.indexOf(",")+1);
				String type1 = lookup_arr_type.get(0);
				int i1 = 1;
				String class2 = temp2.substring(temp2.indexOf(",")+1);
				String type2 = lookup_arr_type.get(1);
				int i2 = 2;
				Object[] temp_obj1 = null;
				Object[] temp_obj2 = null;
				
				if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj1 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj1 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class1.equals("Integer") == true || class1.equals("Long") == true || class1.equals("BigDecimal") == true || class1.equals("Double") == true || class1.equals("Float") == true) 
					temp_obj1 = Arrays.copyOf(number_array,number_array.length); 
				else if(class1.equals("Date") == true || class1.equals("Time") == true || class1.equals("Timestamp") == true ) 
					temp_obj1 = Arrays.copyOf(date_dt,date_dt.length); 
				
				if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj2 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj2 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class2.equals("Integer") == true || class2.equals("Long") == true || class2.equals("BigDecimal") == true || class2.equals("Double") == true || class2.equals("Float") == true) 
					temp_obj2 = Arrays.copyOf(number_array,number_array.length); 
				else if(class2.equals("Date") == true || class2.equals("Time") == true || class2.equals("Timestamp") == true ) 
					temp_obj2 = Arrays.copyOf(date_dt,date_dt.length); 
				
				for(int l=0; l< temp_obj1.length; l++) {
					for(int m=0; m < temp_obj2.length; m++) {
						System.out.print("PASSING --> "+temp_obj1[l]+", "+temp_obj2[m]+"\t");
						try {
							queryCount++;									
							pstmt.setObject(i1,temp_obj1[l]);
							pstmt.setObject(i2,temp_obj2[m]);
							pstmt.executeUpdate();
							
						} catch (Exception e) {
								errorqueryCount++;
								System.out.println("ERROR --> "+e.getMessage());
						}
					
					
					
					}
				}
				
			} else if (size ==3) {
				ResultSet rs = null;
				ResultSetMetaData rsmd = null;
				String temp1 = lookup_arr.get(0) ;
				String temp2 = lookup_arr.get(1) ;
				String temp3 = lookup_arr.get(2) ;
				String type1 = lookup_arr_type.get(0);
				String type2 = lookup_arr_type.get(1);
				String type3 = lookup_arr_type.get(2);
				String class1 = temp1.substring(temp1.indexOf(",")+1);
				int i1 = 1;
				String class2 = temp2.substring(temp2.indexOf(",")+1);
				int i2 = 2;
				String class3 = temp3.substring(temp3.indexOf(",")+1);
				int i3 = 3;
				Object[] temp_obj1 = null;
				Object[] temp_obj2 = null;
				Object[] temp_obj3 = null;
				
				if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj1 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj1 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class1.equals("Integer") == true || class1.equals("Long") == true || class1.equals("BigDecimal") == true || class1.equals("Double") == true || class1.equals("Float") == true) 
					temp_obj1 = Arrays.copyOf(number_array,number_array.length);
				else if(class1.equals("Date") == true || class1.equals("Time") == true || class1.equals("Timestamp") == true ) 
					temp_obj1 = Arrays.copyOf(date_dt,date_dt.length); 						
				
				if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj2 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj2 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class2.equals("Integer") == true || class2.equals("Long") == true || class2.equals("BigDecimal") == true || class2.equals("Double") == true || class2.equals("Float") == true) 
					temp_obj2 = Arrays.copyOf(number_array,number_array.length); 
				else if(class2.equals("Date") == true || class2.equals("Time") == true || class2.equals("Timestamp") == true ) 
					temp_obj2 = Arrays.copyOf(date_dt,date_dt.length); 	
				
				if(class3.equals("String") == true && type3.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj3 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class3.equals("String") == true && type3.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj3 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class3.equals("Integer") == true || class3.equals("Long") == true || class3.equals("BigDecimal") == true || class3.equals("Double") == true || class3.equals("Float") == true) 
					temp_obj3 = Arrays.copyOf(number_array,number_array.length); 
				else if(class3.equals("Date") == true || class3.equals("Time") == true || class3.equals("Timestamp") == true ) 
					temp_obj3 = Arrays.copyOf(date_dt,date_dt.length); 	
				
				for(int l=0; l< temp_obj1.length; l++) {
					for(int m=0; m < temp_obj2.length; m++) {
						for(int n=0; n < temp_obj3.length; n++) {
							System.out.print("PASSING --> "+temp_obj1[l]+", "+temp_obj2[m]+", "+temp_obj2[n]+"\t");
							try {
								queryCount++;
								pstmt.setObject(i1,temp_obj1[l]);
								pstmt.setObject(i2,temp_obj2[m]);
								pstmt.setObject(i3,temp_obj3[n]);
								pstmt.executeUpdate();
								
							} catch (Exception e) {
									errorqueryCount++;
									System.out.println("ERROR --> "+e.getMessage());
							}
					
					
						}
					}
				}
			} else if (size ==4) {
				ResultSet rs = null;
				ResultSetMetaData rsmd = null;
				
				String temp1 = lookup_arr.get(0) ;
				String temp2 = lookup_arr.get(1) ;
				String temp3 = lookup_arr.get(2) ;
				String temp4 = lookup_arr.get(3) ;
				
				String type1 = lookup_arr_type.get(0);
				String type2 = lookup_arr_type.get(1);
				String type3 = lookup_arr_type.get(2);
				String type4 = lookup_arr_type.get(3);
				
				String class1 = temp1.substring(temp1.indexOf(",")+1);
				int i1 = 1;
				String class2 = temp2.substring(temp2.indexOf(",")+1);
				int i2 = 2;
				String class3 = temp3.substring(temp3.indexOf(",")+1);
				int i3 = 3;
				String class4 = temp4.substring(temp4.indexOf(",")+1);
				int i3 = 4;
				
				Object[] temp_obj1 = null;
				Object[] temp_obj2 = null;
				Object[] temp_obj3 = null;
				Object[] temp_obj4 = null;
				
				if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj1 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class1.equals("String") == true && type1.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj1 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class1.equals("Integer") == true || class1.equals("Long") == true || class1.equals("BigDecimal") == true || class1.equals("Double") == true || class1.equals("Float") == true) 
					temp_obj1 = Arrays.copyOf(number_array,number_array.length);
				else if(class1.equals("Date") == true || class1.equals("Time") == true || class1.equals("Timestamp") == true ) 
					temp_obj1 = Arrays.copyOf(date_dt,date_dt.length); 						
				
				if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj2 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class2.equals("String")==true && type2.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj2 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class2.equals("Integer") == true || class2.equals("Long") == true || class2.equals("BigDecimal") == true || class2.equals("Double") == true || class2.equals("Float") == true) 
					temp_obj2 = Arrays.copyOf(number_array,number_array.length); 
				else if(class2.equals("Date") == true || class2.equals("Time") == true || class2.equals("Timestamp") == true ) 
					temp_obj2 = Arrays.copyOf(date_dt,date_dt.length); 	
				
				if(class3.equals("String") == true && type3.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj3 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class3.equals("String") == true && type3.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj3 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class3.equals("Integer") == true || class3.equals("Long") == true || class3.equals("BigDecimal") == true || class3.equals("Double") == true || class3.equals("Float") == true) 
					temp_obj3 = Arrays.copyOf(number_array,number_array.length); 
				else if(class3.equals("Date") == true || class3.equals("Time") == true || class3.equals("Timestamp") == true ) 
					temp_obj3 = Arrays.copyOf(date_dt,date_dt.length);

				if(class4.equals("String") == true && type4.equalsIgnoreCase("INTERVAL") == false) 
					temp_obj4 = Arrays.copyOf(string_dt,string_dt.length); 
				else if(class4.equals("String") == true && type4.equalsIgnoreCase("INTERVAL") == true) 
					temp_obj4 = Arrays.copyOf(interval_dt,interval_dt.length); 
				else if(class4.equals("Integer") == true || class4.equals("Long") == true || class4.equals("BigDecimal") == true || class4.equals("Double") == true || class4.equals("Float") == true) 
					temp_obj4 = Arrays.copyOf(number_array,number_array.length); 
				else if(class4.equals("Date") == true || class4.equals("Time") == true || class4.equals("Timestamp") == true ) 
					temp_obj4 = Arrays.copyOf(date_dt,date_dt.length);				
				
				for(int l=0; l< temp_obj1.length; l++) {
					for(int m=0; m < temp_obj2.length; m++) {
						for(int n=0; n < temp_obj3.length; n++) {
							for(int o=0; o < temp_obj4.length; o++){
								System.out.print("PASSING --> "+temp_obj1[l]+", "+temp_obj2[m]+", "+temp_obj3[n]+", "+temp_obj4[o]+"\t");
								try {
									queryCount++;
									pstmt.setObject(i1,temp_obj1[l]);
									pstmt.setObject(i2,temp_obj2[m]);
									pstmt.setObject(i3,temp_obj3[n]);
									pstmt.setObject(i4,temp_obj4[o]);
									pstmt.executeUpdate();
									
								} catch (Exception e) {
										errorqueryCount++;
										System.out.println("ERROR --> "+e.getMessage());
								}
							}
					
						}
					}
				}
			}
					
		
		
		
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("Executed "+queryCount+" queries, "+errorqueryCount+" queries failed.");
		System.out.println("===============================================================================");
		
			
			
    }
}
