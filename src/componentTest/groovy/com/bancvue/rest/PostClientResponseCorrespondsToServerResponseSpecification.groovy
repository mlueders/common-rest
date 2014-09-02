package com.bancvue.rest

import com.bancvue.rest.client.ClientRequestExecutor
import com.bancvue.rest.client.response.CreateResponse
import com.bancvue.rest.example.Widget
import com.bancvue.rest.exception.ConflictingEntityException
import com.bancvue.rest.exception.ValidationException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.client.WebTarget

class PostClientResponseCorrespondsToServerResponseSpecification extends BaseTestSpec {

	private WebTarget widgetResource
	private ClientRequestExecutor clientRequestExecutor

	void setup() {
		widgetResource = baseServiceResource.path("widgets")
		clientRequestExecutor = new ClientRequestExecutor()
		widgetRepository.clear()
	}

	private Widget addWidget(String id) {
		Widget widget = new Widget(id: id)
		widgetRepository.put(id, widget)
		widget
	}

	def "success should return status code 201 and location, client response should convert and return entity"() {
		Widget widget = new Widget(id: "created")

		when:
		CreateResponse createResponse = clientRequestExecutor.createWithPost(widgetResource, widget)

		then:
		createResponse.clientResponse.getStatus() == 201
		createResponse.clientResponse.getLocation() as String == "http://localhost:8080/widgets/created"

		when:
		Widget actualWidget = createResponse.getValidatedResponse(Widget)

		then:
		widget == actualWidget
		!widget.is(actualWidget)
	}

	def "object already exists should return status code 409 with entity, client response should convert to exception"() {
		Widget widget = addWidget("duplicate")

		when:
		CreateResponse createResponse = clientRequestExecutor.createWithPost(widgetResource, widget)

		then:
		createResponse.clientResponse.getStatus() == 409

		when:
		createResponse.getValidatedResponse(Widget)

		then:
		ConflictingEntityException ex = thrown(ConflictingEntityException)
		ex.entity == widget
	}

	def "invalid object should return status code 422, client response should convert to exception"() {
		Widget invalid = new Widget()

		when:
		CreateResponse createResponse = clientRequestExecutor.createWithPost(widgetResource, invalid)

		then:
		createResponse.clientResponse.getStatus() == 422

		when:
		createResponse.getValidatedResponse(Widget)

		then:
		thrown ValidationException

		// TODO: what about the body?  can we standardize on reporting invalid objects?
	}

	def "application error should return status code 500, client response should convert to http exception"() {
		Widget widget = new Widget(id: "app-error")
		widget.initApplicationError()

		when:
		CreateResponse createResponse = clientRequestExecutor.createWithPost(widgetResource, widget)

		then:
		createResponse.clientResponse.getStatus() == 500

		when:
		createResponse.getValidatedResponse(Widget)

		then:
		WebApplicationException ex = thrown()
		ex.response.status == 500
	}
}
