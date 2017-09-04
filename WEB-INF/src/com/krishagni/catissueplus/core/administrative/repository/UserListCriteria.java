package com.krishagni.catissueplus.core.administrative.repository;

import com.krishagni.catissueplus.core.common.events.AbstractListCriteria;

import java.util.Date;

public class UserListCriteria extends AbstractListCriteria<UserListCriteria> {
	private String name;
	
	private String loginName;
	
	private String activityStatus;
	
	private String instituteName;
	
	private String domainName;
	
	private boolean listAll;

	private String type;

	private Date activeSince;
	
	@Override
	public UserListCriteria self() {
		return this;
	}

	public String name() {
		return name;
	}

	public UserListCriteria name(String name) {
		this.name = name;
		return self();
	}

	public String loginName() {
		return loginName;
	}

	public UserListCriteria loginName(String loginName) {
		this.loginName = loginName;
		return self();
	}

	public String activityStatus() {
		return activityStatus;
	}

	public UserListCriteria activityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
		return self();
	}
	
	public UserListCriteria instituteName(String instituteName) {
		this.instituteName = instituteName;
		return self();
	}
	
	public String instituteName() {
		return instituteName;
	}	
	
	public UserListCriteria domainName(String domainName) {
		this.domainName = domainName;
		return self();
	}
	
	public String domainName() {
		return domainName;
	}
	
	public boolean listAll() {
		return listAll;
	}
	
	public UserListCriteria listAll(boolean listAll) {
		this.listAll = listAll;
		return self();
	}

	public String type() {
		return type;
	}

	public UserListCriteria type(String type) {
		this.type = type;
		return self();
	}

	public Date activeSince() {
		return activeSince;
	}

	public UserListCriteria activeSince(Date activeSince) {
		this.activeSince = activeSince;
		return self();
	}
}
