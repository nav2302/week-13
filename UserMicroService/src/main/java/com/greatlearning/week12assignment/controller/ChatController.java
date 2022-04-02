package com.greatlearning.week12assignment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greatlearning.week12assignment.service.ProducerService;

@RestController
@RequestMapping("/api/kafka")
public class ChatController {
	
	@Autowired
	ProducerService producerService;
	
	@PostMapping(value = "/sendToAdmin")
    public void sendMessageAdmin(@RequestParam String message) {
        producerService.sendMessageToAdmin(message);
    }
	
	
	@PostMapping(value = "/sendToUser")
    public void sendMessageToUser(@RequestParam String message) {
        producerService.sendMessageToUser(message);
    }
}
