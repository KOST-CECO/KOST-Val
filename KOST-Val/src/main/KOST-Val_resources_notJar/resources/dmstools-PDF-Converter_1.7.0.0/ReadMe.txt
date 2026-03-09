ReadMe zu dmstools PDF/A Converter


Installation:

Die Installation erfolgt durch Ausführen der Datei dmsPDFConverter.exe
Hierbei ist es erforderlich, eine Benutzerkennung mit Administratorrechten zu verwenden.


Unterstützte Betriebssysteme:

- Windows XP SP3, x86/x64 (eingeschränkt)
- Windows Server 2003 SP2, x86/x64 (eingeschränkt)
- Windows Server 2003 R2, x86/x64 (eingeschränkt)
- Windows Vista SP2, x86/x64
- Windows Server 2008 SP2, x86/x64
- Windows 7 SP1, x86/x64
- Windows Server 2008 R2 SP1, x86/x64
- Windows 8.1, x86/x64
- Windows Server 2012 R2, x64
- Windows 10, x86/x64
- Windows Server 2016, x64
- Windows Server 2019, x64
- Windows 11, x86/x64
- Windows Server 2022, x64


Am Ende der Installation wird das Programmverzeichnis geöffnet.
Bitte setzen Sie sich zur Eingabe der Lizenzinformationen mit dem Benutzerservice in Verbindung.

Known Bugs/Bekannte Fehler:

- none

Known Glitches/Bekannte Probleme:

- none


Revision History:

1.0.0.5		Started revision history
1.0.0.6 	Fixed problem with small PDF files
1.0.0.7		Changed linearization license handling
		Disabled linearization
		Fixed problem with very small PDF files
1.0.0.8		Added linearization commandline option
		Added pattern commandline option
1.0.0.9		Added outputintent commandline option
		Added author commandline option
		Added creator commandline option
		Improved XMP standard conformance
		Improved XMP for better results with Adobe Acrobat 7.0.7 preflight
		Fixed OutputIntents dictionary declaration
		Added COM Interface

1.1.0.0		Improved COM interface
		New image processing library
		Preparations for new PDF library

1.2.0.0		New PDF library
		Upgraded image processing library to version 3.8.0.3
1.2.0.1		Use CCITT FAX G4 compression for b/w TIFF
		Use large strip size for tiff compression
		Added Reset method for COM interface
1.2.0.2		Resolution was not detected correctly for some jpeg files without correct resolution information
		Fixed a bug in scaling function for predefined image size
1.2.0.3		Fixed a bug in reading the document information for import documents
1.2.0.4		Fixed a glitch where opening a existing PDF was resetting the converter settings
1.2.0.5		Increased stripesize for TIFFG4 compressed images
1.2.0.6		Changed PDF/A creation code to comply with XMP specification rather than with the conflicting PDF/A specification
		Added commandline file mode
1.2.0.7		Fixed bug in raw pdf parser (escaped parenthesis inside string was not handled correctly)
		Upgraded PDF library to version 6.2.26.312
1.2.0.8		Added AddImageEx function for better control over image import 
		Upgraded PDF library to version 6.2.27.319
1.2.0.9		Fixed GMT offsets for document dates
		Removed usage of old PDF library as TIFF/CCITTG4 workaround is no longer needed and gives errors on some TIFF/JPEG files
1.2.0.10	Added PDF input option
1.2.0.11	Added more descriptive ide error messages for image processing library errors
		Added PCL input option
1.2.0.12	Fixed problem with unicode pdf strings in raw pdf parser
1.2.0.13	Fixed problem with image files (mostly TIFF) with different X and Y resolutions
1.2.0.14	Added option to limit the output resolution
		Added option to limit the jpeg quality
1.2.0.15	Fixed problem with stream formatting

1.3.0.0		Added demo mode label
1.3.0.1		Fixed bug where color profile was damaged on computers with non-european codepages
1.3.0.2		Disabled demo mode label for nodelocked licenses
1.3.0.3		Removed memory leak
		Added debug logging option
		Added multithreading support
1.3.0.4		Added option to set default image resolution
		Added option to set image options from the commandline
		Added fallback to preprocessing mode if direct image insertion fails
		Upgraded image processing library to version 3.13.1.0
1.3.0.5		Added preprocessing option
		Added title option
1.3.0.6		Upgraded image processing library to version 3.13.1.1
		Reenabled large strip size for tiff compression
1.3.0.7		Fixed several performance issues
1.3.0.8		Added swap limit option
		Changed instancing to single use
1.3.0.9		Changed default to not using a swapfile
		Upgraded PDF library to version 6.25.4.549
1.3.0.10	Fixed swapfile handling
1.3.0.11	Fixed memory leak
1.3.0.12	Upgraded image processing library to version 3.15.3.1

1.4.0.0		Upgraded PDF library to version 6.30.34.99
		Added preflight function
1.4.0.1		Added functionality to add multiple PDF files
		Upgraded PDF library to version 6.30.45.132
1.4.0.2		Added option to display common dialogs
		Fixed bug where save/conformance errors were not displayed properly

1.5.0.0		Upgraded PDF library to version 6.30.51.155
		Improved PDF conversion and output file generation
		Improved display of commandline options
1.5.0.1		Upgraded PDF library to version 6.40.1.1
		Added option to create PDF/A-2
1.5.0.2		Upgraded PDF library to version 6.40.1.2
		Fixed bug where image input failed
1.5.0.3		Fixed bug where pdf input failed in demo mode
1.5.0.4		Extended COM interface
		Added option to create PDF/A-3
		Upgraded PDF library to version 6.40.5.14
		Added EMF/WMF support
		Added support for EXIF rotation
		Upgraded image processing library to version 3.15.3.4
1.5.0.5		Upgraded PDF library to version 6.40.10.26
		Upgraded image processing library to 3.15.3.6
		Improved handling of low memory conditions
1.5.0.6		Removed image transparency in PDF/A-1 mode
		Upgraded PDF library to version 6.40.11.28
		Fixed file sorting
		Added relative path placeholder for commandline pattern option
1.5.0.7		Optimized handling of pdf transparency in PDF/A-1 mode for PDF input files
		Optimized compression in PDF/A-1 mode for PDF input files
		Upgraded PDF library to version 6.40.13.36
		Added parameter pagewidth, pageheight, resolution, saveoptions to commandline and COM interface
		Added parameter deletesource to commandline interface
		Improved error messages
		Improved handling of broken pdf files
		Added option to handle numeric options in hexadecimal notation in commandline interface
1.5.0.8		Fixed bug where rasterization of transparencies was active for PDF/A-2 conversion
		Upgraded PDF library to version 6.40.24.62
		Added support for JBIG2 compression
		Added option to force best compression

1.6.0.0		Added support for PDF/A-1a, PDF/A-2a, PDF/A-2u, PDF/A-3a and PDF/A-3u creation  
		Upgraded PDF library to version 6.40.30.90
1.6.0.1		Fixed bug where omitting the label parameter when calling Engine.AddImage raised an error
		Improved default options for PDF conversion
		Upgraded PDF library to version 6.40.72.208
1.6.0.2		Upgraded PDF library to version 6.40.94.265
		Added PDF/A-4 support
		Added font replacement preset list

1.7.0.0		Added option to overide license information via api
		Upgraded PDF library to version 6.40.102.294
