package com.example.LibraryManager2;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@XmlRootElement
class SignUpObject
{
    private String  Name, Gender, Address, Occupation, Psw;
    private long AadhaarNo;
    public long getAadhaarNo() {
        return AadhaarNo;
    }

    public String getPsw() {
        return this.Psw;
    }

    public void setAadhaarNo(long aadhaarNo) {
        AadhaarNo = aadhaarNo;
    }

    public void setPsw(String psw) {
        this.Psw = psw;
    }

    public String getName() {
        return Name;
    }

    public String getAddress() {
        return Address;
    }

    public String getGender() {
        return this.Gender;
    }

    public String getOccupation() {
        return Occupation;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public void setGender(String gender) {
        this.Gender = gender;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setOccupation(String occupation) {
        Occupation = occupation;
    }
}
@XmlRootElement
class LogInObject
{
    protected long AadhaarNo1;
    protected String  Psw1;

    public String getPsw1() {
        return Psw1;
    }

    public void setPsw1(String psw1) {
        this.Psw1 = psw1;
    }

    public long getAadhaarNo1() {
        return AadhaarNo1;
    }

    public void setAadhaarNo1(long aadhaarNo1) {
        this.AadhaarNo1 = aadhaarNo1;
    }
}

@Provider
@Path("/")
public class SignIn {
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/signup")
    public String createUser(SignUpObject signUpObject) {
        String res="";
        int flag=0;
        long req=signUpObject.getAadhaarNo();
        try{

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/bookStore","srivishnu","newzoho");

            Statement stmt=con.createStatement();
            String val= String.format("('%d','%s','%s','%s','%s','%d','%s')",signUpObject.getAadhaarNo(),signUpObject.getName(),signUpObject.getAddress()
                    ,signUpObject.getOccupation(),signUpObject.getGender(),0,signUpObject.getPsw());
            flag=stmt.executeUpdate("insert into reader values "+val);
            con.close();

        }catch(Exception e){ res=e.toString();}
        return "SUCC"+signUpObject.getGender()+signUpObject.getName()+res+" flag "+flag;
    }
    @POST
    @Produces({MediaType.TEXT_PLAIN})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/login")
    public String loginUser(LogInObject logInObject) {
        String res="not";
        try{

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/bookStore","srivishnu","newzoho");

            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select r.Name from reader r where r.AadhaarNo="+logInObject.getAadhaarNo1()+" and r.psw='"+logInObject.getPsw1()+"';");

            if(rs.next())
            {
                res=rs.getString(1);
            }
            con.close();

        }catch(Exception e){ res=e.toString();}
        return res;
    }
}