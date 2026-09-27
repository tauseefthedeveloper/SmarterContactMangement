package com.scm.service;

import java.security.SecureRandom;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Service
public class EmailService {
		
	public boolean sendEmail(String message, String subject, String to, String from) {

		boolean f=false;
		// host server
		String host = "smtp.gmail.com";

		// get the system properties
		Properties properties = System.getProperties();
		System.out.println(properties);

		// setting important information in system properties
		properties.put("mail.smtp.host", host);
		properties.put("port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// to get session object
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("tauseefkhan3738@gmail.com", "oyzqyjokzzfklryi");
			}
		});

		session.setDebug(true);

		// compose the message
		MimeMessage mimeMessage = new MimeMessage(session);
		try {
			mimeMessage.setFrom(from);
			mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			mimeMessage.setSubject(subject);
			mimeMessage.setText(message, "UTF-8", "html");

			// send the message to user
			Transport.send(mimeMessage);
			f=true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return f;
	}

	public boolean sendOtpUserOnRegister(String email,HttpSession session) {
		boolean f=false;
		
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
		      + "    <p style='font-size:15px; color:#555;'>We received a request to register your account.</p>"
		      + "    <p style='font-size:15px; color:#555;'>Please use the OTP below to verify your identity:</p>"
		      + "    <div style='text-align:center; margin:25px 0;'>"
		      + "      <span style='font-size:28px; letter-spacing:4px; font-weight:bold; color:#ffffff; background:#007bff; padding:12px 24px; border-radius:6px;'>"
		      +            otp
		      + "      </span>"
		      + "    </div>"
		      + "    <p style='font-size:14px; color:#555;'>Do not share it with anyone.</p>"
		      + "    <p style='font-size:14px; color:#555;'>If you did not request a registration OTP, please ignore this email.</p>"
		      + "    <br>"
		      + "    <p style='font-size:14px; color:#333;'>Regards,<br>"
		      + "    <b>Smarter Contact Management Team</b></p>"
		      + "    <hr style='border:none; border-top:1px solid #eee;'>"
		      + "    <p style='font-size:12px; color:#999; text-align:center;'>© 2025 Smarter Contact Management. All rights reserved.</p>"
		      + "  </div>"
		      + "</div>";

		String subject = "Smarter Contact Management registration OTP for verification";
		String to = email;
		String from = "tauseefkhan3738@gmail.com";
	    
		f=this.sendEmail(message,subject,to,from);
		session.setAttribute("generatedOTP", otp);
		return f;
	}
}
