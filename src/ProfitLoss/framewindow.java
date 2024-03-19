package ProfitLoss;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;



public class framewindow implements ActionListener{
	JTextArea entry;
	JButton button;
	JLabel label;
	JLabel totalLabel;
	JComboBox companyBox;
	JFrame frame;
	JScrollPane scrollPane;
	JScrollPane textPane;
	sql SQL = new sql();
framewindow() {
	frame = new JFrame("Profit Loss");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setSize(675, 800);
	frame.setLayout(new GridLayout(2, 1));
	
	JPanel panel = new JPanel();
	panel.setLayout(new GridLayout(1, 2));
	panel.setVisible(true);
	JPanel compPanel = new JPanel();
	compPanel.setLayout(null);
	compPanel.setVisible(true);
	
	
	companyBox = new JComboBox();
	companyBox.addItem("MPT");
	companyBox.addItem("MPTE");
	companyBox.addItem("MPTM");
	companyBox.addItem("MPTC");
	companyBox.addItem("MPTS");
	companyBox.setBounds(180, 70, 80, 40);
	companyBox.setVisible(true);
	
    entry = new JTextArea();
	entry.setVisible(true);
	entry.setBounds(180, 30, 400, 30);
	

	button = new JButton();
	button.setText("Submit");
	button.setBounds(180, 130, 80, 40);
	button.setVisible(true);
	button.addActionListener(this);
	
	label = new JLabel();
	label.setText("No Jobs selected");
	label.setVisible(true);
	
	totalLabel = new JLabel();
	totalLabel.setVisible(true);
	totalLabel.setBounds(180, 300, 270, 70);
	
	textPane = new JScrollPane(entry);
	textPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	textPane.getVerticalScrollBar().setUnitIncrement(15);
	textPane.setVisible(true);
	textPane.setBounds(0, 0, 50, 50);
	compPanel.add(textPane);

	
	compPanel.add(totalLabel);
	compPanel.add(companyBox);
	panel.add(compPanel);

	frame.add(panel);
	compPanel.add(button);

	frame.setVisible(true);
	
	scrollPane = new JScrollPane(label);
	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	scrollPane.getVerticalScrollBar().setUnitIncrement(15);
	frame.add(scrollPane);
	scrollPane.setVisible(true);
	
	
	
	
	
	   frame.addComponentListener(new ComponentAdapter() {
           public void componentResized(ComponentEvent e) {
               resizeLabelToWindowSize();
           }
       });
}

@Override
public void actionPerformed(ActionEvent e) {
	if (e.getSource()==button) {
		String company = (String) companyBox.getSelectedItem();
		String test = entry.getText();
		String[] lines = test.split("\n");
		ArrayList<String> filtered = new ArrayList<>();
		StringBuilder multhtml = new StringBuilder("<html><head><style>td{padding: 18px, 21px;}</style></head>");
		int wordcount = 0;
		
		for (String line: lines) {
			if(!line.trim().isEmpty()) {
				filtered.add(line);
			}
		}
		String[] finalLines = filtered.toArray(new String[0]);
		
		
		for (String line: finalLines) {
			wordcount += 1;
		}
		System.out.println("word count"  + wordcount);
		if (wordcount == 1) {
		test = test.replace("\n", "");
		
		double jobPrice  = 0;
		double matlcost = 0;
		double subprice = 0;
		try {
			//for loop for each job.
			
			String subString = "";
			SQL.runPro(test, company);
			try {
				 jobPrice 	=		SQL.getPrice(test, company);
				 matlcost 	=		SQL.getMatlCost(test, company);
				ArrayList sub = SQL.getSubCost(test, company);
				for(int i = 0; i < sub.size(); i++) {
					subString = "Subcontract: $"  + sub.get(i) + "<br>";
					double tempdoub = (double) sub.get(i);
					subprice = subprice + tempdoub;
				}
				System.out.println(subString);
				 System.out.println(jobPrice);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			double totalCost = SQL.getTotal();
			List<finalData> Final = SQL.getFinal();
			double totalLab = SQL.getLabor();
			boolean shipped = SQL.getShipped(test, company);


			StringBuilder hmtlContent = new StringBuilder("<html><head><style>td{padding: 18px, 21px;}</style></head>Labor Cost: $" + totalLab + "<br>Material Cost: $" + matlcost + "<br>" + subString + " <table>");

			//String labelResults = "<html>OP&nbsp&nbsp&nbsp &nbsp     - &nbsp&nbsp&nbsp   &nbsp  Time   &nbsp&nbsp&nbsp&nbsp   -   &nbsp&nbsp&nbsp&nbsp OpCost  &nbsp&nbsp&nbsp&nbsp   -   &nbsp&nbsp&nbsp&nbsp LaborQTY  &nbsp&nbsp&nbsp &nbsp    - &nbsp&nbsp&nbsp&nbsp  Time per Part<br>";
			for (int i =0; i < Final.size();i++) {
				finalData tempdata = Final.get(i);
				ArrayList result = new ArrayList<>();
				if(i == 0) {
					result.add("OP");
					result.add("Op Cost");
					result.add("Charge Time");
					result.add("C.T Per Part");
					result.add("QTY");
					result.add("Max Split");
				}
				result.add(tempdata.op);
				result.add("$" + tempdata.opcost);
				result.add(tempdata.opTime);
				result.add(tempdata.tpp);
				result.add(tempdata.qty);
				result.add(tempdata.split);

	
				for (int q = 0; q < result.size(); q++) {
				if (q%6 == 0) {
					if (q > 0) {
						hmtlContent.append("</tr>");
					}
					hmtlContent.append("<tr>");
					//labelResults += "<br>";
				}
				hmtlContent.append("<td>").append(result.get(q)).append("</td>");
			}
			}
			hmtlContent.append("</tr></table></html>");
			
			
	
			
			label.setText(hmtlContent.toString());
			totalCost = totalCost + matlcost;
			totalCost = totalCost + subprice;
			totalCost = totalCost + totalLab;
			BigDecimal ot = new BigDecimal(totalCost).setScale(2, RoundingMode.HALF_UP);
			totalCost = ot.doubleValue();
			
			String total = String.valueOf(totalCost);
			double totaltosub = Double.parseDouble(total);
			double profitLoss =  jobPrice - totaltosub;
	    	BigDecimal bd = new BigDecimal(profitLoss).setScale(2, RoundingMode.HALF_UP);	
	    	double finalprofitLoss = bd.doubleValue();
			
	    	double percent = 0;
	    	if (jobPrice != 0) {
	    	percent = (totaltosub/jobPrice) * 100;
	    	}
	    	if (percent != 0) {
	    	if (percent > 100) {
	    		//negative
	    		percent = percent-100;
	    		percent = percent * -1;
	    	} else {
	    		percent = 100-percent;
	    	}
	    	}
	    	BigDecimal pc = new BigDecimal(percent).setScale(2, RoundingMode.HALF_UP);	
	    	percent = pc.doubleValue();
			if(shipped == true) {
				if(finalprofitLoss > 0) {
			totalLabel.setText("<html>Total Cost: $" + total + "<br>Sale price: $" + jobPrice + "<br><br><b><span style='color:green;'> Profit/Loss: $" + finalprofitLoss + "<br>Percent: " + percent + "%</span></html>");
			} else {
				totalLabel.setText("<html>Total Cost: $" + total + "<br>Sale price: $" + jobPrice + "<br><br><b><span style='color:red;'> Profit/Loss: $" + finalprofitLoss + "<br>Percent: " + percent + "%</span></html>");
				
			}
				
		
				
			} else {
				if(finalprofitLoss > 0) {
				totalLabel.setText("<html>Total Cost: $" + total + "<br>Sale price: $" + jobPrice + "<br><br><b><span style='color:red;'> Profit/Loss: $" + finalprofitLoss + "<br></span><span style='color:blue;'>JOB NOT SHIPPED</span></html>");
			} else {
				totalLabel.setText("<html>Total Cost: $" + total + "<br>Sale price: $" + jobPrice + "<br><br><b><span style='color:red;'> Profit/Loss: $" + finalprofitLoss + "<br></span><span style='color:blue;'>JOB NOT SHIPPED</span></html>");
				
			}
			}
			hmtlContent.setLength(0);
			SQL.finaldataList.clear();
			System.out.println("labor" + totalLab);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		

	} else { //multiple jobs
		for (int q = 0; q < wordcount; q++) {
			double jobPrice  = 0;
			double matlcost = 0;
			double subprice = 0;
			try {
				//for loop for each job.
				
				test = lines[q];
				String subString = "";
				SQL.runPro(test, company);
				boolean shipped = SQL.getShipped(test, company);

				try {
					 jobPrice 	=		SQL.getPrice(test, company);
					 matlcost 	=		SQL.getMatlCost(test, company);
					ArrayList sub = SQL.getSubCost(test, company);
					for(int i = 0; i < sub.size(); i++) {
						subString = "Subcontract: $"  + sub.get(i) + "<br>";
						double tempdoub = (double) sub.get(i);
						subprice = subprice + tempdoub;
					}
					System.out.println(subString);
					 System.out.println(jobPrice);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				double totalCost = SQL.getTotal();
				List<finalData> Final = SQL.getFinal();
				double totalLab = SQL.getLabor();


				multhtml.append("<b><p1>Job Number: " + test + "</b><br>Labor Cost: $" + totalLab + "<br>Material Cost: $" + matlcost + "<br>" + subString + " <table>");

				//String labelResults = "<html>OP&nbsp&nbsp&nbsp &nbsp     - &nbsp&nbsp&nbsp   &nbsp  Time   &nbsp&nbsp&nbsp&nbsp   -   &nbsp&nbsp&nbsp&nbsp OpCost  &nbsp&nbsp&nbsp&nbsp   -   &nbsp&nbsp&nbsp&nbsp LaborQTY  &nbsp&nbsp&nbsp &nbsp    - &nbsp&nbsp&nbsp&nbsp  Time per Part<br>";
				for (int i =0; i < Final.size();i++) {
					finalData tempdata = Final.get(i);
					ArrayList result = new ArrayList<>();
					if(i == 0) {
						
						result.add("OP");
						result.add("Op Cost");
						result.add("Charge Time");
						result.add("C.T Per Part");
						result.add("QTY");
						result.add("Max Split");
					}
			    	System.out.println("iteration: " + i);
					result.add(tempdata.op);
					result.add("$" + tempdata.opcost);
					result.add(tempdata.opTime);
					result.add(tempdata.tpp);
					result.add(tempdata.qty);
					result.add(tempdata.split);

		
					for (int x = 0; x < result.size(); x++) {
					if (x%6 == 0) {
						if (x > 0) {
							multhtml.append("</tr>");
						}
						multhtml.append("<tr>");
						//labelResults += "<br>";
					}
					multhtml.append("<td>").append(result.get(x)).append("</td>");
				}
				}
				multhtml.append("</tr></table>");
		
				
		
				
				
				totalCost = totalCost + matlcost;
				totalCost = totalCost + subprice;
				totalCost = totalCost + totalLab;
				BigDecimal ot = new BigDecimal(totalCost).setScale(2, RoundingMode.HALF_UP);
				totalCost = ot.doubleValue();
				
				String total = String.valueOf(totalCost);
				double totaltosub = Double.parseDouble(total);
				double profitLoss =  jobPrice - totaltosub;
		    	BigDecimal bd = new BigDecimal(profitLoss).setScale(2, RoundingMode.HALF_UP);	
		    	double finalprofitLoss = bd.doubleValue();
		    	
		    	System.out.println(totaltosub);
		    	System.out.println(jobPrice);
		    	double percent = 0;
		    	if (jobPrice != 0) {
		    	percent = (totaltosub/jobPrice) * 100;
		    	}
		    	if (percent != 0) {
		    	if (percent > 100) {
		    		//negative
		    		percent = percent-100;
		    		percent = percent * -1;
		    	} else {
		    		percent = 100-percent;
		    	}
		    	}
		    	BigDecimal pc = new BigDecimal(percent).setScale(2, RoundingMode.HALF_UP);	
		    	percent = pc.doubleValue();
		    	
		    	if (shipped == true) {
				if (finalprofitLoss > 0) { 
		    	multhtml.append("<span style='color:green;'>Profit/Loss: $" + finalprofitLoss + "<br>Percent: " + percent + "%</span><br><br>");
				} else {
					multhtml.append("<span style='color:red;'>Profit/Loss: $" + finalprofitLoss + "<br>Percent: " + percent + "%</span><br><br>");
				}
		    	} else {
		    		if (finalprofitLoss > 0) { 
				    	multhtml.append("<span style='color:green;'>Profit/Loss: $" + finalprofitLoss + "<br>Percent: " + percent + "%</span><br><span style='color:blue;'>Job Not Shipped</span><br><br>");
						} else {
							multhtml.append("<span style='color:red;'>Profit/Loss: $" + finalprofitLoss + "<br>Percent: " + percent + "%</span><br><span style='color:blue;'>Job Not Shipped</span><br><br>");
						}
		    	}
				
				
				//totalLabel.setText("<html>Total Cost: $" + total + "<br>Sale price: $" + jobPrice + "<br><br><b> Profit/Loss: $" + finalprofitLoss + "</b></html>");
		    	totalLabel.setText("");
		    	System.out.println(multhtml);
				SQL.finaldataList.clear();
				System.out.println("labor" + totalLab);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ClassNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		
			
			
			
			
			
		}
		multhtml.append("</html>");
		label.setText(multhtml.toString());
		System.out.println(multhtml.toString());
		System.out.println("refactored");
	}
	
	
}
}


public void resizeLabelToWindowSize() {
	int width = frame.getContentPane().getWidth();
    int height = frame.getContentPane().getHeight();

    companyBox.setBounds(width/2-40, height/4-100, 80, 30);
    textPane.setBounds(width/2-300, height/3-185, 180, 300);
    button.setBounds(width/2-40, height/3-90, 80, 30);
    totalLabel.setBounds(width/2-40, height/3-50, 180, 100);
    
}
}
