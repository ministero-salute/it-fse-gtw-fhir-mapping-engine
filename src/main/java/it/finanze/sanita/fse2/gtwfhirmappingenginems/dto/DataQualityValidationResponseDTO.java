package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DataQualityValidationResponse", description = "Ordered validation results and their aggregate validity.")
public class DataQualityValidationResponseDTO {
	@Schema(description = "One result for each requested validation, in request order.", requiredMode = Schema.RequiredMode.REQUIRED)
	private List<ValidationResultDTO> results;

	@Schema(description = "True only when every requested validation result is valid. Warnings do not make the response invalid.",
			requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
	public boolean isValid() {
		if (results == null) {
			return true;
		}
		return results.stream().allMatch(ValidationResultDTO::isValid);
	}
}
