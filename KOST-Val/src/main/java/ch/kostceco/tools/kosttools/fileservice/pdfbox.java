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

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class pdfbox {

	/**
	 * Extrahiert den Text einer PDF-Datei und gibt das Ergebnis als String zurueck
	 * 
	 * @param pdfFile PDF-Datei, welche ausgelesen werden soll
	 * @return String mit Text aus dem PDF
	 */
	public static String getTextPdfbox(File pdfFile) throws InterruptedException {
		String resultExec = "NoTextInPdf";

		try {
			File file = pdfFile;
			PDDocument pdDoc = Loader.loadPDF(file);
			PDFTextStripper pdfStripper = new PDFTextStripper();
			resultExec = pdfStripper.getText(pdDoc);
			resultExec = resultExec.replaceAll("\n", "__");

			// System.out.println( resultExec );
		} catch (IOException e) {
			e.printStackTrace();
		}

		return resultExec;
	}
}
