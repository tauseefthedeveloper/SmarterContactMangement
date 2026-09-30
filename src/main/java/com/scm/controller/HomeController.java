package com.scm.controller;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.scm.SmarterContactManagementApplication;
import com.scm.dao.ContactRepository;
import com.scm.dao.UserRepository;
import com.scm.entities.User;
import com.scm.helper.Message;
import com.scm.service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

    private final ContactRepository contactRepository;

	private final SmarterContactManagementApplication smarterContactManagementApplication;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ContactRepository contactRepo;
	
	@Autowired
	private EmailService emailService;

	HomeController(SmarterContactManagementApplication smarterContactManagementApplication, ContactRepository contactRepository) {
		this.smarterContactManagementApplication = smarterContactManagementApplication;
		this.contactRepository = contactRepository;
	}

	@GetMapping("/")
	public String homePage(Model m) {
		m.addAttribute("title", "Home - Smarter Contact Management");
		
		long totalContact=this.contactRepo.count();
		
		m.addAttribute("totalContact", totalContact);
		
		return "index";
	}

	@GetMapping("/about")
	public String aboutPage(Model m) {
		m.addAttribute("title", "About - Smarter Contact Management");
		return "about";
	}

	@GetMapping("/signup")
	public String signupPage(Model m, HttpSession session) {
		m.addAttribute("title", "Signup - Smarter Contact Management");
		m.addAttribute("user", new User());

		Message msg = (Message) session.getAttribute("message");
		if (msg != null) {
			m.addAttribute("message", msg);
			session.removeAttribute("message");
		}
		return "signup";
	}

	@GetMapping("/signin")
	public String loginPageCustom(Model m) {
		m.addAttribute("title", "Login - Smarter Contact Management");
		
		return "login";
	}

	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agree", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {
		model.addAttribute("title", "Home - Smarter Contact Management");
		User tempUser=this.userRepo.getUserByUserName(user.getEmail());
		if(tempUser!=null) {
			model.addAttribute("message",new Message("User already exits. Please login ", "danger"));
			return "login";
		}
		try {
			if (!agreement) {
				throw new Exception("Please accept the terms and condition's.");
			}

			System.out.println(user);

			if (result.hasFieldErrors()) {
				System.out.println("Has errors");
		        return "signup.html";
		    }

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
						
			if(this.emailService.sendOtpUserOnRegister(user.getEmail(),session)) {
				model.addAttribute("title", "Verify OTP - Smarter Contact Management");
				model.addAttribute("message",new Message("We have sent an OTP to your register email please verify the email!","success"));
				
				session.setAttribute("user", user);
				
				return "verify_otp_on_register";
			}else {
				session.setAttribute("message",new Message("Something went wrong please check your email is correct or not!","danger"));
				return "redirect:/signup";
			}
			
			
		} catch (Exception e) {
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "danger"));
		}

		return "redirect:/signup";
	}
	
	
	@PostMapping("/verifyOtpOnRegister")
	public String verifyOTPRegister(@RequestParam("otp") int otp,Model model,HttpSession session) {
		User user=(User) session.getAttribute("user");
		
		int generatedOTP=(int) session.getAttribute("generatedOTP");
		
		model.addAttribute("title", "Verify OTP - Smarter Contact Management");

		if(otp==generatedOTP) {
			model.addAttribute("message", new Message("Registration is successfull!", "success"));
			this.userRepo.save(user);
			return "login";
		}else {
			model.addAttribute("message", new Message("Please enter a correct OTP!", "danger"));
			return "verify_otp_on_register";
		}	
	}
}
