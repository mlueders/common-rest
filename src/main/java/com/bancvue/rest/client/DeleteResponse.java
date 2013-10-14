package com.bancvue.rest.client;

import com.bancvue.rest.exception.UnexpectedResponseExceptionFactory;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import org.apache.http.HttpStatus;

public class DeleteResponse {

	private ClientResponse clientResponse;
	private UnexpectedResponseExceptionFactory exceptionFactory;

	public DeleteResponse(ClientResponse clientResponse, UnexpectedResponseExceptionFactory exceptionFactory) {
		this.clientResponse = clientResponse;
		this.exceptionFactory = exceptionFactory;
	}

	public <T> T assertEntityDeletedAndGet(Class<T> type) {
		return assertEntityDeletedAndGet(type, EntityResolver.CLASS_RESOLVER);
	}

	public <T> T assertEntityDeletedAndGet(GenericType<T> type) {
		return assertEntityDeletedAndGet(type, EntityResolver.GENERIC_TYPE_RESOLVER);
	}

	private <T> T assertEntityDeletedAndGet(Object typeOrGenericType, EntityResolver resolver) {
		try {
			return doAssertEntityDeletedAndGet(typeOrGenericType, resolver);
		} finally {
			clientResponse.close();
		}
	}

	private <T> T doAssertEntityDeletedAndGet(Object typeOrGenericType, EntityResolver resolver) {
		if (clientResponse.getStatus() == HttpStatus.SC_OK) {
			return resolver.getEntity(clientResponse, typeOrGenericType);
		} else if (clientResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
			return null;
		}
		throw exceptionFactory.createException(clientResponse);
	}

}
