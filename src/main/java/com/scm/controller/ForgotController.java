package com.scm.controller;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.scm.dao.UserRepository;
import com.scm.entities.User;
import com.scm.helper.Message;
import com.scm.service.EmailService;

import jakarta.servlet.http.HttpSession;


@Controller
public class ForgotController {

	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/forgot")
	public String openforgotForm(Model model) {
		model.addAttribute("title","Forgot password - Smarter Contact Management!");
		return "forgot_email_pass";
	}
	
	@PostMapping("/sendOtp")
	public String sendOtpUser(@RequestParam("email") String email,Model model,
			RedirectAttributes redirectAttributes, HttpSession session) {
		model.addAttribute("title","Verify OTP - Smarter Contact Management!");
		
		User user=this.userRepo.getUserByUserName(email);
		if(user==null) {
			model.addAttribute("message",new Message("User not found. Please register "
					+ "<a style='color:inherit;text-decoration:underline;font-style:italic;' "
					+ "href='/signup'>here</a>.", "danger"));
			return "forgot_email_pass";
		}
		
		//otp generation
		SecureRandom random = new SecureRandom();
	    int otp = 100000 + random.nextInt(900000); // 6 digit
		
		//send otp
		String message =
		        "<div style='font-family: Arial, sans-serif; background-color:#f4f6f8; padding:20px;'>"
		      + "  <div style='max-width:600px; margin:auto; background:#ffffff; padding:25px; border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1);'>"
		      + "    <div style='text-align:center;'>"
		      + "      <img src='https://cdn-icons-png.flaticon.com/512/942/942748.png' width='80' alt='SCM Logo'/>"
		      + "      <h2 style='color:#2c3e50;'>Smarter Contact Management</h2>"
		      + "    </div>"
		      + "    <hr style='border:none; border-top:1px solid #eee;'>"
		      + "    <p style='font-size:15px; color:#333;'>Hello,</p>"
		      + "    <p style='font-size:15px; color:#555;'>We received a request to reset your account password.</p>"
		      + "    <p style='font-size:15px; color:#555;'>Please use the OTP below to verify your identity:</p>"
		      + "    <div style='text-align:center; margin:25px 0;'>"
		      + "      <span style='font-size:28px; letter-spacing:4px; font-weight:bold; color:#ffffff; background:#007bff; padding:12px 24px; border-radius:6px;'>"
		      +            otp
		      + "      </span>"
		      + "    </div>"
		      + "    <p style='font-size:14px; color:#555;'>Do not share it with anyone.</p>"
		      + "    <p style='font-size:14px; color:#555;'>If you did not request a password reset, please ignore this email.</p>"
		      + "    <br>"
		      + "    <p style='font-size:14px; color:#333;'>Regards,<br>"
		      + "    <b>Smarter Contact Management Team</b></p>"
		      + "    <hr style='border:none; border-top:1px solid #eee;'>"
		      + "    <p style='font-size:12px; color:#999; text-align:center;'>© 2025 Smarter Contact Management. All rights reserved.</p>"
		      + "  </div>"
		      + "</div>";

		String subject = "Smarter Contact Management password forgetting OTP for verification";
		String to = email;
		String from = "tauseefkhan3738@gmail.com";
	    boolean f=this.emailService.sendEmail(message,subject,to,from);
		
	    if(f) {
	    	model.addAttribute("message",new Message("We have sent an OTP to your email...", "success"));
		    session.setAttribute("otp",otp);
		    session.setAttribute("email",email);
	    	
	    	return "verify_otp";
	    }else {
	    	
	    	model.addAttribute("message",new Message("Something went wrong! Please try after some times", "danger"));

	    	return "forgot_email_pass";
	    }
	}
	
	@PostMapping("/verifyOtp")
	public String verifyEmailOTP(@RequestParam("otp") int otp,Model model,RedirectAttributes redirectAttributes
			,HttpSession session) {
		
		int generatedOtp=(int) session.getAttribute("otp");
		String email =(String) session.getAttribute("email");
		
		
		if(otp==generatedOtp) {
			
			User user=this.userRepo.getUserByUserName(email);
			
			if(user==null) {
				
				model.addAttribute("message",new Message("User does not exits!", "danger"));
				return "forgot_email_pass";
			}else {
				model.addAttribute("title","Change password - Smarter Contact Management!");
		    	model.addAttribute("message",new Message("OTP verified successully!", "success"));

				return "change_pass_form";
			}
			
		}else {
			
	    	model.addAttribute("message",new Message("Please enter correct OTP", "danger"));

			return "verify_otp";
		}
	}
	
	
	//change the password after forgetting
	@PostMapping("/changePassword")
	public String changingPassword(@RequestParam("newPassword") String newPassowrd,HttpSession session,
			Model model,RedirectAttributes redirectAttributes
			) {
		model.addAttribute("title","Change password - Smarter Contact Management!");

		String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

		if (!newPassowrd.matches(passwordRegex)) {
			model.addAttribute("message", new Message(
					"Password must be at least 8 characters long and include uppercase, lowercase, number & special character!",
					"danger"));
			return "change_pass_form";
		}
		
		String email=(String) session.getAttribute("email");
		User user=this.userRepo.getUserByUserName(email);
		user.setPassword(this.passwordEncoder.encode(newPassowrd));
		this.userRepo.save(user);
		
    	model.addAttribute("message",new Message("Password changed successfully!", "success"));

		
		return "redirect:/signin?changed=true";
	}
	
}
