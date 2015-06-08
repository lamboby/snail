package com.itrustoor.boby.snail;

public class Wifilist {
 
        private String sch_id = null;  
        private String stu_id = null;          
        private String  mac=null;
        public void setschid(String schid) {  
            this.sch_id= schid;  
        }    
        public String getschid() {  
            return sch_id;  
        }    
        public void setcard(String card) {  
            this.stu_id =card;  
        }    
        public String getcard(){
        	return stu_id;
        }
        public String  getschool() {  
            return mac;  
        }    
        public void setschoollist(String schoollist) {  
            this.mac= schoollist;
        }  
  
     
    
}
