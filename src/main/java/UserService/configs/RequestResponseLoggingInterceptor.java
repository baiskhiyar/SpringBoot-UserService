package UserService.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collection;

@Component
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Log the request details before going to the controller.
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        logRequest(requestWrapper);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Log the response details after the controller returns a response.
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        logResponse(responseWrapper, request);
        responseWrapper.copyBodyToResponse(); // Copy the cached response body to the actual response
    }

    private void logRequest(ContentCachingRequestWrapper requestWrapper) throws IOException {
        String requestURI = requestWrapper.getRequestURI();
        String method = requestWrapper.getMethod();
        String queryString = requestWrapper.getQueryString();
        String payload = getPayload(requestWrapper);
        List<String> headers = getHeaders(requestWrapper);

        logger.info("Request: {} {}?{} Payload: {} Headers: {}", method, requestURI, queryString, payload, headers);
    }

    private void logResponse(ContentCachingResponseWrapper responseWrapper, HttpServletRequest request) throws IOException {
        String requestURI = request.getRequestURI();
        int status = responseWrapper.getStatus();
        String payload = getResponsePayload(responseWrapper);
        List<String> headers = getResponseHeaders(responseWrapper);

        logger.info("Response: {} Status: {} Payload: {} Headers: {}", requestURI, status, payload, headers);
    }

    private String getPayload(ContentCachingRequestWrapper request) throws IOException {
        ContentCachingRequestWrapper wrapper =  request;
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    String payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                    return payload;
                }
                catch (UnsupportedEncodingException e) {
                    return "[unknown]";
                }
            }
        }
        return "[empty]";
    }

    private String getResponsePayload(ContentCachingResponseWrapper response) throws IOException {
        ContentCachingResponseWrapper wrapper = response;
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    return new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                }
                catch (UnsupportedEncodingException e) {
                    return "[unknown]";
                }
            }
        }
        return "[empty]";
    }

    private List<String> getHeaders(HttpServletRequest request) {
        List<String> headerNames = Arrays.stream(request.getHeaderNames().asIterator().next().split(",")).toList();
        return headerNames.stream().map(header -> header + ":" + request.getHeader(header)).collect(Collectors.toList());
    }

    private List<String> getResponseHeaders(HttpServletResponse response) {
        Collection<String> headerNames = response.getHeaderNames();
        return headerNames.stream().map(header -> header + ":" + response.getHeader(header)).collect(Collectors.toList());
    }
}
