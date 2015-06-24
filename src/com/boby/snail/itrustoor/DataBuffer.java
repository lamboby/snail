package com.boby.snail.itrustoor;

public class DataBuffer {
	private String card, atttime;
	private int schoolid;
	private int Isin;
	private int id;
	public void setid(int sid){
		this.id=sid;
	}
	public int getid(){
		return id;
	}
	public void setcard(String scard){
		this.card=scard;
	}
	public String getcard(){
		return card;
	}
	public void setatttime(String satttime){
		this.atttime=satttime;
	}
	public String getatttime(){
		return atttime;
	}
	public void setschoolid(int sschoolid ){
		this.schoolid=sschoolid;
	}
	public int getschoolid(){
		return schoolid;
	}
	public void setIsin(int sisin){
		this.Isin=sisin;
	}
	public int getIsin(){
		return Isin;
	}

}
