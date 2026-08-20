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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ch.kostceco.tools.kosttools.runtime.Cmd;
import ch.kostceco.tools.kosttools.util.StreamGobbler;
import ch.kostceco.tools.kosttools.util.Util;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class ImageMagick {
	private static String exeDir = ".." + File.separator + "ImageMagick";
	private static String magickExe = exeDir + File.separator + "magick.exe";
	public static String NEWLINE = System.getProperty("line.separator");

	/**
	 * fuehrt eine Lesekontrolle mit ImageMagick via cmd durch und speichert das
	 * Ergebnis in ein File (Report). Gibt zurueck ob Report existiert oder nicht
	 * 
	 * @param checkFile    Datei, welche validiert werden soll
	 * @param report       Datei fuer den Report
	 * @param workDir      Temporaeres Verzeichnis
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String ob Report existiert oder nicht ggf Exception
	 */
	public static String execImageMagick(File checkFile, File report, File workDir, String dirOfJarPath)
			throws InterruptedException {
		// TODO: execImageMagick
		boolean out = true;
		File fmagickExe = new File(dirOfJarPath + File.separator + magickExe);
		// falls das File von einem vorhergehenden Durchlauf bereits existiert,
		// loeschen wir es
		if (report.exists()) {
			report.delete();
		}

		try {

			Process p = new ProcessBuilder("taskkill", "/F", "/IM", "magick.exe").start();

			int exitCode = p.waitFor();
			//System.out.println("ExitCode: " + exitCode);
			if (exitCode == 0) {
				// System.out.println("ImageMagick wurde erfolgreich gestoppt " + exitCode);
			} else if (exitCode == 128) {
				// System.out.println("ImageMagick -> kein passender Prozess " + exitCode);
			} else {
				// System.out.println("ImageMagick -> " + exitCode);
			}
		    if (p != null && p.isAlive()) {
		        p.destroy();
		        try {
		            if (!p.waitFor(1, TimeUnit.SECONDS)) {
		                p.destroyForcibly();
		            }
		        } catch (InterruptedException e) {
		            Thread.currentThread().interrupt();
		            p.destroyForcibly();
		        }
		    }
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		// ImageMagick-Befehl: PathTo_magick.exe checkFile -regard-warnings -verbose
		// info: 2> report

		// mit -limit thread 4 koennte eingeschraenkt werden wie viele Kerne maximal
		// verwendet werden besser leer lassen und dann wird das maximum genommen
		String command = "\"\"" + fmagickExe.getAbsolutePath() + "\" \"" + checkFile.getAbsolutePath()
				+ "\" -regard-warnings -verbose info: 2>\"" + report.getAbsolutePath() + "\"\"";

		// System.out.println( "command: " + command );

		Util.switchOffConsole();
		String resultExec = Cmd.execToString(command, out, workDir);
		Util.switchOnConsole();

		// ImageMagick gibt teilweise Eigenschaften raus, die replaced oder ignoriert
		// werden
		// muss

		// System.out.println( "resultExec: " + resultExec );
		resultExec = "OK";

		if (resultExec.equals("OK")) {
			if (report.exists()) {
				// alles io bleibt bei OK
			} else {
				// Datei nicht angelegt...
				resultExec = "NoReport";
			}
		}
		return resultExec;
	}

	/**
	 * fuehrt eine Kontrolle aller benoetigten Dateien von ImageMagick durch und
	 * gibt das Ergebnis als String zurueck
	 * 
	 * @param dirOfJarPath String mit dem Pfad von wo das Programm gestartet wurde
	 * @return String mit Kontrollergebnis
	 */
	public static String checkImageMagick(String dirOfJarPath) {
		// TODO: checkImageMagick
		String result = "";
		boolean checkFiles = true;
		// Pfad zum Programm existiert die Dateien?

		File fmagickExe = new File(dirOfJarPath + File.separator + magickExe);

		if (!fmagickExe.exists()) {
			if (checkFiles) {
				// erste fehlende Datei
				result = " " + exeDir + ": " + magickExe;
				checkFiles = false;
			} else {
				result = result + ", " + magickExe;
				checkFiles = false;
			}
		}

		if (checkFiles) {
			result = "OK";
		}
		return result;
	}

	/**
	 * fuehrt einen Vergleich mit ImageMagick via cmd durch und speichert das
	 * Ergebnis in ein File (Report). Gibt zurueck ob Report existiert oder nicht
	 * 
	 * @param origJpgs           Ordner mit den Jpgs aus dem Original
	 * @param repJpgs            Ordner mit den Jpgs aus der Reparatur
	 * @param imTolerance        Toleranz in %
	 * @param workDir            Temporaeres Verzeichnis
	 * @param directoryOfLogfile Log Verzeichnis
	 * @param dirOfJarPath       String mit dem Pfad von wo das Programm gestartet
	 *                           wurde
	 * @return String ob Abweichung existiert oder nicht ggf Exception
	 */
	public static String execCompare(File origJpgs, File repJpgs, String imTolerance, float percentageValid,
			File workDir, File directoryOfLogfile, String dirOfJarPath) throws InterruptedException {
		// TODO 1) execCompare
		boolean isValid = true;
		String exitStr = "";
		String imToleranceTxt = imTolerance + "_" + percentageValid;

		// Thread.sleep(20000);
		// Warten einschalten wenn Manipulationen gemacht werden sollen

		File reportId;
		File mask;
		String imgPx1 = "1";
		Boolean isSizePixel = true;
		String errorSP = "";

		StringBuffer concatenatedOutputs = new StringBuffer();

		try {
			File[] origJpgFiles = origJpgs.listFiles();

			if (origJpgFiles != null) {
				for (File origJpgFile : origJpgFiles) {
					if (origJpgFile.isFile()) {
						Boolean isPageSimy = true;
						String pageNr = origJpgFile.getName().replace(".jpg", "");
						// System.out.println("Original = " + origJpgFile.getAbsolutePath());
						File repJpgFile = new File(repJpgs + File.separator + origJpgFile.getName());
						// System.out.println("Repair = " + repJpgFile.getAbsolutePath());
						File origDatei = origJpgFile;
						File repDatei = repJpgFile;
						String diffPixel = execCompareAe(origDatei, repDatei, imTolerance, directoryOfLogfile);
						// System.out.println("diffPixel " + pageNr + " = " + diffPixel);
						if (diffPixel.equals("empty")) {
							return "empty";
						}
						if (diffPixel.equals("0")) {
							// Bilder sind Pixelidentisch
						} else {
							// Bilder weiter analysieren
							String pathToOutputId = directoryOfLogfile.getAbsolutePath() + File.separator
									+ origDatei.getName() + "_identify_report.txt";
							String pathToMask = directoryOfLogfile.getAbsolutePath() + File.separator
									+ origDatei.getName() + "_mask.jpg";
							reportId = new File(pathToOutputId);
							mask = new File(pathToMask);
							/*
							 * compare -fuzz 15% -quiet -identify -verbose -highlight-color DarkRed
							 * Image_1.jpg Image_2.jpg mask.jpg >>results_id.txt
							 * 
							 * String command = "cmd /c \"\"" + magickExe + "\" compare -fuzz " +
							 * imTolerance +
							 * " -quiet -identify -verbose -highlight-color DarkRed -lowlight-color PaleGreen \""
							 * + origDatei.getAbsolutePath() + "\" \"" + repDatei.getAbsolutePath() +
							 * "\" \"" + pathToMask + "\" >>\"" + pathToOutputId + "\"";
							 */

							String command = "cmd /c \"\"" + magickExe + "\" compare -fuzz " + imTolerance
									+ " -quiet -identify -verbose -highlight-color DarkRed -lowlight-color PaleGreen \""
									+ origDatei.getAbsolutePath() + "\" \"" + repDatei.getAbsolutePath() + "\" \""
									+ pathToMask + "\" >>\"" + pathToOutputId + "\"";
							/*
							 * Das redirect Zeichen verunmöglicht eine direkte eingabe. mit dem
							 * geschachtellten Befehl gehts: cmd /c\"urspruenlicher Befehl\"
							 */

							// System.out.println("command = " + command);

							Process proc = null;
							Runtime rt = null;

							try {
								// falls das File bereits existiert, z.B. von einem vorhergehenden Durchlauf,
								// loeschen wir es
								if (reportId.exists()) {
									reportId.delete();
								}
								if (mask.exists()) {
									mask.delete();
								}
								Util.switchOffConsole();
								rt = Runtime.getRuntime();
								proc = rt.exec(command.toString().split(" "));
								// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden
								// ist!

								// Fehleroutput holen
								StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
								// Output holen
								StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
								// Threads starten
								errorGobbler.start();
								outputGobbler.start();
								// Warte, bis wget fertig ist
								proc.waitFor();
								Util.switchOnConsole();
								// Kontrolle ob die Reports existieren
								if (!reportId.exists()) {
								    if (proc != null && proc.isAlive()) {
								        proc.destroy();
								        try {
								            if (!proc.waitFor(1, TimeUnit.SECONDS)) {
								                proc.destroyForcibly();
								            }
								        } catch (InterruptedException ep) {
								            Thread.currentThread().interrupt();
								            proc.destroyForcibly();
								        }
								    }
									return "ERROR_XML_IMCMP_NOREPORT, " + reportId.getAbsolutePath();
								}
							} catch (Exception e) {
							    if (proc != null && proc.isAlive()) {
							        proc.destroy();
							        try {
							            if (!proc.waitFor(1, TimeUnit.SECONDS)) {
							                proc.destroyForcibly();
							            }
							        } catch (InterruptedException ep) {
							            Thread.currentThread().interrupt();
							            proc.destroyForcibly();
							        }
							    }
								return "ERROR_XML_IMCMP_SERVICEFAILED, " + e.getMessage();
							}
						    if (proc != null && proc.isAlive()) {
						        proc.destroy();
						        try {
						            if (!proc.waitFor(1, TimeUnit.SECONDS)) {
						                proc.destroyForcibly();
						            }
						        } catch (InterruptedException ep) {
						            Thread.currentThread().interrupt();
						            proc.destroyForcibly();
						        }
						    }

							// Ende IMCMP direkt auszuloesen

							// TODO: Marker: ReportId und auswerten (Groesse und der Pixel)
							try {
								BufferedReader in = new BufferedReader(new FileReader(reportId));
								String line;
								String imgSize1 = "1";
								String imgSize2 = "2";
								String imgPx2 = "2";

								while ((line = in.readLine()) != null) {

									concatenatedOutputs.append(line);
									concatenatedOutputs.append(NEWLINE);

									/*
									 * Format: TIFF (Tagged Image File Format) Mime type: image/tiff
									 * 
									 * Geometry: 2469x3568+0+0
									 * 
									 * Channel statistics:
									 * 
									 * Pixels: 8809392
									 * 
									 * Geometry und Pixels scheinen immer ausgegeben zu werden
									 * 
									 * Gemotry und Pixels muessen identisch sein
									 */
									if (line.contains("  Geometry: ")) {
										if (imgSize1.equals("1")) {
											imgSize1 = line;
										} else {
											imgSize2 = line;
										}
									} else if (line.contains("  Pixels: ")) {
										if (imgPx1.equals("1")) {
											imgPx1 = line;
											// System.out.println("Seite 1: Pixel = " + line);
										} else {
											imgPx2 = line;
											// System.out.println("Seite 2: Pixel = " + line);
										}
									}

									// TODO: Marker: Auswertung und Fehlerausgabe wenn nicht bestanden.
								}
								if (imgPx1.equals("1") && imgPx2.equals("2") && imgSize1.equals("1")
										&& imgSize2.equals("2")) {
									// System.out.println("identify_report ist leer oder enthaelt nicht das was er
									// sollte");
									isSizePixel = false;
									errorSP = errorSP + " " + pageNr + " " + "ERROR_XML_IMCMP_NOREPORTTEXT";
								} else {
									// System.out.println("OK: identify_report enthaelt das was er sollte");
								}
								if (!imgPx1.equals(imgPx2)) {
									// System.out.println("die beiden Bilder haben nicht gleich viel Pixels");
									isSizePixel = false;
									errorSP = errorSP + " " + pageNr + " " + "ERROR_XML_CI_PIXELINVALID " + imgPx1 + " "
											+ imgPx2;
								} else {
									// System.out.println("OK: die beiden Bilder haben gleich viel Pixels");
								}
								if (!imgSize1.equals(imgSize2)) {
									// System.out.println("die beiden Bilder sind nicht gleich gross");
									isSizePixel = false;
									errorSP = errorSP + " " + pageNr + " " + "ERROR_XML_CI_SIZEINVALID " + imgSize1
											+ " " + imgSize2;
								} else {
									// System.out.println("OK: die beiden Bilder sind gleich gross");
								}
								if (!isSizePixel) {
									// Groessen und Pixelvergleich nicht bestanden
									isValid = false;
								}
								in.close();
							} catch (Exception e) {
								isValid = false;
								errorSP = errorSP + " " + pageNr + " " + "ERROR_XML_UNKNOWN - identify: "
										+ e.getMessage();
							}

							// TODO: Marker: Report auswerten (Bildvergleich) wenn groesse & PixelAnzahl
							// identisch
							if (isSizePixel) {
								try {
									try {
										int diffInt = Integer.parseInt(diffPixel);

										/*
										 * Bilder mit einer Abweichung (Int): Prozent ermitteln und mit
										 * percentageInvalid abgleichen
										 */
										double z1 = 0;
										double z2 = 0;
										float percentageCalc = (float) 0.0;
										float percentageCalcInv = (float) 0.0;

										/*
										 * Invalide z1 [allInt] und total px z2 aus imgPx1 "    Pixels: 8809392"
										 * extrahieren
										 */
										String lineReport = imgPx1.substring(12);
										// lineReport = 8809392
										z2 = Double.parseDouble(lineReport);
										z1 = diffInt;
										// System.out.println("Anzahl Pixel = " + z2 + " (" + lineReport + ") "+"Anzahl
										// Error = " + z1 + " (" + diffInt + ")");

										percentageCalc = (float) (100 - (100 / z2 * z1));
										percentageCalcInv = 100 - percentageCalc;
										// System.out.println("Abweichung = " + percentageCalcInv + "%");

										// Prozentzahlen vergleichen
										if (percentageValid > percentageCalc) {
											// Bilder mit einer groesseren Abweichung
											isValid = false;
											isPageSimy = false;

											exitStr = " " + pageNr + " " + "ERROR_XML_CI_CIINVALID " + percentageCalcInv
													+ " " + z2 + " " + imToleranceTxt + " " + z1;
											// System.out.println("");
											/*
											 * System.out.println("Aehnlich in % = " + percentageCalc + " (Abweichung: "
											 * + percentageCalcInv + ")   " + "Minimal geforderte Aehnichkeit in % = " +
											 * percentageValid + " -> " + pageNr + " " + isPageSimy);
											 */
										}

									} catch (NumberFormatException e) {
										// Anzahl der Abweichenden Pixel konnte nicht nach int umgewandelt werden
										isValid = false;
										exitStr = " " + pageNr + " " + "ERROR_XML_CI_CIINVALIDSTR " + " " + diffPixel;
									}
								} catch (Exception e) {
									return "Compare failed 2";
								}
							}
							// reports der einzelnen Seite immer loeschen
							if (reportId.exists()) {
								reportId.delete();
							}
							if (isPageSimy) {
								// mask der einzelnen Seite loeschen wenn aehnlich
								if (mask.exists()) {
									mask.delete();
								}
							} else {
								isValid = false;
							}
						}
					}
				}
				if (isValid) {
					Util.deleteDir(directoryOfLogfile);
				} else {
					if (directoryOfLogfile.isDirectory()) {
						String[] children = directoryOfLogfile.list();
						if (children.length == 0) {
							// System.out.println("leerer Ordner");
							Util.deleteDir(directoryOfLogfile);
						}
					}
				}
				if (!isValid) {
					return exitStr + " " + errorSP;
				}
			}
		} catch (Exception e) {
			return "Compare failed";
		}

		/*
		 * if (resultExec.equals("OK")) { if (report.exists()) { // alles io bleibt bei
		 * OK } else { // Datei nicht angelegt... resultExec = "NoReport"; } }
		 */
		return "OK";
	}

	/**
	 * fuehrt einen Vergleich mit ImageMagick via cmd durch und gibt aus wie viele
	 * Pixel sich unterscheiden
	 * 
	 * @param origDatei          Original Datei
	 * @param repDatei           Reparatur Datei
	 * @param imTolerance        Toleranz in %
	 * @param directoryOfLogfile Log Verzeichnis
	 * @return String Anzahl Pixel welche abweichen
	 */
	public static String execCompareAe(File origDatei, File repDatei, String imTolerance, File directoryOfLogfile)
			throws InterruptedException {
		// TODO: 2) Ermitteln wie viele Pixel sich unterscheiden;
		String diffPixel = "";

		// Thread.sleep(20000);
		// Warten einschalten wenn Manipulationen gemacht werden sollen

		String pathToOutput = directoryOfLogfile.getAbsolutePath() + File.separator + "_AE.txt";

		File diffPixelFile = new File(pathToOutput);
		if (diffPixelFile.exists()) {
			Util.deleteFile(diffPixelFile);
		}

		/*
		 * compare -fuzz 15% -metric AE Image_1.jpg Image_2.jpg mask.jpg 2>results.txt
		 * 
		 */

		String command = "cmd /c \"\"" + magickExe + "\" compare -fuzz " + imTolerance + " -metric AE \""
				+ origDatei.getAbsolutePath() + "\" \"" + repDatei.getAbsolutePath() + "\" null 2>\"" + diffPixelFile;
		/*
		 * Das redirect Zeichen verunmoeglicht eine direkte eingabe. mit dem
		 * geschachtellten Befehl gehts: cmd /c\"urspruenlicher Befehl\"
		 */

		// System.out.println("command = " + command);

		Process proc = null;
		Runtime rt = null;

		try {
			Util.switchOffConsole();
			rt = Runtime.getRuntime();
			proc = rt.exec(command.toString().split(" "));
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden
			// ist!

			// Fehleroutput holen
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

			// Output holen
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			// Threads starten
			errorGobbler.start();
			outputGobbler.start();

			// Warte, bis wget fertig ist
			proc.waitFor();

			Util.switchOnConsole();

			diffPixel = Util.getStringFromFile(diffPixelFile);
			// System.out.println("diffPixel ( ) = " + diffPixel);
			if (diffPixel.isEmpty()) {
				diffPixel = "empty";
				// System.out.println("diffPixel (empty) = " + diffPixel);
				// wenn empty 0.10 sekunden warten und wiederholen
				Thread.sleep(100);
				return "empty";
			} else {
				// Invalide Zahl aus der Klammer extrahieren all: 50.0846 (0.000764242)
				int pos = diffPixel.indexOf('(');
				diffPixel = diffPixel.substring(0, pos - 1);
				// System.out.println("diffPixel sub = " + diffPixel);
				diffPixel = diffPixel.replace(" ", "");
				// System.out.println("diffPixel space = " + diffPixel);
			}
		} catch (Exception e) {
			if (diffPixelFile.exists()) {
				Util.deleteFile(diffPixelFile);
			}
			return "ERROR_XML_IMCMP_SERVICEFAILED (Ae) " + e.getMessage();
		}
		if (diffPixelFile.exists()) {
			Util.deleteFile(diffPixelFile);
		}
		return diffPixel;
	}
}
