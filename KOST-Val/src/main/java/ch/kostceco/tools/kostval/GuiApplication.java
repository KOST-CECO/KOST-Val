package ch.kostceco.tools.kostval;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class GuiApplication extends Application
{

	@Override
	public void start( Stage stage ) throws Exception
	{
		try {
			// festhalten von wo die Applikation (exe) gestartet wurde
			String dirOfJarPath = "";

				/* dirOfJarPath damit auch absolute Pfade kein Problem sind Dies ist ein generelles TODO in
				 * allen Modulen. Zuerst immer dirOfJarPath ermitteln und dann alle Pfade mit dirOfJarPath +
				 * File.separator + erweitern. */
				String path = new File( "" ).getAbsolutePath();
				String locationOfJarPath = path;
				dirOfJarPath = locationOfJarPath;
				if ( locationOfJarPath.endsWith( ".jar" ) || locationOfJarPath.endsWith( ".exe" ) ) {
					File file = new File( locationOfJarPath );
					dirOfJarPath = file.getParent();
				}

			// Read file fxml and draw interface.
			Parent root;
			root = FXMLLoader
					.load( getClass().getResource( "GuiView.fxml" ) );
			Scene scene = new Scene( root );
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setTitle( "KOST-Val" );
			Image kostvalIcon = new Image( "file:"+dirOfJarPath + File.separator + "doc" + File.separator+"valicon.png" );
			// Image kostvalIcon = new Image( "file:valicon.png" );
			stage.getIcons().add( kostvalIcon );
			stage.setScene(scene );
			stage.show();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/** @param args
	 *          the command line arguments */
	public static void main( String[] args )
	{
		launch( args );
	}

}