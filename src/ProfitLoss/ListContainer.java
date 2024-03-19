package ProfitLoss;

import java.util.ArrayList;

public class ListContainer {
public ArrayList laborDtl;
public ArrayList opCodes;
public ArrayList employees;
public ArrayList qty;


public ListContainer(ArrayList qty, ArrayList employees, ArrayList labor, ArrayList opcodes) {
	this.laborDtl = labor;
	this.opCodes = opcodes;
	this.employees = employees;
	this.qty = qty;
}
}
