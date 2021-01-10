package nwhacks1.backEnd.Hack2021BackEnd;

import User.UserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.json.simple.*;
import org.json.simple.parser.*;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

@RestController
public class BackEndController {
    //List of Users
    ArrayList<UserInfo> list = new ArrayList<UserInfo>();

    /**
     * GET request to confirm proper login.
     * @param username
     * @param password
     * @param response
     */
    @GetMapping("/api/user/login/check")
    public void loginCheck(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password, HttpServletResponse response){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getUserName().equals(username)){
                if(list.get(i).getPassword().equals(password)){
                    //successful login
                    response.setStatus(201);
                    return; //completed
                }
            }
        }
        //login failed
        response.setStatus(401);
    }

    /**
     * Adds new user
     * @param userInfo
     * @param response
     */
    @PostMapping("/api/user/login/create")
    public void createAccount(@RequestBody UserInfo userInfo, HttpServletResponse response){
        list.add(userInfo);
        Gson gson = new Gson();
        String json = gson.toJson(userInfo);

        try {
            Files.write(Paths.get("data.json"), json.getBytes(), StandardOpenOption.APPEND); //not sure if this will work
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        response.setStatus(202);
    }


    /**
     * Updates the users account to needing a ride
     * @param username
     * @param response
     */
    @GetMapping("api/user/need")
    public void needRide(@RequestParam(value = "username") String username, HttpServletResponse response){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getUserName().equals(username)){
                list.get(i).setNeedRide(true);
                response.setStatus(203);
                return;
            }
        }
        response.setStatus(403);
    }


    /**
     * startup method runs the code needed before the server starts
     * This method will just be fetching data from the json file
     */
    @PostConstruct
    public void startUp(){
        File file = new File("data.json");
        if(!file.exists()){
            try {
                file.createNewFile();
                System.out.println("file made!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
            System.out.println("File already here!");
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader("data.json"));
            String line = br.readLine();
            while(line !=null){
                UserInfo info = new Gson().fromJson(line, UserInfo.class);
                System.out.println(info.getUserName());
                list.add(info);
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
