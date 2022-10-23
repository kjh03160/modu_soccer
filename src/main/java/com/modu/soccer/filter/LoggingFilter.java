package com.modu.soccer.filter;

import com.modu.soccer.enums.MDCKey;
import com.modu.soccer.utils.UserContextUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;
import software.amazon.awssdk.http.HttpStatusCode;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

	int SLOW_API = 5;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		try {
			setClientInfoOnMDC(request);
			log.info("\n[REQUEST] {} {}\nHeaders : {}\nRequest : {}\n", request.getMethod(),
				request.getRequestURI(),getHeaders(request), getRequestBody(requestWrapper)
			);
			long start = System.currentTimeMillis();
			filterChain.doFilter(requestWrapper, responseWrapper);
			long end = System.currentTimeMillis();
			double elasped = (end - start) / 1000.0;

			if (responseWrapper.getStatus() >= HttpStatusCode.INTERNAL_SERVER_ERROR) {
				log.error("\n[RESPONSE] elasped: {}, status code: {}, body: {}\n",
					elasped, responseWrapper.getStatus(), getResponseBody(responseWrapper));
			} else if (elasped > SLOW_API) {
				log.error("\n[SLOW API RESPONSE] elasped: {}, status code: {}, body: {}\n",
					elasped, responseWrapper.getStatus(), getResponseBody(responseWrapper));
			} else {
				log.info("\n[RESPONSE] elasped: {}, status code: {}, body: {}\n", elasped,
					responseWrapper.getStatus(), getResponseBody(responseWrapper));
			}
		} finally {
			MDC.clear();
			UserContextUtil.clear();
		}
	}

	private Map<String, String> getHeaders(HttpServletRequest request) {
		Map<String, String> headerMap = new HashMap<>();

		Enumeration<String> headerArray = request.getHeaderNames();
		while (headerArray.hasMoreElements()) {
			String headerName = headerArray.nextElement();
			headerMap.put(headerName, request.getHeader(headerName));
		}
		return headerMap;
	}

	private String getRequestBody(ContentCachingRequestWrapper request) {
		ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request,
			ContentCachingRequestWrapper.class);
		if (wrapper != null) {
			byte[] buf = wrapper.getContentAsByteArray();
			if (buf.length > 0) {
				try {
					return new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
				} catch (UnsupportedEncodingException e) {
					return " - ";
				}
			}
		}
		return " - ";
	}

	private String getResponseBody(final HttpServletResponse response) throws IOException {
		String payload = null;
		ContentCachingResponseWrapper wrapper =
			WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
		if (wrapper != null) {
			byte[] buf = wrapper.getContentAsByteArray();
			if (buf.length > 0) {
				payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
				wrapper.copyBodyToResponse();
			}
		}
		return null == payload ? " - " : payload;
	}

	private void setClientInfoOnMDC(HttpServletRequest request) {
		MDC.put(MDCKey.REQUEST_UUID.getKey(), UUID.randomUUID().toString());
		MDC.put(MDCKey.CLIENT_IP.getKey(), getClientIp(request));
//		MDC.put(MDCKey.USER_AGENT.getKey(), request.getHeader("User-Agent"));
	}

	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");

		if (ip == null) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}

