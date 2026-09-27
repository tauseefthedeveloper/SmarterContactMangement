package com.scm.dao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserProfileDTO {

    @NotBlank(message="Name should contain 4-20 charactor's")
    @Size(min = 4, max = 20)
    private String name;

    @Email
    @NotBlank(message="Email can't be empty")
    private String email;

    @Size(max = 500)
    @NotBlank(message="Please write something about your self")
    private String about;
    
    public UserProfileDTO() {
    	
    }

    public UserProfileDTO(@NotBlank @Size(min = 4, max = 20) String name, @Email String email,
			@Size(max = 500) String about, int id) {
		super();
		this.name = name;
		this.email = email;
		this.about = about;
		this.id = id;
	}

	private int id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
    
    
    
}
