
package com.krishagni.catissueplus.core.extapp.services.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.Participant;
import com.krishagni.catissueplus.core.extapp.events.SubjectDto;
import com.krishagni.catissueplus.core.extapp.services.CrudService;

import edu.wustl.common.util.XMLPropertyHandler;
import edu.wustl.common.util.logger.Logger;
import edu.wustl.common.velocity.VelocityManager;

public class MirthParticipantService implements CrudService {

	private static final transient Logger logger = Logger.getCommonLogger(MirthParticipantService.class);

	private static CrudService mirthParticipantSvc = null;

	private static String SUBJECT_TEMPLATE = "SubjectTemplate.vm";

	private static String FAIL = "FAIL";

	public static CrudService getInstance() {
		if (mirthParticipantSvc == null) {
			mirthParticipantSvc = new MirthParticipantService();
		}
		return mirthParticipantSvc;
	}

	@Override
	public String insert(Object domainObj, String studyId) {
		SubjectDto subject = populateSubjectDto(domainObj, studyId);
		subject.setOperation("Add");
		String response = sendSubject(subject);
		return response;
	}

	@Override
	public String update(Object domainObj, String studyId) {
		SubjectDto subject = populateSubjectDto(domainObj, studyId);
		subject.setOperation("Update");
		String response = sendSubject(subject);
		return response;
	}

	private SubjectDto populateSubjectDto(Object domainObj, String studyId) {
		Participant participant = (Participant) domainObj;
		SubjectDto subject = new SubjectDto();
		subject.setStudyId(studyId);
		subject.setGender(participant.getGender());
		Date dob = participant.getBirthDate();
		subject.setBirthDate(dob);

		Map<String, CollectionProtocolRegistration> cprCollection = participant.getCprCollection();
		for (Entry<String, CollectionProtocolRegistration> entry : cprCollection.entrySet()) {
			Date regDate = entry.getValue().getRegistrationDate();
			subject.setEnrollmentDate(regDate);
			subject.setLabel(entry.getValue().getProtocolParticipantIdentifier().toString());
		}
		return subject;
	}

	public String sendSubject(SubjectDto subject) {
		URL url;
		HttpURLConnection connection = null;
		try {
			// Create connection
			String mirthUrl = XMLPropertyHandler.getValue("MirthUrl");
			url = new URL(mirthUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/xml");
			List<SubjectDto> subjects = new ArrayList<SubjectDto>();
			subjects.add(subject);

			VelocityManager vm = VelocityManager.getInstance();
			String xml = vm.evaluate(subjects, SUBJECT_TEMPLATE);

			connection.setDoOutput(true);

			connection.setRequestProperty("Content-Length", "" + Integer.toString(xml.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(xml);
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
			}
			rd.close();
			return response.toString();

		}
		catch (Exception e) {
			logger.error("Error while sending http request to mirth of Participant with PPId " + subject.getLabel());
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return FAIL;
	}

	@Override
	public String delete() {
		return null;
	}

}
