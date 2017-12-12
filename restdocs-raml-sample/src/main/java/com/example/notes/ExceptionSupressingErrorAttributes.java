package com.example.notes;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

@Component
class ExceptionSupressingErrorAttributes extends DefaultErrorAttributes {

	@Override
	public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes,
			boolean includeStackTrace) {
		Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, includeStackTrace);
		errorAttributes.remove("exception");
		Object message = requestAttributes.getAttribute("javax.servlet.error.message", RequestAttributes.SCOPE_REQUEST);
		if (message != null) {
			errorAttributes.put("message", message);
		}
		return errorAttributes;
	}
}
