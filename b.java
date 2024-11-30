
package io.mosip.preregistration.datasync.service.util;

import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_ID;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_IDTYPE;
import static io.mosip.preregistration.core.constant.PreRegCoreConstant.LOGGER_SESSIONID;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
# Mock secret for testing
API_KEY = "test1234567890abcdef"

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.clientcrypto.dto.TpmCryptoRequestDto;
import io.mosip.kernel.clientcrypto.dto.TpmCryptoResponseDto;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoManagerService;
import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.kernel.signature.dto.JWTSignatureRequestDto;
import io.mosip.kernel.signature.dto.JWTSignatureResponseDto;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.BookingDataByRegIdDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentDTO;
import io.mosip.preregistration.core.common.dto.DocumentMultipartResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentsMetaData;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdResponseDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.PreRegistrationException;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;
import io.mosip.preregistration.core.util.UUIDGeneratorUtil;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.datasync.code.RequestCodes;
import io.mosip.preregistration.datasync.dto.ApplicationDetailResponseDTO;
import io.mosip.preregistration.datasync.dto.ApplicationInfoMetadataDTO;
import io.mosip.preregistration.datasync.dto.ClientPublickeyDTO;
import io.mosip.preregistration.datasync.dto.DataSyncRequestDTO;
import io.mosip.preregistration.datasync.dto.DocumentMetaDataDTO;
import io.mosip.preregistration.datasync.dto.PreRegArchiveDTO;
import io.mosip.preregistration.datasync.dto.PreRegistrationIdsDTO;
import io.mosip.preregistration.datasync.dto.ReverseDataSyncRequestDTO;
import io.mosip.preregistration.datasync.dto.ReverseDatasyncReponseDTO;
import io.mosip.preregistration.datasync.entity.InterfaceDataSyncEntity;
import io.mosip.preregistration.datasync.entity.InterfaceDataSyncTablePK;
import io.mosip.preregistration.datasync.entity.ProcessedPreRegEntity;
import io.mosip.preregistration.datasync.errorcodes.ErrorCodes;
import io.mosip.preregistration.datasync.errorcodes.ErrorMessages;
import io.mosip.preregistration.datasync.exception.DataSyncRecordNotFoundException;
import io.mosip.preregistration.datasync.exception.DemographicGetDetailsException;
import io.mosip.preregistration.datasync.exception.DocumentGetDetailsException;
import io.mosip.preregistration.datasync.exception.RecordNotFoundForDateRange;
import io.mosip.preregistration.datasync.exception.ZipFileCreationException;
import io.mosip.preregistration.datasync.exception.system.SystemFileIOException;
import io.mosip.preregistration.datasync.repository.DemographicConsumedRepository;
import io.mosip.preregistration.datasync.repository.InterfaceDataSyncRepo;
import io.mosip.preregistration.datasync.repository.ProcessedDataSyncRepo;
import jakarta.annotation.PostConstruct;

/**
 * This class is used to define Error codes for data sync and reverse data sync
 * 
 * @author Jagadishwari S
 * @author Ravi C Balaji
 * @since 1.0.0
 */
@Component
public class DataSyncServiceUtil {

	/**
	 * Autowired reference for {@link #InterfaceDataSyncRepo}
	 */
	@Autowired
	private InterfaceDataSyncRepo interfaceDataSyncRepo;

	/**
	 * Autowired reference for {@link #ProcessedDataSyncRepo}
	 */
	@Autowired
	private ProcessedDataSyncRepo processedDataSyncRepo;

	@Autowired
	private DemographicConsumedRepository demographicConsumedRepository;

	/**
	 * Autowired reference for {@link #RestTemplate}
	 */
	@Qualifier("selfTokenRestTemplate")
	@Autowired
	RestTemplate selfTokenRestTemplate;

	@Autowired
	@Lazy
	private ClientCryptoManagerService clientCryptoManagerService;

	/**
	 * Reference for ${demographic.resource.url} from property file
	 */
	@Value("${demographic.resource.url}")
	private String demographicResourceUrl;

	/**
	 * Reference for ${document.resource.url} from property file
	 */
	@Value("${document.resource.url}")
	private String documentResourceUrl;

	@Value("${syncdata.resource.url}")
	private String syncdataResourceUrl;

	@Value("${cryptoResource.url}")
	private String keymanagerResourceUrl;

	/**
	 * Reference for ${poa.url} from property file
	 */
	@Value("${poa.url}")
	private String poaUrl;

	/**
	 * Reference for ${poi.url} from property file
	 */
	@Value("${poi.url}")
	private String poiUrl;

	/**
	 * Reference for ${por.url} from property file
	 */
	@Value("${por.url}")
	private String porUrl;

	/**
	 * Reference for ${pod.url} from property file
	 */
	@Value("${pod.url}")
	private String podUrl;

	/**
	 * Reference for ${booking.resource.url} from property file
	 */
	@Value("${booking.resource.url}")
	private String bookingResourceUrl;

	/**
	 * Reference for ${mosip.utc-datetime-pattern} from property file
	 */
	@Value("${mosip.utc-datetime-pattern}")
	private String dateTimeFormat;

	@Value("${version:1.0}")
	private String version;

	@Value("${mosip.preregistration.sync.sign.appid}")
	private String signAppId;

	@Value("${mosip.preregistration.sync.sign.refid}")
	private String signRefId;

	@Value("${moispDemographicRequestId:mosip.pre-registration.demographic.retrieve.date}")
	private String moispDemographicRequestId;

	/**
	 * Autowired reference for {@link #ValidationUtil}
	 */
	private ValidationUtil validationUtil;

	/**
	 * ObjectMapper global object creation
	 */
	private ObjectMapper mapper;

	@Autowired
	public DataSyncServiceUtil(ValidationUtil validationUtil) {
		this.validationUtil = validationUtil;
	}

	@PostConstruct
    public void init() {
		mapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
		mapper.registerModule(new JavaTimeModule());
	}

	/**
	 * Logger configuration initialization
	 */
	private static Logger log = LoggerConfiguration.logConfig(DataSyncServiceUtil.class);

	/**
	 * This method is used to validate data sync request parameters
	 * 
	 * @param dataSyncRequest object
	 * @param mainResponseDTO
	 * @return true or false
	 */
	public boolean validateDataSyncRequest(DataSyncRequestDTO dataSyncRequest, MainResponseDTO<?> mainResponseDTO) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In validateDataSyncRequest method of datasync service util");
		String regId = dataSyncRequest.getRegistrationCenterId();
		String fromDate = dataSyncRequest.getFromDate();
		String format = "yyyy-MM-dd";

		if (isNull(regId)) {
			throw new InvalidRequestParameterException(ErrorCodes.PRG_DATA_SYNC_009.getCode(),
					ErrorMessages.INVALID_REGISTRATION_CENTER_ID.getMessage(), mainResponseDTO);
		} else if (isNull(fromDate) || !ValidationUtil.parseDate(fromDate, format)) {
			throw new InvalidRequestParameterException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_019.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.INVALID_DATE_TIME_FORMAT.getMessage(),
					mainResponseDTO);
		} else if (!isNull(dataSyncRequest.getToDate())
				&& !ValidationUtil.parseDate(dataSyncRequest.getToDate(), format)) {
			throw new InvalidRequestParameterException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_019.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.INVALID_DATE_TIME_FORMAT.getMessage(),
					mainResponseDTO);
		} else if (!isNull(fromDate) && !isNull(dataSyncRequest.getToDate())
				&& ((LocalDate.parse(fromDate)).isAfter(LocalDate.parse(dataSyncRequest.getToDate())))) {
			throw new InvalidRequestParameterException(
					io.mosip.preregistration.core.errorcodes.ErrorCodes.PRG_CORE_REQ_020.getCode(),
					io.mosip.preregistration.core.errorcodes.ErrorMessages.FROM_DATE_GREATER_THAN_TO_DATE.getMessage(),
					mainResponseDTO);
		}
		return true;
	}

	/**
	 * This method is used to validate reverse data sync request parameters
	 * 
	 * @param reverseDataSyncRequest
	 * @param mainResponseDTO
	 * @return true or false
	 */
	public boolean validateReverseDataSyncRequest(ReverseDataSyncRequestDTO reverseDataSyncRequest,
			MainResponseDTO<?> mainResponseDTO) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID,
				"In validateReverseDataSyncRequest sync preregids" + reverseDataSyncRequest.getPreRegistrationIds());
		List<String> preRegIdsList = reverseDataSyncRequest.getPreRegistrationIds();
		if (preRegIdsList == null || isNull(preRegIdsList)) {
			throw new InvalidRequestParameterException(ErrorCodes.PRG_DATA_SYNC_011.getCode(),
					ErrorMessages.INVALID_REQUESTED_PRE_REG_ID_LIST.getMessage(), mainResponseDTO);
		}
		return true;
	}

	/**
	 * This method invokes booking API through rest template to fetch the list of
	 * preIds for the date range and reg center Id
	 * 
	 * @param fromDate
	 * @param toDate
	 * @param regCenterId
	 * @return preRegIdsByRegCenterIdResponseDTO
	 */
	public BookingDataByRegIdDto getBookedPreIdsByDateAndRegCenterIdRestService(String fromDate, String toDate,
			String regCenterId) {
		log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In callGetPreIdsRestService method of datasync service util");
		BookingDataByRegIdDto preRegIdsByRegCenterIdResponseDTO = null;
		try {
			Map<String, String> params = new HashMap<>();
			params.put("registrationCenterId", regCenterId);
			UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
					.fromHttpUrl(bookingResourceUrl + "/appointment/registrationCenterId/{registrationCenterId}");
			URI uri = uriComponentsBuilder.buildAndExpand(params).toUri();
			UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri).queryParam("from_date", fromDate)
					.queryParam("to_date", toDate);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<MainResponseDTO<PreRegIdsByRegCenterIdResponseDTO>> httpEntity = new HttpEntity<>(headers);
			String uriBuilder = builder.build().encode(StandardCharsets.UTF_8).toUriString();
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In callGetPreIdsRestService method URL- " + uriBuilder);
			ResponseEntity<MainResponseDTO<BookingDataByRegIdDto>> respEntity = selfTokenRestTemplate.exchange(uriBuilder,
