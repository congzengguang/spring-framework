/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet;

import java.util.HashMap;

import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * FlashMap为一个请求提供了一种存储打算在另一个请求中使用的属性的方法. 从一个URL重定向到另一个URL时，最常需要这样做 -- e.g. the Post/Redirect/Get pattern.
 * 之前保存了FlashMap重定向（通常在会话中）并在之后可用 重定向并立即删除。
 *
 * <p>可以使用请求路径和请求参数来设置FlashMap 帮助识别目标请求. 没有这些信息, 一个FlashMap是 提供给下一个请求, 可能是也可能不是预期的收件人。在重定向,
 *  目标URL已知，可以使用该信息更新FlashMap.
 *  这是在自动完成的时候完成的 使用{@code org.springframework.web.servlet.view.RedirectView}。
 *
 * <p>Note: 带注释的控制器通常不会直接使用FlashMap.
 * 例如 {@code org.springframework.web.servlet.mvc.support.RedirectAttributes}
 * 有关在带注释的控制器中使用闪存属性的概述.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 * @see FlashMapManager
 */
@SuppressWarnings("serial")
public final class FlashMap extends HashMap<String, Object> implements Comparable<FlashMap> {

	@Nullable
	private String targetRequestPath;

	private final MultiValueMap<String, String> targetRequestParams = new LinkedMultiValueMap<>(4);

	private long expirationTime = -1;


	/**
	 * Provide a URL path to help identify the target request for this FlashMap.
	 * <p>The path may be absolute (e.g. "/application/resource") or relative to the
	 * current request (e.g. "../resource").
	 */
	public void setTargetRequestPath(@Nullable String path) {
		this.targetRequestPath = path;
	}

	/**
	 * Return the target URL path (or {@code null} if none specified).
	 */
	@Nullable
	public String getTargetRequestPath() {
		return this.targetRequestPath;
	}

	/**
	 * Provide request parameters identifying the request for this FlashMap.
	 * @param params a Map with the names and values of expected parameters
	 */
	public FlashMap addTargetRequestParams(@Nullable MultiValueMap<String, String> params) {
		if (params != null) {
			params.forEach((key, values) -> {
				for (String value : values) {
					addTargetRequestParam(key, value);
				}
			});
		}
		return this;
	}

	/**
	 * Provide a request parameter identifying the request for this FlashMap.
	 * @param name the expected parameter name (skipped if empty)
	 * @param value the expected value (skipped if empty)
	 */
	public FlashMap addTargetRequestParam(String name, String value) {
		if (StringUtils.hasText(name) && StringUtils.hasText(value)) {
			this.targetRequestParams.add(name, value);
		}
		return this;
	}

	/**
	 * Return the parameters identifying the target request, or an empty map.
	 */
	public MultiValueMap<String, String> getTargetRequestParams() {
		return this.targetRequestParams;
	}

	/**
	 * Start the expiration period for this instance.
	 * @param timeToLive the number of seconds before expiration
	 */
	public void startExpirationPeriod(int timeToLive) {
		this.expirationTime = System.currentTimeMillis() + timeToLive * 1000;
	}

	/**
	 * Set the expiration time for the FlashMap. This is provided for serialization
	 * purposes but can also be used instead {@link #startExpirationPeriod(int)}.
	 * @since 4.2
	 */
	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
	}

	/**
	 * Return the expiration time for the FlashMap or -1 if the expiration
	 * period has not started.
	 * @since 4.2
	 */
	public long getExpirationTime() {
		return this.expirationTime;
	}

	/**
	 * Return whether this instance has expired depending on the amount of
	 * elapsed time since the call to {@link #startExpirationPeriod}.
	 */
	public boolean isExpired() {
		return (this.expirationTime != -1 && System.currentTimeMillis() > this.expirationTime);
	}


	/**
	 * Compare two FlashMaps and prefer the one that specifies a target URL
	 * path or has more target URL parameters. Before comparing FlashMap
	 * instances ensure that they match a given request.
	 */
	@Override
	public int compareTo(FlashMap other) {
		int thisUrlPath = (this.targetRequestPath != null ? 1 : 0);
		int otherUrlPath = (other.targetRequestPath != null ? 1 : 0);
		if (thisUrlPath != otherUrlPath) {
			return otherUrlPath - thisUrlPath;
		}
		else {
			return other.targetRequestParams.size() - this.targetRequestParams.size();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FlashMap)) {
			return false;
		}
		FlashMap otherFlashMap = (FlashMap) other;
		return (super.equals(otherFlashMap) &&
				ObjectUtils.nullSafeEquals(this.targetRequestPath, otherFlashMap.targetRequestPath) &&
				this.targetRequestParams.equals(otherFlashMap.targetRequestParams));
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + ObjectUtils.nullSafeHashCode(this.targetRequestPath);
		result = 31 * result + this.targetRequestParams.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "FlashMap [attributes=" + super.toString() + ", targetRequestPath=" +
				this.targetRequestPath + ", targetRequestParams=" + this.targetRequestParams + "]";
	}

}
