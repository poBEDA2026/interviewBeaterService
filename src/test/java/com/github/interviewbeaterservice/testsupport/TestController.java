package com.github.interviewbeaterservice.testsupport;

import com.github.interviewbeaterservice.auth.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/__test__")
public class TestController {

    @GetMapping("/whoami")
    public String whoami() {
        return AuthContext.currentUserId().map(String::valueOf).orElse("anonymous");
    }
}
