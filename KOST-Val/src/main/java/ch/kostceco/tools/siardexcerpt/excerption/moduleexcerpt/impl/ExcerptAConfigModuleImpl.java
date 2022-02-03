/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016-2022 Claire Roethlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * SIARDexcerpt is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This application is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA or see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.kosttools.util.Util;
import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptAConfigException;
import ch.kostceco.tools.siardexcerpt.excerption.ValidationModuleImpl;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptAConfigModule;

/** Config Datei ausfuellen */
public class ExcerptAConfigModuleImpl extends ValidationModuleImpl implements ExcerptAConfigModule
{

	public static String NEWLINE = System.getProperty( "line.separator" );

	@Override
	public boolean validate( File siardDatei, File configFileHard, String inputMainname,
			Map<String, String> configMap, Locale locale ) throws ExcerptAConfigException
	{

		boolean result = true;

		String toplevelDir = siardDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		try {
			/** f) Config bei Bedarf mainname / (..) gemaess metadata.xml ausfuellen
			 * 
			 * Wenn mainname && mainname exist => Haupttabelle = mainname (mainname, mainfolder,
			 * primarykeyname, primarykeycell, title) ausfuellen
			 * 
			 * -> dann wenn foreignKey = primaryKey.maintable subtables (name, folder, foreignkeycell)
			 * ausfuellen
			 * 
			 * Ansonsten
			 * 
			 * count primaryKey
			 * 
			 * if pkInt = 1 dann config maintable (mainname, mainfolder, primarykeyname, primarykeycell,
			 * title) ausfuellen
			 * 
			 * -> dann wenn foreignKey = primaryKey subtables (name, folder, foreignkeycell) ausfuellen
			 * 
			 * if pkInt > 1 dann analog oben aber mit dieser, welche am meisten verwendet wird
			 * 
			 * if pkInt = 0 dann jene Tablle mit den meisten column. Keine Verknuepfung mit anderen
			 * Tabellen.
			 *
			 * Die 12 searchCells definieren: Die erste ist der pk. Die restlichen werden mit ersten 11
			 * respektive 12 sinnvollen Spalten definiert und erstellt. Sinnvoll bedeutet, dass die
			 * verschiedenen types priorisiert werden. (nur wenn mehr als 12 column in maintable)
			 * 
			 * Hoechste Prio (1) fuer "CHARACTER VARYING", "CHARACTER" und "DATE".
			 * 
			 * 2 fuer "DECIMAL", "NATIONAL CHARACTER VARYING" und "NATIONAL CHARACTER".
			 * 
			 * 3 fuer "BIGINT", "INTEGER", "SMALLINT" und "NUMERIC".
			 * 
			 * 4 Fuer "DOUBLE PRECISION", "FLOAT", "INTERVAL" und "REAL".
			 * 
			 * 5 fuer "TIME", "TIME WITH TIME ZONE", "TIMESTAMT" und "TIMESTAMP WITH TIME ZONE".
			 * 
			 * 6 fuer "BINARY VARYING", "BINARY", "BIT VARYING", "BIT" und "XML".
			 * 
			 * 7 fuer "BINARY LARGE OBJECT", "BOOLEAN", "CHARACTER LARGE OBJECT", "NATIONAL CHARACTER
			 * LARGE OBJECT" und ggf andere */

			Boolean boolMainname = false;
			Boolean boolPKname = false;

			// Information aus metadata holen
			String mainname = "";
			String mainfolder = "";
			String mainschemaname = "";
			String mainschemafolder = "";
			String primarykeyname = "";
			String primarykeycell = "";
			String nameProv = "";
			String numberProv = "";
			String typeProv = "";

			/* array mit den 84 werten (String)
			 * 
			 * 0-11 = Prio1
			 * 
			 * 12-23 = Prio2
			 * 
			 * 24-35 = Prio3
			 * 
			 * 36-47 = Prio4
			 * 
			 * 48-59 = Prio5
			 * 
			 * 60-71 = Prio6
			 * 
			 * 72-83 = Prio7 */
			String[] array84number;
			array84number = new String[84];
			String[] array84name;
			array84name = new String[84];
			// array mit den 12 hoechsten werten (String)
			String[] array12number;
			array12number = new String[12];
			String[] array12name;
			array12name = new String[12];

			String mainnameProv = "";
			String mainfolderProv = "";
			String primarykeynameProv = "";
			String primarykeycellProv = "";
			String title = "";
			File xmlExtracted = new File( siardDatei.getAbsolutePath() + File.separator + "header"
					+ File.separator + "metadata.xml" );

			try {

				DocumentBuilderFactory dbfConfig = DocumentBuilderFactory.newInstance();
				DocumentBuilder dbConfig = dbfConfig.newDocumentBuilder();
				Document docConfig = dbConfig.parse( new FileInputStream( xmlExtracted ), "UTF8" );
				docConfig.getDocumentElement().normalize();
				dbfConfig.setFeature( "http://xml.org/sax/features/namespaces", false );

				/* columns.column werden mit number erweitert, damit die Zuordnung nicht jedesmal via
				 * Zaehler erfolgen muss */
				NodeList nlColumns = docConfig.getElementsByTagName( "columns" );
				for ( int x = 0; x < nlColumns.getLength(); x++ ) {
					// System.out.println( "Anzahl Columns: " + nlColumns.getLength() );
					Node nodeColumns = nlColumns.item( x );
					/* Element number = docConfigInit.createElement("number"); number.setTextContent( "c" +
					 * (1) ); nodeColumns.appendChild(number); */
					NodeList nlColumn = nodeColumns.getChildNodes();
					int counter = 0;
					// System.out.println( "Anzahl Column: " + (nlColumn.getLength()+1)/2 );
					for ( int y = 0; y < nlColumn.getLength(); y++ ) {
						Node subNodeColumns = nlColumn.item( y );
						if ( subNodeColumns.getNodeName().equals( "column" ) ) {
							// nur Column mit nummer erweitern und nicht auch Kommentare
							counter = counter + 1;
							Node nodeColumn = nlColumn.item( y );
							NodeList nlColumnDetail = nodeColumn.getChildNodes();
							Element number = docConfig.createElement( "number" );
							number.setTextContent( "c" + counter );
							for ( int z = 0; z < nlColumnDetail.getLength(); z++ ) {
								Node subNode = nlColumnDetail.item( z );
								if ( subNode.getNodeName().equals( "type" ) ) {
									// nodeColumn.appendChild(number);
									nodeColumn.insertBefore( number, subNode );
								}
							}
							for ( int z = 0; z < nlColumnDetail.getLength(); z++ ) {
								Node subNode = nlColumnDetail.item( z );
								if ( subNode.getNodeName().equals( "number" ) ) {
									// System.out.println( "number " + subNode.getTextContent() );
								}
							}
						} else {
							// Kommentar wird nicht mit number ergaenzt
						}
					}
				}
				NodeList nlFK = docConfig.getElementsByTagName( "foreignKey" );
				// System.out.println( "Anzahl foreignKey: " + nlFK.getLength() );
				NodeList nlPK = docConfig.getElementsByTagName( "primaryKey" );
				// System.out.println( "Anzahl Primarykey: " + nlPK.getLength() );

				// TODO: maintable = mainname wenn diese angegeben wurde & existiert
				if ( !inputMainname.equals( "(..)" ) ) {
					// System.out.println( "inputMainname: " + inputMainname );
					/* Kontrollieren ob inputMainname existiert. Wenn nicht dann false und gleich wie (..)
					 * weiter fahren */
					NodeList nlTables = docConfig.getElementsByTagName( "tables" );
					int xMainTable = 0;
					int x0MainTable = 0;
					for ( int x0 = 0; x0 < nlTables.getLength(); x0++ ) {
						Node nodeTablesDetail = nlTables.item( x0 );
						NodeList nlTable = nodeTablesDetail.getChildNodes();
						for ( int x = 0; x < nlTable.getLength(); x++ ) {
							if ( !boolMainname ) {
								Node nodeTable = nlTable.item( x );
								NodeList nlChildTable = nodeTable.getChildNodes();
								for ( int y = 0; y < nlChildTable.getLength(); y++ ) {
									Node nodeDetail = nlChildTable.item( y );
									if ( nodeDetail.getNodeName().equals( "name" ) ) {
										mainnameProv = nodeDetail.getTextContent();
										// System.out.println( "mainnameProv " + mainnameProv );
										if ( mainnameProv.equals( inputMainname ) ) {
											// System.out.println( "mainname " +" = " + inputMainname );
											mainname = mainnameProv;
											boolMainname = true;
											xMainTable = x;
											x0MainTable = x0;
											x0 = nlTables.getLength();

											// mainfolder herauslesen
											Node mainTables = nodeDetail.getParentNode();
											NodeList nlTablesChild = mainTables.getChildNodes();
											for ( int x2 = 0; x2 < nlTablesChild.getLength(); x2++ ) {
												// fuer jedes Subelement der Tabelle (name, folder, description...) ...
												Node subNode = nlTablesChild.item( x2 );
												if ( subNode.getNodeName().equals( "folder" ) ) {
													mainfolder = subNode.getTextContent();
												}
											}

											// Schema name und folder herauslesen
											Node parentMainTables = mainTables.getParentNode().getParentNode();
											NodeList nlSchemaChild = parentMainTables.getChildNodes();
											for ( int x3 = 0; x3 < nlSchemaChild.getLength(); x3++ ) {
												// fuer jedes Subelement der Tabelle (name, folder, description...) ...
												Node subNode3 = nlSchemaChild.item( x3 );
												if ( subNode3.getNodeName().equals( "name" ) ) {
													mainschemaname = subNode3.getTextContent();
												} else if ( subNode3.getNodeName().equals( "folder" ) ) {
													mainschemafolder = subNode3.getTextContent();
												}
											}

										}
									} else if ( nodeDetail.getNodeName().equals( "folder" ) ) {
										mainfolderProv = nodeDetail.getTextContent();
										// System.out.println( "mainfolderProv " + mainfolderProv );
									} else if ( nodeDetail.getNodeName().equals( "primaryKey" ) ) {
										NodeList nlPKmn = nodeDetail.getChildNodes();
										for ( int z = 0; z < nlPKmn.getLength(); z++ ) {
											Node nodeDetailPK = nlPKmn.item( z );
											if ( nodeDetailPK.getNodeName().equals( "column" ) ) {
												primarykeynameProv = nodeDetailPK.getTextContent();
												// System.out.println( "primarykeynameProv " + primarykeynameProv );
											}
										}
									}
								}
								if ( boolMainname ) {
									/* haupttabelle gefunden */
									mainfolder = mainfolderProv;
									primarykeyname = primarykeynameProv;
								}
							} else {
								/* haupttabelle gefunden */
								mainfolder = mainfolderProv;
								primarykeyname = primarykeynameProv;

								x0 = nlTables.getLength();
								// beendet die For-schleife
							}
						}
					}
					if ( mainfolder == "" ) {
						mainfolder = mainfolderProv;
					}
					if ( primarykeyname == "" ) {
						primarykeyname = primarykeynameProv;
					}
					// primarykeycell mit xMainTable herausfinden
					Node nodeTable = nlTables.item( x0MainTable );
					NodeList nlChildTable = nodeTable.getChildNodes();
					// for ( int y = 0; y < nlChildTable.getLength(); y++ ) {
					Node nodeDetailTable = nlChildTable.item( xMainTable );
					NodeList nlChildDetailTable = nodeDetailTable.getChildNodes();
					for ( int y = 0; y < nlChildDetailTable.getLength(); y++ ) {
						Node nodeSubDetailTable = nlChildDetailTable.item( y );
						// System.out.println( "NodeName = " + nodeSubDetailTable.getNodeName() );
						if ( nodeSubDetailTable.getNodeName().equals( "columns" ) ) {
							NodeList nlColumnsMT = nodeSubDetailTable.getChildNodes();
							for ( int z = 0; z < nlColumnsMT.getLength(); z++ ) {
								// fuer jedes Subelement der Colummns (column) ...
								Node nodeDetailMT = nlColumnsMT.item( z );
								// System.out.println( nodeDetailMT.getNodeName() );
								if ( nodeDetailMT.getNodeName().equals( "column" ) ) {
									// System.out.println( "primarykeyname: "+ primarykeyname );
									NodeList nlColumnMT = nodeDetailMT.getChildNodes();
									for ( int z1 = 0; z1 < nlColumnMT.getLength(); z1++ ) {
										// fuer jedes Subelement der Colummn (name, number, description...) ...
										Node nodeDetailMtC = nlColumnMT.item( z1 );
										/* System.out.println( nodeDetailMtC.getNodeName() + " : " +
										 * nodeDetailMtC.getTextContent() ); */
										if ( nodeDetailMtC.getNodeName().equals( "name" ) ) {
											if ( nodeDetailMtC.getTextContent().equals( primarykeyname ) ) {
												boolPKname = true;
												z = nlColumnsMT.getLength();
											} else {
												boolPKname = false;
											}
										} else if ( nodeDetailMtC.getNodeName().equals( "number" ) ) {
											primarykeycellProv = nodeDetailMtC.getTextContent();
										}
									}
									if ( boolPKname ) {
										primarykeycell = primarykeycellProv;
										z = nlColumnsMT.getLength();
										y = nlChildTable.getLength();
									}
								}
							}

							for ( int zC = 0; zC < nlColumnsMT.getLength(); zC++ ) {
								// fuer jedes Subelement der Colummns (column) ...
								Node nodeColumn = nlColumnsMT.item( zC );
								NodeList nlMostColumn = nodeColumn.getChildNodes();
								for ( int yC = 0; yC < nlMostColumn.getLength(); yC++ ) {
									// fuer jedes Subelement der Colummn (name, number, description...) ...
									Node subNode = nlMostColumn.item( yC );
									if ( subNode.getNodeName().equals( "name" ) ) {
										nameProv = subNode.getTextContent();
									} else if ( subNode.getNodeName().equals( "number" ) ) {
										numberProv = subNode.getTextContent();
									} else if ( subNode.getNodeName().equals( "type" ) ) {
										typeProv = subNode.getTextContent();
									}
								}
								/* Hoechste Prio (1) fuer "CHARACTER VARYING", "CHARACTER" und "DATE".
								 * 
								 * 2 fuer "DECIMAL", "NATIONAL CHARACTER VARYING" und "NATIONAL CHARACTER".
								 * 
								 * 3 fuer "BIGINT", "INTEGER", "SMALLINT" und "NUMERIC".
								 * 
								 * 4 Fuer "DOUBLE PRECISION", "FLOAT", "INTERVAL" und "REAL".
								 * 
								 * 5 fuer "TIME", "TIME WITH TIME ZONE", "TIMESTAMT" und "TIMESTAMP WITH TIME ZONE".
								 * 
								 * 6 fuer "BINARY VARYING", "BINARY", "BIT VARYING", "BIT" und "XML".
								 * 
								 * 7 fuer "BINARY LARGE OBJECT", "BOOLEAN", "CHARACTER LARGE OBJECT",
								 * "NATIONAL CHARACTER LARGE OBJECT" und ggf andere */

								if ( !numberProv.equals( "" ) || !nameProv.equals( "" )
										|| !typeProv.equals( "" ) ) {
									/* System.out.println( "Name: " + nameProv + " / Number: " + numberProv +
									 * " / Type: " + typeProv ); */
									if ( numberProv.equals( primarykeycell ) ) {
										nameProv = "";
										numberProv = "";
										typeProv = "";
									} else if ( typeProv.startsWith( "BINARY LARGE OBJECT" )
											|| typeProv.startsWith( "BOOLEAN" )
											|| typeProv.startsWith( "CHARACTER LARGE OBJECT" )
											|| typeProv.startsWith( "NATIONAL CHARACTER LARGE OBJECT" ) ) {
										// 72-83 = Prio7
										for ( int z = 72; z < 84; z++ ) {
											if ( array84number[z] == null ) {
												array84name[z] = nameProv;
												array84number[z] = numberProv;
												z = 85;
											}
										}
										nameProv = "";
										numberProv = "";
										typeProv = "";
									} else if ( typeProv.startsWith( "XML" )
											|| typeProv.startsWith( "BINARY VARYING" ) || typeProv.startsWith( "BINARY" )
											|| typeProv.startsWith( "BIT VARYING" ) || typeProv.startsWith( "BIT" ) ) {
										// 60-71 = Prio6 "BINARY VARYING", "BINARY", "BIT VARYING", "BIT" und "XML"
										for ( int z = 60; z < 72; z++ ) {
											if ( array84number[z] == null ) {
												array84name[z] = nameProv;
												array84number[z] = numberProv;
												z = 73;
											}
										}
										nameProv = "";
										numberProv = "";
										typeProv = "";
									} else if ( typeProv.startsWith( "TIMESTAMP WITH TIME ZONE" )
											|| typeProv.startsWith( "TIMESTAMP" )
											|| typeProv.startsWith( "TIME WITH TIME ZONE" )
											|| typeProv.startsWith( "TIME" ) ) {
										// 48-59 = Prio5 "TIME", "TIME WITH TIME ZONE", "TIMESTAMP" und
										// "TIMESTAMP WITH TIME ZONE"
										for ( int z = 48; z < 60; z++ ) {
											if ( array84number[z] == null ) {
												array84name[z] = nameProv;
												array84number[z] = numberProv;
												z = 61;
											}
										}
										nameProv = "";
										numberProv = "";
										typeProv = "";
									} else if ( typeProv.startsWith( "DOUBLE PRECISION" )
											|| typeProv.startsWith( "FLOAT" ) || typeProv.startsWith( "INTERVAL" )
											|| typeProv.startsWith( "REAL" ) ) {
										// 36-47 = Prio4 "DOUBLE PRECISION", "FLOAT", "INTERVAL" und "REAL"
										for ( int z = 36; z < 48; z++ ) {
											if ( array84number[z] == null ) {
												array84name[z] = nameProv;
												array84number[z] = numberProv;
												z = 49;
											}
										}
										nameProv = "";
										numberProv = "";
										typeProv = "";
									} else if ( typeProv.startsWith( "BIGINT" ) || typeProv.startsWith( "INTEGER" )
											|| typeProv.startsWith( "SMALLINT" ) || typeProv.startsWith( "NUMERIC" ) ) {
										// 24-35 = Prio3 "BIGINT", "INTEGER", "SMALLINT" und "NUMERIC"
										/* System.out.println( "Prio 3 = Name: " + nameProv + " / Number: " + numberProv
										 * + " / Type: " + typeProv ); */
										for ( int z = 24; z < 36; z++ ) {
											if ( array84number[z] == null ) {
												array84name[z] = nameProv;
												array84number[z] = numberProv;
												z = 37;
											}
										}
										nameProv = "";
										numberProv = "";
										typeProv = "";
									} else if ( typeProv.startsWith( "DECIMAL" )
											|| typeProv.startsWith( "NATIONAL CHARACTER VARYING" )
											|| typeProv.startsWith( "NATIONAL CHARACTER" ) ) {
										// 12-23 = Prio2 "DECIMAL", "NATIONAL CHARACTER VARYING" und
										// "NATIONAL CHARACTER"
										for ( int z = 12; z < 24; z++ ) {
											if ( array84number[z] == null ) {
												array84name[z] = nameProv;
												array84number[z] = numberProv;
												z = 25;
											}
										}
										nameProv = "";
										numberProv = "";
										typeProv = "";
									} else if ( typeProv.startsWith( "CHARACTER VARYING" )
											|| typeProv.startsWith( "CHARACTER" ) || typeProv.startsWith( "DATE" ) ) {
										// 0-11 = Prio1 "CHARACTER VARYING", "CHARACTER" und "DATE"
										/* System.out.println( "Prio 1 = Name: " + nameProv + " / Number: " + numberProv
										 * + " / Type: " + typeProv ); */
										for ( int z = 0; z < 12; z++ ) {
											if ( array84number[z] == null ) {
												array84name[z] = nameProv;
												array84number[z] = numberProv;
												z = 13;
											}
										}
										nameProv = "";
										numberProv = "";
										typeProv = "";
									} else {
										// PRIO 7 fuer den Rest
										for ( int z = 72; z < 84; z++ ) {
											if ( array84number[z] == null ) {
												array84name[z] = nameProv;
												array84number[z] = numberProv;
												z = 85;
											}
										}
										nameProv = "";
										numberProv = "";
										typeProv = "";
									}
								}
							}
							/* die verschiedenen Prio zusammenfassen. p1 mit p2 erweitern, dann p3, dann p4...
							 * 
							 * beim array84 werden die leerstellen geloescht respektive mit dem Nachfolger
							 * ersetzt */
							int yArray = 0;
							for ( int x = 0; x < 83; x++ ) {
								// System.out.println( Arrays.toString( array84number ) );
								if ( array84number[x] == null ) {
									for ( yArray = x + 1; yArray < 84; yArray++ ) {
										if ( array84number[x] == null ) {
											array84number[x] = array84number[yArray];
											array84number[yArray] = null;
											array84name[x] = array84name[yArray];
											array84name[yArray] = null;
										}
									}
								}
							}

							array12name[0] = "<mc1name>" + array84name[0] + "</mc1name>";
							array12number[0] = "<mc1number>" + array84number[0] + "</mc1number>";
							array12name[1] = "<mc2name>" + array84name[1] + "</mc2name>";
							array12number[1] = "<mc2number>" + array84number[1] + "</mc2number>";
							array12name[2] = "<mc3name>" + array84name[2] + "</mc3name>";
							array12number[2] = "<mc3number>" + array84number[2] + "</mc3number>";
							array12name[3] = "<mc4name>" + array84name[3] + "</mc4name>";
							array12number[3] = "<mc4number>" + array84number[3] + "</mc4number>";
							array12name[4] = "<mc5name>" + array84name[4] + "</mc5name>";
							array12number[4] = "<mc5number>" + array84number[4] + "</mc5number>";
							array12name[5] = "<mc6name>" + array84name[5] + "</mc6name>";
							array12number[5] = "<mc6number>" + array84number[5] + "</mc6number>";
							array12name[6] = "<mc7name>" + array84name[6] + "</mc7name>";
							array12number[6] = "<mc7number>" + array84number[6] + "</mc7number>";
							array12name[7] = "<mc8name>" + array84name[7] + "</mc8name>";
							array12number[7] = "<mc8number>" + array84number[7] + "</mc8number>";
							array12name[8] = "<mc9name>" + array84name[8] + "</mc9name>";
							array12number[8] = "<mc9number>" + array84number[8] + "</mc9number>";
							array12name[9] = "<mc10name>" + array84name[9] + "</mc10name>";
							array12number[9] = "<mc10number>" + array84number[9] + "</mc10number>";
							array12name[10] = "<mc11name>" + array84name[10] + "</mc11name>";
							array12number[10] = "<mc11number>" + array84number[10] + "</mc11number>";
						}
					}
				}

				// TODO (..) oder tabelle hat nicht existiert -> boolMainname=false
				if ( !boolMainname ) {
					// System.out.println( "(..) oder tabelle hat nicht existiert -> boolMainname=false" );
					if ( nlPK.getLength() == 0 ) {
						/* kein Primaerschluessel. if pkInt = 0 dann jene Tabelle mit den meisten column. Der
						 * Schluessel ist die erste Spalte, welche nicht Nullable sein darf oder die erste.
						 * Keine Verknuepfung mit anderen Tabellen. */
						int tableX2 = 0;
						int tableColNum = 0;

						int tableX2Prov = 0;
						int tableColNumProv = 0;

						/* nlColumns erstellen, fuer jede die anzahl name ermitteln */
						NodeList nlCs = docConfig.getElementsByTagName( "columns" );
						for ( int x2 = 0; x2 < nlCs.getLength(); x2++ ) {
							NodeList nlC = nlCs.item( x2 ).getChildNodes();
							tableX2Prov = x2;
							tableColNumProv = nlC.getLength();
							// System.out.println( "PROV: " + tableX2Prov + " " + tableColNumProv );
							if ( tableColNumProv > tableColNum ) {
								tableColNum = tableColNumProv;
								tableX2 = tableX2Prov;
								// System.out.println( tableColNum + " " + tableX2 );
								tableX2Prov = 0;
								tableColNumProv = 0;
							} else {
								tableX2Prov = 0;
								tableColNumProv = 0;
							}
						}
						Node mostColumn = nlCs.item( tableX2 );
						NodeList nlColumn = mostColumn.getChildNodes();
						/* System.out.println( "428 " + tableColNum + " " + tableX2 + " - " +
						 * nlColumn.getLength() ); */
						for ( int x3 = 0; x3 < nlColumn.getLength(); x3++ ) {
							Node nodeColumn = nlColumn.item( x3 );
							NodeList nlColumnChild = nodeColumn.getChildNodes();
							/* System.out.println( "432 " + x3 + ": " + nodeColumn.getNodeName() + " -- " +
							 * nodeColumn.getTextContent() + " => " + nlColumnChild.getLength() ); */

							for ( int x4 = 0; x4 < nlColumnChild.getLength(); x4++ ) {
								// fuer jedes Subelement der Zelle (name, description...) ...
								Node subNodeIII = nlColumnChild.item( x4 );
								// System.out.println( "445 " + subNodeIII.getNodeName() );
								// System.out.println( "446 " + subNodeIII.getTextContent() );

								if ( subNodeIII.getNodeName().equals( "name" ) ) {
									primarykeynameProv = subNodeIII.getTextContent();
									// System.out.println( "449 " + primarykeynameProv );
								} else if ( subNodeIII.getNodeName().equals( "number" ) ) {
									primarykeycellProv = subNodeIII.getTextContent();
									// System.out.println( "452 " + primarykeycellProv );
								} else if ( subNodeIII.getNodeName().equals( "nullable" ) ) {
									if ( subNodeIII.getTextContent().equals( "false" ) ) {
										// System.out.println( "Column mit Nullable=false: " + x3 );
										if ( primarykeycell.equals( "" ) ) {
											// primarykeycell="c" + x4 + 1 ;
											// primarykeycellProvInt = x3;
										}
									}
								}
							}
							if ( !primarykeycellProv.equals( "" ) && primarykeycell.equals( "" ) ) {
								primarykeycell = primarykeycellProv;
								primarykeyname = primarykeynameProv;
								primarykeynameProv = "";
								primarykeycellProv = "";
							} else {
								primarykeynameProv = "";
								primarykeycellProv = "";
							}
						}
						if ( primarykeyname.equals( "" ) || primarykeycell.equals( "" ) ) {
							primarykeyname = "c1";
							primarykeycell = "c1";
						}
						Node mostColumnTable = mostColumn.getParentNode();

						// Schema name und folder herauslesen
						Node mainTables = mostColumnTable.getParentNode();
						Node mainSchema = mainTables.getParentNode();
						NodeList nlSchemaChild = mainSchema.getChildNodes();
						for ( int x = 0; x < nlSchemaChild.getLength(); x++ ) {
							// fuer jedes Subelement der Tabelle (name, folder, description...) ...
							Node subNode = nlSchemaChild.item( x );
							if ( subNode.getNodeName().equals( "name" ) ) {
								mainschemaname = subNode.getTextContent();
							} else if ( subNode.getNodeName().equals( "folder" ) ) {
								mainschemafolder = subNode.getTextContent();
							}
						}

						NodeList nlTableChild = mostColumnTable.getChildNodes();
						for ( int x = 0; x < nlTableChild.getLength(); x++ ) {
							// fuer jedes Subelement der Tabelle (name, folder, description...) ...
							Node subNode = nlTableChild.item( x );
							// System.out.println("445 " + subNodeIII.getNodeName() );
							// System.out.println("446 " + subNodeIII.getTextContent());
							if ( subNode.getNodeName().equals( "name" ) ) {
								mainname = subNode.getTextContent();
							} else if ( subNode.getNodeName().equals( "folder" ) ) {
								mainfolder = subNode.getTextContent();
							}
						}
						NodeList nlmostColumn = mostColumn.getChildNodes();

						for ( int zC = 0; zC < nlmostColumn.getLength(); zC++ ) {
							// fuer jedes Subelement der Colummns (column) ...
							Node nodeColumn = nlmostColumn.item( zC );
							NodeList nlMostColumn = nodeColumn.getChildNodes();
							for ( int yC = 0; yC < nlMostColumn.getLength(); yC++ ) {
								// fuer jedes Subelement der Colummn (name, number, description...) ...
								Node subNode = nlMostColumn.item( yC );
								if ( subNode.getNodeName().equals( "name" ) ) {
									nameProv = subNode.getTextContent();
								} else if ( subNode.getNodeName().equals( "number" ) ) {
									numberProv = subNode.getTextContent();
								} else if ( subNode.getNodeName().equals( "type" ) ) {
									typeProv = subNode.getTextContent();
								}
							}
							/* Hoechste Prio (1) fuer "CHARACTER VARYING", "CHARACTER" und "DATE".
							 * 
							 * 2 fuer "DECIMAL", "NATIONAL CHARACTER VARYING" und "NATIONAL CHARACTER".
							 * 
							 * 3 fuer "BIGINT", "INTEGER", "SMALLINT" und "NUMERIC".
							 * 
							 * 4 Fuer "DOUBLE PRECISION", "FLOAT", "INTERVAL" und "REAL".
							 * 
							 * 5 fuer "TIME", "TIME WITH TIME ZONE", "TIMESTAMT" und "TIMESTAMP WITH TIME ZONE".
							 * 
							 * 6 fuer "BINARY VARYING", "BINARY", "BIT VARYING", "BIT" und "XML".
							 * 
							 * 7 fuer "BINARY LARGE OBJECT", "BOOLEAN", "CHARACTER LARGE OBJECT",
							 * "NATIONAL CHARACTER LARGE OBJECT" und ggf andere */

							if ( !numberProv.equals( "" ) || !nameProv.equals( "" ) || !typeProv.equals( "" ) ) {
								/* System.out.println( "Name: " + nameProv + " / Number: " + numberProv +
								 * " / Type: " + typeProv ); */
								if ( numberProv.equals( primarykeycell ) ) {
									nameProv = "";
									numberProv = "";
									typeProv = "";
								} else if ( typeProv.startsWith( "BINARY LARGE OBJECT" )
										|| typeProv.startsWith( "BOOLEAN" )
										|| typeProv.startsWith( "CHARACTER LARGE OBJECT" )
										|| typeProv.startsWith( "NATIONAL CHARACTER LARGE OBJECT" ) ) {
									// 72-83 = Prio7
									for ( int z = 72; z < 84; z++ ) {
										if ( array84number[z] == null ) {
											array84name[z] = nameProv;
											array84number[z] = numberProv;
											z = 85;
										}
									}
									nameProv = "";
									numberProv = "";
									typeProv = "";
								} else if ( typeProv.startsWith( "XML" ) || typeProv.startsWith( "BINARY VARYING" )
										|| typeProv.startsWith( "BINARY" ) || typeProv.startsWith( "BIT VARYING" )
										|| typeProv.startsWith( "BIT" ) ) {
									// 60-71 = Prio6 "BINARY VARYING", "BINARY", "BIT VARYING", "BIT" und "XML"
									for ( int z = 60; z < 72; z++ ) {
										if ( array84number[z] == null ) {
											array84name[z] = nameProv;
											array84number[z] = numberProv;
											z = 73;
										}
									}
									nameProv = "";
									numberProv = "";
									typeProv = "";
								} else if ( typeProv.startsWith( "TIMESTAMP WITH TIME ZONE" )
										|| typeProv.startsWith( "TIMESTAMP" )
										|| typeProv.startsWith( "TIME WITH TIME ZONE" )
										|| typeProv.startsWith( "TIME" ) ) {
									// 48-59 = Prio5 "TIME", "TIME WITH TIME ZONE", "TIMESTAMP" und
									// "TIMESTAMP WITH TIME ZONE"
									for ( int z = 48; z < 60; z++ ) {
										if ( array84number[z] == null ) {
											array84name[z] = nameProv;
											array84number[z] = numberProv;
											z = 61;
										}
									}
									nameProv = "";
									numberProv = "";
									typeProv = "";
								} else if ( typeProv.startsWith( "DOUBLE PRECISION" )
										|| typeProv.startsWith( "FLOAT" ) || typeProv.startsWith( "INTERVAL" )
										|| typeProv.startsWith( "REAL" ) ) {
									// 36-47 = Prio4 "DOUBLE PRECISION", "FLOAT", "INTERVAL" und "REAL"
									for ( int z = 36; z < 48; z++ ) {
										if ( array84number[z] == null ) {
											array84name[z] = nameProv;
											array84number[z] = numberProv;
											z = 49;
										}
									}
									nameProv = "";
									numberProv = "";
									typeProv = "";
								} else if ( typeProv.startsWith( "BIGINT" ) || typeProv.startsWith( "INTEGER" )
										|| typeProv.startsWith( "SMALLINT" ) || typeProv.startsWith( "NUMERIC" ) ) {
									// 24-35 = Prio3 "BIGINT", "INTEGER", "SMALLINT" und "NUMERIC"
									/* System.out.println( "Prio 3 = Name: " + nameProv + " / Number: " + numberProv +
									 * " / Type: " + typeProv ); */
									for ( int z = 24; z < 36; z++ ) {
										if ( array84number[z] == null ) {
											array84name[z] = nameProv;
											array84number[z] = numberProv;
											z = 37;
										}
									}
									nameProv = "";
									numberProv = "";
									typeProv = "";
								} else if ( typeProv.startsWith( "DECIMAL" )
										|| typeProv.startsWith( "NATIONAL CHARACTER VARYING" )
										|| typeProv.startsWith( "NATIONAL CHARACTER" ) ) {
									// 12-23 = Prio2 "DECIMAL", "NATIONAL CHARACTER VARYING" und
									// "NATIONAL CHARACTER"
									for ( int z = 12; z < 24; z++ ) {
										if ( array84number[z] == null ) {
											array84name[z] = nameProv;
											array84number[z] = numberProv;
											z = 25;
										}
									}
									nameProv = "";
									numberProv = "";
									typeProv = "";
								} else if ( typeProv.startsWith( "CHARACTER VARYING" )
										|| typeProv.startsWith( "CHARACTER" ) || typeProv.startsWith( "DATE" ) ) {
									// 0-11 = Prio1 "CHARACTER VARYING", "CHARACTER" und "DATE"
									/* System.out.println( "Prio 1 = Name: " + nameProv + " / Number: " + numberProv +
									 * " / Type: " + typeProv ); */
									for ( int z = 0; z < 12; z++ ) {
										if ( array84number[z] == null ) {
											array84name[z] = nameProv;
											array84number[z] = numberProv;
											z = 13;
										}
									}
									nameProv = "";
									numberProv = "";
									typeProv = "";
								} else {
									// PRIO 7 fuer den Rest
									for ( int z = 72; z < 84; z++ ) {
										if ( array84number[z] == null ) {
											array84name[z] = nameProv;
											array84number[z] = numberProv;
											z = 85;
										}
									}
									nameProv = "";
									numberProv = "";
									typeProv = "";
								}
							}
						}
						/* die verschiedenen Prio zusammenfassen. p1 mit p2 erweitern, dann p3, dann p4...
						 * 
						 * beim array84 werden die leerstellen geloescht respektive mit dem Nachfolger
						 * ersetzt */
						int yArray = 0;
						for ( int x = 0; x < 83; x++ ) {
							// System.out.println( Arrays.toString( array84number ) );
							if ( array84number[x] == null ) {
								for ( yArray = x + 1; yArray < 84; yArray++ ) {
									if ( array84number[x] == null ) {
										array84number[x] = array84number[yArray];
										array84number[yArray] = null;
										array84name[x] = array84name[yArray];
										array84name[yArray] = null;
									}
								}
							}
						}

						array12name[0] = "<mc1name>" + array84name[0] + "</mc1name>";
						array12number[0] = "<mc1number>" + array84number[0] + "</mc1number>";
						array12name[1] = "<mc2name>" + array84name[1] + "</mc2name>";
						array12number[1] = "<mc2number>" + array84number[1] + "</mc2number>";
						array12name[2] = "<mc3name>" + array84name[2] + "</mc3name>";
						array12number[2] = "<mc3number>" + array84number[2] + "</mc3number>";
						array12name[3] = "<mc4name>" + array84name[3] + "</mc4name>";
						array12number[3] = "<mc4number>" + array84number[3] + "</mc4number>";
						array12name[4] = "<mc5name>" + array84name[4] + "</mc5name>";
						array12number[4] = "<mc5number>" + array84number[4] + "</mc5number>";
						array12name[5] = "<mc6name>" + array84name[5] + "</mc6name>";
						array12number[5] = "<mc6number>" + array84number[5] + "</mc6number>";
						array12name[6] = "<mc7name>" + array84name[6] + "</mc7name>";
						array12number[6] = "<mc7number>" + array84number[6] + "</mc7number>";
						array12name[7] = "<mc8name>" + array84name[7] + "</mc8name>";
						array12number[7] = "<mc8number>" + array84number[7] + "</mc8number>";
						array12name[8] = "<mc9name>" + array84name[8] + "</mc9name>";
						array12number[8] = "<mc9number>" + array84number[8] + "</mc9number>";
						array12name[9] = "<mc10name>" + array84name[9] + "</mc10name>";
						array12number[9] = "<mc10number>" + array84number[9] + "</mc10number>";
						array12name[10] = "<mc11name>" + array84name[10] + "</mc11name>";
						array12number[10] = "<mc11number>" + array84number[10] + "</mc11number>";

					} else {

						if ( nlPK.getLength() > 1 && nlFK.getLength() > 0 ) {
							/* TODO: MARKER -> mehrere Primaerschluessel und mindestens ein foreignKey. der
							 * Hauptschluessel soll der sein, welcher am meisten verwendet wird. */
							// for ( int x = 0; x < nlPK.getLength(); x++ ) {

							int refTabNumber0 = -1;
							int refTabCounter0 = 0;
							String refTabValue0 = "";
							int refTabNumber1 = -1;
							int refTabCounter1 = 0;
							String refTabValue1 = "";
							int refTabNumber2 = -1;
							int refTabCounter2 = 0;
							String refTabValue2 = "";
							int refTabNumber3 = -1;
							int refTabCounter3 = 0;
							String refTabValue3 = "";
							int refTabNumber4 = -1;
							int refTabCounter4 = 0;
							String refTabValue4 = "";
							int refTabNumber5 = -1;
							int refTabCounter5 = 0;
							String refTabValue5 = "";
							int refTabNumber6 = -1;
							int refTabCounter6 = 0;
							String refTabValue6 = "";
							int refTabNumber7 = -1;
							int refTabCounter7 = 0;
							String refTabValue7 = "";
							int refTabNumber8 = -1;
							int refTabCounter8 = 0;
							String refTabValue8 = "";
							int refTabNumber9 = -1;
							int refTabCounter9 = 0;
							String refTabValue9 = "";
							int refTabHalf = 0;

							/* eine nodelist mit referencedTable erstellen, Treffer anzahl auslesen und die
							 * Haelfte bestimmen. Wert der 1. Referenced Tablename herauslesen & zaehler =1. Dann
							 * der zweite Wert herauslesen, wenn gleich zaehler erhoehen, ansonsten nr merken. Den
							 * 3. und ff Wert herauslesen und ggf zaehler erhoehen. Wenn Zaehler am Ende Hoeher/=
							 * der haelfte ist, ist es der meistverwendete Primaerschluessel. Ansonsten wert
							 * speichern und mit anderen Werten gleich weiterfahren. Wenn keiner die Haelfte
							 * erreicht, herausfinder welcher die meisten treffer hat. */

							NodeList nlRT = docConfig.getElementsByTagName( "referencedTable" );
							// System.out.println( "Anzahl ReferencedTable: " + (nlRT.getLength() + 1) / 2 );
							for ( int x1 = 0; x1 < nlRT.getLength(); x1++ ) {
								refTabHalf = nlRT.getLength() / 2;
								if ( refTabNumber0 == -1 ) {
									refTabNumber0 = x1;
									refTabCounter0 = 1;
									refTabValue0 = nlRT.item( x1 ).getTextContent();
								} else {
									// es existiert bereits die erste Referenz
									if ( nlRT.item( x1 ).getTextContent().equals( refTabValue0 ) ) {
										// identisch mit der 1. Ref --> Zaehler erhoehen
										refTabCounter0 = refTabCounter0 + 1;
									} else if ( refTabNumber1 == -1 ) {
										refTabNumber1 = x1;
										refTabCounter1 = 1;
										refTabValue1 = nlRT.item( x1 ).getTextContent();
									} else {
										// es existiert bereits die 2. Referenz
										if ( nlRT.item( x1 ).getTextContent().equals( refTabValue1 ) ) {
											// identisch mit der 2. Ref --> Zaehler erhoehen
											refTabCounter1 = refTabCounter1 + 1;
										} else if ( refTabNumber2 == -1 ) {
											refTabNumber2 = x1;
											refTabCounter2 = 1;
											refTabValue2 = nlRT.item( x1 ).getTextContent();
										} else {
											// es existiert bereits die 3. Referenz
											if ( nlRT.item( x1 ).getTextContent().equals( refTabValue2 ) ) {
												// identisch mit der 3. Ref --> Zaehler erhoehen
												refTabCounter2 = refTabCounter2 + 1;
											} else if ( refTabNumber3 == -1 ) {
												refTabNumber3 = x1;
												refTabCounter3 = 1;
												refTabValue3 = nlRT.item( x1 ).getTextContent();
											} else {
												// es existiert bereits die 4. Referenz
												if ( nlRT.item( x1 ).getTextContent().equals( refTabValue3 ) ) {
													// identisch mit der 4. Ref --> Zaehler erhoehen
													refTabCounter3 = refTabCounter3 + 1;
												} else if ( refTabNumber4 == -1 ) {
													refTabNumber4 = x1;
													refTabCounter4 = 1;
													refTabValue4 = nlRT.item( x1 ).getTextContent();
												} else {
													// es existiert bereits die 5. Referenz
													if ( nlRT.item( x1 ).getTextContent().equals( refTabValue4 ) ) {
														// identisch mit der 5. Ref --> Zaehler erhoehen
														refTabCounter4 = refTabCounter4 + 1;
													} else if ( refTabNumber5 == -1 ) {
														refTabNumber5 = x1;
														refTabCounter5 = 1;
														refTabValue5 = nlRT.item( x1 ).getTextContent();
													} else {
														// es existiert bereits die 6. Referenz
														if ( nlRT.item( x1 ).getTextContent().equals( refTabValue5 ) ) {
															// identisch mit der 6. Ref --> Zaehler erhoehen
															refTabCounter5 = refTabCounter5 + 1;
														} else if ( refTabNumber6 == -1 ) {
															refTabNumber6 = x1;
															refTabCounter6 = 1;
															refTabValue6 = nlRT.item( x1 ).getTextContent();
														} else {
															// es existiert bereits die 7. Referenz
															if ( nlRT.item( x1 ).getTextContent().equals( refTabValue6 ) ) {
																// identisch mit der 7. Ref --> Zaehler erhoehen
																refTabCounter6 = refTabCounter6 + 1;
															} else if ( refTabNumber7 == -1 ) {
																refTabNumber7 = x1;
																refTabCounter7 = 1;
																refTabValue7 = nlRT.item( x1 ).getTextContent();
															} else {
																// es existiert bereits die 8. Referenz
																if ( nlRT.item( x1 ).getTextContent().equals( refTabValue7 ) ) {
																	// identisch mit der 8. Ref --> Zaehler erhoehen
																	refTabCounter7 = refTabCounter7 + 1;
																} else if ( refTabNumber8 == -1 ) {
																	refTabNumber8 = x1;
																	refTabCounter8 = 1;
																	refTabValue8 = nlRT.item( x1 ).getTextContent();
																} else {
																	// es existiert bereits die 9. Referenz
																	if ( nlRT.item( x1 ).getTextContent().equals( refTabValue8 ) ) {
																		// identisch mit der 9. Ref --> Zaehler erhoehen
																		refTabCounter8 = refTabCounter8 + 1;
																	} else if ( refTabNumber9 == -1 ) {
																		refTabNumber9 = x1;
																		refTabCounter9 = 1;
																		refTabValue9 = nlRT.item( x1 ).getTextContent();
																	} else {
																		// es existiert bereits die 10. Referenz
																		if ( nlRT.item( x1 ).getTextContent().equals( refTabValue9 ) ) {
																			// identisch mit der 10. Ref --> Zaehler erhoehen
																			refTabCounter9 = refTabCounter9 + 1;
																		} else {
																			// nichts, mehr referenzen sind nicht vorgesehen
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
							/* System.out.println( "0=" + refTabCounter0 + "  1=" + refTabCounter1 + "  2=" +
							 * refTabCounter2 + "  3=" + refTabCounter3 + "  4=" + refTabCounter4 + "  5=" +
							 * refTabCounter5 + "  6=" + refTabCounter6 + "  7=" + refTabCounter7 + "  8=" +
							 * refTabCounter8 + "  9=" + refTabCounter9 ); */
							if ( refTabCounter0 >= refTabHalf ) {
								mainname = refTabValue0;
							} else if ( refTabCounter1 >= refTabHalf ) {
								mainname = refTabValue1;
							} else if ( refTabCounter2 >= refTabHalf ) {
								mainname = refTabValue2;
							} else if ( refTabCounter3 >= refTabHalf ) {
								mainname = refTabValue3;
							} else if ( refTabCounter4 >= refTabHalf ) {
								mainname = refTabValue4;
							} else if ( refTabCounter5 >= refTabHalf ) {
								mainname = refTabValue5;
							} else if ( refTabCounter6 >= refTabHalf ) {
								mainname = refTabValue6;
							} else if ( refTabCounter7 >= refTabHalf ) {
								mainname = refTabValue7;
							} else if ( refTabCounter8 >= refTabHalf ) {
								mainname = refTabValue8;
							} else if ( refTabCounter9 >= refTabHalf ) {
								mainname = refTabValue9;
							} else {
								// Herausfinden welcher Zaehler der Groessere ist
								int refTabCounter = 0;
								if ( refTabCounter0 < refTabCounter1 ) {
									refTabCounter = refTabCounter1;
									mainname = refTabValue1;
								} else {
									refTabCounter = refTabCounter0;
									mainname = refTabValue0;
								}
								if ( refTabCounter < refTabCounter2 ) {
									refTabCounter = refTabCounter2;
									mainname = refTabValue2;
								}
								if ( refTabCounter < refTabCounter3 ) {
									refTabCounter = refTabCounter3;
									mainname = refTabValue3;
								}
								if ( refTabCounter < refTabCounter4 ) {
									refTabCounter = refTabCounter4;
									mainname = refTabValue4;
								}
								if ( refTabCounter < refTabCounter5 ) {
									refTabCounter = refTabCounter5;
									mainname = refTabValue5;
								}
								if ( refTabCounter < refTabCounter6 ) {
									refTabCounter = refTabCounter6;
									mainname = refTabValue6;
								}
								if ( refTabCounter < refTabCounter7 ) {
									refTabCounter = refTabCounter7;
									mainname = refTabValue7;
								}
								if ( refTabCounter < refTabCounter8 ) {
									refTabCounter = refTabCounter8;
									mainname = refTabValue8;
								}
								if ( refTabCounter < refTabCounter9 ) {
									refTabCounter = refTabCounter9;
									mainname = refTabValue9;
								}
							}

							// mainname ist jetzt bekannt
							inputMainname = mainname;
							// System.out.println( "inputMainname: " + inputMainname );
							/* Kontrollieren ob inputMainname existiert. Wenn nicht dann false und gleich wie (..)
							 * weiter fahren */
							NodeList nlTables = docConfig.getElementsByTagName( "tables" );
							int xMainTable = 0;
							int x0MainTable = 0;
							for ( int x0 = 0; x0 < nlTables.getLength(); x0++ ) {
								Node nodeTablesDetail = nlTables.item( x0 );
								NodeList nlTable = nodeTablesDetail.getChildNodes();
								for ( int x = 0; x < nlTable.getLength(); x++ ) {
									if ( !boolMainname ) {
										Node nodeTable = nlTable.item( x );
										NodeList nlChildTable = nodeTable.getChildNodes();
										for ( int y = 0; y < nlChildTable.getLength(); y++ ) {
											Node nodeDetail = nlChildTable.item( y );
											if ( nodeDetail.getNodeName().equals( "name" ) ) {
												mainnameProv = nodeDetail.getTextContent();
												// System.out.println( "mainnameProv " + mainnameProv );
												if ( mainnameProv.equals( inputMainname ) ) {
													// System.out.println( "mainname " +" = " + inputMainname );
													mainname = mainnameProv;
													boolMainname = true;
													xMainTable = x;
													x0MainTable = x0;
													x0 = nlTables.getLength();

													// Schema name und folder herauslesen
													Node mainTables = nodeTable.getParentNode();
													Node mainSchema = mainTables.getParentNode();
													NodeList nlSchemaChild = mainSchema.getChildNodes();
													for ( int x1 = 0; x1 < nlSchemaChild.getLength(); x1++ ) {
														// fuer jedes Subelement der Tabelle (name, folder, description...) ...
														Node subNode = nlSchemaChild.item( x1 );
														if ( subNode.getNodeName().equals( "name" ) ) {
															mainschemaname = subNode.getTextContent();
														} else if ( subNode.getNodeName().equals( "folder" ) ) {
															mainschemafolder = subNode.getTextContent();
														}
													}

												}
											} else if ( nodeDetail.getNodeName().equals( "folder" ) ) {
												mainfolderProv = nodeDetail.getTextContent();
												// System.out.println( "mainfolderProv " + mainfolderProv );
											} else if ( nodeDetail.getNodeName().equals( "primaryKey" ) ) {
												NodeList nlPKmn = nodeDetail.getChildNodes();
												for ( int z = 0; z < nlPKmn.getLength(); z++ ) {
													Node nodeDetailPK = nlPKmn.item( z );
													if ( nodeDetailPK.getNodeName().equals( "column" ) ) {
														primarykeynameProv = nodeDetailPK.getTextContent();
														// System.out.println( "primarykeynameProv " + primarykeynameProv );
													}
												}
											}
										}
										if ( boolMainname ) {
											/* haupttabelle gefunden */
											mainfolder = mainfolderProv;
											primarykeyname = primarykeynameProv;
										}
									} else {
										/* haupttabelle gefunden */
										mainfolder = mainfolderProv;
										primarykeyname = primarykeynameProv;

										x0 = nlTables.getLength();
										// beendet die For-schleife

										// Schema name und folder herauslesen
										Node mainTables = nodeTablesDetail.getParentNode();
										NodeList nlTablesChild = mainTables.getChildNodes();
										for ( int x2 = 0; x2 < nlTablesChild.getLength(); x2++ ) {
											// fuer jedes Subelement der Tabelle (name, folder, description...) ...
											Node subNode = nlTablesChild.item( x2 );
											if ( subNode.getNodeName().equals( "name" ) ) {
												mainschemaname = subNode.getTextContent();
											} else if ( subNode.getNodeName().equals( "folder" ) ) {
												mainschemafolder = subNode.getTextContent();
											}
										}

									}
								}
							}
							if ( mainfolder == "" ) {
								mainfolder = mainfolderProv;
							}
							if ( primarykeyname == "" ) {
								primarykeyname = primarykeynameProv;
							}
							// primarykeycell mit xMainTable herausfinden
							Node nodeTable = nlTables.item( x0MainTable );
							NodeList nlChildTable = nodeTable.getChildNodes();
							// for ( int y = 0; y < nlChildTable.getLength(); y++ ) {
							Node nodeDetailTable = nlChildTable.item( xMainTable );
							NodeList nlChildDetailTable = nodeDetailTable.getChildNodes();
							for ( int y = 0; y < nlChildDetailTable.getLength(); y++ ) {
								Node nodeSubDetailTable = nlChildDetailTable.item( y );
								// System.out.println( "NodeName = " + nodeSubDetailTable.getNodeName() );
								if ( nodeSubDetailTable.getNodeName().equals( "columns" ) ) {
									NodeList nlColumnsMT = nodeSubDetailTable.getChildNodes();
									for ( int z = 0; z < nlColumnsMT.getLength(); z++ ) {
										// fuer jedes Subelement der Colummns (column) ...
										Node nodeDetailMT = nlColumnsMT.item( z );
										// System.out.println( nodeDetailMT.getNodeName() );
										if ( nodeDetailMT.getNodeName().equals( "column" ) ) {
											// System.out.println( "primarykeyname: "+ primarykeyname );
											NodeList nlColumnMT = nodeDetailMT.getChildNodes();
											for ( int z1 = 0; z1 < nlColumnMT.getLength(); z1++ ) {
												// fuer jedes Subelement der Colummn (name, number, description...) ...
												Node nodeDetailMtC = nlColumnMT.item( z1 );
												/* System.out.println( nodeDetailMtC.getNodeName() + " : " +
												 * nodeDetailMtC.getTextContent() ); */
												if ( nodeDetailMtC.getNodeName().equals( "name" ) ) {
													if ( nodeDetailMtC.getTextContent().equals( primarykeyname ) ) {
														boolPKname = true;
														z = nlColumnsMT.getLength();
													} else {
														boolPKname = false;
													}
												} else if ( nodeDetailMtC.getNodeName().equals( "number" ) ) {
													primarykeycellProv = nodeDetailMtC.getTextContent();
												}
											}
											if ( boolPKname ) {
												primarykeycell = primarykeycellProv;
												z = nlColumnsMT.getLength();
												y = nlChildTable.getLength();
											}
										}
									}

									for ( int zC = 0; zC < nlColumnsMT.getLength(); zC++ ) {
										// fuer jedes Subelement der Colummns (column) ...
										Node nodeColumn = nlColumnsMT.item( zC );
										NodeList nlMostColumn = nodeColumn.getChildNodes();
										for ( int yC = 0; yC < nlMostColumn.getLength(); yC++ ) {
											// fuer jedes Subelement der Colummn (name, number, description...) ...
											Node subNode = nlMostColumn.item( yC );
											if ( subNode.getNodeName().equals( "name" ) ) {
												nameProv = subNode.getTextContent();
											} else if ( subNode.getNodeName().equals( "number" ) ) {
												numberProv = subNode.getTextContent();
											} else if ( subNode.getNodeName().equals( "type" ) ) {
												typeProv = subNode.getTextContent();
											}
										}
										/* Hoechste Prio (1) fuer "CHARACTER VARYING", "CHARACTER" und "DATE".
										 * 
										 * 2 fuer "DECIMAL", "NATIONAL CHARACTER VARYING" und "NATIONAL CHARACTER".
										 * 
										 * 3 fuer "BIGINT", "INTEGER", "SMALLINT" und "NUMERIC".
										 * 
										 * 4 Fuer "DOUBLE PRECISION", "FLOAT", "INTERVAL" und "REAL".
										 * 
										 * 5 fuer "TIME", "TIME WITH TIME ZONE", "TIMESTAMT" und
										 * "TIMESTAMP WITH TIME ZONE".
										 * 
										 * 6 fuer "BINARY VARYING", "BINARY", "BIT VARYING", "BIT" und "XML".
										 * 
										 * 7 fuer "BINARY LARGE OBJECT", "BOOLEAN", "CHARACTER LARGE OBJECT",
										 * "NATIONAL CHARACTER LARGE OBJECT" und ggf andere */

										if ( !numberProv.equals( "" ) || !nameProv.equals( "" )
												|| !typeProv.equals( "" ) ) {
											/* System.out.println( "Name: " + nameProv + " / Number: " + numberProv +
											 * " / Type: " + typeProv ); */
											if ( numberProv.equals( primarykeycell ) ) {
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "BINARY LARGE OBJECT" )
													|| typeProv.startsWith( "BOOLEAN" )
													|| typeProv.startsWith( "CHARACTER LARGE OBJECT" )
													|| typeProv.startsWith( "NATIONAL CHARACTER LARGE OBJECT" ) ) {
												// 72-83 = Prio7
												for ( int z = 72; z < 84; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 85;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "XML" )
													|| typeProv.startsWith( "BINARY VARYING" )
													|| typeProv.startsWith( "BINARY" ) || typeProv.startsWith( "BIT VARYING" )
													|| typeProv.startsWith( "BIT" ) ) {
												// 60-71 = Prio6 "BINARY VARYING", "BINARY", "BIT VARYING", "BIT" und "XML"
												for ( int z = 60; z < 72; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 73;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "TIMESTAMP WITH TIME ZONE" )
													|| typeProv.startsWith( "TIMESTAMP" )
													|| typeProv.startsWith( "TIME WITH TIME ZONE" )
													|| typeProv.startsWith( "TIME" ) ) {
												// 48-59 = Prio5 "TIME", "TIME WITH TIME ZONE", "TIMESTAMP" und
												// "TIMESTAMP WITH TIME ZONE"
												for ( int z = 48; z < 60; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 61;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "DOUBLE PRECISION" )
													|| typeProv.startsWith( "FLOAT" ) || typeProv.startsWith( "INTERVAL" )
													|| typeProv.startsWith( "REAL" ) ) {
												// 36-47 = Prio4 "DOUBLE PRECISION", "FLOAT", "INTERVAL" und "REAL"
												for ( int z = 36; z < 48; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 49;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "BIGINT" )
													|| typeProv.startsWith( "INTEGER" ) || typeProv.startsWith( "SMALLINT" )
													|| typeProv.startsWith( "NUMERIC" ) ) {
												// 24-35 = Prio3 "BIGINT", "INTEGER", "SMALLINT" und "NUMERIC"
												/* System.out.println( "Prio 3 = Name: " + nameProv + " / Number: " +
												 * numberProv + " / Type: " + typeProv ); */
												for ( int z = 24; z < 36; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 37;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "DECIMAL" )
													|| typeProv.startsWith( "NATIONAL CHARACTER VARYING" )
													|| typeProv.startsWith( "NATIONAL CHARACTER" ) ) {
												// 12-23 = Prio2 "DECIMAL", "NATIONAL CHARACTER VARYING" und
												// "NATIONAL CHARACTER"
												for ( int z = 12; z < 24; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 25;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "CHARACTER VARYING" )
													|| typeProv.startsWith( "CHARACTER" ) || typeProv.startsWith( "DATE" ) ) {
												// 0-11 = Prio1 "CHARACTER VARYING", "CHARACTER" und "DATE"
												/* System.out.println( "Prio 1 = Name: " + nameProv + " / Number: " +
												 * numberProv + " / Type: " + typeProv ); */
												for ( int z = 0; z < 12; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 13;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else {
												// PRIO 7 fuer den Rest
												for ( int z = 72; z < 84; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 85;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											}
										}
									}
									/* die verschiedenen Prio zusammenfassen. p1 mit p2 erweitern, dann p3, dann p4...
									 * 
									 * beim array84 werden die leerstellen geloescht respektive mit dem Nachfolger
									 * ersetzt */
									int yArray = 0;
									for ( int x = 0; x < 83; x++ ) {
										// System.out.println( Arrays.toString( array84number ) );
										if ( array84number[x] == null ) {
											for ( yArray = x + 1; yArray < 84; yArray++ ) {
												if ( array84number[x] == null ) {
													array84number[x] = array84number[yArray];
													array84number[yArray] = null;
													array84name[x] = array84name[yArray];
													array84name[yArray] = null;
												}
											}
										}
									}

									array12name[0] = "<mc1name>" + array84name[0] + "</mc1name>";
									array12number[0] = "<mc1number>" + array84number[0] + "</mc1number>";
									array12name[1] = "<mc2name>" + array84name[1] + "</mc2name>";
									array12number[1] = "<mc2number>" + array84number[1] + "</mc2number>";
									array12name[2] = "<mc3name>" + array84name[2] + "</mc3name>";
									array12number[2] = "<mc3number>" + array84number[2] + "</mc3number>";
									array12name[3] = "<mc4name>" + array84name[3] + "</mc4name>";
									array12number[3] = "<mc4number>" + array84number[3] + "</mc4number>";
									array12name[4] = "<mc5name>" + array84name[4] + "</mc5name>";
									array12number[4] = "<mc5number>" + array84number[4] + "</mc5number>";
									array12name[5] = "<mc6name>" + array84name[5] + "</mc6name>";
									array12number[5] = "<mc6number>" + array84number[5] + "</mc6number>";
									array12name[6] = "<mc7name>" + array84name[6] + "</mc7name>";
									array12number[6] = "<mc7number>" + array84number[6] + "</mc7number>";
									array12name[7] = "<mc8name>" + array84name[7] + "</mc8name>";
									array12number[7] = "<mc8number>" + array84number[7] + "</mc8number>";
									array12name[8] = "<mc9name>" + array84name[8] + "</mc9name>";
									array12number[8] = "<mc9number>" + array84number[8] + "</mc9number>";
									array12name[9] = "<mc10name>" + array84name[9] + "</mc10name>";
									array12number[9] = "<mc10number>" + array84number[9] + "</mc10number>";
									array12name[10] = "<mc11name>" + array84name[10] + "</mc11name>";
									array12number[10] = "<mc11number>" + array84number[10] + "</mc11number>";
								}
							}

						} else {
							// TODO: MARKER -> nur ein Primaerschluessel, entsprechend ist dies der
							// Hauptschluessel
							// oder die erste Tabelle mit einem Primaerschluessel wen keine Fremdschluessel
							// existieren.
							// */
							Node nodePK = nlPK.item( 0 );
							NodeList childNodesPK = nodePK.getChildNodes();
							for ( int y = 0; y < childNodesPK.getLength(); y++ ) {
								Node subNodePK = childNodesPK.item( y );
								if ( subNodePK.getNodeName().equals( "column" ) ) {
									primarykeyname = new String( subNodePK.getTextContent() );
								}
							}
							Node nodeParentPK = nodePK.getParentNode();
							// nodeParentPK = table
							NodeList childNodesTablePK = nodeParentPK.getChildNodes();

							// Schema name und folder herauslesen
							Node mainTable = nodeParentPK.getParentNode();
							Node mainTables = mainTable.getParentNode();
							NodeList nlTablesChild = mainTables.getChildNodes();
							for ( int x2 = 0; x2 < nlTablesChild.getLength(); x2++ ) {
								// fuer jedes Subelement der Tabelle (name, folder, description...) ...
								Node subNode = nlTablesChild.item( x2 );
								if ( subNode.getNodeName().equals( "name" ) ) {
									mainschemaname = subNode.getTextContent();
								} else if ( subNode.getNodeName().equals( "folder" ) ) {
									mainschemafolder = subNode.getTextContent();
								}
							}

							for ( int y = 0; y < childNodesTablePK.getLength(); y++ ) {
								Node subNodeTablePK = childNodesTablePK.item( y );
								if ( subNodeTablePK.getNodeName().equals( "name" ) ) {
									mainname = new String( subNodeTablePK.getTextContent() );
								} else if ( subNodeTablePK.getNodeName().equals( "folder" ) ) {
									mainfolder = new String( subNodeTablePK.getTextContent() );
								} else if ( subNodeTablePK.getNodeName().equals( "columns" ) ) {

									NodeList childNodesColumns = subNodeTablePK.getChildNodes();
									for ( int y1 = 0; y1 < childNodesColumns.getLength(); y1++ ) {
										// fuer jede Zelle (column) ...
										Node subNodeII = childNodesColumns.item( y1 );
										NodeList childNodesColumn = subNodeII.getChildNodes();
										for ( int z = 0; z < childNodesColumn.getLength(); z++ ) {
											// fuer jedes Subelement der Zelle (name, description...) ...
											Node subNodeIII = childNodesColumn.item( z );
											if ( subNodeIII.getNodeName().equals( "name" ) ) {
												primarykeynameProv = subNodeIII.getTextContent();
												nameProv = primarykeynameProv;
											} else if ( subNodeIII.getNodeName().equals( "number" ) ) {
												primarykeycellProv = subNodeIII.getTextContent();
												// System.out.println( "Zelle Nr " + primarykeycellProv );
												numberProv = primarykeycellProv;
											} else if ( subNodeIII.getNodeName().equals( "type" ) ) {
												typeProv = subNodeIII.getTextContent();
											}
										}
										if ( primarykeynameProv.equals( primarykeyname ) ) {
											primarykeycell = primarykeycellProv;
										}

										/* Hoechste Prio (1) fuer "CHARACTER VARYING", "CHARACTER" und "DATE".
										 * 
										 * 2 fuer "DECIMAL", "NATIONAL CHARACTER VARYING" und "NATIONAL CHARACTER".
										 * 
										 * 3 fuer "BIGINT", "INTEGER", "SMALLINT" und "NUMERIC".
										 * 
										 * 4 Fuer "DOUBLE PRECISION", "FLOAT", "INTERVAL" und "REAL".
										 * 
										 * 5 fuer "TIME", "TIME WITH TIME ZONE", "TIMESTAMT" und
										 * "TIMESTAMP WITH TIME ZONE".
										 * 
										 * 6 fuer "BINARY VARYING", "BINARY", "BIT VARYING", "BIT" und "XML".
										 * 
										 * 7 fuer "BINARY LARGE OBJECT", "BOOLEAN", "CHARACTER LARGE OBJECT",
										 * "NATIONAL CHARACTER LARGE OBJECT" und ggf andere */

										if ( !numberProv.equals( "" ) || !nameProv.equals( "" )
												|| !typeProv.equals( "" ) ) {
											/* System.out.println( "Name: " + nameProv + " / Number: " + numberProv +
											 * " / Type: " + typeProv ); */
											if ( numberProv.equals( primarykeycell ) ) {
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "BINARY LARGE OBJECT" )
													|| typeProv.startsWith( "BOOLEAN" )
													|| typeProv.startsWith( "CHARACTER LARGE OBJECT" )
													|| typeProv.startsWith( "NATIONAL CHARACTER LARGE OBJECT" ) ) {
												// 72-83 = Prio7
												for ( int z = 72; z < 84; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 85;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "XML" )
													|| typeProv.startsWith( "BINARY VARYING" )
													|| typeProv.startsWith( "BINARY" ) || typeProv.startsWith( "BIT VARYING" )
													|| typeProv.startsWith( "BIT" ) ) {
												// 60-71 = Prio6 "BINARY VARYING", "BINARY", "BIT VARYING", "BIT" und
												// "XML"
												for ( int z = 60; z < 72; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 73;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "TIMESTAMP WITH TIME ZONE" )
													|| typeProv.startsWith( "TIMESTAMP" )
													|| typeProv.startsWith( "TIME WITH TIME ZONE" )
													|| typeProv.startsWith( "TIME" ) ) {
												// 48-59 = Prio5 "TIME", "TIME WITH TIME ZONE", "TIMESTAMP" und
												// "TIMESTAMP WITH TIME ZONE"
												for ( int z = 48; z < 60; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 61;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "DOUBLE PRECISION" )
													|| typeProv.startsWith( "FLOAT" ) || typeProv.startsWith( "INTERVAL" )
													|| typeProv.startsWith( "REAL" ) ) {
												// 36-47 = Prio4 "DOUBLE PRECISION", "FLOAT", "INTERVAL" und "REAL"
												for ( int z = 36; z < 48; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 49;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "BIGINT" )
													|| typeProv.startsWith( "INTEGER" ) || typeProv.startsWith( "SMALLINT" )
													|| typeProv.startsWith( "NUMERIC" ) ) {
												// 24-35 = Prio3 "BIGINT", "INTEGER", "SMALLINT" und "NUMERIC"
												/* System.out.println( "Prio 3 = Name: " + nameProv + " / Number: " +
												 * numberProv + " / Type: " + typeProv ); */
												for ( int z = 24; z < 36; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 37;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "DECIMAL" )
													|| typeProv.startsWith( "NATIONAL CHARACTER VARYING" )
													|| typeProv.startsWith( "NATIONAL CHARACTER" ) ) {
												// 12-23 = Prio2 "DECIMAL", "NATIONAL CHARACTER VARYING" und
												// "NATIONAL CHARACTER"
												for ( int z = 12; z < 24; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 25;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else if ( typeProv.startsWith( "CHARACTER VARYING" )
													|| typeProv.startsWith( "CHARACTER" ) || typeProv.startsWith( "DATE" ) ) {
												// 0-11 = Prio1 "CHARACTER VARYING", "CHARACTER" und "DATE"
												/* System.out.println( "Prio 1 = Name: " + nameProv + " / Number: " +
												 * numberProv + " / Type: " + typeProv ); */
												for ( int z = 0; z < 12; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 13;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											} else {
												// PRIO 7 fuer den Rest
												for ( int z = 72; z < 84; z++ ) {
													if ( array84number[z] == null ) {
														array84name[z] = nameProv;
														array84number[z] = numberProv;
														z = 85;
													}
												}
												nameProv = "";
												numberProv = "";
												typeProv = "";
											}
										}
									}
									/* die verschiedenen Prio zusammenfassen. p1 mit p2 erweitern, dann p3, dann p4...
									 * 
									 * beim array84 werden die leerstellen geloescht respektive mit dem Nachfolger
									 * ersetzt */
									int yArray = 0;
									for ( int xArray = 0; xArray < 83; xArray++ ) {
										// System.out.println( Arrays.toString( array84number ) );
										if ( array84number[xArray] == null ) {
											for ( yArray = xArray + 1; yArray < 84; yArray++ ) {
												if ( array84number[xArray] == null ) {
													array84number[xArray] = array84number[yArray];
													array84number[yArray] = null;
													array84name[xArray] = array84name[yArray];
													array84name[yArray] = null;
												}
											}
										}
									}

									array12name[0] = "<mc1name>" + array84name[0] + "</mc1name>";
									array12number[0] = "<mc1number>" + array84number[0] + "</mc1number>";
									array12name[1] = "<mc2name>" + array84name[1] + "</mc2name>";
									array12number[1] = "<mc2number>" + array84number[1] + "</mc2number>";
									array12name[2] = "<mc3name>" + array84name[2] + "</mc3name>";
									array12number[2] = "<mc3number>" + array84number[2] + "</mc3number>";
									array12name[3] = "<mc4name>" + array84name[3] + "</mc4name>";
									array12number[3] = "<mc4number>" + array84number[3] + "</mc4number>";
									array12name[4] = "<mc5name>" + array84name[4] + "</mc5name>";
									array12number[4] = "<mc5number>" + array84number[4] + "</mc5number>";
									array12name[5] = "<mc6name>" + array84name[5] + "</mc6name>";
									array12number[5] = "<mc6number>" + array84number[5] + "</mc6number>";
									array12name[6] = "<mc7name>" + array84name[6] + "</mc7name>";
									array12number[6] = "<mc7number>" + array84number[6] + "</mc7number>";
									array12name[7] = "<mc8name>" + array84name[7] + "</mc8name>";
									array12number[7] = "<mc8number>" + array84number[7] + "</mc8number>";
									array12name[8] = "<mc9name>" + array84name[8] + "</mc9name>";
									array12number[8] = "<mc9number>" + array84number[8] + "</mc9number>";
									array12name[9] = "<mc10name>" + array84name[9] + "</mc10name>";
									array12number[9] = "<mc10number>" + array84number[9] + "</mc10number>";
									array12name[10] = "<mc11name>" + array84name[10] + "</mc11name>";
									array12number[10] = "<mc11number>" + array84number[10] + "</mc11number>";
								}
							}
						}
						// }
					}
				}

				// TODO Config maintable erstellen
				title = getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_TITLE, mainname );
				title = "<mtitle>" + title + "</mtitle>";
				String valueMainname = mainname;
				mainname = "<mname>" + mainname + "</mname>";
				mainfolder = "<mfolder>" + mainfolder + "</mfolder>";
				String valuePKname = primarykeyname;
				primarykeyname = "<mpkname>" + primarykeyname + "</mpkname>";
				primarykeycell = "<mpkcell>" + primarykeycell + "</mpkcell>";
				String valueMainschemaname = mainschemaname;
				mainschemaname = "<mschemaname>" + mainschemaname + "</mschemaname>";
				mainschemafolder = "<mschemafolder>" + mainschemafolder + "</mschemafolder>";

				// replace (..) mit wert
				String titleNo = "<mtitle>(..)</mtitle>";
				Util.oldnewstring( titleNo, title, configFileHard );
				String mainnameNo = "<mname>(..)</mname>";
				Util.oldnewstring( mainnameNo, mainname, configFileHard );
				String mainfolderNo = "<mfolder>(..)</mfolder>";
				Util.oldnewstring( mainfolderNo, mainfolder, configFileHard );
				String primarykeynameNo = "<mpkname>(..)</mpkname>";
				Util.oldnewstring( primarykeynameNo, primarykeyname, configFileHard );
				String primarykeycellNo = "<mpkcell>(..)</mpkcell>";
				Util.oldnewstring( primarykeycellNo, primarykeycell, configFileHard );
				String mainschemanameNo = "<mschemaname>(..)</mschemaname>";
				Util.oldnewstring( mainschemanameNo, mainschemaname, configFileHard );
				String mainschemafolderNo = "<mschemafolder>(..)</mschemafolder>";
				Util.oldnewstring( mainschemafolderNo, mainschemafolder, configFileHard );
				String name1No = "<mc1name>(..)</mc1name>";
				Util.oldnewstring( name1No, array12name[0], configFileHard );
				String number1No = "<mc1number>(..)</mc1number>";
				Util.oldnewstring( number1No, array12number[0], configFileHard );
				String name2No = "<mc2name>(..)</mc2name>";
				Util.oldnewstring( name2No, array12name[1], configFileHard );
				String number2No = "<mc2number>(..)</mc2number>";
				Util.oldnewstring( number2No, array12number[1], configFileHard );
				String name3No = "<mc3name>(..)</mc3name>";
				Util.oldnewstring( name3No, array12name[2], configFileHard );
				String number3No = "<mc3number>(..)</mc3number>";
				Util.oldnewstring( number3No, array12number[2], configFileHard );
				String name4No = "<mc4name>(..)</mc4name>";
				Util.oldnewstring( name4No, array12name[3], configFileHard );
				String number4No = "<mc4number>(..)</mc4number>";
				Util.oldnewstring( number4No, array12number[3], configFileHard );
				String name5No = "<mc5name>(..)</mc5name>";
				Util.oldnewstring( name5No, array12name[4], configFileHard );
				String number5No = "<mc5number>(..)</mc5number>";
				Util.oldnewstring( number5No, array12number[4], configFileHard );
				String name6No = "<mc6name>(..)</mc6name>";
				Util.oldnewstring( name6No, array12name[5], configFileHard );
				String number6No = "<mc6number>(..)</mc6number>";
				Util.oldnewstring( number6No, array12number[5], configFileHard );
				String name7No = "<mc7name>(..)</mc7name>";
				Util.oldnewstring( name7No, array12name[6], configFileHard );
				String number7No = "<mc7number>(..)</mc7number>";
				Util.oldnewstring( number7No, array12number[6], configFileHard );
				String name8No = "<mc8name>(..)</mc8name>";
				Util.oldnewstring( name8No, array12name[7], configFileHard );
				String number8No = "<mc8number>(..)</mc8number>";
				Util.oldnewstring( number8No, array12number[7], configFileHard );
				String name9No = "<mc9name>(..)</mc9name>";
				Util.oldnewstring( name9No, array12name[8], configFileHard );
				String number9No = "<mc9number>(..)</mc9number>";
				Util.oldnewstring( number9No, array12number[8], configFileHard );
				String name10No = "<mc10name>(..)</mc10name>";
				Util.oldnewstring( name10No, array12name[9], configFileHard );
				String number10No = "<mc10number>(..)</mc10number>";
				Util.oldnewstring( number10No, array12number[9], configFileHard );
				String name11No = "<mc11name>(..)</mc11name>";
				Util.oldnewstring( name11No, array12name[10], configFileHard );
				String number11No = "<mc11number>(..)</mc11number>";
				Util.oldnewstring( number11No, array12number[10], configFileHard );

				/* TODO: config subtables erstellen.
				 * 
				 * <subtable1>
				 * <st1keyname>(..)</st1keyname><st1name>(..)</st1name><st1folder>(..)</st1folder
				 * ><st1fkcell>(..)</st1fkcell> </subtable1> <subtables>(..)</subtables> mit Werte
				 * ersetzten.
				 * 
				 * Wenn mainname = referencedTable und referenced = primarykeyname, dann ermitteln, welcher
				 * name und folder diese Tabelle hat und die Zellnummer der reference-column.
				 * 
				 * Das fuer jede Tabelle wiederholen, zu einem Wert zusammensetzten und ersetzen. */

				// Information zu referencedTable und referenced aus metadata holen
				String referencedColumn = "";
				String referencedProv = "";
				String referencedTableProv = "";
				String referencedSchemaProv = "";
				String referencedColumnProv = "";
				String subKeyNameProv = "";
				String subKeyName = "";
				String subName = "";
				String subFolder = "";
				String subKeyCell = "";
				String subKeyCellProv = "";
				String subNameSchema = "";
				String subFolderSchema = "";
				Integer stcounter = 0;
				boolean column = false;

				try {
					// NodeList nlFK = docConfig.getElementsByTagName( "foreignKey" );
					for ( int x = 0; x < nlFK.getLength(); x++ ) {
						referencedTableProv = "";
						referencedSchemaProv = "";
						subKeyNameProv = "";
						referencedColumnProv = "";
						referencedProv = "";
						Node nodeFK = nlFK.item( x );
						NodeList childNodesFK = nodeFK.getChildNodes();
						for ( int y = 0; y < childNodesFK.getLength(); y++ ) {

							Node subNodeFK = childNodesFK.item( y );
							if ( subNodeFK.getNodeName().equals( "referencedTable" ) ) {
								referencedTableProv = new String( subNodeFK.getTextContent() );
							} else if ( subNodeFK.getNodeName().equals( "referencedSchema" ) ) {
								referencedSchemaProv = new String( subNodeFK.getTextContent() );
							} else if ( subNodeFK.getNodeName().equals( "name" ) ) {
								subKeyNameProv = new String( subNodeFK.getTextContent() );
							} else if ( subNodeFK.getNodeName().equals( "reference" ) ) {
								NodeList nlRef = subNodeFK.getChildNodes();
								for ( int z = 0; z < nlRef.getLength(); z++ ) {
									Node nodeRef = nlRef.item( z );

									if ( nodeRef.getNodeName().equals( "column" ) ) {
										referencedColumnProv = nodeRef.getTextContent();
									}
									if ( nodeRef.getNodeName().equals( "referenced" ) ) {
										referencedProv = nodeRef.getTextContent();
									}
								}
							}
						}
						// System.out.println( valueMainname + " =? " + referencedTableProv );
						if ( valueMainname.equals( referencedTableProv )
								&& valueMainschemaname.equals( referencedSchemaProv )
								&& valuePKname.equals( referencedProv ) ) {
							// referencedTable = referencedTableProv;
							subKeyName = subKeyNameProv;
							referencedColumn = referencedColumnProv;
							// referenced = referencedProv;
							referencedTableProv = "";
							referencedSchemaProv = "";
							subKeyNameProv = "";
							referencedColumnProv = "";
							referencedProv = "";
							Node nodeParentFK = nodeFK.getParentNode();
							Node nodeParentFKs = nodeParentFK.getParentNode();
							// nodeParentFKs = table
							NodeList childNodesTableFK = nodeParentFKs.getChildNodes();
							/* <foreignKey> <name>DEPT_MGR_FK</name> <referencedSchema>HR</referencedSchema>
							 * <referencedTable>EMPLOYEES</referencedTable> <reference>
							 * <column>MANAGER_ID</column> <referenced>EMPLOYEE_ID</referenced> </reference>
							 * <deleteAction>RESTRICT</deleteAction> <updateAction>CASCADE</updateAction>
							 * </foreignKey> */
							for ( int y = 0; y < childNodesTableFK.getLength(); y++ ) {
								Node subNodeTableFK = childNodesTableFK.item( y );
								if ( subNodeTableFK.getNodeName().equals( "name" ) ) {
									subName = new String( subNodeTableFK.getTextContent() );
								} else if ( subNodeTableFK.getNodeName().equals( "folder" ) ) {
									subFolder = new String( subNodeTableFK.getTextContent() );
								} else if ( subNodeTableFK.getNodeName().equals( "columns" ) ) {

									NodeList childNodesColumns = subNodeTableFK.getChildNodes();
									for ( int y1 = 0; y1 < childNodesColumns.getLength(); y1++ ) {
										// System.out .println (childNodesColumns.item( y1 ).getTextContent());
										NodeList childNodesColumn = childNodesColumns.item( y1 ).getChildNodes();
										for ( int y2 = 0; y2 < childNodesColumn.getLength(); y2++ ) {
											// fuer jede Zelle (column) ...
											Node columnTable = childNodesColumn.item( y2 );
											// System.out.println(columnTable.getNodeName());
											if ( columnTable.getNodeName().equals( "name" ) ) {
												if ( columnTable.getTextContent().equals( referencedColumn ) ) {
													column = true;
													// System.out.println( "Column stimmt ueberein" );
												} else {
													column = false;
												}
											} else if ( columnTable.getNodeName().equals( "number" ) ) {
												subKeyCellProv = columnTable.getTextContent();
											}
											if ( column && subKeyCellProv != "" ) {
												stcounter = stcounter + 1;
												subKeyCell = subKeyCellProv;
												subName = " <st" + stcounter + "keyname>" + subKeyName + "</st" + stcounter
														+ "keyname><st" + stcounter + "name>" + subName + "</st" + stcounter
														+ "name>";
												subFolder = "<st" + stcounter + "folder>" + subFolder + "</st" + stcounter
														+ "folder>";
												subKeyCell = "<st" + stcounter + "fkcell>" + subKeyCell + "</st" + stcounter
														+ "fkcell>";

												// Schema name und folder herauslesen
												Node mainTables = nodeParentFKs.getParentNode();
												Node mainSchema = mainTables.getParentNode();
												NodeList nlSchemaChild = mainSchema.getChildNodes();
												for ( int x1 = 0; x1 < nlSchemaChild.getLength(); x1++ ) {
													// fuer jedes Subelement der Tabelle (name, folder, description...) ...
													Node subNode = nlSchemaChild.item( x1 );
													if ( subNode.getNodeName().equals( "folder" ) ) {
														subFolderSchema = subNode.getTextContent();
														// System.out.println(subFolderSchema);
													} else if ( subNode.getNodeName().equals( "name" ) ) {
														subNameSchema = subNode.getTextContent();
														// System.out.println(subNameSchema);
													}
												}

												subFolderSchema = "<st" + stcounter + "schemafolder>" + subFolderSchema
														+ "</st" + stcounter + "schemafolder>" + "<st" + stcounter
														+ "schemaname>" + subNameSchema + "</st" + stcounter + "schemaname>";
												/* subName = " <subtable><keyname>" + subKeyName + "</keyname><name>" +
												 * subName + "</name>"; subFolder = "<folder>" + subFolder + "</folder>";
												 * subKeyCell = "<foreignkeycell>" + subKeyCell +
												 * "</foreignkeycell></subtable></subtables>"; */

												// replace (..) mit wert
												// String subtablesNo = "<subtables>(..)</subtables>";
												// String subtables = "<subtables></subtables>";
												// Util.oldnewstring( subtablesNo, subtables, configFileHard );
												String subtablesNo = "<st" + stcounter + "keyname>(..)</st" + stcounter
														+ "keyname><st" + stcounter + "name>(..)</st" + stcounter + "name><st"
														+ stcounter + "folder>(..)</st" + stcounter + "folder><st" + stcounter
														+ "fkcell>(..)</st" + stcounter + "fkcell><st" + stcounter
														+ "schemafolder>(..)</st" + stcounter + "schemafolder><st" + stcounter
														+ "schemaname>(..)</st" + stcounter + "schemaname>";

												String subtable = subName + subFolder + subKeyCell + subFolderSchema;
												// System.out.println( "933: " + subtable );
												Util.oldnewstring( subtablesNo, subtable, configFileHard );
												subName = "";
												subFolder = "";
												subKeyCell = "";
												subKeyCellProv = "";
												subFolderSchema = "";

											} else {
												subKeyCell = "";
												subKeyCellProv = "";
											}
										}
									}
								}
							}

						} else {
							referencedTableProv = "";
							subKeyNameProv = "";
							referencedColumnProv = "";
							referencedProv = "";
							column = false;
						}

					}

				} catch ( Exception e ) {
					System.out
							.println( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_A ) );
					System.out.println( getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN,
							e.getMessage() ) );
					return false;
				}

			} catch ( Exception e ) {
				System.out
						.println( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_A ) );
				System.out.println(
						getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

		} catch ( Exception e ) {
			System.out.println( getTextResourceServiceExc().getText( locale, EXC_MESSAGE_XML_MODUL_A ) );
			System.out.println(
					getTextResourceServiceExc().getText( locale, EXC_ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}
		return result;
	}
}
