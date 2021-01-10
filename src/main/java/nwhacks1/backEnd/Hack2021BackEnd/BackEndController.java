package nwhacks1.backEnd.Hack2021BackEnd;

import User.UserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.maps.GeoApiContext;
import org.json.simple.*;
import org.json.simple.parser.*;
import org.apache.catalina.User;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class BackEndController {
    //List of Users
    List<UserInfo> list = new ArrayList<UserInfo>();
    private static final String API_KEY = "YOUR_API_KEY";
    private static final GeoApiContext context = null;

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
                    System.out.println("Log in successful!");
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
    public void needRide(@RequestParam(value = "username") String username, @RequestParam(value = "address") String address,HttpServletResponse response){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getUserName().equals(username)){
                list.get(i).setNeedRide(true);
                list.get(i).setAddress(address);
                response.setStatus(203);
                return;
            }
        }
        response.setStatus(403);
    }

    @GetMapping("api/user/give")
    public void giveRide(@RequestParam(value = "username") String username, HttpServletResponse response){
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).getUserName().equals(username)) {
                list.get(i).setOfferingRides(true);
                response.setStatus(205);
                return;
            }
        }
        response.setStatus(405);
    }


    @GetMapping("api/user/list/bylocation")
    public List<UserInfo> listOfRidersByLocation(@RequestParam(value = "username") String username, @RequestParam(value = "address") String address, HttpServletResponse response){
        List <UserInfo> riders = new ArrayList<UserInfo>();
        int distance;
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).isNeedRide()) {
                UserInfo user = list.get(i);

                try {
                    URL url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + address + "&destination=" + user.getAddress() +"&key=Enter Key Here");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String output = br.lines().collect(Collectors.joining());
                    //System.out.println(output);
                    int index = output.indexOf("distance");
                    String subOutput = output.substring(index, index + 50);
                    int indexOfIndex = subOutput.indexOf("text");
                    String subOfSubString = subOutput.substring(indexOfIndex);
                    String subOfSubOfSubString = subOfSubString.substring(9, 12);
                    distance = Integer.parseInt(subOfSubString);
                    user.setDistance(distance);
                    System.out.println(distance);
                    connection.disconnect();
                    riders.add(user);

                } catch (Exception e) {
                    response.setStatus(406);
                    e.printStackTrace();
                }
            }
        }
        //order the list by smallest distance
        int smallest;
        for(int i = 0; i < riders.size(); i++){
            smallest = riders.get(i).getDistance();
            for(int j = 0; j < riders.size(); j++){
                if(riders.get(j).getDistance() < smallest){
                    smallest = riders.get(i).getDistance();
                    UserInfo tmp = riders.get(i);
                    riders.set(i, riders.get(j));
                    riders.set(j, tmp);
                }
            }

        }
        response.setStatus(206);
        return riders;
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
                info.setOfferingRides(false);
                info.setNeedRide(false);
                info.setDistance(0);
                System.out.println(info.getUserName());
                list.add(info);
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
