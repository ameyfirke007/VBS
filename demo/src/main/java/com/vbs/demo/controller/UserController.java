package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;
    @PostMapping("/register")
    public String register(@RequestBody User user)
    {

        userRepo.save(user);
        History h1 =new History();
        if(user.getRole().equalsIgnoreCase("admin")){
            h1.setDescription("ADMIN SELF CREATED "+user.getUsername());
        }
        else h1.setDescription("USER SELF CREATED "+user.getUsername());
        historyRepo.save(h1);
        return "SIGNUP SUCCESSFUL";
    }
    @PostMapping("/login")
    public String login(@RequestBody LoginDto u)
    {
        User user = userRepo.findByusername(u.getUsername());
        if(user == null)
        {
            return "User Not Found";
        }
        if(!u.getPassword().equals(user.getPassword()))
        {
            return "Password Not Match";
        }
        if(!u.getRole().equals(user.getRole()))
        {
            return "Role Not Match";
        }
        return String.valueOf(user.getId());
    }
    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user =userRepo.findById(id).orElseThrow(()->new RuntimeException("USER NOT FOUND"));

        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance((user.getBalance()));
        return displayDto;
    }
    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        History h1 =new History();
        User user = userRepo.findById((obj.getId())).orElseThrow(()->new RuntimeException("Not Found"));

        if(obj.getKey().equalsIgnoreCase("name")){
            if(user.getName().equalsIgnoreCase(obj.getValue())) return "Same not Allowed";
            h1.setDescription("USER CHANGED NAME FROM "+user.getName()+" TO "+obj.getValue());
            user.setName(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("password")){
            if(user.getPassword().equalsIgnoreCase(obj.getValue())) return "Same not Allowed";
            h1.setDescription("USER CHANGED PASSWORD "+ user.getUsername());
            user.setPassword(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("email")){
            User test = userRepo.findByEmail(obj.getValue());
            if(test != null ) return "Email Already";
            if(user.getEmail().equalsIgnoreCase(obj.getValue())) return "Same not Allowed";
            h1.setDescription("USER CHANGED EMAIL FROM "+user.getEmail()+" TO "+obj.getValue());
            user.setEmail(obj.getValue());
        }
        historyRepo.save(h1);
        userRepo.save(user);
        return "Update Successfully";
    }
    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable  int adminId)
    {
        History h1 =new History();
        h1.setDescription("USER "+user.getUsername()+" CREATED BY ADMIN "+adminId);
        historyRepo.save(h1);
        userRepo.save(user);
        return "Successfully added";
    }
    // SORT BY AND ORDER
    @GetMapping("/users")
    public List<User> getAllUsers (@RequestParam String  sortBy, @RequestParam String  order)

    {
        Sort sort;
        if(order.equalsIgnoreCase("desc"))
        {
            sort=Sort.by(sortBy).descending();
        }
        else
        {
            sort=Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("customer",sort);
    }
    //SEARCH
    @GetMapping("/users/{keyword}")
    public List<User> getUsers(@PathVariable String keyword) {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword, "customer");
    }
    //DELETE
    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String deleteUser(@PathVariable int userId,@PathVariable int adminId)
    {
        History h1 =new History();
        User user=userRepo.findById(userId).orElseThrow(()->new RuntimeException("Not Found"));
        if(user.getBalance()>0)
        {
            return "Balance should be zero";
        }
        h1.setDescription("USER "+ user.getId()+" DELETED BY ADMIN "+adminId);
        historyRepo.save(h1);
        userRepo.delete((user));
        return "User Deleted Successfully";
    }
}
