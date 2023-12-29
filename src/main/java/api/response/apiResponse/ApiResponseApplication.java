package api.response.apiResponse;

import api.response.apiResponse.entities.concretes.Address;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@SpringBootApplication
public class ApiResponseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiResponseApplication.class, args);

		String url = "https://www.usom.gov.tr/api/address/index";

		WebClient.Builder builder = WebClient.builder();

		try {
			Address address = builder.build()
					.get()
					.uri(url)
					.retrieve()
					.bodyToMono(Address.class)
					.block();

			System.out.println(address);
		} catch (WebClientResponseException e) {
			System.err.println("Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
		}

	}
}
