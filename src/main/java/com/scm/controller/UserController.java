package com.scm.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.scm.dao.ContactRepository;
import com.scm.dao.UserProfileDTO;
import com.scm.dao.UserRepository;
import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	private final HomeController homeController;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private ContactRepository contactRepo;
	
	@Autowired
	private jakarta.servlet.ServletContext servletContext;


	UserController(HomeController homeController) {
		this.homeController = homeController;
	}

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		// get the all details of user
		User user = userRepo.getUserByUserName(username);

		model.addAttribute("user", user);
	}

	@GetMapping("/dashboard")
	public String userDashboard(Model model, Principal principal) {

		model.addAttribute("title", "Home User Dashboard - Smarter Contact Management");

		model.addAttribute("activePage", "dashboard");
		String uEmail=principal.getName();
		User user=this.userRepo.getUserByUserName(uEmail);
		List<Contact> contact=user.getContacts();
		int totalContacts=contact.size();
		model.addAttribute("totalContacts",totalContacts);
		return "user/user_dashboard";
	}

	@GetMapping("/addContact")
	public String addContactForm(Model model) {
		model.addAttribute("title", "Add Contact User Dashboard - Smarter Contact Management");
		model.addAttribute("contact", new Contact());
		model.addAttribute("activePage", "addContact");

		return "user/add_contact";
	}
	
	//image saving 
	private String saveImageToStatic(MultipartFile image) throws Exception {

	    String uploadDir = servletContext.getRealPath("/static/images/");
	    File dir = new File(uploadDir);

	    if (!dir.exists()) {
	        dir.mkdirs();
	    }

	    String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
	    Path path = Paths.get(uploadDir + File.separator + fileName);

	    Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	    return fileName;
	}


	@PostMapping("/processContact")
	public String processAddContactForm(@Valid @ModelAttribute("contact") Contact contact, BindingResult result,
			Model model, @RequestParam("profileContact") MultipartFile imageUrl, Principal principal,
			RedirectAttributes redirectAttributes) {
		model.addAttribute("title", "Add Contact User Dashboard - Smarter Contact Management");


		try {

			String userEmail = principal.getName();
			User user = this.userRepo.getUserByUserName(userEmail);
			System.out.println("contact added");

			if (imageUrl.isEmpty()) {
			    contact.setImageUrl("default.png");
			} else {
			    String fileName = saveImageToStatic(imageUrl);
			    contact.setImageUrl(fileName);
			}



			if (result.hasFieldErrors()) {
				model.addAttribute("contact", contact);
				System.out.println("Has errors");
				return "user/add_contact.html";
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepo.save(user);
			
			redirectAttributes.addFlashAttribute("message", new Message("Contact saved!", "success"));

			model.addAttribute("contact", new Contact());

		} catch (Exception e) {
			System.out.println(e);
			redirectAttributes.addFlashAttribute("message", new Message("Something went wrong!", "danger"));

		}
		return "user/add_contact";
	}

	@GetMapping("/showContact/{page}")
	public String viewContactPage(@PathVariable("page") Integer page, Model model,
			Principal principal) {

		model.addAttribute("title", "View Contact's User Dashboard - Smarter Contact Management");
		model.addAttribute("activePage", "showContact");

		
		try {
			String userEmail = principal.getName();
			User user = this.userRepo.getUserByUserName(userEmail);
			Pageable pageAble = PageRequest.of(page, 5);
			Page<Contact> contacts = this.contactRepo.findContactsByUserId(user.getId(), pageAble);
			model.addAttribute("contacts", contacts);
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", contacts.getTotalPages());

		} catch (Exception e) {
			System.out.println(e);
		}

		return "user/view_contact";
	}

	@GetMapping("/{contactId}/contact")
	public String showingFullContactDetails(@PathVariable("contactId") int contactId, Model model,
			Principal principal) {
		model.addAttribute("title", "View Contact's User Dashboard - Smarter Contact Management");

		Optional<Contact> contactOptional = this.contactRepo.findById(contactId);
		Contact contact = contactOptional.get();

		String userEmail = principal.getName();
		User user = this.userRepo.getUserByUserName(userEmail);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}

		return "user/contact_details";
	}

	@GetMapping("/deleteContact/{cid}")
	public String deleteUserContacts(@PathVariable("cid") Integer cid, @RequestParam("page") Integer page,
			Principal principal, RedirectAttributes redirectAttributes, Model model) {


		long totalContacts = 0;
		model.addAttribute("title", "Delete Contact User Dashboard - Smarter Contact Management");

		try {
			String userEmail = principal.getName();
			User user = this.userRepo.getUserByUserName(userEmail);

			Contact contact = this.contactRepo.findById(cid)
					.orElseThrow(() -> new RuntimeException("Contact not found"));

			totalContacts = contactRepo.countContactsByUserId(user.getId());

			if (user.getId() == contact.getUser().getId()) {
				if (!contact.getImageUrl().equals("default.png")) {
				    String uploadDir = servletContext.getRealPath("/static/images/");
				    File deleteFile = new File(uploadDir + File.separator + contact.getImageUrl());
				    if (deleteFile.exists()) deleteFile.delete();
				}
				this.contactRepo.delete(contact);
				redirectAttributes.addFlashAttribute("message", new Message("Contact deleted successfully!", "success"));

			}

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("message", new Message("Failed to delete contact!", "danger"));
		}
		int totalPage = (int) Math.ceil((double) totalContacts / 5);
		if (page >= totalPage) {
			page = totalPage - 1;
		}
		if (page < 0) {
			page = 0;
		}

		return "redirect:/user/showContact/" + page;
	}

	@PostMapping("/editContact/{cid}")
	public String updateContactDetails(@PathVariable("cid") Integer cid, Model model,
			Principal principal) {
		model.addAttribute("title", "Update Contact User Dashboard - Smarter Contact Management");


		String userEmail = principal.getName();
		User user = this.userRepo.getUserByUserName(userEmail);

		Contact contact = this.contactRepo.findById(cid).orElseThrow(() -> new RuntimeException("Contact not found"));
		model.addAttribute("contact", contact);

		return "user/update_form";
	}

	@PostMapping("/processEdit")
	public String processEditForm(@Valid @ModelAttribute("contact") Contact contact, BindingResult result,
			RedirectAttributes redirectAttributes, @RequestParam("profileContact") MultipartFile imageUrl, Model model,
			Principal principal) {
		model.addAttribute("title", "Update Contact User Dashboard - Smarter Contact Management");

		try {

			Contact oldContact = this.contactRepo.findById(contact.getCid()).get();

			if (result.hasFieldErrors()) {
				model.addAttribute("contact", contact);
				System.out.println("Has errors");
				return "user/update_form.html";
			}
			if (!imageUrl.isEmpty()) {
				File deleteFile = new ClassPathResource("/static/images").getFile();
				File file1 = new File(deleteFile, oldContact.getImageUrl());
				file1.delete();

				File file = new ClassPathResource("/static/images").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + imageUrl.getOriginalFilename());

				Files.copy(imageUrl.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImageUrl(imageUrl.getOriginalFilename());
			} else {
				contact.setImageUrl(oldContact.getImageUrl());
			}

			String userEmail = principal.getName();
			User user = this.userRepo.getUserByUserName(userEmail);
			contact.setUser(user);
			this.contactRepo.save(contact);
			redirectAttributes.addFlashAttribute("message", new Message("Contact updated", "success"));

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("message", new Message("Something went wrong!", "danger"));
			System.out.println(e);
		}

		return "redirect:/user/" + contact.getCid() + "/contact";
	}

	// profile handler
	@GetMapping("/profile")
	public String profileHandler(Model model, Principal principal) {
		model.addAttribute("title", "Profile User Dashboard - Smarter Contact Management");
		model.addAttribute("activePage", "profile");

		String userEmail = principal.getName();
		User user = this.userRepo.getUserByUserName(userEmail);
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));
		model.addAttribute("user", user);
		model.addAttribute("dto",new UserProfileDTO());

		int totalContacts = (int) this.contactRepo.countContactsByUserId(user.getId());

		model.addAttribute("totalContact", totalContacts);

		return "user/user_profile";
	}

	// edit details saver handler
	@PostMapping("/updateProfile")
	public String updateProfile(@Valid @ModelAttribute("dto") UserProfileDTO dto, BindingResult result,Model model,
			@RequestParam("imageUrl") MultipartFile image, Principal principal, RedirectAttributes redirectAttributes) {

		model.addAttribute("title", "Profile User Dashboard - Smarter Contact Management");
		model.addAttribute("activePage", "profile");
		String userEmail = principal.getName();
		User user = this.userRepo.getUserByUserName(userEmail);
		int totalContacts = (int) this.contactRepo.countContactsByUserId(user.getId());

		
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("openProfileModal", true);
			return "user/user_profile";
		}

		user.setName(dto.getName());
		user.setEmail(dto.getEmail());
		user.setAbout(dto.getAbout());

		if (!image.isEmpty()) {
			// save image
			try {
				File file = new ClassPathResource("/static/images").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + image.getOriginalFilename());
				Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				user.setImageUrl(image.getOriginalFilename());
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			user.setImageUrl(user.getImageUrl());
		}

		userRepo.save(user);
		redirectAttributes.addFlashAttribute("message", new Message("Profile Updated!", "success"));
		return "redirect:/user/profile";
	}

	@GetMapping("/settings")
	public String openTheSetting(Model model) {
		model.addAttribute("title", "Setting's User Dashboard - Smarter Contact Management");
		model.addAttribute("activePage", "settings");
		return "user/settings";
	}

	@PostMapping("/changePassword")
	public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassowrd,
			@RequestParam String confirmNewPassword, Principal principal, RedirectAttributes redirectAttributes) {

		User user = userRepo.getUserByUserName(principal.getName());

		if (!newPassowrd.equals(confirmNewPassword)) {
			redirectAttributes.addFlashAttribute("message", new Message("Passwords do not match!", "danger"));
			return "redirect:/user/settings";
		}

		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			redirectAttributes.addFlashAttribute("message", new Message("Wrong old password!", "danger"));
			return "redirect:/user/settings";
		}

		String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

		if (!newPassowrd.matches(passwordRegex)) {
			redirectAttributes.addFlashAttribute("message", new Message(
					"Password must be at least 8 characters long and include uppercase, lowercase, number & special character!",
					"danger"));
			return "redirect:/user/settings";
		}

		user.setPassword(passwordEncoder.encode(newPassowrd));
		userRepo.save(user);

		redirectAttributes.addFlashAttribute("message", new Message("Password changed successfully!", "success"));

		return "redirect:/user/dashboard";
	}

}