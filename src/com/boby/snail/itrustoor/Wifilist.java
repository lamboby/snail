package com.boby.snail.itrustoor;

public class Wifilist {
	private int sch_id = 0;
	private String stu_id = null;
	private String mac = null;
	private String sch_name=null;

	public void setschoolname(String sname)
	{
		this.sch_name=sname;
	}
	public String getschoolname()
	{
		return sch_name;
	}
	public void setschid(int schid) {
		this.sch_id = schid;
	}

	public int getschid() {
		return sch_id;
	}

	public void setcard(String card) {
		this.stu_id = card;
	}

	public String getcard() {
		return stu_id;
	}

	public String getschool() {
		return mac;
	}

	public void setschoollist(String schoollist) {
		this.mac = schoollist;
	}
}
