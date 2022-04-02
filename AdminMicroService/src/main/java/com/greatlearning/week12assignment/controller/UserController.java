package com.greatlearning.week12assignment.controller;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greatlearning.week12assignment.config.SwaggerConfig;
import com.greatlearning.week12assignment.model.User;
import com.greatlearning.week12assignment.model.UserDto;
import com.greatlearning.week12assignment.response.UserResponse;
import com.greatlearning.week12assignment.service.UserService;

import io.swagger.annotations.Api;

@Api(tags = { SwaggerConfig.UserController_TAG })
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')") // Authorizing only Admin
public class UserController {

	@Autowired
	UserService userService;

	@GetMapping
	public ResponseEntity<Iterable<UserResponse>> getAllUsers() {
		List<UserResponse> usersResponses = new ArrayList<>();
		List<User> users = userService.getUsers();
		if(users != null) {
			users.stream().forEach(user -> {
				usersResponses.add(UserResponse.builder().firstName(user.getFirstName())
						.lastName(user.getLastName()).email(user.getEmail()).build());
			});
		}
		return new ResponseEntity<>(usersResponses, HttpStatus.OK);
	}

	@GetMapping({ "/{email}" })
	public ResponseEntity<UserResponse> getUser(@PathVariable String email) {

		User user = userService.findByEmail(email);
		return new ResponseEntity<>(UserResponse.builder().firstName(user.getFirstName()).lastName(user.getLastName())
				.email(user.getEmail()).build(), HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<UserResponse> saveUser(@Valid @RequestBody UserDto user) {
		User savedUser = userService.save(User.builder().firstName(user.getFirstName()).lastName(user.getLastName())
				.email(user.getEmail()).password(user.getPassword()).build());
		return new ResponseEntity<>(UserResponse.builder().firstName(savedUser.getFirstName())
				.lastName(savedUser.getLastName()).email(savedUser.getEmail()).build(), HttpStatus.CREATED);
	}

	@PutMapping({ "/{email}" })
	public ResponseEntity<UserResponse> updateUser(@PathVariable("email") String email,
			@Valid @RequestBody UserDto user) {

		User updatedUser = userService.updateUser(email, User.builder().firstName(user.getFirstName())
				.lastName(user.getLastName()).email(user.getEmail()).password(user.getPassword()).build());
		return new ResponseEntity<>(UserResponse.builder().firstName(updatedUser.getFirstName())
				.lastName(updatedUser.getLastName()).email(updatedUser.getEmail()).build(), HttpStatus.OK);
	}

	@DeleteMapping({ "/{email}" })
	@Transactional
	public ResponseEntity<UserResponse> deleteUser(@PathVariable("email") String email) {
		userService.deleteUser(email);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
