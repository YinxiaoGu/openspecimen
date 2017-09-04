
package com.krishagni.catissueplus.core.administrative.repository.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.krishagni.catissueplus.core.administrative.domain.ForgotPasswordToken;
import com.krishagni.catissueplus.core.administrative.domain.Password;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.administrative.repository.UserDao;
import com.krishagni.catissueplus.core.administrative.repository.UserListCriteria;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;
import com.krishagni.catissueplus.core.common.util.Utility;

public class UserDaoImpl extends AbstractDao<User> implements UserDao {
	
	@Override
	public Class<?> getType() {
		return User.class;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getUsers(UserListCriteria listCrit) {
		return getUsersListQuery(listCrit)
			.setFirstResult(listCrit.startAt())
			.setMaxResults(listCrit.maxResults())
			.addOrder(Order.asc("u.lastName"))
			.addOrder(Order.asc("u.firstName"))
			.list();
	}
	
	public Long getUsersCount(UserListCriteria listCrit) {
		Number count = (Number) getUsersListQuery(listCrit)
			.setProjection(Projections.rowCount())
			.uniqueResult();
		return count.longValue();
	}

	public List<User> getUsersByIds(Collection<Long> userIds) {
		return getUsersByIdsAndInstitute(userIds, null);
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getUsersByIdsAndInstitute(Collection<Long> userIds, Long instituteId) {
		Criteria criteria = sessionFactory.getCurrentSession()
			.createCriteria(User.class, "u")
			.add(Restrictions.in("u.id", userIds));
		
		if (instituteId != null) {
			criteria.createAlias("u.institute", "inst")
				.add(Restrictions.eq("inst.id", instituteId));
		}
		
		return criteria.list();
	}
	
	public User getUser(String loginName, String domainName) {
		String hql = String.format(GET_USER_BY_LOGIN_NAME_HQL, " and activityStatus != 'Disabled'");
		List<User> users = executeGetUserByLoginNameHql(hql, loginName, domainName);
		return users.isEmpty() ? null : users.get(0);
	}
	
	@Override
	public User getSystemUser() {
		return getUser(User.SYS_USER, User.DEFAULT_AUTH_DOMAIN);
	}
	
	public User getUserByEmailAddress(String emailAddress) {
		String hql = String.format(GET_USER_BY_EMAIL_HQL, " and activityStatus != 'Disabled'");
		List<User> users = executeGetUserByEmailAddressHql(hql, emailAddress);
		return users.isEmpty() ? null : users.get(0);
	}
	
	public Boolean isUniqueLoginName(String loginName, String domainName) {
		String hql = String.format(GET_USER_BY_LOGIN_NAME_HQL, "");
		List<User> users = executeGetUserByLoginNameHql(hql, loginName, domainName);
		return users.isEmpty();
	}
	
	public Boolean isUniqueEmailAddress(String emailAddress) {
		String hql = String.format(GET_USER_BY_EMAIL_HQL, "");
		List<User> users = executeGetUserByEmailAddressHql(hql, emailAddress);
		
		return users.isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	public List<DependentEntityDetail> getDependentEntities(Long userId) {
		List<Object[]> rows = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_DEPENDENT_ENTITIES)
				.setLong("userId", userId)
				.list();
		
		return getDependentEntities(rows);
	}

	@SuppressWarnings("unchecked")
	public ForgotPasswordToken getFpToken(String token) {
		List<ForgotPasswordToken> result = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_FP_TOKEN)
				.setString("token", token)
				.list();
		
		return result.isEmpty() ? null : result.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public ForgotPasswordToken getFpTokenByUser(Long userId) {
		List<ForgotPasswordToken> result = sessionFactory.getCurrentSession()
				.getNamedQuery(GET_FP_TOKEN_BY_USER)
				.setLong("userId", userId)
				.list();
		
		return result.isEmpty() ? null : result.get(0);
	}
	
	@Override
	public void saveFpToken(ForgotPasswordToken token) {
		sessionFactory.getCurrentSession().saveOrUpdate(token);
	};
	
	@Override
	public void deleteFpToken(ForgotPasswordToken token) {
		getCurrentSession().delete(token);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getActiveUsersEmailIds(Date startDate, Date endDate) {
		return sessionFactory.getCurrentSession()
			.getNamedQuery(GET_ACTIVE_USERS_EMAIL_IDS)
			.setTimestamp("startDate", startDate)
			.setTimestamp("endDate", endDate)
			.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Password> getPasswordsUpdatedBefore(Date updateDate) {
		List<Object[]> rows = getCurrentSession().getNamedQuery(GET_PASSWDS_UPDATED_BEFORE)
			.setDate("updateDate", updateDate)
			.list();

		return rows.stream().map(row -> {
			int idx = 0;

			User user = new User();
			user.setId((Long)row[idx++]);
			user.setFirstName((String)row[idx++]);
			user.setLastName((String)row[idx++]);
			user.setEmailAddress((String)row[idx++]);

			Password password = new Password();
			password.setUser(user);
			password.setUpdationDate((Date)row[idx++]);

			return password;

		}).collect(Collectors.toList());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getInactiveUsers(Date lastLoginTime) {
		return getCurrentSession().getNamedQuery(GET_INACTIVE_USERS)
			.setDate("lastLoginTime", lastLoginTime)
			.list();
	}

	@Override
	public int updateStatus(List<User> users, String status) {
		if (CollectionUtils.isEmpty(users)) {
			return 0;
		}

		return getCurrentSession().getNamedQuery(UPDATE_STATUS)
			.setString("activityStatus", status)
			.setParameterList("userIds", users.stream().map(u -> u.getId()).collect(Collectors.toList()))
			.executeUpdate();
	}

	@Override
	public List<User> getSuperAndInstituteAdmins(String instituteName) {
		UserListCriteria crit = new UserListCriteria().activityStatus("Active").type("SUPER");
		List<User> users = getUsers(crit);
		users.addAll(getUsers(crit.type("INSTITUTE").instituteName(instituteName)));
		return users;
	}

	private Criteria getUsersListQuery(UserListCriteria crit) {
		Criteria criteria = sessionFactory.getCurrentSession()
			.createCriteria(User.class, "u")
			.add( // not system user
				Restrictions.not(Restrictions.conjunction()
					.add(Restrictions.in("u.loginName", excludeUsersList()))
					.add(Restrictions.eq("u.authDomain.name", User.DEFAULT_AUTH_DOMAIN))
				)
			);

		return addSearchConditions(criteria, crit);
	}

	private String[] excludeUsersList() {
		return new String[] {
			User.SYS_USER,
			"public_catalog_user",
			"public_dashboard_user"
		};
	}

	@SuppressWarnings("unchecked")
	private List<User> executeGetUserByLoginNameHql(String hql, String loginName, String domainName) {
		return sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setString("loginName", loginName)
				.setString("domainName", domainName)
				.list();
	}
	
	@SuppressWarnings("unchecked")
	private List<User> executeGetUserByEmailAddressHql(String hql, String emailAddress) {
		return sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setString("emailAddress", emailAddress)
				.list();
	}
	
	private Criteria addSearchConditions(Criteria criteria, UserListCriteria listCrit) {
		String searchString = listCrit.query();
		
		if (StringUtils.isBlank(searchString)) {
			addNameRestriction(criteria, listCrit.name());
			addLoginNameRestriction(criteria, listCrit.loginName());
		} else {
			criteria.add(
				Restrictions.disjunction()
					.add(Restrictions.ilike("u.firstName", searchString, MatchMode.ANYWHERE))
					.add(Restrictions.ilike("u.lastName",  searchString, MatchMode.ANYWHERE))
					.add(Restrictions.ilike("u.loginName", searchString, MatchMode.ANYWHERE))
			);
		}

		if (CollectionUtils.isNotEmpty(listCrit.ids())) {
			criteria.add(Restrictions.in("u.id", listCrit.ids()));
		}
		
		addActivityStatusRestriction(criteria, listCrit.activityStatus());
		addInstituteRestriction(criteria, listCrit.instituteName());
		addDomainRestriction(criteria, listCrit.domainName());
		addTypeRestriction(criteria, listCrit.type());
		addActiveSinceRestriction(criteria, listCrit.activeSince());
		return criteria;
	}

	private void addNameRestriction(Criteria criteria, String name) {
		if (StringUtils.isBlank(name)) {
			return;
		}
		
		criteria.add(
			Restrictions.disjunction()
				.add(Restrictions.ilike("u.firstName", name, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("u.lastName", name, MatchMode.ANYWHERE))
		);
	}
	
	private void addLoginNameRestriction(Criteria criteria, String loginName) {
		if (StringUtils.isBlank(loginName)) {
			return;
		}
		
		criteria.add(Restrictions.ilike("u.loginName", loginName, MatchMode.ANYWHERE));
	}
	
	private void addActivityStatusRestriction(Criteria criteria, String activityStatus) {
		if (StringUtils.isBlank(activityStatus)) {
			return;
		}
		
		criteria.add(Restrictions.eq("u.activityStatus", activityStatus));
	}

	private void addTypeRestriction(Criteria criteria, String type) {
		if (StringUtils.isBlank(type)) {
			return;
		}

		criteria.add(Restrictions.eq("u.type", User.Type.valueOf(type)));
	}
	
	private void addInstituteRestriction(Criteria criteria, String instituteName) {
		if (StringUtils.isBlank(instituteName)) {
			return;
		}
		
		criteria.createAlias("u.institute", "institute")
			.add(Restrictions.eq("institute.name", instituteName));
	}
	
	private void addDomainRestriction(Criteria criteria, String domainName) {
		if (StringUtils.isBlank(domainName)) {
			return;
		}
		
		criteria.createAlias("u.authDomain", "domain")
			.add(Restrictions.eq("domain.name", domainName));
	}

	private void addActiveSinceRestriction(Criteria criteria, Date activeSince) {
		if (activeSince == null) {
			return;
		}

		criteria.add(Restrictions.ge("u.creationDate", Utility.chopTime(activeSince)));
	}

	private List<DependentEntityDetail> getDependentEntities(List<Object[]> rows) {
		List<DependentEntityDetail> dependentEntities = new ArrayList<DependentEntityDetail>();
		
		for (Object[] row: rows) {
			String name = (String)row[0];
			int count = ((Number)row[1]).intValue();
			dependentEntities.add(DependentEntityDetail.from(name, count));
		}
		
		return dependentEntities;
 	}

	private static final String GET_USER_BY_LOGIN_NAME_HQL =
			"from com.krishagni.catissueplus.core.administrative.domain.User where loginName = :loginName and authDomain.name = :domainName  %s";
	
	private static final String GET_USER_BY_EMAIL_HQL = 
			"from com.krishagni.catissueplus.core.administrative.domain.User where emailAddress = :emailAddress %s";
	
	private static final String FQN = User.class.getName();

	private static final String GET_DEPENDENT_ENTITIES = FQN + ".getDependentEntities";

	private static final String TOKEN_FQN = ForgotPasswordToken.class.getName();
	
	private static final String GET_FP_TOKEN_BY_USER = TOKEN_FQN + ".getFpTokenByUser";
	
	private static final String GET_FP_TOKEN = TOKEN_FQN + ".getFpToken";

	private static final String GET_ACTIVE_USERS_EMAIL_IDS = FQN + ".getActiveUsersEmailIds";
	
	private static final String GET_PASSWDS_UPDATED_BEFORE = FQN + ".getPasswordsUpdatedBeforeDate";
	
	private static final String GET_INACTIVE_USERS = FQN + ".getInactiveUsers";
	
	private static final String UPDATE_STATUS = FQN + ".updateStatus";
}
