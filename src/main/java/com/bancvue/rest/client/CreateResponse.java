package com.bancvue.rest.client;

import com.bancvue.rest.exception.ConflictException;
import com.bancvue.rest.exception.ConflictingEntityException;
import com.bancvue.rest.exception.UnexpectedResponseExceptionFactory;
import com.bancvue.rest.exception.ValidationException;
import org.apache.http.HttpStatus;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public class CreateResponse {

	public static final String ENTITY_ALREADY_EXISTS = "Entity already exists";
	private Response clientResponse;
	private UnexpectedResponseExceptionFactory exceptionFactory;

	public CreateResponse(Response clientResponse, UnexpectedResponseExceptionFactory exceptionFactory) {
		this.clientResponse = clientResponse;
		this.exceptionFactory = exceptionFactory;
	}

	public <T> T assertEntityCreatedAndGetResponse(Class<T> type) {
		return assertEntityCreatedAndGetResponse(type, EntityResolver.CLASS_RESOLVER);
	}

	public <T> T assertEntityCreatedAndGetResponse(GenericType<T> genericType) {
		return assertEntityCreatedAndGetResponse(genericType, EntityResolver.GENERIC_TYPE_RESOLVER);
	}

	private <T> T assertEntityCreatedAndGetResponse(Object typeOrGenericType, EntityResolver resolver) {
		try {
			return doAssertEntityCreatedAndGetResponse(typeOrGenericType, resolver);
		} finally {
			clientResponse.close();
		}
	}

	private <T> T doAssertEntityCreatedAndGetResponse(Object typeOrGenericType, EntityResolver resolver) {
		if (clientResponse.getStatus() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
			String msg = EntityResolver.CLASS_RESOLVER.getEntity(clientResponse, String.class);
			throw new ValidationException(msg);
		} else if (clientResponse.getStatus() == HttpStatus.SC_CONFLICT) {
			T entity = resolver.getEntity(clientResponse, typeOrGenericType);
			if(entity != null){
				throw new ConflictingEntityException(ENTITY_ALREADY_EXISTS, entity);
			}
			throw new ConflictException(ENTITY_ALREADY_EXISTS);
		} else if (clientResponse.getStatus() != HttpStatus.SC_CREATED) {
			throw exceptionFactory.createException(clientResponse);
		}
		return resolver.getEntity(clientResponse, typeOrGenericType);
	}
}
