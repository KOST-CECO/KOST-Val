<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"
                xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
                xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:qdt="urn:un:unece:uncefact:data:standard:QualifiedDataType:10"
                xmlns:a="urn:un:unece:uncefact:data:standard:QualifiedDataType:100"
                >
<xsl:output method="html"
            encoding="UTF-8"
            omit-xml-declaration="yes"
            doctype-public="-//W3C//DTD HTML 4.01//EN"
            media-type="text/html"
            indent="no"/>
<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="/">
	<html>
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
      <style>
				body{
					font-family: Verdana;
					font-size: small;
					display:block;
				}
				table{
					border: 1px solid #040;
					border-collapse: collapse;
				}
				th{
					text-align:left;
					border: 1px solid #040;
					border-collapse: collapse;
				}
				td{
					text-align:left;
					border: 1px solid #040;
					border-collapse: collapse;
				}
				td.numeric{
					text-align:right;
				}
			</style>
			<title>
			</title>
		</head>
		<body>
			<div class="seller_trade_party">
				<xsl:apply-templates select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty"/>
			</div>
      <br />
			<div class="buyer_trade_party">
				<xsl:apply-templates select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty"/>
			</div>
      <br />
			<div class="invoice_header">
				<xsl:apply-templates select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction" mode="invoice_header"/>
			</div>
      <br />
			<div class="invoice_data">
				<xsl:apply-templates select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction" mode="invoice_data"/>
			</div>
      <br />
			<div class="payment_terms">
				<xsl:apply-templates select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction" mode="payment_terms"/>
			</div>
      <br />
			<div class="payment_info">
				<xsl:apply-templates select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction" mode="payment_info"/>
			</div>
		</body>
	</html>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty">
	<table>
		<tr>
			<th>Rechnungsersteller</th>
		</tr>
		<tr>
			<td>
				<xsl:value-of select="ram:Name"/>
			</td>
		</tr>
		<tr>
			<td>
				<xsl:value-of select="ram:PostalTradeAddress/ram:LineOne"/>
			</td>
		</tr>
		<tr>
			<td>
				<xsl:value-of select="ram:PostalTradeAddress/ram:PostcodeCode"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="ram:PostalTradeAddress/ram:CityName"/>
			</td>
		</tr>
		<tr>
			<td>
				<xsl:value-of select="ram:PostalTradeAddress/ram:CountryID"/>
			</td>
		</tr>
	</table>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:template match="rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty">
	<table>
		<tr>
			<th>Rechnungsempfänger</th>
		</tr>
		<tr>
			<td>
				<xsl:value-of select="ram:Name"/>
			</td>
		</tr>
		<tr>
			<td>
				<xsl:value-of select="ram:DefinedTradeContact/ram:PersonName"/>
			</td>
		</tr>
		<tr>
			<td>
				<xsl:value-of select="ram:PostalTradeAddress/ram:LineOne"/>
			</td>
		</tr>
		<tr>
			<td>
				<xsl:value-of select="ram:PostalTradeAddress/ram:PostcodeCode"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="ram:PostalTradeAddress/ram:CityName"/>
			</td>
		</tr>
		<tr>
			<td>
				<xsl:value-of select="ram:PostalTradeAddress/ram:CountryID"/>
			</td>
		</tr>
	</table>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->

<xsl:template match="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction" mode="invoice_header">
	<table>
		<tr>
			<th colspan="2">Rechnung</th>
		</tr>
		<tr>
			<td>Rechnungsnummer</td><td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:ExchangedDocument/ram:ID"/></td>
		</tr>
		<tr>
			<td>Rechnungsdatum</td><td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:ExchangedDocument/ram:IssueDateTime"/></td>
		</tr>
		<tr>
			<td>Leistungsdatum</td><td><xsl:value-of select="ram:ApplicableHeaderTradeDelivery/ram:ActualDeliverySupplyChainEvent/ram:OccurrenceDateTime"/></td>
		</tr>
		<tr>
			<td>Referenz (bitte bei Zahlung angeben)</td><td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:PaymentReference"/></td>
		</tr>
		<tr>
			<td>Kundennummer</td><td><xsl:value-of select="ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:ID"/></td>
		</tr>
		<tr>
			<td>Beträge in</td><td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:InvoiceCurrencyCode"/></td>
		</tr>
		<tr>
			<td>Hinweis</td><td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:ExchangedDocument/ram:IncludedNote/ram:Content"/></td>
		</tr>
	</table>
</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->

<xsl:template match="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction" mode="invoice_data">
	<table>
		<tr>
			<th>Pos</th><th>Art.Nr</th><th>Artikelbeschreibung</th><th>Menge</th><th>Einheit</th><th>Einheitspreis</th><th>Betrag</th><th>USt.%</th>
		</tr>
		<xsl:for-each select="ram:IncludedSupplyChainTradeLineItem">
			<tr>
				<td class="numeric"><xsl:number/></td>
				<td><xsl:value-of select="ram:SpecifiedTradeProduct/ram:SellerAssignedID"/></td>
				<td><xsl:value-of select="ram:SpecifiedTradeProduct/ram:Name"/><br/><xsl:text>GTIN: </xsl:text><xsl:value-of select="ram:SpecifiedTradeProduct/ram:GlobalID"/></td>
				<td class="numeric"><xsl:value-of select="ram:SpecifiedLineTradeDelivery/ram:BilledQuantity"/></td>
				<td>Stk.</td>
				<td class="numeric"><xsl:value-of select="ram:SpecifiedLineTradeAgreement/ram:NetPriceProductTradePrice/ram:ChargeAmount"/></td>
				<td class="numeric"><xsl:value-of select="ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeSettlementLineMonetarySummation/ram:LineTotalAmount"/></td>
				<td class="numeric"><xsl:value-of select="ram:SpecifiedLineTradeSettlement/ram:ApplicableTradeTax/ram:RateApplicablePercent"/></td>
			</tr>
		</xsl:for-each>

		<tr>
			<td></td>
			<td colspan="5">Rechnungssumme Netto (excl. USt.)</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:TaxBasisTotalAmount"/></td>
			<td></td>
		</tr>

		<xsl:for-each select="ram:ApplicableHeaderTradeSettlement/ram:ApplicableTradeTax">
			<tr>
				<td></td>
				<td colspan="4">Steuerbasisbetrag USt. <xsl:value-of select="ram:ApplicablePercent"/>%</td>
				<td class="numeric"><xsl:value-of select="ram:BasisAmount"/></td>
				<td class="numeric"><xsl:value-of select="ram:CalculatedAmount"/></td>
				<td></td>
			</tr>
		</xsl:for-each>

		<tr>
			<td></td>
			<td colspan="5">Rechnungssumme Brutto (inkl. USt.)</td>
			<td class="numeric"><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:GrandTotalAmount"/></td>
			<td></td>
		</tr>
	</table>
</xsl:template>


<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->

<xsl:template match="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction" mode="payment_terms">
	<table>
		<tr>
			<th colspan="2">Zahlungsbedingungen</th>
		</tr>
		<tr>
			<td>Zahlungsziel</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:DueDateDateTime"/></td>
		</tr>
		<tr>
			<td>Zahlungsziel (Skonto <xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:ApplicableTradePaymentDiscountTerms/ram:CalculationPercent"/>%)</td>
			<td>
        <xsl:choose>
          <xsl:when test="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:ApplicableTradePaymentDiscountTerms/ram:BasisPeriodMeasure != ''">
            <xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:ApplicableTradePaymentDiscountTerms/ram:BasisPeriodMeasure"/> Tage
          </xsl:when>
          <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </td>
		</tr>
		<tr>
			<td>Zahlungsinformationen</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:Description"/></td>
		</tr>
	</table>

</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->

<xsl:template match="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction" mode="payment_info">
	<table>
		<tr><th colspan="2">Bank- und Steuerinformatinen</th></tr>
		<tr>
			<td>Kontonr.</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans/ram:PayeePartyCreditorFinancialAccount/ram:ProprietaryID"/></td>
		</tr>
		<tr>
			<td>IBAN-Nr.</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans/ram:PayeePartyCreditorFinancialAccount/ram:IBANID"/></td>
		</tr>
		<tr>
			<td>BLZ</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans/ram:PayeeSpecifiedCreditorFinancialInstitution/ram:GermanBankleitzahlID"/></td>
		</tr>
		<tr>
			<td>BIC</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans/ram:PayeeSpecifiedCreditorFinancialInstitution/ram:BICID"/></td>
		</tr>
		<tr>
			<td>Bankname</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans/ram:PayeeSpecifiedCreditorFinancialInstitution/ram:Name"/></td>
		</tr>
		<tr>
			<td>USt.-Identnr.</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:SpecifiedTaxRegistration/ram:ID[@schemeID='VA']"/></td>
		</tr>
		<tr>
			<td>Steuernr.</td>
			<td><xsl:value-of select="ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:SpecifiedTaxRegistration/ram:ID[@schemeID='FC']"/></td>
		</tr>
	</table>

</xsl:template>

<!-- ++++++++++++++++++++++++++++++++++++++++++++++ -->

</xsl:stylesheet>
