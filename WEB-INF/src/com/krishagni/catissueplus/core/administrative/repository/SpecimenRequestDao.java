package com.krishagni.catissueplus.core.administrative.repository;

import java.util.List;

import com.krishagni.catissueplus.core.administrative.domain.SpecimenRequest;
import com.krishagni.catissueplus.core.common.repository.Dao;

public interface SpecimenRequestDao extends Dao<SpecimenRequest> {
	public List<SpecimenRequest> getSpecimenRequests(SpecimenRequestListCriteria crit);
}
