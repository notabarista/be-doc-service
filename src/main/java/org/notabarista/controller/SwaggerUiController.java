package org.notabarista.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.notabarista.config.ServiceDefinitionsContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SwaggerUiController {

	@Autowired
	private ServiceDefinitionsContext definitionContext;

	@GetMapping(value = "/")
	public String home(HttpServletRequest req, HttpServletResponse resp) {

		StringBuffer clientHost = new StringBuffer();
		String[] serverUrl = req.getRequestURL().toString().split(":");

		if (req.getHeader("X-Forwarded-Host") != null) {

			if (serverUrl[0].contains("http")) {
				clientHost.append("http://");
			} else {
				if (serverUrl[0].contains("https")) {
					clientHost.append("https://");
				}
			}
			clientHost.append(req.getHeader("X-Forwarded-Host"));
			clientHost.append('/');

		} else {
			// we don't have client host in headers
			clientHost.append(serverUrl);
		}

		return "redirect:" + clientHost.toString() + "apidocs/swagger-ui.html";
	}

	@GetMapping("/apidoc/{servicename}")
	@ResponseBody
	public String getServiceDocs(@PathVariable("servicename") String serviceName) {

		return definitionContext.getSwaggerDefinition(serviceName);
	}
}
