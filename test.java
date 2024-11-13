String uriBuilder = builderFull.build().encode(StandardCharsets.UTF_8).toUriString();
			log.info(LOGGER_SESSIONID, LOGGER_IDTYPE, LOGGER_ID, "In getAllBookedApplicationIds method URL- " + uriBuilder);
			ResponseEntity<MainResponseDTO<List<ApplicationDetailResponseDTO>>> respEntity = selfTokenRestTemplate
					.exchange(uriBuilder, HttpMethod.GET, httpEntity,
