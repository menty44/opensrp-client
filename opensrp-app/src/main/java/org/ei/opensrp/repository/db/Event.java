package org.ei.opensrp.repository.db;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event extends BaseDataObject{
	private String _id;

	private Map<String, String> identifiers;

	private String baseEntityId;
	
	private String locationId;
	
	private DateTime eventDate;
	
	private String eventType;
	
	private String formSubmissionId;
	
	private String providerId;
	
	private List<Obs> obs;
	
	private String entityType;
	
	private Map<String, String> details;
	
	private long version;
	
	public Event() {
		this.version = System.currentTimeMillis();
	}

	public Event(String baseEntityId, String id, String eventType, DateTime eventDate, String entityType,
			String providerId, String locationId, String formSubmissionId) {
		this.baseEntityId = baseEntityId;
		this._id = id;
		this.eventType = eventType;
		this.eventDate = eventDate;
		this.entityType = entityType;
		this.providerId = providerId;
		this.locationId = locationId;
		this.formSubmissionId = formSubmissionId;
		this.version = System.currentTimeMillis();
	}
	
	public List<Obs> getObs() {
		if(obs == null){
			obs = new ArrayList<>();
		}
		return obs;
	}

	public Obs getObs(String parent, String concept) {
		if(obs == null){
			obs = new ArrayList<>();
		}
		for (Obs o : obs) {
			// parent is blank OR matches with obs parent
			if((StringUtils.isBlank(parent)
					|| (StringUtils.isNotBlank(o.getParentCode()) && parent.equalsIgnoreCase(o.getParentCode())))
				&& o.getFieldCode().equalsIgnoreCase(concept)){
				return o; //TODO handle duplicates
			}
		}
		return null;
	}

	/**
	 * WARNING: Overrides all existing obs
	 * @param obs
	 * @return
	 */
	public void setObs(List<Obs> obs) {
		this.obs = obs;
	}
	
	public void addObs(Obs observation) {
		if(obs == null){
			obs = new ArrayList<>();
		}
		
		obs.add(observation);
	}
	
	public String getBaseEntityId() {
		return baseEntityId;
	}
	
	public void setBaseEntityId(String baseEntityId) {
		this.baseEntityId = baseEntityId;
	}

	public Map<String, String> getIdentifiers() {
		if(identifiers == null){
			identifiers = new HashMap<>();
		}
		return identifiers;
	}

	public String getIdentifier(String identifierType) {
		if(identifiers == null){
			return null;
		}
		for (String k : identifiers.keySet()) {
			if(k.equalsIgnoreCase(identifierType)){
				return identifiers.get(k);
			}
		}
		return null;
	}

	/**
	 * Returns field matching the regex. Note that incase of multiple fields matching criteria
	 * function would return first match. The must be well formed to find out a single value
	 * @param regex
	 * @return
	 */
	public String getIdentifierMatchingRegex(String regex) {
		for (Map.Entry<String, String> a : getIdentifiers().entrySet()) {
			if(a.getKey().matches(regex)){
				return a.getValue();
			}
		}
		return null;
	}

	public void setIdentifiers(Map<String, String> identifiers) {
		this.identifiers = identifiers;
	}

	public void addIdentifier(String identifierType, String identifier) {
		if(identifiers == null){
			identifiers = new HashMap<>();
		}

	}

	public void removeIdentifier(String identifierType) {
		identifiers.remove(identifierType);
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public DateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(DateTime eventDate) {
		this.eventDate = eventDate;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getFormSubmissionId() {
		return formSubmissionId;
	}

	public void setFormSubmissionId(String formSubmissionId) {
		this.formSubmissionId = formSubmissionId;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public Map<String, String> getDetails() {
		return details;
	}

	public void setDetails(Map<String, String> details) {
		this.details = details;
	}
	
	public void addDetails(String key, String val) {
		if(details == null){
			details = new HashMap<>();
		}
		details.put(key, val);
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Obs findObs(String parentId, boolean nonEmpty, String... fieldIds){
		Obs res = null;
		for (String f : fieldIds){
			for(Obs o : getObs()){
				// if parent is specified and not matches leave and move forward
				if(StringUtils.isNotBlank(parentId) && !o.getParentCode().equalsIgnoreCase(parentId)){
					continue;
				}

				if(o.getFieldCode().equalsIgnoreCase(f) || o.getFormSubmissionField().equalsIgnoreCase(f)){
					// obs is found and first  one.. should throw exception if multiple obs found with same names/ids
					if(nonEmpty && (o.getValues().isEmpty())){
						continue;
					}
					if(res == null){
						res = o;
					}
					else throw new RuntimeException("Multiple obs found with name or ids specified ");
				}
			}
		}
		return res;
	}

	public Event withBaseEntityId(String baseEntityId) {
		this.baseEntityId = baseEntityId;
		return this;
	}

	/**
	 * WARNING: Overrides all existing identifiers
	 * @param identifiers
	 * @return
	 */
	public Event withIdentifiers(Map<String, String> identifiers) {
		this.identifiers = identifiers;
		return this;
	}

	public Event withIdentifier(String identifierType, String identifier) {
		if(identifiers == null){
			identifiers = new HashMap<>();
		}
		identifiers.put(identifierType, identifier);
		return this;
	}

	public Event withLocationId(String locationId) {
		this.locationId = locationId;
		return this;
	}

	public Event withEventDate(DateTime eventDate) {
		this.eventDate = eventDate;
		return this;
	}

	public Event withEventType(String eventType) {
		this.eventType = eventType;
		return this;
	}

	public Event withFormSubmissionId(String formSubmissionId) {
		this.formSubmissionId = formSubmissionId;
		return this;
	}

	public Event withProviderId(String providerId) {
		this.providerId = providerId;
		return this;
	}

	public Event withEntityType(String entityType) {
		this.entityType = entityType;
		return this;
	}
	
	/**
	 * WARNING: Overrides all existing obs
	 * @param obs
	 * @return
	 */
	public Event withObs(List<Obs> obs) {
		this.obs = obs;
		return this;
	}
	
	public Event withObs(Obs observation) {
		if(obs == null){
			obs = new ArrayList<>();
		}
		obs.add(observation);
		return this;
	}
	
}
