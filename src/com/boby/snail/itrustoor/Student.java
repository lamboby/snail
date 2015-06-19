package com.boby.snail.itrustoor;


public class Student {
	private String sch_id = null;
	private String stu_id = null;
	private String school_name=null;
	private String mac = null;

	public void setschid(String schid) {
		this.sch_id = schid;
	}

	public String getschid() {
		return sch_id;
	}
	public void setschoolname(String school_name) {
		this.school_name = school_name;
	}

	public String getschoolname() {
		return school_name;
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
