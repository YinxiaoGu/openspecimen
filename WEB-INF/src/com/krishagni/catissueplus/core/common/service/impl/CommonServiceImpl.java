package com.krishagni.catissueplus.core.common.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.domain.UnhandledException;
import com.krishagni.catissueplus.core.common.errors.CommonErrorCode;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.events.UnhandledExceptionSummary;
import com.krishagni.catissueplus.core.common.repository.UnhandledExceptionListCriteria;
import com.krishagni.catissueplus.core.common.service.CommonService;
import com.krishagni.catissueplus.core.common.util.AuthUtil;
import com.krishagni.catissueplus.core.common.util.Utility;
import com.krishagni.rbac.common.errors.RbacErrorCode;

public class CommonServiceImpl implements CommonService {
	private static final Log logger = LogFactory.getLog(CommonServiceImpl.class);

	private DaoFactory daoFactory;
	
	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<UnhandledExceptionSummary>> getUnhandledExceptions(RequestEvent<UnhandledExceptionListCriteria> req) {
		try {
			User user = AuthUtil.getCurrentUser();
			if (!user.isInstituteAdmin() && !user.isAdmin()) {
				return ResponseEvent.userError(RbacErrorCode.ADMIN_RIGHTS_REQUIRED);
			}

			UnhandledExceptionListCriteria listCrit = req.getPayload();
			if (listCrit.fromDate() != null) {
				listCrit.fromDate(Utility.chopTime(listCrit.fromDate()));
			}
			
			if (listCrit.toDate() != null) {
				listCrit.toDate(Utility.getEndOfDay(listCrit.toDate()));
			}

			if (!user.isAdmin()) {
				//
				// must be institute admin; therefore filter exception list based on
				// exceptions received by institute users
				//
				listCrit.instituteId(user.getInstitute().getId());
			}
			
			List<UnhandledException> exceptions = daoFactory.getUnhandledExceptionDao().getUnhandledExceptions(listCrit);
			return ResponseEvent.response(UnhandledExceptionSummary.from(exceptions));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<String> getUnhandledExceptionLog(RequestEvent<Long> req) {
		try {
			User user = AuthUtil.getCurrentUser();
			if (!user.isInstituteAdmin() && !user.isAdmin()) {
				throw OpenSpecimenException.userError(RbacErrorCode.ADMIN_RIGHTS_REQUIRED);
			}

			UnhandledException exception = daoFactory.getUnhandledExceptionDao().getById(req.getPayload());
			if (exception == null) {
				return ResponseEvent.userError(CommonErrorCode.EXCEPTION_NOT_FOUND, req.getPayload());
			}

			if (!user.isAdmin() && !user.getInstitute().equals(exception.getUser().getInstitute())) {
				return ResponseEvent.userError(RbacErrorCode.ACCESS_DENIED);
			}

			String log = new StringBuilder()
				.append("Input Arguments: \n").append(exception.getInputArgs()).append("\n")
				.append("Stack Trace: \n").append(exception.getStackTrace()).append("\n")
				.toString();
			return ResponseEvent.response(log);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public Long saveUnhandledException(UnhandledException exception) {
		Long exceptionId = null;

		try {
			daoFactory.getUnhandledExceptionDao().saveOrUpdate(exception, true);
			exceptionId = exception.getId();
		} catch (Exception e) {
			logger.error("Error saving details of unhandled exception to DB", e);
		}

		return exceptionId;
	}

	@Override
	public String getLatestReleaseNotes() {
		try {
			Resource[] resources = new PathMatchingResourcePatternResolver().getResources("/release-notes/*");
			List<File> files = new ArrayList<>();
			for (Resource resource : resources) {
				File file = resource.getFile();
				if (file.isDirectory() || !file.getName().endsWith(".html")) {
					continue;
				}

				files.add(file);
			}

			File file = files.stream()
				.sorted((f1, f2) -> -1 * f1.getName().compareTo(f2.getName()))
				.findFirst().orElse(null);
			return file != null ? Utility.getFileText(file) : "No release notes!";
		} catch (Exception e) {
			logger.error("Error reading release notes", e);
			throw new RuntimeException("Error reading release notes", e);
		}
	}
}
