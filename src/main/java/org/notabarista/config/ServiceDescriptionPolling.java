package org.notabarista.config;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ServiceDescriptionPolling {

	private static final String DEFAULT_SWAGGER_URL = "/v2/api-docs";
	private static final String KEY_SWAGGER_URL = "swagger_url";

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private ServiceDefinitionsContext definitionContext;

	private final RestTemplate template;

	public ServiceDescriptionPolling() {
		this.template = new RestTemplate();
	}

	@Scheduled(fixedDelayString = "${swagger.config.refreshrate}")
	public void refreshSwaggerConfigurations() {
		log.debug("Starting Service Definition Context refresh");

		discoveryClient.getServices().stream().forEach(serviceId -> {
			log.debug("Attempting service definition refresh for Service : {} ", serviceId);
			List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceId);
			if (serviceInstances == null || serviceInstances.isEmpty()) {
				log.info("No instances available for service : {} ", serviceId);
			} else {
				ServiceInstance instance = serviceInstances.get(0);
				String swaggerURL = getSwaggerURL(instance);

				Optional<Object> jsonData = getSwaggerDefinitionForAPI(serviceId, swaggerURL);

				if (jsonData.isPresent()) {
					String content = getJSON(serviceId, jsonData.get());
					definitionContext.addServiceDefinition(serviceId, content);
				} else {
					log.error("Skipping service id : {} Error : Could not get Swagegr definition from API ", serviceId);
				}

				log.info("Service Definition Context Refreshed at :  {}", LocalDate.now());
			}
		});
	}

	private String getSwaggerURL(ServiceInstance instance) {
		String swaggerURL = instance.getMetadata().get(KEY_SWAGGER_URL);
		return swaggerURL != null ? instance.getUri() + swaggerURL : instance.getUri() + DEFAULT_SWAGGER_URL;
	}

	private Optional<Object> getSwaggerDefinitionForAPI(String serviceName, String url) {
		log.debug("Accessing the SwaggerDefinition JSON for Service : {} : URL : {} ", serviceName, url);
		try {
			Object jsonData = template.getForObject(url, Object.class);
			return Optional.of(jsonData);
		} catch (RestClientException ex) {
			log.error("Error while getting service definition for service : {} Error : {} ", serviceName,
					ex.getMessage());
			return Optional.empty();
		}

	}

	public String getJSON(String serviceId, Object jsonData) {
		try {
			return new ObjectMapper().writeValueAsString(jsonData);
		} catch (JsonProcessingException e) {
			log.error("Error : {} ", e.getMessage());
			return "";
		}
	}
}
