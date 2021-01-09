package nwhacks1.backEnd.Hack2021BackEnd;

import User.UserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@RestController
public class BackEndController {
    //List of Users
    ArrayList <UserInfo> list = new ArrayList<>();

    @GetMapping("/api/user/login/check")
    public void loginCheck(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password, HttpServletResponse response){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getUserName().equals(username)){
                if(list.get(i).getPassword().equals(password)){
                    //successful login
                    response.setStatus(201);
                }
            }
        }

    }

}
