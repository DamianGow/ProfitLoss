package ProfitLoss;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class sql {
	double totalcost = 0;
	String url = "";
	List<finalData> finaldataList = new ArrayList<>();
    int maxsplit = 0;
    int overone = 0;
    double totLabor = 0;
    
    
    
    public void runPro(String jobnum, String company) throws SQLException {
    	//sendquery gives opcodes, employees, qty, labordtl
    	ListContainer listCont = sendQuery(jobnum, company);
    	System.out.println(listCont.opCodes);
    	//start for loop.
    	
    	List<data> opData = retrieveTimes(jobnum, company, listCont.opCodes, listCont.employees, listCont.qty, listCont.laborDtl);
    	for (int i = 0; i < opData.size(); i++) {
    		System.out.println(opData.size() + "size");
    		Overlap over = overlap(opData, company);
    		data temp = opData.get(i);
    		double opTime = getTotalTime(temp, over.in, over.out);
    		setOpValues(temp, opTime, company);
    	}
    }
    
    public ListContainer sendQuery(String jobnum, String company) throws SQLException {
ArrayList result = new ArrayList<>();
	Connection connection = null;
	Statement stmt = null;
	ResultSet rs = null;
	this.totalcost = 0;
	this.totLabor = 0;
	ArrayList opcodes = new ArrayList<>();
	ArrayList employees = new ArrayList<>();
	ArrayList times = new ArrayList<>();
	ArrayList qty = new ArrayList<>();
	ArrayList labordtl = new ArrayList<>();
	ArrayList opCost = new ArrayList<>();


	try {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		connection = DriverManager.getConnection(url);
		stmt = connection.createStatement();

		
		rs = stmt.executeQuery("Select labordtlseq, laborqty,  opcode, employeenum, burdenrate from dangerzone.erp.labordtl where jobnum = '" + jobnum + "' and company = '" + company + "' and laborcollection = 1 and opcode <> 'wc15' order by oprseq asc");
				
		while (rs.next()) {
			String data = rs.getString("employeenum");
			if (!data.equals("") && !data.equals("9999") && !data.equals(null) && !data.equals(" ")) {
				if (rs.getDouble("laborqty") > 0) {
				employees.add(data);
	
				data = rs.getString("opcode");
				opcodes.add(data);
							
				data = rs.getString("laborqty");
				qty.add(data);
				
				data = rs.getString("labordtlseq");
				labordtl.add(data);
				
			}
			}
		
			
		}
		ListContainer listCont = new ListContainer(qty, employees, labordtl, opcodes);
		return listCont;
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (ClassNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	return null;

    }
    
    public List<data> retrieveTimes(String jobnum, String company, ArrayList opcodes, ArrayList employees, ArrayList qty, ArrayList labordtl) throws SQLException {
	//break one pass employees, qty, and labordtl.
    	List<data> dataList = new ArrayList<>();
for (int i = 0; i < opcodes.size(); i++) { //iterate through all opcodes/labortransactions in this job and get the times/dates ran
	
	Connection connection = DriverManager.getConnection(url);
	Statement stmt = connection.createStatement();
	ResultSet rs = stmt.executeQuery("Select burdenrate, opcode, employeenum, laborqty, payrolldate, dspclockintime, dspclockouttime from dangerzone.erp.labordtl where jobnum = '" + jobnum + "' and company = '" + company + "' and employeenum = '" + employees.get(i)+ "' and laborcollection = 1 and laborqty = " + qty.get(i) + " and labordtlseq = " + labordtl.get(i) );
	System.out.println("Select burdenrate, opcode, employeenum, laborqty, payrolldate, dspclockintime, dspclockouttime from dangerzone.erp.labordtl where jobnum = '" + jobnum + "' and company = '" + company + "' and employeenum = '" + employees.get(i)+ "' and laborcollection = 1 and laborqty = " + qty.get(i) );
	while (rs.next()) {
		data curr = new data();
		curr.Payroll = rs.getString("payrolldate");
		 String test = rs.getString("dspclockintime");
		 test = test.replace(":", ".");
		 curr.Start = test;
				 
				 
		 test = rs.getString("dspclockouttime");
		 test = test.replace(":", ".");
		 curr.End = test;
 
		 curr.op = rs.getString("opcode");


		 curr.laborqty = rs.getString("laborqty");
		 

		 curr.empID = rs.getString("employeenum");
		 
		 curr.burden = rs.getString("burdenrate");

		 dataList.add(curr);
		 
		 
}


}
return dataList;
    }


//break two pass datalist, move overla[p out and in down here
public Overlap overlap(List<data> dataList, String company) throws SQLException {
ArrayList overlapIn = new ArrayList<>();
ArrayList overlapOut = new ArrayList<>();
for (int i = 0; i < dataList.size(); i++) {
	data temp = (data) dataList.get(i);
	double compStart = Double.parseDouble(temp.Start);
	double compEnd = Double.parseDouble(temp.End);
	if (compStart < compEnd) { //dayshit

	//get all times from that employee to find overlap.
	Connection connection = DriverManager.getConnection(url);
	Statement stmt = connection.createStatement();
	ResultSet rs = stmt.executeQuery("Select jobnum, dspclockintime, dspclockouttime from dangerzone.erp.labordtl where company = '" + company + "' and employeenum = '" + temp.empID + "' and payrolldate = '"+temp.Payroll + "' and ((clockintime >= " + temp.Start + " and clockintime <= " + temp.End + ") or (clockouttime >= " + temp.Start + " and clockintime <= " + temp.Start + ") or (clockintime >= " + temp.Start + " and clockintime < " + temp.End + " and clockouttime >= " + temp.End + "))");
	while (rs.next()) {
		if(!rs.getString("jobnum").equals("")) {
			 String data = rs.getString("dspclockintime");
			 data = data.replace(":", ".");
			 overlapIn.add(data);
			 data = rs.getString("dspclockouttime");
			 data = data.replace(":", ".");
			 overlapOut.add(data);
		}
	}
	} else {//night shift coverage
		
		//get all times from that employee to find overlap.
		Connection connection = DriverManager.getConnection(url);
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("Select jobnum, dspclockintime, dspclockouttime from dangerzone.erp.labordtl where company = '" + company + "' and employeenum = '" + temp.empID + "' and payrolldate = '"+temp.Payroll + "'");
		while (rs.next()) {
			if(!rs.getString("jobnum").equals("")) {
				double excessnew = 0;
				 String data = rs.getString("dspclockintime");
				 data = data.replace(":", ".");
				 double compdata = Double.parseDouble(data);
				 String data2 = rs.getString("dspclockouttime");
				 data2 = data2.replace(":", ".");
				 double compdata2 = Double.parseDouble(data2);
				 if (compdata > compdata2) { //get the difference from midnight to the start of the job, add it to how far after midnight, then add the sum to the start time to get the new end time. like 27:20
					 double ammount = 24 - compdata;
					 ammount = ammount + compdata2;
					 ammount = compdata + ammount;
					 data2 = String.valueOf(ammount);
					 //make data2 string
					 if (ammount % 1 > 0.6) {
						 excessnew = (ammount % 1) - 0.6;
						 ammount += 1;
						 ammount += excessnew;
						 data2 = String.valueOf(ammount);
					 }
					 
				 }
				 
				 overlapIn.add(data);
				 overlapOut.add(data2);
			}
		}
		
		
	}
}
Overlap overlap = new Overlap(overlapIn, overlapOut);
return overlap;
}
	

public double getTotalTime(data temp, ArrayList overlapIn, ArrayList overlapOut) {
//break three	 pass temp(data not final) object, overlap out and in
	double totalSeconds = 0;
	String start = temp.Start;
	String end = temp.End;
	
	Double startTime = Double.parseDouble(start);
	Double endTime = Double.parseDouble(end);
	
	 if (startTime > endTime) { //get the difference from midnight to the start of the job, add it to how far after midnight, then add the sum to the start time to get the new end time. like 27:20
		 double ammount = 24 - startTime;
		 double excess = 0;
		 ammount = ammount + endTime;
		 endTime = startTime + ammount;
			if (endTime% 1 > 0.6) {
				excess = (endTime % 1) - 0.6;
				endTime += 1;
				endTime += excess;
			}
		 
	 }
	
	for (double q = startTime; q <= endTime;) {
		
		
		
		int index = 0;
		for (int x = 0; x < overlapIn.size(); x++) {
		String in = (String) overlapIn.get(x);
		double currIn = Double.parseDouble(in);
		String out = (String) overlapOut.get(x);
		double currOut = Double.parseDouble(out);
	
		if (q >= currIn && q <= currOut) {
			index += 1;
		}
		}
		
		if (index != 0) {
			totalSeconds += 60/index;
			System.out.println("adding " + 60/index + " seconds out of " + totalSeconds + "total. Time is currently " + q);
		}	else {
			totalSeconds = 60;
		}
		
		if (index > maxsplit) {
			maxsplit = index;
		}
		
		System.out.println(q);
		q = q += .01;
		BigDecimal bd = new BigDecimal(q).setScale(2, RoundingMode.HALF_UP);
		
		
		q = bd.doubleValue();
		if (Math.abs((q% 1) - 0.60) < 0.01) {
			q += 0.4;
		}
	}
	return totalSeconds;
}
	
	//break 5???
public void setOpValues(data temp, double totalSeconds, String company) throws SQLException {
	//pass total seconds, and data temp.
	finalData tempfinal = new finalData();
	tempfinal.op = temp.op;
	if (overone == 2) {
		maxsplit = 1;
	}
	overone = 0;
	tempfinal.split = maxsplit;
	maxsplit = 0;

	BigDecimal bd = new BigDecimal(totalSeconds/60).setScale(2, RoundingMode.HALF_UP);
	tempfinal.opTime = bd.doubleValue();
	
	String burdenRate = temp.burden;
	Double burdenCost = Double.parseDouble(burdenRate);
	double tempx = (tempfinal.opTime/60) * burdenCost;
	bd = new BigDecimal(tempx).setScale(2, RoundingMode.HALF_UP);
	tempfinal.opcost = bd.doubleValue();

	totalcost += tempfinal.opcost;
	
	
	
	String round = temp.laborqty;
	Double rounddouble = Double.parseDouble(round);
	bd = new BigDecimal(rounddouble).setScale(2, RoundingMode.HALF_UP);
	tempfinal.qty = bd.doubleValue();
	
	
	double tempX = tempfinal.opTime/tempfinal.qty;
	bd = new BigDecimal(tempX).setScale(2, RoundingMode.HALF_UP);
	tempfinal.tpp = bd.doubleValue();
	

	finaldataList.add(tempfinal);

	
	//break six
	
	double empcost = 1;
	Connection connection = DriverManager.getConnection(url);
	Statement stmt = connection.createStatement();
	ResultSet rs = stmt.executeQuery("Select * from dangerzone.erp.empbasic where empid = '" + temp.empID + "' and company = '" + company + "'");
	while (rs.next()) {
		  empcost = rs.getDouble("laborrate");
	}
	
	
	
	totLabor += (tempfinal.opTime/60)*empcost;
	
	
}

    
    public double getTotal() {
    	BigDecimal bd = new BigDecimal(totalcost).setScale(2, RoundingMode.HALF_UP);	
    	totalcost = bd.doubleValue();
    return totalcost;
}
    
    public double getLabor() {
    	BigDecimal bd = new BigDecimal(totLabor).setScale(2, RoundingMode.HALF_UP);	
    	totLabor = bd.doubleValue();
    return totLabor;
}
    
    public List<finalData> getFinal() {

    	return finaldataList;
    }
    
    public boolean getShipped(String jobnum, String company) throws ClassNotFoundException {
		boolean shipped = false;
try {

    		String ordernum = null;
    		String orderline = null;
    		Connection connection = null;
    		Statement stmt = null;
    		ResultSet rs = null;
    		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    		connection = DriverManager.getConnection(url);
    		stmt = connection.createStatement();
    		
    		rs = stmt.executeQuery("Select * from dangerzone.erp.jobprod where jobnum = '" + jobnum + "' and company = '" + company + "'");
    		while (rs.next()) {
    			 ordernum = rs.getString("ordernum");
    			 orderline = rs.getString("orderline");
    	
    		}
    		rs = stmt.executeQuery("Select * from dangerzone.erp.shipdtl where ordernum = '" + ordernum + "' and orderline = '" + orderline + "' and company = '" + company + "'");
    		while (rs.next()) {
   			  shipped = true;
   		}
    		
    		
} catch (SQLException e1) {
	// TODO Auto-generated catch block
	shipped = false;
}
return shipped;
    }
    
    public double getPrice(String jobnum, String company) throws ClassNotFoundException {
    	double jobPrice = 0;
    	double discount = 1;
    	try {
    		
    		String ordernum = null;
    		String orderline = null;
    		Connection connection = null;
    		Statement stmt = null;
    		ResultSet rs = null;
    		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    		connection = DriverManager.getConnection(url);
    		stmt = connection.createStatement();
    		
    		rs = stmt.executeQuery("Select * from dangerzone.erp.jobprod where jobnum = '" + jobnum + "' and company = '" + company + "'");
    		while (rs.next()) {
    			 ordernum = rs.getString("ordernum");
    			 orderline = rs.getString("orderline");
    	
    		}
    		
    		
    		
    		rs = stmt.executeQuery("Select * from dangerzone.erp.orderdtl where ordernum = '" + ordernum + "' and orderline = '" + orderline + "' and company = '" + company + "'");
    		while (rs.next()) {
   			 jobPrice  = rs.getDouble("extpricedtl");
   			 discount  = rs.getDouble("discountpercent");
   		}
    		} catch (SQLException e1) {
    		// TODO Auto-generated catch block
    		e1.printStackTrace();
    }
    	System.out.println("discount b4" + discount);
    	if (discount != 0.0) {
    		discount = 100-discount;
    		discount = discount/100;
    		System.out.println("discount " + discount);
    	} else {
    		discount = 1;
    		System.out.println("discount is 1");
    	}
    	jobPrice = jobPrice * discount;
    	BigDecimal bd = new BigDecimal(jobPrice).setScale(2, RoundingMode.HALF_UP);	
    	jobPrice = bd.doubleValue();
		return jobPrice;
    }
    
    public double getMatlCost(String jobnum, String company) throws ClassNotFoundException {
		double matlcost = 0;
		double purmtlcost = 0;
		double standmtlcost = 0;
		String partnum = "";
		double reqqty = 1;
		
		try {
    		

    		Connection connection = null;
    		Statement stmt = null;
    		ResultSet rs = null;
    		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    		connection = DriverManager.getConnection(url);
    		stmt = connection.createStatement();
    		
    		rs = stmt.executeQuery("Select * from dangerzone.erp.jobmtl where jobnum = '" + jobnum + "' and company = '" + company + "'");
    		System.out.println("Select * from dangerzone.erp.jobmtl where jobnum = '" + jobnum + "' and company = '" + company + "'");
    		while (rs.next()) {
    			 matlcost = rs.getDouble("materialmtlcost");
    			 purmtlcost = rs.getDouble("estunitcost");
    			 partnum = rs.getString("partnum");
    			 reqqty = rs.getDouble("requiredqty");
    		}
    		rs = stmt.executeQuery("Select * from dangerzone.erp.part where company = '" + company + "' and partnum = '"  + partnum + "'");
    		System.out.println("Select * from dangerzone.erp.part where company = '" + company + "' and partnum = '"  + partnum + "'");
    		while (rs.next()) {
    			standmtlcost = rs.getDouble("unitprice");
    			standmtlcost = standmtlcost * .3;
    		}
    		
    		if (standmtlcost == 0)  {
    			if (purmtlcost == 0) {
    				//do nothing.
    			} else {
    				matlcost = purmtlcost;
    			}
    		} else {
    			matlcost = standmtlcost;
    		}
    		
    		} catch (SQLException e1) {
    		// TODO Auto-generated catch block
    		e1.printStackTrace();
    }
		matlcost = matlcost * reqqty;
		BigDecimal bd = new BigDecimal(matlcost).setScale(2, RoundingMode.HALF_UP);	
		matlcost = bd.doubleValue();
		
		return matlcost;
    }
    
    public ArrayList getSubCost(String jobnum, String company) throws ClassNotFoundException {
  		ArrayList sub = new ArrayList<>();
      	try {
      		
      		

      		Connection connection = null;
      		Statement stmt = null;
      		ResultSet rs = null;
      		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      		connection = DriverManager.getConnection(url);
      		stmt = connection.createStatement();
      		
      		rs = stmt.executeQuery("Select * from dangerzone.erp.joboper where jobnum = '" + jobnum + "' and company = '" + company + "' and subcontract = 1");
      		while (rs.next()) {
      			sub.add(rs.getDouble("actlabcost"));
  
      		}
      		
      		

      		} catch (SQLException e1) {
      		// TODO Auto-generated catch block
      		e1.printStackTrace();
      }
  		return sub;
      }
    
    
}


