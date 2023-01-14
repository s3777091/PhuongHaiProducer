package com.example.phuonghaiproducer.Controller;

import com.example.phuonghaiproducer.Entity.ProductCatogary;
import com.example.phuonghaiproducer.Entity.UserEntity;
import com.example.phuonghaiproducer.Repository.UserRepository;
import com.example.phuonghaiproducer.Request.AddRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RestController
public class UserController {

    @Autowired
    KafkaTemplate<String, Object> controlTemplate;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/api/register/user")
    public CompletableFuture<ResponseEntity<?>> registerUser(@RequestBody AddRegister addRegister) {
        UserEntity user = userRepository.findByFullName(addRegister.getUserName());
        if(user == null) {

            UserEntity ss = new UserEntity();
            ss.setFullName(addRegister.getUserName());
            ss.setPassword(addRegister.getUserPassword());
            controlTemplate.send("UserRegister", ss);
            return CompletableFuture.completedFuture(new ResponseEntity<>("Success create account", HttpStatus.OK));
        } else {
            return CompletableFuture.completedFuture(new ResponseEntity<>("Account already existing", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/api/login/user")
    public CompletableFuture<ResponseEntity<?>> login(@RequestBody AddRegister addRegister) {
        UserEntity user = userRepository.findByFullName(addRegister.getUserName());
        if(Objects.equals(user.getPassword(), addRegister.getUserPassword())){
            return CompletableFuture.completedFuture(new ResponseEntity<>(user, HttpStatus.OK));
        }
        return CompletableFuture.completedFuture(new ResponseEntity<>("Wrong user input", HttpStatus.INTERNAL_SERVER_ERROR));
    }


}
