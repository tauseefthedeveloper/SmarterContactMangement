package com.scm.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="CONTACT")
public class Contact {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int Cid;
	
	@NotBlank(message="Name can't be empyt")
	@Size(min=2,max=20,message="Name should contain 2-20 character's")
	private String name;
	
	private String NickName;
	private String work;
	private String email;
	
	@Column(unique = true)
	@Pattern(regexp="^[0-9]{10}$", message="Phone number must be exactly 10 digits")
	private String phone;
	
	private String imageUrl;
	@Column(length=1500)
	@Size(min=15,max=1500,message="Message contain only 15-1500 character's")
	private String description;

	@ManyToOne
	@JsonIgnore
	private User user;

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public int getCid() {
		return Cid;
	}
	public void setCid(int cid) {
		Cid = cid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNickName() {
		return NickName;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public void setNickName(String nickName) {
		NickName = nickName;
	}
	public String getWork() {
		return work;
	}
	public void setWork(String work) {
		this.work = work;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "Contact [Cid=" + Cid + ", name=" + name + ", NickName=" + NickName + ", work=" + work + ", email="
				+ email + ", phone=" + phone + ", imageUrl=" + imageUrl + ", description=" + description + ", user="
				+ user + "]";
	}
	
	
		
	

}
