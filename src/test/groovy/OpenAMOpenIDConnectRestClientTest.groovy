import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import spock.lang.Ignore
import spock.lang.Specification

class OpenAMOpenIDConnectRestClientTest extends Specification{


	def openam = new RESTClient("http://openam.example.com:8080/")
	def clientName = "example_rest_client"
	def realm = "/"

	@Ignore("utility method")
	def "authenticate"(){
		return openam.post(
				path: "/openam/json/authenticate",
				contentType: ContentType.JSON,
				headers: [
					'X-OpenAM-Username': 'amAdmin',
					'X-OpenAM-Password': 'password',
				],
				body: []).data.tokenId
	}

	def "authenticate_test"() {
		when:
		def tokenId = authenticate()
		then:
		tokenId != null
	}

	def "register_oauth2client"() {
		setup:
		def tokenId = authenticate()
		when:
		def res = openam.post(
				path: "/openam/frrest/oauth2/client",
				query: [ _action: "create" ],
				contentType: ContentType.JSON,
				headers: [
					'iPlanetDirectoryPro': tokenId
				],
				body: [
					"client_id":[clientName],
					"realm":[realm],
					"userpassword":["password"],
					"com.forgerock.openam.oauth2provider.clientType":["Public"],
					"com.forgerock.openam.oauth2provider.redirectionURIs":
					[
						"www.client.com",
						"www.example.com"
					],
					"com.forgerock.openam.oauth2provider.scopes":["cn", "sn"],
					"com.forgerock.openam.oauth2provider.defaultScopes":["cn"],
					"com.forgerock.openam.oauth2provider.name":["My Test Client"],
					"com.forgerock.openam.oauth2provider.description":["OAuth 2.0 Client"]]
				);
		then:
		res.data.success == "true"
	}
	def "get_info_oauth2client"() {
		setup:
		def tokenId = authenticate()
		when:
		def res = openam.get(
				path: "/openam/json/agents/${clientName}",
				contentType: ContentType.JSON,
				headers: [
					'iPlanetDirectoryPro': tokenId
				],
				);
		then:
		res.data.username == clientName
	}

	def "unregister_oauth2client"() {
		setup:
		def tokenId = authenticate()

		when:
		def res = openam.delete(
				path: "/openam/frrest/oauth2/client/${clientName}",
				query: [ realm: realm ],
				contentType: ContentType.JSON,
				headers: [
					'iPlanetDirectoryPro': tokenId
				],
				);
		then:
		res.data.success == "true"
	}
}
