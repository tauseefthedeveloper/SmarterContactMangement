package com.scm.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.scm.dao.ContactRepository;
import com.scm.dao.UserRepository;
import com.scm.entities.Contact;
import com.scm.entities.User;

@RestController
public class SearchController {
	
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ContactRepository contactRepo;
	
	//search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal){
		
		String userEmail=principal.getName();
		User user=this.userRepo.getUserByUserName(userEmail);
		
		List<Contact> contacts=this.contactRepo.findByNameContainingAndUser(query,user);
		
		return ResponseEntity.ok(contacts);
	}
	
}
