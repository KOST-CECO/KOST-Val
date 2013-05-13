/*== TIFF-Val ==================================================================================
The TIFF-Val application is used for validate Submission Information Package (SIP).
Copyright (C) 2013 Claire Röthlisberger (KOST-CECO)
-----------------------------------------------------------------------------------------------
TIFF-Val is a development of the KOST-CECO. All rights rest with the KOST-CECO. 
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

package ch.kostceco.tools.tiffval.validation.module1.impl;

import java.io.File;
import java.util.List;

import ch.kostceco.tools.tiffval.exception.module1.Validation1bFolderStructureException;
import ch.kostceco.tools.tiffval.validation.ValidationModuleImpl;
import ch.kostceco.tools.tiffval.validation.module1.Validation1bFolderStructureModule;
import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;

/**
 * Besteht eine korrekte primäre Verzeichnisstruktur: /header/metadata.xml
 * /header/xsd /content
 * 
 * @author razm Daniel Ludin, Bedag AG @version 0.2.0
 */
public class Validation1bFolderStructureModuleImpl extends ValidationModuleImpl
		implements Validation1bFolderStructureModule
{

	@SuppressWarnings("unchecked")
	@Override
	public boolean validate( File sipDatei )
			throws Validation1bFolderStructureException
	{

		boolean bExistsXsdFolder = false;
		boolean bExistsContentFolder = false;
		boolean bExistsMetadataFile = false;

		String toplevelDir = sipDatei.getName();
		int lastDotIdx = toplevelDir.lastIndexOf( "." );
		toplevelDir = toplevelDir.substring( 0, lastDotIdx );

		try {

			Zip64File zipfile = new Zip64File( sipDatei );
			List<FileEntry> fileEntryList = zipfile.getListFileEntries();
			for ( FileEntry fileEntry : fileEntryList ) {

				String name = fileEntry.getName();

				if ( (name.equals( "content/" ) || name.equals( toplevelDir
						+ "/content/" ))
						&& (fileEntry.isDirectory()) ) {
					bExistsContentFolder = true;
				}

				if ( (name.equals( "header/xsd/" ) || name.equals( toplevelDir
						+ "/header/xsd/" ))
						&& (fileEntry.isDirectory()) ) {
					bExistsXsdFolder = true;
				}

				if ( (name.equals( "header/metadata.xml" ) || name
						.equals( toplevelDir + "/header/metadata.xml" ))
						&& (!fileEntry.isDirectory()) ) {
					bExistsMetadataFile = true;
				}

			}
			zipfile.close();

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_MODULE_Ab )
							+ getTextResourceService().getText( MESSAGE_DASHES )
							+ e.getMessage() );

			return false;

		}

		return (bExistsContentFolder && bExistsMetadataFile && bExistsXsdFolder);
	}

}
