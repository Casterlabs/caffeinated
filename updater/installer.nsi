OutFile "dist\Caffeinated-Installer.exe"

InstallDir $PROGRAMFILES64\Casterlabs\Caffeinated
RequestExecutionLevel admin

# Installer
Section
	SetOutPath $INSTDIR

	# Create the uninstaller
	WriteUninstaller "$INSTDIR\Uninstall.exe"

	# Create shortcuts
	CreateShortcut "$SMPROGRAMS\Casterlabs\Casterlabs Caffeinated.lnk" "$INSTDIR\Casterlabs-Caffeinated.exe"
	CreateShortcut "$SMPROGRAMS\Casterlabs\Uninstall Caffeinated.lnk" "$INSTDIR\Uninstall.exe"

	# Add files
	File /r "dist\windows\*"
SectionEnd
 
# Uninstaller
Section "uninstall"
	# First, delete the uninstaller
	Delete "$INSTDIR\Uninstall.exe"
 
	# Second, remove the shortcuts
	Delete "$SMPROGRAMS\Casterlabs\Casterlabs Caffeinated.lnk"
	Delete "$SMPROGRAMS\Casterlabs\Uninstall Caffeinated.lnk"

	# Lastly, wipe the directory
	Delete $INSTDIR
SectionEnd