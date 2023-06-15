/* == KOST-Val ==================================================================================
 * The KOST-Val application is used for validate TIFF, SIARD, PDF/A, JP2, JPEG, PNG, XML-Files and
 * Submission Information Package (SIP). Copyright (C) Claire Roethlisberger (KOST-CECO),
 * Christian Eugster, Olivier Debenath, Peter Schneider (Staatsarchiv Aargau), Markus Hahn
 * (coderslagoon), Daniel Ludin (BEDAG AG)
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

package ch.kostceco.tools.kostval.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** @author Rc Claire Roethlisberger, KOST-CECO */
public class Logger
{

	/** Apache Commons Log. */
	private Log log;

	/**
	 * Instanzierung eines benannten Loggers. Der Name des Loggers entspricht
	 * dem Namen der uebergebenen Klasse. Sinn: Beim Einsatz der Apache Log4j
	 * API koennen ueber das Konfig File Package oder Klassenfilter gesetzt
	 * werden.
	 * 
	 * @param clazz
	 *            Class for which a log name will be derived.
	 */
	public Logger( Class<?> clazz )
	{

		// Log Instanz ueber Factory holen.
		this.log = LogFactory.getLog( clazz );
	}

	/**
	 * Logt einen Fehler der die Stabilitaet des Programms beeinflusst.
	 * 
	 * @param message
	 *            Fehlermeldung.
	 */
	public void logFatal( String message )
	{
		this.log.fatal( message );

	}

	/**
	 * Logt einen Fehler der die Stabilitaet des Programms beeinflusst.
	 * 
	 * @param message
	 *            Fehlermeldung.
	 * @param t
	 *            Ursache des Fehlers.
	 */
	public void logFatal( String message, Throwable t )
	{
		this.log.fatal( message, t );
	}

	/**
	 * Logt einen Fehler, der nicht automatisch behoben werden kann.
	 * 
	 * @param message
	 *            Fehlermeldung.
	 */
	public void logError( String message )
	{
		this.log.error( message );
	}

	/**
	 * Logt einen Fehler, der nicht automatisch behoben werden kann.
	 * 
	 * @param message
	 *            Fehlermeldung.
	 * @param t
	 *            Ursache des Fehlers.
	 */
	public void logError( String message, Throwable t )
	{
		this.log.error( message, t );
	}

	/**
	 * Logt einen Fehler, der behoben oder uebergangen werden konnte.
	 * 
	 * @param message
	 *            Meldung.
	 */
	public void logWarning( String message )
	{
		this.log.warn( message );
	}

	/**
	 * Logt einen Fehler, der behoben oder uebergangen werden konnte.
	 * 
	 * @param message
	 *            Meldung.
	 * @param t
	 *            Ursache des Warnung.
	 */
	public void logWarning( String message, Throwable t )
	{
		this.log.warn( message, t );
	}

	/**
	 * Logt eine Information zum Programmablauf.
	 * 
	 * @param message
	 *            Meldung.
	 */
	/*
	 * public void logInfo( String message ) { // this.log.info( message );
	 * this.log.info( "" ); }
	 * 
	 * /** Logt eine Information zum Programmablauf.
	 * 
	 * @param message Meldung.
	 * 
	 * @param t Ursache.
	 */
	/*
	 * public void logInfo( String message, Throwable t ) { // this.log.info(
	 * message, t ); this.log.info( "" ); }
	 * 
	 * /** Logt eine Information zum Nachvollziehen des Programmstatus.
	 * 
	 * @param message Meldung.
	 */
	/*
	 * public void logDebug( String message ) { this.log.debug( message ); }
	 * 
	 * /** Logt eine Information zum Nachvollziehen des Programmstatus.
	 * 
	 * @param message Meldung.
	 * 
	 * @param t Ursache.
	 */
	/*
	 * public void logDebug( String message, Throwable t ) { this.log.debug(
	 * message, t ); }
	 */

	/**
	 * Ist Logging auf Level Fatal aktiv?
	 * 
	 * @return true, falls aktiv, ansonsten false.
	 */
	public boolean isFatalEnabled()
	{
		return this.log.isFatalEnabled();
	}

	/**
	 * Ist Logging auf Level Error aktiv?
	 * 
	 * @return true, falls aktiv, ansonsten false.
	 */
	public boolean isErrorEnabled()
	{
		return this.log.isErrorEnabled();
	}

	/**
	 * Ist Logging auf Level Warn aktiv?
	 * 
	 * @return true, falls aktiv, ansonsten false.
	 */
	public boolean isWarnEnabled()
	{
		return this.log.isWarnEnabled();
	}

	/**
	 * Ist Logging auf Level Info aktiv?
	 * 
	 * @return true, falls aktiv, ansonsten false.
	 */
	/*
	 * public boolean isInfoEnabled() { // return this.log.isInfoEnabled();
	 * return true; }
	 * 
	 * /** Ist Logging auf Level Debug aktiv?
	 * 
	 * @return true, falls aktiv, ansonsten false.
	 */
	/*
	 * public boolean isDebugEnabled() { // return this.log.isDebugEnabled();
	 * return true; } /** Ist Logging auf Level Debug wird nicht mehr angeboten,
	 * da es als einziges noch log4j benoetigte und es sicherheitsprobleme gibt
	 * respektive gab
	 */

}
