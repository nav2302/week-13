package com.greatlearning.week12assignment;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.greatlearning.week12assignment.exception.MailAlreadyExistsException;
import com.greatlearning.week12assignment.model.Item;
import com.greatlearning.week12assignment.model.Role;
import com.greatlearning.week12assignment.model.User;
import com.greatlearning.week12assignment.repository.OrderViewRepository;
import com.greatlearning.week12assignment.service.ItemService;
import com.greatlearning.week12assignment.service.UserService;

import lombok.extern.slf4j.Slf4j;

/*
 * PLEASE NOTE THE SAMPLE USER ID AND PASSWORD FOR ADMIN AND USER HAVE BEEN DEFINED BELOW YOU CAN USE THAT TO AUTHENTICATE IN ORDER 
 * TO USE SWAGGER APIS
 * 
 * localhost:8888/swagger-ui.html
 */

@SpringBootApplication
@Slf4j
@EnableKafka
public class UserMicroServiceApplication implements CommandLineRunner{

	@Autowired
	UserService userService;
	
	@Autowired
	ItemService itemService;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	OrderViewRepository orderViewRepository;

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		SpringApplication.run(UserMicroServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Creating one sample User and Admin User
		
		User adminUser = User.builder().email("admin@gmail.com").firstName("admin").lastName("last")
				.password(bCryptPasswordEncoder.encode("admin"))
				.roles(Arrays.asList(Role.builder().name("ROLE_ADMIN").build())).build();
		User sampleUser = User.builder().email("user@gmail.com").firstName("user").lastName("last")
				.password(bCryptPasswordEncoder.encode("user"))
				.roles(Arrays.asList(Role.builder().name("ROLE_USER").build())).build();
		
		Item item = Item.builder().name("Item" + Math.random()).price(100.0).build();
		itemService.save(item);
		
		try {
			userService.save(adminUser);
			userService.save(sampleUser);
		} catch (MailAlreadyExistsException e) {
			log.error("Mail Already exists in Database");
		}
	}

}
