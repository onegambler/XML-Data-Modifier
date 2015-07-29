package com.kobaltmusic.dataremover.input;

import com.kobaltmusic.dataremover.core.ContentReplaceRule;
import com.kobaltmusic.dataremover.core.Rule;
import com.kobaltmusic.dataremover.xml.manipulation.XPathRule;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPropertiesReaderTest {

    private final JsonPropertiesReader reader = new JsonPropertiesReader();
    private static final String PROPERTIES_PATH = "properties.json";

    @Test
    public void testRead() throws IOException, URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(PROPERTIES_PATH);
        Objects.requireNonNull(resource);
        Map<String, XPathRule> properties = reader.read(Paths.get(resource.toURI()).toString());
        assertThat(properties).hasSize(4);

        assertThat(properties.get("/dsr:SalesReportToSocietyMessage/SalesReport/SalesByCommercialModel/SalesByTerritory/ReleaseTransactions/SalesTransaction/SalesData/NumberOfConsumerSalesGross"))
                .isNotNull()
                .matches(xPathRule -> xPathRule.getRuleSet().size() == 1)
                .matches(xPathRule ->
                {
                    Rule rule = xPathRule.getRuleSet().stream().findFirst().get();
                    return rule instanceof ContentReplaceRule && rule.equals(new ContentReplaceRule("*", "0"));
                }, "Rule match");

        assertThat(properties.get("/dsr:SalesReportToSocietyMessage/SalesReport/SalesByCommercialModel/SalesByTerritory/ReleaseTransactions/SalesTransaction/SalesData/NumberOfUnitAdjustments"))
                .isNotNull()
                .matches(xPathRule -> xPathRule.getRuleSet().size() == 1)
                .matches(xPathRule ->
                {
                    Rule rule = xPathRule.getRuleSet().stream().findFirst().get();
                    return rule instanceof ContentReplaceRule && rule.equals(new ContentReplaceRule("\\d", "rep"));
                }, "Rule match");

        assertThat(properties.get("/dsr:SalesReportToSocietyMessage/SalesReport/SalesByCommercialModel/SalesByTerritory/ReleaseTransactions/SalesTransaction/SalesData/NumberOfFreeUnitsToConsumers"))
                .isNotNull()
                .matches(xPathRule -> xPathRule.getRuleSet().size() == 1)
                .matches(xPathRule ->
                {
                    Rule rule = xPathRule.getRuleSet().stream().findFirst().get();
                    return rule instanceof ContentReplaceRule && rule.equals(new ContentReplaceRule("\\s", "newString"));
                }, "Rule match");

        assertThat(properties.get("/dsr:SalesReportToSocietyMessage/SalesReport/SalesByCommercialModel/SalesByTerritory/ReleaseTransactions/SalesTransaction/SalesData/PriceConsumerPaidExcSalesTax"))
                .isNotNull()
                .matches(xPathRule -> xPathRule.getRuleSet().size() == 1)
                .matches(xPathRule ->
                {
                    Rule rule = xPathRule.getRuleSet().stream().findFirst().get();
                    return rule instanceof ContentReplaceRule && rule.equals(new ContentReplaceRule("*", "0"));
                }, "Rule match");
    }

}