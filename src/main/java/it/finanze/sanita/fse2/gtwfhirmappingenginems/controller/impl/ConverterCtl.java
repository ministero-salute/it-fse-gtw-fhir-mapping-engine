package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.IConverterCtl;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirConverterReqDto;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.FHIRR4Helper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConverterSrv;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ConverterCtl implements IConverterCtl {

	@Autowired
	private IConverterSrv converterSrv;
	
	@Override
	public TransformResDTO convertToTransactionBundle(FhirConverterReqDto fhirConverterDto, HttpServletRequest request) {
		TransformResDTO out = new TransformResDTO();
		Bundle bundle = FHIRR4Helper.deserializeResource(Bundle.class, fhirConverterDto.getBundleDocument(), true);
		out.setJson(converterSrv.convert(bundle, fhirConverterDto.getDocumentReferenceDTO()));
		return out;
	}

}
