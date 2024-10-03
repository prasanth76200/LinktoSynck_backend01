// package com.example.linktosync.configs;

// // import org.springframework.core.MethodParameter;
// // import org.springframework.http.HttpOutputMessage;
// import org.springframework.http.converter.HttpMessageConverter;
// import org.springframework.stereotype.Component;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// // import org.springframework.web.bind.annotation.GetMapping;
// // import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

// // import org.springframework.http.server.ServerHttpRequest;
// // import org.springframework.http.server.ServerHttpResponse;
// import org.springframework.security.web.csrf.CsrfToken;

// import io.swagger.v3.oas.models.media.MediaType;

// @ControllerAdvice
// @Component
// public class CsrfHeaderModifier implements ResponseBodyAdvice<CsrfToken> {

//     @Override
//     public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
//         return CsrfToken.class.isAssignableFrom(returnType.getParameterType());
//     }

//     @Override
//     public CsrfToken beforeBodyWrite(CsrfToken body, 
//                                       MethodParameter returnType, 
//                                       MediaType selectedContentType, 
//                                       Class<? extends HttpMessageConverter<?>> selectedConverterType, 
//                                       ServerHttpRequest request, 
//                                       ServerHttpResponse response) {
//         response.getHeaders().add("Set-Cookie", "XSRF-TOKEN=" + body.getToken() + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=3600;");
//         return body;
//     }
// }
