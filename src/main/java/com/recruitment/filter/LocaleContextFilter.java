package com.recruitment.filter;

import java.io.IOException;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LocaleContextFilter extends OncePerRequestFilter {

	private final LocaleResolver localeResolver;

	public LocaleContextFilter(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		Locale locale = localeResolver.resolveLocale(request);
		LocaleContextHolder.setLocale(locale);
		try {
			filterChain.doFilter(request, response);
		} finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}
}
