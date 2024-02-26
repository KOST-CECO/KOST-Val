﻿/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate Files and Submission Information Package (SIP).
 * Copyright (C) Claire Roethlisberger (KOST-CECO), Christian Eugster, Olivier Debenath,
 * Peter Schneider (Staatsarchiv Aargau), Markus Hahn (coderslagoon), Daniel Ludin (BEDAG AG)
 * -----------------------------------------------------------------------------------------------
 * KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. This application
 * is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. BEDAG AG and Daniel Ludin hereby disclaims all copyright
 * interest in the program SIP-Val v0.2.0 written by Daniel Ludin (BEDAG AG). Switzerland, 1 March
 * 2011. This application is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the follow GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA or see
 * <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.kostval.validation.modulepdfa;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import ch.kostceco.tools.kostval.exception.modulepdfa.ValidationApdfavalidationException;
import ch.kostceco.tools.kostval.validation.ValidationModule;

/**
 * Ist die vorliegende PDF-Datei eine valide PDFA-Datei? PDFA Validierungs mit
 * PDFTron und oder PDF-Tools.
 * 
 * Folgendes ist Konfigurierbar: Hauptvalidator sowie ob eine duale Validierung
 * durchgefuehrt werden soll oder nicht. Bei der dualen Validierung muessen
 * beide Validatoren die Datei als invalide betrachten, damit diese als invalid
 * gilt. Bei Uneinigkeit gilt diese als valid.
 * 
 * Es wird falls vorhanden die Vollversion von PDF-Tools verwendet. KOST-Val
 * muss nicht angepasst werden und verwendet automatisch den internen
 * Schluessel, sollte keine Vollversion existieren.
 * 
 * Zuerste erfolgt eine Erkennung, wenn diese io kommt die Validierung mit
 * PDFTron und oder PDF-Tools. Die Fehler werden den Einzelnen Gruppen (Modulen)
 * zugeordnet
 * 
 * @author Rc Claire Roethlisberger, KOST-CECO
 */

public interface ValidationAvalidationAiModule extends ValidationModule
{

	public boolean validate( File valDatei, File directoryOfLogfile,
			Map<String, String> configMap, Locale locale, File logFile,
			String dirOfJarPath ) throws ValidationApdfavalidationException;

}
