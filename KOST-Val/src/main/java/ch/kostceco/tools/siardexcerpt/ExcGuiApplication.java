/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt v0.9.0 application is used for excerpt a record from a SIARD-File. Copyright (C)
 * Claire Roethlisberger (KOST-CECO)
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

package ch.kostceco.tools.siardexcerpt;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ExcGuiApplication extends Application
{

	@Override
	public void start( Stage stage ) throws Exception
	{
		try {
			// festhalten von wo die Applikation (exe) gestartet wurde
			String dirOfJarPath = "";

			/*
			 * dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist
			 * ein generelles TODO in allen Modulen. Zuerst immer dirOfJarPath
			 * ermitteln und dann alle Pfade mit dirOfJarPath + File.separator +
			 * erweitern.
			 */
			String path = new File( "" ).getAbsolutePath();
			String locationOfJarPath = path;
			dirOfJarPath = locationOfJarPath;
			if ( locationOfJarPath.endsWith( ".jar" )
					|| locationOfJarPath.endsWith( ".exe" )
					|| locationOfJarPath.endsWith( "." ) ) {
				File file = new File( locationOfJarPath );
				dirOfJarPath = file.getParent();
			}

			// Read file fxml and draw interface.
			Parent root;
			root = FXMLLoader
					.load( getClass().getResource( "ExcGuiView.fxml" ) );
			Scene scene = new Scene( root );
			scene.getStylesheets().add( getClass()
					.getResource( "application.css" ).toExternalForm() );
			stage.setTitle( "SIARDexcerpt" );
			Image toolIcon = new Image( "file:" + dirOfJarPath + File.separator
					+ "doc" + File.separator + "excicon.png" );
			stage.getIcons().add( toolIcon );
			stage.setScene( scene );
			stage.show();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main( String[] args )
	{
		launch( args );
	}

}