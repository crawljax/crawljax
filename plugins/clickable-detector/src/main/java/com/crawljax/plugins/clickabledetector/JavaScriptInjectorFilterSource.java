package com.crawljax.plugins.clickabledetector;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.regex.Pattern;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * A proxy filter that is capable of inserting strings into the HEAD element of incoming HTML
 * documents. Only if the incoming HTML has a HEAD element the code is added.
 */
public class JavaScriptInjectorFilterSource implements HttpFiltersSource {

	private static final Logger LOG = LoggerFactory
	        .getLogger(JavaScriptInjectorFilterSource.class);

	private final Pattern head = Pattern.compile("\\<HEAD\\>", Pattern.CASE_INSENSITIVE);
	private final List<String> insertInHead;

	/**
	 * @param insertInHeadTop
	 *            A list of strings you want to insert into the top of the HEAD of any incoming
	 *            HTML. The string can be any HTML and is not escaped or validated.
	 */
	public JavaScriptInjectorFilterSource(List<String> insertInHeadTop) {
		this.insertInHead = ImmutableList.copyOf(insertInHeadTop);
	}

	@Override
	public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext context) {

		return new HttpFiltersAdapter(originalRequest) {

			@Override
			public HttpObject responsePre(HttpObject httpObject) {
				if (httpObject instanceof FullHttpResponse
				        && isHtmlResponse((FullHttpResponse) httpObject)) {
					LOG.info("Intercepting {}", originalRequest.getUri());
					return intercept((FullHttpResponse) httpObject);
				}

				return httpObject;
			}
		};
	}

	private boolean isHtmlResponse(FullHttpResponse httpObject) {
		String content = httpObject.headers().get("Content-Type");
		if (content == null) {
			LOG.info("No content type specified");
			return false;
		} else {
			return content.contains("html");
		}
	}

	private FullHttpResponse intercept(FullHttpResponse httpObject) {
		Charset charset = getCharSet(httpObject.headers().get("Content-Type"));
		ByteBuf content = httpObject.content();
		Preconditions.checkState(content.isReadable(), "Content not readable");
		String replacement =
		        head.matcher(content.toString(charset))
		                .replaceFirst("<HEAD> \n" + replacements());
		content.clear();
		content.writeBytes(replacement.getBytes(Charsets.UTF_8));
		HttpHeaders.setContentLength(httpObject, content.readableBytes());
		return httpObject;
	}

	private String replacements() {
		return Joiner.on("\n").join(insertInHead) + "\n";
	}

	private Charset getCharSet(String s) {
		int index = s.toLowerCase().indexOf("charset=");
		if (index > 0) {
			String charset = s.substring(index + 8);
			try {
				return Charset.forName(charset);
			} catch (UnsupportedCharsetException e) {
				LOG.warn("Unsupported charset {}. Using UTF-8", charset);

			}
		}
		return Charsets.UTF_8;
	}

	@Override
	public int getMaximumRequestBufferSizeInBytes() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaximumResponseBufferSizeInBytes() {
		return Integer.MAX_VALUE;
	}

}
