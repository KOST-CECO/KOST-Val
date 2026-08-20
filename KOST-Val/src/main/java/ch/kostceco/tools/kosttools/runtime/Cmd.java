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

package ch.kostceco.tools.kosttools.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/** @author Rc Claire Roethlisberger, KOST-CECO */

public class Cmd {

	/**
	 * fuehrt eine cmd durch und gibt den Text der Konsole als String zurueck
	 * 
	 * @param command String des Command, wie er in der Konsole eingegeben wird
	 * @param out     bei true wird der OUTPUT auch in den String geschrieben. Bei
	 *                False nur ERROR.
	 * @param workDir Temporaeres Verzeichnis
	 * @return String mit der Konsolenausgabe von ERROR und ggf OUTPUT
	 */
	public static String execToString(String command, boolean out, File workDir) throws InterruptedException {
		/*
		 * command = "\"\"" + exeFile.getAbsolutePath() + "\"" +
		 * " --noout --stream --nowarning --schema " + "\"" + xsdFile.getAbsolutePath()
		 * + "\"" + " " + "\"" + xmlFile.getAbsolutePath() + "\"\"";
		 */

		// System.out.println( "executing command: " + command );
		Process p = null;
		try {
			p = Runtime.getRuntime().exec("cmd /c " + command, null, workDir);
			// sed funktioniert nicht mit .split(" ")

		} catch (IOException ex) {
			System.out.println("IOException exec P: " + ex);
		}
	    if (p != null && p.isAlive()) {
	    	p.destroy();
	        try {
	            if (!p.waitFor(1, TimeUnit.SECONDS)) {
	            	p.destroyForcibly();
	            }
	        } catch (InterruptedException ep) {
	            Thread.currentThread().interrupt();
	            p.destroyForcibly();
	        }
	    }

		String line = "";
		String lineE = "";
		String lineReturn = line;
		try {
			if (out) {
				// System.out.println( "OUTPUT" );
				InputStream stream = p.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(stream));
				while ((line = in.readLine()) != null) {
					System.out.println(line);
					if (lineReturn.equals("")) {
						lineReturn = line;
					} else {
						if (lineReturn.contains(line)) {
							// Fehler bereits festgehalten (dublikat)
						} else {
							lineReturn = lineReturn + "</Message><Message>" + line;
						}
					}
				}
				in.close();
			}
			// System.out.println( "ERROR-OUTPUT" );
			InputStream streamE = p.getErrorStream();
			BufferedReader inE = new BufferedReader(new InputStreamReader(streamE));
			while ((lineE = inE.readLine()) != null) {
				// System.out.println(lineE);
				if (lineReturn.equals("")) {
					lineReturn = "ERROR: " + lineE;
				} else {
					if (lineReturn.contains(lineE)) {
						// Fehler bereits festgehalten (dublikat)
					} else {
						lineReturn = lineReturn + "</Message><Message>ERROR: " + lineE;
					}
				}
			}
			inE.close();
		} catch (IOException ex) {
			System.out.println("IOException exec Out Err: " + ex);
		}
		if (lineReturn.equals("")) {
			lineReturn = "OK";
		}
		// System.out.println("return String exec: "+lineReturn);
		return lineReturn;
	}

	// TODO
	public static String execToStringSplit(String command, boolean out, File workDir) throws InterruptedException {
		/*
		 * command = "\"\"" + exeFile.getAbsolutePath() + "\"" +
		 * " --noout --stream --nowarning --schema " + "\"" + xsdFile.getAbsolutePath()
		 * + "\"" + " " + "\"" + xmlFile.getAbsolutePath() + "\"\"";
		 */

		// System.out.println( "executing command: " + command );
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(("cmd /c " + command).split(" "), null, workDir);
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag
			// vorhanden ist!

		} catch (IOException ex) {
			System.out.println("IOException exec P: " + ex);
		}
	    if (p != null && p.isAlive()) {
	    	p.destroy();
	        try {
	            if (!p.waitFor(1, TimeUnit.SECONDS)) {
	            	p.destroyForcibly();
	            }
	        } catch (InterruptedException ep) {
	            Thread.currentThread().interrupt();
	            p.destroyForcibly();
	        }
	    }

		String line = "";
		String lineE = "";
		String lineReturn = line;
		try {
			if (out) {
				// System.out.println( "OUTPUT" );
				InputStream stream = p.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(stream));
				while ((line = in.readLine()) != null) {
					// System.out.println(line);
					if (lineReturn.equals("")) {
						lineReturn = line;
					} else {
						if (lineReturn.contains(line)) {
							// Fehler bereits festgehalten (dublikat)
						} else {
							lineReturn = lineReturn + "</Message><Message>" + line;
						}
					}
				}
				in.close();
			}
			// System.out.println( "ERROR-OUTPUT" );
			InputStream streamE = p.getErrorStream();
			BufferedReader inE = new BufferedReader(new InputStreamReader(streamE));
			while ((lineE = inE.readLine()) != null) {
				// System.out.println(lineE);
				if (lineReturn.equals("")) {
					lineReturn = "ERROR: " + lineE;
				} else {
					if (lineReturn.contains(lineE)) {
						// Fehler bereits festgehalten (dublikat)
					} else {
						lineReturn = lineReturn + "</Message><Message>ERROR: " + lineE;
					}
				}
			}
			inE.close();
		} catch (IOException ex) {
			System.out.println("IOException exec Out Err: " + ex);
		}
		if (lineReturn.equals("")) {
			lineReturn = "OK";
		}
		// System.out.println("return String exec: "+lineReturn);
		return lineReturn;
	}

	// TODO
	public static String execToStringSplitDv(String command, boolean out, File workDir) throws InterruptedException {
		/*
		 * exec nur für diskretValidator, da dieser immer wieder haengen bleibt
		 * 
		 * command = "\"\"" + exeFile.getAbsolutePath() + "\"" +
		 * " --noout --stream --nowarning --schema " + "\"" + xsdFile.getAbsolutePath()
		 * + "\"" + " " + "\"" + xmlFile.getAbsolutePath() + "\"\"";
		 */

		// System.out.println( "executing command: " + command );
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(("cmd /c " + command).split(" "), null, workDir);
			// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag
			// vorhanden ist!
		} catch (IOException ex) {
			System.out.println("IOException exec P: " + ex);
		}
	    if (p != null && p.isAlive()) {
	    	p.destroy();
	        try {
	            if (!p.waitFor(1, TimeUnit.SECONDS)) {
	            	p.destroyForcibly();
	            }
	        } catch (InterruptedException ep) {
	            Thread.currentThread().interrupt();
	            p.destroyForcibly();
	        }
	    }

		String line1 = "";
		String line2 = "";
		String lineE = "";
		String lineReturn = line1;
		Boolean exception = false;
		try {
			if (out) {
				/*
				 * diskretvalidator bleibt manchmal wegen StackOverFlow stecken, versuch dies zu
				 * umgehen, indem beim diskretvalidatorzuerst getErrorStream ausgewertet wird
				 */

				InputStream streamE1 = p.getErrorStream();
				BufferedReader inE1 = new BufferedReader(new InputStreamReader(streamE1));
				long secondsE = System.currentTimeMillis();
				while (secondsE + (60 * 1000) > System.currentTimeMillis()) {
					if (inE1.readLine() != null) {
						// ende
						break;
					}
					lineE = inE1.readLine();
					// System.out.println("lineE "+lineE);
					if (lineE.contains("java.lang.StackOverflowError") || lineE.contains("java.lang.OutOfMemoryError")
							|| lineE.contains("java.lang.NullPointerException") || lineE.contains("java.io.IOException")
							|| lineE.contains("Exception in thread")) {
						// System.out.println(" - E "+lineE);
						/*
						 * diskretvalidator bleibt manchmal wegen StackOverFlow stecken, versuch dies zu
						 * umgehen
						 */
						exception = true;
						break;
					}
				}
				inE1.close();

				if (!exception) {
					InputStream stream2 = p.getInputStream();
					BufferedReader in2 = new BufferedReader(new InputStreamReader(stream2));
					while ((line2 = in2.readLine()) != null) {
						// System.out.println("line2 "+line2);
						if (lineReturn.equals("")) {
							lineReturn = line2;
						} else {
							if (lineReturn.contains(line2)) {
								// Fehler bereits festgehalten (dublikat)
							} else {
								lineReturn = lineReturn + "</Message><Message>" + line2;
							}
						}
					}
					in2.close();

					InputStream streamE2 = p.getErrorStream();
					BufferedReader inE2 = new BufferedReader(new InputStreamReader(streamE2));
					while ((lineE = inE2.readLine()) != null) {
						// System.out.println("lineE."+lineE);
						if (lineReturn.equals("")) {
							lineReturn = "ERROR: " + lineE;
						} else {
							if (lineReturn.contains(lineE)) {
								// Fehler bereits festgehalten (dublikat)
							} else {
								lineReturn = lineReturn + "</Message><Message>ERROR: " + lineE;
							}
						}
					}
					inE2.close();

				}
			}
		} catch (IOException ex) {
			// System.out.println("IOException exec Out Err: " + ex);
		}
		if (lineReturn.equals("")) {
			lineReturn = "OK";
		}
		// System.out.println("return String exec: "+lineReturn);
		return lineReturn;
	}

}
