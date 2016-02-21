import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Fanchao Zhou on 2/19/2016.
 */
public class Assignment4 extends HttpServlet {
    int userCnt;
    ArrayList<UserInfo> userList;

    @Override
    public void init(){
        userList = new ArrayList<UserInfo>(0);
        userCnt = 0;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response){

        try{
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            String name = request.getParameter("UserName");
            if(name != null){
                response(name);
            }
            for(int count = 0; count < userCnt; count++){
                out.println("<h1>" + (count+1)+". "+userList.get(count)+ "</h1>");
            }
            out.close();
        }catch(Exception e){
            System.err.println(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response){

        try{
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            String name = request.getParameter("UserName");
            if(name != null) {
                response(name);
            }
            for(int count = 0; count < userCnt; count++){
                out.println("<h1>"+(count+1)+ ". "+userList.get(count)+"</h1>");
            }
            out.close();
        }catch(Exception e){
            System.err.println(e);
        }
    }

    private void response(String name){
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String time = sdfDate.format(now);

        ++userCnt;
        userList.add(new UserInfo(name, time));
        System.out.println(userCnt + ". " + "name: " + name + "  " + "time:" + time);
    }
}

class UserInfo{
    private String name;
    private String time;


    UserInfo(String name, String time){
        this.name = name;
        this.time = time;
    }

    public String toString(){
        return (name+"  "+time);
    }
}