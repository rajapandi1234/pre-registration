/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import lombok.Getter;

/**
 * This class defines the ConnectionUnavailableException that occurs when the
 * connection is unavailable.
 * 
 * @author Rajath KR
 * @since 1.0.0
 * 
 */

@Getter
public class CephConnectionUnavailableException extends BaseUncheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private MainResponseDTO<?> response;

	/**
	 * Default constructor
	 */
	public CephConnectionUnavailableException() {
		super();
	}

	/**
	 * @param errorCode pass Error code
	 * @param message   pass Error Message
	 * @param cause     pass Error cause
	 */
	public CephConnectionUnavailableException(String errorCode, String message, Throwable cause,
			MainResponseDTO<?> response) {
		super(errorCode, message, cause);
		this.response = response;
	}

	/**
	 * @param errorCode pass Error code
	 * @param message   pass Error Message
	 */
	public CephConnectionUnavailableException(String errorCode, String message) {
		super(errorCode, message);
	}

	/**
	 * 
	 * @param errorCode pass Error code
	 * @param message   pass Error Message
	 * @param response  pass response
	 */
	public CephConnectionUnavailableException(String errorCode, String message, MainResponseDTO<?> response) {
		super(errorCode, message);
		this.response = response;
	}
}