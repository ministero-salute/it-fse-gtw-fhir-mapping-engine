package it.finanze.sanita.fse2.gtwfhirmappingenginems.cda;

import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.SystemTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
public class CDAHelper {

    public static ExtractedInfoDTO extractInfo(final String cda, String system) {
        ExtractedInfoDTO out = null;
        try {
            org.jsoup.nodes.Document docT = Jsoup.parse(cda);

            List<String> templateIdRoots = docT.select("templateid")
                    .stream()
                    .map(el -> el.attr("root"))
                    .filter(root -> root != null && !root.isEmpty())
                    .collect(Collectors.toList());

            String schemaVersion = docT.select("typeid").get(0).attr("extension");

            out = new ExtractedInfoDTO(templateIdRoots, schemaVersion, SystemTypeEnum.of(system));
        } catch (Exception ex) {
            log.error("Error while extracting info for schematron ", ex);
            throw new BusinessException("Error while extracting info for schematron ", ex);
        }
        return out;
    }

}
