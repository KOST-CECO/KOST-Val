/* == KOST-Tools ================================================================================
 * KOST-Tools. Copyright (C) KOST-CECO.
 * -----------------------------------------------------------------------------------------------
 * KOST-Tools is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. BEDAG AG and Daniel Ludin hereby disclaims all
 * copyright interest in the program SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland,
 * 1 March 2011. This application is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the follow GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or see
 * <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kosttools.fileservice;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import org.verapdf.core.VeraPDFException;
import org.verapdf.features.FeatureExtractorConfig;
import org.verapdf.features.FeatureFactory;
import org.verapdf.features.FeatureObjectType;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.metadata.fixer.FixerFactory;
import org.verapdf.metadata.fixer.MetadataFixerConfig;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.validators.ValidatorConfig;
import org.verapdf.pdfa.validation.validators.ValidatorConfigBuilder;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.BatchProcessor;
import org.verapdf.processor.FormatOption;
import org.verapdf.processor.ProcessorConfig;
import org.verapdf.processor.ProcessorFactory;
import org.verapdf.processor.TaskType;
import org.verapdf.processor.plugins.PluginsCollectionConfig;

import ch.kostceco.tools.kosttools.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class verapdf {

	/**
	 * TODO: Validiert mit VeraPDF die Datei valDatei und gibt aus, ob die
	 * Validierung bestanden ist oder nicht.
	 * 
	 * valid bestande
	 * 
	 * invalid nicht bestanden
	 * 
	 * Error Fehler beim ausfuehren
	 * 
	 * Der Report wird geschrieben
	 * 
	 * @return Boolean Validierung bestanden oder nicht
	 */
	@SuppressWarnings("unchecked")
	public static String execVerapdfVal(File valDatei, File workDir, String dirOfJarPath, String level,
			File verapdfReportFile) throws InterruptedException {
		/*
		 * Aktualisieren von verapdf =========================
		 * 
		 * herunterladen von verapdf-gf-installer.zip auf der Seite
		 * https://software.verapdf.org/dev/
		 * 
		 * Installieren. Danach die Datei greenfield-apps-1.27.59.jar vom bin Ordner des
		 * Installationsverzeichnises in die lib kopieren
		 */

		// System.out.println(" initialise VeraGreenfieldFoundryProvider ");
		VeraGreenfieldFoundryProvider.initialise();

		String verapdfReport;
		Boolean isValidverapdf = false;
		String returnVerapdf = "";

		// Verwende Standard Config jedoch mit vorgegebenem Level
		PDFAFlavour flavour = PDFAFlavour.fromString(level.toLowerCase());
		ValidatorConfig validatorConfig = new ValidatorConfigBuilder().flavour(flavour).build();

		// Verwende Standard features config
		FeatureExtractorConfig featureConfig = FeatureFactory.defaultConfig();

		// Verwende Standard plugins config
		PluginsCollectionConfig pluginsConfig = PluginsCollectionConfig.defaultConfig();

		// Verwende Standard fixer config
		MetadataFixerConfig fixerConfig = FixerFactory.defaultConfig();

		// Tasks configuring
		@SuppressWarnings("rawtypes")
		EnumSet tasks = EnumSet.noneOf(TaskType.class);
		tasks.add(TaskType.VALIDATE);
		File valDateiNorm = new File(workDir.getAbsolutePath() + File.separator + "veraPDF.pdf");
		if (valDateiNorm.exists()) {
			Util.deleteFile(valDateiNorm);
		}
		try {
			Util.copyFile(valDatei, valDateiNorm);
			if (valDateiNorm.exists()) {
				// ok
			} else {
				Util.copyFile(valDatei, valDateiNorm);
			}
		} catch (IOException e) {
			isValidverapdf = false;
			returnVerapdf = returnVerapdf + "</Message><Message> - Exception copyFile: " + e.getMessage();
		}
		// Erstellen von processor config
		ProcessorConfig processorConfig = ProcessorFactory.fromValues(validatorConfig, featureConfig, pluginsConfig,
				fixerConfig, tasks);

		// Erstellen von processor und output stream.
		ByteArrayOutputStream reportStream = new ByteArrayOutputStream();
		try (BatchProcessor processor = ProcessorFactory.fileBatchProcessor(processorConfig)) {
			// in die zu validierende Liste mit Dateien,
			// welche validiert werden sollen, wird nur valDatei eingetragen
			List<File> files = new ArrayList<>();
			files.add(valDateiNorm);

			/*
			 * INFO: Der processor gibt in seltenen Faellen eine
			 * 
			 * WARNUNG: Exception caught when validating item
			 * 
			 * org.verapdf.core.ValidationException: Caught unexpected runtime exception
			 * during validation
			 * 
			 * aus, welche von meiner Seite aus nicht abgefangen werden kann (catch; Console
			 * ausschalten / umleiten)
			 * 
			 * --> Hinweis auf Konsole wenn invalid aber keine Fehlermeldung im Log
			 */

			processor.process(files, ProcessorFactory.getHandler(FormatOption.XML, false, reportStream,
					processorConfig.getValidatorConfig().isRecordPasses()));
			// System.out.println(System.currentTimeMillis() + " - End processor");

			// stream als utf8 in die Datei schreiben (true = append = hinzufuegen)
			verapdfReport = reportStream.toString("utf-8");
			String filename = verapdfReportFile.getAbsolutePath();
			FileWriter fw = new FileWriter(filename, true);
			fw.write(verapdfReport);
			fw.close();

			String veraPDFvalid = "<validationReports compliant=\"1\" ";
			if (Util.stringInFile(valDateiNorm.getAbsolutePath(), verapdfReportFile)) {
				// System.out.println(" verapdf wurde korrekt durchgefuehrt");
				if (Util.stringInFile(veraPDFvalid, verapdfReportFile)) {
					isValidverapdf = true;
				} else {
					isValidverapdf = false;
				}
			} else {
				isValidverapdf = false;
			}
		} catch (VeraPDFException e) {
			isValidverapdf = false;
			returnVerapdf = returnVerapdf + "</Message><Message> - VeraPDF Exception: " + e.getMessage();
		} catch (IOException excep) {
			isValidverapdf = false;
			returnVerapdf = returnVerapdf + "</Message><Message> - VeraPDF IOException: " + excep.getMessage();
		} catch (Exception ex) {
			isValidverapdf = false;
			returnVerapdf = returnVerapdf + "</Message><Message> - VeraPDF other Exception: " + ex.getMessage();
		}

		if (returnVerapdf.equals("")) {
			if (isValidverapdf) {
				returnVerapdf = "valid";
			} else {
				returnVerapdf = "invalid";
			}
		}
		Util.deleteFile(valDateiNorm);
		return returnVerapdf;
	}

	/**
	 * TODO: Extrahiert mit VeraPDF die Metadaten zu den einzelnen Signaturen in
	 * PDF-Dateien und gibt aus, ob die es extrahiert werden konnte oder nicht.
	 * 
	 * OK konnte extrahiert werdenbestande
	 * 
	 * Error Fehler beim ausfuehren
	 * 
	 * Der Report wird geschrieben
	 * 
	 * @return Boolean Validierung bestanden oder nicht
	 */
	@SuppressWarnings("unchecked")
	public static String execVerapdfSig(File valDatei, File workDir, File signatureTmp, Locale locale)
			throws InterruptedException {

		// System.out.println(" initialise VeraGreenfieldFoundryProvider ");
		VeraGreenfieldFoundryProvider.initialise();

		String verapdfSigMetadata = "";

		// Verwende Standard Config
		ValidatorConfig validatorConfig = ValidatorFactory.defaultConfig();

		// Verwende Standard features config und fuege Signature hinzu
		FeatureExtractorConfig featureConfig = FeatureFactory.defaultConfig();
		featureConfig.getEnabledFeatures().add(FeatureObjectType.SIGNATURE);

		// Verwende Standard plugins config
		PluginsCollectionConfig pluginsConfig = PluginsCollectionConfig.defaultConfig();

		// Verwende Standard fixer config
		MetadataFixerConfig fixerConfig = FixerFactory.defaultConfig();

		// Tasks configuring
		@SuppressWarnings("rawtypes")
		EnumSet tasks = EnumSet.noneOf(TaskType.class);
		tasks.add(TaskType.EXTRACT_FEATURES);
		File valDateiNorm = new File(workDir.getAbsolutePath() + File.separator + "veraPDF.pdf");
		if (valDateiNorm.exists()) {
			Util.deleteFile(valDateiNorm);
		}
		try {
			Util.copyFile(valDatei, valDateiNorm);
			if (valDateiNorm.exists()) {
				// ok
			} else {
				Util.copyFile(valDatei, valDateiNorm);
			}
		} catch (IOException e) {
			verapdfSigMetadata = verapdfSigMetadata + "</Message><Message> - Exception copyFile: " + e.getMessage();
		}
		// Erstellen von processor config
		ProcessorConfig processorConfig = ProcessorFactory.fromValues(validatorConfig, featureConfig, pluginsConfig,
				fixerConfig, tasks);

		// Erstellen von processor und output stream.
		ByteArrayOutputStream reportStream = new ByteArrayOutputStream();
		try (BatchProcessor processor = ProcessorFactory.fileBatchProcessor(processorConfig)) {
			// in die zu validierende Liste mit Dateien,
			// welche validiert werden sollen, wird nur valDatei eingetragen
			List<File> files = new ArrayList<>();
			files.add(valDateiNorm);

			/*
			 * INFO: Der processor gibt in seltenen Faellen eine
			 * 
			 * WARNUNG: Exception caught when validating item
			 * 
			 * org.verapdf.core.ValidationException: Caught unexpected runtime exception
			 * during validation
			 * 
			 * aus, welche von meiner Seite aus nicht abgefangen werden kann (catch; Console
			 * ausschalten / umleiten)
			 * 
			 * --> Hinweis auf Konsole wenn invalid aber keine Fehlermeldung im Log
			 */

			processor.process(files, ProcessorFactory.getHandler(FormatOption.XML, false, reportStream,
					processorConfig.getValidatorConfig().isRecordPasses()));
			// System.out.println(System.currentTimeMillis() + " - End processor");

			// stream als utf8 in die Datei schreiben (true = append = hinzufuegen)
			verapdfSigMetadata = reportStream.toString("utf-8");
			String filename = signatureTmp.getAbsolutePath();
			FileWriter fw = new FileWriter(filename, true);
			fw.write(verapdfSigMetadata);
			fw.close();

			Integer countSig = 0;
			// Start unnoetige Zeile zu loeschen
			BufferedReader reader = new BufferedReader(new FileReader(signatureTmp));
			String lineModif = "";
			StringBuilder sb = new StringBuilder();
			while ((lineModif = reader.readLine()) != null) {
				// System.out.println(lineModif);

				/*
				 * <signature> <filter>Adobe.PPKLite</filter>
				 * <subFilter>ETSI.CAdES.detached</subFilter> <contents>bla</contents>
				 * <name>Muster</name> <signDate>2023-03-01T11:13:13.000Z</signDate>
				 * <reason>Grund</reason> <contactInfo>Adobe.PPKLite</contactInfo> </signature>
				 * <signature> <filter>Adobe.PPKLite</filter>
				 * <subFilter>adbe.pkcs7.detached</subFilter> <contents>bla</contents>
				 * <signDate>2023-03-08T08:14:54.000+01:00</signDate> <location></location>
				 * <reason></reason> <contactInfo>Adobe.PPKLite</contactInfo> </signature>
				 */
				if (lineModif.contains("veraPDF.pdf</name>")) {
					// line mit veraPDF.pdf</name> wird NICHT behalten
				} else if (lineModif.contains("<signature>")) {
					// Signaturzaehler erhoehen
					countSig++;
					// line mit <signature> wird behalten
					sb.append("<Message></Message><Message>Metadaten der Signatur " + countSig + " [verapdf]</Message>");
					sb.append("\r\n");
				} else if (lineModif.contains("<name>")) {
					// line mit <name> wird behalten
					sb.append(lineModif);
					sb.append("\r\n");
				} else if (lineModif.contains("<signDate>")) {
					// line mit <signDate> wird behalten
					sb.append(lineModif);
					sb.append("\r\n");
				} else if (lineModif.contains("<location>")) {
					// line mit <location> wird behalten
					sb.append(lineModif);
					sb.append("\r\n");
				} else if (lineModif.contains("<reason>")) {
					// line mit <reason> wird behalten
					sb.append(lineModif);
					sb.append("\r\n");
				} else {
					// alle anderen Zeilen loeschen
				}
			}
			// <Message>Metadaten der Signatur 1 [verapdf]</Message>
			// <Message> - Zeitpunkt der Unterschrift (Anbringen Signatur): 01.03.2023 11:
			// 13: ?? UTC</Message>
			// <Message> - Name: </Message>
			// <Message> - Ort: </Message>
			// <Message> - Grund: </Message>
			verapdfSigMetadata = sb.toString();

			// log erstellen und uebersetzten
			if (locale.toString().contains("fr")) {
				verapdfSigMetadata = verapdfSigMetadata.replace("Metadaten der Signatur","Metadonnees de la signature");
				verapdfSigMetadata = verapdfSigMetadata.replace("<signDate>",
						"<Message> - Date de la signature (apposition de la signature) : ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</signDate>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<name>", "<Message> - Nom : ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</name>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<location>", "<Message> - Lieu : ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</location>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<reason>", "<Message> - Raison : ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</reason>", "</Message>");
			} else if (locale.toString().contains("it")) {
				verapdfSigMetadata = verapdfSigMetadata.replace("Metadaten der Signatur","Metadati della firma");
				verapdfSigMetadata = verapdfSigMetadata.replace("<signDate>",
						"<Message> - Data della firma (apposizione della firma): ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</signDate>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<name>", "<Message> - Nome: ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</name>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<location>", "<Message> - Luogo: ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</location>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<reason>", "<Message> - Motivo: ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</reason>", "</Message>");
			} else if (locale.toString().contains("en")) {
				verapdfSigMetadata = verapdfSigMetadata.replace("Metadaten der Signatur","Metadata of the signature");
				verapdfSigMetadata = verapdfSigMetadata.replace("<signDate>",
						"<Message> - Date of signature (affixing signature): ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</signDate>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<name>", "<Message> - Name: ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</name>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<location>", "<Message> - Location: ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</location>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<reason>", "<Message> - Reason: ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</reason>", "</Message>");
			} else {
				// de als Standard
				verapdfSigMetadata = verapdfSigMetadata.replace("<signDate>",
						"<Message> - Zeitpunkt der Unterschrift (Anbringen Signatur): ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</signDate>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<name>", "<Message> - Name: ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</name>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<location>", "<Message> - Ort: ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</location>", "</Message>");
				verapdfSigMetadata = verapdfSigMetadata.replace("<reason>", "<Message> - Grund: ");
				verapdfSigMetadata = verapdfSigMetadata.replace("</reason>", "</Message>");
			}
			reader.close();
			// set to null
			reader = null;
			FileWriter writer = new FileWriter(signatureTmp);
			writer.write(verapdfSigMetadata);
			writer.close();

		} catch (VeraPDFException e) {
			verapdfSigMetadata = verapdfSigMetadata + "</Message><Message> - VeraPDF Exception: " + e.getMessage();
		} catch (IOException excep) {
			verapdfSigMetadata = verapdfSigMetadata + "</Message><Message> - VeraPDF IOException: "
					+ excep.getMessage();
		} catch (Exception ex) {
			verapdfSigMetadata = verapdfSigMetadata + "</Message><Message> - VeraPDF other Exception: "
					+ ex.getMessage();
		}
		// System.out.println(" Sig Metadata: " + verapdfSigMetadata);

		Util.deleteFile(valDateiNorm);
		Util.deleteFile(signatureTmp);
		return verapdfSigMetadata;
	}
}
