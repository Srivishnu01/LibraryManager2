package com.example.LibraryManager2;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

@XmlRootElement
class StringABC {

    private List<String> Arr;

    public List<String> getArr() {
        return Arr;
    }

    public void setArr(List<String> arr) {
        this.Arr = arr;
    }
}
@XmlRootElement
class ReserveObject extends LogInObject {

    private List<Long> ArrayOfISBN;

    public List<Long> getArrayOfISBN() {
        return ArrayOfISBN;
    }

    public void setArrayOfISBN(List<Long> arrayOfISBN) {
        this.ArrayOfISBN = arrayOfISBN;
    }
}
@XmlRootElement
class SearchObject extends LogInObject{

    private String SearchAuthor,SearchTitle,SearchDomain,SortBy;

    public void setSortBy(String sortBy) {
        SortBy = sortBy;
    }

    public String getSortBy() {
        return SortBy;
    }

    public String getSearchAuthor() {
        return SearchAuthor;
    }

    public String getSearchDomain() {
        return SearchDomain;
    }

    public String getSearchTitle() {
        return SearchTitle;
    }

    public void setSearchAuthor(String searchAuthor) {
        this.SearchAuthor = searchAuthor;
    }

    public void setSearchDomain(String searchDomain) {
        this.SearchDomain = searchDomain;
    }

    public void setSearchTitle(String searchTitle) {
        this.SearchTitle = searchTitle;
    }

}

@Provider
@Path("/book")
public class Controller {

    private Statement stmt;
    private Connection con;
    private void initSQL(){
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/bookStore", "srivishnu", "newzoho");

            stmt = con.createStatement();
        }catch (Exception e)
        {

        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hold-count/{AadhaarNo}/")
    public String getHoldCount(
            @PathParam("AadhaarNo") long AadhaarNo,
            @QueryParam("psw") String psw
    ){
        String count="";
        try {

            initSQL();
            ResultSet rs = stmt.executeQuery(
                    "select r.hold_count from reader r where r.AadhaarNo=" + AadhaarNo + " and r.psw='" + psw + "';");

            if(rs.next())
            {
//                count+=rs.getInt(1);(working inappropriate for duplicate entries)
                rs=stmt.executeQuery("select count(ALL *) from transaction t where t.AadhaarNo=" + AadhaarNo + " and t.returnTime is null ;");
                if(rs.next())
                {
                    count+=rs.getLong(1);
                }
            }
            con.close();
        }
        catch (Exception e){

        }
            return count;
    }


    @PUT
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/filter")
    public String getFiltered(SearchObject searchObject)
    {
        String books="";
        try{

            initSQL();
            ResultSet rs=stmt.executeQuery("select r.Name from reader r where r.AadhaarNo="+searchObject.getAadhaarNo1()+" and r.psw='"+searchObject.getPsw1()+"';");

            if(rs.next()) {
                String query="select b.ISBN,b.Author,b.Title,b.Domain,b.availCount from book b where" +
                        " b.Author like '%" + searchObject.getSearchAuthor() + "%' and " +
                        " b.Title like '%" + searchObject.getSearchTitle() + "%' and " +
                        " b.Domain like '%" + searchObject.getSearchDomain() + "%' and " +
                        " b.availCount>0" +
                        " order by b."+searchObject.getSortBy()+" asc;";

                rs = stmt.executeQuery(query);
                while (rs.next()) {

                    long ISBN = rs.getLong(1);
                    books += "<tr><td><input type=\"checkbox\" value=" + ISBN + "></td><td>"+ISBN+"</td>";
                    for (int i = 2; i <= 4; i++) {
                        books += "<td>" + rs.getString(i) + "</td>";
                    }
                    books += "<td>"+rs.getInt(5)+"</td></tr>";
                }
            }
            else
            {
                return "Login failed, Sorry you are not logged in. please login to view the content";
            }
            con.close();

        }catch(Exception e){

        }
        return books;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dashboard/{AadhaarNo}")
    public StringABC getDashboard( @PathParam("AadhaarNo") long AadhaarNo,
                                @QueryParam("psw") String psw)
    {

        String a="",b="",c="";

        try {

            initSQL();
            ResultSet rs = stmt.executeQuery(
                    "select r.hold_count from reader r where r.AadhaarNo=" + AadhaarNo + " and r.psw='" + psw + "';");

            if(rs.next())
            {
               rs=stmt.executeQuery(
                       "select b.ISBN,b.Author,b.Title,b.Domain,t.IssueTime,t.returnTime from transaction t" +
                               " inner join book b on t.ISBN=b.ISBN where " +
                               "t.AadhaarNo="+AadhaarNo+" order by t.reserveTime DESC ;");
               while(rs.next())
               {
                   long ISBN = rs.getLong(1);
                   if(rs.getDate(5)==null)
                   {

                       a += "<tr><td><input type=\"checkbox\" value=" + ISBN + "></td><td>"+ISBN+"</td>";
                       for (int i = 2; i <= 4; i++) {
                           a += "<td>" + rs.getString(i) + "</td>";
                       }
                   }
                   else if(rs.getDate(6)==null)
                   {
                       b += "<tr><td>--</td><td>"+ISBN+"</td>";
                       for (int i = 2; i <= 4; i++) {
                           b += "<td>" + rs.getString(i) + "</td>";
                       }
                   }
                   else
                   {
                       c += "<tr><td>##</td><td>"+ISBN+"</td>";
                       for (int i = 2; i <= 4; i++) {
                           c += "<td>" + rs.getString(i) + "</td>";
                       }
                   }

               }
            }
            con.close();
        }
        catch (Exception e){

        }
        StringABC stringABC=new StringABC();
        stringABC.setArr(Arrays.asList(a,b,c));
        return stringABC;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/check-in/{AadhaarNo}")
    public StringABC getCheckIn( @PathParam("AadhaarNo") long AadhaarNo,
                                   @QueryParam("psw") String psw)
    {

        String a="",b="",c="";

        try {

            initSQL();
            ResultSet rs = stmt.executeQuery(
                    "select r.hold_count from reader r where r.AadhaarNo=" + AadhaarNo + " and r.psw='" + psw + "';");

            if(rs.next())
            {
                rs=stmt.executeQuery(
                        "select b.ISBN,b.Author,b.Title,b.Domain,t.IssueTime,t.returnTime from transaction t" +
                                " inner join book b on t.ISBN=b.ISBN where " +
                                "t.AadhaarNo="+AadhaarNo+" order by t.reserveTime DESC ;");
                while(rs.next())
                {
                    if(rs.getDate(6)!=null)
                    {
                        continue;
                    }
                    long ISBN = rs.getLong(1);
                    String temp= "<tr><td><input type=\"checkbox\" value=" + ISBN + "></td><td>"+ISBN+"</td>";
                    for (int i = 2; i <= 4; i++) {
                        temp += "<td>" + rs.getString(i) + "</td>";
                    }
                    if(rs.getDate(5)==null)
                    {
                        a += temp;
                    }
                    else
                    {
                        b += temp;
                    }
                }
            }
            con.close();
        }
        catch (Exception e){

        }
        StringABC stringABC=new StringABC();
        stringABC.setArr(Arrays.asList(a,b,c));
        return stringABC;
    }


    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/reserve")
    public String reserveBooks(ReserveObject reserveObject)
    {
        List<Long> arr=reserveObject.getArrayOfISBN();
        Long aadhaarNo1=reserveObject.getAadhaarNo1();

        int len=arr.size();
        try {

            initSQL();
            ResultSet rs = stmt.executeQuery("select r.AadhaarNo from reader r where r.AadhaarNo=" + aadhaarNo1 + " and r.psw='" + reserveObject.getPsw1() + "';");

            if (rs.next()) {

                String trValues="";
                for (Long i:arr) {
                    trValues+=String.format(" (%d, %d),",aadhaarNo1, i);
                }
                trValues=trValues.substring(0,trValues.length()-1);

                stmt.executeUpdate("insert into transaction (AadhaarNo,ISBN) values "+trValues+";");
                stmt.executeUpdate("update reader r set r.hold_count=r.hold_count+"+len+" where r.AadhaarNo="+aadhaarNo1+";");
                trValues=arr.toString();
                stmt.executeUpdate("update book b set b.availCount=b.availCount-1 where b.ISBN in ("+trValues.substring(1,trValues.length()-1)+");");
            }
            else{
                return "login failed at reserve books";
            }
            con.close();
        }catch (Exception e){

        }
        return "Success";
    }


    @DELETE
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/reserve")
    public String unreserveBooks(ReserveObject reserveObject)
    {
        List<Long> arr=reserveObject.getArrayOfISBN();
        Long aadhaarNo1=reserveObject.getAadhaarNo1();

        int len=arr.size();
        try {

            initSQL();
            ResultSet rs = stmt.executeQuery("select r.AadhaarNo from reader r where r.AadhaarNo=" + aadhaarNo1 + " and r.psw='" + reserveObject.getPsw1() + "';");

            if (rs.next()) {

                String trValues=arr.toString();
                trValues=trValues.substring(1,trValues.length()-1);

                stmt.execute("delete from transaction where AadhaarNo="+aadhaarNo1+" and ISBN in ("+trValues+");");
                stmt.execute("update reader r set r.hold_count=r.hold_count-"+len+" where r.AadhaarNo="+aadhaarNo1+";");
                for (Long val:arr) {
                  stmt.execute("update book b set b.availCount=b.availCount+1 where b.ISBN ="+val +" limit 1;");
                }
            }
            else{
                return "login failed at un-reserve books";
            }
            con.close();
        }catch (Exception e){

        }
        return "Success";
    }



    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/return")
    public String returnBooks(ReserveObject reserveObject)
    {
        List<Long> arr=reserveObject.getArrayOfISBN();
        Long aadhaarNo1=reserveObject.getAadhaarNo1();

        int len=arr.size();
        try {


            initSQL();
            ResultSet rs = stmt.executeQuery("select r.AadhaarNo from reader r where r.AadhaarNo=" + aadhaarNo1 + " and r.psw='" + reserveObject.getPsw1() + "';");

            if (rs.next()) {

                String trValues=arr.toString();
                trValues=trValues.substring(1,trValues.length()-1);

                stmt.execute("update transaction t set t.returnTime=CURRENT_TIMESTAMP where t.AadhaarNo="+aadhaarNo1+" and t.returnTime is null and t.IssueTime is not null  and t.ISBN in ("+trValues+") ;");
                stmt.execute("update reader r set r.hold_count=r.hold_count-"+len+" where r.AadhaarNo="+aadhaarNo1+";");

            }
            else{
                return "login failed at return books";
            }
            con.close();
        }catch (Exception e){

        }
        return "Success";
    }



    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/take")
    public String takeBooks(ReserveObject reserveObject)
    {
        List<Long> arr=reserveObject.getArrayOfISBN();
        Long aadhaarNo1=reserveObject.getAadhaarNo1();

        int len=arr.size();
        try {

            initSQL();
            ResultSet rs = stmt.executeQuery("select r.AadhaarNo from reader r where r.AadhaarNo=" + aadhaarNo1 + " and r.psw='" + reserveObject.getPsw1() + "';");

            if (rs.next()) {

                String trValues=arr.toString();
                trValues=trValues.substring(1,trValues.length()-1);

                stmt.execute("update transaction t set t.IssueTime=CURRENT_TIMESTAMP where t.AadhaarNo="+aadhaarNo1+" and t.IssueTime is null  and t.ISBN in ("+trValues+") ;");

            }
            else{
                return "login failed at take books";
            }
            con.close();
        }catch (Exception e){

        }
        return "Success";
    }

}