package com.bancvue.rest

import com.bancvue.rest.client.ClientRequestExecutor
import com.bancvue.rest.client.response.GetResponse
import com.bancvue.rest.example.Widget
import com.bancvue.rest.exception.NotFoundException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.client.WebTarget
import spock.lang.Shared

class GetClientResponseCorrespondsToServerResponseSpecification extends BaseTestSpec {

	@Shared
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

	def "success should return status code 200, client response should convert and return entity"() {
		Widget expectedWidget = addWidget("wid")

		when:
		GetResponse getResponse = clientRequestExecutor.get(widgetResource.path("wid"))

		then:
		getResponse.clientResponse.getStatus() == 200

		when:
		Widget actualWidget = getResponse.getValidatedResponse(Widget)

		then:
		expectedWidget == actualWidget
		!expectedWidget.is(actualWidget)
	}

	def "should throw not found exception when return status code 404"() {
		when:
		GetResponse getResponse = clientRequestExecutor.get(widgetResource.path("wid"))

		then:
		getResponse.clientResponse.getStatus() == 404

		when:
		getResponse.getValidatedResponse(Widget)

		then:
		thrown NotFoundException
	}

	def "application error should return status code 500, client response should convert to http exception"() {
		Widget expectedWidget = addWidget("wid")
		expectedWidget.initApplicationError()

		when:
		GetResponse getResponse = clientRequestExecutor.get(widgetResource.path("wid"))

		then:
		getResponse.clientResponse.getStatus() == 500

		when:
		getResponse.getValidatedResponse(Widget)

		then:
		WebApplicationException ex = thrown()
		ex.response.status == 500
	}
}
