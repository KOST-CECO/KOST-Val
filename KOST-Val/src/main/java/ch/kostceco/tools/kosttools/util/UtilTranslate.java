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

package ch.kostceco.tools.kosttools.util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class UtilTranslate {

	/** Englisch String ins Deutsche aendern */
	public static String enTOde(String en) {
		String de = en;
		// TODO: de = de.replace("en", "de");
		de = de.replace("A circular mapping shall not exist", "Es darf keine kreisfoermige Zuordnung geben.");
		de = de.replace(
				"A CMap shall not reference any other CMap except those listed in ISO 32000-1:2008, 9.7.5.2, Table 118",
				"Eine CMap darf nicht auf eine andere CMap verweisen, ausser auf die in ISO 32000-1:2008, 9.7.5.2, Tabelle 118 aufgefuehrten.");
		de = de.replace("A conforming file shall not contain a CID value greater than 65535",
				"Eine konforme Datei darf keinen CID-Wert ueber 65535 enthalten.");
		de = de.replace("A conforming file shall not contain a DeviceN colour space with more than 32 colourants",
				"Eine konforme Datei darf keinen DeviceN-Farbraum mit mehr als 32 Farben enthalten.");
		de = de.replace("A conforming file shall not contain any integer greater than 2147483647.",
				"Eine konforme Datei darf keine Ganzzahl groesser als 2147483647 enthalten.");
		de = de.replace("A conforming file shall not contain any integer less than -2147483648",
				"Eine konforme Datei darf keine Ganzzahl kleiner als -2147483648 enthalten.");
		de = de.replace("A conforming file shall not contain any name longer than 127 bytes",
				"Eine konforme Datei darf keinen Namen enthalten, der laenger als 127 Byte ist.");
		de = de.replace("A conforming file shall not contain any PostScript XObjects",
				"Eine konforme Datei darf keine PostScript XObjects enthalten.");
		de = de.replace("A conforming file shall not contain any real number closer to zero than +/-1.175 x 10^(-38)",
				"Eine konforme Datei darf keine reelle Zahl enthalten, die naeher an Null liegt als +/-1,175 x 10^(-38).");
		de = de.replace("A conforming file shall not contain any real number outside the range of +/-3.403 x 10^38",
				"Eine konforme Datei darf keine reelle Zahl enthalten, die ausserhalb des Bereichs von +/-3,403 x 10^38 liegt.");
		de = de.replace("A conforming file shall not contain any reference XObjects",
				"Eine konforme Datei darf keine XObjects-Referenzen enthalten.");
		de = de.replace("A conforming file shall not contain any string longer than 32767 bytes",
				"Eine konforme Datei darf keine Zeichenkette enthalten, die laenger als 32767 Byte ist.");
		de = de.replace("A conforming file shall not contain more than 8388607 indirect objects",
				"Eine konforme Datei darf nicht mehr als 8388607 indirekte Objekte enthalten.");
		de = de.replace("A conforming file shall not nest q/Q pairs by more than 28 nesting levels",
				"Eine konforme Datei darf q/Q-Paare nicht mehr als 28 Verschachtelungsebenen tief verschachteln.");
		de = de.replace(
				"A conforming reader shall use only that colour space and shall ignore all other colour space specifications",
				"Ein konformer Reader darf nur diesen Farbraum verwenden und muss alle anderen Farbraumspezifikationen ignorieren.");
		de = de.replace(
				"A content stream shall not contain any operators not defined in PDF Reference even if such operators are bracketed by the BX/EX compatibility operators",
				"Ein Content-Stream darf keine in der PDF-Referenz nicht definierten Operatoren enthalten, selbst wenn solche Operatoren von den BX/EX-Kompatibilitaetsoperatoren eingeklammert werden.");
		de = de.replace(
				"A content stream that references other objects, such as images and fonts that are necessary to fully render or process the stream, shall have an explicitly associated Resources dictionary as described in ISO 32000-1:2008, 7.8.3",
				"Ein Content-Stream, der auf andere Objekte verweist, wie Bilder und Schriftarten, die fuer die vollstaendige Darstellung oder Verarbeitung des Streams erforderlich sind, muss ueber ein explizit zugeordnetes Ressourcenverzeichnis verfuegen, wie in ISO 32000-1:2008, 7.8.3, beschrieben.");
		de = de.replace("A documents Catalog shall not contain the NeedsRendering key",
				"Der Katalog eines Dokuments darf den Schluessel NeedsRendering nicht enthalten.");
		de = de.replace("A Field dictionary shall not contain the A or AA keys",
				"Ein Feldverzeichnis darf die Schluessel A oder AA nicht enthalten.");
		de = de.replace("A Field dictionary shall not include an AA entry for an additional-actions dictionary",
				"Ein Feldverzeichnis darf keinen AA-Eintrag fuer ein Zusatzaktionsverzeichnis enthalten.");
		de = de.replace(
				"A file specification dictionary, as defined in ISO 32000-1:2008, 7.11.3, may contain the EF key, provided that the embedded file is compliant with either ISO 19005-1 or this part of ISO 19005",
				"Ein Dateispezifikationswoerterbuch gemaess ISO 32000-1:2008, 7.11.3, kann den Schluessel EF enthalten, vorausgesetzt, die eingebettete Datei entspricht entweder ISO 19005-1 oder diesem Teil von ISO 19005.");
		de = de.replace("A file specification dictionary, as defined in PDF 3.10.2, shall not contain the EF key",
				"Ein Dateispezifikationswoerterbuch gemaess PDF 3.10.2 darf den Schluessel EF nicht enthalten.");
		de = de.replace(
				"A files name dictionary, as defined in PDF Reference 3.6.3, shall not contain the EmbeddedFiles key",
				"Das Dateinamen-Verzeichnis einer Datei, wie in PDF-Referenz 3.6.3 definiert, darf nicht den Schluessel EmbeddedFiles enthalten.");
		de = de.replace(
				"A font referenced for use solely in rendering mode 3 is therefore not rendered and is thus exempt from the embedding requirement.",
				"Eine Schriftart, die ausschliesslich fuer die Verwendung im Rendering-Modus 3 referenziert wird, wird daher nicht gerendert und ist somit von der Einbettungsanforderung ausgenommen.");
		de = de.replace(
				"A form XObject dictionary shall not contain any of the following: - the OPI key; - the Subtype2 key with a value of PS; - the PS key",
				"Ein Formular-XObject-Verzeichnis darf keines der folgenden Elemente enthalten: - den Schluessel OPI; - den Schluessel Subtype2 mit dem Wert PS; - den Schluessel PS.");
		de = de.replace("A form XObject dictionary shall not contain the Subtype2 key with a value of PS or the PS key",
				"Ein Formular-XObject-Verzeichnis darf den Subtype2-Schluessel mit dem Wert PS oder den PS-Schluessel nicht enthalten.");
		de = de.replace(
				"A Group object with an S key with a value of Transparency shall not be included in a form XObject.",
				"Ein Gruppenobjekt mit einem S-Schluessel mit dem Wert Transparenz darf nicht in einem Formular-XObject enthalten sein.");
		de = de.replace(
				"A Group object with an S key with a value of Transparency shall not be included in a page dictionary",
				"Ein Gruppenobjekt mit einem S-Schluessel mit dem Wert Transparenz darf nicht in einem Seitenverzeichnis enthalten sein.");
		de = de.replace(
				"A hexadecimal string is written as a sequence of hexadecimal digits (0–9 and either A–F or a–f)",
				"Eine hexadezimale Zeichenfolge wird als eine Folge von hexadezimalen Ziffern (0–9 und entweder A–F oder a–f) geschrieben.");
		de = de.replace(
				"A language identifier shall either be the empty text string, to indicate that the language is unknown, or a Language-Tag as defined in RFC 3066, Tags for the Identification of Languages",
				"Ein Sprachidentifikator muss entweder die leere Textzeichenfolge sein, um anzuzeigen, dass die Sprache unbekannt ist, oder ein Sprach-Tag, wie in RFC 3066, Tags zur Identifizierung von Sprachen, definiert.");
		de = de.replace("A Level A conforming file shall specify the value of pdfaid:conformance as A",
				"Eine Datei, die mit Level A konform ist, muss den Wert von pdfaid:conformance als A angeben.");
		de = de.replace("A Level A conforming file shall specify the value of pdfaid:conformance as A.",
				"Eine Datei, die mit Level A konform ist, muss den Wert von pdfaid:conformance als A angeben.");
		de = de.replace("A Level B conforming file shall specify the value of pdfaid:conformance as B",
				"Eine Datei, die mit Level B konform ist, muss den Wert von pdfaid:conformance als B angeben.");
		de = de.replace("A Level B conforming file shall specify the value of pdfaid:conformance as B.",
				"Eine Datei, die mit Level B konform ist, muss den Wert von pdfaid:conformance als B angeben.");
		de = de.replace("A Level U conforming file shall specify the value of pdfaid:conformance as U",
				"Eine mit Level U konforme Datei muss den Wert von pdfaid:conformance als U angeben.");
		de = de.replace(
				"A PDF/A-1 OutputIntent is an OutputIntent dictionary, as defined by PDF Reference 9.10.4, that is included in the files OutputIntents array and has GTS_PDFA1 as the value of its S key and a valid ICC profile stream as the value its DestOutputProfile key",
				"Ein PDF/A-1 OutputIntent ist ein OutputIntent-Verzeichnis, wie in PDF Reference 9.10.4 definiert, das im OutputIntents-Array der Datei enthalten ist und GTS_PDFA1 als Wert seines S-Schluessels und einen gueltigen ICC-Profil-Stream als Wert seines DestOutputProfile-Schluessels hat.");
		de = de.replace(
				"A PDF/A-2 compliant document shall not contain a reference to the .notdef glyph from any of the text showing operators, regardless of text rendering mode, in any content stream",
				"Ein PDF/A-2-konformes Dokument darf in keinem Text, der Operatoren anzeigt, unabhaengig vom Textwiedergabemodus, in einem Content-Stream einen Verweis auf die Glyphe .notdef enthalten.");
		de = de.replace("A stream dictionary shall not contain the F, FFilter, or FDecodeParms keys",
				"Ein stream dictionary darf nicht die Schluessel F, FFilter oder FDecodeParms enthalten.");
		de = de.replace("A stream object dictionary shall not contain the F, FFilter, or FDecodeParms keys",
				"Ein stream object dictionary darf nicht die Schluessel F, FFilter oder FDecodeParms enthalten.");
		de = de.replace("A Widget annotation dictionary shall not contain the A or AA keys",
				"Ein Widget annotation dictionary darf nicht die Schluessel A oder AA enthalten.");
		de = de.replace(
				"A Widget annotation dictionary shall not include an AA entry for an additional-actions dictionary",
				"Ein Widget-Annotation-Verzeichnis darf keinen AA-Eintrag fuer ein Additional-Actions-Verzeichnis enthalten.");
		de = de.replace("Absolute real value must be less than or equal to 32767.0",
				"Der absolute Realwert muss kleiner oder gleich 32767,0 sein.");
		de = de.replace("Additionally, the 3D, Sound, Screen and Movie types shall not be permitted",
				"Ausserdem sind die Typen 3D, Sound, Screen und Movie nicht zulaessig.");
		de = de.replace("Additionally, the deprecated set-state and no-op actions shall not be permitted",
				"Ausserdem sind die veralteten Set-State- und No-Op-Aktionen nicht zulaessig.");
		de = de.replace("Additionally, the deprecated set-state and no-op actions shall not be permitted.",
				"Zusaetzlich sind die veralteten Aktionen set-state und no-op nicht zulaessig.");
		de = de.replace("Additionally, the FileAttachment, Sound and Movie types shall not be permitted",
				"Zusaetzlich sind die Typen FileAttachment, Sound und Movie nicht zulaessig.");
		de = de.replace(
				"All CMaps used within a conforming file, except Identity-H and Identity-V, shall be embedded in that file as described in PDF Reference 5.6.4",
				"Alle in einer konformen Datei verwendeten CMaps, mit Ausnahme von Identity-H und Identity-V, muessen gemaess der Beschreibung in der PDF-Referenz 5.6.4 in diese Datei eingebettet werden.");
		de = de.replace(
				"All CMaps used within a PDF/A-2 file, except those listed in ISO 32000-1:2008, 9.7.5.2, Table 118, shall be embedded in that file as described in ISO 32000-1:2008, 9.7.5",
				"Alle CMaps, die in einer PDF/A-2-Datei verwendet werden, mit Ausnahme der in ISO 32000-1:2008, 9.7.5.2, Tabelle 118 aufgefuehrten, muessen in diese Datei eingebettet werden, wie in ISO 32000-1:2008, 9.7.5 beschrieben.");
		de = de.replace("All colour channels in the JPEG2000 data shall have the same bit-depth",
				"Alle Farbkanaele in den JPEG2000-Daten muessen dieselbe Bittiefe aufweisen.");
		de = de.replace(
				"All content of all XMP packets shall be well-formed, as defined by Extensible Markup Language (XML) 1.0 (Third Edition), 2.1, and the RDF/XML Syntax Specification (Revised)",
				"Der gesamte Inhalt aller XMP-Pakete muss wohlgeformt sein, wie in Extensible Markup Language (XML) 1.0 (Dritte Ausgabe), 2.1 und der RDF/XML-Syntaxspezifikation (ueberarbeitet) definiert.");
		de = de.replace(
				"All fields described in each of the tables in 6.6.2.3.3 shall be present in any extension schema container schema",
				"Alle in den einzelnen Tabellen in 6.6.2.3.3 beschriebenen Felder muessen in jedem Erweiterungsschema-Containerschema vorhanden sein.");
		de = de.replace(
				"All fonts and font programs used in a conforming file, regardless of rendering mode usage, shall conform to the provisions in ISO 32000-1:2008, 9.6 and 9.7, as well as to the font specifications referenced by these provisions.",
				"Alle in einer konformen Datei verwendeten Schriftarten und Schriftartprogramme muessen unabhaengig vom verwendeten Rendering-Modus den Bestimmungen in ISO 32000-1:2008, 9.6 und 9.7 sowie den in diesen Bestimmungen genannten Schriftartenspezifikationen entsprechen.");
		de = de.replace(
				"All fonts used in a conforming file shall conform to the font specifications defined in PDF Reference 5.5.",
				"Alle in einer konformen Datei verwendeten Schriftarten muessen den in PDF Reference 5.5 definierten Schriftartenspezifikationen entsprechen.");
		de = de.replace(
				"All halftones in a conforming PDF/A-2 file shall have the value 1 or 5 for the HalftoneType key",
				"Alle Halbtoene in einer konformen PDF/A-2-Datei muessen den Wert 1 oder 5 fuer den Schluessel HalftoneType aufweisen.");
		de = de.replace(
				"All ICCBased colour spaces shall be embedded as ICC profile streams as described in PDF Reference 4.5",
				"Alle ICC-basierten Farbraeume muessen als ICC-Profil-Streams eingebettet werden, wie in der PDF-Referenz 4.5 beschrieben.");
		de = de.replace(
				"All ICCBased colour spaces shall be embedded as ICC profile streams as described in PDF Reference 4.5.",
				"Alle ICC-basierten Farbraeume muessen als ICC-Profil-Streams eingebettet werden, wie in der PDF-Referenz 4.5 beschrieben.");
		de = de.replace("All metadata streams present in the PDF shall conform to the XMP Specification.",
				"Alle im PDF vorhandenen Metadaten-Streams muessen der XMP-Spezifikation entsprechen.");
		de = de.replace(
				"All non-standard structure types shall be mapped to the nearest functionally equivalent standard type, as defined in ISO 32000-1:2008, 14.8.4, in the role map dictionary of the structure tree root",
				"Alle nicht standardmaessigen Strukturtypen muessen dem naechstgelegenen funktional aequivalenten Standardtyp zugeordnet werden, wie in ISO 32000-1:2008, 14.8.4, im Role Map Dictionary der Structure Tree Root definiert.");
		de = de.replace(
				"All non-standard structure types shall be mapped to the nearest functionally equivalent standard type, as defined in PDF Reference 9.7.4, in the role map dictionary of the structure tree root",
				"Alle nicht standardmaessigen Strukturtypen muessen dem naechstgelegenen funktional aequivalenten Standardtyp zugeordnet werden, wie in PDF Reference 9.7.4, im Role Map Dictionary der Structure Tree Root definiert.");
		de = de.replace(
				"All non-symbolic TrueType fonts shall have either MacRomanEncoding or WinAnsiEncoding as the value for the Encoding key in the Font dictionary or as the value for the BaseEncoding key in the dictionary that is the value of the Encoding key in the Font dictionary.",
				"Alle nicht-symbolischen TrueType-Schriftarten muessen entweder MacRomanEncoding oder WinAnsiEncoding als Wert fuer den Encoding-Schluessel im Font-Verzeichnis oder als Wert fuer den BaseEncoding-Schluessel im Verzeichnis, das den Wert des Encoding-Schluessels im Font-Verzeichnis darstellt, haben.");
		de = de.replace(
				"All non-symbolic TrueType fonts shall specify MacRomanEncoding or WinAnsiEncoding, either as the value of the Encoding entry in the font dictionary or as the value of the BaseEncoding entry in the dictionary that is the value of the Encoding entry in the font dictionary.",
				"Alle nicht-symbolischen TrueType-Schriftarten muessen MacRomanEncoding oder WinAnsiEncoding angeben, entweder als Wert des Eintrags Encoding im Schriftartenverzeichnis oder als Wert des Eintrags BaseEncoding im Verzeichnis, das den Wert des Eintrags Encoding im Schriftartenverzeichnis darstellt.");
		de = de.replace(
				"All non-white-space characters in hexadecimal strings shall be in the range 0 to 9, A to F or a to f",
				"Alle Nicht-Leerzeichen-Zeichen in Hexadezimalzeichenfolgen muessen im Bereich von 0 bis 9, A bis F oder a bis f liegen.");
		de = de.replace(
				"All properties specified in XMP form shall use either the predefined schemas defined in the XMP Specification, ISO 19005-1 or this part of ISO 19005, or any extension schemas that comply with 6.6.2.3.2",
				"Alle in XMP-Form angegebenen Eigenschaften muessen entweder die in der XMP-Spezifikation, ISO 19005-1 oder diesem Teil von ISO 19005 definierten vordefinierten Schemas oder beliebige Erweiterungsschemas verwenden, die mit 6.6.2.3.2 uebereinstimmen.");
		de = de.replace(
				"All Separation arrays within a single PDF/A-2 file (including those in Colorants dictionaries) that have the same name shall have the same tintTransform and alternateSpace.",
				"Alle Separations-Arrays innerhalb einer einzelnen PDF/A-2-Datei (einschliesslich derjenigen in Farbwoerterbuechern) mit demselben Namen muessen denselben tintTransform und alternateSpace aufweisen.");
		de = de.replace(
				"All standard stream filters listed in ISO 32000-1:2008, 7.4, Table 6 may be used, with the exception of LZWDecode.",
				"Alle in ISO 32000-1:2008, 7.4, Tabelle 6 aufgefuehrten Standard-Stream-Filter koennen verwendet werden, mit Ausnahme von LZWDecode.");
		de = de.replace("All symbolic TrueType fonts shall not specify an Encoding entry in the font dictionary",
				"Alle symbolischen TrueType-Schriftarten duerfen keinen Encoding-Eintrag im Schriftartenverzeichnis angeben.");
		de = de.replace("An annotation dictionary shall contain the F key.",
				"Ein Anmerkungswoerterbuch muss den F-Schluessel enthalten.");
		de = de.replace(
				"An annotation dictionary shall not contain the C array or the IC array unless the colour space of the DestOutputProfile in the PDF/A-1 OutputIntent dictionary, defined in 6.2.2, is RGB",
				"Ein Anmerkungswoerterbuch darf das C-Array oder das IC-Array nur enthalten, wenn der Farbraum des DestOutputProfile im PDF/A-1 OutputIntent-Woerterbuch, definiert in 6.2.2, RGB ist.");
		de = de.replace("An annotation dictionary shall not contain the CA key with a value other than 1.0",
				"Ein Anmerkungswoerterbuch darf den CA-Schluessel nur mit dem Wert 1.0 enthalten.");
		de = de.replace("An ExtGState dictionary shall not contain the HTP key",
				"Ein ExtGState-Woerterbuch darf den HTP-Schluessel nicht enthalten.");
		de = de.replace("An ExtGState dictionary shall not contain the TR key",
				"Ein ExtGState-Woerterbuch darf den TR-Schluessel nicht enthalten.");
		de = de.replace("An ExtGState dictionary shall not contain the TR2 key with a value other than Default",
				"Ein ExtGState-Verzeichnis darf den Schluessel TR2 nicht mit einem anderen Wert als Default enthalten.");
		de = de.replace("An Image dictionary shall not contain the Alternates key",
				"Ein Image-Verzeichnis darf den Schluessel Alternates nicht enthalten.");
		de = de.replace("An Image dictionary shall not contain the OPI key",
				"Ein Image-Verzeichnis darf den Schluessel OPI nicht enthalten.");
		de = de.replace("An XObject dictionary (Image or Form) shall not contain the OPI key",
				"Ein XObject-Verzeichnis (Image oder Form) darf den Schluessel OPI nicht enthalten.");
		de = de.replace("An XObject dictionary shall not contain the SMask key",
				"Ein XObject-Verzeichnis darf den Schluessel SMask nicht enthalten.");
		de = de.replace("Annotation types not defined in ISO 32000-1 shall not be permitted.",
				"Annotationstypen, die nicht in ISO 32000-1 definiert sind, sind nicht zulaessig.");
		de = de.replace("Annotation types not defined in PDF Reference shall not be permitted.",
				"Annotationstypen, die nicht in der PDF-Referenz definiert sind, sind nicht zulaessig.");
		de = de.replace("As of PDF 1.4, N must be 1, 3, or 4", "Ab PDF 1.4 muss N 1, 3 oder 4 sein.");
		de = de.replace("At minimum, it shall include the signers X.509 signing certificate",
				"Sie muss mindestens das X.509-Signaturzertifikat des Unterzeichners enthalten.");
		de = de.replace(
				"At minimum, there shall only be a single signer (e.g. a single SignerInfo structure) in the PDF Signature",
				"In der PDF-Signatur darf es nur einen einzigen Unterzeichner geben (z. B. eine einzige SignerInfo-Struktur).");
		de = de.replace("BaseFont - name - (Required) The PostScript name of the font",
				"BaseFont - Name - (Erforderlich) Der PostScript-Name der Schriftart.");
		de = de.replace("Compression and whether or not an object is direct or indirect shall be ignored",
				"Komprimierung und die Angabe, ob ein Objekt direkt oder indirekt ist, werden ignoriert.");
		de = de.replace(
				"Content streams shall not contain any operators not defined in ISO 32000-1 even if such operators are bracketed by the BX/EX compatibility operators",
				"Content Streams duerfen keine in ISO 32000-1 nicht definierten Operatoren enthalten, selbst wenn solche Operatoren von den BX/EX-Kompatibilitaetsoperatoren eingeklammert werden.");
		de = de.replace("dc:creator shall contain exactly one entry", "dc:creator muss genau einen Eintrag enthalten.");
		de = de.replace(
				"DeviceCMYK may be used only if the file has a PDF/A-1 OutputIntent that uses a CMYK colour space",
				"DeviceCMYK darf nur verwendet werden, wenn die Datei ueber einen PDF/A-1 OutputIntent verfuegt, der einen CMYK-Farbraum verwendet.");
		de = de.replace(
				"DeviceCMYK shall only be used if a device independent DefaultCMYK colour space has been set or if a DeviceN-based DefaultCMYK colour space has been set when the DeviceCMYK colour space is used or the file has a PDF/A OutputIntent that contains a CMYK destination profile",
				"DeviceCMYK darf nur verwendet werden, wenn ein geraeteunabhaengiger Standard-CMYK-Farbraum festgelegt wurde oder wenn ein DeviceN-basierter Standard-CMYK-Farbraum festgelegt wurde, wenn der DeviceCMYK-Farbraum verwendet wird, oder wenn die Datei einen PDF/A-OutputIntent mit einem CMYK-Zielprofil enthaelt.");
		de = de.replace(
				"DeviceGray shall only be used if a device independent DefaultGray colour space has been set when the DeviceGray colour space is used, or if a PDF/A OutputIntent is present",
				"DeviceGray darf nur verwendet werden, wenn bei Verwendung des DeviceGray-Farbraums ein geraeteunabhaengiger Standard-Gray-Farbraum festgelegt wurde oder wenn ein PDF/A-OutputIntent vorhanden ist.");
		de = de.replace(
				"DeviceRGB may be used only if the file has a PDF/A-1 OutputIntent that uses an RGB colour space",
				"DeviceRGB darf nur verwendet werden, wenn die Datei einen PDF/A-1-OutputIntent hat, der einen RGB-Farbraum verwendet.");
		de = de.replace(
				"DeviceRGB shall only be used if a device independent DefaultRGB colour space has been set when the DeviceRGB colour space is used, or if the file has a PDF/A OutputIntent that contains an RGB destination profile",
				"DeviceRGB darf nur verwendet werden, wenn bei Verwendung des DeviceRGB-Farbraums ein geraeteunabhaengiger DefaultRGB-Farbraum festgelegt wurde oder wenn die Datei einen PDF/A-OutputIntent mit einem RGB-Zielprofil enthaelt.");
		de = de.replace(
				"Each optional content configuration dictionary shall contain the Name key, whose value shall be unique amongst all optional content configuration dictionaries within the PDF/A-2 file",
				"Jedes optionale Inhaltskonfigurationsverzeichnis muss den Schluessel name enthalten, dessen Wert innerhalb aller optionalen Inhaltskonfigurationsverzeichnisse innerhalb der PDF/A-2-Datei eindeutig sein muss.");
		de = de.replace(
				"Each optional content configuration dictionary that forms the value of the D key, or that is an element in the array that forms the value of the Configs key in the OCProperties dictionary, shall contain the Name key",
				"Jedes optionale Inhaltskonfigurationsverzeichnis, das den Wert des Schluessels D bildet oder ein Element in dem Array ist, das den Wert des Schluessels Configs im OCProperties-Verzeichnis bildet, muss den Schluessel name enthalten.");
		de = de.replace(
				"Embedded font programs shall define all font glyphs referenced for rendering with conforming file",
				"Eingebettete Schriftprogramme muessen alle Schriftzeichen definieren, auf die fuer die Darstellung mit konformen Dateien verwiesen wird.");
		de = de.replace("Embedded fonts shall define all glyphs referenced for rendering within the conforming file.",
				"Eingebettete Schriftarten muessen alle Glyphen definieren, die fuer die Darstellung in der konformen Datei referenziert werden.");
		de = de.replace(
				"Every annotation (including those whose Subtype value is Widget, as used for form fields), except for the two cases listed below, shall have at least one appearance dictionary: - annotations where the value of the Rect key consists of an array where value 1 is equal to value 3 and value 2 is equal to value 4; - annotations whose Subtype value is Popup or Link",
				"Jede Anmerkung (einschliesslich derjenigen, deren Subtype-Wert Widget ist, wie er fuer Formularfelder verwendet wird), mit Ausnahme der beiden unten aufgefuehrten Faelle, muss mindestens ein Appearance Dictionary haben: - Anmerkungen, bei denen der Wert des Schluessels Rect aus einem Array besteht, bei dem Wert 1 gleich Wert 3 und Wert 2 gleich Wert 4 ist; - Anmerkungen, deren Subtype-Wert Popup oder Link ist.");
		de = de.replace("Every form field shall have an appearance dictionary associated with the fields data",
				"Jedes Formularfeld muss ueber ein mit den Daten des Feldes verknuepftes Erscheinungswoerterbuch verfuegen.");
		de = de.replace(
				"Except for annotation dictionaries whose Subtype value is Popup, all annotation dictionaries shall contain the F key",
				"Mit Ausnahme von Anmerkungswoerterbuechern, deren Subtype-Wert Popup ist, muessen alle Anmerkungswoerterbuecher den F-Schluessel enthalten.");
		de = de.replace(
				"Extension schema descriptions shall be specified using the PDF/A extension schema description schema defined in this clause",
				"Erweiterungsschema-Beschreibungen muessen unter Verwendung des in diesem Abschnitt definierten PDF/A-Erweiterungsschema-Beschreibungschemas angegeben werden.");
		de = de.replace(
				"Extension schemas shall be specified using the PDF/A extension schema container schema defined in 6.6.2.3.3.",
				"Erweiterungsschemas muessen unter Verwendung des in 6.6.2.3.3 definierten PDF/A-Erweiterungsschema-Containerschemas angegeben werden.");
		de = de.replace(
				"Field category of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text, value external or internal and namespace prefix pdfaProperty",
				"Das Feld category des PDF/A-Eigenschaftswerttyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ Text, den Wert external oder internal und das Namensraumpraefix pdfaProperty haben.");
		de = de.replace(
				"Field description of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField",
				"Das Feld description des PDF/A-Feldwerttyps im PDF/A-Erweiterungsschema muss den Typ Text und das Namensraumpraefix pdfaField haben.");
		de = de.replace(
				"Field description of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaProperty",
				"Das Feld description des PDF/A-Eigenschaftswerttyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ Text und das Namensraumpraefix pdfaProperty haben.");
		de = de.replace(
				"Field description of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaType",
				"Das Feld description des PDF/A-Wertetyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ Text und das Namensraumpraefix pdfaType haben.");
		de = de.replace(
				"Field field of the PDF/A ValueType value type in the PDF/A extension schema shall have type Seq Field and namespace prefix pdfaType",
				"Das Feld field des PDF/A-ValueType-Werttyps im PDF/A-Erweiterungsschema muss den Typ Seq Field und das Namensraumpraefix pdfaType haben.");
		de = de.replace(
				"Field name of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField",
				"Das Feld name des PDF/A-Field-Werttyps im PDF/A-Erweiterungsschema muss den Typ Text und das Namensraumpraefix pdfaField haben.");
		de = de.replace(
				"Field name of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaProperty",
				"Das Feld name des PDF/A-Eigenschaftswerttyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ Text und das Namensraumpraefix pdfaProperty haben.");
		de = de.replace(
				"Field namespaceURI of the PDF/A Schema value type in the PDF/A extension schema shall be present and shall have type URI and namespace prefix pdfaSchema",
				"Das Feld namespaceURI des PDF/A-Schemawerttyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ URI und das Namensraumpraefix pdfaSchema haben.");
		de = de.replace(
				"Field namespaceURI of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type URI and namespace prefix pdfaType",
				"Das Feld namespaceURI des PDF/A-Wertetyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ URI und das Namensraumpraefix pdfaType haben.");
		de = de.replace(
				"Field prefix of the PDF/A Schema value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaSchema",
				"Das Feld prefix des PDF/A-Schemawerttyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ Text und das Namensraumpraefix pdfaSchema haben.");
		de = de.replace(
				"Field prefix of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaType",
				"Das Feld prefix des PDF/A-ValueType-Werttyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ Text und das Namensraumpraefix pdfaType aufweisen.");
		de = de.replace(
				"Field property of the PDF/A Schema value type in the PDF/A extension schema shall have type Seq Property and namespace prefix pdfaSchema",
				"Das Feld property des PDF/A-Schema-Werttyps im PDF/A-Erweiterungsschema muss den Typ Seq Property und das Namensraumpraefix pdfaSchema aufweisen.");
		de = de.replace(
				"Field schema of the PDF/A Schema value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaSchema",
				"Das Feld schema des PDF/A-Schema-Werttyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ Text und das Namensraumpraefix pdfaSchema haben.");
		de = de.replace(
				"Field type of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaType",
				"Das Feld type des PDF/A-ValueType-Werttyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ Text und das Namensraumpraefix pdfaType haben.");
		de = de.replace(
				"Field valueType of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField.",
				"Das Feld valueType des PDF/A-Feldwerttyps im PDF/A-Erweiterungsschema muss den Typ Text und das Namensraumpraefix pdfaField aufweisen.");
		de = de.replace(
				"Field valueType of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaProperty.",
				"Das Feld valueType des PDF/A-Eigenschaftswerttyps im PDF/A-Erweiterungsschema muss vorhanden sein und den Typ Text und das Namensraumpraefix pdfaProperty aufweisen.");
		de = de.replace(
				"Field valueType of the PDF/A Schema value type in the PDF/A extension schema shall have type Seq ValueType and namespace prefix pdfaSchema",
				"Das Feld valueType des PDF/A-Schema-Werttyps im PDF/A-Erweiterungsschema muss den Typ Seq ValueType und das Namensraumpraefix pdfaSchema haben.");
		de = de.replace("Filters that are not listed in ISO 32000-1:2008, 7.4, Table 6 shall not be used",
				"Filter, die nicht in ISO 32000-1:2008, 7.4, Tabelle 6 aufgefuehrt sind, duerfen nicht verwendet werden.");
		de = de.replace(
				"FirstChar - integer - (Required except for the standard 14 fonts) The first character code defined in the fonts Widths array",
				"FirstChar - integer - (Erforderlich, ausser fuer die 14 Standardschriftarten) Der erste Zeichencode, der im Breiten-Array der Schriftart definiert ist.");
		de = de.replace(
				"Font names, names of colourants in Separation and DeviceN colour spaces, and structure type names, after expansion of character sequences escaped with a NUMBER SIGN (23h), if any, shall be valid UTF-8 character sequences",
				"Schriftartnamen, Namen von Farbstoffen in Separations- und DeviceN-Farbraeumen und Strukturtypenamen muessen nach der Erweiterung von Zeichenfolgen, die mit einem NUMMERNSIGN (23h) maskiert sind, gueltige UTF-8-Zeichenfolgen sein.");
		de = de.replace("Font programs cmap tables for all symbolic TrueType fonts shall contain exactly one encoding",
				"Die cmap-Tabellen von Schriftprogrammen fuer alle symbolischen TrueType-Schriftarten muessen genau eine Codierung enthalten.");
		de = de.replace(
				"For all annotation dictionaries containing an AP key, the appearance dictionary that it defines as its value shall contain only the N key",
				"Fuer alle Anmerkungswoerterbuecher, die einen AP-Schluessel enthalten, soll das Erscheinungsbildwoerterbuch, das es als seinen Wert definiert, nur den N-Schluessel enthalten.");
		de = de.replace(
				"For all CIDFont subsets referenced within a conforming file, the font descriptor dictionary shall include a CIDSet stream identifying which CIDs are present in the embedded CIDFont file, as described in PDF Reference Table 5.20",
				"Fuer alle CIDFont-Teilmengen, auf die in einer konformen Datei verwiesen wird, soll das Schriftbeschreibungswoerterbuch einen CIDSet-Datenstrom enthalten, der angibt, welche CIDs in der eingebetteten CIDFont-Datei vorhanden sind, wie in PDF-Referenztabelle 5.20 beschrieben.");
		de = de.replace(
				"For all non-symbolic TrueType fonts used for rendering, the embedded TrueType font program shall contain one or several non-symbolic cmap entries such that all necessary glyph lookups can be carried out",
				"Fuer alle nicht-symbolischen TrueType-Schriften, die fuer die Darstellung verwendet werden, muss das eingebettete TrueType-Schriftprogramm einen oder mehrere nicht-symbolische cmap-Eintraege enthalten, damit alle erforderlichen Glyphen-Suchen durchgefuehrt werden koennen.");
		de = de.replace(
				"For all Type 1 font subsets referenced within a conforming file, the font descriptor dictionary shall include a CharSet string listing the character names defined in the font subset, as described in PDF Reference Table 5.18",
				"Fuer alle Type-1-Schriftuntergruppen, auf die in einer konformen Datei verwiesen wird, muss das Schriftbeschreibungswoerterbuch eine CharSet-Zeichenfolge enthalten, in der die in der Schriftuntergruppe definierten Zeichennamen aufgefuehrt sind, wie in PDF-Referenztabelle 5.18 beschrieben.");
		de = de.replace(
				"For all Type 2 CIDFonts that are used for rendering, the CIDFont dictionary shall contain a CIDToGIDMap entry that shall be a stream mapping from CIDs to glyph indices or the name Identity, as described in PDF Reference Table 5.13",
				"Fuer alle Type-2-CIDFonts, die fuer die Darstellung verwendet werden, muss das CIDFont-Verzeichnis einen CIDToGIDMap-Eintrag enthalten, der eine Stream-Zuordnung von CIDs zu Glyphenindizes oder dem Namen Identity ist, wie in PDF-Referenztabelle 5.13 beschrieben.");
		de = de.replace("For an inline image, the I key shall have a value of false",
				"Fuer ein Inline-Bild muss der Schluessel I den Wert false haben.");
		de = de.replace(
				"For any character, regardless of its rendering mode, that is mapped to a code or codes in the Unicode Private Use Area (PUA), an ActualText entry as described in ISO 32000-1:2008, 14.9.4 shall be present for this character or a sequence of characters of which such a character is a part",
				"Fuer jedes Zeichen, das unabhaengig von seinem Rendering-Modus einem oder mehreren Codes im Unicode Private Use Area (PUA) zugeordnet ist, muss ein ActualText-Eintrag gemaess ISO 32000-1:2008, 14.9.4 fuer dieses Zeichen oder eine Zeichenfolge, zu der ein solches Zeichen gehoert, vorhanden sein.");
		de = de.replace(
				"For any given composite (Type 0) font referenced within a conforming file, the CIDSystemInfo entries of its CIDFont and CMap dictionaries shall be compatible.",
				"Fuer jeden zusammengesetzten (Typ 0) Font, auf den in einer konformen Datei verwiesen wird, muessen die CIDSystemInfo-Eintraege seiner CIDFont- und CMap-Woerterbuecher kompatibel sein.");
		de = de.replace(
				"For any given composite (Type 0) font within a conforming file, the CIDSystemInfo entry in its CIDFont dictionary and its Encoding dictionary shall have the following relationship: - If the Encoding key in the Type 0 font dictionary is Identity-H or Identity-V, any values of Registry, Ordering, and Supplement may be used in the CIDSystemInfo entry of the CIDFont.",
				"Fuer jeden beliebigen zusammengesetzten (Typ 0) Font in einer konformen Datei muss der CIDSystemInfo-Eintrag in seinem CIDFont-Verzeichnis und seinem Encoding-Verzeichnis die folgende Beziehung aufweisen: - Wenn der Encoding-Schluessel im Typ 0 Font-Verzeichnis Identity-H oder Identity-V ist, koennen alle Werte von Registry, Ordering und Supplement im CIDSystemInfo-Eintrag des CIDFont verwendet werden.");
		de = de.replace(
				"For any spot colour used in a DeviceN or NChannel colour space, an entry in the Colorants dictionary shall be present",
				"Fuer jede in einem DeviceN- oder NChannel-Farbraum verwendete Schmuckfarbe muss ein Eintrag im Colorants-Verzeichnis vorhanden sein.");
		de = de.replace(
				"For every font embedded in a conforming file and used for rendering, the glyph width information in the font dictionary and in the embedded font program shall be consistent",
				"Fuer jede in eine konforme Datei eingebettete und fuer die Darstellung verwendete Schriftart muessen die Informationen zur Glyphenbreite im Schriftartenverzeichnis und im eingebetteten Schriftartenprogramm uebereinstimmen.");
		de = de.replace(
				"For those CMaps that are embedded, the integer value of the WMode entry in the CMap dictionary shall be identical to the WMode value in the embedded CMap stream",
				"Fuer eingebettete CMaps muss der Ganzzahlwert des WMode-Eintrags im CMap-Verzeichnis mit dem WMode-Wert im eingebetteten CMap-Stream identisch sein.");
		de = de.replace("Halftones in a conforming PDF/A-2 file shall not contain a HalftoneName key",
				"Halbtoene in einer konformen PDF/A-2-Datei duerfen keinen HalftoneName-Schluessel enthalten.");
		de = de.replace("Hexadecimal strings shall contain an even number of non-white-space characters",
				"Hexadezimale Zeichenfolgen muessen eine gerade Anzahl von Nicht-Leerzeichen enthalten.");
		de = de.replace(
				"If a files OutputIntents array contains more than one entry, as might be the case where a file is compliant with this part of ISO 19005 and at the same time with PDF/X-4 or PDF/E-1, then all entries that contain a DestOutputProfile key shall have as the value of that key the same indirect object, which shall be a valid ICC profile stream",
				"Wenn das OutputIntents-Array einer Datei mehr als einen Eintrag enthaelt, wie es der Fall sein koennte, wenn eine Datei mit diesem Teil von ISO 19005 und gleichzeitig mit PDF/X-4 oder PDF/E-1 konform ist, dann muessen alle Eintraege, die einen DestOutputProfile-Schluessel enthalten, als Wert dieses Schluessels dasselbe indirekte Objekt haben, das ein gueltiger ICC-Profil-Stream sein muss.");
		de = de.replace(
				"If a files OutputIntents array contains more than one entry, then all entries that contain a DestOutputProfile key shall have as the value of that key the same indirect object, which shall be a valid ICC profile stream",
				"Wenn das OutputIntents-Array einer Datei mehr als einen Eintrag enthaelt, muessen alle Eintraege, die einen DestOutputProfile-Schluessel enthalten, als Wert dieses Schluessels dasselbe indirekte Objekt aufweisen, das ein gueltiger ICC-Profil-Stream sein muss.");
		de = de.replace("If a ca key is present in an ExtGState object, its value shall be 1.0",
				"Wenn ein ca-Schluessel in einem ExtGState-Objekt vorhanden ist, muss sein Wert 1.0 sein.");
		de = de.replace(
				"If an annotation dictionarys Subtype key has a value of Widget and its FT key has a value of Btn, the value of the N key shall be an appearance subdictionary",
				"Wenn der Subtype-Schluessel eines Anmerkungswoerterbuchs den Wert Widget und der FT-Schluessel den Wert Btn aufweist, muss der Wert des N-Schluessels ein Unterwoerterbuch fuer das Erscheinungsbild sein.");
		de = de.replace(
				"If an annotation dictionarys Subtype key has value other than Widget, or if FT key associated with Widget annotation has value other than Btn, the value of the N key shall be an appearance stream",
				"Wenn der Subtype-Schluessel eines Anmerkungswoerterbuchs einen anderen Wert als Widget hat oder wenn der FT-Schluessel, der mit der Widget-Anmerkung verknuepft ist, einen anderen Wert als Btn hat, muss der Wert des N-Schluessels ein Appearance-Stream sein.");
		de = de.replace("If an Image dictionary contains the BitsPerComponent key, its value shall be 1, 2, 4 or 8",
				"Wenn ein Bildwoerterbuch den BitsPerComponent-Schluessel enthaelt, muss sein Wert 1, 2, 4 oder 8 sein.");
		de = de.replace("If an Image dictionary contains the BitsPerComponent key, its value shall be 1, 2, 4, 8 or 16",
				"Wenn ein Bildwoerterbuch den BitsPerComponent-Schluessel enthaelt, muss sein Wert 1, 2, 4, 8 oder 16 sein.");
		de = de.replace("If an Image dictionary contains the Interpolate key, its value shall be false",
				"Wenn ein Bildwoerterbuch den Schluessel Interpolate enthaelt, muss sein Wert false sein.");
		de = de.replace("If an Image dictionary contains the Interpolate key, its value shall be false.",
				"Wenn ein Bildwoerterbuch den Schluessel Interpolate enthaelt, muss sein Wert false sein.");
		de = de.replace(
				"If an optional content configuration dictionary contains the Order key, the array which is the value of this Order key shall contain references to all OCGs in the conforming file",
				"Wenn ein optionales Inhaltskonfigurationswoerterbuch den Schluessel Order enthaelt, muss das Array, das den Wert dieses Schluessels Order darstellt, Verweise auf alle OCGs in der konformen Datei enthalten.");
		de = de.replace("If an SMask key appears in an ExtGState dictionary, its value shall be None",
				"Wenn ein Schluessel SMask in einem ExtGState-Woerterbuch erscheint, muss sein Wert None sein.");
		de = de.replace(
				"If an uncalibrated colour space is used in a file then that file shall contain a PDF/A-1 OutputIntent, as defined in 6.2.2",
				"Wenn in einer Datei ein nicht kalibrierter Farbraum verwendet wird, muss diese Datei einen PDF/A-1 OutputIntent enthalten, wie in 6.2.2 definiert.");
		de = de.replace(
				"If DocMDP is present, then the Signature References dictionary (ISO 32000-1:2008, 12.8.1, Table 253) shall not contain the keys DigestLocation, DigestMethod, and DigestValue",
				"Wenn DocMDP vorhanden ist, darf das Signature References-Verzeichnis (ISO 32000-1:2008, 12.8.1, Tabelle 253) nicht die Schluessel DigestLocation, DigestMethod und DigestValue enthalten.");
		de = de.replace(
				"If present, the F keys Print flag bit shall be set to 1 and its Hidden, Invisible, ToggleNoView, and NoView flag bits shall be set to 0",
				"Falls vorhanden, muss das Bit Print Flag des F-Schluessels auf 1 gesetzt werden und seine Bits Hidden, Invisible, ToggleNoView und NoView Flag muessen auf 0 gesetzt werden.");
		de = de.replace(
				"If the document does not contain a PDF/A OutputIntent, then all Page objects that contain transparency shall include the Group key, and the attribute dictionary that forms the value of that Group key shall include a CS entry whose value shall be used as the default blending colour space",
				"Wenn das Dokument keinen PDF/A-OutputIntent enthaelt, muessen alle Seitenobjekte, die Transparenz enthalten, den Gruppenschluessel enthalten, und das Attributverzeichnis, das den Wert dieses Gruppenschluessels bildet, muss einen CS-Eintrag enthalten, dessen Wert als Standard-Mischfarbraum verwendet werden soll.");
		de = de.replace(
				"If the FontDescriptor dictionary of an embedded CID font contains a CIDSet stream, then it shall identify all CIDs which are present in the font program, regardless of whether a CID in the font is referenced or used by the PDF or not",
				"Wenn das FontDescriptor-Verzeichnis einer eingebetteten CID-Schriftart einen CIDSet-Stream enthaelt, muessen alle CIDs identifiziert werden, die im Schriftprogramm vorhanden sind, unabhaengig davon, ob ein CID in der Schriftart von der PDF-Datei referenziert oder verwendet wird oder nicht.");
		de = de.replace(
				"If the FontDescriptor dictionary of an embedded Type 1 font contains a CharSet string, then it shall list the character names of all glyphs present in the font program, regardless of whether a glyph in the font is referenced or used by the PDF or not",
				"Wenn das FontDescriptor-Verzeichnis einer eingebetteten Type-1-Schriftart eine CharSet-Zeichenfolge enthaelt, muss es die Namen aller Glyphen im Schriftprogramm auflisten, unabhaengig davon, ob eine Glyphe in der Schriftart von der PDF-Datei referenziert oder verwendet wird oder nicht.");
		de = de.replace(
				"If the Lang entry is present in the document catalog dictionary or in a structure element dictionary or property list, its value shall be a language identifier as defined by RFC 1766, Tags for the Identification of Languages, as described in PDF Reference 9.8.1",
				"Wenn der Lang-Eintrag im Dokumentkatalog-Woerterbuch oder in einem Strukturelement-Woerterbuch oder einer Eigenschaftsliste vorhanden ist, muss sein Wert ein Sprachidentifikator gemaess RFC 1766, Tags for the Identification of Languages, wie in PDF Reference 9.8.1 beschrieben, sein.");
		de = de.replace(
				"If the Lang entry is present in the documents Catalog dictionary or in a structure element dictionary or property list, its value shall be a language identifier as described in ISO 32000-1:2008, 14.9.2.",
				"Wenn der Lang-Eintrag im Katalogwoerterbuch des Dokuments oder in einem Strukturelementwoerterbuch oder einer Eigenschaftsliste vorhanden ist, muss sein Wert ein Sprachidentifikator sein, wie in ISO 32000-1:2008, 14.9.2, beschrieben.");
		de = de.replace(
				"If the number of colour space specifications in the JPEG2000 data is greater than 1, there shall be exactly one colour space specification that has the value 0x01 in the APPROX field",
				"Wenn die Anzahl der Farbraumspezifikationen in den JPEG2000-Daten groesser als 1 ist, muss es genau eine Farbraumspezifikation geben, die den Wert 0x01 im Feld APPROX hat.");
		de = de.replace("If the value of the Encoding entry is a dictionary, it shall not contain a Differences entry",
				"Wenn der Wert des Eintrags Encoding ein Woerterbuch ist, darf es keinen Eintrag Differences enthalten.");
		de = de.replace(
				"In a cross reference subsection header the starting object number and the range shall be separated by a single SPACE character (20h)",
				"In einer Querverweis-Unterabschnittsueberschrift muessen die Startobjektnummer und der Bereich durch ein einzelnes Leerzeichen (20h) getrennt werden.");
		de = de.replace(
				"In a linearized PDF, if the ID keyword is present in both the first page trailer dictionary and the last trailer dictionary, the value to both instances of the ID keyword shall be identical",
				"Wenn in einem linearisierten PDF das ID-Schluesselwort sowohl im ersten Seiten-Trailer-Verzeichnis als auch im letzten Trailer-Verzeichnis vorhanden ist, muss der Wert fuer beide Instanzen des ID-Schluesselworts identisch sein.");
		de = de.replace(
				"In addition, all non-symbolic TrueType fonts shall not define a Differences array unless all of the glyph names in the Differences array are listed in the Adobe Glyph List and the embedded font program contains at least the Microsoft Unicode (3,1 – Platform ID = 3, Encoding ID = 1) encoding in the cmap table",
				"Ausserdem duerfen alle nicht-symbolischen TrueType-Schriftarten kein Differences-Array definieren, es sei denn, alle Glyphnamen im Differences-Array sind in der Adobe Glyph List aufgefuehrt und das eingebettete Schriftprogramm enthaelt mindestens die Microsoft Unicode-Codierung (3,1 – Platform ID = 3, Encoding ID = 1) in der cmap-Tabelle.");
		de = de.replace(
				"In addition, the Crypt filter shall not be used unless the value of the Name key in the decode parameters dictionary is Identity.",
				"Ausserdem darf der Crypt-Filter nur verwendet werden, wenn der Wert des Name-Schluessels im Decodierungsparameter-Verzeichnis Identity ist.");
		de = de.replace(
				"In addition, the DestOutputProfileRef key, as defined in ISO 15930-7:2010, Annex A, shall not be present in any PDF/X OutputIntent",
				"Ausserdem darf der DestOutputProfileRef-Schluessel, wie in ISO 15930-7:2010, Anhang A definiert, in keinem PDF/X-OutputIntent vorhanden sein.");
		de = de.replace(
				"In all cases for TrueType fonts that are to be rendered, character codes shall be able to be mapped to glyphs according to ISO 32000-1:2008, 9.6.6.4 without the use of a non-standard mapping chosen by the conforming processor",
				"In allen Faellen muessen Zeichencodes fuer TrueType-Schriften, die gerendert werden sollen, gemaess ISO 32000-1:2008, 9.6.6.4 Glyphen zugeordnet werden koennen, ohne dass eine nicht standardmaessige Zuordnung verwendet wird, die vom konformen Prozessor ausgewaehlt wurde.");
		de = de.replace(
				"In evaluating equivalence, the PDF objects shall be compared, rather than the computational result of the use of those PDF objects.",
				"Bei der Bewertung der Aequivalenz sind die PDF-Objekte zu vergleichen und nicht das Rechenergebnis der Verwendung dieser PDF-Objekte.");
		de = de.replace(
				"In other words, the Registry and Ordering strings of the CIDSystemInfo dictionaries for that font shall be identical, unless the value of the Encoding key in the font dictionary is Identity-H or Identity-V",
				"Mit anderen Worten: Die Zeichenketten Registry und Ordering der CIDSystemInfo-Woerterbuecher fuer diese Schriftart muessen identisch sein, es sei denn, der Wert des Encoding-Schluessels im Schriftarten-Woerterbuch ist Identity-H oder Identity-V.");
		de = de.replace("Interactive form fields shall not perform actions of any type",
				"Interaktive Formularfelder duerfen keine Aktionen ausfuehren.");
		de = de.replace(
				"ISO 32000-1:2008, 9.7.4, Table 117 requires that all embedded Type 2 CIDFonts in the CIDFont dictionary shall contain a CIDToGIDMap entry that shall be a stream mapping from CIDs to glyph indices or the name Identity, as described in ISO 32000-1:2008, 9.7.4, Table 117",
				"Gemaess ISO 32000-1:2008, 9.7.4, Tabelle 117 muessen alle eingebetteten CIDFonts vom Typ 2 im CIDFont-Verzeichnis einen CIDToGIDMap-Eintrag enthalten, der eine Stream-Zuordnung von CIDs zu Glyphenindizes oder dem Namen Identity ist, wie in ISO 32000-1:2008, 9.7.4, Tabelle 117 beschrieben.");
		de = de.replace("JPEG2000 enumerated colour space 19 (CIEJab) shall not be used",
				"JPEG2000-Aufzaehlungsfarbraum 19 (CIEJab) darf nicht verwendet werden.");
		de = de.replace("Largest Integer value is 2,147,483,647.", "Der groesste Ganzzahlwert ist 2.147.483.647.");
		de = de.replace(
				"LastChar - integer - (Required except for the standard 14 fonts) The last character code defined in the fonts Widths array",
				"LastChar - Ganzzahl - (Erforderlich, ausser fuer die 14 Standardschriftarten) Der letzte Zeichencode, der im Breiten-Array der Schriftart definiert ist.");
		de = de.replace("Maximum capacity of a dictionary (in entries) is 4095",
				"Die maximale Kapazitaet eines Woerterbuchs (in Eintraegen) betraegt 4095.");
		de = de.replace("Maximum capacity of an array (in elements) is 8191",
				"Die maximale Kapazitaet eines Arrays (in Elementen) betraegt 8191.");
		de = de.replace("Maximum depth of graphics state nesting by q and Q operators is 28",
				"Die maximale Verschachtelungstiefe von Grafikstatus durch q- und Q-Operatoren betraegt 28.");
		de = de.replace("Maximum length of a name (in bytes) is 127",
				"Die maximale Laenge eines Namens (in Byte) betraegt 127.");
		de = de.replace("Maximum length of a string (in bytes) is 65535",
				"Die maximale Laenge einer Zeichenfolge (in Byte) betraegt 65535.");
		de = de.replace("Maximum number of DeviceN components is 8",
				"Die maximale Anzahl von DeviceN-Komponenten betraegt 8.");
		de = de.replace("Maximum number of indirect objects in a PDF file is 8,388,607",
				"Die maximale Anzahl indirekter Objekte in einer PDF-Datei betraegt 8.388.607.");
		de = de.replace("Maximum value of a CID (character identifier) is 65,535",
				"Der Hoechstwert eines CID (Zeichenidentifikator) betraegt 65.535.");
		de = de.replace("Metadata object stream dictionaries shall not contain the Filter key",
				"Metadaten-Objektstromwoerterbuecher duerfen den Filter-Schluessel nicht enthalten.");
		de = de.replace("Named actions other than NextPage, PrevPage, FirstPage, and LastPage shall not be permitted",
				"Andere benannte Aktionen als NextPage, PrevPage, FirstPage und LastPage sind nicht zulaessig.");
		de = de.replace(
				"No data can follow the last end-of-file marker except a single optional end-of-line marker as described in ISO 32000-1:2008, 7.5.5",
				"Auf die letzte Dateiende-Markierung duerfen keine Daten folgen, mit Ausnahme einer einzelnen optionalen Zeilenende-Markierung, wie in ISO 32000-1:2008, 7.5.5 beschrieben.");
		de = de.replace("No data shall follow the last end-of-file marker except a single optional end-of-line marker",
				"Auf die letzte Dateiende-Markierung duerfen keine Daten folgen, mit Ausnahme einer einzelnen optionalen Zeilenende-Markierung.");
		de = de.replace(
				"No keys other than UR3 and DocMDP shall be present in a permissions dictionary (ISO 32000-1:2008, 12.8.4, Table 258)",
				"In einem Berechtigungsverzeichnis duerfen keine anderen Schluessel als UR3 und DocMDP vorhanden sein (ISO 32000-1:2008, 12.8.4, Tabelle 258).");
		de = de.replace(
				"Only blend modes that are specified in ISO 32000-1:2008 shall be used for the value of the BM key in an extended graphic state dictionary",
				"Fuer den Wert des BM-Schluessels in einem erweiterten Grafikstatus-Verzeichnis duerfen nur Mischmodi verwendet werden, die in ISO 32000-1:2008 angegeben sind.");
		de = de.replace(
				"Otherwise, the corresponding Registry and Ordering strings in both CIDSystemInfo dictionaries shall be identical, and the value of the Supplement key in the CIDSystemInfo dictionary of the CIDFont shall be less than or equal to the Supplement key in the CIDSystemInfo dictionary of the CMap",
				"Andernfalls muessen die entsprechenden Registrierungs- und Bestellzeichenfolgen in beiden CIDSystemInfo-Verzeichnissen identisch sein, und der Wert des Ergaenzungsschluessels im CIDSystemInfo-Verzeichnis des CIDFont muss kleiner oder gleich dem Ergaenzungsschluessel im CIDSystemInfo-Verzeichnis des CMap sein.");
		de = de.replace(
				"Overprint mode (as set by the OPM value in an ExtGState dictionary) shall not be one (1) when an ICCBased CMYK colour space is used for stroke and overprinting for stroke is set to true, or when ICCBased CMYK colour space is used for fill and overprinting for fill is set to true, or both",
				"Der Ueberdrucken-Modus (wie durch den OPM-Wert in einem ExtGState-Verzeichnis festgelegt) darf nicht eins (1) sein, wenn ein ICCBased CMYK-Farbraum fuer die Kontur verwendet wird und das Ueberdrucken fuer die Kontur auf wahr gesetzt ist, oder wenn ein ICCBased CMYK-Farbraum fuer die Fuellung verwendet wird und das Ueberdrucken fuer die Fuellung auf wahr gesetzt ist, oder beides.");
		de = de.replace("pdfaExtension:schemas - Bag Schema - Description of extension schemas",
				"pdfaExtension:schemas - Bag Schema - Beschreibung der Erweiterungsschemata.");
		de = de.replace(
				"Properties specified in XMP form shall use either the predefined schemas defined in XMP Specification, or extension schemas that comply with XMP Specification",
				"Fuer Eigenschaften, die in XMP-Form angegeben werden, sind entweder die in der XMP-Spezifikation definierten vordefinierten Schemas oder Erweiterungsschemas zu verwenden, die der XMP-Spezifikation entsprechen.");
		de = de.replace("Property amd of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"Die Eigenschaft amd des PDF/A-Identifikationsschemas muss das Namensraumpraefix pdfaid aufweisen.");
		de = de.replace("Property conformance of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"Die Eigenschaft conformance des PDF/A-Identifikationsschemas muss das Namensraumpraefix pdfaid aufweisen.");
		de = de.replace("Property corr of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"Die Eigenschaft corr des PDF/A-Identifikationsschemas muss das Namensraumpraefix pdfaid aufweisen.");
		de = de.replace("Property part of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"Eigenschaft part des PDF/A-Identifikationsschemas muss das Namensraumpraefix pdfaid haben.");
		de = de.replace("Smallest integer value is -2,147,483,648",
				"Der kleinste ganzzahlige Wert ist -2.147.483.648.");
		de = de.replace("Standard tags shall not be remapped to a non-standard type",
				"Standard-Tags duerfen nicht einem nicht standardmaessigen Typ zugeordnet werden.");
		de = de.replace(
				"Subtype - name - (Required) The type of font; must be Type1 for Type 1 fonts, MMType1 for multiple master fonts, TrueType for TrueType fonts Type3 for Type 3 fonts, Type0 for Type 0 fonts and CIDFontType0 or CIDFontType2 for CID fonts",
				"Subtype - name - (Erforderlich) Der Schriftartentyp; muss Type1 fuer Type-1-Schriftarten, MMType1 fuer Multiple-Master-Schriftarten, TrueType fuer TrueType-Schriftarten, Type3 fuer Type-3-Schriftarten, Type0 fuer Type-0-Schriftarten und CIDFontType0 oder CIDFontType2 fuer CID-Schriftarten sein.");
		de = de.replace("Symbolic TrueType fonts shall not contain an Encoding entry in the font dictionary",
				"Symbolische TrueType-Schriftarten duerfen keinen Eintrag fuer die Kodierung im Schriftartenverzeichnis enthalten.");
		de = de.replace("The % character of the file header shall occur at byte offset 0 of the file.",
				"Das %-Zeichen des Datei-Headers muss am Byte-Offset 0 der Datei auftreten.");
		de = de.replace(
				"The aforementioned EOL marker shall be immediately followed by a % (25h) character followed by at least four bytes, each of whose encoded byte values shall have a decimal value greater than 127",
				"Auf die oben genannte EOL-Markierung muss unmittelbar ein %-Zeichen (25h) folgen, gefolgt von mindestens vier Bytes, deren kodierte Byte-Werte jeweils einen Dezimalwert von mehr als 127 haben muessen.");
		de = de.replace("The AS key shall not appear in any optional content configuration dictionary",
				"Der AS-Schluessel darf in keinem optionalen Inhaltskonfigurationsverzeichnis erscheinen.");
		de = de.replace("The bit-depth of the JPEG2000 data shall have a value in the range 1 to 38.",
				"Die Bit-Tiefe der JPEG2000-Daten muss einen Wert zwischen 1 und 38 haben.");
		de = de.replace("The bytes attribute shall not be used in the header of an XMP packet",
				"Das Bytes-Attribut darf nicht im Header eines XMP-Pakets verwendet werden.");
		de = de.replace(
				"The Catalog dictionary of a conforming file shall contain the Metadata key whose value is a metadata stream as defined in ISO 32000-1:2008, 14.3.2",
				"Das Katalog-Verzeichnis einer konformen Datei muss den Metadaten-Schluessel enthalten, dessen Wert ein Metadaten-Stream gemaess ISO 32000-1:2008, 14.3.2 ist.");
		de = de.replace(
				"The cmap table in the embedded font program for a symbolic TrueType font shall contain either exactly one encoding or it shall contain, at least, the Microsoft Symbol (3,0 - Platform ID=3, Encoding ID=0) encoding",
				"Die cmap-Tabelle im eingebetteten Schriftartprogramm fuer eine symbolische TrueType-Schriftart muss entweder genau eine Codierung enthalten oder mindestens die Microsoft-Symbol-Codierung (3,0 - Platform ID=3, Encoding ID=0).");
		de = de.replace("The document catalog dictionary of a conforming file shall contain the Metadata key",
				"Das Dokumentkatalog-Verzeichnis einer konformen Datei muss den Metadaten-Schluessel enthalten.");
		de = de.replace(
				"The document catalog dictionary shall include a MarkInfo dictionary containing an entry, Marked, whose value shall be true",
				"Das Dokumentkatalog-Verzeichnis muss ein MarkInfo-Verzeichnis enthalten, das einen Eintrag Marked enthaelt, dessen Wert true sein muss.");
		de = de.replace(
				"The document catalog dictionary shall include a MarkInfo dictionary with a Marked entry in it, whose value shall be true",
				"Das Dokumentkatalog-Woerterbuch muss ein MarkInfo-Woerterbuch mit einem Marked-Eintrag enthalten, dessen Wert true sein muss.");
		de = de.replace("The document catalog dictionary shall not contain a key with the name OCProperties",
				"Das Dokumentkatalog-Woerterbuch darf keinen Schluessel mit dem Namen OCProperties enthalten.");
		de = de.replace(
				"The document catalog dictionary shall not include an AA entry for an additional-actions dictionary",
				"Das Dokumentkatalog-Woerterbuch darf keinen AA-Eintrag fuer ein Woerterbuch fuer zusaetzliche Aktionen enthalten.");
		de = de.replace("The document catalog shall not contain the Requirements key",
				"Der Dokumentkatalog darf den Schluessel Requirements nicht enthalten.");
		de = de.replace("The documents Catalog shall not include an AA entry for an additional-actions dictionary",
				"Der Katalog des Dokuments darf keinen AA-Eintrag fuer ein Woerterbuch fuer zusaetzliche Aktionen enthalten.");
		de = de.replace(
				"The documents interactive form dictionary that forms the value of the AcroForm key in the documents Catalog of a PDF/A-2 file, if present, shall not contain the XFA key",
				"Das interaktive Formularwoerterbuch des Dokuments, das den Wert des AcroForm-Schluessels im Dokumentkatalog einer PDF/A-2-Datei bildet, falls vorhanden, darf den XFA-Schluessel nicht enthalten.");
		de = de.replace("The encoding attribute shall not be used in the header of an XMP packet",
				"Das Kodierungsattribut darf nicht im Header eines XMP-Pakets verwendet werden.");
		de = de.replace("The endstream keyword shall be preceded by an EOL marker",
				"Dem Schluesselwort endstream muss ein EOL-Marker vorangestellt werden.");
		de = de.replace(
				"The extension schema container schema uses the namespace URI http://www.aiim.org/pdfa/ns/extension/.",
				"Das Erweiterungsschema-Containerschema verwendet den Namensraum-URI http://www.aiim.org/pdfa/ns/extension/.");
		de = de.replace(
				"The F key’s Print flag bit shall be set to 1 and its Hidden, Invisible and NoView flag bits shall be set to 0",
				"Das Bit Print Flag des F-Schluessels muss auf 1 gesetzt werden und die Bits Hidden, Invisible und NoView muessen auf 0 gesetzt werden.");
		de = de.replace(
				"The file header line shall be immediately followed by a comment consisting of a % character followed by at least four characters, each of whose encoded byte values shall have a decimal value greater than 127",
				"Auf die Datei-Header-Zeile muss unmittelbar ein Kommentar folgen, der aus einem %-Zeichen gefolgt von mindestens vier Zeichen besteht, wobei jeder der kodierten Byte-Werte einen Dezimalwert von mehr als 127 haben muss.");
		de = de.replace(
				"The file header shall begin at byte zero and shall consist of %PDF-1.n followed by a single EOL marker, where n is a single digit number between 0 (30h) and 7 (37h)",
				"Der Datei-Header muss bei Byte Null beginnen und aus %PDF-1.n gefolgt von einem einzelnen EOL-Marker bestehen, wobei n eine einzelne Ziffer zwischen 0 (30h) und 7 (37h) ist.");
		de = de.replace("The file specification dictionary for an embedded file shall contain the F and UF keys",
				"Das Dateispezifikationsverzeichnis fuer eine eingebettete Datei muss die Schluessel F und UF enthalten.");
		de = de.replace(
				"The file trailer dictionary shall contain the ID keyword whose value shall be File Identifiers as defined in ISO 32000-1:2008, 14.4",
				"Das Dateianhangsverzeichnis muss das Schluesselwort ID enthalten, dessen Wert die in ISO 32000-1:2008, 14.4 definierten Dateikennungen sind.");
		de = de.replace("The file trailer dictionary shall contain the ID keyword.",
				"Das Dateianhangsverzeichnis muss das Schluesselwort ID enthalten.");
		de = de.replace(
				"The file trailer referred to is either the last trailer dictionary in a PDF file, as described in PDF Reference 3.4.4 and 3.4.5, or the first page trailer in a linearized PDF file, as described in PDF Reference F.2",
				"Der Dateianhaenger, auf den Bezug genommen wird, ist entweder das letzte Anhaengerverzeichnis in einer PDF-Datei, wie in PDF-Referenz 3.4.4 und 3.4.5 beschrieben, oder der erste Seitenanhaenger in einer linearisierten PDF-Datei, wie in PDF-Referenz F.2 beschrieben.");
		de = de.replace(
				"The first line of a PDF file is a header identifying the version of the PDF specification to which the file conforms",
				"Die erste Zeile einer PDF-Datei ist ein Header, der die Version der PDF-Spezifikation angibt, der die Datei entspricht.");
		de = de.replace(
				"The following keys, if present in an ExtGState object, shall have the values shown: BM - Normal or Compatible",
				"Die folgenden Schluessel muessen, sofern sie in einem ExtGState-Objekt vorhanden sind, die angegebenen Werte aufweisen: BM - Normal oder kompatibel.");
		de = de.replace("The following keys, if present in an ExtGState object, shall have the values shown: CA - 1.0",
				"Die folgenden Schluessel muessen, sofern sie in einem ExtGState-Objekt vorhanden sind, die angegebenen Werte aufweisen: CA - 1.0.");
		de = de.replace(
				"The Font dictionary of all fonts shall define the map of all used character codes to Unicode values, either via a ToUnicode entry, or other mechanisms as defined in ISO 19005-2, 6.2.11.7.2",
				"Das Font-Verzeichnis aller Schriftarten muss die Zuordnung aller verwendeten Zeichencodes zu Unicode-Werten definieren, entweder ueber einen ToUnicode-Eintrag oder andere Mechanismen, wie in ISO 19005-2, 6.2.11.7.2 definiert.");
		de = de.replace(
				"The font dictionary shall include a ToUnicode entry whose value is a CMap stream object that maps character codes to Unicode values, as described in PDF Reference 5.9, unless the font meets any of the following three conditions: (*) fonts that use the predefined encodings MacRomanEncoding, MacExpertEncoding or WinAnsiEncoding, or that use the predefined Identity-H or Identity-V CMaps; (*) Type 1 fonts whose character names are taken from the Adobe standard Latin character set or the set of named characters in the Symbol font, as defined in PDF Reference Appendix D; (*) Type 0 fonts whose descendant CIDFont uses the Adobe-GB1, Adobe-CNS1, Adobe-Japan1 or Adobe-Korea1 character collections",
				"Das Schriftartenverzeichnis muss einen ToUnicode-Eintrag enthalten, dessen Wert ein CMap-Stream-Objekt ist, das Zeichencodes Unicode-Werten zuordnet, wie in der PDF-Referenz 5.9 beschrieben, es sei denn, die Schriftart erfuellt eine der folgenden drei Bedingungen: (*) Schriften, die die vordefinierten Kodierungen MacRomanEncoding, MacExpertEncoding oder WinAnsiEncoding verwenden oder die vordefinierten Identity-H- oder Identity-V-CMaps verwenden; (*) Type-1-Schriften, deren Zeichennamen aus dem Adobe-Standard-Zeichensatz fuer lateinische Zeichen lateinischen Zeichensatz oder aus dem Satz benannter Zeichen in der Schriftart Symbol stammen, wie in Anhang D der PDF-Referenz definiert; (*) Schriftarten vom Typ 0, deren Nachkomme CIDFont die Zeichensammlungen Adobe-GB1, Adobe-CNS1, Adobe-Japan1 oder Adobe-Korea1 verwendet.");
		de = de.replace(
				"The font programs for all fonts used for rendering within a conforming file shall be embedded within that file, as defined in ISO 32000-1:2008, 9.9",
				"Die Schriftartprogramme fuer alle Schriftarten, die fuer die Darstellung in einer konformen Datei verwendet werden, muessen gemaess ISO 32000-1:2008, 9.9, in diese Datei eingebettet werden.");
		de = de.replace(
				"The font programs for all fonts used within a conforming file shall be embedded within that file, as defined in PDF Reference 5.8, except when the fonts are used exclusively with text rendering mode 3",
				"Die Schriftartprogramme fuer alle in einer konformen Datei verwendeten Schriftarten muessen in diese Datei eingebettet werden, wie in der PDF-Referenz 5.8 definiert, es sei denn, die Schriftarten werden ausschliesslich mit dem Textwiedergabemodus 3 verwendet.");
		de = de.replace("The generation number and obj keyword shall be separated by a single white-space character.",
				"Die Generierungsnummer und das Schluesselwort obj muessen durch ein einzelnes Leerzeichen getrennt werden.");
		de = de.replace("The Hide action shall not be permitted (Corrigendum 2)",
				"Die Aktion Hide ist nicht zulaessig (Berichtigung 2).");
		de = de.replace("The keyword Encrypt shall not be used in the trailer dictionary",
				"Das Schluesselwort Encrypt darf nicht im Trailer Dictionary verwendet werden.");
		de = de.replace(
				"The Launch, Sound, Movie, ResetForm, ImportData and JavaScript actions shall not be permitted.",
				"Die Aktionen Launch, Sound, Movie, ResetForm, ImportData und JavaScript sind nicht zulaessig.");
		de = de.replace(
				"The Launch, Sound, Movie, ResetForm, ImportData, Hide, SetOCGState, Rendition, Trans, GoTo3DView and JavaScript actions shall not be permitted.",
				"Die Aktionen Launch, Sound, Movie, ResetForm, ImportData, Hide, SetOCGState, Rendition, Trans, GoTo3DView und JavaScript sind nicht zulaessig.");
		de = de.replace(
				"The logical structure of the conforming file shall be described by a structure hierarchy rooted in the StructTreeRoot entry of the document catalog dictionary, as described in PDF Reference 9.6",
				"Die logische Struktur der konformen Datei muss durch eine Strukturhierarchie beschrieben werden, die im StructTreeRoot-Eintrag des Dokumentkatalog-Woerterbuchs verwurzelt ist, wie in der PDF-Referenz 9.6 beschrieben.");
		de = de.replace(
				"The logical structure of the conforming file shall be described by a structure hierarchy rooted in the StructTreeRoot entry of the documents Catalog dictionary, as described in ISO 32000-1:2008, 14.7",
				"Die logische Struktur der konformen Datei muss durch eine Strukturhierarchie beschrieben werden, die im StructTreeRoot-Eintrag des Dokumentkatalog-Verzeichnisses verwurzelt ist, wie in ISO 32000-1:2008, 14.7 beschrieben.");
		de = de.replace("The LZWDecode filter shall not be permitted", "Der LZWDecode-Filter ist nicht zulaessig.");
		de = de.replace(
				"The metadata stream shall conform to XMP Specification and well formed PDFAExtension Schema for all extensions",
				"Der Metadatenstrom muss der XMP-Spezifikation und dem wohlgeformten PDFAExtension-Schema fuer alle Erweiterungen entsprechen.");
		de = de.replace(
				"The NeedAppearances flag of the interactive form dictionary shall either not be present or shall be false",
				"Das NeedAppearances-Flag des interaktiven Formularwoerterbuchs darf entweder nicht vorhanden oder falsch sein.");
		de = de.replace(
				"The number of color components in the color space described by the ICC profile data must match the number of components actually in the ICC profile.",
				"Die Anzahl der Farbkomponenten im Farbraum, der durch die ICC-Profildaten beschrieben wird, muss mit der Anzahl der Komponenten uebereinstimmen, die tatsaechlich im ICC-Profil enthalten sind.");
		de = de.replace("The number of colour channels in the JPEG2000 data shall be 1, 3 or 4",
				"Die Anzahl der Farbkanaele in den JPEG2000-Daten muss 1, 3 oder 4 betragen.");
		de = de.replace("The obj and endobj keywords shall each be followed by an EOL marker",
				"Auf die Schluesselwoerter obj und endobj muss jeweils ein EOL-Marker folgen.");
		de = de.replace("The object number and endobj keyword shall each be preceded by an EOL marker.",
				"Den Schluesselwoertern object number und endobj muss jeweils ein EOL-Marker vorangestellt werden.");
		de = de.replace("The object number and generation number shall be separated by a single white-space character.",
				"Die Objektnummer und die Generierungsnummer muessen durch ein einzelnes Leerzeichen getrennt werden.");
		de = de.replace(
				"The only valid values of this key in PDF 1.4 are Type1C - Type 1–equivalent font program represented in the Compact Font Format (CFF) and CIDFontType0C - Type 0 CIDFont program represented in the Compact Font Format (CFF)",
				"Die einzigen gueltigen Werte dieses Schluessels in PDF 1.4 sind Type1C – Type 1-aequivalentes Font-Programm, dargestellt im Compact Font Format (CFF) und CIDFontType0C – Type 0 CIDFont-Programm, dargestellt im Compact Font Format (CFF).");
		de = de.replace(
				"The only valid values of this key in PDF 1.7 are Type1C - Type 1–equivalent font program represented in the Compact Font Format (CFF), CIDFontType0C - Type 0 CIDFont program represented in the Compact Font Format (CFF) and OpenType - OpenType® font program, as described in the OpenType Specification v.1.4",
				"Die einzigen gueltigen Werte dieses Schluessels in PDF 1.7 sind Type1C - Type 1-aequivalentes Schriftartprogramm, dargestellt im Compact Font Format (CFF), CIDFontType0C - Type 0 CIDFont-Programm, dargestellt im Compact Font Format (CFF) und OpenType - OpenType®-Schriftartprogramm, wie in der OpenType-Spezifikation v.1.4 beschrieben.");
		de = de.replace("The Page dictionary shall not include an AA entry for an additional-actions dictionary",
				"Das Seitenwoerterbuch darf keinen AA-Eintrag fuer ein Woerterbuch fuer zusaetzliche Aktionen enthalten.");
		de = de.replace(
				"The PDF Signature (a DER-encoded PKCS#7 binary data object) shall be placed into the Contents entry of the signature dictionary.",
				"Die PDF-Signatur (ein DER-codiertes PKCS#7-Binaerdatenobjekt) muss im Eintrag Contents des Signaturwoerterbuchs platziert werden.");
		de = de.replace(
				"The PDF/A version and conformance level of a file shall be specified using the PDF/A Identification extension schema",
				"Die PDF/A-Version und die Konformitaetsstufe einer Datei muessen unter Verwendung des PDF/A-Erweiterungsschemas fuer die Identifizierung angegeben werden.");
		de = de.replace("The PKCS#7 object shall conform to the PKCS#7 specification in RFC 2315.",
				"Das PKCS#7-Objekt muss der PKCS#7-Spezifikation in RFC 2315 entsprechen.");
		de = de.replace(
				"The profile stream that is the value of the DestOutputProfile key shall either be an output profile (Device Class = prtr) or a monitor profile (Device Class = mntr).",
				"Der Profil-Stream, der den Wert des Schluessels DestOutputProfile darstellt, muss entweder ein Ausgabeprofil (Geraeteklasse = prtr) oder ein Monitorprofil (Geraeteklasse = mntr) sein.");
		de = de.replace(
				"The profile that forms the stream of an ICCBased colour space shall conform to ICC.1:1998-09, ICC.1:2001-12, ICC.1:2003-09 or ISO 15076-1",
				"Das Profil, das den Datenstrom eines ICC-basierten Farbraums bildet, muss mit ICC.1:1998-09, ICC.1:2001-12, ICC.1:2003-09 oder ISO 15076-1 konform sein.");
		de = de.replace("The profiles shall have a colour space of either GRAY, RGB, or CMYK",
				"Die Profile muessen einen Farbraum von entweder GRAY, RGB oder CMYK haben.");
		de = de.replace("The required schema namespace prefix is pdfaExtension.",
				"Das erforderliche Schema-Namensraumpraefix ist pdfaExtension.");
		de = de.replace(
				"The size of any of the page boundaries described in ISO 32000-1:2008, 14.11.2 shall not be less than 3 units in either direction, nor shall it be greater than 14 400 units in either direction",
				"Die Groesse der in ISO 32000-1:2008, 14.11.2 beschriebenen Seitengrenzen darf in keiner Richtung weniger als 3 Einheiten und nicht mehr als 14 400 Einheiten betragen.");
		de = de.replace(
				"The stream keyword shall be followed either by a CARRIAGE RETURN (0Dh) and LINE FEED (0Ah) character sequence or by a single LINE FEED (0Ah) character.",
				"Auf das Schluesselwort stream muss entweder eine Zeichenfolge aus CARRIAGE RETURN (0Dh) und LINE FEED (0Ah) oder ein einzelnes LINE FEED-Zeichen (0Ah) folgen.");
		de = de.replace(
				"The stream keyword shall be followed either by a CARRIAGE RETURN (0Dh) and LINE FEED (0Ah) character sequence or by a single LINE FEED character.",
				"Auf das Schluesselwort stream muss entweder eine Zeichenfolge aus CARRIAGE RETURN (0Dh) und LINE FEED (0Ah) oder ein einzelnes LINE FEED-Zeichen folgen.");
		de = de.replace("The subtype is the value of the Subtype key, if present, in the font file stream dictionary.",
				"Der Subtyp ist der Wert des Schluessels Subtype, falls vorhanden, im Font-Datei-Stream-Verzeichnis.");
		de = de.replace(
				"The TransferFunction key in a halftone dictionary shall be used only as required by ISO 32000-1",
				"Der Schluessel TransferFunction in einem Halbtonverzeichnis darf nur gemaess ISO 32000-1 verwendet werden.");
		de = de.replace(
				"The Unicode values specified in the ToUnicode CMap shall all be greater than zero (0), but not equal to either U+FEFF or U+FFFE",
				"Die in der CMap ToUnicode angegebenen Unicode-Werte muessen alle groesser als Null (0) sein, duerfen aber weder U+FEFF noch U+FFFE entsprechen.");
		de = de.replace("The value of pdfaid:part shall be the part number of ISO 19005 to which the file conforms",
				"Der Wert von pdfaid:part muss die Teilenummer von ISO 19005 sein, der die Datei entspricht.");
		de = de.replace(
				"The value of Author entry from the document information dictionary, if present, and its analogous XMP property dc:creator shall be equivalent.",
				"Der Wert des Eintrags Author aus dem Dokumentinformationsverzeichnis, falls vorhanden, und seine analoge XMP-Eigenschaft dc:creator muessen gleichwertig sein.");
		de = de.replace(
				"The value of CreationDate entry from the document information dictionary, if present, and its analogous XMP property xmp:CreateDate shall be equivalent",
				"Der Wert des Eintrags CreationDate aus dem Dokumentinformationsverzeichnis, sofern vorhanden, und seine analoge XMP-Eigenschaft xmp:CreateDate muessen gleichwertig sein.");
		de = de.replace(
				"The value of Creator entry from the document information dictionary, if present, and its analogous XMP property xmp:CreatorTool shall be equivalent",
				"Der Wert des Eintrags Creator aus dem Dokumentinformationsverzeichnis, sofern vorhanden, und seine analoge XMP-Eigenschaft xmp:CreatorTool muessen gleichwertig sein.");
		de = de.replace(
				"The value of Keywords entry from the document information dictionary, if present, and its analogous XMP property pdf:Keywords shall be equivalent",
				"Der Wert des Eintrags Keywords aus dem Dokumentinformationsverzeichnis, sofern vorhanden, und seine analoge XMP-Eigenschaft pdf:Keywords muessen gleichwertig sein.");
		de = de.replace(
				"The value of ModDate entry from the document information dictionary, if present, and its analogous XMP property xmp:ModifyDate shall be equivalent",
				"Der Wert des Eintrags ModDate aus dem Dokumentinformationsverzeichnis, sofern vorhanden, und seine entsprechende XMP-Eigenschaft xmp:ModifyDate muessen gleichwertig sein.");
		de = de.replace(
				"The value of Producer entry from the document information dictionary, if present, and its analogous XMP property pdf:Producer shall be equivalent",
				"Der Wert des Eintrags Producer aus dem Dokumentinformationsverzeichnis, sofern vorhanden, und seine entsprechende XMP-Eigenschaft pdf:Producer muessen gleichwertig sein.");
		de = de.replace(
				"The value of Subject entry from the document information dictionary, if present, and its analogous XMP property dc:description[x-default] shall be equivalent",
				"Der Wert des Eintrags Subject aus dem Dokumentinformationsverzeichnis, sofern vorhanden, und seine entsprechende XMP-Eigenschaft dc:description[‚x-default‘] muessen gleichwertig sein.");
		de = de.replace(
				"The value of the F key in the Inline Image dictionary shall not be LZW, LZWDecode, Crypt, a value not listed in ISO 32000-1:2008, Table 6, or an array containing any such value",
				"Der Wert des Schluessels F im Inline-Bildverzeichnis darf nicht LZW, LZWDecode, Crypt, ein in ISO 32000-1:2008, Tabelle 6, nicht aufgefuehrter Wert oder ein Array sein, das einen solchen Wert enthaelt.");
		de = de.replace(
				"The value of the Length key specified in the stream dictionary shall match the number of bytes in the file following the LINE FEED (0Ah) character after the stream keyword and preceding the EOL marker before the endstream keyword",
				"Der Wert des im Stream-Verzeichnis angegebenen Laengen-Schluessels muss mit der Anzahl der Bytes in der Datei uebereinstimmen, die auf das Zeichen LINE FEED (0Ah) nach dem Schluesselwort Stream und vor der EOL-Markierung vor dem Schluesselwort Endstream folgt.");
		de = de.replace(
				"The value of the Length key specified in the stream dictionary shall match the number of bytes in the file following the LINE FEED character after the stream keyword and preceding the EOL marker before the endstream keyword",
				"Der Wert des im Stream-Verzeichnis angegebenen Laengen-Schluessels muss mit der Anzahl der Bytes in der Datei uebereinstimmen, die auf das Zeichen LINE FEED nach dem Schluesselwort Stream und vor der EOL-Markierung vor dem Schluesselwort Endstream folgt.");
		de = de.replace("The value of the METH entry in its colr box shall be 0x01, 0x02 or 0x03.",
				"Der Wert des METH-Eintrags in seinem colr-Feld muss 0x01, 0x02 oder 0x03 sein.");
		de = de.replace(
				"The value of Title entry from the document information dictionary, if present, and its analogous XMP property dc:title[x-default] shall be equivalent",
				"Der Wert des Title-Eintrags aus dem Dokumentinformationsverzeichnis, falls vorhanden, und seine analoge XMP-Eigenschaft dc:title[‚x-default‘] muessen gleichwertig sein.");
		de = de.replace("The XMP package must be encoded as UTF-8", "Das XMP-Paket muss als UTF-8 codiert sein.");
		de = de.replace(
				"The xref keyword and the cross reference subsection header shall be separated by a single EOL marker",
				"Das Schluesselwort xref und die Ueberschrift des Querverweis-Unterabschnitts muessen durch einen einzelnen EOL-Marker getrennt sein.");
		de = de.replace(
				"The xref keyword and the cross-reference subsection header shall be separated by a single EOL marker",
				"Das Schluesselwort xref und die Ueberschrift des Querverweis-Unterabschnitts muessen durch einen einzelnen EOL-Marker getrennt sein.");
		de = de.replace("There shall be no AlternatePresentations entry in the documents name dictionary",
				"Es darf keinen AlternatePresentations-Eintrag im Namensverzeichnis des Dokuments geben.");
		de = de.replace("There shall be no PresSteps entry in any Page dictionary",
				"Es darf keinen PresSteps-Eintrag in einem Seitenverzeichnis geben.");
		de = de.replace(
				"Type - name - (Required) The type of PDF object that this dictionary describes; must be Font for a font dictionary",
				"Typ – Name – (erforderlich) Der Typ des PDF-Objekts, das dieses Verzeichnis beschreibt; muss Font fuer ein Schriftartenverzeichnis sein.");
		de = de.replace("Value of valueType property shall be defined",
				"Der Wert der Eigenschaft valueType muss definiert sein.");
		de = de.replace(
				"When computing the digest for the file, it shall be computed over the entire file, including the signature dictionary but excluding the PDF Signature itself",
				"Bei der Berechnung des Digests fuer die Datei muss die gesamte Datei einbezogen werden, einschliesslich des Signaturwoerterbuchs, aber ausschliesslich der PDF-Signatur selbst.");
		de = de.replace(
				"Where a rendering intent is specified, its value shall be one of the four values defined in ISO 32000-1:2008, Table 70: RelativeColorimetric, AbsoluteColorimetric, Perceptual or Saturation",
				"Wenn eine Wiedergabeart angegeben ist, muss ihr Wert einer der vier in ISO 32000-1:2008, Tabelle 70, definierten Werte sein: Relative Colorimetric, Absolute Colorimetric, Perceptual oder Saturation.");
		de = de.replace(
				"Where a rendering intent is specified, its value shall be one of the four values defined in PDF Reference RelativeColorimetric, AbsoluteColorimetric, Perceptual or Saturation",
				"Wenn eine Wiedergabeart angegeben ist, muss ihr Wert einer der vier in der PDF-Referenz definierten Werte sein: Relative Colorimetric, Absolute Colorimetric, Perceptual oder Saturation.");
		de = de.replace(
				"Widths - array - (Required except for the standard 14 fonts; indirect reference preferred) An array of (LastChar − FirstChar + 1) widths",
				"Breiten – Array – (Erforderlich, ausser fuer die 14 Standardschriftarten; indirekte Referenz bevorzugt) Ein Array von (LastChar – FirstChar + 1) Breiten.");
		de = de.replace("Xref streams shall not be used", "Xref-Streams darf nicht verwendet werden");
		return de;
	}

	/** Englisch String ins Franzesische aendern */
	public static String enTOfr(String en) {
		String fr = en;
		// TODO: fr = fr.replace("en", "fr");
		fr = fr.replace("A circular mapping shall not exist", "Il ne doit pas y avoir de correspondance circulaire.");
		fr = fr.replace(
				"A CMap shall not reference any other CMap except those listed in ISO 32000-1:2008, 9.7.5.2, Table 118",
				"Une CMap ne doit pas faire reference a une autre CMap, a l`exception de celles enumerees dans la norme ISO 32000-1:2008, 9.7.5.2, tableau 118.");
		fr = fr.replace("A conforming file shall not contain a CID value greater than 65535",
				"Un fichier conforme ne doit pas contenir une valeur CID superieure a 65535.");
		fr = fr.replace("A conforming file shall not contain a DeviceN colour space with more than 32 colourants",
				"Un fichier conforme ne doit pas contenir un espace colorimetrique DeviceN comportant plus de 32 colorants.");
		fr = fr.replace("A conforming file shall not contain any integer greater than 2147483647.",
				"Un fichier conforme ne doit pas contenir de nombre entier superieur a 2147483647.");
		fr = fr.replace("A conforming file shall not contain any integer less than -2147483648",
				"Un fichier conforme ne doit pas contenir un nombre entier inferieur a -2147483648.");
		fr = fr.replace("A conforming file shall not contain any name longer than 127 bytes",
				"Un fichier conforme ne doit pas contenir de nom de plus de 127 octets.");
		fr = fr.replace("A conforming file shall not contain any PostScript XObjects",
				"Un fichier conforme ne doit pas contenir d`objets XObjects PostScript.");
		fr = fr.replace("A conforming file shall not contain any real number closer to zero than +/-1.175 x 10^(-38)",
				"Un fichier conforme ne doit pas contenir de nombre reel plus proche de zero que +/-1,175 x 10^(-38).");
		fr = fr.replace("A conforming file shall not contain any real number outside the range of +/-3.403 x 10^38",
				"Un fichier conforme ne doit pas contenir de nombre reel en dehors de l`intervalle +/-3.403 x 10^38.");
		fr = fr.replace("A conforming file shall not contain any reference XObjects",
				"Un fichier conforme ne doit pas contenir de XObjects de reference.");
		fr = fr.replace("A conforming file shall not contain any string longer than 32767 bytes",
				"Un fichier conforme ne doit pas contenir de chaine de caracteres d`une longueur superieure a 32767 octets.");
		fr = fr.replace("A conforming file shall not contain more than 8388607 indirect objects",
				"Un fichier conforme ne doit pas contenir plus de 8388607 objets indirects.");
		fr = fr.replace("A conforming file shall not nest q/Q pairs by more than 28 nesting levels",
				"Un fichier conforme ne doit pas imbriquer des paires q/Q sur plus de 28 niveaux d`imbrication.");
		fr = fr.replace(
				"A conforming reader shall use only that colour space and shall ignore all other colour space specifications",
				"Un lecteur conforme doit utiliser uniquement cet espace couleur et ignorer toutes les autres specifications d`espace couleur.");
		fr = fr.replace(
				"A content stream shall not contain any operators not defined in PDF Reference even if such operators are bracketed by the BX/EX compatibility operators",
				"Un flux de contenu ne doit pas contenir d`operateurs non definis dans la reference PDF, meme si ces operateurs sont mis entre parentheses par les operateurs de compatibilite BX/EX.");
		fr = fr.replace(
				"A content stream that references other objects, such as images and fonts that are necessary to fully render or process the stream, shall have an explicitly associated Resources dictionary as described in ISO 32000-1:2008, 7.8.3",
				"Un flux de contenu qui fait reference a d`autres objets, tels que des images et des polices de caracteres, necessaires au rendu ou au traitement complet du flux, doit etre explicitement associe a un dictionnaire de ressources tel que decrit dans la norme ISO 32000-1:2008, 7.8.3.");
		fr = fr.replace("A documents Catalog shall not contain the NeedsRendering key",
				"Le catalogue d`un document ne doit pas contenir la cle NeedsRendering.");
		fr = fr.replace("A Field dictionary shall not contain the A or AA keys",
				"Un dictionnaire de champs ne doit pas contenir les cles A ou AA.");
		fr = fr.replace("A Field dictionary shall not include an AA entry for an additional-actions dictionary",
				"Un dictionnaire de champs ne doit pas comporter d`entree AA pour un dictionnaire d`actions supplementaires.");
		fr = fr.replace(
				"A file specification dictionary, as defined in ISO 32000-1:2008, 7.11.3, may contain the EF key, provided that the embedded file is compliant with either ISO 19005-1 or this part of ISO 19005",
				"Un dictionnaire de specification de fichier, tel que defini dans la norme ISO 32000-1:2008, 7.11.3, peut contenir la cle EF, a condition que le fichier incorpore soit conforme a la norme ISO 19005-1 ou a la presente partie de la norme ISO 19005.");
		fr = fr.replace("A file specification dictionary, as defined in PDF 3.10.2, shall not contain the EF key",
				"Un dictionnaire de specification de fichier, tel que defini dans le PDF 3.10.2, ne doit pas contenir la cle EF.");
		fr = fr.replace(
				"A files name dictionary, as defined in PDF Reference 3.6.3, shall not contain the EmbeddedFiles key",
				"Le dictionnaire des noms de fichiers, tel que defini dans la reference PDF 3.6.3, ne doit pas contenir la cle EmbeddedFiles.");
		fr = fr.replace(
				"A font referenced for use solely in rendering mode 3 is therefore not rendered and is thus exempt from the embedding requirement.",
				"Une police referencee pour etre utilisee uniquement en mode de rendu 3 n`est donc pas rendue et n`est donc pas soumise a l`exigence d`incorporation.");
		fr = fr.replace(
				"A form XObject dictionary shall not contain any of the following: - the OPI key; - the Subtype2 key with a value of PS; - the PS key",
				"Le dictionnaire XObject d`un formulaire ne doit contenir aucun des elements suivants - la cle OPI ; - la cle Subtype2 avec une valeur PS ; - la cle PS.");
		fr = fr.replace("A form XObject dictionary shall not contain the Subtype2 key with a value of PS or the PS key",
				"Le dictionnaire d`un objet XObject de formulaire ne doit pas contenir la cle de sous-type 2 avec une valeur PS ou la cle PS.");
		fr = fr.replace(
				"A Group object with an S key with a value of Transparency shall not be included in a form XObject.",
				"Un objet de groupe dont la cle S a pour valeur Transparence ne doit pas etre inclus dans un objet XObject de formulaire.");
		fr = fr.replace(
				"A Group object with an S key with a value of Transparency shall not be included in a page dictionary",
				"Un objet de groupe dont la cle S a pour valeur Transparence ne doit pas etre inclus dans un dictionnaire de pages.");
		fr = fr.replace(
				"A hexadecimal string is written as a sequence of hexadecimal digits (0–9 and either A–F or a–f)",
				"Une chaine hexadecimale s`ecrit comme une sequence de chiffres hexadecimaux (0-9 et A-F ou a-f).");
		fr = fr.replace(
				"A language identifier shall either be the empty text string, to indicate that the language is unknown, or a Language-Tag as defined in RFC 3066, Tags for the Identification of Languages",
				"Un identifiant de langue est soit une chaine de texte vide, pour indiquer que la langue est inconnue, soit une etiquette de langue telle que definie dans la RFC 3066, etiquettes pour l`identification des langues.");
		fr = fr.replace("A Level A conforming file shall specify the value of pdfaid:conformance as A",
				"Un fichier conforme de niveau A doit specifier la valeur de pdfaid:conformance comme A.");
		fr = fr.replace("A Level A conforming file shall specify the value of pdfaid:conformance as A.",
				"Un fichier conforme au niveau A doit specifier que la valeur de pdfaid:conformance est A.");
		fr = fr.replace("A Level B conforming file shall specify the value of pdfaid:conformance as B",
				"Un fichier conforme de niveau B doit specifier la valeur de pdfaid:conformance comme etant B.");
		fr = fr.replace("A Level B conforming file shall specify the value of pdfaid:conformance as B.",
				"Un fichier conforme de niveau B indique que la valeur de pdfaid:conformance est B.");
		fr = fr.replace("A Level U conforming file shall specify the value of pdfaid:conformance as U",
				"Un fichier conforme au niveau U doit specifier la valeur de pdfaid:conformance comme etant U.");
		fr = fr.replace(
				"A PDF/A-1 OutputIntent is an OutputIntent dictionary, as defined by PDF Reference 9.10.4, that is included in the files OutputIntents array and has GTS_PDFA1 as the value of its S key and a valid ICC profile stream as the value its DestOutputProfile key",
				"Un OutputIntent PDF/A-1 est un dictionnaire OutputIntent, tel que defini par PDF Reference 9.10.4, qui est inclus dans le tableau OutputIntents du fichier et qui a pour valeur de sa cle S GTS_PDFA1 et pour valeur de sa cle DestOutputProfile un flux de profil ICC valide.");
		fr = fr.replace(
				"A PDF/A-2 compliant document shall not contain a reference to the .notdef glyph from any of the text showing operators, regardless of text rendering mode, in any content stream",
				"Un document conforme a la norme PDF/A-2 ne doit pas contenir de reference au glyphe .notdef de l`un des operateurs d`affichage de texte, quel que soit le mode de rendu du texte, dans un flux de contenu.");
		fr = fr.replace("A stream dictionary shall not contain the F, FFilter, or FDecodeParms keys",
				"Un dictionnaire de flux ne doit pas contenir les cles F, FFilter ou FDecodeParms.");
		fr = fr.replace("A stream object dictionary shall not contain the F, FFilter, or FDecodeParms keys",
				"Un dictionnaire d`objets de flux ne doit pas contenir les cles F, FFilter ou FDecodeParms.");
		fr = fr.replace("A Widget annotation dictionary shall not contain the A or AA keys",
				"Le dictionnaire d`annotation d`un widget ne doit pas contenir les cles A ou AA.");
		fr = fr.replace(
				"A Widget annotation dictionary shall not include an AA entry for an additional-actions dictionary",
				"Le dictionnaire d`annotations d`un widget ne doit pas inclure d`entree AA pour un dictionnaire d`actions supplementaires.");
		fr = fr.replace("Absolute real value must be less than or equal to 32767.0",
				"La valeur reelle absolue doit etre inferieure ou egale a 32767.0.");
		fr = fr.replace("Additionally, the 3D, Sound, Screen and Movie types shall not be permitted",
				"En outre, les types 3D, Sound, Screen et Movie ne sont pas autorises.");
		fr = fr.replace("Additionally, the deprecated set-state and no-op actions shall not be permitted",
				"En outre, les actions set-state et no-op, qui sont depreciees, ne sont pas autorisees.");
		fr = fr.replace("Additionally, the deprecated set-state and no-op actions shall not be permitted.",
				"En outre, les actions set-state et no-op depreciees ne sont pas autorisees.");
		fr = fr.replace("Additionally, the FileAttachment, Sound and Movie types shall not be permitted",
				"En outre, les types FileAttachment, Sound et Movie ne sont pas autorises.");
		fr = fr.replace(
				"All CMaps used within a conforming file, except Identity-H and Identity-V, shall be embedded in that file as described in PDF Reference 5.6.4",
				"Toutes les CMaps utilisees dans un fichier conforme, a l`exception de Identity-H et Identity-V, doivent etre incorporees dans ce fichier comme decrit dans la reference PDF 5.6.4.");
		fr = fr.replace(
				"All CMaps used within a PDF/A-2 file, except those listed in ISO 32000-1:2008, 9.7.5.2, Table 118, shall be embedded in that file as described in ISO 32000-1:2008, 9.7.5",
				"Toutes les CMaps utilisees dans un fichier PDF/A-2, a l`exception de celles enumerees dans la norme ISO 32000-1:2008, 9.7.5.2, tableau 118, doivent etre incorporees dans ce fichier comme decrit dans la norme ISO 32000-1:2008, 9.7.5.");
		fr = fr.replace("All colour channels in the JPEG2000 data shall have the same bit-depth",
				"Tous les canaux de couleur des donnees JPEG2000 doivent avoir la meme profondeur de bits.");
		fr = fr.replace(
				"All content of all XMP packets shall be well-formed, as defined by Extensible Markup Language (XML) 1.0 (Third Edition), 2.1, and the RDF/XML Syntax Specification (Revised)",
				"Le contenu de tous les paquets XMP doit etre bien forme, conformement a la definition du langage de balisage extensible (XML) 1.0 (troisieme edition), 2.1, et a la specification de syntaxe RDF/XML (revisee).");
		fr = fr.replace(
				"All fields described in each of the tables in 6.6.2.3.3 shall be present in any extension schema container schema",
				"Tous les champs decrits dans chacun des tableaux du paragraphe 6.6.2.3.3 doivent etre presents dans tout schema de conteneur de schema d`extension.");
		fr = fr.replace(
				"All fonts and font programs used in a conforming file, regardless of rendering mode usage, shall conform to the provisions in ISO 32000-1:2008, 9.6 and 9.7, as well as to the font specifications referenced by these provisions.",
				"Toutes les polices et tous les programmes de polices utilises dans un fichier conforme, quel que soit le mode de rendu utilise, doivent etre conformes aux dispositions de la norme ISO 32000-1:2008, 9.6 et 9.7, ainsi qu`aux specifications des polices referencees par ces dispositions.");
		fr = fr.replace(
				"All fonts used in a conforming file shall conform to the font specifications defined in PDF Reference 5.5.",
				"Toutes les polices utilisees dans un fichier conforme doivent etre conformes aux specifications des polices definies dans la reference PDF 5.5.");
		fr = fr.replace(
				"All halftones in a conforming PDF/A-2 file shall have the value 1 or 5 for the HalftoneType key",
				"Toutes les demi-teintes d`un fichier PDF/A-2 conforme doivent avoir la valeur 1 ou 5 pour la cle HalftoneType.");
		fr = fr.replace(
				"All ICCBased colour spaces shall be embedded as ICC profile streams as described in PDF Reference 4.5",
				"Tous les espaces colorimetriques bases sur l`ICC doivent etre integres sous forme de flux de profils ICC, comme decrit dans la reference PDF 4.5.");
		fr = fr.replace(
				"All ICCBased colour spaces shall be embedded as ICC profile streams as described in PDF Reference 4.5.",
				"Tous les espaces colorimetriques bases sur l`ICC doivent etre integres sous forme de flux de profils ICC, comme decrit dans la reference PDF 4.5.");
		fr = fr.replace("All metadata streams present in the PDF shall conform to the XMP Specification.",
				"Tous les flux de metadonnees presents dans le PDF doivent etre conformes a la specification XMP.");
		fr = fr.replace(
				"All non-standard structure types shall be mapped to the nearest functionally equivalent standard type, as defined in ISO 32000-1:2008, 14.8.4, in the role map dictionary of the structure tree root",
				"Tous les types de structure non standard doivent etre mis en correspondance avec le type standard fonctionnellement equivalent le plus proche, tel que defini dans la norme ISO 32000-1:2008, 14.8.4, dans le dictionnaire de correspondance des roles de la racine de l`arborescence.");
		fr = fr.replace(
				"All non-standard structure types shall be mapped to the nearest functionally equivalent standard type, as defined in PDF Reference 9.7.4, in the role map dictionary of the structure tree root",
				"Tous les types de structure non standard doivent etre mis en correspondance avec le type standard fonctionnellement equivalent le plus proche, tel que defini dans la reference PDF 9.7.4, dans le dictionnaire de cartographie des roles de la racine de l`arborescence.");
		fr = fr.replace(
				"All non-symbolic TrueType fonts shall have either MacRomanEncoding or WinAnsiEncoding as the value for the Encoding key in the Font dictionary or as the value for the BaseEncoding key in the dictionary that is the value of the Encoding key in the Font dictionary.",
				"Toutes les polices TrueType non symboliques doivent avoir soit MacRomanEncoding soit WinAnsiEncoding comme valeur de la cle Encoding dans le dictionnaire Font ou comme valeur de la cle BaseEncoding dans le dictionnaire qui est la valeur de la cle Encoding dans le dictionnaire Font.");
		fr = fr.replace(
				"All non-symbolic TrueType fonts shall specify MacRomanEncoding or WinAnsiEncoding, either as the value of the Encoding entry in the font dictionary or as the value of the BaseEncoding entry in the dictionary that is the value of the Encoding entry in the font dictionary.",
				"Toutes les polices TrueType non symboliques doivent specifier MacRomanEncoding ou WinAnsiEncoding, soit comme valeur de l`entree Encoding du dictionnaire de polices, soit comme valeur de l`entree BaseEncoding du dictionnaire qui est la valeur de l`entree Encoding du dictionnaire de polices.");
		fr = fr.replace(
				"All non-white-space characters in hexadecimal strings shall be in the range 0 to 9, A to F or a to f",
				"Tous les caracteres autres que les espaces blancs dans les chaines hexadecimales doivent etre compris entre 0 et 9, A et F ou a et f.");
		fr = fr.replace(
				"All properties specified in XMP form shall use either the predefined schemas defined in the XMP Specification, ISO 19005-1 or this part of ISO 19005, or any extension schemas that comply with 6.6.2.3.2",
				"Toutes les proprietes specifiees sous forme XMP doivent utiliser les schemas predefinis dans la specification XMP, la norme ISO 19005-1 ou la presente partie de la norme ISO 19005, ou tout schema d`extension conforme a l`article 6.6.2.3.2.");
		fr = fr.replace(
				"All Separation arrays within a single PDF/A-2 file (including those in Colorants dictionaries) that have the same name shall have the same tintTransform and alternateSpace.",
				"Tous les tableaux de separation d`un meme fichier PDF/A-2 (y compris ceux des dictionnaires de colorants) qui portent le meme nom doivent avoir les memes tintTransform et alternateSpace.");
		fr = fr.replace(
				"All standard stream filters listed in ISO 32000-1:2008, 7.4, Table 6 may be used, with the exception of LZWDecode.",
				"Tous les filtres de flux standard enumeres dans la norme ISO 32000-1:2008, 7.4, tableau 6, peuvent etre utilises, a l`exception de LZWDecode.");
		fr = fr.replace("All symbolic TrueType fonts shall not specify an Encoding entry in the font dictionary",
				"Toutes les polices TrueType symboliques ne doivent pas specifier d`entree Encoding dans le dictionnaire des polices.");
		fr = fr.replace("An annotation dictionary shall contain the F key.",
				"Un dictionnaire d`annotations doit contenir la cle F.");
		fr = fr.replace(
				"An annotation dictionary shall not contain the C array or the IC array unless the colour space of the DestOutputProfile in the PDF/A-1 OutputIntent dictionary, defined in 6.2.2, is RGB",
				"Un dictionnaire d`annotations ne doit pas contenir le tableau C ou le tableau IC, sauf si l`espace colorimetrique du DestOutputProfile dans le dictionnaire PDF/A-1 OutputIntent, defini au point 6.2.2, est RGB.");
		fr = fr.replace("An annotation dictionary shall not contain the CA key with a value other than 1.0",
				"Un dictionnaire d`annotations ne doit pas contenir la cle CA avec une valeur autre que 1.0.");
		fr = fr.replace("An ExtGState dictionary shall not contain the HTP key",
				"Un dictionnaire ExtGState ne doit pas contenir la cle HTP.");
		fr = fr.replace("An ExtGState dictionary shall not contain the TR key",
				"Un dictionnaire ExtGState ne doit pas contenir la cle TR.");
		fr = fr.replace("An ExtGState dictionary shall not contain the TR2 key with a value other than Default",
				"Le dictionnaire ExtGState ne doit pas contenir la cle TR2 avec une valeur autre que Default.");
		fr = fr.replace("An Image dictionary shall not contain the Alternates key",
				"Un dictionnaire d`images ne doit pas contenir la cle Alternates.");
		fr = fr.replace("An Image dictionary shall not contain the OPI key",
				"Un dictionnaire d`images ne doit pas contenir la cle OPI.");
		fr = fr.replace("An XObject dictionary (Image or Form) shall not contain the OPI key",
				"Un dictionnaire XObject (image ou formulaire) ne doit pas contenir la cle OPI.");
		fr = fr.replace("An XObject dictionary shall not contain the SMask key",
				"Un dictionnaire XObject ne doit pas contenir la cle SMask.");
		fr = fr.replace("Annotation types not defined in ISO 32000-1 shall not be permitted.",
				"Les types d`annotation non definis dans la norme ISO 32000-1 ne sont pas autorises.");
		fr = fr.replace("Annotation types not defined in PDF Reference shall not be permitted.",
				"Les types d`annotation non definis dans la reference PDF ne sont pas autorises.");
		fr = fr.replace("As of PDF 1.4, N must be 1, 3, or 4",
				"a partir de la version 1.4 du PDF, N doit etre egal a 1, 3 ou 4.");
		fr = fr.replace("At minimum, it shall include the signers X.509 signing certificate",
				"Au minimum, il doit inclure le certificat de signature X.509 du signataire.");
		fr = fr.replace(
				"At minimum, there shall only be a single signer (e.g. a single SignerInfo structure) in the PDF Signature",
				"Au minimum, il ne doit y avoir qu`un seul signataire (par exemple, une seule structure SignerInfo) dans la signature PDF.");
		fr = fr.replace("BaseFont - name - (Required) The PostScript name of the font",
				"BaseFont - name - (obligatoire) Le nom PostScript de la police.");
		fr = fr.replace("Compression and whether or not an object is direct or indirect shall be ignored",
				"La compression et le fait qu`un objet soit direct ou indirect sont ignores.");
		fr = fr.replace(
				"Content streams shall not contain any operators not defined in ISO 32000-1 even if such operators are bracketed by the BX/EX compatibility operators",
				"Les flux de contenu ne doivent pas contenir d`operateurs non definis dans la norme ISO 32000-1, meme si ces operateurs sont mis entre parentheses par les operateurs de compatibilite BX/EX.");
		fr = fr.replace("dc:creator shall contain exactly one entry",
				"dc:creator doit contenir exactement une entree.");
		fr = fr.replace(
				"DeviceCMYK may be used only if the file has a PDF/A-1 OutputIntent that uses a CMYK colour space",
				"DeviceCMYK ne peut etre utilise que si le fichier a un PDF/A-1 OutputIntent qui utilise un espace couleur CMYK.");
		fr = fr.replace(
				"DeviceCMYK shall only be used if a device independent DefaultCMYK colour space has been set or if a DeviceN-based DefaultCMYK colour space has been set when the DeviceCMYK colour space is used or the file has a PDF/A OutputIntent that contains a CMYK destination profile",
				"DeviceCMYK n`est utilise que si un espace couleur DefaultCMYK independant du peripherique a ete defini ou si un espace couleur DefaultCMYK base sur DeviceN a ete defini lorsque l`espace couleur DeviceCMYK est utilise ou si le fichier a un PDF/A OutputIntent qui contient un profil de destination CMYK.");
		fr = fr.replace(
				"DeviceGray shall only be used if a device independent DefaultGray colour space has been set when the DeviceGray colour space is used, or if a PDF/A OutputIntent is present",
				"DeviceGray ne doit etre utilise que si un espace colorimetrique DefaultGray independant du peripherique a ete defini lorsque l`espace colorimetrique DeviceGray est utilise, ou si un PDF/A OutputIntent est present.");
		fr = fr.replace(
				"DeviceRGB may be used only if the file has a PDF/A-1 OutputIntent that uses an RGB colour space",
				"DeviceRGB ne peut etre utilise que si le fichier possede un PDF/A-1 OutputIntent qui utilise un espace colorimetrique RVB.");
		fr = fr.replace(
				"DeviceRGB shall only be used if a device independent DefaultRGB colour space has been set when the DeviceRGB colour space is used, or if the file has a PDF/A OutputIntent that contains an RGB destination profile",
				"DeviceRGB n`est utilise que si un espace colorimetrique DefaultRGB independant du peripherique a ete defini lorsque l`espace colorimetrique DeviceRGB est utilise, ou si le fichier possede un PDF/A OutputIntent qui contient un profil de destination RVB.");
		fr = fr.replace(
				"Each optional content configuration dictionary shall contain the Name key, whose value shall be unique amongst all optional content configuration dictionaries within the PDF/A-2 file",
				"Chaque dictionnaire de configuration de contenu facultatif doit contenir la cle Name, dont la valeur doit etre unique parmi tous les dictionnaires de configuration de contenu facultatif du fichier PDF/A-2.");
		fr = fr.replace(
				"Each optional content configuration dictionary that forms the value of the D key, or that is an element in the array that forms the value of the Configs key in the OCProperties dictionary, shall contain the Name key",
				"Chaque dictionnaire de configuration de contenu facultatif qui constitue la valeur de la cle D ou qui est un element du tableau qui constitue la valeur de la cle Configs dans le dictionnaire OCProperties doit contenir la cle Name.");
		fr = fr.replace(
				"Embedded font programs shall define all font glyphs referenced for rendering with conforming file",
				"Les programmes de polices integrees doivent definir tous les glyphes de polices references pour le rendu avec un fichier conforme.");
		fr = fr.replace("Embedded fonts shall define all glyphs referenced for rendering within the conforming file.",
				"Les polices integrees doivent definir tous les glyphes references pour le rendu dans le fichier conforme.");
		fr = fr.replace(
				"Every annotation (including those whose Subtype value is Widget, as used for form fields), except for the two cases listed below, shall have at least one appearance dictionary: - annotations where the value of the Rect key consists of an array where value 1 is equal to value 3 and value 2 is equal to value 4; - annotations whose Subtype value is Popup or Link",
				"Chaque annotation (y compris celles dont la valeur de sous-type est Widget, telle qu`utilisee pour les champs de formulaire), a l`exception des deux cas enumeres ci-dessous, doit avoir au moins un dictionnaire d`apparence : - les annotations où la valeur de la cle Rect consiste en un tableau où la valeur 1 est egale a la valeur 3 et la valeur 2 est egale a la valeur 4 ; - les annotations dont la valeur de sous-type est Popup ou Link.");
		fr = fr.replace("Every form field shall have an appearance dictionary associated with the fields data",
				"Chaque champ de formulaire doit avoir un dictionnaire d`apparence associe aux donnees du champ.");
		fr = fr.replace(
				"Except for annotation dictionaries whose Subtype value is Popup, all annotation dictionaries shall contain the F key",
				"a l`exception des dictionnaires d`annotations dont la valeur du sous-type est Popup, tous les dictionnaires d`annotations doivent contenir la cle F.");
		fr = fr.replace(
				"Extension schema descriptions shall be specified using the PDF/A extension schema description schema defined in this clause",
				"Les descriptions des schemas d`extension doivent etre specifiees a l`aide du schema de description des schemas d`extension PDF/A defini dans la presente clause.");
		fr = fr.replace(
				"Extension schemas shall be specified using the PDF/A extension schema container schema defined in 6.6.2.3.3.",
				"Les schemas d`extension doivent etre specifies a l`aide du schema de conteneur de schema d`extension PDF/A defini a l`article 6.6.2.3.3.");
		fr = fr.replace(
				"Field category of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text, value external or internal and namespace prefix pdfaProperty",
				"Le champ category du type de valeur PDF/A Property du schema d`extension PDF/A doit etre present et avoir le type Text, la valeur external ou internal et le prefixe d`espace de noms pdfaProperty.");
		fr = fr.replace(
				"Field description of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField",
				"Le champ description du type de valeur Field du schema d`extension PDF/A doit etre de type Text et porter le prefixe d`espace de noms pdfaField.");
		fr = fr.replace(
				"Field description of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaProperty",
				"Le champ description du type de valeur PDF/A Property dans le schema d`extension PDF/A doit etre present et avoir le type Text et le prefixe d`espace de noms pdfaProperty.");
		fr = fr.replace(
				"Field description of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaType",
				"Le champ description du type de valeur PDF/A ValueType dans le schema d`extension PDF/A doit etre present et doit avoir le type Text et le prefixe d`espace de noms pdfaType.");
		fr = fr.replace(
				"Field field of the PDF/A ValueType value type in the PDF/A extension schema shall have type Seq Field and namespace prefix pdfaType",
				"Le champ field du type de valeur PDF/A ValueType dans le schema d`extension PDF/A doit etre de type Seq Field et porter le prefixe d`espace de noms pdfaType.");
		fr = fr.replace(
				"Field name of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField",
				"Field `name` of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField (Champ nom du type de valeur PDF/A Field dans le schema d`extension PDF/A).");
		fr = fr.replace(
				"Field name of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaProperty",
				"Le champ name du type de valeur PDF/A Property dans le schema d`extension PDF/A doit etre present et doit avoir le type Text et le prefixe d`espace de noms pdfaProperty.");
		fr = fr.replace(
				"Field namespaceURI of the PDF/A Schema value type in the PDF/A extension schema shall be present and shall have type URI and namespace prefix pdfaSchema",
				"Le champ namespaceURI du type de valeur PDF/A Schema du schema d`extension PDF/A doit etre present et doit avoir le type URI et le prefixe d`espace de noms pdfaSchema.");
		fr = fr.replace(
				"Field namespaceURI of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type URI and namespace prefix pdfaType",
				"Le champ namespaceURI du type de valeur PDF/A ValueType du schema d`extension PDF/A doit etre present et doit avoir le type URI et le prefixe d`espace de noms pdfaType.");
		fr = fr.replace(
				"Field prefix of the PDF/A Schema value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaSchema",
				"Le champ prefix du type de valeur PDF/A Schema du schema d`extension PDF/A doit etre present et doit avoir le type Text et le prefixe d`espace de noms pdfaSchema.");
		fr = fr.replace(
				"Field prefix of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaType",
				"Le champ prefix du type de valeur PDF/A ValueType du schema d`extension PDF/A doit etre present et doit avoir le type Text et le prefixe d`espace de noms pdfaType.");
		fr = fr.replace(
				"Field property of the PDF/A Schema value type in the PDF/A extension schema shall have type Seq Property and namespace prefix pdfaSchema",
				"Le champ property du type de valeur PDF/A Schema du schema d`extension PDF/A doit etre de type Seq Property et porter le prefixe d`espace de noms pdfaSchema.");
		fr = fr.replace(
				"Field schema of the PDF/A Schema value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaSchema",
				"Le champ schema du type de valeur PDF/A Schema dans le schema d`extension PDF/A doit etre present et avoir le type Text et le prefixe d`espace de noms pdfaSchema.");
		fr = fr.replace(
				"Field type of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaType",
				"Le champ type du type de valeur PDF/A ValueType du schema d`extension PDF/A doit etre present et doit avoir le type Text et le prefixe d`espace de noms pdfaType.");
		fr = fr.replace(
				"Field valueType of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField.",
				"Le champ valueType du type de valeur PDF/A Field du schema d`extension PDF/A doit etre de type Text et porter le prefixe d`espace de noms pdfaField.");
		fr = fr.replace(
				"Field valueType of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaProperty.",
				"Le champ valueType du type de valeur PDF/A Property du schema d`extension PDF/A doit etre present et avoir le type Text et le prefixe d`espace de noms pdfaProperty.");
		fr = fr.replace(
				"Field valueType of the PDF/A Schema value type in the PDF/A extension schema shall have type Seq ValueType and namespace prefix pdfaSchema",
				"Le champ valueType du type de valeur PDF/A Schema du schema d`extension PDF/A doit etre de type Seq ValueType et porter le prefixe d`espace de noms pdfaSchema.");
		fr = fr.replace("Filters that are not listed in ISO 32000-1:2008, 7.4, Table 6 shall not be used",
				"Les filtres qui ne figurent pas dans la norme ISO 32000-1:2008, 7.4, tableau 6, ne doivent pas etre utilises.");
		fr = fr.replace(
				"FirstChar - integer - (Required except for the standard 14 fonts) The first character code defined in the fonts Widths array",
				"FirstChar - integer - (obligatoire sauf pour les polices standard 14) Le premier code de caractere defini dans le tableau Widths de la police.");
		fr = fr.replace(
				"Font names, names of colourants in Separation and DeviceN colour spaces, and structure type names, after expansion of character sequences escaped with a NUMBER SIGN (23h), if any, shall be valid UTF-8 character sequences",
				"Les noms de polices, les noms de colorants dans les espaces colorimetriques Separation et DeviceN, et les noms de types de structures, apres expansion des sequences de caracteres echappees par un SIGNE NUMeRIQUE (23h), le cas echeant, doivent etre des sequences de caracteres UTF-8 valides.");
		fr = fr.replace("Font programs cmap tables for all symbolic TrueType fonts shall contain exactly one encoding",
				"Les tables cmap des programmes de polices pour toutes les polices symboliques TrueType doivent contenir exactement un encodage.");
		fr = fr.replace(
				"For all annotation dictionaries containing an AP key, the appearance dictionary that it defines as its value shall contain only the N key",
				"Pour tous les dictionnaires d`annotations contenant une cle AP, le dictionnaire d`apparence qu`il definit comme sa valeur ne doit contenir que la cle N.");
		fr = fr.replace(
				"For all CIDFont subsets referenced within a conforming file, the font descriptor dictionary shall include a CIDSet stream identifying which CIDs are present in the embedded CIDFont file, as described in PDF Reference Table 5.20",
				"Pour tous les sous-ensembles CIDFont references dans un fichier conforme, le dictionnaire des descripteurs de police doit inclure un flux CIDSet identifiant les CID presents dans le fichier CIDFont integre, comme decrit dans le tableau de reference PDF 5.20.");
		fr = fr.replace(
				"For all non-symbolic TrueType fonts used for rendering, the embedded TrueType font program shall contain one or several non-symbolic cmap entries such that all necessary glyph lookups can be carried out",
				"Pour toutes les polices TrueType non symboliques utilisees pour le rendu, le programme de police TrueType integre doit contenir une ou plusieurs entrees cmap non symboliques afin que toutes les recherches de glyphes necessaires puissent etre effectuees.");
		fr = fr.replace(
				"For all Type 1 font subsets referenced within a conforming file, the font descriptor dictionary shall include a CharSet string listing the character names defined in the font subset, as described in PDF Reference Table 5.18",
				"Pour tous les sous-ensembles de polices de type 1 references dans un fichier conforme, le dictionnaire des descripteurs de polices doit inclure une chaine CharSet enumerant les noms de caracteres definis dans le sous-ensemble de polices, comme decrit dans le tableau de reference PDF 5.18.");
		fr = fr.replace(
				"For all Type 2 CIDFonts that are used for rendering, the CIDFont dictionary shall contain a CIDToGIDMap entry that shall be a stream mapping from CIDs to glyph indices or the name Identity, as described in PDF Reference Table 5.13",
				"Pour toutes les polices CIDFont de type 2 utilisees pour le rendu, le dictionnaire CIDFont doit contenir une entree CIDToGIDMap qui doit etre une correspondance de flux entre les CID et les indices de glyphes ou le nom Identity, comme decrit dans le tableau de reference PDF 5.13.");
		fr = fr.replace("For an inline image, the I key shall have a value of false",
				"Pour une image en ligne, la cle I doit avoir la valeur false.");
		fr = fr.replace(
				"For any character, regardless of its rendering mode, that is mapped to a code or codes in the Unicode Private Use Area (PUA), an ActualText entry as described in ISO 32000-1:2008, 14.9.4 shall be present for this character or a sequence of characters of which such a character is a part",
				"Pour tout caractere, quel que soit son mode de rendu, qui est mis en correspondance avec un ou plusieurs codes dans la zone d`utilisation privee d`Unicode (PUA), une entree ActualText telle que decrite dans la norme ISO 32000-1:2008, 14.9.4 doit etre presente pour ce caractere ou une sequence de caracteres dont ce caractere fait partie.");
		fr = fr.replace(
				"For any given composite (Type 0) font referenced within a conforming file, the CIDSystemInfo entries of its CIDFont and CMap dictionaries shall be compatible.",
				"Pour toute police composite (type 0) referencee dans un fichier conforme, les entrees CIDSystemInfo de ses dictionnaires CIDFont et CMap doivent etre compatibles.");
		fr = fr.replace(
				"For any given composite (Type 0) font within a conforming file, the CIDSystemInfo entry in its CIDFont dictionary and its Encoding dictionary shall have the following relationship: - If the Encoding key in the Type 0 font dictionary is Identity-H or Identity-V, any values of Registry, Ordering, and Supplement may be used in the CIDSystemInfo entry of the CIDFont.",
				"Pour toute police composite (de type 0) donnee dans un fichier conforme, l`entree CIDSystemInfo de son dictionnaire CIDFont et son dictionnaire Encoding doivent avoir la relation suivante : - Si la cle Encoding du dictionnaire des polices de type 0 est Identity-H ou Identity-V, toutes les valeurs de Registry, Ordering et Supplement peuvent etre utilisees dans l`entree CIDSystemInfo du CIDFont.");
		fr = fr.replace(
				"For any spot colour used in a DeviceN or NChannel colour space, an entry in the Colorants dictionary shall be present",
				"Pour toute couleur d`accompagnement utilisee dans un espace couleur DeviceN ou NChannel, une entree du dictionnaire Colorants doit etre presente.");
		fr = fr.replace(
				"For every font embedded in a conforming file and used for rendering, the glyph width information in the font dictionary and in the embedded font program shall be consistent",
				"Pour chaque police integree dans un fichier conforme et utilisee pour le rendu, les informations relatives a la largeur des glyphes dans le dictionnaire des polices et dans le programme des polices integrees doivent etre coherentes.");
		fr = fr.replace(
				"For those CMaps that are embedded, the integer value of the WMode entry in the CMap dictionary shall be identical to the WMode value in the embedded CMap stream",
				"Pour les CMap integrees, la valeur entiere de l`entree WMode dans le dictionnaire CMap doit etre identique a la valeur WMode dans le flux CMap integre.");
		fr = fr.replace("Halftones in a conforming PDF/A-2 file shall not contain a HalftoneName key",
				"Les demi-tons d`un fichier PDF/A-2 conforme ne doivent pas contenir de cle HalftoneName.");
		fr = fr.replace("Hexadecimal strings shall contain an even number of non-white-space characters",
				"Les chaines hexadecimales doivent contenir un nombre pair de caracteres sans espace blanc.");
		fr = fr.replace(
				"If a files OutputIntents array contains more than one entry, as might be the case where a file is compliant with this part of ISO 19005 and at the same time with PDF/X-4 or PDF/E-1, then all entries that contain a DestOutputProfile key shall have as the value of that key the same indirect object, which shall be a valid ICC profile stream",
				"Si le tableau OutputIntents d`un fichier contient plus d`une entree, comme cela peut etre le cas lorsqu`un fichier est conforme a cette partie de la norme ISO 19005 et en meme temps a la norme PDF/X-4 ou PDF/E-1, toutes les entrees qui contiennent une cle DestOutputProfile doivent avoir comme valeur de cette cle le meme objet indirect, qui doit etre un flux de profil ICC valide.");
		fr = fr.replace(
				"If a files OutputIntents array contains more than one entry, then all entries that contain a DestOutputProfile key shall have as the value of that key the same indirect object, which shall be a valid ICC profile stream",
				"Si le tableau OutputIntents d`un fichier contient plus d`une entree, toutes les entrees qui contiennent une cle DestOutputProfile doivent avoir comme valeur de cette cle le meme objet indirect, qui doit etre un flux de profil ICC valide.");
		fr = fr.replace("If a ca key is present in an ExtGState object, its value shall be 1.0",
				"Si une cle ca est presente dans un objet ExtGState, sa valeur doit etre 1.0.");
		fr = fr.replace(
				"If an annotation dictionarys Subtype key has a value of Widget and its FT key has a value of Btn, the value of the N key shall be an appearance subdictionary",
				"Si la cle Subtype d`un dictionnaire d`annotations a pour valeur Widget et que sa cle FT a pour valeur Btn, la valeur de la cle N doit etre un sous-dictionnaire d`apparence.");
		fr = fr.replace(
				"If an annotation dictionarys Subtype key has value other than Widget, or if FT key associated with Widget annotation has value other than Btn, the value of the N key shall be an appearance stream",
				"Si la cle de sous-type d`un dictionnaire d`annotations a une valeur autre que Widget, ou si la cle FT associee a l`annotation Widget a une valeur autre que Btn, la valeur de la cle N est un flux d`apparence.");
		fr = fr.replace("If an Image dictionary contains the BitsPerComponent key, its value shall be 1, 2, 4 or 8",
				"Si un dictionnaire d`images contient la cle BitsPerComponent, sa valeur doit etre 1, 2, 4 ou 8.");
		fr = fr.replace("If an Image dictionary contains the BitsPerComponent key, its value shall be 1, 2, 4, 8 or 16",
				"Si un dictionnaire d`images contient la cle BitsPerComponent, sa valeur doit etre 1, 2, 4, 8 ou 16.");
		fr = fr.replace("If an Image dictionary contains the Interpolate key, its value shall be false",
				"Si un dictionnaire d`images contient la cle Interpolate, sa valeur doit etre false.");
		fr = fr.replace("If an Image dictionary contains the Interpolate key, its value shall be false.",
				"Si un dictionnaire d`images contient la cle Interpolate, sa valeur doit etre false.");
		fr = fr.replace(
				"If an optional content configuration dictionary contains the Order key, the array which is the value of this Order key shall contain references to all OCGs in the conforming file",
				"Si un dictionnaire de configuration de contenu optionnel contient la cle Order, le tableau correspondant a la valeur de cette cle Order doit contenir des references a tous les BCG du fichier conforme.");
		fr = fr.replace("If an SMask key appears in an ExtGState dictionary, its value shall be None",
				"Si une cle SMask apparait dans un dictionnaire ExtGState, sa valeur doit etre None.");
		fr = fr.replace(
				"If an uncalibrated colour space is used in a file then that file shall contain a PDF/A-1 OutputIntent, as defined in 6.2.2",
				"Si un espace couleur non calibre est utilise dans un fichier, celui-ci doit contenir un PDF/A-1 OutputIntent, tel que defini au point 6.2.2.");
		fr = fr.replace(
				"If DocMDP is present, then the Signature References dictionary (ISO 32000-1:2008, 12.8.1, Table 253) shall not contain the keys DigestLocation, DigestMethod, and DigestValue",
				"Si DocMDP est present, le dictionnaire Signature References (ISO 32000-1:2008, 12.8.1, Table 253) ne doit pas contenir les cles DigestLocation, DigestMethod et DigestValue.");
		fr = fr.replace(
				"If present, the F keys Print flag bit shall be set to 1 and its Hidden, Invisible, ToggleNoView, and NoView flag bits shall be set to 0",
				"S`il est present, le bit d`indicateur d`impression de la cle F doit etre mis a 1 et ses bits d`indicateurs Hidden, Invisible, ToggleNoView et NoView doivent etre mis a 0.");
		fr = fr.replace(
				"If the document does not contain a PDF/A OutputIntent, then all Page objects that contain transparency shall include the Group key, and the attribute dictionary that forms the value of that Group key shall include a CS entry whose value shall be used as the default blending colour space",
				"Si le document ne contient pas de PDF/A OutputIntent, tous les objets Page qui contiennent de la transparence doivent inclure la cle Group et le dictionnaire d`attributs qui forme la valeur de cette cle Group doit inclure une entree CS dont la valeur doit etre utilisee comme espace colorimetrique de melange par defaut.");
		fr = fr.replace(
				"If the FontDescriptor dictionary of an embedded CID font contains a CIDSet stream, then it shall identify all CIDs which are present in the font program, regardless of whether a CID in the font is referenced or used by the PDF or not",
				"Si le dictionnaire FontDescriptor d`une police CID integree contient un flux CIDSet, il doit identifier tous les CID presents dans le programme de police, qu`un CID de la police soit reference ou utilise par le PDF ou non.");
		fr = fr.replace(
				"If the FontDescriptor dictionary of an embedded Type 1 font contains a CharSet string, then it shall list the character names of all glyphs present in the font program, regardless of whether a glyph in the font is referenced or used by the PDF or not",
				"Si le dictionnaire FontDescriptor d`une police de type 1 integree contient une chaine CharSet, il repertorie les noms de caracteres de tous les glyphes presents dans le programme de police, qu`un glyphe de la police soit reference ou utilise par le PDF ou non.");
		fr = fr.replace(
				"If the Lang entry is present in the document catalog dictionary or in a structure element dictionary or property list, its value shall be a language identifier as defined by RFC 1766, Tags for the Identification of Languages, as described in PDF Reference 9.8.1",
				"Si l`entree Lang est presente dans le dictionnaire du catalogue de documents ou dans un dictionnaire d`elements de structure ou une liste de proprietes, sa valeur doit etre un identifiant de langue tel que defini par la RFC 1766, Tags for the Identification of Languages, comme decrit dans la reference PDF 9.8.1.");
		fr = fr.replace(
				"If the Lang entry is present in the documents Catalog dictionary or in a structure element dictionary or property list, its value shall be a language identifier as described in ISO 32000-1:2008, 14.9.2.",
				"Si l`entree Lang est presente dans le dictionnaire du catalogue du document ou dans un dictionnaire d`elements de structure ou une liste de proprietes, sa valeur doit etre un identifiant de langue tel que decrit dans la norme ISO 32000-1:2008, 14.9.2.");
		fr = fr.replace(
				"If the number of colour space specifications in the JPEG2000 data is greater than 1, there shall be exactly one colour space specification that has the value 0x01 in the APPROX field",
				"Si le nombre de specifications d`espace couleur dans les donnees JPEG2000 est superieur a 1, il doit y avoir exactement une specification d`espace couleur ayant la valeur 0x01 dans le champ APPROX.");
		fr = fr.replace("If the value of the Encoding entry is a dictionary, it shall not contain a Differences entry",
				"Si la valeur de l`entree Encoding est un dictionnaire, elle ne doit pas contenir d`entree Differences.");
		fr = fr.replace(
				"In a cross reference subsection header the starting object number and the range shall be separated by a single SPACE character (20h)",
				"Dans un en-tete de sous-section de reference croisee, le numero de l`objet de depart et la gamme doivent etre separes par un seul caractere ESPACE (20h).");
		fr = fr.replace(
				"In a linearized PDF, if the ID keyword is present in both the first page trailer dictionary and the last trailer dictionary, the value to both instances of the ID keyword shall be identical",
				"Dans un PDF linearise, si le mot-cle ID est present a la fois dans le dictionnaire de la premiere page et dans celui de la derniere page, la valeur des deux instances du mot-cle ID doit etre identique.");
		fr = fr.replace(
				"In addition, all non-symbolic TrueType fonts shall not define a Differences array unless all of the glyph names in the Differences array are listed in the Adobe Glyph List and the embedded font program contains at least the Microsoft Unicode (3,1 – Platform ID = 3, Encoding ID = 1) encoding in the cmap table",
				"En outre, toutes les polices TrueType non symboliques ne doivent pas definir de tableau de differences a moins que tous les noms de glyphes du tableau de differences ne figurent dans la liste des glyphes d`Adobe et que le programme de police integre contienne au moins l`encodage Microsoft Unicode (3,1 - Platform ID = 3, Encoding ID = 1) dans la table cmap.");
		fr = fr.replace(
				"In addition, the Crypt filter shall not be used unless the value of the Name key in the decode parameters dictionary is Identity.",
				"En outre, le filtre Crypt ne doit pas etre utilise a moins que la valeur de la cle Name dans le dictionnaire des parametres de decodage ne soit Identity.");
		fr = fr.replace(
				"In addition, the DestOutputProfileRef key, as defined in ISO 15930-7:2010, Annex A, shall not be present in any PDF/X OutputIntent",
				"En outre, la cle DestOutputProfileRef, telle que definie dans la norme ISO 15930-7:2010, annexe A, ne doit pas etre presente dans un PDF/X OutputIntent.");
		fr = fr.replace(
				"In all cases for TrueType fonts that are to be rendered, character codes shall be able to be mapped to glyphs according to ISO 32000-1:2008, 9.6.6.4 without the use of a non-standard mapping chosen by the conforming processor",
				"Dans tous les cas, pour les polices TrueType qui doivent etre rendues, les codes de caracteres doivent pouvoir etre mis en correspondance avec les glyphes conformement a la norme ISO 32000-1:2008, 9.6.6.4, sans recours a une mise en correspondance non standard choisie par le processeur conforme.");
		fr = fr.replace(
				"In evaluating equivalence, the PDF objects shall be compared, rather than the computational result of the use of those PDF objects.",
				"Lors de l`evaluation de l`equivalence, les objets PDF doivent etre compares, plutot que le resultat informatique de l`utilisation de ces objets PDF.");
		fr = fr.replace(
				"In other words, the Registry and Ordering strings of the CIDSystemInfo dictionaries for that font shall be identical, unless the value of the Encoding key in the font dictionary is Identity-H or Identity-V",
				"En d`autres termes, les chaines Registry et Ordering des dictionnaires CIDSystemInfo pour cette police doivent etre identiques, sauf si la valeur de la cle Encoding dans le dictionnaire de police est Identity-H ou Identity-V.");
		fr = fr.replace("Interactive form fields shall not perform actions of any type",
				"Les champs de formulaire interactifs ne doivent pas effectuer d`actions de quelque type que ce soit.");
		fr = fr.replace(
				"ISO 32000-1:2008, 9.7.4, Table 117 requires that all embedded Type 2 CIDFonts in the CIDFont dictionary shall contain a CIDToGIDMap entry that shall be a stream mapping from CIDs to glyph indices or the name Identity, as described in ISO 32000-1:2008, 9.7.4, Table 117",
				"La norme ISO 32000-1:2008, 9.7.4, tableau 117, exige que toutes les polices CIDFont de type 2 integrees dans le dictionnaire CIDFont contiennent une entree CIDToGIDMap qui doit etre une correspondance de flux entre les CID et les indices de glyphes ou le nom Identity, comme decrit dans la norme ISO 32000-1:2008, 9.7.4, tableau 117.");
		fr = fr.replace("JPEG2000 enumerated colour space 19 (CIEJab) shall not be used",
				"L`espace couleur enumere JPEG2000 19 (CIEJab) ne doit pas etre utilise.");
		fr = fr.replace("Largest Integer value is 2,147,483,647.", "La plus grande valeur entiere est 2 147 483 647.");
		fr = fr.replace(
				"LastChar - integer - (Required except for the standard 14 fonts) The last character code defined in the fonts Widths array",
				"LastChar - entier - (obligatoire sauf pour les polices standard 14) Le dernier code de caractere defini dans le tableau Widths de la police.");
		fr = fr.replace("Maximum capacity of a dictionary (in entries) is 4095",
				"La capacite maximale d`un dictionnaire (en entrees) est de 4095.");
		fr = fr.replace("Maximum capacity of an array (in elements) is 8191",
				"La capacite maximale d`un tableau (en elements) est de 8191.");
		fr = fr.replace("Maximum depth of graphics state nesting by q and Q operators is 28",
				"La profondeur maximale de l`imbrication des etats graphiques par les operateurs q et Q est de 28.");
		fr = fr.replace("Maximum length of a name (in bytes) is 127",
				"La longueur maximale d`un nom (en octets) est de 127.");
		fr = fr.replace("Maximum length of a string (in bytes) is 65535",
				"La longueur maximale d`une chaine (en octets) est de 65535.");
		fr = fr.replace("Maximum number of DeviceN components is 8",
				"Le nombre maximum de composants DeviceN est de 8.");
		fr = fr.replace("Maximum number of indirect objects in a PDF file is 8,388,607",
				"Le nombre maximal d`objets indirects dans un fichier PDF est de 8 388 607.");
		fr = fr.replace("Maximum value of a CID (character identifier) is 65,535",
				"La valeur maximale d`un CID (identifiant de caractere) est de 65 535.");
		fr = fr.replace("Metadata object stream dictionaries shall not contain the Filter key",
				"Les dictionnaires de flux d`objets de metadonnees ne doivent pas contenir la cle Filter.");
		fr = fr.replace("Named actions other than NextPage, PrevPage, FirstPage, and LastPage shall not be permitted",
				"Les actions nommees autres que NextPage, PrevPage, FirstPage et LastPage ne sont pas autorisees.");
		fr = fr.replace(
				"No data can follow the last end-of-file marker except a single optional end-of-line marker as described in ISO 32000-1:2008, 7.5.5",
				"Aucune donnee ne peut suivre le dernier marqueur de fin de fichier, a l`exception d`un seul marqueur de fin de ligne facultatif tel que decrit dans la norme ISO 32000-1:2008, 7.5.5.");
		fr = fr.replace("No data shall follow the last end-of-file marker except a single optional end-of-line marker",
				"Aucune donnee ne doit suivre le dernier marqueur de fin de fichier, a l`exception d`un seul marqueur de fin de ligne facultatif.");
		fr = fr.replace(
				"No keys other than UR3 and DocMDP shall be present in a permissions dictionary (ISO 32000-1:2008, 12.8.4, Table 258)",
				"Aucune cle autre que UR3 et DocMDP ne doit etre presente dans un dictionnaire de permissions (ISO 32000-1:2008, 12.8.4, tableau 258).");
		fr = fr.replace(
				"Only blend modes that are specified in ISO 32000-1:2008 shall be used for the value of the BM key in an extended graphic state dictionary",
				"Seuls les modes de fusion specifies dans l`ISO 32000-1:2008 doivent etre utilises pour la valeur de la cle BM dans un dictionnaire d`etats graphiques etendu.");
		fr = fr.replace(
				"Otherwise, the corresponding Registry and Ordering strings in both CIDSystemInfo dictionaries shall be identical, and the value of the Supplement key in the CIDSystemInfo dictionary of the CIDFont shall be less than or equal to the Supplement key in the CIDSystemInfo dictionary of the CMap",
				"Sinon, les chaines de registre et de commande correspondantes dans les deux dictionnaires CIDSystemInfo doivent etre identiques, et la valeur de la cle Supplement dans le dictionnaire CIDSystemInfo de la police CIDFont doit etre inferieure ou egale a la cle Supplement dans le dictionnaire CIDSystemInfo de la police CMap.");
		fr = fr.replace(
				"Overprint mode (as set by the OPM value in an ExtGState dictionary) shall not be one (1) when an ICCBased CMYK colour space is used for stroke and overprinting for stroke is set to true, or when ICCBased CMYK colour space is used for fill and overprinting for fill is set to true, or both",
				"Le mode de surimpression (defini par la valeur OPM dans un dictionnaire ExtGState) ne doit pas etre egal a un (1) lorsqu`un espace de couleur CMJN base sur l`ICC est utilise pour le trait et que la surimpression pour le trait est definie comme vraie, ou lorsque l`espace de couleur CMJN base sur l`ICC est utilise pour le remplissage et que la surimpression pour le remplissage est definie comme vraie, ou les deux.");
		fr = fr.replace("pdfaExtension:schemas - Bag Schema - Description of extension schemas",
				"pdfaExtension:schemas - Bag Schema - Description des schemas d`extension.");
		fr = fr.replace(
				"Properties specified in XMP form shall use either the predefined schemas defined in XMP Specification, or extension schemas that comply with XMP Specification",
				"Les proprietes specifiees sous forme XMP doivent utiliser soit les schemas predefinis definis dans la specification XMP, soit des schemas d`extension conformes a la specification XMP.");
		fr = fr.replace("Property amd of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"La propriete amd du schema d`identification PDF/A doit avoir le prefixe d`espace de noms pdfaid.");
		fr = fr.replace("Property conformance of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"La propriete conformance du schema d`identification PDF/A a pour prefixe d`espace de noms pdfaid.");
		fr = fr.replace("Property corr of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"La propriete corr du schema d`identification du PDF/A a pour prefixe d`espace de noms pdfaid.");
		fr = fr.replace("Property part of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"La propriete part du schema d`identification PDF/A doit avoir le prefixe d`espace de noms pdfaid.");
		fr = fr.replace("Smallest integer value is -2,147,483,648",
				"La plus petite valeur entiere est -2,147,483,648.");
		fr = fr.replace("Standard tags shall not be remapped to a non-standard type",
				"Les balises standard ne doivent pas etre reaffectees a un type non standard.");
		fr = fr.replace(
				"Subtype - name - (Required) The type of font; must be Type1 for Type 1 fonts, MMType1 for multiple master fonts, TrueType for TrueType fonts Type3 for Type 3 fonts, Type0 for Type 0 fonts and CIDFontType0 or CIDFontType2 for CID fonts",
				"Sous-type - nom - (obligatoire) Le type de police ; doit etre Type1 pour les polices de type 1, MMType1 pour les polices a maitres multiples, TrueType pour les polices TrueType, Type3 pour les polices de type 3, Type0 pour les polices de type 0 et CIDFontType0 ou CIDFontType2 pour les polices CID.");
		fr = fr.replace("Symbolic TrueType fonts shall not contain an Encoding entry in the font dictionary",
				"Les polices TrueType symboliques ne doivent pas contenir d`entree Encoding dans le dictionnaire des polices.");
		fr = fr.replace("The % character of the file header shall occur at byte offset 0 of the file.",
				"Le caractere % de l`en-tete du fichier doit se trouver au decalage d`octet 0 du fichier.");
		fr = fr.replace(
				"The aforementioned EOL marker shall be immediately followed by a % (25h) character followed by at least four bytes, each of whose encoded byte values shall have a decimal value greater than 127",
				"Le marqueur EOL susmentionne doit etre immediatement suivi d`un caractere % (25h) suivi d`au moins quatre octets, dont chacune des valeurs d`octet codees doit avoir une valeur decimale superieure a 127.");
		fr = fr.replace("The AS key shall not appear in any optional content configuration dictionary",
				"La cle AS ne doit apparaitre dans aucun dictionnaire de configuration de contenu facultatif.");
		fr = fr.replace("The bit-depth of the JPEG2000 data shall have a value in the range 1 to 38.",
				"La profondeur de bits des donnees JPEG2000 doit avoir une valeur comprise entre 1 et 38.");
		fr = fr.replace("The bytes attribute shall not be used in the header of an XMP packet",
				"L`attribut bytes ne doit pas etre utilise dans l`en-tete d`un paquet XMP.");
		fr = fr.replace(
				"The Catalog dictionary of a conforming file shall contain the Metadata key whose value is a metadata stream as defined in ISO 32000-1:2008, 14.3.2",
				"Le dictionnaire de catalogue d`un fichier conforme doit contenir la cle Metadata dont la valeur est un flux de metadonnees tel que defini dans la norme ISO 32000-1:2008, 14.3.2.");
		fr = fr.replace(
				"The cmap table in the embedded font program for a symbolic TrueType font shall contain either exactly one encoding or it shall contain, at least, the Microsoft Symbol (3,0 - Platform ID=3, Encoding ID=0) encoding",
				"La table cmap du programme de police integre pour une police TrueType symbolique doit contenir soit exactement un encodage, soit au moins l`encodage Microsoft Symbol (3,0 - Platform ID=3, Encoding ID=0).");
		fr = fr.replace("The document catalog dictionary of a conforming file shall contain the Metadata key",
				"Le dictionnaire du catalogue de documents d`un fichier conforme doit contenir la cle de metadonnees.");
		fr = fr.replace(
				"The document catalog dictionary shall include a MarkInfo dictionary containing an entry, Marked, whose value shall be true",
				"Le dictionnaire du catalogue de documents doit inclure un dictionnaire MarkInfo contenant une entree, Marked, dont la valeur doit etre true (vrai).");
		fr = fr.replace(
				"The document catalog dictionary shall include a MarkInfo dictionary with a Marked entry in it, whose value shall be true",
				"Le dictionnaire du catalogue de documents comprend un dictionnaire MarkInfo contenant une entree Marked, dont la valeur est true.");
		fr = fr.replace("The document catalog dictionary shall not contain a key with the name OCProperties",
				"Le dictionnaire de catalogue de documents ne doit pas contenir de cle portant le nom OCProperties.");
		fr = fr.replace(
				"The document catalog dictionary shall not include an AA entry for an additional-actions dictionary",
				"Le dictionnaire du catalogue de documents ne doit pas contenir d`entree AA pour un dictionnaire additional-actions.");
		fr = fr.replace("The document catalog shall not contain the Requirements key",
				"Le catalogue de documents ne doit pas contenir la cle Requirements.");
		fr = fr.replace("The documents Catalog shall not include an AA entry for an additional-actions dictionary",
				"Le catalogue du document ne doit pas contenir d`entree AA pour un dictionnaire d`actions supplementaires.");
		fr = fr.replace(
				"The documents interactive form dictionary that forms the value of the AcroForm key in the documents Catalog of a PDF/A-2 file, if present, shall not contain the XFA key",
				"Le dictionnaire de formulaires interactifs du document qui forme la valeur de la cle AcroForm dans le catalogue du document d`un fichier PDF/A-2, s`il est present, ne doit pas contenir la cle XFA.");
		fr = fr.replace("The encoding attribute shall not be used in the header of an XMP packet",
				"L`attribut encoding ne doit pas etre utilise dans l`en-tete d`un paquet XMP.");
		fr = fr.replace("The endstream keyword shall be preceded by an EOL marker",
				"Le mot-cle endstream doit etre precede d`un marqueur EOL.");
		fr = fr.replace(
				"The extension schema container schema uses the namespace URI http://www.aiim.org/pdfa/ns/extension/.",
				"Le schema de conteneur du schema d`extension utilise l`espace de noms URI http://www.aiim.org/pdfa/ns/extension/.");
		fr = fr.replace(
				"The F key’s Print flag bit shall be set to 1 and its Hidden, Invisible and NoView flag bits shall be set to 0",
				"Le bit d`indicateur d`impression de la cle F doit etre mis a 1 et ses bits d`indicateurs Hidden, Invisible et NoView doivent etre mis a 0.");
		fr = fr.replace(
				"The file header line shall be immediately followed by a comment consisting of a % character followed by at least four characters, each of whose encoded byte values shall have a decimal value greater than 127",
				"La ligne d`en-tete du fichier est immediatement suivie d`un commentaire compose d`un caractere % suivi d`au moins quatre caracteres, dont les valeurs codees en octets ont toutes une valeur decimale superieure a 127.");
		fr = fr.replace(
				"The file header shall begin at byte zero and shall consist of %PDF-1.n followed by a single EOL marker, where n is a single digit number between 0 (30h) and 7 (37h)",
				"L`en-tete du fichier commence a l`octet zero et se compose de %PDF-1.n suivi d`un seul marqueur EOL, où n est un nombre a un chiffre compris entre 0 (30h) et 7 (37h).");
		fr = fr.replace("The file specification dictionary for an embedded file shall contain the F and UF keys",
				"Le dictionnaire de specification de fichier pour un fichier incorpore doit contenir les cles F et UF.");
		fr = fr.replace(
				"The file trailer dictionary shall contain the ID keyword whose value shall be File Identifiers as defined in ISO 32000-1:2008, 14.4",
				"Le dictionnaire de fin de fichier doit contenir le mot-cle ID dont la valeur doit etre Identificateurs de fichiers tels que definis dans la norme ISO 32000-1:2008, 14.4.");
		fr = fr.replace("The file trailer dictionary shall contain the ID keyword.",
				"Le dictionnaire de la fin du fichier doit contenir le mot-cle ID.");
		fr = fr.replace(
				"The file trailer referred to is either the last trailer dictionary in a PDF file, as described in PDF Reference 3.4.4 and 3.4.5, or the first page trailer in a linearized PDF file, as described in PDF Reference F.2",
				"La fin de fichier a laquelle il est fait reference est soit le dernier dictionnaire de fin d`un fichier PDF, tel que decrit dans la reference PDF 3.4.4 et 3.4.5, soit la premiere page de fin d`un fichier PDF linearise, tel que decrit dans la reference PDF F.2.");
		fr = fr.replace(
				"The first line of a PDF file is a header identifying the version of the PDF specification to which the file conforms",
				"La premiere ligne d`un fichier PDF est un en-tete identifiant la version de la specification PDF a laquelle le fichier est conforme.");
		fr = fr.replace(
				"The following keys, if present in an ExtGState object, shall have the values shown: BM - Normal or Compatible",
				"Les cles suivantes, si elles sont presentes dans un objet ExtGState, doivent avoir les valeurs indiquees : BM - Normal ou Compatible.");
		fr = fr.replace("The following keys, if present in an ExtGState object, shall have the values shown: CA - 1.0",
				"Les cles suivantes, si elles sont presentes dans un objet ExtGState, ont les valeurs indiquees : CA - 1.0.");
		fr = fr.replace(
				"The Font dictionary of all fonts shall define the map of all used character codes to Unicode values, either via a ToUnicode entry, or other mechanisms as defined in ISO 19005-2, 6.2.11.7.2",
				"Le dictionnaire des polices de toutes les polices doit definir la correspondance entre tous les codes de caracteres utilises et les valeurs Unicode, soit par une entree ToUnicode, soit par d`autres mecanismes definis dans la norme ISO 19005-2, 6.2.11.7.2.");
		fr = fr.replace(
				"The font dictionary shall include a ToUnicode entry whose value is a CMap stream object that maps character codes to Unicode values, as described in PDF Reference 5.9, unless the font meets any of the following three conditions: (*) fonts that use the predefined encodings MacRomanEncoding, MacExpertEncoding or WinAnsiEncoding, or that use the predefined Identity-H or Identity-V CMaps; (*) Type 1 fonts whose character names are taken from the Adobe standard Latin character set or the set of named characters in the Symbol font, as defined in PDF Reference Appendix D; (*) Type 0 fonts whose descendant CIDFont uses the Adobe-GB1, Adobe-CNS1, Adobe-Japan1 or Adobe-Korea1 character collections",
				"Le dictionnaire de polices doit inclure une entree ToUnicode dont la valeur est un objet de flux CMap qui etablit une correspondance entre les codes de caracteres et les valeurs Unicode, comme decrit dans la reference PDF 5. 9, a moins que la police ne reponde a l`une des trois conditions suivantes : (*) les polices qui utilisent les encodages predefinis MacRomanEncoding, MacExpertEncoding ou WinAnsiEncoding, ou qui utilisent les CMaps Identity-H ou Identity-V predefinis ; (*) les polices de type 1 dont les noms de caracteres proviennent du jeu de caracteres latins standard d`Adobe ou du jeu de caracteres nommes de la police Symbol, tel que defini dans l`annexe D de la reference PDF ; (*) les polices de type 0 dont le descendant CIDFont utilise les collections de caracteres Adobe-GB1, Adobe-CNS1, Adobe-Japan1 ou Adobe-Korea1.");
		fr = fr.replace(
				"The font programs for all fonts used for rendering within a conforming file shall be embedded within that file, as defined in ISO 32000-1:2008, 9.9",
				"Les programmes de polices de toutes les polices utilisees pour le rendu dans un fichier conforme doivent etre integres dans ce fichier, comme defini dans la norme ISO 32000-1:2008, 9.9.");
		fr = fr.replace(
				"The font programs for all fonts used within a conforming file shall be embedded within that file, as defined in PDF Reference 5.8, except when the fonts are used exclusively with text rendering mode 3",
				"Les programmes de polices de toutes les polices utilisees dans un fichier conforme doivent etre incorpores dans ce fichier, comme defini dans PDF Reference 5.8, sauf lorsque les polices sont utilisees exclusivement avec le mode de rendu de texte 3.");
		fr = fr.replace("The generation number and obj keyword shall be separated by a single white-space character.",
				"Le numero de generation et le mot-cle obj doivent etre separes par un seul caractere d`espacement.");
		fr = fr.replace("The Hide action shall not be permitted (Corrigendum 2)",
				"L`action Hide n`est pas autorisee (Corrigendum 2).");
		fr = fr.replace("The keyword Encrypt shall not be used in the trailer dictionary",
				"Le mot-cle Encrypt ne doit pas etre utilise dans le dictionnaire des bandes-annonces.");
		fr = fr.replace(
				"The Launch, Sound, Movie, ResetForm, ImportData and JavaScript actions shall not be permitted.",
				"Les actions Launch, Sound, Movie, ResetForm, ImportData et JavaScript ne sont pas autorisees.");
		fr = fr.replace(
				"The Launch, Sound, Movie, ResetForm, ImportData, Hide, SetOCGState, Rendition, Trans, GoTo3DView and JavaScript actions shall not be permitted.",
				"Les actions Launch, Sound, Movie, ResetForm, ImportData, Hide, SetOCGState, Rendition, Trans, GoTo3DView et JavaScript ne sont pas autorisees.");
		fr = fr.replace(
				"The logical structure of the conforming file shall be described by a structure hierarchy rooted in the StructTreeRoot entry of the document catalog dictionary, as described in PDF Reference 9.6",
				"La structure logique du fichier conforme doit etre decrite par une hierarchie de structures ayant pour racine l`entree StructTreeRoot du dictionnaire du catalogue de documents, comme decrit dans la reference PDF 9.6.");
		fr = fr.replace(
				"The logical structure of the conforming file shall be described by a structure hierarchy rooted in the StructTreeRoot entry of the documents Catalog dictionary, as described in ISO 32000-1:2008, 14.7",
				"La structure logique du fichier conforme doit etre decrite par une hierarchie de structure ancree dans l`entree StructTreeRoot du dictionnaire du catalogue du document, comme decrit dans la norme ISO 32000-1:2008, 14.7.");
		fr = fr.replace("The LZWDecode filter shall not be permitted", "Le filtre LZWDecode n`est pas autorise.");
		fr = fr.replace(
				"The metadata stream shall conform to XMP Specification and well formed PDFAExtension Schema for all extensions",
				"Le flux de metadonnees doit etre conforme a la specification XMP et au schema PDFAExtension bien forme pour toutes les extensions.");
		fr = fr.replace(
				"The NeedAppearances flag of the interactive form dictionary shall either not be present or shall be false",
				"L`indicateur NeedAppearances du dictionnaire de formulaires interactifs doit etre soit absent, soit faux.");
		fr = fr.replace(
				"The number of color components in the color space described by the ICC profile data must match the number of components actually in the ICC profile.",
				"Le nombre de composantes de couleur dans l`espace de couleur decrit par les donnees du profil ICC doit correspondre au nombre de composantes effectivement presentes dans le profil ICC.");
		fr = fr.replace("The number of colour channels in the JPEG2000 data shall be 1, 3 or 4",
				"Le nombre de canaux de couleur dans les donnees JPEG2000 doit etre de 1, 3 ou 4.");
		fr = fr.replace("The obj and endobj keywords shall each be followed by an EOL marker",
				"Les mots-cles obj et endobj doivent chacun etre suivis d`un marqueur EOL.");
		fr = fr.replace("The object number and endobj keyword shall each be preceded by an EOL marker.",
				"Le numero d`objet et le mot-cle endobj sont tous deux precedes d`un marqueur EOL.");
		fr = fr.replace("The object number and generation number shall be separated by a single white-space character.",
				"Le numero d`objet et le numero de generation sont separes par un seul caractere d`espacement.");
		fr = fr.replace(
				"The only valid values of this key in PDF 1.4 are Type1C - Type 1–equivalent font program represented in the Compact Font Format (CFF) and CIDFontType0C - Type 0 CIDFont program represented in the Compact Font Format (CFF)",
				"Les seules valeurs valides de cette cle dans PDF 1.4 sont Type1C - Programme de police equivalent au Type 1 represente dans le Compact Font Format (CFF) et CIDFontType0C - Programme de police CIDFont de Type 0 represente dans le Compact Font Format (CFF).");
		fr = fr.replace(
				"The only valid values of this key in PDF 1.7 are Type1C - Type 1–equivalent font program represented in the Compact Font Format (CFF), CIDFontType0C - Type 0 CIDFont program represented in the Compact Font Format (CFF) and OpenType - OpenType® font program, as described in the OpenType Specification v.1.4",
				"Les seules valeurs valides de cette cle dans PDF 1.7 sont Type1C - Programme de police equivalent au Type 1 represente dans le Compact Font Format (CFF), CIDFontType0C - Programme CIDFont de Type 0 represente dans le Compact Font Format (CFF) et OpenType - Programme de police OpenType®, tel que decrit dans l`OpenType Specification v.1.4.");
		fr = fr.replace("The Page dictionary shall not include an AA entry for an additional-actions dictionary",
				"Le dictionnaire de pages ne doit pas inclure d`entree AA pour un dictionnaire d`actions supplementaires.");
		fr = fr.replace(
				"The PDF Signature (a DER-encoded PKCS#7 binary data object) shall be placed into the Contents entry of the signature dictionary.",
				"La signature PDF (un objet de donnees binaires PKCS#7 code en DER) doit etre placee dans l`entree Contents du dictionnaire des signatures.");
		fr = fr.replace(
				"The PDF/A version and conformance level of a file shall be specified using the PDF/A Identification extension schema",
				"La version PDF/A et le niveau de conformite d`un fichier doivent etre specifies a l`aide du schema d`extension d`identification PDF/A.");
		fr = fr.replace("The PKCS#7 object shall conform to the PKCS#7 specification in RFC 2315.",
				"L`objet PKCS#7 doit etre conforme a la specification PKCS#7 du RFC 2315.");
		fr = fr.replace(
				"The profile stream that is the value of the DestOutputProfile key shall either be an output profile (Device Class = prtr) or a monitor profile (Device Class = mntr).",
				"Le flux de profils correspondant a la valeur de la cle DestOutputProfile doit etre un profil de sortie (Device Class = prtr) ou un profil de moniteur (Device Class = mntr).");
		fr = fr.replace(
				"The profile that forms the stream of an ICCBased colour space shall conform to ICC.1:1998-09, ICC.1:2001-12, ICC.1:2003-09 or ISO 15076-1",
				"Le profil qui forme le flux d`un espace couleur ICCBased doit etre conforme aux normes ICC.1:1998-09, ICC.1:2001-12, ICC.1:2003-09 ou ISO 15076-1.");
		fr = fr.replace("The profiles shall have a colour space of either GRAY, RGB, or CMYK",
				"Les profils doivent avoir un espace couleur soit GRAY, soit RGB, soit CMYK.");
		fr = fr.replace("The required schema namespace prefix is pdfaExtension.",
				"Le prefixe de l`espace de noms du schema requis est pdfaExtension.");
		fr = fr.replace(
				"The size of any of the page boundaries described in ISO 32000-1:2008, 14.11.2 shall not be less than 3 units in either direction, nor shall it be greater than 14 400 units in either direction",
				"La taille de l`une des limites de page decrites dans la norme ISO 32000-1:2008, 14.11.2 ne doit pas etre inferieure a 3 unites dans un sens ou dans l`autre, ni superieure a 14 400 unites dans un sens ou dans l`autre.");
		fr = fr.replace(
				"The stream keyword shall be followed either by a CARRIAGE RETURN (0Dh) and LINE FEED (0Ah) character sequence or by a single LINE FEED (0Ah) character.",
				"Le mot-cle stream doit etre suivi soit d`une sequence de caracteres CARRIAGE RETURN (0Dh) et LINE FEED (0Ah), soit d`un seul caractere LINE FEED (0Ah).");
		fr = fr.replace(
				"The stream keyword shall be followed either by a CARRIAGE RETURN (0Dh) and LINE FEED (0Ah) character sequence or by a single LINE FEED character.",
				"Le mot-cle stream doit etre suivi soit d`une sequence de caracteres CARRIAGE RETURN (0Dh) et LINE FEED (0Ah), soit d`un seul caractere LINE FEED.");
		fr = fr.replace("The subtype is the value of the Subtype key, if present, in the font file stream dictionary.",
				"Le sous-type est la valeur de la cle Subtype, si elle existe, dans le dictionnaire de flux du fichier de polices.");
		fr = fr.replace(
				"The TransferFunction key in a halftone dictionary shall be used only as required by ISO 32000-1",
				"La cle TransferFunction d`un dictionnaire de demi-teintes ne doit etre utilisee que conformement a la norme ISO 32000-1.");
		fr = fr.replace(
				"The Unicode values specified in the ToUnicode CMap shall all be greater than zero (0), but not equal to either U+FEFF or U+FFFE",
				"Les valeurs Unicode specifiees dans la carte CMap ToUnicode doivent toutes etre superieures a zero (0), mais ne doivent pas etre egales a U+FEFF ou U+FFFE.");
		fr = fr.replace("The value of pdfaid:part shall be the part number of ISO 19005 to which the file conforms",
				"La valeur de pdfaid:part doit etre le numero de la partie de la norme ISO 19005 a laquelle le fichier est conforme.");
		fr = fr.replace(
				"The value of Author entry from the document information dictionary, if present, and its analogous XMP property dc:creator shall be equivalent.",
				"La valeur de l`entree Author du dictionnaire d`informations sur le document, si elle existe, et sa propriete XMP analogue dc:creator doivent etre equivalentes.");
		fr = fr.replace(
				"The value of CreationDate entry from the document information dictionary, if present, and its analogous XMP property xmp:CreateDate shall be equivalent",
				"La valeur de l`entree CreationDate du dictionnaire d`informations sur le document, si elle est presente, et sa propriete XMP analogue xmp:CreateDate sont equivalentes.");
		fr = fr.replace(
				"The value of Creator entry from the document information dictionary, if present, and its analogous XMP property xmp:CreatorTool shall be equivalent",
				"La valeur de l`entree Creator du dictionnaire d`informations sur le document, le cas echeant, et sa propriete XMP analogue xmp:CreatorTool sont equivalentes.");
		fr = fr.replace(
				"The value of Keywords entry from the document information dictionary, if present, and its analogous XMP property pdf:Keywords shall be equivalent",
				"La valeur de l`entree Keywords du dictionnaire d`informations sur le document, si elle est presente, et sa propriete XMP analogue pdf:Keywords sont equivalentes.");
		fr = fr.replace(
				"The value of ModDate entry from the document information dictionary, if present, and its analogous XMP property xmp:ModifyDate shall be equivalent",
				"La valeur de l`entree ModDate du dictionnaire d`informations sur le document, si elle est presente, et sa propriete XMP analogue xmp:ModifyDate sont equivalentes.");
		fr = fr.replace(
				"The value of Producer entry from the document information dictionary, if present, and its analogous XMP property pdf:Producer shall be equivalent",
				"La valeur de l`entree Producer du dictionnaire d`informations sur le document, si elle est presente, et sa propriete XMP analogue pdf:Producer sont equivalentes.");
		fr = fr.replace(
				"The value of Subject entry from the document information dictionary, if present, and its analogous XMP property dc:description[x-default] shall be equivalent",
				"La valeur de l`entree Subject du dictionnaire d`informations sur les documents, le cas echeant, et sa propriete XMP analogue dc:description[“x-default”] sont equivalentes.");
		fr = fr.replace(
				"The value of the F key in the Inline Image dictionary shall not be LZW, LZWDecode, Crypt, a value not listed in ISO 32000-1:2008, Table 6, or an array containing any such value",
				"La valeur de la cle F du dictionnaire des images en ligne ne doit pas etre LZW, LZWDecode, Crypt, une valeur non repertoriee dans le tableau 6 de la norme ISO 32000-1:2008 ou un tableau contenant l`une de ces valeurs.");
		fr = fr.replace(
				"The value of the Length key specified in the stream dictionary shall match the number of bytes in the file following the LINE FEED (0Ah) character after the stream keyword and preceding the EOL marker before the endstream keyword",
				"La valeur de la cle Length specifiee dans le dictionnaire stream doit correspondre au nombre d`octets du fichier suivant le caractere LINE FEED (0Ah) apres le mot-cle stream et precedant le marqueur EOL avant le mot-cle endstream.");
		fr = fr.replace(
				"The value of the Length key specified in the stream dictionary shall match the number of bytes in the file following the LINE FEED character after the stream keyword and preceding the EOL marker before the endstream keyword",
				"La valeur de la cle Length specifiee dans le dictionnaire de flux doit correspondre au nombre d`octets dans le fichier suivant le caractere LINE FEED apres le mot-cle stream et precedant le marqueur EOL avant le mot-cle endstream.");
		fr = fr.replace("The value of the METH entry in its colr box shall be 0x01, 0x02 or 0x03.",
				"La valeur de l`entree METH dans sa case colr doit etre 0x01, 0x02 ou 0x03.");
		fr = fr.replace(
				"The value of Title entry from the document information dictionary, if present, and its analogous XMP property dc:title[x-default] shall be equivalent",
				"La valeur de l`entree Title du dictionnaire d`informations du document, si elle est presente, et sa propriete XMP analogue dc:title[“x-default”] doivent etre equivalentes.");
		fr = fr.replace("The XMP package must be encoded as UTF-8", "Le paquet XMP doit etre encode en UTF-8.");
		fr = fr.replace(
				"The xref keyword and the cross reference subsection header shall be separated by a single EOL marker",
				"Le mot-cle xref et l`en-tete de la sous-section de reference croisee doivent etre separes par un seul marqueur EOL.");
		fr = fr.replace(
				"The xref keyword and the cross-reference subsection header shall be separated by a single EOL marker",
				"Le mot-cle xref et l`en-tete de sous-section de reference croisee doivent etre separes par un seul marqueur EOL.");
		fr = fr.replace("There shall be no AlternatePresentations entry in the documents name dictionary",
				"Il n`y a pas d`entree AlternatePresentations dans le dictionnaire des noms du document.");
		fr = fr.replace("There shall be no PresSteps entry in any Page dictionary",
				"Il ne doit pas y avoir d`entree PresSteps dans le dictionnaire des pages.");
		fr = fr.replace(
				"Type - name - (Required) The type of PDF object that this dictionary describes; must be Font for a font dictionary",
				"Type - nom - (obligatoire) Le type d`objet PDF decrit par ce dictionnaire ; doit etre Font pour un dictionnaire de polices.");
		fr = fr.replace("Value of valueType property shall be defined",
				"La valeur de la propriete valueType doit etre definie.");
		fr = fr.replace(
				"When computing the digest for the file, it shall be computed over the entire file, including the signature dictionary but excluding the PDF Signature itself",
				"Lors du calcul du condense du fichier, celui-ci doit etre calcule sur l`ensemble du fichier, y compris le dictionnaire de signatures mais a l`exclusion de la signature PDF elle-meme.");
		fr = fr.replace(
				"Where a rendering intent is specified, its value shall be one of the four values defined in ISO 32000-1:2008, Table 70: RelativeColorimetric, AbsoluteColorimetric, Perceptual or Saturation",
				"Lorsqu`une intention de rendu est specifiee, sa valeur doit etre l`une des quatre valeurs definies dans la norme ISO 32000-1:2008, tableau 70 : RelativeColorimetric, AbsoluteColorimetric, Perceptual ou Saturation.");
		fr = fr.replace(
				"Where a rendering intent is specified, its value shall be one of the four values defined in PDF Reference RelativeColorimetric, AbsoluteColorimetric, Perceptual or Saturation",
				"Lorsqu`une intention de rendu est specifiee, sa valeur doit etre l`une des quatre valeurs definies dans la reference PDF : RelativeColorimetric, AbsoluteColorimetric, Perceptual ou Saturation.");
		fr = fr.replace(
				"Widths - array - (Required except for the standard 14 fonts; indirect reference preferred) An array of (LastChar − FirstChar + 1) widths",
				"Widths - array - (Obligatoire sauf pour les 14 polices standard ; reference indirecte preferee) Un tableau de (LastChar - FirstChar + 1) largeurs.");
		fr = fr.replace("Xref streams shall not be used", "Les flux Xref ne doivent pas etre utilises");
		return fr;
	}

	/** Englisch String ins Intalienische aendern */
	public static String enTOit(String en) {
		String it = en;
		// TODO: it = it.replace("en", "it");
		it = it.replace("A circular mapping shall not exist", "Non deve esistere una mappatura circolare.");
		it = it.replace(
				"A CMap shall not reference any other CMap except those listed in ISO 32000-1:2008, 9.7.5.2, Table 118",
				"Una CMap non deve fare riferimento a nessun`altra CMap ad eccezione di quelle elencate in ISO 32000-1:2008, 9.7.5.2, Tabella 118.");
		it = it.replace("A conforming file shall not contain a CID value greater than 65535",
				"Un file conforme non deve contenere un valore CID superiore a 65535.");
		it = it.replace("A conforming file shall not contain a DeviceN colour space with more than 32 colourants",
				"Un file conforme non deve contenere uno spazio colore DeviceN con piu di 32 coloranti.");
		it = it.replace("A conforming file shall not contain any integer greater than 2147483647.",
				"Un file conforme non deve contenere alcun numero intero superiore a 2147483647.");
		it = it.replace("A conforming file shall not contain any integer less than -2147483648",
				"Un file conforme non deve contenere alcun numero intero inferiore a -2147483648.");
		it = it.replace("A conforming file shall not contain any name longer than 127 bytes",
				"Un file conforme non deve contenere nomi piu lunghi di 127 byte.");
		it = it.replace("A conforming file shall not contain any PostScript XObjects",
				"Un file conforme non deve contenere alcun XObject PostScript.");
		it = it.replace("A conforming file shall not contain any real number closer to zero than +/-1.175 x 10^(-38)",
				"Un file conforme non deve contenere alcun numero reale piu vicino a zero di +/-1,175 x 10^(-38).");
		it = it.replace("A conforming file shall not contain any real number outside the range of +/-3.403 x 10^38",
				"Un file conforme non deve contenere alcun numero reale al di fuori dell`intervallo +/-3,403 x 10^38.");
		it = it.replace("A conforming file shall not contain any reference XObjects",
				"Un file conforme non deve contenere alcun riferimento a XObjects.");
		it = it.replace("A conforming file shall not contain any string longer than 32767 bytes",
				"Un file conforme non deve contenere stringhe piu lunghe di 32767 byte.");
		it = it.replace("A conforming file shall not contain more than 8388607 indirect objects",
				"Un file conforme non deve contenere piu di 8388607 oggetti indiretti.");
		it = it.replace("A conforming file shall not nest q/Q pairs by more than 28 nesting levels",
				"Un file conforme non deve annidare le coppie q/Q per piu di 28 livelli di annidamento.");
		it = it.replace(
				"A conforming reader shall use only that colour space and shall ignore all other colour space specifications",
				"Un lettore conforme deve utilizzare solo quello spazio colore e ignorare tutte le altre specifiche di spazio colore.");
		it = it.replace(
				"A content stream shall not contain any operators not defined in PDF Reference even if such operators are bracketed by the BX/EX compatibility operators",
				"Un flusso di contenuto non deve contenere alcun operatore non definito in PDF Reference, anche se tali operatori sono inseriti tra gli operatori di compatibilita BX/EX.");
		it = it.replace(
				"A content stream that references other objects, such as images and fonts that are necessary to fully render or process the stream, shall have an explicitly associated Resources dictionary as described in ISO 32000-1:2008, 7.8.3",
				"Un flusso di contenuto che fa riferimento ad altri oggetti, come immagini e font, necessari per il rendering o l`elaborazione completa del flusso, deve avere un dizionario delle risorse esplicitamente associato, come descritto nella norma ISO 32000-1:2008, 7.8.3.");
		it = it.replace("A documents Catalog shall not contain the NeedsRendering key",
				"Il catalogo di un documento non deve contenere la chiave NeedsRendering.");
		it = it.replace("A Field dictionary shall not contain the A or AA keys",
				"Il dizionario Field non deve contenere le chiavi A o AA.");
		it = it.replace("A Field dictionary shall not include an AA entry for an additional-actions dictionary",
				"Un dizionario di campo non deve includere una voce AA per un dizionario di azioni aggiuntive.");
		it = it.replace(
				"A file specification dictionary, as defined in ISO 32000-1:2008, 7.11.3, may contain the EF key, provided that the embedded file is compliant with either ISO 19005-1 or this part of ISO 19005",
				"Un dizionario delle specifiche del file, come definito nella ISO 32000-1:2008, 7.11.3, puo contenere la chiave EF, a condizione che il file incorporato sia conforme alla ISO 19005-1 o a questa parte della ISO 19005.");
		it = it.replace("A file specification dictionary, as defined in PDF 3.10.2, shall not contain the EF key",
				"Un dizionario delle specifiche del file, come definito in PDF 3.10.2, non deve contenere la chiave EF.");
		it = it.replace(
				"A files name dictionary, as defined in PDF Reference 3.6.3, shall not contain the EmbeddedFiles key",
				"Il dizionario dei nomi dei file, come definito in PDF Reference 3.6.3, non deve contenere la chiave EmbeddedFiles.");
		it = it.replace(
				"A font referenced for use solely in rendering mode 3 is therefore not rendered and is thus exempt from the embedding requirement.",
				"Un font a cui si fa riferimento per l`uso esclusivo nella modalita di rendering 3 non viene renderizzato ed e quindi esente dal requisito di incorporazione.");
		it = it.replace(
				"A form XObject dictionary shall not contain any of the following: - the OPI key; - the Subtype2 key with a value of PS; - the PS key",
				"Il dizionario XObject di un modulo non deve contenere nessuno dei seguenti elementi: - la chiave OPI; - la chiave Subtype2 con valore PS; - la chiave PS.");
		it = it.replace("A form XObject dictionary shall not contain the Subtype2 key with a value of PS or the PS key",
				"Il dizionario di un modulo XObject non deve contenere la chiave Subtype2 con valore PS o la chiave PS.");
		it = it.replace(
				"A Group object with an S key with a value of Transparency shall not be included in a form XObject.",
				"Un oggetto Group con una chiave S con valore Transparency non deve essere incluso in un form XObject.");
		it = it.replace(
				"A Group object with an S key with a value of Transparency shall not be included in a page dictionary",
				"Un oggetto Gruppo con una chiave S con valore Trasparenza non deve essere incluso in un dizionario di pagina.");
		it = it.replace(
				"A hexadecimal string is written as a sequence of hexadecimal digits (0–9 and either A–F or a–f)",
				"Una stringa esadecimale e scritta come una sequenza di cifre esadecimali (0-9 e A-F o a-f).");
		it = it.replace(
				"A language identifier shall either be the empty text string, to indicate that the language is unknown, or a Language-Tag as defined in RFC 3066, Tags for the Identification of Languages",
				"Un identificatore di lingua puo essere una stringa di testo vuota, per indicare che la lingua e sconosciuta, oppure un Language-Tag come definito in RFC 3066, Tags for the Identification of Languages.");
		it = it.replace("A Level A conforming file shall specify the value of pdfaid:conformance as A",
				"Un file conforme al livello A deve specificare il valore di pdfaid:conformance come A.");
		it = it.replace("A Level A conforming file shall specify the value of pdfaid:conformance as A.",
				"Un file conforme al livello A deve specificare il valore di pdfaid:conformance come A.");
		it = it.replace("A Level B conforming file shall specify the value of pdfaid:conformance as B",
				"Un file conforme di livello B deve specificare il valore di pdfaid:conformance come B.");
		it = it.replace("A Level B conforming file shall specify the value of pdfaid:conformance as B.",
				"Un file conforme al livello B deve specificare il valore di pdfaid:conformance come B.");
		it = it.replace("A Level U conforming file shall specify the value of pdfaid:conformance as U",
				"Un file conforme al livello U deve specificare il valore di pdfaid:conformance come U.");
		it = it.replace(
				"A PDF/A-1 OutputIntent is an OutputIntent dictionary, as defined by PDF Reference 9.10.4, that is included in the files OutputIntents array and has GTS_PDFA1 as the value of its S key and a valid ICC profile stream as the value its DestOutputProfile key",
				"Un OutputIntent PDF/A-1 e un dizionario OutputIntent, come definito da PDF Reference 9.10.4, incluso nell`array OutputIntents del file e avente GTS_PDFA1 come valore della chiave S e un flusso di profilo ICC valido come valore della chiave DestOutputProfile.");
		it = it.replace(
				"A PDF/A-2 compliant document shall not contain a reference to the .notdef glyph from any of the text showing operators, regardless of text rendering mode, in any content stream",
				"Un documento conforme a PDF/A-2 non deve contenere un riferimento al glifo .notdef di uno qualsiasi degli operatori di visualizzazione del testo, indipendentemente dalla modalita di rendering del testo, in nessun flusso di contenuto.");
		it = it.replace("A stream dictionary shall not contain the F, FFilter, or FDecodeParms keys",
				"Il dizionario di un flusso non deve contenere le chiavi F, FFilter o FDecodeParms.");
		it = it.replace("A stream object dictionary shall not contain the F, FFilter, or FDecodeParms keys",
				"Il dizionario di un oggetto stream non deve contenere le chiavi F, FFilter o FDecodeParms.");
		it = it.replace("A Widget annotation dictionary shall not contain the A or AA keys",
				"Il dizionario delle annotazioni di un widget non deve contenere le chiavi A o AA.");
		it = it.replace(
				"A Widget annotation dictionary shall not include an AA entry for an additional-actions dictionary",
				"Il dizionario delle annotazioni di un widget non deve includere una voce AA per un dizionario delle azioni aggiuntive.");
		it = it.replace("Absolute real value must be less than or equal to 32767.0",
				"Il valore reale assoluto deve essere inferiore o uguale a 32767,0.");
		it = it.replace("Additionally, the 3D, Sound, Screen and Movie types shall not be permitted",
				"Inoltre, i tipi 3D, Sound, Screen e Movie non sono consentiti.");
		it = it.replace("Additionally, the deprecated set-state and no-op actions shall not be permitted",
				"Inoltre, non sono consentite le azioni deprecate set-state e no-op.");
		it = it.replace("Additionally, the deprecated set-state and no-op actions shall not be permitted.",
				"Inoltre, le azioni deprecate set-state e no-op non sono consentite.");
		it = it.replace("Additionally, the FileAttachment, Sound and Movie types shall not be permitted",
				"Inoltre, i tipi FileAttachment, Sound e Movie non sono consentiti.");
		it = it.replace(
				"All CMaps used within a conforming file, except Identity-H and Identity-V, shall be embedded in that file as described in PDF Reference 5.6.4",
				"Tutte le CMap utilizzate all`interno di un file conforme, ad eccezione di Identity-H e Identity-V, devono essere incorporate in tale file come descritto nel riferimento PDF 5.6.4.");
		it = it.replace(
				"All CMaps used within a PDF/A-2 file, except those listed in ISO 32000-1:2008, 9.7.5.2, Table 118, shall be embedded in that file as described in ISO 32000-1:2008, 9.7.5",
				"Tutte le CMap utilizzate in un file PDF/A-2, ad eccezione di quelle elencate in ISO 32000-1:2008, 9.7.5.2, Tabella 118, devono essere incorporate nel file come descritto in ISO 32000-1:2008, 9.7.5.");
		it = it.replace("All colour channels in the JPEG2000 data shall have the same bit-depth",
				"Tutti i canali di colore nei dati JPEG2000 devono avere la stessa profondita di bit.");
		it = it.replace(
				"All content of all XMP packets shall be well-formed, as defined by Extensible Markup Language (XML) 1.0 (Third Edition), 2.1, and the RDF/XML Syntax Specification (Revised)",
				"Il contenuto di tutti i pacchetti XMP deve essere ben formato, come definito da Extensible Markup Language (XML) 1.0 (Terza edizione), 2.1 e dalla Specifica di sintassi RDF/XML (riveduta).");
		it = it.replace(
				"All fields described in each of the tables in 6.6.2.3.3 shall be present in any extension schema container schema",
				"Tutti i campi descritti in ciascuna delle tabelle in 6.6.2.3.3 devono essere presenti in qualsiasi schema contenitore dello schema di estensione.");
		it = it.replace(
				"All fonts and font programs used in a conforming file, regardless of rendering mode usage, shall conform to the provisions in ISO 32000-1:2008, 9.6 and 9.7, as well as to the font specifications referenced by these provisions.",
				"Tutti i font e i programmi di font utilizzati in un file conforme, indipendentemente dall`utilizzo della modalita di rendering, devono essere conformi alle disposizioni della ISO 32000-1:2008, 9.6 e 9.7, nonche alle specifiche dei font a cui fanno riferimento tali disposizioni.");
		it = it.replace(
				"All fonts used in a conforming file shall conform to the font specifications defined in PDF Reference 5.5.",
				"Tutti i font utilizzati in un file conforme devono essere conformi alle specifiche dei font definite in PDF Reference 5.5.");
		it = it.replace(
				"All halftones in a conforming PDF/A-2 file shall have the value 1 or 5 for the HalftoneType key",
				"Tutti i mezzitoni in un file PDF/A-2 conforme devono avere il valore 1 o 5 per la chiave HalftoneType.");
		it = it.replace(
				"All ICCBased colour spaces shall be embedded as ICC profile streams as described in PDF Reference 4.5",
				"Tutti gli spazi colore basati su ICC devono essere incorporati come flussi di profili ICC come descritto nel riferimento PDF 4.5.");
		it = it.replace(
				"All ICCBased colour spaces shall be embedded as ICC profile streams as described in PDF Reference 4.5.",
				"Tutti gli spazi colore basati su ICC devono essere incorporati come flussi di profili ICC come descritto in PDF Reference 4.5.");
		it = it.replace("All metadata streams present in the PDF shall conform to the XMP Specification.",
				"Tutti i flussi di metadati presenti nel PDF devono essere conformi alle specifiche XMP.");
		it = it.replace(
				"All non-standard structure types shall be mapped to the nearest functionally equivalent standard type, as defined in ISO 32000-1:2008, 14.8.4, in the role map dictionary of the structure tree root",
				"Tutti i tipi di struttura non standard devono essere mappati al tipo standard funzionalmente equivalente piu vicino, come definito in ISO 32000-1:2008, 14.8.4, nel dizionario della mappa dei ruoli della radice dell`albero delle strutture.");
		it = it.replace(
				"All non-standard structure types shall be mapped to the nearest functionally equivalent standard type, as defined in PDF Reference 9.7.4, in the role map dictionary of the structure tree root",
				"Tutti i tipi di struttura non standard devono essere mappati al tipo standard funzionalmente equivalente piu vicino, come definito in PDF Reference 9.7.4, nel dizionario della mappa dei ruoli della radice dell`albero delle strutture.");
		it = it.replace(
				"All non-symbolic TrueType fonts shall have either MacRomanEncoding or WinAnsiEncoding as the value for the Encoding key in the Font dictionary or as the value for the BaseEncoding key in the dictionary that is the value of the Encoding key in the Font dictionary.",
				"Tutti i font TrueType non simbolici devono avere MacRomanEncoding o WinAnsiEncoding come valore della chiave Encoding nel dizionario Font o come valore della chiave BaseEncoding nel dizionario che e il valore della chiave Encoding nel dizionario Font.");
		it = it.replace(
				"All non-symbolic TrueType fonts shall specify MacRomanEncoding or WinAnsiEncoding, either as the value of the Encoding entry in the font dictionary or as the value of the BaseEncoding entry in the dictionary that is the value of the Encoding entry in the font dictionary.",
				"Tutti i font TrueType non simbolici devono specificare MacRomanEncoding o WinAnsiEncoding, come valore della voce Encoding nel dizionario dei font o come valore della voce BaseEncoding nel dizionario che e il valore della voce Encoding nel dizionario dei font.");
		it = it.replace(
				"All non-white-space characters in hexadecimal strings shall be in the range 0 to 9, A to F or a to f",
				"Tutti i caratteri senza spazio bianco nelle stringhe esadecimali devono essere compresi nell`intervallo da 0 a 9, da A a F o da a f.");
		it = it.replace(
				"All properties specified in XMP form shall use either the predefined schemas defined in the XMP Specification, ISO 19005-1 or this part of ISO 19005, or any extension schemas that comply with 6.6.2.3.2",
				"Tutte le proprieta specificate in forma XMP devono utilizzare gli schemi predefiniti definiti nella specifica XMP, nella norma ISO 19005-1 o in questa parte della norma ISO 19005, oppure gli schemi di estensione conformi al punto 6.6.2.3.2.");
		it = it.replace(
				"All Separation arrays within a single PDF/A-2 file (including those in Colorants dictionaries) that have the same name shall have the same tintTransform and alternateSpace.",
				"Tutte le matrici di separazione all`interno di un singolo file PDF/A-2 (comprese quelle nei dizionari dei coloranti) che hanno lo stesso nome devono avere lo stesso tintTransform e alternateSpace.");
		it = it.replace(
				"All standard stream filters listed in ISO 32000-1:2008, 7.4, Table 6 may be used, with the exception of LZWDecode.",
				"Possono essere utilizzati tutti i filtri di flusso standard elencati in ISO 32000-1:2008, 7.4, Tabella 6, ad eccezione di LZWDecode.");
		it = it.replace("All symbolic TrueType fonts shall not specify an Encoding entry in the font dictionary",
				"Tutti i font TrueType simbolici non devono specificare una voce Encoding nel dizionario dei font.");
		it = it.replace("An annotation dictionary shall contain the F key.",
				"Un dizionario delle annotazioni deve contenere la chiave F.");
		it = it.replace(
				"An annotation dictionary shall not contain the C array or the IC array unless the colour space of the DestOutputProfile in the PDF/A-1 OutputIntent dictionary, defined in 6.2.2, is RGB",
				"Un dizionario delle annotazioni non deve contenere la matrice C o la matrice IC a meno che lo spazio colore del profilo DestOutputProfile nel dizionario PDF/A-1 OutputIntent, definito in 6.2.2, sia RGB.");
		it = it.replace("An annotation dictionary shall not contain the CA key with a value other than 1.0",
				"Un dizionario delle annotazioni non deve contenere la chiave CA con un valore diverso da 1.0.");
		it = it.replace("An ExtGState dictionary shall not contain the HTP key",
				"Un dizionario ExtGState non deve contenere la chiave HTP.");
		it = it.replace("An ExtGState dictionary shall not contain the TR key",
				"Un dizionario ExtGState non deve contenere la chiave TR.");
		it = it.replace("An ExtGState dictionary shall not contain the TR2 key with a value other than Default",
				"Il dizionario ExtGState non deve contenere la chiave TR2 con un valore diverso da Default.");
		it = it.replace("An Image dictionary shall not contain the Alternates key",
				"Il dizionario Image non deve contenere la chiave Alternates.");
		it = it.replace("An Image dictionary shall not contain the OPI key",
				"Il dizionario Image non deve contenere la chiave OPI.");
		it = it.replace("An XObject dictionary (Image or Form) shall not contain the OPI key",
				"Un dizionario XObject (Image o Form) non deve contenere la chiave OPI.");
		it = it.replace("An XObject dictionary shall not contain the SMask key",
				"Un dizionario XObject non deve contenere la chiave SMask.");
		it = it.replace("Annotation types not defined in ISO 32000-1 shall not be permitted.",
				"I tipi di annotazione non definiti in ISO 32000-1 non sono consentiti.");
		it = it.replace("Annotation types not defined in PDF Reference shall not be permitted.",
				"I tipi di annotazione non definiti in PDF Reference non sono consentiti.");
		it = it.replace("As of PDF 1.4, N must be 1, 3, or 4", "A partire da PDF 1.4, N deve essere 1, 3 o 4.");
		it = it.replace("At minimum, it shall include the signers X.509 signing certificate",
				"Deve includere almeno il certificato di firma X.509 del firmatario.");
		it = it.replace(
				"At minimum, there shall only be a single signer (e.g. a single SignerInfo structure) in the PDF Signature",
				"Come minimo, nella firma PDF deve essere presente un solo firmatario (ad esempio, una sola struttura SignerInfo).");
		it = it.replace("BaseFont - name - (Required) The PostScript name of the font",
				"BaseFont - nome - (Obbligatorio) Il nome PostScript del carattere.");
		it = it.replace("Compression and whether or not an object is direct or indirect shall be ignored",
				"La compressione e il fatto che un oggetto sia diretto o indiretto sono ignorati.");
		it = it.replace(
				"Content streams shall not contain any operators not defined in ISO 32000-1 even if such operators are bracketed by the BX/EX compatibility operators",
				"I flussi di contenuto non devono contenere operatori non definiti nella norma ISO 32000-1, anche se tali operatori sono inseriti tra gli operatori di compatibilita BX/EX.");
		it = it.replace("dc:creator shall contain exactly one entry",
				"dc:creator deve contenere esattamente una voce.");
		it = it.replace(
				"DeviceCMYK may be used only if the file has a PDF/A-1 OutputIntent that uses a CMYK colour space",
				"DeviceCMYK puo essere utilizzato solo se il file ha un OutputIntent PDF/A-1 che utilizza uno spazio colore CMYK.");
		it = it.replace(
				"DeviceCMYK shall only be used if a device independent DefaultCMYK colour space has been set or if a DeviceN-based DefaultCMYK colour space has been set when the DeviceCMYK colour space is used or the file has a PDF/A OutputIntent that contains a CMYK destination profile",
				"DeviceCMYK deve essere utilizzato solo se e stato impostato uno spazio colore DefaultCMYK indipendente dal dispositivo o se e stato impostato uno spazio colore DefaultCMYK basato su DeviceN quando viene utilizzato lo spazio colore DeviceCMYK o il file ha un OutputIntent PDF/A che contiene un profilo di destinazione CMYK.");
		it = it.replace(
				"DeviceGray shall only be used if a device independent DefaultGray colour space has been set when the DeviceGray colour space is used, or if a PDF/A OutputIntent is present",
				"DeviceGray deve essere utilizzato solo se e stato impostato uno spazio colore DefaultGray indipendente dal dispositivo quando viene utilizzato lo spazio colore DeviceGray o se e presente un OutputIntent PDF/A.");
		it = it.replace(
				"DeviceRGB may be used only if the file has a PDF/A-1 OutputIntent that uses an RGB colour space",
				"DeviceRGB puo essere utilizzato solo se il file ha un OutputIntent PDF/A-1 che utilizza uno spazio colore RGB.");
		it = it.replace(
				"DeviceRGB shall only be used if a device independent DefaultRGB colour space has been set when the DeviceRGB colour space is used, or if the file has a PDF/A OutputIntent that contains an RGB destination profile",
				"DeviceRGB deve essere utilizzato solo se e stato impostato uno spazio colore DefaultRGB indipendente dal dispositivo quando viene utilizzato lo spazio colore DeviceRGB o se il file ha un OutputIntent PDF/A che contiene un profilo di destinazione RGB.");
		it = it.replace(
				"Each optional content configuration dictionary shall contain the Name key, whose value shall be unique amongst all optional content configuration dictionaries within the PDF/A-2 file",
				"Ogni dizionario opzionale di configurazione del contenuto deve contenere la chiave Name, il cui valore deve essere unico tra tutti i dizionari opzionali di configurazione del contenuto all`interno del file PDF/A-2.");
		it = it.replace(
				"Each optional content configuration dictionary that forms the value of the D key, or that is an element in the array that forms the value of the Configs key in the OCProperties dictionary, shall contain the Name key",
				"Ogni dizionario opzionale di configurazione del contenuto che costituisce il valore della chiave D o che e un elemento dell`array che costituisce il valore della chiave Configs nel dizionario OCProperties deve contenere la chiave Name.");
		it = it.replace(
				"Embedded font programs shall define all font glyphs referenced for rendering with conforming file",
				"I programmi di font incorporati devono definire tutti i glifi dei font a cui si fa riferimento per il rendering con un file conforme.");
		it = it.replace("Embedded fonts shall define all glyphs referenced for rendering within the conforming file.",
				"I font incorporati devono definire tutti i glifi a cui si fa riferimento per il rendering all`interno del file conforme.");
		it = it.replace(
				"Every annotation (including those whose Subtype value is Widget, as used for form fields), except for the two cases listed below, shall have at least one appearance dictionary: - annotations where the value of the Rect key consists of an array where value 1 is equal to value 3 and value 2 is equal to value 4; - annotations whose Subtype value is Popup or Link",
				"Ogni annotazione (comprese quelle il cui valore di sottotipo e Widget, come si usa per i campi dei moduli), ad eccezione dei due casi elencati di seguito, deve avere almeno un dizionario di aspetto: - annotazioni in cui il valore della chiave Rect consiste in una matrice in cui il valore 1 e uguale al valore 3 e il valore 2 e uguale al valore 4; - annotazioni il cui valore di sottotipo e Popup o Link.");
		it = it.replace("Every form field shall have an appearance dictionary associated with the fields data",
				"Ogni campo del modulo deve avere un dizionario di aspetto associato ai dati del campo.");
		it = it.replace(
				"Except for annotation dictionaries whose Subtype value is Popup, all annotation dictionaries shall contain the F key",
				"Ad eccezione dei dizionari di annotazioni il cui valore di sottotipo e Popup, tutti i dizionari di annotazioni devono contenere la chiave F.");
		it = it.replace(
				"Extension schema descriptions shall be specified using the PDF/A extension schema description schema defined in this clause",
				"Le descrizioni degli schemi di estensione devono essere specificate utilizzando lo schema di descrizione degli schemi di estensione PDF/A definito in questa clausola.");
		it = it.replace(
				"Extension schemas shall be specified using the PDF/A extension schema container schema defined in 6.6.2.3.3.",
				"Gli schemi di estensione devono essere specificati utilizzando lo schema del contenitore dello schema di estensione PDF/A definito in 6.6.2.3.3.");
		it = it.replace(
				"Field category of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text, value external or internal and namespace prefix pdfaProperty",
				"Il campo category del tipo di valore PDF/A Property nello schema di estensione PDF/A deve essere presente e deve avere tipo Text, valore external o internal e prefisso namespace pdfaProperty.");
		it = it.replace(
				"Field description of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField",
				"Il campo description del tipo di valore PDF/A Field nello schema di estensione PDF/A deve avere tipo Text e prefisso dello spazio dei nomi pdfaField.");
		it = it.replace(
				"Field description of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaProperty",
				"Il campo description del tipo di valore PDF/A Property nello schema di estensione PDF/A deve essere presente e deve avere tipo Text e prefisso dello spazio dei nomi pdfaProperty.");
		it = it.replace(
				"Field description of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaType",
				"Il campo description del tipo di valore PDF/A ValueType nello schema di estensione PDF/A deve essere presente e deve avere tipo Text e prefisso dello spazio dei nomi pdfaType.");
		it = it.replace(
				"Field field of the PDF/A ValueType value type in the PDF/A extension schema shall have type Seq Field and namespace prefix pdfaType",
				"Il campo field del tipo di valore PDF/A ValueType nello schema di estensione PDF/A deve avere tipo Seq Field e prefisso dello spazio dei nomi pdfaType.");
		it = it.replace(
				"Field name of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField",
				"Il campo name del tipo di valore PDF/A Field nello schema di estensione PDF/A deve avere tipo Text e prefisso dello spazio dei nomi pdfaField.");
		it = it.replace(
				"Field name of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaProperty",
				"Il campo name del tipo di valore PDF/A Property nello schema di estensione PDF/A deve essere presente e deve avere tipo Text e prefisso dello spazio dei nomi pdfaProperty.");
		it = it.replace(
				"Field namespaceURI of the PDF/A Schema value type in the PDF/A extension schema shall be present and shall have type URI and namespace prefix pdfaSchema",
				"Il campo namespaceURI del tipo di valore PDF/A Schema nello schema di estensione PDF/A deve essere presente e deve avere tipo URI e prefisso dello spazio dei nomi pdfaSchema.");
		it = it.replace(
				"Field namespaceURI of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type URI and namespace prefix pdfaType",
				"Il campo namespaceURI del tipo di valore PDF/A ValueType nello schema di estensione PDF/A deve essere presente e deve avere il tipo URI e il prefisso dello spazio dei nomi pdfaType.");
		it = it.replace(
				"Field prefix of the PDF/A Schema value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaSchema",
				"Il campo prefix del tipo di valore PDF/A Schema nello schema di estensione PDF/A deve essere presente e deve avere tipo Text e prefisso dello spazio dei nomi pdfaSchema.");
		it = it.replace(
				"Field prefix of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaType",
				"Il campo prefix del tipo di valore PDF/A ValueType nello schema di estensione PDF/A deve essere presente e deve avere tipo Text e prefisso dello spazio dei nomi pdfaType.");
		it = it.replace(
				"Field property of the PDF/A Schema value type in the PDF/A extension schema shall have type Seq Property and namespace prefix pdfaSchema",
				"Il campo property del tipo di valore PDF/A Schema nello schema di estensione PDF/A deve avere tipo Seq Property e prefisso dello spazio dei nomi pdfaSchema.");
		it = it.replace(
				"Field schema of the PDF/A Schema value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaSchema",
				"Il campo schema del tipo di valore PDF/A Schema nello schema di estensione PDF/A deve essere presente e deve avere tipo Text e prefisso dello spazio dei nomi pdfaSchema.");
		it = it.replace(
				"Field type of the PDF/A ValueType value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaType",
				"Il campo type del tipo di valore PDF/A ValueType nello schema di estensione PDF/A deve essere presente e deve avere tipo Text e prefisso dello spazio dei nomi pdfaType.");
		it = it.replace(
				"Field valueType of the PDF/A Field value type in the PDF/A extension schema shall have type Text and namespace prefix pdfaField.",
				"Il campo valueType del tipo di valore PDF/A Field nello schema di estensione PDF/A deve avere tipo Text e prefisso dello spazio dei nomi pdfaField.");
		it = it.replace(
				"Field valueType of the PDF/A Property value type in the PDF/A extension schema shall be present and shall have type Text and namespace prefix pdfaProperty.",
				"Il campo valueType del tipo di valore PDF/A Property nello schema di estensione PDF/A deve essere presente e deve avere tipo Text e prefisso dello spazio dei nomi pdfaProperty.");
		it = it.replace(
				"Field valueType of the PDF/A Schema value type in the PDF/A extension schema shall have type Seq ValueType and namespace prefix pdfaSchema",
				"Il campo valueType del tipo di valore PDF/A Schema nello schema di estensione PDF/A deve essere di tipo Seq ValueType e prefisso dello spazio dei nomi pdfaSchema.");
		it = it.replace("Filters that are not listed in ISO 32000-1:2008, 7.4, Table 6 shall not be used",
				"I filtri non elencati nella ISO 32000-1:2008, 7.4, Tabella 6 non devono essere utilizzati.");
		it = it.replace(
				"FirstChar - integer - (Required except for the standard 14 fonts) The first character code defined in the fonts Widths array",
				"FirstChar - intero - (richiesto tranne che per i font standard 14) Il primo codice di carattere definito nell`array Widths del font.");
		it = it.replace(
				"Font names, names of colourants in Separation and DeviceN colour spaces, and structure type names, after expansion of character sequences escaped with a NUMBER SIGN (23h), if any, shall be valid UTF-8 character sequences",
				"I nomi dei font, i nomi dei coloranti negli spazi colore Separation e DeviceN e i nomi dei tipi di struttura, dopo l`espansione delle sequenze di caratteri evase con un SEGNO NUMERICO (23h), se presenti, devono essere sequenze di caratteri UTF-8 valide.");
		it = it.replace("Font programs cmap tables for all symbolic TrueType fonts shall contain exactly one encoding",
				"Le tabelle cmap dei programmi di font per tutti i font TrueType simbolici devono contenere esattamente una codifica.");
		it = it.replace(
				"For all annotation dictionaries containing an AP key, the appearance dictionary that it defines as its value shall contain only the N key",
				"Per tutti i dizionari di annotazione che contengono una chiave AP, il dizionario dell`aspetto che definisce come valore deve contenere solo la chiave N.");
		it = it.replace(
				"For all CIDFont subsets referenced within a conforming file, the font descriptor dictionary shall include a CIDSet stream identifying which CIDs are present in the embedded CIDFont file, as described in PDF Reference Table 5.20",
				"Per tutti i sottoinsiemi di CIDFont a cui si fa riferimento all`interno di un file conforme, il dizionario dei descrittori di font deve includere un flusso CIDSet che identifica quali CID sono presenti nel file CIDFont incorporato, come descritto nella Tabella di riferimento PDF 5.20.");
		it = it.replace(
				"For all non-symbolic TrueType fonts used for rendering, the embedded TrueType font program shall contain one or several non-symbolic cmap entries such that all necessary glyph lookups can be carried out",
				"Per tutti i font TrueType non simbolici utilizzati per il rendering, il programma di font TrueType incorporato deve contenere una o piu voci cmap non simboliche che consentano di effettuare tutte le ricerche di glifi necessarie.");
		it = it.replace(
				"For all Type 1 font subsets referenced within a conforming file, the font descriptor dictionary shall include a CharSet string listing the character names defined in the font subset, as described in PDF Reference Table 5.18",
				"Per tutti i sottoinsiemi di font di Tipo 1 a cui si fa riferimento in un file conforme, il dizionario dei descrittori di font deve includere una stringa CharSet che elenca i nomi dei caratteri definiti nel sottoinsieme di font, come descritto nella Tabella di riferimento PDF 5.18.");
		it = it.replace(
				"For all Type 2 CIDFonts that are used for rendering, the CIDFont dictionary shall contain a CIDToGIDMap entry that shall be a stream mapping from CIDs to glyph indices or the name Identity, as described in PDF Reference Table 5.13",
				"Per tutti i CIDFont di tipo 2 utilizzati per il rendering, il dizionario CIDFont deve contenere una voce CIDToGIDMap che deve essere una mappatura del flusso dai CID agli indici dei glifi o al nome Identity, come descritto nella Tabella di riferimento PDF 5.13.");
		it = it.replace("For an inline image, the I key shall have a value of false",
				"Per un`immagine in linea, la chiave I deve avere il valore false.");
		it = it.replace(
				"For any character, regardless of its rendering mode, that is mapped to a code or codes in the Unicode Private Use Area (PUA), an ActualText entry as described in ISO 32000-1:2008, 14.9.4 shall be present for this character or a sequence of characters of which such a character is a part",
				"Per qualsiasi carattere, indipendentemente dalla modalita di rendering, che sia mappato a uno o piu codici nell`area d`uso privata (PUA) di Unicode, deve essere presente una voce ActualText come descritto in ISO 32000-1:2008, 14.9.4 per questo carattere o per una sequenza di caratteri di cui tale carattere fa parte.");
		it = it.replace(
				"For any given composite (Type 0) font referenced within a conforming file, the CIDSystemInfo entries of its CIDFont and CMap dictionaries shall be compatible.",
				"Per ogni font composito (Tipo 0) a cui si fa riferimento in un file conforme, le voci CIDSystemInfo dei dizionari CIDFont e CMap devono essere compatibili.");
		it = it.replace(
				"For any given composite (Type 0) font within a conforming file, the CIDSystemInfo entry in its CIDFont dictionary and its Encoding dictionary shall have the following relationship: - If the Encoding key in the Type 0 font dictionary is Identity-H or Identity-V, any values of Registry, Ordering, and Supplement may be used in the CIDSystemInfo entry of the CIDFont.",
				"Per qualsiasi font composito (Tipo 0) all`interno di un file conforme, la voce CIDSystemInfo del suo dizionario CIDFont e il suo dizionario Encoding devono avere la seguente relazione: - Se la chiave Encoding nel dizionario del font Tipo 0 e Identity-H o Identity-V, nella voce CIDSystemInfo del CIDFont possono essere utilizzati tutti i valori di Registry, Ordering e Supplement.");
		it = it.replace(
				"For any spot colour used in a DeviceN or NChannel colour space, an entry in the Colorants dictionary shall be present",
				"Per ogni tinta piatta utilizzata in uno spazio colore DeviceN o NChannel, deve essere presente una voce nel dizionario Colorants.");
		it = it.replace(
				"For every font embedded in a conforming file and used for rendering, the glyph width information in the font dictionary and in the embedded font program shall be consistent",
				"Per ogni font incorporato in un file conforme e utilizzato per il rendering, le informazioni sulla larghezza dei glifi nel dizionario dei font e nel programma di font incorporato devono essere coerenti.");
		it = it.replace(
				"For those CMaps that are embedded, the integer value of the WMode entry in the CMap dictionary shall be identical to the WMode value in the embedded CMap stream",
				"Per le CMap incorporate, il valore intero della voce WMode nel dizionario CMap deve essere identico al valore WMode nel flusso CMap incorporato.");
		it = it.replace("Halftones in a conforming PDF/A-2 file shall not contain a HalftoneName key",
				"I mezzitoni in un file PDF/A-2 conforme non devono contenere una chiave HalftoneName.");
		it = it.replace("Hexadecimal strings shall contain an even number of non-white-space characters",
				"Le stringhe esadecimali devono contenere un numero pari di caratteri senza spazi bianchi.");
		it = it.replace(
				"If a files OutputIntents array contains more than one entry, as might be the case where a file is compliant with this part of ISO 19005 and at the same time with PDF/X-4 or PDF/E-1, then all entries that contain a DestOutputProfile key shall have as the value of that key the same indirect object, which shall be a valid ICC profile stream",
				"Se l`array OutputIntents di un file contiene piu di una voce, come nel caso in cui un file sia conforme a questa parte della norma ISO 19005 e allo stesso tempo a PDF/X-4 o PDF/E-1, tutte le voci che contengono una chiave DestOutputProfile devono avere come valore di tale chiave lo stesso oggetto indiretto, che deve essere un flusso di profilo ICC valido.");
		it = it.replace(
				"If a files OutputIntents array contains more than one entry, then all entries that contain a DestOutputProfile key shall have as the value of that key the same indirect object, which shall be a valid ICC profile stream",
				"Se l`array OutputIntents di un file contiene piu di una voce, tutte le voci che contengono una chiave DestOutputProfile devono avere come valore di tale chiave lo stesso oggetto indiretto, che deve essere un flusso di profilo ICC valido.");
		it = it.replace("If a ca key is present in an ExtGState object, its value shall be 1.0",
				"Se una chiave ca e presente in un oggetto ExtGState, il suo valore sara 1.0.");
		it = it.replace(
				"If an annotation dictionarys Subtype key has a value of Widget and its FT key has a value of Btn, the value of the N key shall be an appearance subdictionary",
				"Se la chiave Subtype di un dizionario di annotazioni ha un valore di Widget e la sua chiave FT ha un valore di Btn, il valore della chiave N deve essere un sottodizionario di aspetto.");
		it = it.replace(
				"If an annotation dictionarys Subtype key has value other than Widget, or if FT key associated with Widget annotation has value other than Btn, the value of the N key shall be an appearance stream",
				"Se la chiave Subtype di un dizionario di annotazioni ha un valore diverso da Widget o se la chiave FT associata all`annotazione Widget ha un valore diverso da Btn, il valore della chiave N sara un flusso di aspetto.");
		it = it.replace("If an Image dictionary contains the BitsPerComponent key, its value shall be 1, 2, 4 or 8",
				"Se un dizionario Immagine contiene la chiave BitsPerComponent, il suo valore sara 1, 2, 4 o 8.");
		it = it.replace("If an Image dictionary contains the BitsPerComponent key, its value shall be 1, 2, 4, 8 or 16",
				"Se il dizionario delle immagini contiene la chiave BitsPerComponent, il suo valore e 1, 2, 4, 8 o 16.");
		it = it.replace("If an Image dictionary contains the Interpolate key, its value shall be false",
				"Se il dizionario di un`immagine contiene la chiave Interpolate, il suo valore sara false.");
		it = it.replace("If an Image dictionary contains the Interpolate key, its value shall be false.",
				"Se un dizionario Immagine contiene la chiave Interpolate, il suo valore sara false.");
		it = it.replace(
				"If an optional content configuration dictionary contains the Order key, the array which is the value of this Order key shall contain references to all OCGs in the conforming file",
				"Se un dizionario di configurazione del contenuto opzionale contiene la chiave Order, l`array che e il valore di questa chiave Order deve contenere i riferimenti a tutti gli OCG del file conforme.");
		it = it.replace("If an SMask key appears in an ExtGState dictionary, its value shall be None",
				"Se una chiave SMask compare in un dizionario ExtGState, il suo valore deve essere None.");
		it = it.replace(
				"If an uncalibrated colour space is used in a file then that file shall contain a PDF/A-1 OutputIntent, as defined in 6.2.2",
				"Se in un file viene utilizzato uno spazio colore non calibrato, tale file deve contenere un OutputIntent PDF/A-1, come definito in 6.2.2.");
		it = it.replace(
				"If DocMDP is present, then the Signature References dictionary (ISO 32000-1:2008, 12.8.1, Table 253) shall not contain the keys DigestLocation, DigestMethod, and DigestValue",
				"Se DocMDP e presente, il dizionario Signature References (ISO 32000-1:2008, 12.8.1, Tabella 253) non deve contenere le chiavi DigestLocation, DigestMethod e DigestValue.");
		it = it.replace(
				"If present, the F keys Print flag bit shall be set to 1 and its Hidden, Invisible, ToggleNoView, and NoView flag bits shall be set to 0",
				"Se presente, il bit del flag Print della chiave F deve essere impostato su 1 e i bit dei flag Hidden, Invisible, ToggleNoView e NoView devono essere impostati su 0.");
		it = it.replace(
				"If the document does not contain a PDF/A OutputIntent, then all Page objects that contain transparency shall include the Group key, and the attribute dictionary that forms the value of that Group key shall include a CS entry whose value shall be used as the default blending colour space",
				"Se il documento non contiene un OutputIntent PDF/A, tutti gli oggetti pagina che contengono trasparenza devono includere la chiave Group e il dizionario degli attributi che forma il valore di tale chiave Group deve includere una voce CS il cui valore deve essere utilizzato come spazio colore di miscelazione predefinito.");
		it = it.replace(
				"If the FontDescriptor dictionary of an embedded CID font contains a CIDSet stream, then it shall identify all CIDs which are present in the font program, regardless of whether a CID in the font is referenced or used by the PDF or not",
				"Se il dizionario FontDescriptor di un font CID incorporato contiene un flusso CIDSet, deve identificare tutti i CID presenti nel programma di font, indipendentemente dal fatto che un CID del font sia o meno referenziato o utilizzato dal PDF.");
		it = it.replace(
				"If the FontDescriptor dictionary of an embedded Type 1 font contains a CharSet string, then it shall list the character names of all glyphs present in the font program, regardless of whether a glyph in the font is referenced or used by the PDF or not",
				"Se il dizionario FontDescriptor di un font Type 1 incorporato contiene una stringa CharSet, allora elenca i nomi dei caratteri di tutti i glifi presenti nel programma del font, indipendentemente dal fatto che un glifo del font sia o meno referenziato o utilizzato dal PDF.");
		it = it.replace(
				"If the Lang entry is present in the document catalog dictionary or in a structure element dictionary or property list, its value shall be a language identifier as defined by RFC 1766, Tags for the Identification of Languages, as described in PDF Reference 9.8.1",
				"Se la voce Lang e presente nel dizionario del catalogo del documento o in un dizionario di elementi della struttura o in un elenco di proprieta, il suo valore deve essere un identificatore di lingua come definito da RFC 1766, Tag per l`identificazione delle lingue, come descritto in PDF Reference 9.8.1.");
		it = it.replace(
				"If the Lang entry is present in the documents Catalog dictionary or in a structure element dictionary or property list, its value shall be a language identifier as described in ISO 32000-1:2008, 14.9.2.",
				"Se la voce Lang e presente nel dizionario del catalogo del documento o in un dizionario di elementi di struttura o in un elenco di proprieta, il suo valore deve essere un identificatore di lingua come descritto in ISO 32000-1:2008, 14.9.2.");
		it = it.replace(
				"If the number of colour space specifications in the JPEG2000 data is greater than 1, there shall be exactly one colour space specification that has the value 0x01 in the APPROX field",
				"Se il numero di specifiche dello spazio colore nei dati JPEG2000 e maggiore di 1, deve esserci esattamente una specifica dello spazio colore con il valore 0x01 nel campo APPROX.");
		it = it.replace("If the value of the Encoding entry is a dictionary, it shall not contain a Differences entry",
				"Se il valore della voce Encoding e un dizionario, non deve contenere una voce Differences.");
		it = it.replace(
				"In a cross reference subsection header the starting object number and the range shall be separated by a single SPACE character (20h)",
				"Nell`intestazione di una sottosezione di riferimento incrociato, il numero dell`oggetto iniziale e l`intervallo devono essere separati da un singolo carattere SPAZIO (20h).");
		it = it.replace(
				"In a linearized PDF, if the ID keyword is present in both the first page trailer dictionary and the last trailer dictionary, the value to both instances of the ID keyword shall be identical",
				"In un PDF linearizzato, se la parola chiave ID e presente sia nel dizionario del trailer della prima pagina che in quello dell`ultimo trailer, il valore di entrambe le istanze della parola chiave ID deve essere identico.");
		it = it.replace(
				"In addition, all non-symbolic TrueType fonts shall not define a Differences array unless all of the glyph names in the Differences array are listed in the Adobe Glyph List and the embedded font program contains at least the Microsoft Unicode (3,1 – Platform ID = 3, Encoding ID = 1) encoding in the cmap table",
				"Inoltre, tutti i font TrueType non simbolici non devono definire un array di differenze a meno che tutti i nomi dei glifi nell`array di differenze non siano elencati nell`Elenco glifi Adobe e il programma di font incorporato contenga almeno la codifica Microsoft Unicode (3,1 - ID piattaforma = 3, ID codifica = 1) nella tabella cmap.");
		it = it.replace(
				"In addition, the Crypt filter shall not be used unless the value of the Name key in the decode parameters dictionary is Identity.",
				"Inoltre, il filtro Crypt non deve essere utilizzato a meno che il valore della chiave Name nel dizionario dei parametri di decodifica non sia Identity.");
		it = it.replace(
				"In addition, the DestOutputProfileRef key, as defined in ISO 15930-7:2010, Annex A, shall not be present in any PDF/X OutputIntent",
				"Inoltre, la chiave DestOutputProfileRef, definita in ISO 15930-7:2010, Annex A, non deve essere presente in nessun OutputIntent PDF/X.");
		it = it.replace(
				"In all cases for TrueType fonts that are to be rendered, character codes shall be able to be mapped to glyphs according to ISO 32000-1:2008, 9.6.6.4 without the use of a non-standard mapping chosen by the conforming processor",
				"In tutti i casi di font TrueType da rendere, i codici dei caratteri devono poter essere mappati ai glifi secondo la norma ISO 32000-1:2008, 9.6.6.4 senza l`uso di una mappatura non standard scelta dall`elaboratore conforme.");
		it = it.replace(
				"In evaluating equivalence, the PDF objects shall be compared, rather than the computational result of the use of those PDF objects.",
				"Nel valutare l`equivalenza, devono essere confrontati gli oggetti PDF e non il risultato computazionale dell`uso di tali oggetti PDF.");
		it = it.replace(
				"In other words, the Registry and Ordering strings of the CIDSystemInfo dictionaries for that font shall be identical, unless the value of the Encoding key in the font dictionary is Identity-H or Identity-V",
				"In altre parole, le stringhe Registry e Ordering dei dizionari CIDSystemInfo per quel font devono essere identiche, a meno che il valore della chiave Encoding nel dizionario del font non sia Identity-H o Identity-V.");
		it = it.replace("Interactive form fields shall not perform actions of any type",
				"I campi interattivi dei moduli non devono eseguire azioni di alcun tipo.");
		it = it.replace(
				"ISO 32000-1:2008, 9.7.4, Table 117 requires that all embedded Type 2 CIDFonts in the CIDFont dictionary shall contain a CIDToGIDMap entry that shall be a stream mapping from CIDs to glyph indices or the name Identity, as described in ISO 32000-1:2008, 9.7.4, Table 117",
				"ISO 32000-1:2008, 9.7.4, Tabella 117 richiede che tutti i CIDFont incorporati di Tipo 2 nel dizionario CIDFont contengano una voce CIDToGIDMap che deve essere una mappatura di flusso dai CID agli indici di glifo o al nome Identity, come descritto in ISO 32000-1:2008, 9.7.4, Tabella 117.");
		it = it.replace("JPEG2000 enumerated colour space 19 (CIEJab) shall not be used",
				"Lo spazio colore enumerato 19 di JPEG2000 (CIEJab) non deve essere utilizzato.");
		it = it.replace("Largest Integer value is 2,147,483,647.", "Il valore intero piu grande e 2.147.483.647.");
		it = it.replace(
				"LastChar - integer - (Required except for the standard 14 fonts) The last character code defined in the fonts Widths array",
				"LastChar - intero - (Richiesto tranne che per i font standard 14) L`ultimo codice di carattere definito nell`array Widths del font.");
		it = it.replace("Maximum capacity of a dictionary (in entries) is 4095",
				"La capacita massima di un dizionario (in voci) e 4095.");
		it = it.replace("Maximum capacity of an array (in elements) is 8191",
				"La capacita massima di un array (in elementi) e 8191.");
		it = it.replace("Maximum depth of graphics state nesting by q and Q operators is 28",
				"La profondita massima di annidamento degli stati grafici da parte degli operatori q e Q e 28.");
		it = it.replace("Maximum length of a name (in bytes) is 127",
				"La lunghezza massima di un nome (in byte) e 127.");
		it = it.replace("Maximum length of a string (in bytes) is 65535",
				"La lunghezza massima di una stringa (in byte) e 65535.");
		it = it.replace("Maximum number of DeviceN components is 8", "Il numero massimo di componenti DeviceN e 8.");
		it = it.replace("Maximum number of indirect objects in a PDF file is 8,388,607",
				"Numero massimo di oggetti indiretti in un file PDF: 8.388.607.");
		it = it.replace("Maximum value of a CID (character identifier) is 65,535",
				"Il valore massimo di un CID (identificatore di caratteri) e 65.535.");
		it = it.replace("Metadata object stream dictionaries shall not contain the Filter key",
				"I dizionari dei flussi di oggetti di metadati non devono contenere la chiave Filtro.");
		it = it.replace("Named actions other than NextPage, PrevPage, FirstPage, and LastPage shall not be permitted",
				"Non sono consentite azioni denominate diverse da NextPage, PrevPage, FirstPage e LastPage.");
		it = it.replace(
				"No data can follow the last end-of-file marker except a single optional end-of-line marker as described in ISO 32000-1:2008, 7.5.5",
				"Nessun dato puo seguire l`ultimo marcatore di fine file, eccetto un singolo marcatore opzionale di fine riga come descritto in ISO 32000-1:2008, 7.5.5.");
		it = it.replace("No data shall follow the last end-of-file marker except a single optional end-of-line marker",
				"Nessun dato deve seguire l`ultimo marcatore di fine file, tranne un singolo marcatore opzionale di fine riga.");
		it = it.replace(
				"No keys other than UR3 and DocMDP shall be present in a permissions dictionary (ISO 32000-1:2008, 12.8.4, Table 258)",
				"In un dizionario dei permessi non devono essere presenti chiavi diverse da UR3 e DocMDP (ISO 32000-1:2008, 12.8.4, Tabella 258).");
		it = it.replace(
				"Only blend modes that are specified in ISO 32000-1:2008 shall be used for the value of the BM key in an extended graphic state dictionary",
				"Per il valore della chiave BM in un dizionario di stato grafico esteso devono essere utilizzate solo le modalita di fusione specificate nella norma ISO 32000-1:2008.");
		it = it.replace(
				"Otherwise, the corresponding Registry and Ordering strings in both CIDSystemInfo dictionaries shall be identical, and the value of the Supplement key in the CIDSystemInfo dictionary of the CIDFont shall be less than or equal to the Supplement key in the CIDSystemInfo dictionary of the CMap",
				"Altrimenti, le stringhe di registro e di ordinamento corrispondenti in entrambi i dizionari CIDSystemInfo devono essere identiche e il valore della chiave Supplement nel dizionario CIDSystemInfo del CIDFont deve essere inferiore o uguale alla chiave Supplement nel dizionario CIDSystemInfo della CMap.");
		it = it.replace(
				"Overprint mode (as set by the OPM value in an ExtGState dictionary) shall not be one (1) when an ICCBased CMYK colour space is used for stroke and overprinting for stroke is set to true, or when ICCBased CMYK colour space is used for fill and overprinting for fill is set to true, or both",
				"La modalita di sovrastampa (impostata dal valore OPM in un dizionario ExtGState) non deve essere uno (1) quando viene utilizzato uno spazio colore ICCBased CMYK per il tratto e la sovrastampa per il tratto e impostata su true, o quando viene utilizzato uno spazio colore ICCBased CMYK per il riempimento e la sovrastampa per il riempimento e impostata su true, o entrambi.");
		it = it.replace("pdfaExtension:schemas - Bag Schema - Description of extension schemas",
				"pdfaExtension:schemas - Bag Schema - Descrizione degli schemi di estensione.");
		it = it.replace(
				"Properties specified in XMP form shall use either the predefined schemas defined in XMP Specification, or extension schemas that comply with XMP Specification",
				"Le proprieta specificate in forma XMP devono utilizzare gli schemi predefiniti definiti nelle specifiche XMP o gli schemi di estensione conformi alle specifiche XMP.");
		it = it.replace("Property amd of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"La proprieta amd dello schema di identificazione PDF/A deve avere il prefisso dello spazio dei nomi pdfaid.");
		it = it.replace("Property conformance of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"La proprieta conformance dello schema di identificazione PDF/A ha il prefisso dello spazio dei nomi pdfaid.");
		it = it.replace("Property corr of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"La proprieta corr dello schema di identificazione PDF/A deve avere il prefisso dello spazio dei nomi pdfaid.");
		it = it.replace("Property part of the PDF/A Identification Schema shall have namespace prefix pdfaid",
				"La proprieta part dello schema di identificazione PDF/A deve avere il prefisso dello spazio dei nomi pdfaid.");
		it = it.replace("Smallest integer value is -2,147,483,648", "Il valore intero piu piccolo e -2.147.483.648.");
		it = it.replace("Standard tags shall not be remapped to a non-standard type",
				"I tag standard non devono essere rimappati a un tipo non standard.");
		it = it.replace(
				"Subtype - name - (Required) The type of font; must be Type1 for Type 1 fonts, MMType1 for multiple master fonts, TrueType for TrueType fonts Type3 for Type 3 fonts, Type0 for Type 0 fonts and CIDFontType0 or CIDFontType2 for CID fonts",
				"Sottotipo - nome - (Richiesto) Il tipo di font; deve essere Type1 per i font di tipo 1, MMType1 per i font master multipli, TrueType per i font TrueType Type3 per i font di tipo 3, Type0 per i font di tipo 0 e CIDFontType0 o CIDFontType2 per i font CID.");
		it = it.replace("Symbolic TrueType fonts shall not contain an Encoding entry in the font dictionary",
				"I font TrueType simbolici non devono contenere una voce Encoding nel dizionario dei font.");
		it = it.replace("The % character of the file header shall occur at byte offset 0 of the file.",
				"Il carattere % dell`intestazione del file deve trovarsi al byte 0 del file.");
		it = it.replace(
				"The aforementioned EOL marker shall be immediately followed by a % (25h) character followed by at least four bytes, each of whose encoded byte values shall have a decimal value greater than 127",
				"Il suddetto marcatore EOL deve essere immediatamente seguito da un carattere % (25h) seguito da almeno quattro byte, ciascuno dei quali deve avere un valore decimale superiore a 127.");
		it = it.replace("The AS key shall not appear in any optional content configuration dictionary",
				"La chiave AS non deve comparire in nessun dizionario di configurazione dei contenuti opzionali.");
		it = it.replace("The bit-depth of the JPEG2000 data shall have a value in the range 1 to 38.",
				"La profondita in bit dei dati JPEG2000 deve avere un valore compreso nell`intervallo 1-38.");
		it = it.replace("The bytes attribute shall not be used in the header of an XMP packet",
				"L`attributo byte non deve essere utilizzato nell`intestazione di un pacchetto XMP.");
		it = it.replace(
				"The Catalog dictionary of a conforming file shall contain the Metadata key whose value is a metadata stream as defined in ISO 32000-1:2008, 14.3.2",
				"Il dizionario Catalog di un file conforme deve contenere la chiave Metadata il cui valore e un flusso di metadati come definito nella norma ISO 32000-1:2008, 14.3.2.");
		it = it.replace(
				"The cmap table in the embedded font program for a symbolic TrueType font shall contain either exactly one encoding or it shall contain, at least, the Microsoft Symbol (3,0 - Platform ID=3, Encoding ID=0) encoding",
				"La tabella cmap del programma di font incorporato per un font TrueType simbolico deve contenere esattamente una codifica oppure deve contenere almeno la codifica Microsoft Symbol (3,0 - Platform ID=3, Encoding ID=0).");
		it = it.replace("The document catalog dictionary of a conforming file shall contain the Metadata key",
				"Il dizionario del catalogo documenti di un file conforme deve contenere la chiave Metadati.");
		it = it.replace(
				"The document catalog dictionary shall include a MarkInfo dictionary containing an entry, Marked, whose value shall be true",
				"Il dizionario del catalogo dei documenti include un dizionario MarkInfo contenente una voce, Marked, il cui valore deve essere true.");
		it = it.replace(
				"The document catalog dictionary shall include a MarkInfo dictionary with a Marked entry in it, whose value shall be true",
				"Il dizionario del catalogo dei documenti deve includere un dizionario MarkInfo con una voce Marked, il cui valore deve essere true.");
		it = it.replace("The document catalog dictionary shall not contain a key with the name OCProperties",
				"Il dizionario del catalogo dei documenti non contiene una chiave con il nome OCProperties.");
		it = it.replace(
				"The document catalog dictionary shall not include an AA entry for an additional-actions dictionary",
				"Il dizionario del catalogo dei documenti non contiene una voce AA per un dizionario delle azioni aggiuntive.");
		it = it.replace("The document catalog shall not contain the Requirements key",
				"Il catalogo dei documenti non deve contenere la chiave Requirements.");
		it = it.replace("The documents Catalog shall not include an AA entry for an additional-actions dictionary",
				"Il catalogo del documento non deve includere una voce AA per un dizionario delle azioni aggiuntive.");
		it = it.replace(
				"The documents interactive form dictionary that forms the value of the AcroForm key in the documents Catalog of a PDF/A-2 file, if present, shall not contain the XFA key",
				"Il dizionario dei moduli interattivi del documento che costituisce il valore della chiave AcroForm nel catalogo del documento di un file PDF/A-2, se presente, non deve contenere la chiave XFA.");
		it = it.replace("The encoding attribute shall not be used in the header of an XMP packet",
				"L`attributo encoding non deve essere utilizzato nell`intestazione di un pacchetto XMP.");
		it = it.replace("The endstream keyword shall be preceded by an EOL marker",
				"La parola chiave endstream deve essere preceduta da un marcatore EOL.");
		it = it.replace(
				"The extension schema container schema uses the namespace URI http://www.aiim.org/pdfa/ns/extension/.",
				"Lo schema contenitore dello schema di estensione utilizza lo spazio dei nomi URI http://www.aiim.org/pdfa/ns/extension/.");
		it = it.replace(
				"The F key’s Print flag bit shall be set to 1 and its Hidden, Invisible and NoView flag bits shall be set to 0",
				"Il bit del flag Print della chiave F deve essere impostato su 1 e i suoi bit dei flag Hidden, Invisible e NoView devono essere impostati su 0.");
		it = it.replace(
				"The file header line shall be immediately followed by a comment consisting of a % character followed by at least four characters, each of whose encoded byte values shall have a decimal value greater than 127",
				"La riga di intestazione del file deve essere immediatamente seguita da un commento costituito da un carattere % seguito da almeno quattro caratteri, ognuno dei quali deve avere un valore decimale superiore a 127.");
		it = it.replace(
				"The file header shall begin at byte zero and shall consist of %PDF-1.n followed by a single EOL marker, where n is a single digit number between 0 (30h) and 7 (37h)",
				"L`intestazione del file inizia dal byte zero e consiste in %PDF-1.n seguito da un singolo marcatore EOL, dove `n` e un numero a una cifra compreso tra 0 (30h) e 7 (37h).");
		it = it.replace("The file specification dictionary for an embedded file shall contain the F and UF keys",
				"Il dizionario delle specifiche del file per un file incorporato contiene le chiavi F e UF.");
		it = it.replace(
				"The file trailer dictionary shall contain the ID keyword whose value shall be File Identifiers as defined in ISO 32000-1:2008, 14.4",
				"Il dizionario del rimorchio del file contiene la parola chiave ID, il cui valore e File Identifiers come definito in ISO 32000-1:2008, 14.4.");
		it = it.replace("The file trailer dictionary shall contain the ID keyword.",
				"Il dizionario del file trailer contiene la parola chiave ID.");
		it = it.replace(
				"The file trailer referred to is either the last trailer dictionary in a PDF file, as described in PDF Reference 3.4.4 and 3.4.5, or the first page trailer in a linearized PDF file, as described in PDF Reference F.2",
				"Il file trailer a cui si fa riferimento e il dizionario dell`ultimo trailer di un file PDF, come descritto nei riferimenti PDF 3.4.4 e 3.4.5, o il trailer della prima pagina di un file PDF linearizzato, come descritto nel riferimento PDF F.2.");
		it = it.replace(
				"The first line of a PDF file is a header identifying the version of the PDF specification to which the file conforms",
				"La prima riga di un file PDF e un`intestazione che identifica la versione delle specifiche PDF a cui il file e conforme.");
		it = it.replace(
				"The following keys, if present in an ExtGState object, shall have the values shown: BM - Normal or Compatible",
				"Le seguenti chiavi, se presenti in un oggetto ExtGState, devono avere i valori indicati: BM - Normale o compatibile.");
		it = it.replace("The following keys, if present in an ExtGState object, shall have the values shown: CA - 1.0",
				"Le seguenti chiavi, se presenti in un oggetto ExtGState, devono avere i valori indicati: CA - 1.0.");
		it = it.replace(
				"The Font dictionary of all fonts shall define the map of all used character codes to Unicode values, either via a ToUnicode entry, or other mechanisms as defined in ISO 19005-2, 6.2.11.7.2",
				"Il dizionario dei caratteri di tutti i font deve definire la mappa di tutti i codici dei caratteri utilizzati per i valori Unicode, tramite una voce ToUnicode o altri meccanismi come definito in ISO 19005-2, 6.2.11.7.2.");
		it = it.replace(
				"The font dictionary shall include a ToUnicode entry whose value is a CMap stream object that maps character codes to Unicode values, as described in PDF Reference 5.9, unless the font meets any of the following three conditions: (*) fonts that use the predefined encodings MacRomanEncoding, MacExpertEncoding or WinAnsiEncoding, or that use the predefined Identity-H or Identity-V CMaps; (*) Type 1 fonts whose character names are taken from the Adobe standard Latin character set or the set of named characters in the Symbol font, as defined in PDF Reference Appendix D; (*) Type 0 fonts whose descendant CIDFont uses the Adobe-GB1, Adobe-CNS1, Adobe-Japan1 or Adobe-Korea1 character collections",
				"Il dizionario dei font deve includere una voce ToUnicode il cui valore e un oggetto di flusso CMap che mappa i codici dei caratteri in valori Unicode, come descritto in PDF Reference 5. 9, a meno che il font non soddisfi una delle tre condizioni seguenti: (*) font che utilizzano le codifiche predefinite MacRomanEncoding, MacExpertEncoding o WinAnsiEncoding, o che utilizzano le CMap predefinite Identity-H o Identity-V; (*) font di tipo 1 i cui nomi dei caratteri sono tratti dall`insieme di caratteri latini standard di Adobe o dall`insieme di caratteri denominati nel font Symbol, come definito nell`Appendice D di PDF Reference; (*) font di tipo 0 il cui discendente CIDFont utilizza le raccolte di caratteri Adobe-GB1, Adobe-CNS1, Adobe-Japan1 o Adobe-Korea1.");
		it = it.replace(
				"The font programs for all fonts used for rendering within a conforming file shall be embedded within that file, as defined in ISO 32000-1:2008, 9.9",
				"I programmi dei font per tutti i font utilizzati per il rendering all`interno di un file conforme devono essere incorporati all`interno di tale file, come definito in ISO 32000-1:2008, 9.9.");
		it = it.replace(
				"The font programs for all fonts used within a conforming file shall be embedded within that file, as defined in PDF Reference 5.8, except when the fonts are used exclusively with text rendering mode 3",
				"I programmi dei font per tutti i font utilizzati all`interno di un file conforme devono essere incorporati all`interno di tale file, come definito in PDF Reference 5.8, tranne quando i font sono utilizzati esclusivamente con la modalita di rendering del testo 3.");
		it = it.replace("The generation number and obj keyword shall be separated by a single white-space character.",
				"Il numero di generazione e la parola chiave obj devono essere separati da un singolo carattere di spazio bianco.");
		it = it.replace("The Hide action shall not be permitted (Corrigendum 2)",
				"L`azione Nascondi non e consentita (rettifica 2).");
		it = it.replace("The keyword Encrypt shall not be used in the trailer dictionary",
				"La parola chiave Encrypt non deve essere usata nel dizionario dei trailer.");
		it = it.replace(
				"The Launch, Sound, Movie, ResetForm, ImportData and JavaScript actions shall not be permitted.",
				"Le azioni Launch, Sound, Movie, ResetForm, ImportData e JavaScript non sono consentite.");
		it = it.replace(
				"The Launch, Sound, Movie, ResetForm, ImportData, Hide, SetOCGState, Rendition, Trans, GoTo3DView and JavaScript actions shall not be permitted.",
				"Le azioni Launch, Sound, Movie, ResetForm, ImportData, Hide, SetOCGState, Rendition, Trans, GoTo3DView e JavaScript non sono consentite.");
		it = it.replace(
				"The logical structure of the conforming file shall be described by a structure hierarchy rooted in the StructTreeRoot entry of the document catalog dictionary, as described in PDF Reference 9.6",
				"La struttura logica del file conforme deve essere descritta da una gerarchia di strutture con radice nella voce StructTreeRoot del dizionario del catalogo dei documenti, come descritto in PDF Reference 9.6.");
		it = it.replace(
				"The logical structure of the conforming file shall be described by a structure hierarchy rooted in the StructTreeRoot entry of the documents Catalog dictionary, as described in ISO 32000-1:2008, 14.7",
				"La struttura logica del file conforme deve essere descritta da una gerarchia di strutture con radice nella voce StructTreeRoot del dizionario del catalogo del documento, come descritto nella norma ISO 32000-1:2008, 14.7.");
		it = it.replace("The LZWDecode filter shall not be permitted", "Il filtro LZWDecode non e consentito.");
		it = it.replace(
				"The metadata stream shall conform to XMP Specification and well formed PDFAExtension Schema for all extensions",
				"Il flusso di metadati deve essere conforme alle specifiche XMP e allo schema PDFAExtension ben formato per tutte le estensioni.");
		it = it.replace(
				"The NeedAppearances flag of the interactive form dictionary shall either not be present or shall be false",
				"Il flag NeedAppearances del dizionario del modulo interattivo non deve essere presente o deve essere falso.");
		it = it.replace(
				"The number of color components in the color space described by the ICC profile data must match the number of components actually in the ICC profile.",
				"Il numero di componenti del colore nello spazio colore descritto dai dati del profilo ICC deve corrispondere al numero di componenti effettivamente presenti nel profilo ICC.");
		it = it.replace("The number of colour channels in the JPEG2000 data shall be 1, 3 or 4",
				"Il numero di canali colore nei dati JPEG2000 deve essere 1, 3 o 4.");
		it = it.replace("The obj and endobj keywords shall each be followed by an EOL marker",
				"Le parole chiave obj e endobj devono essere seguite ciascuna da un marcatore EOL.");
		it = it.replace("The object number and endobj keyword shall each be preceded by an EOL marker.",
				"Il numero dell`oggetto e la parola chiave endobj devono essere preceduti da un marcatore EOL.");
		it = it.replace("The object number and generation number shall be separated by a single white-space character.",
				"Il numero dell`oggetto e il numero di generazione devono essere separati da un singolo carattere di spazio bianco.");
		it = it.replace(
				"The only valid values of this key in PDF 1.4 are Type1C - Type 1–equivalent font program represented in the Compact Font Format (CFF) and CIDFontType0C - Type 0 CIDFont program represented in the Compact Font Format (CFF)",
				"Gli unici valori validi di questa chiave in PDF 1.4 sono Type1C - programma di font equivalente al tipo 1 rappresentato nel Compact Font Format (CFF) e CIDFontType0C - programma CIDFont di tipo 0 rappresentato nel Compact Font Format (CFF).");
		it = it.replace(
				"The only valid values of this key in PDF 1.7 are Type1C - Type 1–equivalent font program represented in the Compact Font Format (CFF), CIDFontType0C - Type 0 CIDFont program represented in the Compact Font Format (CFF) and OpenType - OpenType® font program, as described in the OpenType Specification v.1.4",
				"Gli unici valori validi di questa chiave in PDF 1.7 sono Type1C - programma di font equivalente al tipo 1 rappresentato nel Compact Font Format (CFF), CIDFontType0C - programma CIDFont di tipo 0 rappresentato nel Compact Font Format (CFF) e OpenType - programma di font OpenType® , come descritto nella Specifica OpenType v.1.4.");
		it = it.replace("The Page dictionary shall not include an AA entry for an additional-actions dictionary",
				"Il dizionario della pagina non deve includere una voce AA per un dizionario delle azioni aggiuntive.");
		it = it.replace(
				"The PDF Signature (a DER-encoded PKCS#7 binary data object) shall be placed into the Contents entry of the signature dictionary.",
				"La firma PDF (un oggetto dati binario PKCS#7 codificato in DER) deve essere inserita nella voce Contents del dizionario della firma.");
		it = it.replace(
				"The PDF/A version and conformance level of a file shall be specified using the PDF/A Identification extension schema",
				"La versione PDF/A e il livello di conformita di un file devono essere specificati utilizzando lo schema di estensione PDF/A Identification.");
		it = it.replace("The PKCS#7 object shall conform to the PKCS#7 specification in RFC 2315.",
				"L`oggetto PKCS#7 e conforme alle specifiche PKCS#7 in RFC 2315.");
		it = it.replace(
				"The profile stream that is the value of the DestOutputProfile key shall either be an output profile (Device Class = prtr) or a monitor profile (Device Class = mntr).",
				"Il flusso di profili che costituisce il valore della chiave DestOutputProfile deve essere un profilo di uscita (Device Class = prtr) o un profilo di monitoraggio (Device Class = mntr).");
		it = it.replace(
				"The profile that forms the stream of an ICCBased colour space shall conform to ICC.1:1998-09, ICC.1:2001-12, ICC.1:2003-09 or ISO 15076-1",
				"Il profilo che costituisce il flusso di uno spazio colore ICCBased deve essere conforme alle norme ICC.1:1998-09, ICC.1:2001-12, ICC.1:2003-09 o ISO 15076-1.");
		it = it.replace("The profiles shall have a colour space of either GRAY, RGB, or CMYK",
				"I profili devono avere uno spazio colore GRAY, RGB o CMYK.");
		it = it.replace("The required schema namespace prefix is pdfaExtension.",
				"Il prefisso dello spazio dei nomi dello schema richiesto e pdfaExtension.");
		it = it.replace(
				"The size of any of the page boundaries described in ISO 32000-1:2008, 14.11.2 shall not be less than 3 units in either direction, nor shall it be greater than 14 400 units in either direction",
				"La dimensione dei limiti di pagina descritti in ISO 32000-1:2008, 14.11.2 non deve essere inferiore a 3 unita in entrambe le direzioni, ne superiore a 14 400 unita in entrambe le direzioni.");
		it = it.replace(
				"The stream keyword shall be followed either by a CARRIAGE RETURN (0Dh) and LINE FEED (0Ah) character sequence or by a single LINE FEED (0Ah) character.",
				"La parola chiave stream deve essere seguita da una sequenza di caratteri CARRIAGE RETURN (0Dh) e LINE FEED (0Ah) o da un singolo carattere LINE FEED (0Ah).");
		it = it.replace(
				"The stream keyword shall be followed either by a CARRIAGE RETURN (0Dh) and LINE FEED (0Ah) character sequence or by a single LINE FEED character.",
				"La parola chiave stream deve essere seguita da una sequenza di caratteri CARRIAGE RETURN (0Dh) e LINE FEED (0Ah) o da un singolo carattere LINE FEED.");
		it = it.replace("The subtype is the value of the Subtype key, if present, in the font file stream dictionary.",
				"Il sottotipo e il valore della chiave Subtype, se presente, nel dizionario del flusso del file font.");
		it = it.replace(
				"The TransferFunction key in a halftone dictionary shall be used only as required by ISO 32000-1",
				"La chiave TransferFunction in un dizionario dei mezzitoni deve essere utilizzata solo come richiesto dalla norma ISO 32000-1.");
		it = it.replace(
				"The Unicode values specified in the ToUnicode CMap shall all be greater than zero (0), but not equal to either U+FEFF or U+FFFE",
				"I valori Unicode specificati nella CMap ToUnicode devono essere tutti maggiori di zero (0), ma non uguali a U+FEFF o U+FFFE.");
		it = it.replace("The value of pdfaid:part shall be the part number of ISO 19005 to which the file conforms",
				"Il valore di pdfaid:part deve essere il numero di parte della norma ISO 19005 a cui il file e conforme.");
		it = it.replace(
				"The value of Author entry from the document information dictionary, if present, and its analogous XMP property dc:creator shall be equivalent.",
				"Il valore della voce Author del dizionario delle informazioni del documento, se presente, e la sua analoga proprieta XMP dc:creator devono essere equivalenti.");
		it = it.replace(
				"The value of CreationDate entry from the document information dictionary, if present, and its analogous XMP property xmp:CreateDate shall be equivalent",
				"Il valore della voce CreationDate del dizionario delle informazioni del documento, se presente, e la sua analoga proprieta XMP xmp:CreateDate devono essere equivalenti.");
		it = it.replace(
				"The value of Creator entry from the document information dictionary, if present, and its analogous XMP property xmp:CreatorTool shall be equivalent",
				"Il valore della voce Creator del dizionario informativo del documento, se presente, e la sua analoga proprieta XMP xmp:CreatorTool sono equivalenti.");
		it = it.replace(
				"The value of Keywords entry from the document information dictionary, if present, and its analogous XMP property pdf:Keywords shall be equivalent",
				"Il valore della voce Keywords del dizionario delle informazioni del documento, se presente, e la sua analoga proprieta XMP pdf:Keywords sono equivalenti.");
		it = it.replace(
				"The value of ModDate entry from the document information dictionary, if present, and its analogous XMP property xmp:ModifyDate shall be equivalent",
				"Il valore della voce ModDate del dizionario delle informazioni del documento, se presente, e la sua analoga proprieta XMP xmp:ModifyDate sono equivalenti.");
		it = it.replace(
				"The value of Producer entry from the document information dictionary, if present, and its analogous XMP property pdf:Producer shall be equivalent",
				"Il valore della voce Producer del dizionario delle informazioni del documento, se presente, e la sua analoga proprieta XMP pdf:Producer sono equivalenti.");
		it = it.replace(
				"The value of Subject entry from the document information dictionary, if present, and its analogous XMP property dc:description[x-default] shall be equivalent",
				"Il valore della voce Subject del dizionario delle informazioni del documento, se presente, e la sua analoga proprieta XMP dc:description[‘x-default’] sono equivalenti.");
		it = it.replace(
				"The value of the F key in the Inline Image dictionary shall not be LZW, LZWDecode, Crypt, a value not listed in ISO 32000-1:2008, Table 6, or an array containing any such value",
				"Il valore della chiave F nel dizionario dell`immagine in linea non deve essere LZW, LZWDecode, Crypt, un valore non elencato in ISO 32000-1:2008, Tabella 6, o un array contenente uno di questi valori.");
		it = it.replace(
				"The value of the Length key specified in the stream dictionary shall match the number of bytes in the file following the LINE FEED (0Ah) character after the stream keyword and preceding the EOL marker before the endstream keyword",
				"Il valore della chiave Length specificata nel dizionario stream deve corrispondere al numero di byte nel file dopo il carattere LINE FEED (0Ah) dopo la parola chiave stream e prima del marcatore EOL prima della parola chiave endstream.");
		it = it.replace(
				"The value of the Length key specified in the stream dictionary shall match the number of bytes in the file following the LINE FEED character after the stream keyword and preceding the EOL marker before the endstream keyword",
				"Il valore della chiave Length specificata nel dizionario dei flussi deve corrispondere al numero di byte nel file dopo il carattere LINE FEED dopo la parola chiave stream e prima del marcatore EOL prima della parola chiave endstream.");
		it = it.replace("The value of the METH entry in its colr box shall be 0x01, 0x02 or 0x03.",
				"Il valore della voce METH nella casella colr deve essere 0x01, 0x02 o 0x03.");
		it = it.replace(
				"The value of Title entry from the document information dictionary, if present, and its analogous XMP property dc:title[x-default] shall be equivalent",
				"Il valore della voce Title del dizionario delle informazioni del documento, se presente, e la sua analoga proprieta XMP dc:title[‘x-default’] devono essere equivalenti.");
		it = it.replace("The XMP package must be encoded as UTF-8",
				"Il pacchetto XMP deve essere codificato come UTF-8.");
		it = it.replace(
				"The xref keyword and the cross reference subsection header shall be separated by a single EOL marker",
				"La parola chiave xref e l`intestazione della sottosezione di riferimento incrociato devono essere separate da un singolo marcatore EOL.");
		it = it.replace(
				"The xref keyword and the cross-reference subsection header shall be separated by a single EOL marker",
				"La parola chiave xref e l`intestazione della sottosezione di riferimento incrociato devono essere separate da un singolo marcatore EOL.");
		it = it.replace("There shall be no AlternatePresentations entry in the documents name dictionary",
				"Nel dizionario dei nomi del documento non deve essere presente alcuna voce AlternatePresentations.");
		it = it.replace("There shall be no PresSteps entry in any Page dictionary",
				"Non ci devono essere voci PresSteps nel dizionario delle pagine.");
		it = it.replace(
				"Type - name - (Required) The type of PDF object that this dictionary describes; must be Font for a font dictionary",
				"Type - name - (Richiesto) Il tipo di oggetto PDF che questo dizionario descrive; deve essere Font per un dizionario di font.");
		it = it.replace("Value of valueType property shall be defined",
				"Il valore della proprieta valueType deve essere definito.");
		it = it.replace(
				"When computing the digest for the file, it shall be computed over the entire file, including the signature dictionary but excluding the PDF Signature itself",
				"Quando si calcola il digest per il file, questo viene calcolato sull`intero file, compreso il dizionario della firma ma esclusa la firma PDF stessa.");
		it = it.replace(
				"Where a rendering intent is specified, its value shall be one of the four values defined in ISO 32000-1:2008, Table 70: RelativeColorimetric, AbsoluteColorimetric, Perceptual or Saturation",
				"Quando viene specificato un intento di rendering, il suo valore deve essere uno dei quattro valori definiti nella ISO 32000-1:2008, tabella 70: RelativoColorimetrico, AssolutoColorimetrico, Percettivo o Saturazione.");
		it = it.replace(
				"Where a rendering intent is specified, its value shall be one of the four values defined in PDF Reference RelativeColorimetric, AbsoluteColorimetric, Perceptual or Saturation",
				"Se viene specificato un intento di rendering, il suo valore deve essere uno dei quattro valori definiti nel riferimento PDF RelativeColorimetric, AbsoluteColorimetric, Perceptual o Saturation.");
		it = it.replace(
				"Widths - array - (Required except for the standard 14 fonts; indirect reference preferred) An array of (LastChar − FirstChar + 1) widths",
				"Widths - array - (Richiesto tranne che per i font standard 14; preferibile il riferimento indiretto) Un array di (LastChar - FirstChar + 1) larghezze.");
		it = it.replace("Xref streams shall not be used", "I flussi Xref non devono essere utilizzati");
		return it;
	}

}