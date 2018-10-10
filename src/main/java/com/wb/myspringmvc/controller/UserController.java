package com.wb.myspringmvc.controller;

import com.wb.myspringmvc.annotation.Controller;
import com.wb.myspringmvc.annotation.RequestMapping;

@Controller("userController")
public class UserController {

    @RequestMapping("/just-test")
    public void justTest(){
        System.out.println("UserController::justTest");
    }
}
