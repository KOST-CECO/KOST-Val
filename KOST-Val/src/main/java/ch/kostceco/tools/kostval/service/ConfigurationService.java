/*== KOST-Val ==================================================================================
The KOST-Val application is used for validate TIFF, SIARD, and PDF/A-Files. 
Copyright (C) 2012-2013 Claire Röthlisberger (KOST-CECO), Christian Eugster, Olivier Debenath, 
Peter Schneider (Staatsarchiv Aargau)
-----------------------------------------------------------------------------------------------
KOST-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
This application is free software: you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version. 
This application is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the follow GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
Boston, MA 02110-1301 USA or see <http://www.gnu.org/licenses/>.
==============================================================================================*/

package ch.kostceco.tools.kostval.service;

/**
 * Service Interface für die Konfigurationsdatei.
 * 
 * @author Rc Claire Röthlisberger, KOST-CECO
 */
public interface ConfigurationService extends Service
{

	/**
	 * Gibt die Komprimierung aus, welche im TIFF vorkommen dürfen.
	 * 
	 * @return Name der Komprimierung, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedCompression1();

	String getAllowedCompression2();

	String getAllowedCompression3();

	String getAllowedCompression4();

	String getAllowedCompression5();

	String getAllowedCompression7();

	String getAllowedCompression8();

	String getAllowedCompression32773();

	/**
	 * Gibt die Farbraum aus, welche im TIFF vorkommen dürfen.
	 * 
	 * @return Name der Farbraum, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedPhotointer0();

	String getAllowedPhotointer1();

	String getAllowedPhotointer2();

	String getAllowedPhotointer3();

	String getAllowedPhotointer4();

	String getAllowedPhotointer5();

	String getAllowedPhotointer6();

	String getAllowedPhotointer8();

	/**
	 * Gibt die BitsPerSample aus, welche im TIFF vorkommen dürfen.
	 * 
	 * @return Name der BitsPerSample, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedBitspersample1();

	String getAllowedBitspersample2();

	String getAllowedBitspersample4();

	String getAllowedBitspersample8();

	String getAllowedBitspersample16();

	String getAllowedBitspersample32();

	String getAllowedBitspersample64();

	/**
	 * Gibt an ob Multipage im TIFF vorkommen dürfen.
	 * 
	 * @return Name der Multipage, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedMultipage();

	/**
	 * Gibt an ob Tiles im TIFF vorkommen dürfen.
	 * 
	 * @return Name der Tiles, welche im TIFF vorkommen dürfen.
	 */
	String getAllowedTiles();

	/**
	 * Gibt an ob Giga-TIFF vorkommen dürfen.
	 * 
	 * @return Wert 0/1
	 */
	String getAllowedSize();

	/**
	 * Pfad zu Jhove Configuration
	 */
	String getPathToJhoveConfiguration();

	/**
	 * Gibt den Pfad des Arbeitsverzeichnisses zurück. Dieses Verzeichnis wird
	 * z.B. zum Entpacken des .zip-Files verwendet.
	 * 
	 * @return Pfad des Arbeitsverzeichnisses
	 */
	String getPathToWorkDir();

	/**
	 * Gibt die Grenze der Anzahl Zeilen pro Tabelle zurück. Grenze der zu
	 * validierenden XML-Tabelle im Modul H. Diese Grenze wird nur verwendet,
	 * sollte die dazugehörende XSD-Datei die genaue Anzahl Datenzeilen der
	 * XML-Tabelle enthalten. Sind mehr Datenzeilen in der Tabelle enthal-ten
	 * als in der Konfigurationsdatei eingegrenzt, wird diese einzelnen Tabelle
	 * nicht vali-diert, damit einen entsprechenden Out-of-Memory-Fehler
	 * verhindert werden kann und die restlichen Tabellen und Module validiert
	 * werden können.
	 */
	int getTableRowsLimit();

	/**
	 * Gibt an welche Konformität mindestens erreicht werden muss 1a oder 1b
	 * oder no
	 */
	String pdfa1();

	/**
	 * Gibt an welche Konformität mindestens erreicht werden muss 2a oder 2b
	 * oder 2u oder no
	 */
	String pdfa2();

	/**
	 * Gibt den Pfad zum Pdftron Exe zurück.
	 * 
	 * @return Pfad zum Pdftron Exe
	 */
	String getPathToPdftronExe();

}
